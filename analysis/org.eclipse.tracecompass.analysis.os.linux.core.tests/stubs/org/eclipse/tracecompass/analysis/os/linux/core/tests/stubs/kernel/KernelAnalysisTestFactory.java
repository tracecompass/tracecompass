/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.kernel;

import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.LinuxTestCase;

/**
 * Factory of tests for the kernel analysis module
 *
 * @author Geneviève Bastien
 */
public final class KernelAnalysisTestFactory {

    private final static String TRACE_FILE_PATH = "testfiles/kernel_analysis/";

    private KernelAnalysisTestFactory() {

    }

    /**
     * This test case contains simple kernel trace events
     */
    public final static LinuxTestCase KERNEL_SCHED = new LinuxTestCase(TRACE_FILE_PATH + "lttng_kernel_analysis.xml") {

    };
}
