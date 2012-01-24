/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.core.util;

/**
 * Pair utility class, encapsulates a pair of objects.
 * 
 * @author Philippe Sawicki
 * 
 * @param <A>
 *            The type of the first object.
 * @param <B>
 *            The type of the second object.
 */
public abstract class Pair<A, B> {

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
        int hashFirst = fFirst != null ? fFirst.hashCode() : 0;
        int hashSecond = fSecond != null ? fSecond.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    /**
     * Object comparison.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object other) {
        if (other instanceof Pair) {
            Pair<A, B> otherPair = (Pair<A, B>) other;
            return ((fFirst == otherPair.fFirst || (fFirst != null && otherPair.fFirst != null && fFirst.equals(otherPair.fFirst))) && (fSecond == otherPair.fSecond || (fSecond != null
                    && otherPair.fSecond != null && fSecond.equals(otherPair.fSecond))));
        }
        return false;
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