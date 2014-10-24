/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests;

import org.eclipse.tracecompass.tmf.core.tests.shared.DebugSuite;
import org.junit.runner.RunWith;

/**
 * Runner for the lttng2.kernel unit tests.
 */
@RunWith(DebugSuite.class)
@DebugSuite.SuiteClasses({
    ActivatorTest.class,
    org.eclipse.tracecompass.lttng2.ust.core.tests.analysis.memory.AllTests.class,
    org.eclipse.tracecompass.lttng2.ust.core.tests.trace.callstack.AllTests.class
})
public class AllTests {

}
