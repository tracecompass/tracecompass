/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.ui.project.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Operation to modify the workspace that refreshes workspace at the end of the operation.
 *
 * For refreshing periodically use {@link WorkspaceModifyOperation} instead.
 *
 * @author Bernd Hufmann
 *
 */
public abstract class TmfWorkspaceModifyOperation implements IRunnableWithProgress {

    private ISchedulingRule rule;

    /**
     * Creates a new operation.
     */
    protected TmfWorkspaceModifyOperation() {
        this(ResourcesPlugin.getWorkspace().getRoot());
    }

    /**
     * Creates a new operation that will run using the provided scheduling rule.
     *
     * @param rule
     *            The ISchedulingRule to use or <code>null</code>.
     */
    protected TmfWorkspaceModifyOperation(ISchedulingRule rule) {
        this.rule = rule;
    }

    @Override
    public synchronized final void run(IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        final InvocationTargetException[] iteHolder = new InvocationTargetException[1];
        try {
            IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor pm) throws CoreException {
                    try {
                        execute(pm);
                    } catch (InvocationTargetException e) {
                        // Pass it outside the workspace runnable
                        iteHolder[0] = e;
                    } catch (InterruptedException e) {
                        // Re-throw as OperationCanceledException, which will be
                        // caught and re-thrown as InterruptedException below.
                        throw new OperationCanceledException(e.getMessage());
                    }
                    // CoreException and OperationCanceledException are propagated
                }
            };

            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace.run(workspaceRunnable, rule, IWorkspace.AVOID_UPDATE, monitor);
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        } catch (OperationCanceledException e) {
            throw new InterruptedException(e.getMessage());
        }
        // Re-throw the InvocationTargetException, if any occurred
        if (iteHolder[0] != null) {
            throw iteHolder[0];
        }
    }

    /**
     * Performs the steps that are to be treated as a single logical workspace
     * change.
     * <p>
     * Subclasses must implement this method.
     * </p>
     *
     * @param monitor
     *            the progress monitor to use to display progress and field user
     *            requests to cancel
     * @exception CoreException
     *                if the operation fails due to a CoreException
     * @exception InvocationTargetException
     *                if the operation fails due to an exception other than
     *                CoreException
     * @exception InterruptedException
     *                if the operation detects a request to cancel, using
     *                <code>IProgressMonitor.isCanceled()</code>, it should exit
     *                by throwing <code>InterruptedException</code>. It is also
     *                possible to throw <code>OperationCanceledException</code>,
     *                which gets mapped to <code>InterruptedException</code> by
     *                the <code>run</code> method.
     */
    protected abstract void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException;
}