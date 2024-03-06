package com.harana.modules.vertx.models.streams;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AwsChunkedReadStream extends DelegateReadStream<Buffer> {
  private static final Logger log = LoggerFactory.getLogger(AwsChunkedReadStream.class);

  private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.UTF_8);
  private static final byte[] DELIMITER = ";".getBytes(StandardCharsets.UTF_8);

  private int headerEndPos = -1;
  private int chunkEndPos = -1;
  private Handler<Throwable> exceptionHandler;
  private Handler<Void> endHandler;
  private Buffer buffer = Buffer.buffer();

  public AwsChunkedReadStream(ReadStream<Buffer> delegate) {
    super(delegate);
  }

  @Override
  public AwsChunkedReadStream handler(Handler<Buffer> handler) {
    if (handler == null) {
      delegate.handler(null);
      return this;
    }

    delegate.handler(data -> {
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
          handler.handle(buffer.slice(headerEndPos + 1, chunkEndPos));
          if (endHandler != null) endHandler.handle(null);
        }

      } catch(Exception e) {
        if (exceptionHandler != null)
          exceptionHandler.handle(e);
        else
          log.error("Unhandled exception", e);
      }
    });
    return this;
  }

  @Override
  public AwsChunkedReadStream endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    return this;
  }


  @Override
  public AwsChunkedReadStream exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    delegate.exceptionHandler(handler);
    return this;
  }

  private int countUntil(Buffer data, byte[] sequence, int start) {
    for (int i=start ; (i + sequence.length) < data.length() ; i++) {
      var bytes = data.getBytes(i, i + sequence.length);
      if (Arrays.equals(bytes, sequence)) return i;
    }
    return -1;
  }
}
