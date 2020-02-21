/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.common.core;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods for arbitrary objects.
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public final class ObjectUtils {

    private ObjectUtils() {}

    /**
     * Checks deep equality of two objects that may be arrays or collections.
     * The objects (and each of their respective elements, if applicable) must
     * be of the same class to be considered equal. Deep equality is recursively
     * called on each element of an array or collection, if applicable.
     *
     * @param o1
     *            the first object to compare
     * @param o2
     *            the second object to compare
     * @return true if the objects are deeply equal, false otherwise
     */
    public static boolean deepEquals(final @Nullable Object o1, @Nullable final Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        Class<?> class1 = o1.getClass();
        Class<?> class2 = o2.getClass();
        if (!class1.equals(class2)) {
            return false;
        }
        if (class1.isArray()) {
            int length = Array.getLength(o1);
            if (Array.getLength(o2) != length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (!deepEquals(Array.get(o1, i), Array.get(o2, i))) {
                    return false;
                }
            }
            return true;
        } else if (o1 instanceof Collection) {
            Collection<?> c1 = (Collection<?>) o1;
            Collection<?> c2 = (Collection<?>) o2;
            int size = c1.size();
            if (c2.size() != size) {
                return false;
            }
            if (o1 instanceof Set && !(o1 instanceof SortedSet<?>)) {
                /* Iteration order is undefined */
                for (Object e1 : c1) {
                    boolean contains = false;
                    for (Object e2 : c2) {
                        if (deepEquals(e1, e2)) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        return false;
                    }
                }
                return true;
            }
            Iterator<?> i1 = c1.iterator();
            Iterator<?> i2 = c2.iterator();
            while (i1.hasNext()) {
                if (!deepEquals(i1.next(), i2.next())) {
                    return false;
                }
            }
            return true;
        }
        return o1.equals(o2);
    }

    /**
     * Returns a hash code value for an object which may be an array or a
     * collection. The deep hash code is recursively computed for each element
     * of an array or collection, if applicable.
     *
     * @param o
     *            the object
     * @return a hash code value for this object.
     */
    public static int deepHashCode(final @Nullable Object o) {
        if (o == null) {
            return 0;
        }
        if (o.getClass().isArray()) {
            final int prime = 31;
            int result = 1;
            for (int i = 0; i < Array.getLength(o); i++) {
                result = prime * result + deepHashCode(Array.get(o, i));
            }
            return result;
        }
        if (o instanceof Collection) {
            Collection<?> c = (Collection<?>) o;
            if (o instanceof Set && !(o instanceof SortedSet<?>)) {
                /* Iteration order is undefined */
                int result = 0;
                for (Object e : c) {
                    result += deepHashCode(e);
                }
                return result;
            }
            final int prime = 31;
            int result = 1;
            for (Object e : c) {
                result = prime * result + deepHashCode(e);
            }
            return result;
        }
        return o.hashCode();
    }
}
