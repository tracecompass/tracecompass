/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;

/**
 * This is the specification of CtfTmfTrace for use with LTTng 2.x kernel
 * traces.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class LttngKernelTrace extends CtfTmfTrace {

    private static final int CONFIDENCE = 100;

    /**
     * Default constructor
     */
    public LttngKernelTrace() {
        super();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation sets the confidence to 100 if the trace is a valid
     * CTF trace in the "kernel" domain.
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        /*
         * Make sure the trace is openable as a CTF trace. We do this here
         * instead of calling super.validate() to keep the reference to "temp".
         */
        try (CTFTrace temp = new CTFTrace(path);) {
            /* Make sure the domain is "kernel" in the trace's env vars */
            String dom = temp.getEnvironment().get("domain"); //$NON-NLS-1$
            if (dom != null && dom.equals("\"kernel\"")) { //$NON-NLS-1$
                return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
            }
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngKernelTrace_DomainError);

        } catch (CTFReaderException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
        } catch (NullPointerException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
        } catch (final BufferOverflowException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngKernelTrace_TraceReadError + ": " + Messages.LttngKernelTrace_MalformedTrace); //$NON-NLS-1$
        }
    }

}
