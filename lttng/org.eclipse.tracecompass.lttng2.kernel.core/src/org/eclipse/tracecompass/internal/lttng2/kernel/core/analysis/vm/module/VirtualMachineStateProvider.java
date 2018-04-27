/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.module;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.Activator;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VcpuStateValues;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VmAttributes;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.IVirtualMachineModel;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.VirtualCPU;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.VirtualMachine;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.qemukvm.QemuKvmVmModel;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.LttngEventLayout;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperimentUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This is the state provider which translates the virtual machine experiment
 * events to a state system.
 *
 * Attribute tree:
 *
 * <pre>
 * |- Virtual Machines
 * |  |- <Guest Host ID> -> Friendly name (trace name)
 * |  |  |- <VCPU number>
 * |  |  |  |- Status -> <Status value>
 * </pre>
 *
 * The status value of the VCPUs are either {@link VcpuStateValues#VCPU_IDLE},
 * {@link VcpuStateValues#VCPU_UNKNOWN} or {@link VcpuStateValues#VCPU_RUNNING}.
 * Those three values are ORed with flags {@link VcpuStateValues#VCPU_VMM}
 * and/or {@link VcpuStateValues#VCPU_PREEMPT} to indicate respectively whether
 * they are in hypervisor mode or preempted on the host.
 *
 * @author Mohamad Gebai
 */
public class VirtualMachineStateProvider extends AbstractTmfStateProvider {

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 1;

    private static final int SCHED_SWITCH_INDEX = 0;

    /* TODO: An analysis should support many hypervisor models */
    private IVirtualMachineModel fModel;
    private final Table<ITmfTrace, String, @Nullable Integer> fEventNames;
    private final Map<ITmfTrace, IKernelAnalysisEventLayout> fLayouts;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param experiment
     *            The virtual machine experiment
     */
    public VirtualMachineStateProvider(TmfExperiment experiment) {
        super(experiment, "Virtual Machine State Provider"); //$NON-NLS-1$

        fModel = new QemuKvmVmModel(experiment);
        Table<ITmfTrace, String, @Nullable Integer> table = HashBasedTable.create();
        fEventNames = table;
        fLayouts = new HashMap<>();
    }

    // ------------------------------------------------------------------------
    // Event names management
    // ------------------------------------------------------------------------

    private void buildEventNames(ITmfTrace trace) {
        IKernelAnalysisEventLayout layout;
        if (trace instanceof LttngKernelTrace) {
            layout = ((LttngKernelTrace) trace).getKernelEventLayout();
        } else {
            /* Fall-back to the base LttngEventLayout */
            layout = LttngEventLayout.getInstance();
        }
        fLayouts.put(trace, layout);
        fEventNames.put(trace, layout.eventSchedSwitch(), SCHED_SWITCH_INDEX);
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public TmfExperiment getTrace() {
        ITmfTrace trace = super.getTrace();
        if (trace instanceof TmfExperiment) {
            return (TmfExperiment) trace;
        }
        throw new IllegalStateException("VirtualMachineStateProvider: The associated trace should be an experiment"); //$NON-NLS-1$
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public VirtualMachineStateProvider getNewInstance() {
        TmfExperiment trace = getTrace();
        return new VirtualMachineStateProvider(trace);
    }

    @Override
    protected void eventHandle(@Nullable ITmfEvent event) {
        if (event == null) {
            return;
        }

        /* Is the event managed by this analysis */
        final String eventName = event.getName();
        IKernelAnalysisEventLayout eventLayout = fLayouts.get(event.getTrace());
        if (eventLayout == null) {
            buildEventNames(event.getTrace());
            eventLayout = fLayouts.get(event.getTrace());
            if (eventLayout == null) {
                return;
            }
        }

        if (!eventName.equals(eventLayout.eventSchedSwitch()) &&
                !fModel.getRequiredEvents(eventLayout).contains(eventName)) {
            return;
        }

        ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());
        ITmfStateValue value;

        final ITmfEventField content = event.getContent();
        final long ts = event.getTimestamp().getValue();
        final String hostId = event.getTrace().getHostId();
        try {
            /* Do we know this trace's role yet? */
            VirtualMachine host = fModel.getCurrentMachine(event);
            if (host == null) {
                return;
            }

            /* Make sure guest traces are added to the state system */
            if (host.isGuest()) {
                /*
                 * If event from a guest OS, make sure the guest exists in the
                 * state system
                 */
                int vmQuark = -1;
                try {
                    vmQuark = ss.getQuarkRelative(getNodeVirtualMachines(), host.getHostId());
                } catch (AttributeNotFoundException e) {
                    /*
                     * We should enter this catch only once per machine, so it
                     * is not so costly to do compared with adding the trace's
                     * name for each guest event
                     */
                    vmQuark = ss.getQuarkRelativeAndAdd(getNodeVirtualMachines(), host.getHostId());
                    String machineName = event.getTrace().getName();
                    ss.modifyAttribute(ts, machineName, vmQuark);
                }
            }

            /* Have the hypervisor models handle the event first */
            fModel.handleEvent(event);

            /* Handle the event here */
            Integer idx = fEventNames.get(event.getTrace(), eventName);
            int intval = (idx == null ? -1 : idx.intValue());
            switch (intval) {
            case SCHED_SWITCH_INDEX: // "sched_switch":
            /*
             * Fields: string prev_comm, int32 prev_tid, int32 prev_prio, int64
             * prev_state, string next_comm, int32 next_tid, int32 next_prio
             */
            {
                int prevTid = ((Long) content.getField(eventLayout.fieldPrevTid()).getValue()).intValue();
                int nextTid = ((Long) content.getField(eventLayout.fieldNextTid()).getValue()).intValue();

                if (host.isGuest()) {
                    /* Get the event's CPU */
                    Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
                    if (cpu == null) {
                        /*
                         * We couldn't find any CPU information, ignore this
                         * event
                         */
                        break;
                    }

                    /*
                     * If sched switch is from a guest, just update the status
                     * of the virtual CPU to either idle or running
                     */
                    int curStatusQuark = ss.getQuarkRelativeAndAdd(getNodeVirtualMachines(), host.getHostId(),
                            cpu.toString(), VmAttributes.STATUS);
                    value = TmfStateValue.newValueInt(VcpuStateValues.VCPU_IDLE);
                    if (nextTid > 0) {
                        value = TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING);
                    }
                    ss.modifyAttribute(ts, value.unboxValue(), curStatusQuark);
                    break;
                }

                /* Event is not from a guest */
                /* Verify if the previous thread corresponds to a virtual CPU */
                HostThread ht = new HostThread(hostId, prevTid);
                VirtualCPU vcpu = fModel.getVirtualCpu(ht);

                /*
                 * If previous thread is virtual CPU, update status of the
                 * virtual CPU to preempted
                 */
                if (vcpu != null) {
                    VirtualMachine vm = vcpu.getVm();

                    int curStatusQuark = ss.getQuarkRelativeAndAdd(getNodeVirtualMachines(), vm.getHostId(),
                            vcpu.getCpuId().toString(), VmAttributes.STATUS);

                    /* Add the preempted flag to the status */
                    value = ss.queryOngoingState(curStatusQuark);
                    if ((value.unboxInt() & VcpuStateValues.VCPU_IDLE) == 0) {
                        int newVal = Math.max(VcpuStateValues.VCPU_UNKNOWN, value.unboxInt());
                        value = TmfStateValue.newValueInt(newVal | VcpuStateValues.VCPU_PREEMPT);
                        ss.modifyAttribute(ts, value.unboxValue(), curStatusQuark);
                    }
                }

                /* Verify if the next thread corresponds to a virtual CPU */
                ht = new HostThread(hostId, nextTid);
                vcpu = fModel.getVirtualCpu(ht);

                /*
                 * If next thread is virtual CPU, update status of the virtual
                 * CPU the previous status
                 */
                if (vcpu != null) {
                    VirtualMachine vm = vcpu.getVm();
                    int curStatusQuark = ss.getQuarkRelativeAndAdd(getNodeVirtualMachines(), vm.getHostId(),
                            vcpu.getCpuId().toString(), VmAttributes.STATUS);

                    /* Remove the preempted flag from the status */
                    value = ss.queryOngoingState(curStatusQuark);
                    int newVal = Math.max(VcpuStateValues.VCPU_UNKNOWN, value.unboxInt());
                    value = TmfStateValue.newValueInt(newVal & ~VcpuStateValues.VCPU_PREEMPT);
                    ss.modifyAttribute(ts, value.unboxValue(), curStatusQuark);

                }

            }
                break;

            default:
            /* Other events not covered by the main switch */
            {
                HostThread ht = getCurrentHostThread(event, ts);
                if (ht == null) {
                    break;
                }

                /*
                 * Are we entering the hypervisor mode and if so, which virtual
                 * CPU is concerned?
                 */
                VirtualCPU virtualCpu = fModel.getVCpuEnteringHypervisorMode(event, ht, eventLayout);
                if (virtualCpu != null) {
                    /* Add the hypervisor flag to the status */
                    VirtualMachine vm = virtualCpu.getVm();
                    int curStatusQuark = ss.getQuarkRelativeAndAdd(getNodeVirtualMachines(), vm.getHostId(),
                            Long.toString(virtualCpu.getCpuId()), VmAttributes.STATUS);
                    value = ss.queryOngoingState(curStatusQuark);
                    if ((value.unboxInt() & VcpuStateValues.VCPU_IDLE) == 0) {
                        int newVal = Math.max(VcpuStateValues.VCPU_UNKNOWN, value.unboxInt());
                        value = TmfStateValue.newValueInt(newVal | VcpuStateValues.VCPU_VMM);
                        ss.modifyAttribute(ts, value.unboxValue(), curStatusQuark);
                    }
                }

                /*
                 * Are we exiting the hypervisor mode and if so, which virtual
                 * CPU is concerned?
                 */
                virtualCpu = fModel.getVCpuExitingHypervisorMode(event, ht, eventLayout);
                if (virtualCpu != null) {
                    /* Remove the hypervisor flag from the status */
                    VirtualMachine vm = virtualCpu.getVm();
                    int curStatusQuark = ss.getQuarkRelativeAndAdd(getNodeVirtualMachines(), vm.getHostId(),
                            Long.toString(virtualCpu.getCpuId()), VmAttributes.STATUS);
                    value = ss.queryOngoingState(curStatusQuark);
                    int newVal = Math.max(VcpuStateValues.VCPU_UNKNOWN, value.unboxInt());
                    value = TmfStateValue.newValueInt(newVal & ~VcpuStateValues.VCPU_VMM);
                    ss.modifyAttribute(ts, value.unboxValue(), curStatusQuark);
                }

            }
                break;
            }

        } catch (TimeRangeException | StateValueTypeException e) {
            Activator.getDefault().logError("Error handling event in VirtualMachineStateProvider", e); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Convenience methods for commonly-used attribute tree locations
    // ------------------------------------------------------------------------

    private int getNodeVirtualMachines() {
        return checkNotNull(getStateSystemBuilder()).getQuarkAbsoluteAndAdd(VmAttributes.VIRTUAL_MACHINES);
    }

    private @Nullable HostThread getCurrentHostThread(ITmfEvent event, long ts) {
        /* Get the LTTng kernel analysis for the host */
        String hostId = event.getTrace().getHostId();
        KernelAnalysisModule module = TmfExperimentUtils.getAnalysisModuleOfClassForHost(getTrace(), hostId, KernelAnalysisModule.class);
        if (module == null) {
            return null;
        }

        /* Get the CPU the event is running on */
        Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
        if (cpu == null) {
            /* We couldn't find any CPU information, ignore this event */
            return null;
        }

        Integer currentTid = KernelThreadInformationProvider.getThreadOnCpu(module, cpu, ts);
        if (currentTid == null) {
            return null;
        }
        return new HostThread(hostId, currentTid);
    }

}
