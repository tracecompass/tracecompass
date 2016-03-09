/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.collect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.collect.BufferedBlockingQueue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.primitives.Chars;

/**
 * Test suite for the {@link BufferedBlockingQueue}
 */
public class BufferedBlockingQueueTest {

    /** Timeout the tests after 2 minutes */
    @Rule
    public TestRule timeoutRule = new Timeout(2, TimeUnit.MINUTES);

    private static final String testString = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";

    private BufferedBlockingQueue<Character> charQueue;

    /**
     * Test setup
     */
    @Before
    public void init() {
        charQueue = new BufferedBlockingQueue<>(15, 15);
    }

    /**
     * Test inserting one element and removing it.
     */
    @Test
    public void testSingleInsertion() {
        Character element = 'x';
        charQueue.put(element);
        charQueue.flushInputBuffer();

        Character out = charQueue.take();
        assertEquals(element, out);
    }

    /**
     * Test insertion of elements that fit into the input buffer.
     */
    @Test
    public void testSimpleInsertion() {
        String string = "Hello world!";
        for (char elem : string.toCharArray()) {
            charQueue.put(elem);
        }
        charQueue.flushInputBuffer();

        StringBuilder sb = new StringBuilder();
        while (!charQueue.isEmpty()) {
            sb.append(charQueue.take());
        }
        assertEquals(string, sb.toString());
    }

    /**
     * Test insertion of elements that will require more than one input buffer.
     */
    @Test
    public void testLargeInsertion() {
        String string = testString.substring(0, 222);
        for (char elem : string.toCharArray()) {
            charQueue.put(elem);
        }
        charQueue.flushInputBuffer();

        StringBuilder sb = new StringBuilder();
        while (!charQueue.isEmpty()) {
            sb.append(charQueue.take());
        }
        assertEquals(string, sb.toString());
    }

    /**
     * Test the state of the {@link BufferedBlockingQueue#isEmpty()} method at
     * various moments.
     */
    @Test
    public void testIsEmpty() {
        BufferedBlockingQueue<String> stringQueue = new BufferedBlockingQueue<>(15, 15);
        assertTrue(stringQueue.isEmpty());

        stringQueue.put("Hello");
        assertFalse(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        assertFalse(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        assertFalse(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        stringQueue.take();
        assertTrue(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        assertTrue(stringQueue.isEmpty());
    }

    /**
     * Write random data in and read it, several times.
     */
    @Test
    public void testOddInsertions() {
        BufferedBlockingQueue<Object> objectQueue = new BufferedBlockingQueue<>(15, 15);
        LinkedList<Object> expectedValues = new LinkedList<>();
        Random rnd = new Random();
        rnd.setSeed(123);

        for (int i = 0; i < 10; i++) {
            /*
             * The queue's total size is 225 (15x15). We must make sure to not
             * fill it up here!
             */
            for (int j = 0; j < 50; j++) {
                Integer testInt = rnd.nextInt();
                Long testLong = rnd.nextLong();
                Double testDouble = rnd.nextDouble();
                Double testGaussian = rnd.nextGaussian();

                expectedValues.add(testInt);
                expectedValues.add(testLong);
                expectedValues.add(testDouble);
                expectedValues.add(testGaussian);
                objectQueue.put(testInt);
                objectQueue.put(testLong);
                objectQueue.put(testDouble);
                objectQueue.put(testGaussian);
            }
            objectQueue.flushInputBuffer();

            while (!expectedValues.isEmpty()) {
                Object expected = expectedValues.removeFirst();
                Object actual = objectQueue.take();
                assertEquals(expected, actual);
            }
        }
    }

    /**
     * Read with a producer and a consumer
     *
     * @throws InterruptedException
     *             The test was interrupted
     */
    @Test
    public void testMultiThread() throws InterruptedException {
        /* A character not found in the test string */
        final Character lastElement = '%';

        Thread producer = new Thread() {
            @Override
            public void run() {
                for (char c : testString.toCharArray()) {
                    charQueue.put(c);
                }
                charQueue.put(lastElement);
                charQueue.flushInputBuffer();
            }
        };
        producer.start();

        Thread consumer = new Thread() {
            @Override
            public void run() {
                Character s = charQueue.take();
                while (!s.equals(lastElement)) {
                    s = charQueue.take();
                }
            }
        };
        consumer.start();

        consumer.join();
        producer.join();
    }

    /**
     * Read with a producer and a consumer using
     * {@link BufferedBlockingQueue#blockingPeek()}.
     *
     * @throws InterruptedException
     *             The test was interrupted
     */
    @Test
    public void testBlockingPeek() throws InterruptedException {
        /* A character not found in the test string */
        final Character lastElement = '%';

        final StringBuilder sb = new StringBuilder();

        Thread consumer = new Thread() {
            @Override
            public void run() {
                boolean isFinished = false;
                while (!isFinished) {
                    // Read last element without removing it
                    Character s = charQueue.blockingPeek();
                    isFinished = s.equals(lastElement);
                    if (!isFinished) {
                        sb.append(s);
                    }
                    // Remove element
                    charQueue.take();
                }
            }
        };
        consumer.start();

        Thread producer = new Thread() {
            @Override
            public void run() {
                for (char c : testString.toCharArray()) {
                    charQueue.put(c);
                }
                charQueue.put(lastElement);
                charQueue.flushInputBuffer();
            }
        };
        producer.start();

        producer.join();
        consumer.join();

        assertEquals(testString, sb.toString());
    }

    /**
     * Test the contents returned by {@link BufferedBlockingQueue#iterator()}.
     *
     * The test is sequential, because the iterator has no guarantee wrt to its
     * contents when run concurrently.
     */
    @Test
    public void testIteratorContents() {
        Deque<Character> expected = new LinkedList<>();

        /* Iterator should be empty initially */
        assertFalse(charQueue.iterator().hasNext());

        /* Insert the first 50 elements */
        for (int i = 0; i < 50; i++) {
            char c = testString.charAt(i);
            charQueue.put(c);
            expected.addFirst(c);
        }
        LinkedList<Character> actual = new LinkedList<>();
        Iterators.addAll(actual, charQueue.iterator());
        assertSameElements(expected, actual);

        /*
         * Insert more elements, flush the input buffer (should not affect the
         * iteration).
         */
        for (int i = 50; i < 60; i++) {
            char c = testString.charAt(i);
            charQueue.put(c);
            charQueue.flushInputBuffer();
            expected.addFirst(c);
        }
        actual = new LinkedList<>();
        Iterators.addAll(actual, charQueue.iterator());
        assertSameElements(expected, actual);

        /* Consume the 30 last elements from the queue */
        for (int i = 0; i < 30; i++) {
            charQueue.take();
            expected.removeLast();
        }
        actual = new LinkedList<>();
        Iterators.addAll(actual, charQueue.iterator());
        assertSameElements(expected, actual);

        /* Now empty the queue */
        while (!charQueue.isEmpty()) {
            charQueue.take();
            expected.removeLast();
        }
        assertFalse(charQueue.iterator().hasNext());
    }

    /**
     * Utility method to verify that two collections contain the exact same
     * elements, not necessarily in the same iteration order.
     *
     * {@link Collection#equals} requires the iteration order to be the same,
     * which we do not want here.
     *
     * Using a {@link Set} or {@link Collection#containsAll} is not sufficient
     * either, because those will throw away duplicate elements.
     */
    private static <T> void assertSameElements(Collection<T> c1, Collection<T> c2) {
        assertEquals(HashMultiset.create(c1), HashMultiset.create(c2));
    }

    /**
     * Test iterating on the queue while a producer and a consumer threads are
     * using it. The iteration should not affect the elements taken by the
     * consumer.
     */
    @Test
    public void testConcurrentIteration() {
        final BufferedBlockingQueue<String> queue = new BufferedBlockingQueue<>(15, 15);
        final String poisonPill = "That's all folks!";

        /*
         * Convert the test's testBuffer into an array of String, one for each
         * character.
         */
        List<String> strings = Chars.asList(testString.toCharArray()).stream()
            .map(Object::toString)
            .collect(Collectors.toList());

        Iterable<Iterable<String>> results =
                runConcurrencyTest(queue, strings, poisonPill, 1, 1, 1);

        assertEquals(strings, Iterables.getOnlyElement(results));
    }

    /**
     * Run a concurrency test on a {@link BufferedBlockingQueue}, with the
     * specified number of producer, consumer and observer/iterator threads.
     *
     * The returned value represents the elements consumed by each consumer
     * thread. Thus, if there is one consumer, the top-level {@link Iterable}
     * will be of size 1, and the inner one should contain all the elements that
     * were inserted.
     *
     * @param queue
     *            The queue to run the test on
     * @param testBuffer
     *            The data set to insert in the queue. Every producer will
     *            insert one entire set.
     * @param poisonPill
     *            The "poison pill" to indicate the end. Simply make sure it is
     *            a element of type <T> that is not present in the 'testBuffer'.
     * @param nbProducerThreads
     *            Number of producer threads. There should be at least 1.
     * @param nbConsumerThreads
     *            Number of consumer threads. There should be at least 1.
     * @param nbObserverThreads
     *            Number of observer threads. It should be >= 0.
     * @return The consumed elements, as seen by each consumer thread.
     */
    private static <T> Iterable<Iterable<T>> runConcurrencyTest(final BufferedBlockingQueue<T> queue,
            final List<T> testBuffer,
            final @NonNull T poisonPill,
            int nbProducerThreads,
            int nbConsumerThreads,
            int nbObserverThreads) {

        final class ProducerThread implements Runnable {
            @Override
            public void run() {
                for (int i = 0; i < testBuffer.size(); i++) {
                    T elem = testBuffer.get(i);
                    if (elem == null) {
                        // TODO replace with List<@NonNull T> once we can
                        throw new IllegalArgumentException();
                    }
                    queue.put(elem);
                }
                queue.put(poisonPill);
                queue.flushInputBuffer();
            }
        }

        /**
         * The consumer thread will return the elements it read via its Future.
         *
         * Note that if there are multiple consumers, there is no guarantee with
         * regards the contents of an individual one.
         */
        final class ConsumerThread implements Callable<Iterable<T>> {
            @Override
            public Iterable<T> call() {
                List<T> results = new LinkedList<>();
                T elem = queue.take();
                while (!elem.equals(poisonPill)) {
                    results.add(elem);
                    elem = queue.take();
                }
                return results;
            }
        }

        final class ObserverThread implements Runnable {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    final Set<T> results = new HashSet<>();
                    for (T input : queue) {
                        /*
                         * Do something with the element so that this iteration
                         * does not get optimized out.
                         */
                        results.add(input);
                    }
                }
            }
        }

        if (nbProducerThreads < 1 || nbConsumerThreads < 1 || nbObserverThreads < 0) {
            throw new IllegalArgumentException();
        }

        final ExecutorService pool = Executors.newFixedThreadPool(
                nbProducerThreads + nbConsumerThreads + nbObserverThreads);

        /* Consumed elements, per consumer thread */
        List<Future<Iterable<T>>> consumedElements = new LinkedList<>();

        for (int i = 0; i < nbProducerThreads; i++) {
            pool.submit(new ProducerThread());
        }
        for (int i = 0; i < nbConsumerThreads; i++) {
            consumedElements.add(pool.submit(new ConsumerThread()));
        }
        for (int i = 0; i < nbObserverThreads; i++) {
            pool.submit(new ObserverThread());
        }

        List<Iterable<T>> results = new LinkedList<>();
        try {
            /* Convert the Future's to the actual return value */
            for (Future<Iterable<T>> future : consumedElements) {
                Iterable<T> threadResult = future.get();
                results.add(threadResult);
            }

            pool.shutdown();
            boolean success = pool.awaitTermination(2, TimeUnit.MINUTES);
            if (!success) {
                throw new InterruptedException();
            }

        } catch (ExecutionException | InterruptedException e) {
            fail(e.getMessage());
        }

        return results;
    }

}
