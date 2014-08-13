/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.alltests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite for all SWTBot Linux Tools LTTng unit tests.
 *
 * Note that the SWTBot tests need to be executed in a non-UI thread
 * which is why they are separated in a different test suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.eclipse.linuxtools.tmf.ui.swtbot.tests.AllTmfUISWTBotTests.class,
    org.eclipse.linuxtools.tmf.ctf.ui.swtbot.tests.AllTests.class,
    org.eclipse.linuxtools.tmf.pcap.ui.swtbot.tests.AllTests.class,
    org.eclipse.linuxtools.lttng2.kernel.ui.swtbot.tests.AllTests.class
})
public class RunAllSWTBotTests {

}
