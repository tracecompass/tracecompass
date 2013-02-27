/*****************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation, Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Intel Corporation - Initial API and implementation
 *   Ruslan A. Scherbakov, Intel - Initial API and implementation
 *   Alvaro Sanchez-Leon - Updated for TMF
 *   Patrick Tasse - Refactoring
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Selection object for the time graph scale
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public class TimeGraphSelection implements IStructuredSelection {

    List<Object> list = new ArrayList<Object>();

    /**
     * Default constructor
     */
    public TimeGraphSelection() {
    }

    /**
     * "Wrapper" constructor. Instantiate a new selection object with only one
     * existing selection.
     *
     * @param sel
     *            The initial selection to add to this one
     */
    public TimeGraphSelection(Object sel) {
        add(sel);
    }

    /**
     * Add a selection to this one.
     *
     * @param sel
     *            The selection to add
     */
    public void add(Object sel) {
        if (null != sel && !list.contains(sel)) {
            list.add(sel);
        }
    }

    @Override
    public Object getFirstElement() {
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Iterator<Object> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public List<Object> toList() {
        return list;
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
}
