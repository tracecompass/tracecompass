/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadEntryModel;
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
     * Thread ID Comparator
     */
    Comparator<ITimeGraphEntry> TID_COMPARATOR = new Comparator<ITimeGraphEntry>() {
        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            int result = 0;
            ThreadEntryModel model1 = ControlFlowView.getThreadEntryModel(o1);
            ThreadEntryModel model2 = ControlFlowView.getThreadEntryModel(o2);
            if (model1 != null && model2 != null) {
                result = Integer.compare(model1.getThreadId(), model2.getThreadId());
            }
            return result;
        }
    };

    /**
     * Process ID Comparator
     */
    Comparator<ITimeGraphEntry> PID_COMPARATOR = new Comparator<ITimeGraphEntry>() {
        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException();
            }
            int result = 0;
            ThreadEntryModel model1 = ControlFlowView.getThreadEntryModel(o1);
            ThreadEntryModel model2 = ControlFlowView.getThreadEntryModel(o2);
            if (model1 != null && model2 != null) {
                result = Integer.compare(model1.getProcessId(), model2.getProcessId());
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
            ThreadEntryModel model1 = ControlFlowView.getThreadEntryModel(o1);
            ThreadEntryModel model2 = ControlFlowView.getThreadEntryModel(o2);
            if (model1 != null && model2 != null) {
                result = Integer.compare(model1.getParentThreadId(), model2.getParentThreadId());
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

}
