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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * ControlFlowEntry comparators. These do not allow for null arguments.
 *
 * @author Bernd Hufmann
 * @noimplement This interface only contains static definitions.
 */
public interface IControlFlowEntryComparator {

    /**
     * Process Name Comparator
     */
    Comparator<ITimeGraphEntry> PROCESS_NAME_COMPARATOR = new Comparator<ITimeGraphEntry>() {
        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            if (o1 == null || o2 == null || o1.getName() == null || o2.getName() == null) {
                throw new IllegalArgumentException();
            }
            return o1.getName().compareTo(o2.getName());
        }
    };

    /**
     * TreadID Comparator
     */
    Comparator<ITimeGraphEntry> TID_COMPARATOR = new Comparator<ITimeGraphEntry>() {
        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            int result = 0;
            if ((o1 instanceof ControlFlowEntry) && (o2 instanceof ControlFlowEntry)) {
                ControlFlowEntry entry1 = (ControlFlowEntry) o1;
                ControlFlowEntry entry2 = (ControlFlowEntry) o2;
                result = Integer.compare(entry1.getThreadId(), entry2.getThreadId());
            }
            return result;
        }
    };

    /**
     * Parent ThreadID Comparator
     */
    Comparator<ITimeGraphEntry> PTID_COMPARATOR = new Comparator<ITimeGraphEntry>() {
        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            int result = 0;
            if ((o1 instanceof ControlFlowEntry) && (o2 instanceof ControlFlowEntry)) {
                ControlFlowEntry entry1 = (ControlFlowEntry) o1;
                ControlFlowEntry entry2 = (ControlFlowEntry) o2;
                result = Integer.compare(entry1.getParentThreadId(), entry2.getParentThreadId());
            }
            return result;
        }
    };

    /**
     * Birth time Comparator
     */
    Comparator<ITimeGraphEntry> BIRTH_TIME_COMPARATOR = new Comparator<ITimeGraphEntry>() {
        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            return Long.compare(o1.getStartTime(), o2.getStartTime());
        }
    };

    /**
     * Scheduling Comparator - this is for the link optimizer. It compares the
     * values in an invisible column. (scheduled position)
     */
    Comparator<ITimeGraphEntry> SCHEDULING_COMPARATOR = new Comparator<ITimeGraphEntry>() {
        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            if ((o1 instanceof ControlFlowEntry) && (o2 instanceof ControlFlowEntry)) {
                return Long.compare(((ControlFlowEntry) o1).getSchedulingPosition(), ((ControlFlowEntry) o2).getSchedulingPosition());
            }
            return 0;
        }
    };

}
