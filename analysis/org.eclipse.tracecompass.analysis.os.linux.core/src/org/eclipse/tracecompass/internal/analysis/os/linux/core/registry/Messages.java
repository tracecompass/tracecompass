/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.registry;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Messages
 *
 * @author Matthew Khouzam
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.core.registry.messages"; //$NON-NLS-1$
    /**
     * Idle
     */
    public static @Nullable String LinuxStyles_idle = null;
    /**
     * Irq
     */
    public static @Nullable String LinuxStyles_Interrupt = null;
    /**
     * Link label
     */
    public static @Nullable String LinuxStyles_link = null;
    /**
     * Softirq raised
     */
    public static @Nullable String LinuxStyles_softIrqRaised = null;
    /**
     * Softirq
     */
    public static @Nullable String LinuxStyles_softrq = null;
    /**
     * Syscall
     */
    public static @Nullable String LinuxStyles_systemCall = null;
    /**
     * Unknown
     */
    public static @Nullable String LinuxStyles_unknown = null;
    /**
     * User mode
     */
    public static @Nullable String LinuxStyles_usermode = null;
    /**
     * Wait
     */
    public static @Nullable String LinuxStyles_wait = null;
    /**
     * Wait blocked
     */
    public static @Nullable String LinuxStyles_waitBlocked = null;
    /**
     * Wait for CPU
     */
    public static @Nullable String LinuxStyles_waitForCPU = null;
    /**
     * Process style group
     */
    public static @Nullable String LinuxStyles_ProcessGroup = null;
    /**
     * Arrows style group
     */
    public static @Nullable String LinuxStyles_LinkGroup = null;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
