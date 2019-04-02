/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.tests.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.tracecompass.internal.ctf.core.utils.SparseList;
import org.junit.Test;

/**
 * Sparse list test
 *
 * @author Matthew Khouzam
 */
public class SparseListTest {

    /**
     * Create a list
     *
     * @return the list
     */
    protected List<String> createList() {
        return new SparseList<>();
    }

    /**
     * Create a list that copies the data
     *
     * @param reference
     *            the source list
     * @return a list
     */
    protected List<String> createList(List<String> reference) {
        return new SparseList<>(reference);
    }

    /**
     * Test simple building
     */
    @Test
    public void testSimple() {
        List<String> test = createList();
        test.add("Pomme");
        for (int i = 0; i < 99999; i++) {
            test.add(null);
        }
        test.add("Peche");
        for (int i = 0; i < 99999; i++) {
            test.add(null);
        }
        test.add("Poire");
        for (int i = 0; i < 99998; i++) {
            test.add(null);
        }
        test.add("Banane");
        assertEquals(300000, test.size());
    }

    /**
     * Test equality of list
     */
    @Test
    public void testEquality() {
        List<String> test = createList();
        test.add("Pomme");
        test.add("Peche");
        test.add("Poire");
        test.add("Banane");
        List<String> reference = Arrays.asList("Pomme", "Peche", "Poire", "Banane");
        List<String> badReference = Arrays.asList("Apple", "Peach", "Pear", "Banana");
        List<String> outOfOrderReference = Arrays.asList("Pomme", "Peche", "Banane", "Poire");
        assertEquals(reference, test);
        assertNotEquals(badReference, test);
        assertNotEquals(outOfOrderReference, test);
        assertTrue(test.add(null));
        assertNotEquals(reference, test);
    }

    /**
     * Test a copy constructor
     */
    @Test
    public void testCopyConstructor() {
        List<String> reference = Arrays.asList("Pomme", "Peche", "Poire", "Banane");
        List<String> test = createList(reference);
        assertEquals(reference, test);
    }

    /**
     * Test a copy constructor
     */
    @Test
    public void testCopyConstructorWithNull() {
        List<String> reference = new ArrayList<>();
        reference.add(null);
        List<String> test = createList(reference);
        assertEquals(reference, test);
    }

    /**
     * Test with hash collisions
     */
    @Test
    public void testHashCollision() {
        List<String> reference = Arrays.asList("Pomme", "Peche", "Poire", "Banane");
        SparseList<String> test = new SparseList<>();
        test.ensureSize(1000000);
        test.set(242899, reference.get(0));
        test.set(583202, reference.get(1));
        test.set(703005, reference.get(2));
        test.set(962783, reference.get(3));
        assertArrayEquals(reference.toArray(), test.toArray());
        assertArrayEquals(reference.toArray(new String[4]), test.toArray(new String[4]));
    }

    /**
     * Test a copy copy copy copy constructor
     */
    @Test
    public void testRecursiveCopyConstructor() {
        List<String> reference = Arrays.asList("Pomme", "Peche", "Poire", "Banane");
        List<String> test = createList(createList(createList(createList(reference))));
        assertEquals(reference, test);
    }

    /**
     * Test a constructor with nulls
     */
    @Test
    public void testCopyConstructorWithNulls() {
        List<String> reference = Arrays.asList("Pomme", null, "Peche", null, "Poire", null, "Banane", null);
        List<String> test = createList(reference);
        assertEquals(reference, test);
    }

    /**
     * Test Streams and the spliterator
     */
    @Test
    public void testStreams() {
        List<String> reference = Arrays.asList("Pomme", null, "Peche", null, "Poire", null, "Banane", null);
        Optional<String> first = reference.stream().findFirst();
        assertTrue(first.isPresent());
        assertEquals("Pomme", first.get());
        Optional<String> firstNonNull = reference.stream().filter(Objects::nonNull).findFirst();
        assertTrue(firstNonNull.isPresent());
        assertEquals("Pomme", firstNonNull.get());
        assertEquals(4, reference.stream().filter(Objects::isNull).count());
        assertEquals(Arrays.asList("Pomme", null, "Peche", "Poire", "Banane"), reference.stream().distinct().collect(Collectors.toList()));
    }

    /**
     * Test clear... shouldn't work, but it was easy
     */
    @Test
    public void testClear() {
        List<String> test = createList();
        List<String> reference = Arrays.asList("Pomme", "Peche", null, "Poire", "Banane");
        assertTrue(test.isEmpty());
        test.addAll(reference);
        assertEquals(reference, test);
        test.clear();
        assertTrue(test.isEmpty());
    }

    /**
     * Test is empty
     */
    @Test
    public void testIsEmpty() {
        List<String> reference = Arrays.asList("Pomme", "Peche", "Poire", "Banane");
        List<String> test = createList();
        assertTrue(test.isEmpty());
        test.addAll(test);
        assertTrue(test.isEmpty());
        test.addAll(reference);
        assertFalse(test.isEmpty());
        test = createList();
        assertTrue(test.isEmpty());
        test.add(null);
        assertFalse(test.isEmpty());
    }

    /**
     * Test contains with null and other
     */
    @Test
    public void testContains() {
        List<String> reference = Arrays.asList("Pomme", "Peche", "Poire", "Banane");
        List<String> test = createList(reference);
        assertTrue(test.contains("Pomme"));
        assertFalse(test.contains(null));
        assertTrue(test.containsAll(Arrays.asList("Peche", "Pomme")));
        assertFalse(test.containsAll(Arrays.asList("Pomme", null, "Peche", null)));
        // add a null and re-run
        test.add(null);
        assertTrue(test.contains("Pomme"));
        assertTrue(test.contains(null));
        assertTrue(test.containsAll(Arrays.asList("Peche", "Pomme")));
        assertTrue(test.containsAll(Arrays.asList("Pomme", null, "Peche", null)));
    }

    /**
     * Test indexOfs
     */
    @Test
    public void testIndexOf() {
        List<String> test = createList();
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("mushroom");
        test.add("mushroom");
        assertEquals(0, test.indexOf("badger"));
        assertEquals(-1, test.indexOf("a snake"));
        assertEquals(-1, test.indexOf(null));
        assertEquals(-1, test.lastIndexOf(null));
        assertEquals(11, test.lastIndexOf("badger"));
        assertTrue(test.contains("badger"));
        assertTrue(test.containsAll(Collections.singleton("badger")));
        assertTrue(test.containsAll(Arrays.asList("badger", "mushroom")));
    }

    /**
     * Test streams
     */
    @Test
    public void testStream() {
        List<String> test = createList();
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("badger");
        test.add("mushroom");
        test.add("mushroom");
        assertEquals(12, test.parallelStream().filter("badger"::equals).count());
    }

    /**
     * Test toArray
     */
    @Test
    public void testToArray() {
        List<String> reference = Arrays.asList("Pomme", "Peche", "Poire", "Banane");
        List<String> test = createList(reference);
        assertArrayEquals(reference.toArray(), test.toArray());
        assertArrayEquals(reference.toArray(new String[4]), test.toArray(new String[4]));
        List<String> referenceWithNulls = Arrays.asList("Pomme", "Peche", "Poire", null, "Banane");
        test = createList(referenceWithNulls);
        assertArrayEquals(reference.toArray(), test.toArray());
        assertArrayEquals(reference.toArray(new String[4]), test.toArray(new String[4]));
        assertArrayEquals(reference.toArray(new String[4]), test.toArray(new String[1]));
        assertArrayEquals(reference.toArray(new String[5]), test.toArray(new String[5]));
        try {
            assertArrayEquals(reference.toArray(new String[4]), test.toArray(new Long[4]));
            fail("Should not get here");
        } catch (ArrayStoreException e) {
            // correct flow
        }
    }

    /**
     * Test toString
     */
    @Test
    public void testToString() {
        List<String> reference = Arrays.asList("Pomme", "Peche", "Poire", null, "Banane");
        List<String> test = createList(reference);
        assertEquals("[0:Pomme, 1:Peche, 2:Poire, 4:Banane]", test.toString());
    }

    /**
     * Test the set operation
     */
    @Test
    public void testSet() {
        List<String> reference = Arrays.asList("Pomme", "Peche", "Poire", "Banane");
        List<String> test = createList(reference);
        assertEquals(reference, test);
        test.set(0, "pomme");
        assertNotEquals(reference, test);
        try {
            test.set(-1, "pomme");
            fail("Should not get here");
        } catch (IndexOutOfBoundsException e) {
            // correct flow
        }
        try {
            test.set(5, "pomme");
            fail("Should not get here");
        } catch (IndexOutOfBoundsException e) {
            // correct flow
        }
    }

    /**
     * Test the list iterator
     */
    @Test
    public void testListIterator() {
        testListIterator(createList(Arrays.asList("Hola", "yo", "quiero", "un", "UNSUPPORTEDOPERATIONEXCEPTION!")));
    }

    /**
     * Test the list iterator out of order
     */
    @Test
    public void testListIteratorOutOfOrder() {
        SparseList<String> list = new SparseList<>();
        list.ensureSize(5);
        list.set(4, "UNSUPPORTEDOPERATIONEXCEPTION!");
        list.set(2, "quiero");
        list.set(1, "yo");
        list.set(0, "Hola");
        list.set(3, "un");
        testListIterator(list);
    }

    private static void testListIterator(List<String> test) {
        ListIterator<String> iterator = test.listIterator(0);
        assertTrue(iterator.hasNext());
        assertFalse(iterator.hasPrevious());
        try {
            iterator.previous();
            fail("Should not get here");
        } catch (NoSuchElementException e) {
            // correct flow
        }
        assertEquals("Hola", iterator.next());
        assertEquals("yo", iterator.next());
        assertEquals("yo", iterator.previous());
        assertEquals("Hola", iterator.previous());
        try {
            iterator.previous();
            fail("Should not get here");
        } catch (NoSuchElementException e) {
            // correct flow
        }
        assertEquals("Hola", iterator.next());
        assertEquals("yo", iterator.next());
        assertEquals("quiero", iterator.next());
        assertEquals("un", iterator.next());
        assertEquals("UNSUPPORTEDOPERATIONEXCEPTION!", iterator.next());
        try {
            iterator.next();
            fail("Should not get here");
        } catch (NoSuchElementException e) {
            // correct flow
        }
        assertEquals("UNSUPPORTEDOPERATIONEXCEPTION!", iterator.previous());
        assertEquals(3, iterator.previousIndex());
        assertEquals(4, iterator.nextIndex());
        try {
            iterator.remove();
            fail("Should not get here");
        } catch (UnsupportedOperationException e) {
            // correct flow
        }
        try {
            iterator.set("hej");
            fail("Should not get here");
        } catch (UnsupportedOperationException e) {
            // correct flow
        }
        try {
            iterator.add("hi");
            fail("Should not get here");
        } catch (UnsupportedOperationException e) {
            // correct flow
        }
    }

    /**
     * Test a list iterator with nulls
     */
    @Test
    public void testListIteratorWithNull() {
        List<String> list = createList(Arrays.asList("Hello", null, "world"));
        ListIterator<String> iterator = list.listIterator();
        assertEquals("Hello", iterator.next());
        assertEquals(null, iterator.next());
        assertEquals("world", iterator.next());
        assertEquals("world", iterator.previous());
        assertEquals(null, iterator.previous());
        assertEquals("Hello", iterator.previous());
        iterator = list.listIterator(0);
        assertEquals("Hello", iterator.next());
        assertEquals(null, iterator.next());
        assertEquals("world", iterator.next());
        assertEquals("world", iterator.previous());
        assertEquals(null, iterator.previous());
        assertEquals("Hello", iterator.previous());
        iterator = list.listIterator(1);
        assertEquals("Hello", iterator.previous());
        assertEquals("Hello", iterator.next());
        assertEquals(null, iterator.next());
        assertEquals("world", iterator.next());
        assertEquals("world", iterator.previous());
        assertEquals(null, iterator.previous());
        assertEquals("Hello", iterator.previous());
        iterator = list.listIterator(2);
        assertEquals(null, iterator.previous());
        assertEquals("Hello", iterator.previous());
        assertEquals("Hello", iterator.next());
        assertEquals(null, iterator.next());
        assertEquals("world", iterator.next());
        assertEquals("world", iterator.previous());
        assertEquals(null, iterator.previous());
        assertEquals("Hello", iterator.previous());
    }

    /**
     * Test setting the values our of order
     */
    @Test
    public void testUnorderedSet() {
        String value = "badger";
        List<String> test = createList();
        ((SparseList<String>) test).ensureSize(5);
        test.set(4, value);
        test.set(2, value);
        assertEquals(2, test.indexOf(value));
        assertEquals(4, test.lastIndexOf(value));
        assertEquals(0, test.indexOf(null));
        assertEquals(3, test.lastIndexOf(null));
        assertEquals(-1, test.indexOf("mushroom"));
        assertEquals(-1, test.lastIndexOf("mushroom"));
        assertNull(test.get(0));
        assertNull(test.get(1));
        assertEquals(value, test.get(2));
        assertNull(test.get(3));
        assertEquals(value, test.get(4));
        test.set(0, value);
        assertEquals(1, test.indexOf(null));
    }

    /**
     * Reminds people to update tests if they add these features.
     */
    @Test
    public void testUnsupporteds() {
        List<String> test = createList();
        try {
            test.addAll(0, Collections.emptyList());
            fail("Should not get here");
        } catch (UnsupportedOperationException e) {
            // correct flow
        }
        try {
            test.add(0, null);
            fail("Should not get here");
        } catch (UnsupportedOperationException e) {
            // correct flow
        }
        try {
            test.remove(0);
            fail("Should not get here");
        } catch (UnsupportedOperationException e) {
            // correct flow
        }
        try {
            test.subList(0, 0);
            fail("Should not get here");
        } catch (UnsupportedOperationException e) {
            // correct flow
        }
        try {
            test.remove("que?");
            fail("Should not get here");
        } catch (UnsupportedOperationException e) {
            // correct flow
        }
        try {
            test.removeAll(Collections.singletonList("que?"));
            fail("Should not get here");
        } catch (UnsupportedOperationException e) {
            // correct flow
        }
        try {
            test.retainAll(Arrays.asList("que", "pasa?"));
            fail("Should not get here");
        } catch (UnsupportedOperationException e) {
            // correct flow
        }
        try {
            test.get(-1);
            fail("Should not get here");
        } catch (IndexOutOfBoundsException e) {
            // correct flow
        }
        try {
            test.get(test.size());
            fail("Should not get here");
        } catch (IndexOutOfBoundsException e) {
            // correct flow
        }
    }
}
