/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import org.junit.Ignore;

/**
 * SWTBot test for Control Flow View Find dialog
 *
 * @author Jean-Christian Kouame
 */
@Ignore
public class ControlFlowViewFindTest extends FindDialogTestBase {

    private static final String TITLE = "Control Flow";
    private static final String TEXT = "lttng";

    @Override
    protected String getViewTitle() {
        return TITLE;
    }

    @Override
    protected String getFindText() {
        return TEXT;
    }

}
