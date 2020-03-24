/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.ResourcesView;

/**
 * Test the expand state of time graph entries for the {@link ResourcesView}
 *
 * @author Jean-Christian Kouame
 *
 */
public class ResourcesViewUiContextTest extends TimeGraphViewUiContextTestBase {

    @Override
    protected String getViewId() {
        return ResourcesView.ID;
    }

    @Override
    protected String getViewTitle() {
        return "Resources";
    }

    @Override
    protected String[] getItemLabel() {
        return new String[] {"CPU 0 States", "CPU 1 States"};
    }
}
