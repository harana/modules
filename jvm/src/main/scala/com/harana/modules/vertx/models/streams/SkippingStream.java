package com.harana.modules.vertx.models.streams;

import io.vertx.core.Handler;
import io.vertx.core.impl.Arguments;
import io.vertx.core.streams.ReadStream;

import java.util.Objects;

/**
 * @author Thomas Segismont
 */
public final class SkippingStream<T> implements ReadStream<T> {

  private final ReadStream<T> source;
  private final long skip;

  private long skipped;

  public SkippingStream(ReadStream<T> source, long skip) {
    Objects.requireNonNull(source, "Source cannot be null");
    Arguments.require(skip >= 0, "Skip amount must be positive");
    this.source = source;
    this.skip = skip;
  }

  @Override
  public ReadStream<T> exceptionHandler(Handler<Throwable> handler) {
    source.exceptionHandler(handler);
    return this;
  }

  @Override
  public ReadStream<T> handler(Handler<T> handler) {
    if (handler == null) {
      source.handler(null);
      return this;
    }
    source.handler(item -> {
      boolean emit;
      synchronized (this) {
        if (skipped < skip) {
          emit = false;
          skipped++;
        } else {
          emit = true;
        }
      }
      if (emit) {
        handler.handle(item);
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
    long value;
    synchronized (this) {
      if (skipped < skip) {
        if (l < Long.MAX_VALUE - skip + skipped) {
          value = l + skip - skipped;
        } else {
          value = Long.MAX_VALUE;
        }
      } else {
        value = l;
      }
    }
    source.fetch(value);
    return this;
  }

  @Override
  public ReadStream<T> endHandler(Handler<Void> handler) {
    source.endHandler(handler);
    return this;
  }
}