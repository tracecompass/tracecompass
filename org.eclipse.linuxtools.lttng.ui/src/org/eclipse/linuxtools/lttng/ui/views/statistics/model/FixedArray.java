/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Godin (copelnug@gmail.com)  - Initial design and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.statistics.model;

import java.util.Arrays;
import java.util.RandomAccess;

/**
 * <h4>Allow to create a List object that contain an already existing array.</h4>
 * <p>Works like {@link java.util.Arrays#asList} but offers more functions : 
 * <ul>
 * 	<li>{@link #hashCode()}</li>
 * 	<li>{@link #equals(Object)}</li>
 * </ul></p>
 * <p>Those functions allow to use the FixedArray as the key of a {@link java.util.HashMap}.</p>
 *
 * @param <T> Type of the array content.
 */
public final class FixedArray implements RandomAccess, Cloneable {
	/**
	 * Replace {@link java.util.Arrays#copyOf(Object[], int)} that do not exist in java 5.
	 * @param array Original array to copy from.
	 * @param newLength Length of the copy to be returned.
	 * @return A new array consisting of the elements specified.
	 */
	private static int[] copyOf(final int[] array, int newLength) {
		int[] result = new int[newLength]; // Is it useful to use newInstance?
		System.arraycopy(array, 0, result, 0, Math.min(array.length, newLength));
		return result;
	}

	/**
	 * Replace {@link java.util.Arrays#copyOfRange(Object[], int, int)} that do not exist in java 5.
	 * @param <E> Content of the array.
	 * @param array Original array to copy from.
	 * @param start Starting position of the range.
	 * @param end Ending position of the range.
	 * @return A new array consisting of the elements specified.
	 */
	private static int[] copyOfRange(final int[] array, int start, int end) {
		int[] result = new int[end - start];
		System.arraycopy(array, start, result, 0, end - start);
		return result;
	}
	/**
	 * The array.
	 */
	private final int[] fArray;
	
	/**
	 * Constructor.
	 * @param array Array to use. WILL NOT BE COPIED.
	 */
	public FixedArray(final int... array) {
		fArray = array;
	}
	/**
	 * Append a FixedArray to this FixedArray.
	 * @param value The FixedArray to append.
	 * @return A new FixedArray with the elements of the two FixedArray.
	 */
	public FixedArray append(final FixedArray value) {
		FixedArray result = new FixedArray(copyOf(fArray, fArray.length + value.size()));
		System.arraycopy(value.fArray, 0, result.fArray, fArray.length, value.fArray.length);
		return result;
	}
	/**
	 * Append in order many FixedArray to this FixedArray.
	 * @param values The FixedArrays to append.
	 * @return A new FixedArray with the element of all the FixedArray.
	 */
	public FixedArray append(final FixedArray... values) {
		int newLength = 0;
		for(FixedArray value : values)
			newLength += value.size();
		FixedArray result = new FixedArray(copyOf(fArray, fArray.length + newLength));
		newLength = fArray.length;
		for(FixedArray value : values)
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
	public FixedArray append(final int value) {
		FixedArray result = new FixedArray(copyOf(fArray, fArray.length + 1));
		result.fArray[fArray.length] = value;
		return result;
	}
	/**
	 * Append an array of element to the array.
	 * @param values Elements array to append.
	 * @return A new FixedArray with the elements appended.
	 */
	public FixedArray append(final int... values) {
		FixedArray result = new FixedArray(copyOf(fArray, fArray.length + values.length));
		for(int i = 0; i < values.length; ++i)
			result.fArray[fArray.length + i] =  values[i];
		return result;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new FixedArray(copyOf(fArray, fArray.length));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (!(other instanceof FixedArray))
            return false;
        FixedArray array = (FixedArray) other;
        return Arrays.equals(fArray, array.fArray);
    }

	/**
	 * Gets value of given index.
	 * @param index 
	 * @return Value of given index
	 */
	public int get(int index) {
		return fArray[index];
	}
	/**
	 * Get the array reference.
	 * @return The array reference.
	 * @see #toArray FixedArray.toArray() to get a copy of the array.
	 */
	public int[] getArray() {
		return fArray;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
	    return Arrays.hashCode(fArray);
	}
	/**
	 * Sets value at given index.
	 * @param index
	 * @param value
	 * @return returns old value.
	 */
	public int set(int index, int value) {
		int temp = fArray[index];
		fArray[index] = value;
		return temp;
	}
	/**
	 * Gets the size of the array.
	 * @return Size of the array.
	 */
	public int size() {
		return fArray.length;
	}
	/**
	 * Get a array covering only a part of the array.
	 * @param start Starting position of the new array.
	 * @return A new array covering the elements specified.
	 */
	public FixedArray subArray(int start) {
		return new FixedArray(copyOfRange(fArray, start, fArray.length - 1));
	}
	/**
	 * Get a array covering only a part of the array.
	 * @param start Starting position of the new array.
	 * @param length Number of element to include in the new array.
	 * @return A new array covering the elements specified.
	 */
	public FixedArray subArray(int start, int length) {
		return new FixedArray(copyOfRange(fArray, start, length + start));
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Arrays.toString(fArray);
	}
}
