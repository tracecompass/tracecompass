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
 * SWTBot test for Resources view Find dialog
 *
 * @author Jean-Christian Kouame
 */
@Ignore
public class ResourcesViewFindTest extends FindDialogTestBase {

    private static final String TITLE = "Resources";
    private static final String TEXT = "CPU 1";

    @Override
    protected String getViewTitle() {
        return TITLE;
    }

    @Override
    protected String getFindText() {
        return TEXT;
    }

}
