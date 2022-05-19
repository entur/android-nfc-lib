package no.entur.android.nfc.tcpserver;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class CommandInputOutputThread<T, S> extends Thread implements Closeable {

    public interface Listener<T, S> {
        void onReaderStart(CommandInputOutputThread<T, S> reader);
        void onReaderCommand(CommandInputOutputThread<T, S> reader, T input);
        void onReaderClosed(CommandInputOutputThread<T, S> reader, Exception e);
    }

    // wrapper to make the reader thread pass the result back to the writer thread
    public static class PendingInputConsumer<T> {

        private T in;
        private boolean closed;
        private Object ioLock;

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

        // TODO could add some kind of check that the input is indeed a response to the output
        public void close(T in) {
            synchronized (ioLock) {
                this.in = in;

                this.closed = true;
                ioLock.notifyAll();
            }
        }

        public T getIn() {
            return in;
        }
    }

    //private String readerId;
    private final CommandInputOutputThreadListenerWrapper<T, S> listener;
    private final Socket clientSocket;
    private final CommandOutput<S> out;
    private final CommandInput<T> in;
    private boolean closed;

    private Object pendingInputLock = new Object();

    // only one in-flight message at a time (one-shot output or output/input).
    private Object readWriteLock = new Object();

    private PendingInputConsumer<T> pendigInputConsumer;

    public CommandInputOutputThread(Listener<T, S> listener, Socket clientSocket, CommandOutput<S> out, CommandInput<T> in) {
        // wrap all listeners in a dispatch thread, because subsequent command interaction would otherwise be blocked
        this.listener = new CommandInputOutputThreadListenerWrapper<>(listener);
        this.clientSocket = clientSocket;
        this.out = out;
        this.in = in;
    }

    public void run() {
        listener.onReaderStart(this);
        try {
            while(!closed) {
                T next = in.read();
                PendingInputConsumer<T> pending;

                synchronized(pendingInputLock) {
                    pending = pendigInputConsumer;
                }
                if(pending == null) {
                    /*if(next instanceof String && ((String) next).contains("UID")) {
                        readerId = ((String) next).substring(0, ((String) next).indexOf(";"));
                        System.out.println(readerId);
                    }*/
                    listener.onReaderCommand(this, next);
                } else {
                    pending.close(next);
                    // i.e. do not invoke listener for this
                }
            }
            listener.onReaderClosed(this, null);
        } catch (Exception e) {
            if(closed) {
                listener.onReaderClosed(this, null);
            } else {
                listener.onReaderClosed(this, e);
            }
        }
    }

    public T outputInput(S output) throws IOException, InterruptedException {
        PendingInputConsumer<T> consumer = new PendingInputConsumer<>(pendingInputLock);
        synchronized (readWriteLock) {
            synchronized (pendingInputLock) {
                try {
                    this.pendigInputConsumer = consumer;

                    out.write(output);

                    consumer.waitClosed(); // releases pendingInputLock but holds on to readWriteLock
                } finally {
                    this.pendigInputConsumer = null;
                }
            }
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
    }

    public void write(S command) throws IOException {
        synchronized(readWriteLock) {
            out.write(command);
        }
    }

/*    public String getReaderId() {
        return readerId;
    }*/
}
