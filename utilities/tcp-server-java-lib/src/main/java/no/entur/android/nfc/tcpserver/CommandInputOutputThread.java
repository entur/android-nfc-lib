package no.entur.android.nfc.tcpserver;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

public class CommandInputOutputThread<T, S> extends Thread implements Closeable {

    public static final long DEFAULT_RESPONSE_TIMEOUT = 5000;

    public interface Listener<T, S> {
        void onReaderStart(CommandInputOutputThread<T, S> reader);
        void onReaderCommand(CommandInputOutputThread<T, S> reader, T input);
        void onReaderClosed(CommandInputOutputThread<T, S> reader, Exception e);
    }

    // wrapper to make the reader thread pass the result back to the writer thread
    public static class PendingInputConsumer<T> {

        private T in;
        private IOException failure;
        private boolean closed;
        private final Object ioLock;

        public PendingInputConsumer(Object ioLock) {
            this.ioLock = ioLock;
        }

        public void waitClosed() throws InterruptedException {
            synchronized (ioLock) {
                while(!closed) {
                    ioLock.wait();
                }
            }
        }

        /**
         * Wait for input (or failure).
         *
         * @param timeout timeout in milliseconds
         * @return true if closed within the timeout
         */

        public boolean waitClosed(long timeout) throws InterruptedException {
            synchronized (ioLock) {
                long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
                while(!closed) {
                    long remaining = deadline - System.nanoTime();
                    if(remaining <= 0) {
                        return false;
                    }
                    TimeUnit.NANOSECONDS.timedWait(ioLock, remaining);
                }
                return true;
            }
        }

        /**
         * Complete with input. Ignored if already completed.
         *
         * @param in input
         */

        public void close(T in) {
            synchronized (ioLock) {
                if(closed) {
                    return;
                }
                this.in = in;

                this.closed = true;
                ioLock.notifyAll();
            }
        }

        /**
         * Complete with a failure. Ignored if already completed, so that a disconnect right after
         * the response does not overwrite the response.
         *
         * @param failure failure
         */

        public void fail(IOException failure) {
            synchronized (ioLock) {
                if(closed) {
                    return;
                }
                this.failure = failure;

                this.closed = true;
                ioLock.notifyAll();
            }
        }

        public boolean isClosed() {
            synchronized (ioLock) {
                return closed;
            }
        }

        public T getIn() {
            return in;
        }

        public IOException getFailure() {
            return failure;
        }
    }

    private final CommandInputOutputThreadListenerWrapper<T, S> listener;
    private final Socket clientSocket;
    private final CommandOutput<S> out;
    private final CommandInput<T> in;
    private volatile boolean closed;
    private volatile String readerId;
    private final String ip;
    private volatile long responseTimeout = DEFAULT_RESPONSE_TIMEOUT;

    private final Object pendingInputLock = new Object();

    // only one in-flight message at a time (one-shot output or output/input).
    private final Object readWriteLock = new Object();

    private PendingInputConsumer<T> pendigInputConsumer;

    public CommandInputOutputThread(Listener<T, S> listener, Socket clientSocket, CommandOutput<S> out, CommandInput<T> in) {
        // wrap all listeners in a dispatch thread, because subsequent command interaction would otherwise be blocked
        this.listener = new CommandInputOutputThreadListenerWrapper<>(listener);
        this.clientSocket = clientSocket;
        this.out = out;
        this.in = in;

        this.ip = clientSocket.getInetAddress().toString().split("/")[1];
    }

    public void run() {
        listener.onReaderStart(this);
        Exception failure = null;
        try {
            while(!closed) {
                T next = in.read();

                boolean response;
                synchronized(pendingInputLock) {
                    // deliver within the lock, so that the input goes either to the current consumer or the listener
                    PendingInputConsumer<T> pending = pendigInputConsumer;
                    // a completed consumer might not yet be cleared by the waiting thread; pass further input to the listener
                    response = pending != null && !pending.isClosed() && isResponse(next);
                    if(response) {
                        pending.close(next);
                    }
                }
                if(!response) {
                    if(next instanceof String && readerId == null) {
                        String r = (String) next;
                        int index = r.indexOf(',');
                        if(index > 0) {
                            readerId = r.substring(0, index);
                        }
                    }
                    listener.onReaderCommand(this, next);
                }
            }
        } catch (Exception e) {
            if(!closed) {
                failure = e;
            }
        } finally {
            // close the socket and fail any pending consumer
            close();
        }
        listener.onReaderClosed(this, failure);
    }

    /**
     * Check whether some input is a response to an output, or an unsolicited message from the client.
     * Unsolicited messages are passed to the listener even if an output is waiting for a response.
     *
     * @param input input from client
     * @return true if input could be a response
     */

    protected boolean isResponse(T input) {
        return true;
    }

    public T outputInput(S output) throws IOException, InterruptedException {
        return outputInput(output, responseTimeout);
    }

    public T outputInput(S output, long timeout) throws IOException, InterruptedException {
        PendingInputConsumer<T> consumer = new PendingInputConsumer<>(pendingInputLock);
        synchronized (readWriteLock) {
            synchronized (pendingInputLock) {
                if(closed) {
                    throw new EOFException("Connection closed");
                }
                try {
                    this.pendigInputConsumer = consumer;

                    out.write(output);

                    // releases pendingInputLock but holds on to readWriteLock
                    if(!consumer.waitClosed(timeout)) {
                        throw new SocketTimeoutException("No response within " + timeout + "ms");
                    }
                } finally {
                    this.pendigInputConsumer = null;
                }
            }
        }
        IOException failure = consumer.getFailure();
        if(failure != null) {
            EOFException e = new EOFException("Connection closed while waiting for response");
            e.initCause(failure);
            throw e;
        }
        return consumer.getIn();
    }

    public void output(S output) throws IOException {
        synchronized(readWriteLock) {
            out.write(output);
        }
    }

    @Override
    public void close()  {
        if(!closed) {
            closed = true;
            try {
                clientSocket.close();
            } catch(Exception e) {
                // ignore
            }
            try {
                in.close();
            } catch(Exception e) {
                // ignore
            }
            try {
                out.close();
            } catch(Exception e) {
                // ignore
            }
        }
        synchronized (pendingInputLock) {
            PendingInputConsumer<T> pending = pendigInputConsumer;
            if(pending != null) {
                pending.fail(new EOFException("Connection closed"));
            }
        }
    }

    public void write(S command) throws IOException {
        synchronized(readWriteLock) {
            out.write(command);
        }
    }

    public String getReaderId() {
        return readerId;
    }

    public String getIp() {
        return ip;
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Set the timeout for waiting for a response in {@link #outputInput(Object)}.
     *
     * @param responseTimeout timeout in milliseconds
     */

    public void setResponseTimeout(long responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public long getResponseTimeout() {
        return responseTimeout;
    }
}
