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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IEnableChannelDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;

/**
 * <p>
 * Base implementation of a command handler to enable a trace channel.
 * </p>
 *
 * @author Bernd Hufmann
 */
abstract class BaseEnableChannelHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    protected CommandParameter fParam;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Enables channels with given names which are part of this domain. If a
     * given channel doesn't exists it creates a new channel with the given
     * parameters (or default values if given parameter is null).
     *
     * @param param
     *            - a parameter instance with data for the command execution
     * @param channelNames
     *            - a list of channel names to enable on this domain
     * @param info
     *            - channel information to set for the channel (use null for
     *            default)
     * @param isKernel
     *            - a flag for indicating kernel or UST.
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If something goes wrong when enabling the channel
     */
    public abstract void enableChannel(CommandParameter param,
            List<String> channelNames, IChannelInfo info, boolean isKernel,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * @param param - a parameter instance with data for the command execution
     * @return returns the relevant domain (null if domain is not known)
     */
    public abstract TraceDomainComponent getDomain(CommandParameter param);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        fLock.lock();
        try {
            final CommandParameter param = fParam.clone();

            final IEnableChannelDialog dialog =  TraceControlDialogFactory.getInstance().getEnableChannelDialog();
            dialog.setTargetNodeComponent(param.getSession().getTargetNode());
            dialog.setDomainComponent(getDomain(param));
            dialog.setHasKernel(param.getSession().hasKernelProvider());

            if (dialog.open() != Window.OK) {
                return null;
            }

            Job job = new Job(Messages.TraceControl_CreateChannelStateJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    Exception error = null;

                    List<String> channelNames = new ArrayList<>();
                    channelNames.add(dialog.getChannelInfo().getName());

                    try {
                        enableChannel(param, channelNames, dialog.getChannelInfo(), dialog.isKernel(), monitor);
                    } catch (ExecutionException e) {
                        error = e;
                    }

                    // refresh in all cases
                    refresh(param);

                    if (error != null) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_CreateChannelStateFailure, error);
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setUser(true);
            job.schedule();
        } finally {
            fLock.unlock();
        }
        return null;
    }

}
