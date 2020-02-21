/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

/**
 * SWTBot test for Resources view Find dialog
 *
 * @author Jean-Christian Kouame
 */
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
