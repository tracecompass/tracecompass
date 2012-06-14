/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Godin (copelnug@gmail.com)        - Initial design and implementation
 *   Mathieu Denis  (mathieu.denis@polymtl.ca)  - Correction and refactoring
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;

/**
 * <h4>Allow to create a List object that contain an already existing array.</h4>
 * <p>Works like {@link java.util.Arrays#asList} but offers more functions :
 * <ul>
 *  <li>{@link #hashCode()}</li>
 *  <li>{@link #equals(Object)}</li>
 * </ul></p>
 * <p>Those functions allow to use the FixedArray as the key of a {@link java.util.HashMap}.</p>
 *
 * @version 1.0
 * @author Francois Godin
 *
 * @param <T> Type of the array content.
 */
public final class TmfFixedArray<T> extends AbstractList<T> implements RandomAccess, Cloneable {
    /**
     * Replace {@link java.util.Arrays#copyOf(Object[], int)} that do not exist in java 5.
     * @param <E> Content of the array.
     * @param array Original array to copy from.
     * @param newLength Length of the copy to be returned.
     * @return A new array consisting of the elements specified.
     */
    @SuppressWarnings("unchecked")
    private static <E> E[] copyOf(final E[] array, int newLength) {
        E[] result = (E[])Array.newInstance(array.getClass().getComponentType(), newLength); // Is it useful to use newInstance?
        System.arraycopy(array, 0, result, 0, Math.min(array.length, newLength));
        return result;
    }
    /**
     * Replace {@link java.util.Arrays#copyOf(Object[], int, Class)} that do not exist in java 5.
     * @param <E> Content of the array.
     * @param array Original array to copy from.
     * @param newLength Length of the copy to be returned.
     * @param newType Type of the array to be returned.
     * @return A new array consisting of the elements specified.
     */
    @SuppressWarnings("unchecked")
    private static <E, U> E[] copyOf(final U[] array, int newLength, Class<? extends E[]> newType) {
        E[] result = (E[])Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(array, 0, result, 0, Math.min(array.length, newLength));
        return result;
    }
    /**
     * Replace {@link java.util.Arrays#copyOfRange(Object[], int, int)} that do not exist in java 5.
     * @param <E> Content of the array.
     * @param array Original array to copy from.
     * @param start Starting position of the range, inclusive.
     * @param end Ending position of the range, exclusive.
     * @return A new array consisting of the elements specified. The length of the new array is equal to end-start
     */
    @SuppressWarnings("unchecked")
    private static <E> E[] copyOfRange(final E[] array, int start, int end) {
        E[] result = (E[])Array.newInstance(array.getClass().getComponentType(), end - start);
        System.arraycopy(array, start, result, 0, end - start);
        return result;
    }
    /**
     * The array.
     */
    private final T[] fArray;
    /**
     * Constructor.
     * @param array Array to use. WILL NOT BE COPIED.
     */
    public TmfFixedArray(final T... array) {
        fArray = array;
    }
    /**
     * Append a FixedArray to this FixedArray.
     * @param value The FixedArray to append.
     * @return A new FixedArray with the elements of the two FixedArray.
     */
    public TmfFixedArray<T> append(final TmfFixedArray<T> value) {
        TmfFixedArray<T> result = new TmfFixedArray<T>(copyOf(fArray, fArray.length + value.size()));
        System.arraycopy(value.fArray, 0, result.fArray, fArray.length, value.fArray.length);
        return result;
    }
    /**
     * Append in order many FixedArray to this FixedArray.
     * @param values The FixedArrays to append.
     * @return A new FixedArray with the element of all the FixedArray.
     */
    public TmfFixedArray<T> append(final TmfFixedArray<T>... values) {
        int newLength = 0;
        for(TmfFixedArray<T> value : values) {
            newLength += value.size();
        }
                TmfFixedArray<T> result = new TmfFixedArray<T>(copyOf(fArray, fArray.length + newLength));
        newLength = fArray.length;
        for(TmfFixedArray<T> value : values)
        {
            System.arraycopy(value.fArray, 0, result.fArray, newLength, value.fArray.length);
            newLength += value.fArray.length;
        }
        return result;
    }
    /**
     * Append an element to the array.
     * @param value Element to append.
     * @return A new FixedArray with the element appended.
     */
    public TmfFixedArray<T> append(final T value) {
        TmfFixedArray<T> result = new TmfFixedArray<T>(copyOf(fArray, fArray.length + 1));
        result.set(fArray.length, value);
        return result;
    }
    /**
     * Append an array of element to the array.
     * @param values Elements array to append.
     * @return A new FixedArray with the elements appended.
     */
    public TmfFixedArray<T> append(final T... values) {
        TmfFixedArray<T> result = new TmfFixedArray<T>(copyOf(fArray, fArray.length + values.length));
        for(int i = 0; i < values.length; ++i) {
            result.set(fArray.length + i, values[i]);
        }
        return result;
    }
    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone()
    {
        return new TmfFixedArray<T>(copyOf(fArray, fArray.length));
    }
    /*
     * (non-Javadoc)
     * @see java.util.AbstractList#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof TmfFixedArray<?>) {
            return Arrays.equals(fArray, ((TmfFixedArray<?>)o).fArray);
        }
        if(!(o instanceof List)) {
            return false;
        }
        for(int i = 0; i < fArray.length; ++i) {
            if(!fArray[i].equals(o)) {
                return false;
            }
        }
        return true;
    }
    /*
     * (non-Javadoc)
     * @see java.util.AbstractList#get(int)
     */
    @Override
    public T get(int index) {
        return fArray[index];
    }
    /**
     * Get the array reference.
     * @return The array reference.
     * @see #toArray FixedArray.toArray() to get a copy of the array.
     */
    public T[] getArray() {
        return fArray;
    }
    /*
     * (non-Javadoc)
     * @see java.util.AbstractList#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(fArray);
    }
    /*
     * (non-Javadoc)
     * @see java.util.AbstractList#set(int, java.lang.Object)
     */
    @Override
    public T set(int index, T element) {
        T temp = fArray[index];
        fArray[index] = element;
        return temp;
    }
    /*
     * (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return fArray.length;
    }
    /**
     * Get a array covering only a part of the array.
     * @param start Starting position of the new array.
     * @return A new array covering the elements specified.
     */
    public TmfFixedArray<T> subArray(int start) {
        return new TmfFixedArray<T>(copyOfRange(fArray, start, fArray.length));
    }
    /**
     * Get a array covering only a part of the array.
     * @param start Starting position of the new array.
     * @param length Number of element to include in the new array.
     * @return A new array covering the elements specified.
     */
    public TmfFixedArray<T> subArray(int start, int length) {
        return new TmfFixedArray<T>(copyOfRange(fArray, start, length + start));
    }
    /*
     * (non-Javadoc)
     * @see java.util.AbstractCollection#toArray()
     */
    @Override
    public T[] toArray()
    {
        return copyOf(fArray, fArray.length);
    }
    /*
     * (non-Javadoc)
     * @see java.util.AbstractCollection#toArray(T[])
     */
    @Override
    @SuppressWarnings("unchecked")
    public <E> E[] toArray(E[] array)
    {
        if(array.length < fArray.length) {
            return copyOf(fArray, fArray.length,(Class<? extends E[]>)array.getClass());
        }
        System.arraycopy(fArray, 0, array, 0, fArray.length);
        if(array.length > fArray.length) {
            array[fArray.length] = null;
        }
        return array;
    }
    /*
     * (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        return Arrays.toString(fArray);
    }
}
