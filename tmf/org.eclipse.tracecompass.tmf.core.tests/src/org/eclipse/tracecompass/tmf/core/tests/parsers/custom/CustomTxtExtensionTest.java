/******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.parsers.custom;

/**
 * Tests for Custom Text trace extension point.
 */
public class CustomTxtExtensionTest extends AbstractCustomTraceExtensionTest {
    private static final String TEXT_TRACE_TYPE_EXTENSION_ID = "custom.txt.trace:Custom Text:testtxtextension";
    private static final String TRACE_PATH_NAME = "testfiles/txt/valid/001.txt";

    @Override
    protected String getExtensionId() {
        return TEXT_TRACE_TYPE_EXTENSION_ID;
    }

    @Override
    protected String getTestTracePath() {
        return TRACE_PATH_NAME;
    }
}
