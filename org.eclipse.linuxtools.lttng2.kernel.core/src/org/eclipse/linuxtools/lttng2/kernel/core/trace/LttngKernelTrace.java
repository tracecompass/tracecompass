/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Improved validation
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.trace;

import java.nio.BufferOverflowException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Activator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;

/**
 * This is the specification of CtfTmfTrace for use with LTTng 2.x kernel
 * traces.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class LttngKernelTrace extends CtfTmfTrace {

    /**
     * Default constructor
     */
    public LttngKernelTrace() {
        super();
    }

    /**
     * @since 2.0
     */
    @Override
    public IStatus validate(final IProject project, final String path)  {
        CTFTrace temp;
        IStatus validStatus;
        /*
         * Make sure the trace is openable as a CTF trace. We do this here
         * instead of calling super.validate() to keep the reference to "temp".
         */
        try {
            temp = new CTFTrace(path);
        } catch (CTFReaderException e) {
            validStatus = new Status(IStatus.ERROR,  Activator.PLUGIN_ID, e.toString(), e);
            return validStatus;
        } catch (NullPointerException e){
            validStatus = new Status(IStatus.ERROR,  Activator.PLUGIN_ID, e.toString(), e);
            return validStatus;
        } catch (final BufferOverflowException e) {
            validStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngKernelTrace_TraceReadError + ": " + Messages.LttngKernelTrace_MalformedTrace); //$NON-NLS-1$
            return validStatus;
        }

        /* Make sure the domain is "kernel" in the trace's env vars */
        String dom = temp.getEnvironment().get("domain"); //$NON-NLS-1$
        temp.dispose();
        if (dom != null && dom.equals("\"kernel\"")) { //$NON-NLS-1$
            return Status.OK_STATUS;
        }
        validStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngKernelTrace_DomainError);
        return validStatus;
    }

}
