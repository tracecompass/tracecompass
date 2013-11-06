/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.ui.swtbot.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for UI on the lttng kernel perspective
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ImportAndReadKernelSmokeTest.class
})
public class AllTests {

}
