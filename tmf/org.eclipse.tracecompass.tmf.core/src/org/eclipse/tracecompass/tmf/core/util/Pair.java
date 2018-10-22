/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *   Bernd Hufmann - Integrated to TMF, fixed hashCode() and equals() methods
 *   Alexandre Montplaisir - Made non-null and immutable
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.util;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Pair utility class, encapsulates a pair of objects.
 *
 * @param <A>
 *            The type of the first object.
 * @param <B>
 *            The type of the second object.
 *
 * @author Philippe Sawicki
 */
public class Pair<A, B> {

    /**
     * A reference to the first object.
     */
    private final A fFirst;
    /**
     * A reference to the second object.
     */
    private final B fSecond;

    /**
     * Constructor.
     * @param first
     *            The pair's first object.
     * @param second
     *            The pair's second object.
     */
    public Pair(A first, B second) {
        fFirst = first;
        fSecond = second;
    }


    /**
     * Returns a reference to the pair's first object.
     *
     * @return A reference to the pair's first object.
     */
    public A getFirst() {
        return fFirst;
    }

    /**
     * Returns a reference to the pair's second object.
     *
     * @return A reference to the pair's second object.
     */
    public B getSecond() {
        return fSecond;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hashFromNullable(fFirst);
        result = prime * result + hashFromNullable(fSecond);
        return result;
    }

    private static int hashFromNullable(@Nullable Object o) {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>) obj;
        if (!Objects.equals(other.fFirst, fFirst)) {
            return false;
        }
        return (Objects.equals(other.fSecond, fSecond));
    }

    @Override
    public String toString() {
        return "(" + fFirst + ", " + fSecond + ")";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }
}