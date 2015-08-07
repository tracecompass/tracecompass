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

import java.util.Comparator;

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
public class ViewerCompoundComparator extends ViewerComparator {

    /**
     * String comparator, compares two objects by their toString values, if an
     * object is null, it is assigned to an empty string
     */
    public static final ViewerCompoundComparator STRING_COMPARATOR = new ViewerCompoundComparator(new Comparator<Object>() {
        @Override
        public int compare(Object e1, Object e2) {
            String left = NonNullUtils.nullToEmptyString(e1);
            String right = NonNullUtils.nullToEmptyString(e2);
            return left.compareTo(right);
        }
    });

    private ViewerCompoundComparator fNext;

    /**
     * Create a viewer compound comparator
     *
     * @param comparator
     *            selected comparator
     */
    public ViewerCompoundComparator(Comparator<? extends Object> comparator) {
        super(comparator);
    }

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

    private int getNextCompare(Viewer viewer, Object e1, Object e2) {
        return (fNext != null) ? fNext.compare(viewer, e1, e2) : 0;
    }

    @Override
    public Comparator getComparator() {
        return super.getComparator();
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int retVal = getComparator().compare(e1, e2);
        return (retVal != 0) ? retVal : getNextCompare(viewer, e1, e2);
    }
}
