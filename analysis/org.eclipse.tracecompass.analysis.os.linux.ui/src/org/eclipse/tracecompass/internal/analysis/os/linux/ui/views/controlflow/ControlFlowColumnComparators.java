/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.ITimeGraphEntryComparator;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

import com.google.common.collect.ImmutableList;

/**
 *
 * Class with comparators used for sorting the ControlFlowEntries based based on
 * the column selection.
 *
 * @author Bernd Hufmann
 *
 */
public final class ControlFlowColumnComparators {

    /**
     * Default constructor
     */
    private ControlFlowColumnComparators() {}

    /**
     * Process Name comparator. This compares first the trace, then the process name, then the
     * birth time, then the TID finally the parent TID.
     */
    public static final ITimeGraphEntryComparator PROCESS_NAME_COLUMN_COMPARATOR = new ITimeGraphEntryComparator() {
        private final List<Comparator<ITimeGraphEntry>> SECONDARY_COMPARATORS = init();
        private int fDirection = SWT.DOWN;

        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            /* First sort by process name */
            int result = IControlFlowEntryComparator.PROCESS_NAME_COMPARATOR.compare(o1, o2);
            return compareList(result, fDirection, SECONDARY_COMPARATORS, o1, o2);
        }

        @Override
        public void setDirection(int direction) {
            fDirection = direction;
        }

        private List<Comparator<ITimeGraphEntry>> init() {
            ImmutableList.Builder<Comparator<ITimeGraphEntry>> builder = ImmutableList.builder();
            builder.add(IControlFlowEntryComparator.BIRTH_TIME_COMPARATOR)
                .add(IControlFlowEntryComparator.TID_COMPARATOR)
                .add(IControlFlowEntryComparator.PTID_COMPARATOR);
            return builder.build();
        }
    };

    /**
     * Process TID comparator. This compares first the trace, then the process TID, then the
     * birth time, then the process name finally the parent TID.
     */
    public static final ITimeGraphEntryComparator TID_COLUMN_COMPARATOR = new ITimeGraphEntryComparator() {

        private final List<Comparator<ITimeGraphEntry>> SECONDARY_COMPARATORS = init();
        private int fDirection = SWT.DOWN;

        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            /* First sort by TID */
            int result = IControlFlowEntryComparator.TID_COMPARATOR.compare(o1, o2);
            return compareList(result, fDirection, SECONDARY_COMPARATORS, o1, o2);
        }

        @Override
        public void setDirection(int direction) {
            fDirection = direction;
        }

        private List<Comparator<ITimeGraphEntry>> init() {
            ImmutableList.Builder<Comparator<ITimeGraphEntry>> builder = ImmutableList.builder();
            builder.add(IControlFlowEntryComparator.BIRTH_TIME_COMPARATOR)
                .add(IControlFlowEntryComparator.PROCESS_NAME_COMPARATOR)
                .add(IControlFlowEntryComparator.PTID_COMPARATOR);
            return builder.build();
        }

    };

    /**
     * Process PTID comparator. This compares first the trace, then the process
     * parent TID, then the birth time, then the process name finally the TID.
     */
    public static final ITimeGraphEntryComparator PTID_COLUMN_COMPARATOR = new ITimeGraphEntryComparator() {

        private final List<Comparator<ITimeGraphEntry>> SECONDARY_COMPARATORS = init();
        private int fDirection = SWT.DOWN;

        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            /* First sort by PTID */
            int result = IControlFlowEntryComparator.PTID_COMPARATOR.compare(o1, o2);
            return compareList(result, fDirection, SECONDARY_COMPARATORS, o1, o2);
        }

        @Override
        public void setDirection(int direction) {
            fDirection = direction;
        }

        private List<Comparator<ITimeGraphEntry>> init() {
            ImmutableList.Builder<Comparator<ITimeGraphEntry>> builder = ImmutableList.builder();
            builder.add(IControlFlowEntryComparator.BIRTH_TIME_COMPARATOR)
                .add(IControlFlowEntryComparator.PROCESS_NAME_COMPARATOR)
                .add(IControlFlowEntryComparator.TID_COMPARATOR);
            return builder.build();
        }
    };

    /**
     * Process birth time comparator. This compares first the trace, then the
     * birth time, then the process name, then the TID finally the parent TID.
     */
    public static final ITimeGraphEntryComparator BIRTH_TIME_COLUMN_COMPARATOR = new ITimeGraphEntryComparator() {
        private final List<Comparator<ITimeGraphEntry>> SECONDARY_COMPARATORS = init();
        private int fDirection = SWT.DOWN;

        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {

            /* Sort all child processes according to birth time. */
            int result = IControlFlowEntryComparator.BIRTH_TIME_COMPARATOR.compare(o1, o2);
            return compareList(result, fDirection, SECONDARY_COMPARATORS, o1, o2);
        }

        @Override
        public void setDirection(int direction) {
            fDirection = direction;
        }

        private List<Comparator<ITimeGraphEntry>> init() {
            ImmutableList.Builder<Comparator<ITimeGraphEntry>> builder = ImmutableList.builder();
            builder.add(IControlFlowEntryComparator.PROCESS_NAME_COMPARATOR)
                .add(IControlFlowEntryComparator.TID_COMPARATOR)
                .add(IControlFlowEntryComparator.PTID_COMPARATOR);
            return builder.build();
        }
    };

    private static class SchedulingComparator implements ITimeGraphEntryComparator {
        private final List<Comparator<ITimeGraphEntry>> SECONDARY_COMPARATORS = init();
        private int fDirection = SWT.DOWN;
        private Map<ITimeGraphEntry, Long> fSchedulingPosition;

        public SchedulingComparator(Map<ITimeGraphEntry, Long> schedulingPositions) {
            fSchedulingPosition = schedulingPositions;
        }

        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            int result = Long.compare(fSchedulingPosition.getOrDefault(o1, -1L), fSchedulingPosition.getOrDefault(o2, -1L));
            return compareList(result, fDirection, SECONDARY_COMPARATORS, o1, o2);
        }

        @Override
        public void setDirection(int direction) {
            fDirection = direction;
        }

        private static List<Comparator<ITimeGraphEntry>> init() {
            ImmutableList.Builder<Comparator<ITimeGraphEntry>> builder = ImmutableList.builder();
            builder.add(IControlFlowEntryComparator.BIRTH_TIME_COMPARATOR)
                .add(IControlFlowEntryComparator.PROCESS_NAME_COMPARATOR)
                .add(IControlFlowEntryComparator.TID_COMPARATOR)
                .add(IControlFlowEntryComparator.PTID_COMPARATOR);
            return builder.build();
        }
    }

    /**
     * Scheduling comparator for a given position map
     *
     * @param schedulingPositions
     *            A map of entry to its scheduling position
     * @return The comparator
     */
    public static Comparator<ITimeGraphEntry> newSchedulingComparator(Map<ITimeGraphEntry, Long> schedulingPositions) {
        return new SchedulingComparator(schedulingPositions);
    }

    private static int compareList(int prevResult, int direction, List<Comparator<ITimeGraphEntry>> comps, ITimeGraphEntry o1, ITimeGraphEntry o2) {
        int result = prevResult;
        for (Comparator<ITimeGraphEntry> comparator : comps) {
            if (result == 0) {
                result = comparator.compare(o1, o2);
                if (direction == SWT.UP) {
                    result = -result;
                }
            }
        }
        return result;
    }

}
