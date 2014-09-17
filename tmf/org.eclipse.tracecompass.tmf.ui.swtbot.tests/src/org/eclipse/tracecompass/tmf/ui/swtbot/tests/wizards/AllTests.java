/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.wizards;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * SWTBot test suite for testing of the TMF events table.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    StandardImportGzipTraceTest.class,
    TestImportExportPackageWizard.class
})
public class AllTests {
}
