/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.NonNull;

/**
 * SoftIRQ definitions are hard-coded to the following
 *
 * From linux/interrupt.h
 *
 * <pre>
 * enum
 * {
 *     HI_SOFTIRQ=0,
 *     TIMER_SOFTIRQ,
 *     NET_TX_SOFTIRQ,
 *     NET_RX_SOFTIRQ,
 *     BLOCK_SOFTIRQ,
 *     BLOCK_IOPOLL_SOFTIRQ,
 *     TASKLET_SOFTIRQ,
 *     SCHED_SOFTIRQ,
 *     HRTIMER_SOFTIRQ,
 *     RCU_SOFTIRQ,
 *     NR_SOFTIRQS // not used as this is the NUMBER of softirqs
 * };
 * </pre>
 *
 * @author Matthew Khouzam
 */
public final class SoftIrqLabelProvider {

    private SoftIrqLabelProvider() {
        // do nothing
    }

    /**
     * Gets a human readable name for a softirq
     *
     * @param irqNumber
     *            the number of the softirq
     * @return a human readable string, cannot be null, may be empty
     */
    public static @NonNull String getSoftIrq(int irqNumber) {
        switch (irqNumber) {
        case 0:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_softIrq0);
        case 1:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_softIrq1);
        case 2:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_softIrq2);
        case 3:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_softIrq3);
        case 4:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_softIrq4);
        case 5:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_softIrq5);
        case 6:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_softIrq6);
        case 7:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_softIrq7);
        case 8:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_softIrq8);
        case 9:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_softIrq9);
        default:
            return nullToEmptyString(Messages.SoftIrqLabelProvider_Unknown);
        }
    }
}
