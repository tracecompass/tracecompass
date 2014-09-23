/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Command handler implementation to execute command calibrate to quantify LTTng overhead.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class CalibrateHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The command execution parameter.
     */
    protected DomainCommandParameter fParam = null;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }
        fLock.lock();
        try {
            // Make a copy for thread safety
            final DomainCommandParameter param = fParam.clone();

            Job addJob = new Job(Messages.TraceControl_AddCalibrateJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        param.getDomain().calibrate(monitor);
                    } catch (ExecutionException e) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_AddCalibrateFailure, e);
                    }

                    return Status.OK_STATUS;
                }
            };
            addJob.setUser(true);
            addJob.schedule();

        } finally {
            fLock.unlock();
        }
        return Status.OK_STATUS;
    }

    @Override
    public boolean isEnabled() {

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        TraceDomainComponent domain = null;
        TraceSessionComponent session = null;

        // Check if one domain is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceDomainComponent) {
                    TraceDomainComponent tmpDomain = (TraceDomainComponent) element;
                    session = (TraceSessionComponent) tmpDomain.getParent();

                    // Add only TraceDomainComponent whose TraceSessionComponent parent is not destroyed
                    if ((!session.isDestroyed())) {
                        domain = tmpDomain;
                    }
                }
            }
        }

        boolean isEnabled = domain != null;

        fLock.lock();
        try {
            fParam = null;
            if (isEnabled) {
                fParam = new DomainCommandParameter(session, domain);
            }
        } finally {
            fLock.unlock();
        }

        return isEnabled;
    }
}
