/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests;

import org.eclipse.tracecompass.tmf.core.tests.shared.DebugSuite;
import org.junit.runner.RunWith;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all of
 * the tests within its package as well as within any subpackages of its
 * package.
 *
 * @author ematkho
 */
@RunWith(DebugSuite.class)
@DebugSuite.SuiteClasses({
        org.eclipse.tracecompass.tmf.ctf.core.tests.context.AllTests.class,
        org.eclipse.tracecompass.tmf.ctf.core.tests.event.AllTests.class,
        org.eclipse.tracecompass.tmf.ctf.core.tests.iterator.AllTests.class,
        org.eclipse.tracecompass.tmf.ctf.core.tests.trace.AllTests.class,
        org.eclipse.tracecompass.tmf.ctf.core.tests.trace.indexer.AllTests.class,

        /* Tests in other packages (that are there because of CTF) */
        org.eclipse.tracecompass.tmf.ctf.core.tests.temp.request.AllTests.class,
        org.eclipse.tracecompass.tmf.ctf.core.tests.temp.statistics.AllTests.class,
        org.eclipse.tracecompass.tmf.ctf.core.tests.temp.tracemanager.AllTests.class
})
public class AllTests {

}
