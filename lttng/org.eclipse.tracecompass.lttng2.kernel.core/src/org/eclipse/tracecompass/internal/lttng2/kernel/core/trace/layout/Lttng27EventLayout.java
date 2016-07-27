/*******************************************************************************
 * Copyright (c) 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sebastien Lorrain - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules 2.7 and above.
 *
 * @author Sebastien Lorrain
 */
@SuppressWarnings("javadoc")
public class Lttng27EventLayout extends Lttng26EventLayout {

    private static final String X86_IRQ_VECTORS_LOCAL_TIMER_ENTRY = "x86_irq_vectors_local_timer_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_LOCAL_TIMER_EXIT = "x86_irq_vectors_local_timer_exit";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_RESCHEDULE_ENTRY = "x86_irq_vectors_reschedule_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_RESCHEDULE_EXIT = "x86_irq_vectors_reschedule_exit";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_SPURIOUS_ENTRY = "x86_irq_vectors_spurious_apic_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_SPURIOUS_EXIT = "x86_irq_vectors_spurious_apic_exit";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_ERROR_APIC_ENTRY = "x86_irq_vectors_error_apic_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_ERROR_APIC_EXIT = "x86_irq_vectors_error_apic_exit";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_IPI_ENTRY = "x86_irq_vectors_ipi_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_IPI_EXIT = "x86_irq_vectors_ipi_exit";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_IRQ_WORK_ENTRY = "x86_irq_vectors_irq_work_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_IRQ_WORK_EXIT = "x86_irq_vectors_irq_work_exit";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_CALL_FUNCTION_ENTRY = "x86_irq_vectors_call_function_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_CALL_FUNCTION_EXIT = "x86_irq_vectors_call_function_exit"; //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_ENTRY = "x86_irq_vectors_call_function_single_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_EXIT = "x86_irq_vectors_call_function_single_exit";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_THRESHOLD_APIC_ENTRY = "x86_irq_vectors_threshold_apic_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_THRESHOLD_APIC_EXIT = "x86_irq_vectors_threshold_apic_exit";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_ENTRY = "x86_irq_vectors_deferred_error_apic_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_EXIT = "x86_irq_vectors_deferred_error_apic_exit";   //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_THERMAL_APIC_ENTRY = "x86_irq_vectors_thermal_apic_entry";  //$NON-NLS-1$
    private static final String X86_IRQ_VECTORS_THERMAL_APIC_EXIT = "x86_irq_vectors_thermal_apic_exit";  //$NON-NLS-1$

    private static final Collection<String> IPI_ENTRY_SET = ImmutableSet.of(
            X86_IRQ_VECTORS_LOCAL_TIMER_ENTRY,
            X86_IRQ_VECTORS_RESCHEDULE_ENTRY,
            X86_IRQ_VECTORS_SPURIOUS_ENTRY,
            X86_IRQ_VECTORS_ERROR_APIC_ENTRY,
            X86_IRQ_VECTORS_IPI_ENTRY,
            X86_IRQ_VECTORS_IRQ_WORK_ENTRY,
            X86_IRQ_VECTORS_CALL_FUNCTION_ENTRY,
            X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_ENTRY,
            X86_IRQ_VECTORS_THRESHOLD_APIC_ENTRY,
            X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_ENTRY,
            X86_IRQ_VECTORS_THERMAL_APIC_ENTRY);

    private static final Collection<String> IPI_EXIT_SET = ImmutableSet.of(
            X86_IRQ_VECTORS_LOCAL_TIMER_EXIT,
            X86_IRQ_VECTORS_RESCHEDULE_EXIT,
            X86_IRQ_VECTORS_SPURIOUS_EXIT,
            X86_IRQ_VECTORS_ERROR_APIC_EXIT,
            X86_IRQ_VECTORS_IPI_EXIT,
            X86_IRQ_VECTORS_IRQ_WORK_EXIT,
            X86_IRQ_VECTORS_CALL_FUNCTION_EXIT,
            X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_EXIT,
            X86_IRQ_VECTORS_THRESHOLD_APIC_EXIT,
            X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_EXIT,
            X86_IRQ_VECTORS_THERMAL_APIC_EXIT);

    /**
     * Constructor
     */
    protected Lttng27EventLayout() {}

    private static final Lttng27EventLayout INSTANCE = new Lttng27EventLayout();

    public static Lttng27EventLayout getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------
    // New event definition in LTTng 2.7
    // ------------------------------------------------------------------------

    @Override
    public String eventHRTimerStart() {
        return "timer_hrtimer_start"; //$NON-NLS-1$
    }

    @Override
    public String eventHRTimerCancel() {
        return "timer_hrtimer_cancel"; //$NON-NLS-1$
    }

    @Override
    public String eventHRTimerExpireEntry() {
        return "timer_hrtimer_expire_entry"; //$NON-NLS-1$
    }

    @Override
    public String eventHRTimerExpireExit() {
        return "timer_hrtimer_expire_exit"; //$NON-NLS-1$
    }

    @Override
    public String eventSoftIrqRaise() {
        return "irq_softirq_raise"; //$NON-NLS-1$
    }

    @Override
    public String eventSoftIrqEntry() {
        return "irq_softirq_entry"; //$NON-NLS-1$
    }

    @Override
    public String eventSoftIrqExit() {
        return "irq_softirq_exit"; //$NON-NLS-1$
    }

    @Override
    public String eventKmemPageAlloc() {
        return "kmem_mm_page_alloc"; //$NON-NLS-1$
    }

    @Override
    public String eventKmemPageFree() {
        return "kmem_mm_page_free"; //$NON-NLS-1$
    }

    public String x86IrqVectorsLocalTimerEntry() {
        return X86_IRQ_VECTORS_LOCAL_TIMER_ENTRY;
    }

    public String x86IrqVectorsLocalTimerExit() {
        return X86_IRQ_VECTORS_LOCAL_TIMER_EXIT;
    }

    public String x86IrqVectorsRescheduleEntry() {
        return X86_IRQ_VECTORS_RESCHEDULE_ENTRY;
    }

    public String x86IrqVectorsRescheduleExit() {
        return X86_IRQ_VECTORS_RESCHEDULE_EXIT;
    }

    public String x86IrqVectorsSpuriousApicEntry() {
        return X86_IRQ_VECTORS_SPURIOUS_ENTRY;
    }

    public String x86IrqVectorsSpuriousApicExit() {
        return X86_IRQ_VECTORS_SPURIOUS_EXIT;
    }

    public String x86IrqVectorsErrorApicEntry() {
        return X86_IRQ_VECTORS_ERROR_APIC_ENTRY;
    }

    public String x86IrqVectorsErrorApicExit() {
        return X86_IRQ_VECTORS_ERROR_APIC_EXIT;
    }

    public String x86IrqVectorsIpiEntry() {
        return X86_IRQ_VECTORS_IPI_ENTRY;
    }

    public String x86IrqVectorsIpiExit() {
        return X86_IRQ_VECTORS_IPI_EXIT;
    }

    public String x86IrqVectorsIrqWorkEntry() {
        return X86_IRQ_VECTORS_IRQ_WORK_ENTRY;
    }

    public String x86IrqVectorsIrqWorkExit() {
        return X86_IRQ_VECTORS_IRQ_WORK_EXIT;
    }

    public String x86IrqVectorsCallFunctionEntry() {
        return X86_IRQ_VECTORS_CALL_FUNCTION_ENTRY;
    }

    public String x86IrqVectorsCallFunctionExit() {
        return X86_IRQ_VECTORS_CALL_FUNCTION_EXIT;
    }

    public String x86IrqVectorsCallFunctionSingleEntry() {
        return X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_ENTRY;
    }

    public String x86IrqVectorsCallFunctionSingleExit() {
        return X86_IRQ_VECTORS_CALL_FUNCTION_SINGLE_EXIT;
    }

    public String x86IrqVectorsThresholdApicEntry() {
        return X86_IRQ_VECTORS_THRESHOLD_APIC_ENTRY;
    }

    public String x86IrqVectorsThresholdApicExit() {
        return X86_IRQ_VECTORS_THRESHOLD_APIC_EXIT;
    }

    public String x86IrqVectorsDeferredErrorApicEntry() {
        return X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_ENTRY;
    }

    public String x86IrqVectorsDeferredErrorApicExit() {
        return X86_IRQ_VECTORS_DEFERRED_ERROR_APIC_EXIT;
    }

    public String x86IrqVectorsThermalApicEntry() {
        return X86_IRQ_VECTORS_THERMAL_APIC_ENTRY;
    }

    public String x86IrqVectorsThermalApicExit() {
        return X86_IRQ_VECTORS_THERMAL_APIC_EXIT;
    }

    @Override
    public @NonNull Collection<@NonNull String> getIPIIrqVectorsEntries() {
        return IPI_ENTRY_SET;
    }

    @Override
    public @NonNull Collection<@NonNull String> getIPIIrqVectorsExits() {
        return IPI_EXIT_SET;
    }

    // ------------------------------------------------------------------------
    // New field definitions in LTTng 2.7
    // ------------------------------------------------------------------------

    public String fieldParentNSInum() {
        return "parent_ns_inum"; //$NON-NLS-1$
    }

    public String fieldChildNSInum() {
        return "child_ns_inum"; //$NON-NLS-1$
    }

    public String fieldChildVTids() {
        return "vtids"; //$NON-NLS-1$
    }

    public String fieldNSInum() {
        return "ns_inum"; //$NON-NLS-1$
    }

    public String fieldVTid() {
        return "vtid"; //$NON-NLS-1$
    }

    public String fieldPPid() {
        return "ppid"; //$NON-NLS-1$
    }

    public String fieldNSLevel() {
        return "ns_level"; //$NON-NLS-1$
    }

    @Override
    public @NonNull Collection<@NonNull String> eventNetworkReceive() {
        return ImmutableList.of("netif_receive_skb", "net_if_receive_skb"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
