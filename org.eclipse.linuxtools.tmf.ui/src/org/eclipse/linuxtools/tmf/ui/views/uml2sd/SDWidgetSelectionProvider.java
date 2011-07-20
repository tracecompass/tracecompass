/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SDWidgetSelectionProvider.java,v 1.3 2008/01/24 02:29:01 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * Informs all registered listeners of graph node selection change in the Frame
 * 
 * @author sveyrier
 * 
 */
public class SDWidgetSelectionProvider implements ISelectionProvider {

    /**
     * The listener list
     */
    protected ArrayList<ISelectionChangedListener> listenerList = null;

    /**
     * The current selection
     */
    protected ISelection currentSelection = null;

    protected SDWidgetSelectionProvider() {
        listenerList = new ArrayList<ISelectionChangedListener>();
    }

    /**
     * Adds the given listener from the selection change listener list
     * 
     * @param listener the listener to add
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (!listenerList.contains(listener))
            listenerList.add(listener);
    }

    /**
     * Removes the given listener from the selection change listener list
     * 
     * @param listener the listener to remove
     */
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        listenerList.remove(listener);
    }

    /**
     * Changes the selection to the given selection and inform all listener
     * 
     * @param selection the new current selection
     */
    @Override
    public void setSelection(ISelection selection) {
        currentSelection = selection;
        for (int i = 0; i < listenerList.size(); i++) {
            ISelectionChangedListener list = (ISelectionChangedListener) listenerList.get(i);
            list.selectionChanged(new SelectionChangedEvent(this, currentSelection));
        }
    }

    /**
     * Returns the current selection
     * 
     * @return the current selection
     */
    @Override
    public ISelection getSelection() {
        return currentSelection;
    }

}
