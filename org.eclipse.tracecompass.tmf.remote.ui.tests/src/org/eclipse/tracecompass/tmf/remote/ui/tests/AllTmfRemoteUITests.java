/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.remote.ui.tests;

import org.eclipse.tracecompass.tmf.core.tests.shared.DebugSuite;
import org.eclipse.tracecompass.tmf.remote.ui.tests.fetch.AllTests;
import org.junit.runner.RunWith;

/**
 * Master test suite for TMF UI Core.
 */
@RunWith(DebugSuite.class)
@DebugSuite.SuiteClasses({
    AllTests.class
})
public class AllTmfRemoteUITests {

}
