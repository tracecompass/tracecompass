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
 **********************************************************************/

package org.eclipse.linuxtools.lttng2.ust.core.trace;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.internal.lttng2.ust.core.Activator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;

/**
 * Class to contain LTTng-UST traces
 *
 * @author Matthew Khouzam
 * @since 2.1
 */
public class LttngUstTrace extends CtfTmfTrace {

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
}
