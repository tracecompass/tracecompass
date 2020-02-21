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
public class CustomXmlExtensionTest extends AbstractCustomTraceExtensionTest {
    private static final String XML_TRACE_TYPE_EXTENSION_ID = "custom.xml.trace:Custom XML:testxmlextension";
    private static final String TRACE_PATH_NAME = "testfiles/xml/valid/001.xml";

    @Override
    protected String getExtensionId() {
        return XML_TRACE_TYPE_EXTENSION_ID;
    }

    @Override
    protected String getTestTracePath() {
        return TRACE_PATH_NAME;
    }
}
