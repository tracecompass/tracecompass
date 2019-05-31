/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.ctf.core.utils;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Generic Read only List Iterator
 *
 * @author Matthew Khouzam
 *
 * @param <E>
 *            the element type
 */
final class GenericReadOnlyListIterator<E> implements ListIterator<E> {
    private int fCursor;
    private final List<E> fList;

    /**
     * Constructor
     *
     * @param list
     *            the list
     * @param start
     *            the first element to iterate on
     */
    public GenericReadOnlyListIterator(List<E> list, int start) {
        fList = list;
        fCursor = start - 1;
    }

    @Override
    public boolean hasNext() {
        return nextIndex() < fList.size();
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        E next = fList.get(nextIndex());
        fCursor++;
        return next;
    }

    @Override
    public boolean hasPrevious() {
        return previousIndex() >= 0;
    }

    @Override
    public E previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        E prev = fList.get(previousIndex());
        fCursor--;
        return prev;
    }

    @Override
    public int nextIndex() {
        return fCursor + 1;
    }

    @Override
    public int previousIndex() {
        return fCursor;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(E e) {
        throw new UnsupportedOperationException();
    }
}