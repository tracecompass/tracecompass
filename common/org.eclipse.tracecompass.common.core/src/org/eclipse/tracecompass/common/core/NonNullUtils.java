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

import java.util.Arrays;
import java.util.Objects;
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
     * @deprecated use {@link Objects#equals(Object)} instead
     */
    @Deprecated
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
    public static <T> Stream<@NonNull T> checkNotNullContents(@Nullable Stream<T> stream) {
        if (stream == null) {
            throw new NullPointerException();
        }
        return checkNotNull(stream.<@NonNull T> map(t -> checkNotNull(t)));
    }

    /**
     * Ensures an array does not contain any null elements.
     *
     * @param array
     *            The array to check
     * @return The same array, now with guaranteed @NonNull elements
     * @throws NullPointerException
     *             If the array reference or any contained element was null
     * @since 2.0
     */
    public static <T> @NonNull T[] checkNotNullContents(T @Nullable [] array) {
        if (array == null) {
            throw new NullPointerException();
        }
        Arrays.stream(array).forEach(elem -> checkNotNull(elem));
        @SuppressWarnings("null")
        @NonNull T[] ret = (@NonNull T[]) array;
        return ret;
    }
}
