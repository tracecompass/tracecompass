/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.viewers.table;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.tracecompass.common.core.NonNullUtils;

/**
 * ViewerCompoundComparator that can be used to make compound comparisons (1st
 * key 2nd key etc...)
 *
 * @since 2.0
 *
 */
public abstract class ViewerCompoundComparator extends ViewerComparator {

    /**
     * String comparator, compares two objects by their toString values, if an
     * object is null, it is assigned to an empty string
     */
    public static final ViewerCompoundComparator STRING_COMPARATOR = new ViewerCompoundComparator() {
        @Override
        public int compare(Object e1, Object e2) {
            String left = NonNullUtils.nullToEmptyString(e1);
            String right = NonNullUtils.nullToEmptyString(e2);
            return left.compareTo(right);
        }
    };

    ViewerCompoundComparator fNext;

    /**
     * Sets the next comparator
     *
     * @param next
     *            the next comparator
     */
    public void setNext(ViewerCompoundComparator next) {
        fNext = next;
    }

    /**
     * Get the next comparator
     *
     * @return the next comparator
     */
    public ViewerCompoundComparator getNext() {
        return fNext;
    }

    private int getNextComparator(Object e1, Object e2) {
        return (fNext != null) ? fNext.compare(e1, e2) : 0;
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int retVal = compare(e1, e2);
        return (retVal != 0) ? retVal : getNextComparator(e1, e2);
    }

    /**
     * Returns a negative, zero, or positive number depending on whether the
     * first element is less than, equal to, or greater than the second element.
     * <p>
     *
     * @param e1
     *            the first element
     * @param e2
     *            the second element
     * @return a negative number if the first element is less than the second
     *         element; the value <code>0</code> if the first element is equal
     *         to the second element; and a positive number if the first element
     *         is greater than the second element
     */
    public abstract int compare(Object e1, Object e2);
}
