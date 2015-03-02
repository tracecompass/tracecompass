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

package org.eclipse.tracecompass.tmf.remote.core.tests;

import org.eclipse.tracecompass.tmf.core.tests.shared.DebugSuite;
import org.junit.runner.RunWith;

/**
 * Master test suite for TMF Core.
 */
@RunWith(DebugSuite.class)
@DebugSuite.SuiteClasses({
    TmfRemoteCorePluginTest.class,
    org.eclipse.tracecompass.tmf.remote.core.tests.shell.AllTests.class
})
public class AllTmfRemoteCoreTests {

}
