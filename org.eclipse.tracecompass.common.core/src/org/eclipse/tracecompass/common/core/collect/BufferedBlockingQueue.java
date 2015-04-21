/*******************************************************************************
 * Copyright (c) 2015 Ericsson, EfficiOS Inc., and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.collect;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.tracecompass.internal.common.core.Activator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * A BufferedBlockingQueue is a wrapper around a {@link ArrayBlockingQueue},
 * which provides input and output "buffers", so that chunks of elements are
 * inserted into the queue, rather than individual elements.
 *
 * The API provides usual put() and take() methods which work on single
 * elements. This class abstracts the concept of chunking, as well as the
 * required locking, from the users
 *
 * The main use case is for when different threads are doing insertion and
 * removal operations. The added buffering reduces the contention between those
 * two threads.
 *
 * @param <T>
 *            The data type of the elements contained by the queue
 * @since 1.0
 */
public class BufferedBlockingQueue<T> implements Iterable<T> {

    private final BlockingQueue<Deque<T>> fInnerQueue;
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

    /**
     * Constructor
     *
     * @param queueSize
     *            The size of the actual blocking queue. This is the number of
     *            *chunks* that will go in the queue.
     * @param chunkSize
     *            The size of an individual chunk.
     */
    public BufferedBlockingQueue(int queueSize, int chunkSize) {
        fInnerQueue = new ArrayBlockingQueue<>(queueSize);
        fChunkSize = chunkSize;

        fInputBuffer = new ConcurrentLinkedDeque<>();
        /*
         * Set fOutputBuffer to something to avoid a null reference, even though
         * this particular object will never be used.
         */
        fOutputBuffer = new ConcurrentLinkedDeque<>();
    }

    /**
     * Put an element into the queue.
     *
     * This method will block the caller if the inner queue is full, waiting for
     * space to become available.
     *
     * @param element
     *            The element to insert
     */
    public void put(T element) {
        fInputLock.lock();
        try {
            fInputBuffer.addFirst(element);
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
     *
     * This will guarantee that an element that was inserted via the
     * {@link #put} method becomes visible to the {@link #take} method.
     *
     * This method will block if the inner queue is currently full, waiting for
     * space to become available.
     */
    public void flushInputBuffer() {
        fInputLock.lock();
        try {
            /*
             * This call blocks if fInputBuffer is full, effectively blocking
             * the caller until elements are removed via the take() method.
             */
            if (!fInputBuffer.isEmpty()) {
                fInnerQueue.put(fInputBuffer);
                fInputBuffer = new ConcurrentLinkedDeque<>();
                fInputBufferSize = 0;
            }

        } catch (InterruptedException e) {
            Activator.instance().logError("Buffered queue interrupted", e); //$NON-NLS-1$
        } finally {
            fInputLock.unlock();
        }
    }

    /**
     * Retrieve an element from the queue.
     *
     * If the queue is empty, this call will block until an element is inserted.
     *
     * @return The retrieved element. It will be removed from the queue.
     */
    public T take() {
        fOutputLock.lock();
        try {
            if (fOutputBuffer.isEmpty()) {
                /*
                 * Our read buffer is empty, take the next buffer in the queue.
                 * This call will block if the inner queue is empty.
                 */
                fOutputBuffer = checkNotNull(fInnerQueue.take());
            }
            return checkNotNull(fOutputBuffer.removeLast());
        } catch (InterruptedException e) {
            Activator.instance().logError("Buffered queue interrupted", e); //$NON-NLS-1$
            throw new IllegalStateException();
        } finally {
            fOutputLock.unlock();
        }
    }

    /**
     * Does the queue contain at least one element?
     *
     * @return if the queue is empty
     */
    public boolean isEmpty() {
        /*
         * All three isEmpty()s are very fast, but we are hoping it
         * short-circuits on the first two since it would not make sense to have
         * an empty front and back and a full middle.
         */
        return (fInputBuffer.isEmpty() && fOutputBuffer.isEmpty() && fInnerQueue.isEmpty());
    }

    /**
     * Instantiate an iterator on the complete data structure. This includes the
     * inner queue as well as the input and output buffers.
     *
     * If concurrent insertions happen while the iterator is being used, it is
     * possible for an element that was actually in the queue when the call was
     * made to have been removed by the {@link #take} method in the meantime.
     * However, this iterator guarantees that each element is either inside the
     * queue OR was removed by the {@link #take} method. No element should
     * "fall in the cracks".
     *
     * @return An iterator over the whole buffered queue
     */
    @Override
    public Iterator<T> iterator() {
        /*
         * Note that the iterators of ArrayBlockingQueue and
         * ConcurrentLinkedDeque are thread-safe, which allows iterating on them
         * while they are being modified without having to lock the accesses.
         *
         * To make sure we do not "miss" any elements, we need to look through
         * the input buffer first, then the inner queue, then the output buffer.
         */

        Iterator<T> inputIterator = fInputBuffer.iterator();
        Iterator<T> queueIterator = Iterables.concat(fInnerQueue).iterator();
        Iterator<T> outputIterator = fOutputBuffer.iterator();

        return checkNotNull(Iterators.concat(inputIterator, queueIterator, outputIterator));
    }

}
