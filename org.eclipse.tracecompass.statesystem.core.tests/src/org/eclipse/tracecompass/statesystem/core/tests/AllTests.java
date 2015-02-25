/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Enable StateSystemUtils tests
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        StateSystemPushPopTest.class,
        StateSystemUtilsTest.class,
        org.eclipse.tracecompass.statesystem.core.tests.backend.AllTests.class,
        org.eclipse.tracecompass.statesystem.core.tests.statevalue.AllTests.class
})
public class AllTests {

}
