/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Add UST callstack state system
 **********************************************************************/

package org.eclipse.linuxtools.lttng2.ust.core.trace;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.internal.lttng2.ust.core.Activator;
import org.eclipse.linuxtools.internal.lttng2.ust.core.trace.callstack.LttngUstCallStackProvider;
import org.eclipse.linuxtools.tmf.core.callstack.CallStackStateProvider;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;

/**
 * Class to contain LTTng-UST traces
 *
 * @author Matthew Khouzam
 * @since 2.1
 */
public class LttngUstTrace extends CtfTmfTrace {

    /** Name of the history file for the callstack state system */
    private static final String CALLSTACK_FILENAME = "ust-callstack.ht"; //$NON-NLS-1$

    /**
     * Default constructor
     */
    public LttngUstTrace() {
        super();
    }

    @Override
    public IStatus validate(final IProject project, final String path)  {
        CTFTrace temp;
        IStatus status;
        /*  Make sure the trace is openable as a CTF trace. */
        try {
            temp = new CTFTrace(path);
        } catch (CTFReaderException e) {
            status = new Status(IStatus.ERROR,  Activator.PLUGIN_ID, e.toString(), e);
            return status;
        } catch (NullPointerException e){
            status = new Status(IStatus.ERROR,  Activator.PLUGIN_ID, e.toString(), e);
            return status;
        }

        /* Make sure the domain is "ust" in the trace's env vars */
        String dom = temp.getEnvironment().get("domain"); //$NON-NLS-1$
        temp.dispose();
        if (dom != null && dom.equals("\"ust\"")) { //$NON-NLS-1$
            return Status.OK_STATUS;
        }
        status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngUstTrace_DomainError);
        return status;
    }

    @Override
    public IStatus buildStateSystem() {
        super.buildStateSystem();

        /*
         * Build the state system for the UST Callstack (will be empty if the
         * required events are not present).
         */
        String directory = TmfTraceManager.getSupplementaryFileDir(this);
        final File htFile = new File(directory + CALLSTACK_FILENAME);
        ITmfStateProvider csInput = new LttngUstCallStackProvider(this);

        try {
            ITmfStateSystem ss = TmfStateSystemFactory.newFullHistory(htFile, csInput, false);
            registerStateSystem(CallStackStateProvider.ID, ss);
        }  catch (TmfTraceException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
        }

        return Status.OK_STATUS;
    }
}
