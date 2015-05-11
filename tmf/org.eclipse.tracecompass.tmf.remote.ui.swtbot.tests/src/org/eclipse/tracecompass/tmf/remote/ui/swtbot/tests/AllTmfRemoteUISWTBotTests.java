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

package org.eclipse.tracecompass.tmf.remote.ui.swtbot.tests;

import org.eclipse.tracecompass.tmf.remote.ui.swtbot.tests.fetch.FetchRemoteTracesTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * SWTBot test suite for tmf.ui
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    FetchRemoteTracesTest.class,
})
public class AllTmfRemoteUISWTBotTests {
}
