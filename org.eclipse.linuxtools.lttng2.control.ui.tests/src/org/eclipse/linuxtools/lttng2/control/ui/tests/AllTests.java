/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.control.ui.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runner for the test suite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ActivatorTest.class,
    org.eclipse.linuxtools.lttng2.control.ui.tests.model.component.AllTests.class,
    org.eclipse.linuxtools.lttng2.control.ui.tests.service.AllTests.class
})
public class AllTests {

}
