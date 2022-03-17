package no.entur.android.nfc.tcpserver;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class CommandInputOutputThread<T, S> extends Thread implements Closeable {

    public interface Listener<T, S> {
        void onReaderStart(CommandInputOutputThread<T, S> reader);
        void onReaderCommand(CommandInputOutputThread<T, S> reader, T input) throws IOException;
        void onReaderClosed(CommandInputOutputThread<T, S> reader, Exception e);
    }

    // wrapper to make the reader thread pass the result back to the writer thread
    public static class PendingInputConsumer<T> {

        private T in;
        private boolean closed;

        public void waitClosed() throws InterruptedException {
            synchronized (this) {
                while(!closed) {
                    wait();
                }
            }
        }

        // TODO could add some kind of check that the input is indeed a response to the output
        public void close(T in) {
            synchronized (this) {
                this.in = in;

                this.closed = true;
                notifyAll();
            }
        }

        public T getIn() {
            return in;
        }
    }

    private final Listener<T, S> listener;
    private final Socket clientSocket;
    private final CommandOutput<S> out;
    private final CommandInput<T> in;
    private boolean closed;

    private PendingInputConsumer<T> pendigInputConsumer;

    public CommandInputOutputThread(Listener<T, S> listener, Socket clientSocket, CommandOutput<S> out, CommandInput<T> in) {
        this.listener = listener;
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
                synchronized(this) {
                    pending = pendigInputConsumer;
                }
                if(pending == null) {
                    listener.onReaderCommand(this, next);
                } else {
                    pending.close(next);
                    // i.e. do not invoke listener for this
                }
            }
            listener.onReaderClosed(this, null);
        } catch (Exception e) {
            listener.onReaderClosed(this, e);

            if(closed) {
                listener.onReaderClosed(this, null);
            } else {
                listener.onReaderClosed(this, e);
            }
        }
    }

    public T outputInput(S output) throws IOException, InterruptedException {
        PendingInputConsumer<T> consumer = new PendingInputConsumer<>();
        synchronized(this) {
            this.pendigInputConsumer = consumer;
            try {
                out.write(output);

                consumer.waitClosed();
            } finally {
                this.pendigInputConsumer = null;
            }
        }
        return consumer.getIn();
    }

    public void output(S output) throws IOException {
        synchronized(this) {
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
        out.write(command);
    }

}
