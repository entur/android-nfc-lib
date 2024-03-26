package no.entur.android.nfc.tcpserver;

import java.io.Closeable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandInputOutputThreadListenerWrapper<T, S> implements CommandInputOutputThread.Listener<T, S>, Closeable {

    protected final CommandInputOutputThread.Listener<T, S> delegate;
    protected final ExecutorService executor;

    public CommandInputOutputThreadListenerWrapper(CommandInputOutputThread.Listener<T, S> delegate) {
        // the underlying structure does not really work well in parallel, so default to a single thread
        this(delegate, Executors.newSingleThreadExecutor());
    }

    public CommandInputOutputThreadListenerWrapper(CommandInputOutputThread.Listener<T, S> delegate, ExecutorService executor) {
        this.delegate = delegate;
        this.executor = executor;
    }

    @Override
    public void onReaderStart(CommandInputOutputThread<T, S> reader) {
        executor.submit(() -> delegate.onReaderStart(reader));
    }

    @Override
    public void onReaderCommand(CommandInputOutputThread<T, S> reader, T input) {
        executor.submit(() -> delegate.onReaderCommand(reader, input));
    }

    @Override
    public void onReaderClosed(CommandInputOutputThread<T, S> reader, Exception e) {
        executor.submit(() -> {
            try {
                delegate.onReaderClosed(reader, e);
            } finally {
                close();
            }
        });
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
