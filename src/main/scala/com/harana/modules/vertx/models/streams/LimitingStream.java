package com.harana.modules.vertx.models.streams;

import io.vertx.core.Handler;
import io.vertx.core.impl.Arguments;
import io.vertx.core.streams.ReadStream;

import java.util.Objects;

/**
 * @author Thomas Segismont
 */
public final class LimitingStream<T> implements ReadStream<T> {

  private final ReadStream<T> source;
  private final long limit;

  private long received;
  private boolean stopped;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> endHandler;

  public LimitingStream(ReadStream<T> source, long limit) {
    Objects.requireNonNull(source, "Source cannot be null");
    Arguments.require(limit >= 0, "Limit must be positive");
    this.source = source;
    this.limit = limit;
  }

  @Override
  public synchronized ReadStream<T> exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  private synchronized Handler<Throwable> getExceptionHandler() {
    return exceptionHandler;
  }

  @Override
  public ReadStream<T> handler(Handler<T> handler) {
    if (handler == null) {
      source.handler(null);
      return this;
    }
    source
      .exceptionHandler(throwable -> notifyTerminalHandler(getExceptionHandler(), throwable))
      .endHandler(v -> notifyTerminalHandler(getEndHandler(), null))
      .handler(item -> {
        boolean emit, terminate;
        synchronized (this) {
          received++;
          emit = !stopped && received <= limit;
          terminate = !stopped && ((received == 1 && limit == 0) || received == limit);
        }
        if (emit) {
          handler.handle(item);
        }
        if (terminate) {
          notifyTerminalHandler(getEndHandler(), null);
        }
      });
    return this;
  }

  @Override
  public ReadStream<T> pause() {
    source.pause();
    return this;
  }

  @Override
  public ReadStream<T> resume() {
    return fetch(Long.MAX_VALUE);
  }

  @Override
  public ReadStream<T> fetch(long l) {
    source.fetch(l);
    return this;
  }

  @Override
  public synchronized ReadStream<T> endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }

  private synchronized Handler<Void> getEndHandler() {
    return endHandler;
  }

  private <V> void notifyTerminalHandler(Handler<V> handler, V value) {
    Handler<V> h;
    synchronized (this) {
      if (!stopped) {
        stopped = true;
        source.handler(null).exceptionHandler(null).endHandler(null);
        h = handler;
      } else {
        h = null;
      }
    }
    if (h != null) {
      h.handle(value);
    }
  }
}