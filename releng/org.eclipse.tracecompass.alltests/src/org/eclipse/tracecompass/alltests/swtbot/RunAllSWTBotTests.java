/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.alltests.swtbot;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite for all Trace Compass SWTBot unit tests.
 *
 * Note that the SWTBot tests need to be executed in a non-UI thread
 * which is why they are separated in a different test suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    // FIXME Link to all swtbot tests
    org.eclipse.tracecompass.tmf.ui.swtbot.tests.views.TimeGraphViewTest.class
})
public class RunAllSWTBotTests {

}
