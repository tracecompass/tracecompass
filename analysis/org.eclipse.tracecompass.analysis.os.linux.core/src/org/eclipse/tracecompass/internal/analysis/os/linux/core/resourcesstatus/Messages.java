/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus;

import org.eclipse.osgi.util.NLS;

/**
 * Softirq names. Not the C style ones, but descriptive ones
 *
 * @author Matthew Khouzam
 */
class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.messages"; //$NON-NLS-1$

    /** softirq 0 */
    public static String SoftIrqLabelProvider_softIrq0;
    /** softirq 1 */
    public static String SoftIrqLabelProvider_softIrq1;
    /** softirq 2 */
    public static String SoftIrqLabelProvider_softIrq2;
    /** softirq 3 */
    public static String SoftIrqLabelProvider_softIrq3;
    /** softirq 4 */
    public static String SoftIrqLabelProvider_softIrq4;
    /** softirq 5 */
    public static String SoftIrqLabelProvider_softIrq5;
    /** softirq 6 */
    public static String SoftIrqLabelProvider_softIrq6;
    /** softirq 7 */
    public static String SoftIrqLabelProvider_softIrq7;
    /** softirq 8 */
    public static String SoftIrqLabelProvider_softIrq8;
    /** softirq 9 */
    public static String SoftIrqLabelProvider_softIrq9;
    /** other softirq */
    public static String SoftIrqLabelProvider_Unknown;
    /** thread entry */
    public static String ThreadEntry;
    /** cpu entry */
    public static String CpuEntry;
    /** Frequency entry */
    public static String FrequencyEntry;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
