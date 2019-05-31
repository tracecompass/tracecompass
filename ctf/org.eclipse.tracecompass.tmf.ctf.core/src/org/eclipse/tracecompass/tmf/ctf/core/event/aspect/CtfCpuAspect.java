/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
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
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;

/**
 * "CPU" event aspect for CTF traces. Resolves cpu_id in stream first, event
 * fields second.
 *
 * @author Alexandre Montplaisir
 */
public class CtfCpuAspect extends TmfCpuAspect {

    /**
     * Refered in the CTF spec
     */
    private static final @NonNull String CPU_ID = "cpu_id"; //$NON-NLS-1$

    @Override
    public Integer resolve(ITmfEvent event) {
        if (!(event instanceof CtfTmfEvent)) {
            return null;
        }
        int cpu = ((CtfTmfEvent) event).getCPU();
        if (cpu == IEventDefinition.UNKNOWN_CPU) {
            ITmfEventField content = event.getContent();
            Long fieldValue = null;
            if (content != null) {
                fieldValue = content.getFieldValue(Long.class, CPU_ID);
            }
            return fieldValue == null ? null : fieldValue.intValue();
        }
        return cpu;
    }
}
