/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.ResourcesView;

/**
 * Test the expand state of time graph entries for the {@link ResourcesView}
 *
 * @author Jean-Christian Kouame
 *
 */
public class ResourcesViewExpandStateTest extends TimegraphViewExpandStateTestBase {

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
        return new String[] {"CPU 0", "CPU 1"};
    }
}
