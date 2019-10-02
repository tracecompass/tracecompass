/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Event aspect representing the CPU of a trace event. Trace types that do have
 * the notion of CPU can use this to expose it in their traces.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TmfCpuAspect extends TmfDeviceAspect {

    @Override
    public final String getName() {
        return Messages.getMessage(Messages.AspectName_CPU);
    }

    @Override
    public final String getHelpText() {
        return Messages.getMessage(Messages.AspectHelpText_CPU);
    }

    /**
     * Returns the CPU number of the CPU on which this event was executed or
     * {@code null} if the CPU is not available for an event.
     */
    @Override
    public abstract @Nullable Integer resolve(ITmfEvent event);

    @Override
    public boolean equals(@Nullable Object other) {
        /*
         * Consider all sub-instance of this type "equal", so that they get
         * merged in a single CPU column/aspect.
         */
        return (other instanceof TmfCpuAspect);
    }

    @Override
    public String getDeviceType() {
        return "cpu"; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
