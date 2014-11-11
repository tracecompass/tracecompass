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

package org.eclipse.tracecompass.internal.lttng2.kernel.core.event.aspect;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;

import com.google.common.collect.ImmutableList;

/**
 * Event aspects for LTTng kernel traces.
 *
 * @author Alexandre Montplaisir
 */
public final class LttngEventAspects {

    private LttngEventAspects() {}

    @SuppressWarnings("null")
    private static final @NonNull Iterable<ITmfEventAspect> LTTNG_ASPECTS =
            ImmutableList.of(
                    ITmfEventAspect.BaseAspects.TIMESTAMP,
                    new LttngChannelAspect(),
                    ITmfEventAspect.BaseAspects.EVENT_TYPE,
                    ITmfEventAspect.BaseAspects.CONTENTS);

    private static class LttngChannelAspect implements ITmfEventAspect {

        @Override
        public String getName() {
            String ret = Messages.AspectName_Channel;
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof CtfTmfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((CtfTmfEvent) event).getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterId() {
            return ITmfEvent.EVENT_FIELD_REFERENCE;
        }
    }

    /**
     * Get the event aspects defined for LTTng kernel traces.
     *
     * @return The set of aspects
     */
    public static Iterable<ITmfEventAspect> getAspects() {
        return LTTNG_ASPECTS;
    }
}
