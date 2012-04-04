/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IAddContextDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * <b><u>BaseAddContextHandler</u></b>
 * <p>
 * Base command handler implementation to add contexts.
 * </p>
 */
abstract public class BaseAddContextHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The command execution parameter.
     */
    protected CommandParameter fParam = null;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /**
     * Adds contexts to channel(s) and/or event(s) 
     * @param param - a parameter instance with data for the command execution
     * @param contextNames - list contexts to add
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    abstract public void addContexts(CommandParameter param, List<String> contextNames, IProgressMonitor monitor) throws ExecutionException;
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }
        fLock.lock();
        try {
            // Make a copy for thread safety
            final CommandParameter param = fParam.clone();

            UIJob getJob = new UIJob(Messages.TraceControl_GetContextJob) {
                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {

                    try {
                        final List<String> availableContexts = param.getSession().getContextList(monitor);
                        final IAddContextDialog dialog = TraceControlDialogFactory.getInstance().getAddContextDialog();
                        dialog.setAvalibleContexts(availableContexts); 

                        if ((dialog.open() != Window.OK) || (dialog.getContexts().isEmpty())) {
                            return Status.OK_STATUS;
                        }

                        Job addJob = new Job(Messages.TraceControl_AddContextJob) {
                            @Override
                            protected IStatus run(IProgressMonitor monitor) {
                                StringBuffer errorString = new StringBuffer();

                                try {
                                    List<String> contextNames = dialog.getContexts();
                                    addContexts(param, contextNames, monitor);

                                } catch (ExecutionException e) {
                                    errorString.append(e.toString());
                                    errorString.append('\n');
                                }

                                // get session configuration in all cases
                                try {
                                    param.getSession().getConfigurationFromNode(monitor);
                                } catch (ExecutionException e) {
                                    errorString.append(Messages.TraceControl_ListSessionFailure);
                                    errorString.append(": "); //$NON-NLS-1$
                                    errorString.append(e.toString());
                                } 

                                if (errorString.length() > 0) {
                                    return new Status(Status.ERROR, Activator.PLUGIN_ID, errorString.toString());
                                }
                                return Status.OK_STATUS;
                            }
                        };
                        addJob.setUser(true);
                        addJob.schedule();
                    } catch (ExecutionException e) {
                        return new Status(Status.ERROR, Activator.PLUGIN_ID, e.toString());
                    }

                    return Status.OK_STATUS;
                }
            };
            getJob.setUser(false);
            getJob.schedule();

        } finally {
            fLock.unlock();
        }
        return null;
    }
}
