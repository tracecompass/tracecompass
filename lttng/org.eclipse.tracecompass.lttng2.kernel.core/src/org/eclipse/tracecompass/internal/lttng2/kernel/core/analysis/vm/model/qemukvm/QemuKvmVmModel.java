/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.qemukvm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelThreadInformationProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.IVirtualMachineModel;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.VirtualCPU;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.VirtualMachine;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperimentUtils;

import com.google.common.collect.ImmutableSet;

/**
 * The virtual machine model corresponding to the Qemu/KVM hypervisor. It uses
 * the kvm_exit/kvm_entry events to identify entry to and exit from the
 * hypervisor. It also requires vmsync_* events from both guests and hosts to
 * identify which thread from a host belongs to which machine.
 *
 * @author Mohamad Gebai
 */
public class QemuKvmVmModel implements IVirtualMachineModel {

    private static final String KVM = "kvm_"; //$NON-NLS-1$

    /* Associate a host's thread to a virtual CPU */
    private final Map<HostThread, VirtualCPU> fTidToVcpu = new HashMap<>();
    /* Associate a host's thread to a virtual machine */
    private final Map<HostThread, VirtualMachine> fTidToVm = new HashMap<>();
    /* Maps a virtual machine name to a virtual machine */
    private final Map<String, VirtualMachine> fKnownMachines = new HashMap<>();

    private final TmfExperiment fExperiment;

    static final ImmutableSet<String> REQUIRED_EVENTS = ImmutableSet.of(
            QemuKvmStrings.KVM_ENTRY,
            QemuKvmStrings.KVM_EXIT,
            QemuKvmStrings.VMSYNC_GH_GUEST,
            QemuKvmStrings.VMSYNC_GH_HOST,
            QemuKvmStrings.VMSYNC_HG_GUEST,
            QemuKvmStrings.VMSYNC_HG_HOST
            );

    /**
     * Constructor
     *
     * @param exp
     *            The experiment this model applies to
     */
    public QemuKvmVmModel(TmfExperiment exp) {
        fExperiment = exp;
    }

    @Override
    public @Nullable VirtualMachine getCurrentMachine(ITmfEvent event) {
        final String hostId = event.getTrace().getHostId();
        VirtualMachine machine = fKnownMachines.get(hostId);
        if (machine != null) {
            return machine;
        }

        /*
         * TODO: This model wouldn't support a machine (same hostId) being both
         * a guest and a host
         */

        /*
         * This code path would be used only at the beginning of the analysis.
         * Once all the hostIds have been associated with a virtual machine, the
         * machines are all cached and the method returns earlier
         */
        /* Try to get the virtual machine from the event */
        String eventName = event.getName();
        if (eventName.startsWith(KVM)) {
            /* Only the host machine has kvm_* events, so this is a host */
            machine = VirtualMachine.newHostMachine(hostId);
        } else if (eventName.equals(QemuKvmStrings.VMSYNC_GH_GUEST) || eventName.equals(QemuKvmStrings.VMSYNC_HG_GUEST)) {
            /* Those events are only present in the guests */
            TmfEventField field = (TmfEventField) event.getContent();
            ITmfEventField data = field.getField(QemuKvmStrings.VM_UID_PAYLOAD);
            if (data != null) {
                machine = VirtualMachine.newGuestMachine((Long) data.getValue(), hostId);
            }
        }
        if (machine != null) {
            /* Associate the machine to the hostID here, for cached access later */
            fKnownMachines.put(hostId, machine);
        }
        return machine;
    }

    @Override
    public Set<String> getRequiredEvents() {
        return REQUIRED_EVENTS;
    }

    private @Nullable VirtualMachine findVmFromParent(ITmfEvent event, HostThread ht) {
        /*
         * Maybe the parent of the current thread has a VM associated, see if we
         * can infer the VM for this thread
         */
        KernelAnalysisModule module = getLttngKernelModuleFor(ht.getHost());
        if (module == null) {
            return null;
        }

        Integer ppid = KernelThreadInformationProvider.getParentPid(module, ht.getTid(), event.getTimestamp().getValue());
        if (ppid == null) {
            return null;
        }

        HostThread parentHt = new HostThread(ht.getHost(), ppid);
        VirtualMachine vm = fTidToVm.get(parentHt);
        if (vm == null) {
            return null;
        }
        fTidToVm.put(ht, vm);

        return vm;
    }

    @Override
    public @Nullable VirtualCPU getVCpuExitingHypervisorMode(ITmfEvent event, HostThread ht) {
        final String eventName = event.getName();

        /* TODO: Use event layouts for this part also */
        /*
         * The KVM_ENTRY event means we are entering a virtual CPU, so exiting
         * hypervisor mode
         */
        if (!eventName.equals(QemuKvmStrings.KVM_ENTRY)) {
            return null;
        }

        /*
         * Are we entering the hypervisor and if so, which virtual CPU is
         * concerned?
         */
        VirtualMachine vm = fTidToVm.get(ht);
        if (vm == null) {
            vm = findVmFromParent(event, ht);
            if (vm == null) {
                return null;
            }
        }
        /* Associate this thread with the virtual CPU that is going to be run */
        final ITmfEventField content = event.getContent();
        long vcpu_id = (Long) content.getField(QemuKvmStrings.VCPU_ID).getValue();

        VirtualCPU virtualCPU = VirtualCPU.getVirtualCPU(vm, vcpu_id);
        fTidToVcpu.put(ht, virtualCPU);

        return virtualCPU;
    }

    @Override
    public @Nullable VirtualCPU getVCpuEnteringHypervisorMode(ITmfEvent event, HostThread ht) {
        final String eventName = event.getName();
        /*
         * The KVM_EXIT event means we are exiting a virtual CPU, so entering
         * hypervisor mode
         */
        if (!eventName.equals(QemuKvmStrings.KVM_EXIT)) {
            return null;
        }

        return getVirtualCpu(ht);
    }

    @Override
    public @Nullable VirtualCPU getVirtualCpu(HostThread ht) {
        return fTidToVcpu.get(ht);
    }

    @Override
    public void handleEvent(ITmfEvent event) {
        /* Is the event handled by this model */
        final String eventName = event.getName();
        if (!eventName.equals(QemuKvmStrings.VMSYNC_GH_HOST)) {
            return;
        }

        final ITmfEventField content = event.getContent();
        final long ts = event.getTimestamp().getValue();
        final String hostId = event.getTrace().getHostId();

        final Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
        if (cpu == null) {
            /* We couldn't find any CPU information, ignore this event */
            return;
        }

        /* Find a virtual machine with the vm uid payload value */
        ITmfEventField data = content.getField(QemuKvmStrings.VM_UID_PAYLOAD);
        if (data == null) {
            return;
        }
        long vmUid = (Long) data.getValue();
        for (Entry<String, VirtualMachine> entry : fKnownMachines.entrySet()) {
            if (entry.getValue().getVmUid() == vmUid) {
                /*
                 * We found the VM being run, let's associate it with the thread
                 * ID
                 */
                KernelAnalysisModule module = getLttngKernelModuleFor(hostId);
                if (module == null) {
                    break;
                }
                Integer tid = KernelThreadInformationProvider.getThreadOnCpu(module, cpu, ts);
                if (tid == null) {
                    /*
                     * We do not know which process is running at this point. It
                     * may happen at the beginning of the trace.
                     */
                    break;
                }
                HostThread ht = new HostThread(hostId, tid);
                fTidToVm.put(ht, entry.getValue());

                /*
                 * To make sure siblings are also associated with this VM, also
                 * add an entry for the parent TID
                 */
                Integer ppid = KernelThreadInformationProvider.getParentPid(module, tid, ts);
                if (ppid != null) {
                    HostThread parentHt = new HostThread(hostId, ppid);
                    fTidToVm.put(parentHt, entry.getValue());
                }
            }
        }
    }

    private @Nullable KernelAnalysisModule getLttngKernelModuleFor(String hostId) {
        return TmfExperimentUtils.getAnalysisModuleOfClassForHost(fExperiment, hostId, KernelAnalysisModule.class);
    }

}
