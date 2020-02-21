/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Jonathan Rajotte - Support for machine interface LTTng 2.6
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.tests.model.component;

/**
 * Machine interface Kernel session manipulation handling test cases.
 * LTTng 2.6
 */
public class TraceControlCreateSessionMiTest extends TraceControlCreateSessionTest {

    private static final String TEST_STREAM = "CreateSessionTestMi.cfg";

    @Override
    protected String getTestStream() {
        return TEST_STREAM;
    }
}
