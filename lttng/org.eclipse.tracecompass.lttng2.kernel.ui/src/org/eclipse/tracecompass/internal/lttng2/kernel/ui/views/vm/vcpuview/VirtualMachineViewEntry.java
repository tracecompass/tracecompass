/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview;

import java.util.Collection;
import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.trace.VirtualMachineExperiment;
import org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview.VirtualMachineCommon.Type;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.Multimap;

/**
 * An entry, or row, in the Virtual CPU view
 *
 * @author Mohamad Gebai
 */
public final class VirtualMachineViewEntry extends TimeGraphEntry {

    private static final Comparator<ITimeGraphEntry> COMPARATOR = new Comparator<ITimeGraphEntry>() {

        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {
            if (!((o1 instanceof VirtualMachineViewEntry) && (o2 instanceof VirtualMachineViewEntry))) {
                return 0;
            }
            VirtualMachineViewEntry entry1 = (VirtualMachineViewEntry) o1;
            VirtualMachineViewEntry entry2 = (VirtualMachineViewEntry) o2;
            int result = entry1.getType().compareTo(entry2.getType());
            if (result == 0) {
                /* If there is a numeric ID, use it instead */
                if (entry1.getNumericId() != -1) {
                    result = entry1.getNumericId().compareTo(entry2.getNumericId());
                } else {
                    result = entry1.getId().compareTo(entry2.getId());
                }
            }
            return result;
        }
    };

    private final String fId;
    private final @Nullable String fVmName;
    private final ITmfTrace fTrace;
    private final VirtualMachineExperiment fExperiment;
    private final Type fType;
    private final Integer fNid;
    private @Nullable Multimap<Integer, ITmfStateInterval> fThreadIntervals = null;

    /**
     * Private constructor using a builder to build an entry
     *
     * @param builder
     *            The builder from which to build this entry
     */
    private VirtualMachineViewEntry(VmEntryBuilder builder) {
        super(builder.fbEntryName, builder.fbStartTime, builder.fbEndTime);
        fId = builder.fbId;
        fExperiment = builder.fbExperiment;
        /* If trace is not set, initialize to experiment */
        ITmfTrace trace = builder.fbTrace;
        if (trace == null) {
            trace = fExperiment;
        }
        fTrace = trace;
        fType = builder.fbType;
        Integer nid = builder.fbNid;
        if (nid == null) {
            nid = -1;
        }
        fNid = nid;
        fVmName = builder.fbVmName;
        this.sortChildren(COMPARATOR);
    }

    /**
     * Builder class that allows to build an entry by setting the parameters
     * independently, instead of using directly the constructors with many
     * parameters.
     *
     * @author Geneviève Bastien
     */
    public static class VmEntryBuilder {

        private final long fbStartTime;
        private final long fbEndTime;
        private final VirtualMachineExperiment fbExperiment;

        private String fbEntryName;
        private String fbId;
        private Type fbType;
        private @Nullable String fbVmName;
        private @Nullable ITmfTrace fbTrace;
        private @Nullable Integer fbNid;

        /**
         * Virtual Machine Entry builder constructor.
         *
         * @param name
         *            The name of this entry. It is also the default ID of this
         *            entry. So the ID does not need to be set if it is the same
         *            as the name.
         * @param startTime
         *            The start time of the entry
         * @param endTime
         *            The end time of the entry
         * @param experiment
         *            The experiment this entry applies to
         */
        public VmEntryBuilder(String name, long startTime, long endTime, VirtualMachineExperiment experiment) {
            fbEntryName = name;
            fbStartTime = startTime;
            fbEndTime = endTime;
            fbExperiment = experiment;
            fbId = name;
            fbType = Type.NULL;
        }

        /**
         * Sets the ID of this entry
         *
         * @param id
         *            The ID of the virtual machine entry
         * @return The builder with updated fields
         */
        public VmEntryBuilder setId(String id) {
            fbId = id;
            return this;
        }

        /**
         * Sets the virtual machine name of this entry
         *
         * @param vmName
         *            The virtual machine name of the virtual machine entry
         * @return The builder with updated fields
         */
        public VmEntryBuilder setVmName(String vmName) {
            fbVmName = vmName;
            return this;
        }

        /**
         * Sets the trace this entry applies to
         *
         * @param trace
         *            The trace this entry is for
         * @return The builder with updated fields
         */
        public VmEntryBuilder setTrace(ITmfTrace trace) {
            fbTrace = trace;
            return this;
        }

        /**
         * Sets the type of this entry
         *
         * @param type
         *            The type of the virtual machine entry
         * @return The builder with updated fields
         */
        public VmEntryBuilder setType(Type type) {
            fbType = type;
            return this;
        }

        /**
         * Sets the numeric ID of this entry. For VM or VCPU types, it is the
         * quark of the object represented by this entry, for THREAD types, it
         * is the thread ID of the corresponding thread.
         *
         * @param nid
         *            The numeric ID of the virtual machine entry
         * @return The builder with updated fields
         */
        public VmEntryBuilder setNumericId(Integer nid) {
            fbNid = nid;
            return this;
        }

        /**
         * Creates a new instance of {@link VirtualMachineViewEntry} with the
         * fields corresponding to those set in the builder.
         *
         * @return A new {@link VirtualMachineViewEntry} object
         */
        public VirtualMachineViewEntry build() {
            switch (fbType) {
            case VCPU:
                fbEntryName = Messages.VmView_VCpu + ' ' + fbEntryName;
                break;
            case VM:
                fbEntryName = NonNullUtils.nullToEmptyString(NLS.bind(Messages.VmView_virtualMachine, fbEntryName));
                break;
            case NULL:
            case THREAD:
            default:
                break;

            }
            return new VirtualMachineViewEntry(this);
        }

    }

    /**
     * Get the entry's id
     *
     * @return the entry's id
     */
    public String getId() {
        return fId;
    }

    /**
     * Get the name of the virtual machine this entry belongs to
     *
     * @return The name of the virtual machine
     */
    public @Nullable String getVmName() {
        return fVmName;
    }

    /**
     * Get the entry's kernel trace
     *
     * @return the entry's kernel trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Get the entry's kernel trace
     *
     * @return the entry's kernel trace
     */
    public VirtualMachineExperiment getExperiment() {
        return fExperiment;
    }

    /**
     * Get the entry Type of this entry. Uses the virtual machine enum
     * {@link Type}
     *
     * @return The entry type
     */
    public Type getType() {
        return fType;
    }

    /**
     * Retrieve the numeric ID that represents this entry.
     *
     * @return The numeric ID matching the entry
     */
    public Integer getNumericId() {
        return fNid;
    }

    @Override
    public boolean hasTimeEvents() {
        return (fType != Type.NULL);
    }

    /**
     * Get the state intervals for a given thread ID
     *
     * @param threadId
     *            The thread ID for which to get the intervals
     * @return A collection of intervals for this thread, or {@code null} if no
     *         intervals are available for this thread
     */
    public @Nullable Collection<ITmfStateInterval> getThreadIntervals(Integer threadId) {
        final Multimap<Integer, ITmfStateInterval> threadIntervals = fThreadIntervals;
        if (threadIntervals == null) {
            return null;
        }
        return threadIntervals.get(threadId);

    }

    /**
     * Set the intervals for the threads of the corresponding virtual machine.
     * This should be called only if the type of this entry is
     * {@link Type#VM}.
     *
     * @param threadIntervals
     *            The map of intervals for each thread ID
     */
    public void setThreadIntervals(Multimap<Integer, ITmfStateInterval> threadIntervals) {
        fThreadIntervals = threadIntervals;
    }

    /**
     * Get the default implementation of virtual machine entry comparator
     *
     * @return A virtual machine entry comparator
     */
    public static Comparator<ITimeGraphEntry> getComparator() {
        return COMPARATOR;
    }
}
