/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Update to new Event Table API
 *******************************************************************************/

package org.eclipse.tracecompass.btf.core.trace;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.btf.core.event.BtfEvent;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfEventFieldAspect;

import com.google.common.collect.ImmutableList;

/**
 * Columns to use in the BTF event table
 *
 * @author Alexandre Montplaisir
 */
public final class BtfEventAspects {

    private BtfEventAspects() {}

    private static final @NonNull Iterable<ITmfEventAspect> BTF_ASPECTS =
            NonNullUtils.checkNotNull(ImmutableList.of(
                    ITmfEventAspect.BaseAspects.TIMESTAMP,
                    new BtfSourceAspect(),
                    new BtfSourceInstanceAspect(),
                    ITmfEventAspect.BaseAspects.EVENT_TYPE,
                    new BtfTargetAspect(),
                    new BtfTargetInstanceAspect(),
                    new BtfEventAspect(),
                    new BtfNotesAspect()
                    ));

    /**
     * The "source" aspect, whose value comes from {@link ITmfEvent#getSource()}
     */
    private static class BtfSourceAspect implements ITmfEventAspect {

        @Override
        public String getName() {
            return BtfColumnNames.SOURCE.toString();
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof BtfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((BtfEvent) event).getSource();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterId() {
            return ITmfEvent.EVENT_FIELD_SOURCE;
        }
    }

    /**
     * The "source instance" aspect, whose value comes from the field of the
     * same name.
     */
    private static class BtfSourceInstanceAspect extends TmfEventFieldAspect {
        public BtfSourceInstanceAspect() {
            super(BtfColumnNames.SOURCE_INSTANCE.toString(),
                    BtfColumnNames.SOURCE_INSTANCE.toString());
        }
    }

    /**
     * The "target" aspect, taking its value from
     * {@link ITmfEvent#getReference()}.
     */
    private static class BtfTargetAspect implements ITmfEventAspect {

        @Override
        public String getName() {
             return BtfColumnNames.TARGET.toString();
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof BtfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((BtfEvent) event).getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterId() {
            return ITmfEvent.EVENT_FIELD_REFERENCE;
        }
    }

    /**
     * The "target instance" aspect, whose value comes from the field of the
     * same name.
     */
    private static class BtfTargetInstanceAspect extends TmfEventFieldAspect {
        public BtfTargetInstanceAspect() {
            super(BtfColumnNames.TARGET_INSTANCE.toString(),
                    BtfColumnNames.TARGET_INSTANCE.toString());
        }
    }

    /**
     * The "event" aspect, whose value comes from the field of the same name.
     */
    private static class BtfEventAspect extends TmfEventFieldAspect {
        public BtfEventAspect() {
            super(BtfColumnNames.EVENT.toString(),
                    BtfColumnNames.EVENT.toString());
        }
    }

    /**
     * The "notes" column, whose value comes from the field of the same name, if
     * present.
     */
    private static class BtfNotesAspect extends TmfEventFieldAspect {
        public BtfNotesAspect() {
            super(BtfColumnNames.NOTES.toString(),
                    BtfColumnNames.NOTES.toString());
        }
    }

    /**
     * Return the event aspects defined for BTF traces.
     *
     * @return The aspects
     */
    public static @NonNull Iterable<ITmfEventAspect> getAspects() {
        return BTF_ASPECTS;
    }
}