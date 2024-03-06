package com.harana.modules.vertx.models.streams;

import io.vertx.core.Handler;

import java.util.ArrayDeque;

public class InboundBuffer<E> {

  /**
   * A reusable sentinel for signaling the end of a stream.
   */
  public static final Object END_SENTINEL = new Object();

  private final ArrayDeque<E> pending;
  private final long highWaterMark;
  private long demand;
  private Handler<E> handler;
  private boolean overflow;
  private Handler<Void> drainHandler;
  private Handler<Void> emptyHandler;
  private Handler<Throwable> exceptionHandler;
  private boolean emitting;

  public InboundBuffer() {
    this(16L);
  }

  public InboundBuffer(long highWaterMark) {
    if (highWaterMark < 0) {
      throw new IllegalArgumentException("highWaterMark " + highWaterMark + " >= 0");
    }
    this.highWaterMark = highWaterMark;
    this.demand = Long.MAX_VALUE;
    this.pending = new ArrayDeque<>();
  }

  /**
   * Write an {@code element} to the buffer. The element will be delivered synchronously to the handler when
   * it is possible, otherwise it will be queued for later delivery.
   *
   * @param element the element to add
   * @return {@code false} when the producer should stop writing
   */
  public boolean write(E element) {
    Handler<E> handler;
    synchronized (this) {
      if (demand == 0L || emitting) {
        pending.add(element);
        return checkWritable();
      } else {
        if (demand != Long.MAX_VALUE) {
          --demand;
        }
        emitting = true;
        handler = this.handler;
      }
    }
    handleEvent(handler, element);
    return emitPending();
  }

  private boolean checkWritable() {
    if (demand == Long.MAX_VALUE) {
      return true;
    } else {
      long actual = pending.size() - demand;
      boolean writable = actual < highWaterMark;
      overflow |= !writable;
      return writable;
    }
  }

  /**
   * Write an {@code iterable} of {@code elements}.
   *
   * @see #write(E)
   * @param elements the elements to add
   * @return {@code false} when the producer should stop writing
   */
  public boolean write(Iterable<E> elements) {
    synchronized (this) {
      for (E element : elements) {
        pending.add(element);
      }
      if (demand == 0L || emitting) {
        return checkWritable();
      } else {
        emitting = true;
      }
    }
    return emitPending();
  }

  private boolean emitPending() {
    E element;
    Handler<E> h;
    while (true) {
      synchronized (this) {
        int size = pending.size();
        if (demand == 0L) {
          emitting = false;
          boolean writable = size < highWaterMark;
          overflow |= !writable;
          return writable;
        } else if (size == 0) {
          emitting = false;
          return true;
        }
        if (demand != Long.MAX_VALUE) {
          demand--;
        }
        element = pending.poll();
        h = this.handler;
      }
      handleEvent(h, element);
    }
  }

  /**
   * Drain the buffer.
   * <p/>
   * Calling this assumes {@code (demand > 0L && !pending.isEmpty()) == true}
   */
  private void drain() {
    int emitted = 0;
    Handler<Void> drainHandler;
    Handler<Void> emptyHandler;
    while (true) {
      E element;
      Handler<E> handler;
      synchronized (this) {
        int size = pending.size();
        if (size == 0) {
          emitting = false;
          if (overflow) {
            overflow = false;
            drainHandler = this.drainHandler;
          } else {
            drainHandler = null;
          }
          emptyHandler = emitted > 0 ? this.emptyHandler : null;
          break;
        } else if (demand == 0L) {
          emitting = false;
          return;
        }
        emitted++;
        if (demand != Long.MAX_VALUE) {
          demand--;
        }
        element = pending.poll();
        handler = this.handler;
      }
      handleEvent(handler, element);
    }
    if (drainHandler != null) {
      handleEvent(drainHandler, null);
    }
    if (emptyHandler != null) {
      handleEvent(emptyHandler, null);
    }
  }

  private <T> void handleEvent(Handler<T> handler, T element) {
    if (handler != null) {
      try {
        handler.handle(element);
      } catch (Throwable t) {
        handleException(t);
      }
    }
  }

  private void handleException(Throwable err) {
    Handler<Throwable> handler;
    synchronized (this) {
      if ((handler = exceptionHandler) == null) {
        return;
      }
    }
    handler.handle(err);
  }

  /**
   * Request a specific {@code amount} of elements to be fetched, the amount is added to the actual demand.
   * <p/>
   * Pending elements in the buffer will be delivered asynchronously on the context to the handler.
   * <p/>
   * This method can be called from any thread.
   *
   * @return {@code true} when the buffer will be drained
   */
  public boolean fetch(long amount) {
    if (amount < 0L) {
      throw new IllegalArgumentException();
    }
    synchronized (this) {
      demand += amount;
      if (demand < 0L) {
        demand = Long.MAX_VALUE;
      }
      if (emitting || (pending.isEmpty() && !overflow)) {
        return false;
      }
      emitting = true;
    }
    drain();
    return true;
  }

  /**
   * Read the most recent element synchronously.
   * <p/>
   * No handler will be called.
   *
   * @return the most recent element or {@code null} if no element was in the buffer
   */
  public E read() {
    synchronized (this) {
      return pending.poll();
    }
  }

  /**
   * Clear the buffer synchronously.
   * <p/>
   * No handler will be called.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public synchronized InboundBuffer<E> clear() {
    pending.clear();
    return this;
  }

  /**
   * Pause the buffer, it sets the buffer in {@code fetch} mode and clears the actual demand.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public synchronized InboundBuffer<E> pause() {
    demand = 0L;
    return this;
  }

  /**
   * Resume the buffer, and sets the buffer in {@code flowing} mode.
   * <p/>
   * Pending elements in the buffer will be delivered asynchronously on the context to the handler.
   * <p/>
   * This method can be called from any thread.
   *
   * @return {@code true} when the buffer will be drained
   */
  public boolean resume() {
    return fetch(Long.MAX_VALUE);
  }

  /**
   * Set an {@code handler} to be called with elements available from this buffer.
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  public synchronized InboundBuffer<E> handler(Handler<E> handler) {
    this.handler = handler;
    return this;
  }

  /**
   * Set an {@code handler} to be called when the buffer is drained and the producer can resume writing to the buffer.
   *
   * @param handler the handler to be called
   * @return a reference to this, so the API can be used fluently
   */
  public synchronized InboundBuffer<E> drainHandler(Handler<Void> handler) {
    drainHandler = handler;
    return this;
  }

  /**
   * Set an {@code handler} to be called when the buffer becomes empty.
   *
   * @param handler the handler to be called
   * @return a reference to this, so the API can be used fluently
   */
  public synchronized InboundBuffer<E> emptyHandler(Handler<Void> handler) {
    emptyHandler = handler;
    return this;
  }

  /**
   * Set an {@code handler} to be called when an exception is thrown by an handler.
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  public synchronized InboundBuffer<E> exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  /**
   * @return whether the buffer is empty
   */
  public synchronized boolean isEmpty() {
    return pending.isEmpty();
  }

  /**
   * @return whether the buffer is writable
   */
  public synchronized boolean isWritable() {
    return pending.size() < highWaterMark;
  }

  /**
   * @return whether the buffer is paused, i.e it is in {@code fetch} mode and the demand is {@code 0}.
   */
  public synchronized boolean isPaused() {
    return demand == 0L;
  }

  /**
   * @return the actual number of elements in the buffer
   */
  public synchronized int size() {
    return pending.size();
  }
}