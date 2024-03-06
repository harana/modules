package com.harana.modules.vertx.models.streams;

import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Thomas Segismont
 */
public final class MappingStream<T, R> implements ReadStream<R> {

  private final ReadStream<T> source;
  private final Function<T, R> mapping;

  public MappingStream(ReadStream<T> source, Function<T, R> mapping) {
    Objects.requireNonNull(source, "Source cannot be null");
    Objects.requireNonNull(mapping, "Mapping function cannot be null");
    this.source = source;
    this.mapping = mapping;
  }

  @Override
  public ReadStream<R> exceptionHandler(Handler<Throwable> handler) {
    source.exceptionHandler(handler);
    return this;
  }

  @Override
  public ReadStream<R> handler(Handler<R> handler) {
    if (handler == null) {
      source.handler(null);
    } else {
      source.handler(event -> handler.handle(mapping.apply(event)));
    }
    return this;
  }

  @Override
  public ReadStream<R> pause() {
    source.pause();
    return this;
  }

  @Override
  public ReadStream<R> resume() {
    return fetch(Long.MAX_VALUE);
  }

  @Override
  public ReadStream<R> fetch(long amount) {
    source.fetch(amount);
    return this;
  }

  @Override
  public ReadStream<R> endHandler(Handler<Void> endHandler) {
    source.endHandler(endHandler);
    return this;
  }
}