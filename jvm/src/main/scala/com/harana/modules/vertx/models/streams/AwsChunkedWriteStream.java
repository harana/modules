package com.harana.modules.vertx.models.streams;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.reactivestreams.ReactiveWriteStream;
import org.reactivestreams.Subscriber;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AwsChunkedWriteStream implements ReactiveWriteStream<Buffer> {
    private static final Logger log = LoggerFactory.getLogger(AwsChunkedReadStream.class);

    private ReactiveWriteStream<Buffer> delegate;
    
    private int headerEndPos = -1;
    private int chunkEndPos = -1;

    private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DELIMITER = ";".getBytes(StandardCharsets.UTF_8);

    private Handler<Throwable> exceptionHandler;

    private Buffer buffer = Buffer.buffer();

    public AwsChunkedWriteStream(ReactiveWriteStream<Buffer> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Future<Void> write(Buffer data) {
        Buffer payload = availableData(data);
        return delegate.write(payload);
    }

    @Override
    public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
        Buffer payload = availableData(data);
        delegate.write(payload, handler);
    }

    @Override
    public ReactiveWriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
        return delegate.setWriteQueueMaxSize(maxSize);
    }

    @Override
    public boolean writeQueueFull() {
        return delegate.writeQueueFull();
    }

    @Override
    public ReactiveWriteStream<Buffer> drainHandler(Handler<Void> handler) {
        delegate.drainHandler(handler);
        return this;
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        delegate.end(handler);
    }

    @Override
    public ReactiveWriteStream<Buffer> close() {
        delegate.close();
        return this;
    }

    @Override
    public void subscribe(Subscriber<? super Buffer> s) {
        delegate.subscribe(s);
    }

    @Override
    public AwsChunkedWriteStream exceptionHandler(Handler<Throwable> handler) {
        exceptionHandler = handler;
        delegate.exceptionHandler(handler);
        return this;
    }

    private Buffer availableData(Buffer data) {
      try {
        buffer.appendBuffer(data);

        if (headerEndPos == -1) {
          int delimiterPos = countUntil(buffer, DELIMITER, 0) + DELIMITER.length;
          headerEndPos = countUntil(buffer, CRLF, delimiterPos) + CRLF.length;
        }

        if (headerEndPos > 0 && chunkEndPos == -1) {
          chunkEndPos = countUntil(buffer, CRLF, headerEndPos);
        }

        if (headerEndPos > 0 && chunkEndPos > 0) {
            System.err.println("Writing: " + headerEndPos + " -- " + chunkEndPos);
            return buffer.slice(headerEndPos + 1, chunkEndPos);
        }

      } catch(Exception e) {
          if (exceptionHandler != null)
              exceptionHandler.handle(e);
          else
              log.error("Unhandled exception", e);
      }
      return null;
    }

    private int countUntil(Buffer data, byte[] sequence, int start) {
        for (int i=start ; (i + sequence.length) < data.length() ; i++) {
            var bytes = data.getBytes(i, i + sequence.length);
            if (Arrays.equals(bytes, sequence)) return i;
        }
        return -1;
      }
}