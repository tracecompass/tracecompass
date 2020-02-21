/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.uml2sd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * <p>
 * Informs all registered listeners of graph node selection change in the Frame.
 * </p>
 *
 * @version 1.0
 * @author sveyrier
 */
public class SDWidgetSelectionProvider implements ISelectionProvider {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The listener list
     */
    private List<ISelectionChangedListener> fListenerList = null;

    /**
     * The current selection
     */
    private ISelection fCurrentSelection = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     */
    protected SDWidgetSelectionProvider() {
        fListenerList = new ArrayList<>();
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (!fListenerList.contains(listener)) {
            fListenerList.add(listener);
        }
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        fListenerList.remove(listener);
    }

    @Override
    public void setSelection(ISelection selection) {
        fCurrentSelection = selection;
        for (int i = 0; i < fListenerList.size(); i++) {
            ISelectionChangedListener list = fListenerList.get(i);
            list.selectionChanged(new SelectionChangedEvent(this, fCurrentSelection));
        }
    }

    @Override
    public ISelection getSelection() {
        return fCurrentSelection;
    }

}
