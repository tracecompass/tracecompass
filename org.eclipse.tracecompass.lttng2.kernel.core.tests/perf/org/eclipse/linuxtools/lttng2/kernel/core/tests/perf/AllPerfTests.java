/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.perf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Run all performance test suites.
 *
 * @author Alexandre Montplaisir
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        org.eclipse.linuxtools.lttng2.kernel.core.tests.perf.analysis.AllPerfTests.class,
        org.eclipse.linuxtools.lttng2.kernel.core.tests.perf.event.matching.AllPerfTests.class
})
public class AllPerfTests {

}
