/*******************************************************************************
 * Copyright (c) 2015 Ericsson, EfficiOS Inc., and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.collect;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.common.core.Activator;

/**
 * A BufferedBlockingQueue is a wrapper around a {@link BlockingQueue}, which
 * provides input and output "buffers", so that chunks of elements are inserted
 * into the output buffer, rather than individual elements.
 * <p>
 * The API provides usual put() and take() methods which work on single
 * elements. This class abstracts the concept of chunking, as well as the
 * required locking, from the users.
 * <p>
 * The main use case is for when different threads are doing insertion and
 * removal operations. The added buffering reduces the contention between those
 * two threads.
 *
 * @param <T>
 *            The data type of the elements contained by the queue
 * @since 1.0
 */
public class BufferedBlockingQueue<T> implements Iterable<T> {

    private static final String BUFFERED_QUEUE_INTERRUPTED = "Buffered queue interrupted"; //$NON-NLS-1$
    private final BlockingDeque<Deque<T>> fInnerQueue;
    private final Lock fInputLock = new ReentrantLock();
    private final Lock fOutputLock = new ReentrantLock();
    private final int fChunkSize;

    private Deque<T> fInputBuffer;
    private Deque<T> fOutputBuffer;

    /*
     * ConcurrentLinkedDeque's size() method does not run in constant time.
     * Since we know we will only increment it, keep track of the number of
     * insertions ourselves. It does not matter if the actual size is not exact.
     */
    private int fInputBufferSize;

    private final AtomicInteger fSize = new AtomicInteger(0);

    private final Condition fInnerQueueNotEmpty = checkNotNull(fOutputLock.newCondition());

    /**
     * Constructor
     *
     * @param queueSize
     *            The size of the actual blocking queue. This is the number of
     *            additional *chunks* that will go in the queue. The value must
     *            be larger or equal to 0.
     * @param chunkSize
     *            The size of an individual chunk. The value must be larger than
     *            0.
     */
    public BufferedBlockingQueue(int queueSize, int chunkSize) {
        if (queueSize < 0) {
            throw new IllegalArgumentException("queueSize must be >= 0"); //$NON-NLS-1$
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be > 0"); //$NON-NLS-1$
        }
        /* Add one to the queue size for the output buffer */
        fInnerQueue = new LinkedBlockingDeque<>(queueSize + 1);
        fChunkSize = chunkSize;

        fInputBuffer = new ConcurrentLinkedDeque<>();
        /*
         * Create an empty output buffer to avoid a null reference, and add it
         * to the queue. The output buffer is always the head of the queue.
         */
        fOutputBuffer = new ConcurrentLinkedDeque<>();
        fInnerQueue.add(fOutputBuffer);
    }

    /**
     * Put an element at the tail of the queue.
     * <p>
     * This method will block the caller if the output buffer is full, waiting
     * for space to become available.
     *
     * @param element
     *            The element to insert
     */
    public void put(T element) {
        fInputLock.lock();
        try {
            fInputBuffer.add(element);
            fSize.incrementAndGet();
            fInputBufferSize++;
            if (fInputBufferSize >= fChunkSize) {
                this.flushInputBuffer();
            }
        } finally {
            fInputLock.unlock();
        }
    }

    /**
     * Flush the current input buffer, disregarding the expected buffer size
     * limit.
     * <p>
     * This will guarantee that an element that was inserted via the
     * {@link #put} method becomes visible to the {@link #take} method.
     *
     * This method will block if the output buffer is currently full, waiting
     * for space to become available.
     */
    public void flushInputBuffer() {
        boolean signal = false;
        fInputLock.lock();
        try {
            /*
             * This call blocks if the inner queue is full, effectively blocking
             * the caller until elements are removed via the take() method.
             */
            if (!fInputBuffer.isEmpty()) {
                fInnerQueue.put(fInputBuffer);
                fInputBuffer = new ConcurrentLinkedDeque<>();
                fInputBufferSize = 0;
                signal = true;
            }

        } catch (InterruptedException e) {
            Activator.instance().logError(BUFFERED_QUEUE_INTERRUPTED, e);
            Thread.currentThread().interrupt();
        } finally {
            fInputLock.unlock();
        }
        if (signal) {
            fOutputLock.lock();
            try {
                fInnerQueueNotEmpty.signalAll();
            } finally {
                fOutputLock.unlock();
            }
        }
    }

    /**
     * Retrieve the head element from the queue.
     * <p>
     * If the output buffer is empty, this call will block until an element is
     * inserted and fills the input buffer, or until the not-empty input buffer
     * is otherwise manually flushed.
     *
     * @return The retrieved element. It will be removed from the queue.
     */
    public T take() {
        fOutputLock.lock();
        try {
            if (fOutputBuffer.isEmpty()) {
                /*
                 * Our read buffer is empty, remove it from the queue and peek
                 * the next buffer in the queue. The loop will block if the
                 * inner queue is empty, releasing the lock while it waits.
                 */
                Deque<T> value = fInnerQueue.remove();
                if (!value.isEmpty()) {
                    Activator.instance().logError("Queue chunk not empty " + value); //$NON-NLS-1$
                }
                while (fInnerQueue.isEmpty()) {
                    fInnerQueueNotEmpty.await();
                }
                fOutputBuffer = checkNotNull(fInnerQueue.peek());
            }
            /* Our implementation guarantees this output buffer is not empty. */
            T element = checkNotNull(fOutputBuffer.remove());
            fSize.decrementAndGet();
            return element;
        } catch (InterruptedException e) {
            Activator.instance().logError(BUFFERED_QUEUE_INTERRUPTED, e);
            Thread.currentThread().interrupt();
            // won't happen
            throw new IllegalStateException(e);
        } finally {
            fOutputLock.unlock();
        }
    }

    /**
     * Retrieve, but do not remove, the head element of this queue.
     * <p>
     * If the output buffer is empty, this call will block until an element is
     * inserted and fills the input buffer, or until the not-empty input buffer
     * is otherwise manually flushed.
     *
     * @return The head element of this queue, blocking until one is available
     * @since 1.1
     */
    public T blockingPeek() {
        fOutputLock.lock();
        try {
            if (fOutputBuffer.isEmpty()) {
                /*
                 * Our read buffer is empty, remove it from the queue and peek
                 * the next buffer in the queue. The loop will block if the
                 * inner queue is empty, releasing the lock while it waits.
                 */
                Deque<T> value = fInnerQueue.remove();
                if (!value.isEmpty()) {
                    Activator.instance().logError("Queue chunk not empty " + value); //$NON-NLS-1$
                }
                while (fInnerQueue.isEmpty()) {
                    fInnerQueueNotEmpty.await();
                }
                fOutputBuffer = checkNotNull(fInnerQueue.peek());
            }
            /* Our implementation guarantees this output buffer is not empty. */
            return checkNotNull(fOutputBuffer.peek());
        } catch (InterruptedException e) {
            Activator.instance().logError(BUFFERED_QUEUE_INTERRUPTED, e);
            Thread.currentThread().interrupt();
            // won't happen
            throw new IllegalStateException(e);
        } finally {
            fOutputLock.unlock();
        }
    }

    /**
     * Returns true if the queue size is 0.
     *
     * @return true if the queue is empty
     */
    public boolean isEmpty() {
        return (fSize.get() == 0);
    }

    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue
     * @since 1.1
     */
    public int size() {
        return fSize.get();
    }

    /**
     * Instantiate an iterator on the complete data structure. This includes the
     * input buffer as well as the output buffer. The elements will be returned
     * in order from last (tail) to first (head).
     * <p>
     * If concurrent removals happen while the iterator is being used, it is
     * possible for an element that was actually in the queue when the call was
     * made to have been removed by the {@link #take} method in the meantime.
     * However, this iterator guarantees that each element is either inside the
     * queue OR was removed by the {@link #take} method. No element should
     * "fall in the cracks".
     * <p>
     * The iterator itself is not safe to use concurrently by different threads.
     * <p>
     * The {@link Iterator#remove()} operation is not supported by this
     * iterator.
     *
     * @return An iterator over the whole buffered queue in reverse sequence
     */
    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<T> {
        /*
         * Note that the iterators of LinkedBlockingDeque and
         * ConcurrentLinkedDeque are thread-safe, which allows iterating on them
         * while they are being modified without having to lock the accesses.
         *
         * To make sure we do not "miss" any elements, we need to look through
         * the input buffer first, then the inner queue buffers in descending
         * order, ending with the output buffer.
         */
        private @Nullable T fNext = null;
        private Iterator<T> fBufferIterator;
        private final Iterator<Deque<T>> fQueueIterator;

        Itr() {
            fInputLock.lock();
            try {
                fBufferIterator = checkNotNull(fInputBuffer.descendingIterator());
                fQueueIterator = checkNotNull(fInnerQueue.descendingIterator());
            } finally {
                fInputLock.unlock();
            }
        }

        @Override
        public boolean hasNext() {
            if (fNext != null) {
                return true;
            }
            if (fBufferIterator.hasNext()) {
                fNext = fBufferIterator.next();
                return true;
            }
            if (fQueueIterator.hasNext()) {
                fBufferIterator = checkNotNull(fQueueIterator.next().descendingIterator());
                return hasNext();
            }
            return false;
        }

        @Override
        public T next() {
            if (hasNext()) {
                @Nullable T next = fNext;
                if (next != null) {
                    fNext = null;
                    return next;
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
