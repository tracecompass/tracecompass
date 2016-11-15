/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.registry;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;

/**
 * A Linux style
 *
 * @author Matthew Khouzam
 */
public enum LinuxStyle {

    /**
     * Unknown state for thread or CPU
     */
    UNKNOWN(Messages.LinuxStyles_unknown, new RGB(100, 100, 100), 0.33f),

    /**
     * Wait for an unknown reason
     */
    WAIT_UNKNOWN(Messages.LinuxStyles_wait, new RGB(200, 200, 200), 0.50f),

    /**
     * Wait to be scheduled back in
     */
    WAIT_BLOCKED(Messages.LinuxStyles_waitBlocked, new RGB(200, 200, 0), 0.50f),

    /**
     * Wait for the CPU to be available
     */
    WAIT_FOR_CPU(Messages.LinuxStyles_waitForCPU, new RGB(200, 100, 0), 0.50f),

    /**
     * CPU is idle
     */
    IDLE(Messages.LinuxStyles_idle, new RGB(200, 200, 200), 0.66f),

    /**
     * CPU or thread is in usermode
     */
    USERMODE(Messages.LinuxStyles_usermode, new RGB(0, 200, 0), 1.00f),

    /**
     * CPU or thread is in a system call
     */
    SYSCALL(Messages.LinuxStyles_systemCall, new RGB(0, 0, 200), 1.00f),

    /**
     * CPU is in an IRQ
     */
    INTERRUPTED(Messages.LinuxStyles_Interrupt, new RGB(200, 0, 100), 0.75f),

    /**
     * A Softirq or tasklet is raised
     */
    SOFT_IRQ_RAISED(Messages.LinuxStyles_softIrqRaised, new RGB(200, 200, 0), 1.00f),

    /**
     * CPU is in a softirq or tasklet
     */
    SOFT_IRQ(Messages.LinuxStyles_softrq, new RGB(200, 150, 100), 1.00f);

    private final float fHeightFactor;
    private RGBA fColor;
    private String fLabel;

    /**
     * A Linux style
     *
     * @param label
     *            The label of the style
     * @param color
     *            the color
     * @param heightFactor
     *            the hint of the height
     */
    private LinuxStyle(String label, RGB color, float heightFactor) {
        this(label, new RGBA(color.red, color.green, color.blue, 255), heightFactor);
    }

    /**
     * A Linux style
     *
     * @param label
     *            the label of the style
     * @param color
     *            the color
     * @param heightFactor
     *            the hint of the height, between 0 and 1.0
     */
    private LinuxStyle(String label, RGBA color, float heightFactor) {
        if (heightFactor > 1.0 || heightFactor < 0) {
            throw new IllegalStateException("Height factor needs to be between 0 and 1.0, given hint : " + heightFactor); //$NON-NLS-1$
        }
        fLabel = label;
        fColor = color;
        fHeightFactor = heightFactor;
    }

    /**
     * Get the label
     *
     * @return the label to display
     */
    public String getLabel() {
        return fLabel;
    }

    /**
     * Get the color
     *
     * @return the color to display
     */
    public RGBA getColor() {
        return fColor;
    }

    /**
     * Get the hint height, to be multiplied by the
     *
     * @return the heightHint
     */
    public float getHeightFactor() {
        return fHeightFactor;
    }
}
