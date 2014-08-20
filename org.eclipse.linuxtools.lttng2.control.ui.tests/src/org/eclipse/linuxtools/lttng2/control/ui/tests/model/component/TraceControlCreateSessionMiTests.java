/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jonathan Rajotte - Support for machine interface LTTng 2.6
 **********************************************************************/

package org.eclipse.linuxtools.lttng2.control.ui.tests.model.component;

/**
 * Machine interface Kernel session manipulation handling test cases.
 * LTTng 2.6
 */
public class TraceControlCreateSessionMiTests extends TraceControlCreateSessionTests {

    private static final String TEST_STREAM = "CreateSessionTestMi.cfg";

    @Override
    protected String getTestStream() {
        return TEST_STREAM;
    }
}
