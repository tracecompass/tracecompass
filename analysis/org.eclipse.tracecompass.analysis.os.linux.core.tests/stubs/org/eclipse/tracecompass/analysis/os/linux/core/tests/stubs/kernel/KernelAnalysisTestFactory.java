/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
