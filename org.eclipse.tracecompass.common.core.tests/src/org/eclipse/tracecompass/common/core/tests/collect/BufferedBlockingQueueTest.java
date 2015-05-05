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

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.common.core.collect.BufferedBlockingQueue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterators;

/**
 * Test suite for the {@link BufferedBlockingQueue}
 */
public class BufferedBlockingQueueTest {

    /** Timeout the tests after 2 minutes */
    @Rule
    public TestRule timeoutRule = new Timeout(120000);

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
                Integer testInt = NonNullUtils.checkNotNull(rnd.nextInt());
                Long testLong = NonNullUtils.checkNotNull(rnd.nextLong());
                Double testDouble = NonNullUtils.checkNotNull(rnd.nextDouble());
                Double testGaussian = NonNullUtils.checkNotNull(rnd.nextGaussian());

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
     *
     * @throws InterruptedException
     *             The test was interrupted
     * @throws ExecutionException
     *             If one of the sub-threads throws an exception, which should
     *             not happen
     */
    @Test
    public void testConcurrentIteration() throws InterruptedException, ExecutionException {
        final BufferedBlockingQueue<String> queue = new BufferedBlockingQueue<>(15, 15);

        ExecutorService pool = Executors.newFixedThreadPool(3);

        final String poisonPill = "That's all folks!";

        Runnable producer = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < testString.length(); i++) {
                    queue.put(nullToEmptyString(String.valueOf(testString.charAt(i))));
                }
                queue.put(poisonPill);
                queue.flushInputBuffer();
            }
        };

        Callable<String> consumer = new Callable<String>() {
            @Override
            public String call() {
                StringBuilder sb = new StringBuilder();
                String s = queue.take();
                while (!s.equals(poisonPill)) {
                    sb.append(s);
                    s = queue.take();
                }
                return sb.toString();
            }
        };

        Runnable inquisitor = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    final Set<String> results = new HashSet<>();
                    /*
                     * The interest of this test is here: we are iterating on
                     * the queue while it is being used.
                     */
                    for (String input : queue) {
                        results.add(input);
                    }
                }
            }
        };

        pool.submit(producer);
        pool.submit(inquisitor);
        Future<String> message = pool.submit(consumer);

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.MINUTES);

        assertEquals(testString, message.get());
    }


}