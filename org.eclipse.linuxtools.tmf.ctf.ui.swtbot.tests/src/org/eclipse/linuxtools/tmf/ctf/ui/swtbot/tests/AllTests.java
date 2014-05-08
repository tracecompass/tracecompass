/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.ui.swtbot.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * SWTBot test suite for tmf.ui
 *
 * @author Matthew Khouzam
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ImportAndReadSmokeTest.class,
        StandardImportAndReadSmokeTest.class
})
public class AllTests {
}
