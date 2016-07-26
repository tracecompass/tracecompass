/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.qemukvm;

/**
 * Lttng specific strings for the events used by the Qemu/KVM virtual machine
 * model
 *
 * TODO: The whole model should be updated to use the linux event layout. These
 * event names are LTTng-specific
 *
 * @author Mohamad Gebai
 */
@SuppressWarnings({ "nls" })
public interface QemuKvmStrings {

    /* vmsync events */

    /**
     * Event produced by the host, for a message sent from the guest, received
     * by the host
     */
    String VMSYNC_GH_HOST = "vmsync_gh_host";
    /**
     * Event produced by the host, for a message sent from the host, received by
     * the guest
     */
    String VMSYNC_HG_HOST = "vmsync_hg_host";
    /**
     * Event produced by the guest, for a message sent from the guest, received
     * by the host
     */
    String VMSYNC_GH_GUEST = "vmsync_gh_guest";
    /**
     * Event produced by the guest, for a message sent from the host, received
     * by the guest
     */
    String VMSYNC_HG_GUEST = "vmsync_hg_guest";
    /**
     * Event field of previous events, containing a message counter, updated at
     * each message
     */
    String COUNTER_PAYLOAD = "cnt";
    /**
     * Event field of previous events, with a unique UID to identify a single
     * guest on a host
     */
    String VM_UID_PAYLOAD = "vm_uid";
    /**
     * Field from kvm_entry event indicating which virtual CPU is being run
     */
    String VCPU_ID = "vcpu_id";

}
