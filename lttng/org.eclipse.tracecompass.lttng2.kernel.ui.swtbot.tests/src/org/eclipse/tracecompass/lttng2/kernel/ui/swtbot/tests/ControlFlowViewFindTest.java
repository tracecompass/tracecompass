/*******************************************************************************
 * Copyright (c) 2016, 2020 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * SWTBot test for Control Flow View Find dialog
 *
 * @author Jean-Christian Kouame
 */
@RunWith(Parameterized.class)
public class ControlFlowViewFindTest extends FindDialogTestBase {

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "lttng", true },
                { "492", false },
        });
    }

    private static final String TITLE = "Control Flow";
    private final String fText;
    private final boolean fIsAlphaNumeric;

    /**
     * Constructor
     *
     * @param text
     *            The text that must be present in the entry
     * @param isAlpha
     *            Whether this test case is alphanumeric (in which case the case
     *            sensitive and whole word tests will be run)
     */
    public ControlFlowViewFindTest(String text, boolean isAlpha) {
        fText = text;
        fIsAlphaNumeric = isAlpha;
    }

    @Override
    protected String getViewTitle() {
        return TITLE;
    }

    @Override
    protected String getFindText() {
        return fText;
    }

    /**
     * Test the case sensitive search option
     */
    @Override
    @Test
    public void testCaseSensitive() {
        if (fIsAlphaNumeric) {
            super.testCaseSensitive();
        }
    }

    /**
     * Test the whole word search option
     */
    @Override
    @Test
    public void testWholeWord() {
        if (fIsAlphaNumeric) {
            super.testWholeWord();
        }
    }

}
