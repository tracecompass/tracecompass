/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Filled with content
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.ui.views.control;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponentChangedListener;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceControlRoot;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

/**
 * <b><u>ControlView</u></b>
 * <p>
 * View implementation for Trace Control. 
 * </p>
 */
public class ControlView extends ViewPart implements ITraceControlComponentChangedListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.internal.lttng2.ui.views.control"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The tree viewer.
     */
    private TreeViewer fTreeViewer = null;
    
    /**
     * The trace control root node. This provides access to the whole model. 
     */
    private ITraceControlComponent fRoot = null;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    
    /**
     * @return returns the trace control tree node (model).
     */
    public ITraceControlComponent getTraceControlRoot() {
        return fRoot;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        // Create tree viewer
        fTreeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        ColumnViewerToolTipSupport.enableFor(fTreeViewer);

        fTreeViewer.setContentProvider(new TraceControlContentProvider());
        fTreeViewer.setLabelProvider(new TraceControlLabelProvider());

        // Create model root
        fRoot = new TraceControlRoot();
        fRoot.addComponentListener(this);
        fTreeViewer.setInput(fRoot);

        // Create context menu for the tree viewer
        createContextMenu();
        
        getSite().setSelectionProvider(fTreeViewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        fTreeViewer.getControl().setFocus();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponentChangedListener#componentAdded(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent, org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent)
     */
    @Override
    public void componentAdded(ITraceControlComponent parent, ITraceControlComponent component) {
        componentChanged(component);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponentChangedListener#componentRemoved(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent, org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent)
     */
    @Override
    public void componentRemoved(ITraceControlComponent parent, ITraceControlComponent component) {
        componentChanged(component);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponentChangedListener#componentChanged(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent)
     */
    @Override
    public void componentChanged(ITraceControlComponent component) {
        if (fTreeViewer.getTree().isDisposed()) {
            return;
        }

        fTreeViewer.getTree().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTreeViewer.getTree().isDisposed()) {
                    return;
                }
                fTreeViewer.refresh();
                // Change selection needed 
                final ISelection sel = fTreeViewer.getSelection();
                fTreeViewer.setSelection(null);
                fTreeViewer.setSelection(sel);
            }
        });
    }
    
    /**
     * Sets the selected component in the tree
     * @param component - component to select
     */
    public void setSelection(ITraceControlComponent component) {
        StructuredSelection selection = new StructuredSelection(component);
        fTreeViewer.setSelection(selection);
    }

    /**
     * Sets the selected components in the tree
     * @param component - array of components to select
     */
    public void setSelection(ITraceControlComponent[] components) {
        StructuredSelection selection = new StructuredSelection(components);
        fTreeViewer.setSelection(selection);
    }
    
//    public ITraceControlComponent getSelection() {
//        ISelection selection = fTreeViewer.getSelection();
//        
//    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    private void createContextMenu() {
        // First we create a menu Manager
        final MenuManager menuManager = new MenuManager();
        final Menu menu = menuManager.createContextMenu(fTreeViewer.getTree());
        // Set the MenuManager
        fTreeViewer.getTree().setMenu(menu);
        getSite().registerContextMenu(menuManager, fTreeViewer);
    }
}
