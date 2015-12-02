/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests;

import org.eclipse.tracecompass.tmf.core.tests.shared.DebugSuite;
import org.junit.runner.RunWith;

/**
 * Runner for the unit tests of this plugin
 */
@RunWith(DebugSuite.class)
@DebugSuite.SuiteClasses({
    org.eclipse.tracecompass.analysis.os.linux.core.tests.cpuusage.AllTests.class,
    org.eclipse.tracecompass.analysis.os.linux.core.tests.kernelanalysis.AllTests.class,
    org.eclipse.tracecompass.analysis.os.linux.core.tests.latency.AllTests.class
})
public class AllTests {

}
