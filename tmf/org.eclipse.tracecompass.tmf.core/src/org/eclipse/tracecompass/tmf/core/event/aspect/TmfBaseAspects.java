/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

import com.google.common.collect.ImmutableList;

/**
 * Some basic aspects that all trace types should be able to use, using methods
 * found in {@link ITmfEvent}.
 *
 * @author Alexandre Montplaisir
 * @author Geneviève Bastien
 * @since 2.0
 */
public final class TmfBaseAspects {

    private static final ITmfEventAspect<ITmfTimestamp> TIMESTAMP_ASPECT = new ITmfEventAspect<ITmfTimestamp>() {
        @Override
        public String getName() {
            return Messages.getMessage(Messages.AspectName_Timestamp);
        }

        @Override
        public String getHelpText() {
            return ITmfEventAspect.EMPTY_STRING;
        }

        @Override
        public @Nullable ITmfTimestamp resolve(ITmfEvent event) {
            return event.getTimestamp();
        }
    };

    private static final ITmfEventAspect<Long> TIMESTAMP_NANOSECOND_ASPECT = new ITmfEventAspect<Long>() {
        @Override
        public String getName() {
            return Messages.getMessage(Messages.AspectName_Timestamp_Nanoseconds);
        }
        @Override
        public String getHelpText() {
            return Messages.getMessage(Messages.AspectName_Timestamp_Nanoseconds_Help);
        }
        @Override
        public @Nullable Long resolve(ITmfEvent event) {
            return event.getTimestamp().toNanos();
        }
        @Override
        public boolean isHiddenByDefault() {
            return true;
        }
    };

    private static final ITmfEventAspect<String> EVENT_TYPE_ASPECT = new ITmfEventAspect<String>() {
        @Override
        public String getName() {
            return Messages.getMessage(Messages.AspectName_EventType);
        }

        @Override
        public String getHelpText() {
            return Messages.getMessage(Messages.AspectHelpText_EventType);
        }

        @Override
        public @Nullable String resolve(ITmfEvent event) {
            ITmfEventType type = event.getType();
            if (type == null) {
                return null;
            }
            return type.getName();
        }
    };

    private static final TmfEventFieldAspect CONTENTS_ASPECT = new TmfEventFieldAspect(Messages.getMessage(Messages.AspectName_Contents), null, event -> event.getContent()) {
        @Override
        public String getHelpText() {
            return Messages.getMessage(Messages.AspectHelpText_Contents);
        }
    };

    private static final ITmfEventAspect<String> TRACE_NAME_ASPECT = new ITmfEventAspect<String>() {
        @Override
        public String getName() {
            return Messages.getMessage(Messages.AspectName_TraceName);
        }

        @Override
        public String getHelpText() {
            return Messages.getMessage(Messages.AspectHelpText_TraceName);
        }

        @Override
        public @Nullable String resolve(ITmfEvent event) {
            return event.getTrace().getName();
        }
    };

    private static final List<ITmfEventAspect<?>> BASE_ASPECTS = ImmutableList.of(
            getTimestampAspect(),
            getEventTypeAspect(),
            getContentsAspect(),
            getTraceNameAspect());

    private TmfBaseAspects() {

    }

    /**
     * Get the aspect for the event timestamp
     *
     * @return The timestamp aspect
     */
    public static ITmfEventAspect<ITmfTimestamp> getTimestampAspect() {
        return TIMESTAMP_ASPECT;
    }

    /**
     * Get the aspect for the event timestamp in nanoseconds
     *
     * @return The timestamp nanosecond aspect
     * @since 6.2
     */
    public static ITmfEventAspect<Long> getTimestampNsAspect() {
        return TIMESTAMP_NANOSECOND_ASPECT;
    }


    /**
     * Get the aspect for the event type
     *
     * @return The aspect for the event type
     */
    public static ITmfEventAspect<String> getEventTypeAspect() {
        return EVENT_TYPE_ASPECT;
    }

    /**
     * Get the aspect for the aggregated event contents (fields)
     *
     * @return The aspect for the aggregate event contents
     */
    public static TmfEventFieldAspect getContentsAspect() {
        return CONTENTS_ASPECT;
    }

    /**
     * Get the aspect for the trace's name (for experiments with many traces)
     *
     * @return The trace name aspect
     */
    public static ITmfEventAspect<String> getTraceNameAspect() {
        return TRACE_NAME_ASPECT;
    }

    /**
     * Get the list of all common base aspects
     *
     * @return the list of base aspects
     */
    public static List<ITmfEventAspect<?>> getBaseAspects() {
        return BASE_ASPECTS;
    }

}