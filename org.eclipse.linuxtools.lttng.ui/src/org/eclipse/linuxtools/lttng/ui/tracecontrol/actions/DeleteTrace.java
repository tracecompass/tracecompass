/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.tracecontrol.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.lttng.ui.tracecontrol.Messages;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * <b><u>DeleteTrace</u></b>
 * <p>
 * Action implementation to delete a new trace.
 * </p>
 */
public class DeleteTrace implements IObjectActionDelegate, IWorkbenchWindowActionDelegate, IViewActionDelegate {
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private List<TraceResource> fSelectedTraces;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    
    public DeleteTrace() {
        fSelectedTraces = new ArrayList<TraceResource>();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
    }

    /**
     * Gets the first selected target.
     * 
     * @return first selected target.
     */
    protected TraceResource getSelectedTarget() {
        if (fSelectedTraces.size() > 0) {
            return (TraceResource) fSelectedTraces.get(0);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction arg0) {
    	
		boolean confirm = MessageDialog.openConfirm(getShell(),
				Messages.DeleteTrace_ConfirmTitle,
				Messages.DeleteTrace_ConfirmMessage);
		if (!confirm) {
			return;
		}

        int size = fSelectedTraces.size();
        for (int i = 0; i < size; i++) {

            final TraceResource trace = (TraceResource) fSelectedTraces.get(i);

            trace.getParent().removeTrace(trace);
            
            ISystemRegistry registry = SystemStartHere.getSystemRegistry();
            registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, trace, trace.getParent(), trace.getSubSystem(), null);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        
        if (selection instanceof IStructuredSelection) {
            fSelectedTraces.clear();

            // store the selected targets to be used when running
            Iterator<IStructuredSelection> theSet = ((IStructuredSelection) selection).iterator();
            while (theSet.hasNext()) {
                Object obj = theSet.next();
                if (obj instanceof TraceResource) {
                    fSelectedTraces.add((TraceResource)obj);
                }
            }
        }

    }

    /**
     * Returns the active workbench shell of this plug-in.
     * 
     * @return active workbench shell.
     */
    protected Shell getShell() {
        return SystemBasePlugin.getActiveWorkbenchShell();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    }
}
