/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.common.core;

import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods to handle {@link org.eclipse.jdt.annotation.NonNull}
 * annotations.
 *
 * @author Alexandre Montplaisir
 */
public final class NonNullUtils {

    private NonNullUtils() {
    }

    /**
     * Returns a non-null {@link String} for a potentially null object. This
     * method calls {@link Object#toString()} if the object is not null, or
     * returns an empty string otherwise.
     *
     * @param obj
     *            A {@link Nullable} object that we want converted to a string
     * @return The non-null string
     */
    public static String nullToEmptyString(@Nullable Object obj) {
        if (obj == null) {
            return ""; //$NON-NLS-1$
        }
        String str = obj.toString();
        return (str == null ? "" : str); //$NON-NLS-1$
    }

    /**
     * Checks equality with two nullable objects
     *
     * @param o1
     *            the first object to compare
     * @param o2
     *            the second object to compare
     * @return true if o1.equals(o2) or o1 == o2
     * @since 1.0
     */
    public static boolean equalsNullable(final @Nullable Object o1, final @Nullable Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    // ------------------------------------------------------------------------
    // checkNotNull() methods, to convert @Nullable references to @NonNull ones
    // ------------------------------------------------------------------------

    /**
     * Convert a non-annotated object reference to a {@link NonNull} one.
     *
     * If the reference is actually null, a {@link NullPointerException} is
     * thrown. This is usually more desirable than letting an unwanted null
     * reference go through and fail much later.
     *
     * @param obj
     *            The object that is supposed to be non-null
     * @return A {@link NonNull} reference to the same object
     * @throws NullPointerException
     *             If the reference was actually null
     */
    public static <T> @NonNull T checkNotNull(@Nullable T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    /**
     * Convert a non-annotated [] array reference to a @NonNull one.
     *
     * Note that this method does not check the array contents itself, which can
     * still contain null elements.
     *
     * @param array
     *            The array whose reference should not be null
     * @return A {@link NonNull} reference to the array
     * @throws NullPointerException
     *             If the reference was actually null
     * @since 2.0
     */
    public static <T> T[] checkNotNull(T @Nullable [] array) {
        if (array == null) {
            throw new NullPointerException();
        }
        return array;
    }

    /**
     * Convert a non-annotated {@link Iterable} to a NonNull one.
     *
     * Note that, unlike {{@link #checkNotNull(Object)}}, this method does not
     * check the contents itself, which can still contain null elements.
     *
     * @param container
     *            The iterable whose reference should not be null
     * @return A {@link NonNull} reference to the Iterable. The original class
     *         type is preserved.
     * @throws NullPointerException
     *             If the reference was actually null
     * @since 2.0
     */
    public static <T, C extends Iterable<T>> C checkNotNull(@Nullable C container) {
        if (container == null) {
            throw new NullPointerException();
        }
        return container;
    }

    /**
     * Convert a non-annotated {@link Map} to a NonNull one.
     *
     * Note that, unlike {{@link #checkNotNull(Object)}}, this method does not
     * check the keys or values themselves, which can still contain null
     * elements.
     *
     * @param map
     *            The map whose reference should not be null
     * @return A {@link NonNull} reference to the Map
     * @throws NullPointerException
     *             If the reference was actually null
     * @since 2.0
     */
    public static <K, V, M extends Map<K, V>> M checkNotNull(@Nullable M map) {
        if (map == null) {
            throw new NullPointerException();
        }
        return map;
    }

    /**
     * Ensures a {@link Stream} does not contain any null values.
     *
     * This also "upcasts" the reference from a Stream<@Nullable T> to a
     * Stream<@NonNull T>.
     *
     * @param stream
     *            The stream to check for
     * @return A stream with the same elements
     * @throws NullPointerException
     *             If the stream itself or any of its values are null
     * @since 2.0
     */
    public static <T> Stream<@NonNull T> checkNotNullContents(@Nullable Stream<@Nullable T> stream) {
        if (stream == null) {
            throw new NullPointerException();
        }
        return checkNotNull(stream.map(t -> checkNotNull(t)));
    }
}
