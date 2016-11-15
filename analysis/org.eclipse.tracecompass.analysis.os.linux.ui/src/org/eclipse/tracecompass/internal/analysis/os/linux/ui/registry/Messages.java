/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.registry;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 *
 * @author Matthew Khouzam
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.registry.messages"; //$NON-NLS-1$
    /**
     * Idle
     */
    public static String LinuxStyles_idle;
    /**
     * Irq
     */
    public static String LinuxStyles_Interrupt;
    /**
     * Softirq raised
     */
    public static String LinuxStyles_softIrqRaised;
    /**
     * Softirq
     */
    public static String LinuxStyles_softrq;
    /**
     * Syscall
     */
    public static String LinuxStyles_systemCall;
    /**
     * Unknown
     */
    public static String LinuxStyles_unknown;
    /**
     * User mode
     */
    public static String LinuxStyles_usermode;
    /**
     * Wait
     */
    public static String LinuxStyles_wait;
    /**
     * Wait blocked
     */
    public static String LinuxStyles_waitBlocked;
    /**
     * Wait for CPU
     */
    public static String LinuxStyles_waitForCPU;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
