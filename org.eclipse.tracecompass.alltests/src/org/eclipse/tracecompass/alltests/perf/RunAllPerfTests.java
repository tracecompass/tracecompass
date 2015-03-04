/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.alltests.perf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite for all Trace Compass performance tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.eclipse.tracecompass.ctf.core.tests.perf.AllPerfTests.class,
    org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.AllPerfTests.class,
    org.eclipse.tracecompass.pcap.core.tests.perf.AllPerfTests.class,
    org.eclipse.tracecompass.tmf.core.tests.perf.AllPerfTests.class,
    org.eclipse.tracecompass.tmf.ctf.core.tests.perf.AllPerfTests.class
})
public class RunAllPerfTests {

}
