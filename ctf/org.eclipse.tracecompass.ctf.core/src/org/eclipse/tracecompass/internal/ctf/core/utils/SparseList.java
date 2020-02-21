/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.ctf.core.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * <p>
 * Sparse list, a list optimized for when most of the data is <code>null</code>.
 * Nulls will increment the size of the SparseList but they are not stored
 * internally.
 * </p>
 * <p>
 * Note: this iterates in the list order.
 * </p>
 * This implementation does not support:
 * <ul>
 * <li>{@link #add(int, Object)}</li>
 * <li>{@link #addAll(int, Collection)}</li>
 * <li>{@link #remove(int)}</li>
 * <li>{@link #remove(Object)}</li>
 * <li>{@link #removeAll(Collection)}</li>
 * <li>{@link #subList(int, int)}</li>
 * </ul>
 * </p>
 * <p>
 * An efficient shift operation for bulk moving elements in a list would be
 * needed in order to implement these features.
 * </p>
 * TODO: Keep an eye out for a better datastructure... SparseList is intended as
 * a stop-gap fix. It is fine, but if it can be replaced by an externally
 * maintained datastructure, that would be better.
 *
 * @author Matthew Khouzam
 * @param <E>
 *            the element type
 */
public class SparseList<E> implements List<E> {

    /**
     * A backing map used to store the non-null elements
     */
    private final Map<Integer, @NonNull E> fInnerElements = new HashMap<>();
    /**
     * The list size: map size + number of nulls
     */
    private int fSize = 0;

    /**
     * Copy constructor
     *
     * @param events
     *            list of events
     */
    public SparseList(List<E> events) {
        ensureSize(events.size());
        for (int i = 0; i < events.size(); i++) {
            E element = events.get(i);
            if (element != null) {
                set(i, element);
            }
        }
    }

    /**
     * default constructor
     */
    public SparseList() {
        // Do nothing
    }

    @Override
    public int size() {
        return fSize;
    }

    @Override
    public boolean isEmpty() {
        return fSize == 0;
    }

    @Override
    public boolean contains(Object o) {
        return o == null ? size() > fInnerElements.size() : fInnerElements.containsValue(o);
    }

    @Override
    public Iterator<E> iterator() {
        return new GenericReadOnlyListIterator<>(this, 0);
    }

    /**
     * Break in contract with {@link List#toArray()} implementation. This
     * returns an list-ordered array of size N where N is the number of non-null
     * elements. This is a design concession to avoid out of memory errors with
     * the sparse list, since this list should only be used when memory
     * footprint is important.
     *
     * The returned array will be "safe" in that no references to it are
     * maintained by this list. (In other words, this method must allocate a new
     * array even if this list is backed by an array). The caller is thus free
     * to modify the returned array.
     *
     * @return an array containing all of the non-null elements in this list in
     *         proper sequence
     */
    @Override
    public Object[] toArray() {
        int size = fInnerElements.size();
        Object[] retVal = new Object[size];
        Iterator<E> iterator = iterator();
        for (int i = 0; i < size; i++) {
            Object next = null;
            while (iterator.hasNext() && next == null) {
                next = iterator.next();
            }
            retVal[i] = next;
        }
        return retVal;
    }

    /**
     * Break in contract with {@link List#toArray(Object[])} implementation.
     *
     * Returns an array containing all of the <strong>non-null</strong> elements
     * in this list in proper sequence (from first to last element); the runtime
     * type of the returned array is that of the specified array. If the list
     * fits in the specified array, it is returned therein. Otherwise, only the
     * first n elements of the list are returned.
     *
     * This is a design concession to avoid out of memory errors with the sparse
     * list, since this list should only be used when memory footprint is
     * important.
     *
     * @param newArray
     *            the array to return
     * @return newArray, filled with values.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] newArray) {

        Class<?> componentType = newArray.getClass().getComponentType();
        T[] returnArray = newArray;
        int size = fInnerElements.size();
        if (returnArray.length < size) {
            returnArray = (T[]) Array.newInstance(componentType, size);
        }
        for (int i = size; i < returnArray.length; i++) {
            returnArray[i] = null;
        }
        Iterator<E> iterator = iterator();
        for (int i = 0; i < size; i++) {
            @Nullable E next = null;
            while (iterator.hasNext() && next == null) {
                next = iterator.next();
            }
            returnArray[i] = (T) next;
        }
        return returnArray;
    }

    @Override
    public boolean add(E e) {
        if (e != null) {
            fInnerElements.put(fSize, e);
        }
        fSize++;
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Collection<?> nonNullCollection = c;
        if (nonNullCollection.contains(null)) {
            if (!contains(null)) {
                return false;
            }
            nonNullCollection = c.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        return fInnerElements.values().containsAll(nonNullCollection);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        int key = fSize;
        fSize += c.size();
        for (E event : c) {
            set(key, event);
            key++;
        }
        return true;
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Tried to access index " + index + " Sparse list size " + fSize); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return fInnerElements.get(index);
    }

    @Override
    public E set(int index, E element) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Tried to add to index " + index + " Sparse list size " + fSize); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (element != null) {
            return fInnerElements.put(index, element);
        }
        return fInnerElements.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        if (o == null && contains(null)) {
            for (int i = 0; i < size(); i++) {
                if (!fInnerElements.containsKey(i)) {
                    return i;
                }
            }
        }
        int first = Integer.MAX_VALUE;
        for (Entry<Integer, E> entry : fInnerElements.entrySet()) {
            if (Objects.equals(entry.getValue(), o)) {
                first = Math.min(entry.getKey(), first);
            }
        }
        return first == Integer.MAX_VALUE ? -1 : first;
    }

    @Override
    public int lastIndexOf(Object o) {
        int last = -1;
        if (o == null && contains(null)) {
            for (int i = size() - 1; i >= 0; i--) {
                if (!fInnerElements.containsKey(i)) {
                    return i;
                }
            }
        }
        for (Entry<Integer, E> entry : fInnerElements.entrySet()) {
            if (Objects.equals(entry.getValue(), o)) {
                last = Math.max(last, entry.getKey());
            }
        }
        return last;
    }

    @Override
    public void clear() {
        fInnerElements.clear();
        fSize = 0;
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), fSize, Spliterator.ORDERED);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new GenericReadOnlyListIterator<>(this, 0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new GenericReadOnlyListIterator<>(this, index);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size(); i++) {
            E element = get(i);
            if (element != null) {
                sb.append(i).append(':').append(String.valueOf(element));

                if (i < size() - 1) {
                    sb.append(',').append(' ');
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * resize the list
     *
     * @param requestedSize
     *            the new size
     */
    public void ensureSize(int requestedSize) {
        fSize = Math.max(fSize, requestedSize);
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("No add(int, E) in " + this.getClass().getName()); //$NON-NLS-1$
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("No remove(int) in " + this.getClass().getName()); //$NON-NLS-1$
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("No remove(Object) in " + this.getClass().getName()); //$NON-NLS-1$
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("No addAll(int, Collection<? extends E>) in " + this.getClass().getName()); //$NON-NLS-1$
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("No removeAll(Collection<?>) in " + this.getClass().getName()); //$NON-NLS-1$
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("No retainAll(Collection<?> in " + this.getClass().getName()); //$NON-NLS-1$
    }

    @Override
    public @NonNull List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("No subList(int, int) in " + this.getClass().getName()); //$NON-NLS-1$
    }
}