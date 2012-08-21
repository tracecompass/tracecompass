/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.util;

/**
 * Pair utility class, encapsulates a pair of objects.
 *
 * @param <A>
 *            The type of the first object.
 * @param <B>
 *            The type of the second object.
 *
 * @author Philippe Sawicki
 * @since 2.0
 */
public class Pair<A, B> {

    /**
     * A reference to the first object.
     */
    protected A fFirst;
    /**
     * A reference to the second object.
     */
    protected B fSecond;

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
     * Constructor.
     */
    public Pair() {
        this(null, null);
    }

    /**
     * Pair hash code.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fFirst == null) ? 0 : fFirst.hashCode());
        result = prime * result + ((fSecond == null) ? 0 : fSecond.hashCode());
        return result;
    }

    /**
     * Object comparison.
     */
    @Override
    public boolean equals(Object obj) {
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

        if (fFirst == null) {
            if (other.fFirst != null) {
                return false;
            }
        } else if (!fFirst.equals(other.fFirst)) {
            return false;
        }
        if (fSecond == null) {
            if (other.fSecond != null) {
                return false;
            }
        } else if (!fSecond.equals(other.fSecond)) {
            return false;
        }
        return true;
    }

    /**
     * Object to string.
     */
    @Override
    public String toString() {
        return "(" + fFirst + ", " + fSecond + ")";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Returns a reference to the pair's first object.
     * @return A reference to the pair's first object.
     */
    public A getFirst() {
        return fFirst;
    }

    /**
     * Sets the pair's first object.
     * @param first
     *            The pair's first object.
     */
    public void setFirst(A first) {
        fFirst = first;
    }

    /**
     * Returns a reference to the pair's second object.
     * @return A reference to the pair's second object.
     */
    public B getSecond() {
        return fSecond;
    }

    /**
     * Sets the pair's second object.
     * @param second
     *            The pair's second object.
     */
    public void setSecond(B second) {
        fSecond = second;
    }
}