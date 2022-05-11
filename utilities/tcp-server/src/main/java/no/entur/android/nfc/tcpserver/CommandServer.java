package no.entur.android.nfc.tcpserver;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CommandServer {

    public interface Listener {
        void onServerSocketStart(int port);
        void onServerSocketConnection(int port, Socket socket) throws IOException;
        void onServerSocketClosed(int port, Exception e);
    }

    protected static class CommandServerThread extends Thread implements Closeable {

        protected final Listener listener;
        protected final int port;
        protected boolean closed;
        protected ServerSocket serverSocket;

        public CommandServerThread(Listener listener, int port) {
            this.listener = listener;
            this.port = port;
        }

        @Override
        public void run() {
            listener.onServerSocketStart(port);
            try {
                serverSocket = new ServerSocket(port);
                while(!closed) {
                    Socket accept = serverSocket.accept();
                    listener.onServerSocketConnection(serverSocket.getLocalPort(), accept);
                }
                listener.onServerSocketClosed(port, null);
            } catch (Exception e) {
                if(closed) {
                    listener.onServerSocketClosed(port, null);
                } else {
                    listener.onServerSocketClosed(port, e);
                }
            }
        }

        @Override
        public void close() throws IOException {
            if(!closed) {
                closed = true;
                if(serverSocket != null) {
                    serverSocket.close();
                }
            }
        }
    }

    protected CommandServerThread thread;
    protected final Listener listener;
    protected final int port;

    public CommandServer(Listener listener, int port) {
        this.listener = listener;
        this.port = port;
    }

    public void stop() throws IOException {
        synchronized (this) {
            if(thread != null) {
                thread.close();
                thread = null;

                // not waiting for thread to stop
            }
        }
    }

    public void start() {
        synchronized (this) {
            if (thread == null) {
                thread = new CommandServerThread(listener, port);
                thread.start();
            }
        }
    }
}
