package com.harana.modules.vertx.models.streams;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.streams.WriteStream;

public class DelegateWriteStream<T> implements WriteStream<T> {

    protected final WriteStream<T> delegate;

    public DelegateWriteStream(WriteStream<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public WriteStream<T> exceptionHandler(@Nullable Handler<Throwable> handler) {
        delegate.exceptionHandler(handler);
        return this;
    }

    @Override
    public Future<Void> write(T data) {
        return delegate.write(data);
    }

    @Override
    public void write(T data, Handler<AsyncResult<Void>> handler) {
        delegate.write(data, handler);
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        delegate.end(handler);
    }

    @Override
    public WriteStream<T> setWriteQueueMaxSize(int maxSize) {
        return delegate.setWriteQueueMaxSize(maxSize);
    }

    @Override
    public boolean writeQueueFull() {
        return delegate.writeQueueFull();
    }

    @Override
    public WriteStream<T> drainHandler(@Nullable Handler<Void> handler) {
        delegate.drainHandler(handler);
        return this;
    }
}
