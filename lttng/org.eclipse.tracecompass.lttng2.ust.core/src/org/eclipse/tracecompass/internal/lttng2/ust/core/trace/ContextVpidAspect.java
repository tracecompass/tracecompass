/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.trace;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxPidAspect;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * An aspect getting the process from a context field in the event
 *
 * @author Geneviève Bastien
 */
public class ContextVpidAspect extends LinuxPidAspect {

    private final ILttngUstEventLayout fLayout;

    /**
     * Constructor with a layout
     *
     * @param layout
     *            The event layout used by the trace this aspect is for
     */
    public ContextVpidAspect(ILttngUstEventLayout layout) {
        fLayout = layout;
    }

    @Override
    public @Nullable Integer resolve(@NonNull ITmfEvent event) {
        ITmfEventField content = event.getContent();
        Long pid = content.getFieldValue(Long.class, fLayout.contextVpid());
        return pid == null ? null : pid.intValue();
    }

}
