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

package org.eclipse.tracecompass.alltests.swtbot;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite for all Trace Compass SWTBot performance tests.
 *
 * Note that the SWTBot tests need to be executed in a non-UI thread which is
 * why they are separated in a different test suite.
 *
 * Also, these tests typically test the performance of the UI and one might want
 * to enable JUL logging while running these.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.eclipse.tracecompass.lttng2.ust.ui.swtbot.tests.perf.LttngUstResponseBenchmark.class,
    org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests.perf.LttngUiResponseBenchmark.class
})
public class RunAllSWTBotPerfTests {

}
