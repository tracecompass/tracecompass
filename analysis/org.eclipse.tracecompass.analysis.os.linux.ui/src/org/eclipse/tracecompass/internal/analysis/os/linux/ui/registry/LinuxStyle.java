/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.registry;

import java.util.Map;

import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;

import com.google.common.collect.ImmutableMap;

/**
 * A Linux style
 *
 * @author Matthew Khouzam
 */
public enum LinuxStyle {

    /**
     * Unknown state for thread or CPU
     */
    UNKNOWN(Messages.LinuxStyles_unknown, 100, 100, 100, 255, 0.33f),

    /**
     * Link style, the general line style
     */
    LINK(Messages.LinuxStyles_unknown, 100, 100, 100, 255, 0.10f),

    /**
     * Wait for an unknown reason
     */
    WAIT_UNKNOWN(Messages.LinuxStyles_wait, 200, 200, 200, 255, 0.50f),

    /**
     * Wait to be scheduled back in
     */
    WAIT_BLOCKED(Messages.LinuxStyles_waitBlocked, 200, 200, 0, 255, 0.50f),

    /**
     * Wait for the CPU to be available
     */
    WAIT_FOR_CPU(Messages.LinuxStyles_waitForCPU, 200, 100, 0, 255, 0.50f),

    /**
     * CPU is idle
     */
    IDLE(Messages.LinuxStyles_idle, 200, 200, 200, 255, 0.66f),

    /**
     * CPU or thread is in usermode
     */
    USERMODE(Messages.LinuxStyles_usermode, 0, 200, 0, 255, 1.00f),

    /**
     * CPU or thread is in a system call
     */
    SYSCALL(Messages.LinuxStyles_systemCall, 0, 0, 200, 255, 1.00f),

    /**
     * CPU is in an IRQ
     */
    INTERRUPTED(Messages.LinuxStyles_Interrupt, 200, 0, 100, 255, 0.75f),

    /**
     * A Softirq or tasklet is raised
     */
    SOFT_IRQ_RAISED(Messages.LinuxStyles_softIrqRaised, 200, 200, 0, 255, 1.00f),

    /**
     * CPU is in a softirq or tasklet
     */
    SOFT_IRQ(Messages.LinuxStyles_softrq, 200, 150, 100, 255, 1.00f);

    private final Map<String, Object> fMap;

    /**
     * A Linux style
     *
     * @param label
     *            the label of the style
     * @param red
     *            red value, must be between 0 and 255
     * @param green
     *            green value, must be between 0 and 255
     * @param blue
     *            blue value, must be between 0 and 255
     * @param alpha
     *            value, must be between 0 and 255
     * @param heightFactor
     *            the hint of the height, between 0 and 1.0
     */
    private LinuxStyle(String label, int red, int green, int blue, int alpha, float heightFactor) {
        if (label == null) {
            throw new IllegalArgumentException("Label cannot be null"); //$NON-NLS-1$
        }
        if (red > 255 || red < 0) {
            throw new IllegalArgumentException("Red needs to be between 0 and 255"); //$NON-NLS-1$
        }
        if (green > 255 || green < 0) {
            throw new IllegalArgumentException("Green needs to be between 0 and 255"); //$NON-NLS-1$
        }
        if (blue > 255 || blue < 0) {
            throw new IllegalArgumentException("Blue needs to be between 0 and 255"); //$NON-NLS-1$
        }
        if (alpha > 255 || alpha < 0) {
            throw new IllegalArgumentException("alpha needs to be between 0 and 255"); //$NON-NLS-1$
        }
        if (heightFactor > 1.0 || heightFactor < 0) {
            throw new IllegalArgumentException("Height factor needs to be between 0 and 1.0, given hint : " + heightFactor); //$NON-NLS-1$
        }
        fMap = ImmutableMap.of(ITimeEventStyleStrings.label(), label,
                ITimeEventStyleStrings.fillStyle(), ITimeEventStyleStrings.solidColorFillStyle(),
                ITimeEventStyleStrings.fillColor(), new RGBAColor(red, green, blue, alpha).toInt(),
                ITimeEventStyleStrings.heightFactor(), heightFactor);
    }

    /**
     * Get the label
     *
     * @return the label to display
     */
    public String getLabel() {
        return (String) toMap().get(ITimeEventStyleStrings.label());
    }

    /**
     * Get a map of the values corresponding to the fields in
     * {@link ITimeEventStyleStrings}
     *
     * @return the map corresponding to the api defined in
     *         {@link ITimeEventStyleStrings}
     */
    public Map<String, Object> toMap() {
        return fMap;
    }
}
