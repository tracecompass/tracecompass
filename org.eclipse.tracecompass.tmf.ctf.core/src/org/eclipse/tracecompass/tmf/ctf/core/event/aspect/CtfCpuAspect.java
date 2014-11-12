/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event.aspect;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;

/**
 * "CPU" event aspect for CTF traces.
 *
 * @author Alexandre Montplaisir
 */
public class CtfCpuAspect extends TmfCpuAspect {

    @Override
    public String resolve(ITmfEvent event) {
        if (!(event instanceof CtfTmfEvent)) {
            return EMPTY_STRING;
        }
        int cpu = ((CtfTmfEvent) event).getCPU();

        @SuppressWarnings("null")
        @NonNull String ret = String.valueOf(cpu);
        return ret;
    }

    @Override
    public String getFilterId() {
        return null;
    }

}
