/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowView;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.ResourcesView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.PerspectiveFactory;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.AbstractPerspectiveChecker;
import org.eclipse.tracecompass.tmf.ui.views.histogram.HistogramView;
import org.eclipse.tracecompass.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.ui.IPageLayout;
import org.junit.Before;

/**
 * Tracing perspective view checker
 *
 * @author Matthew Khouzam
 */
public class KernelPerspectiveChecker extends AbstractPerspectiveChecker {

    /**
     * Set up arrays for test
     */
    @Before
    public void init() {
        fPerspectiveId = PerspectiveFactory.ID;
        fViewIds = new ArrayList<>();
        fViewIds.addAll(Arrays.asList(new String[] {
                // LTTng views
                HistogramView.ID,
                ControlView.ID,
                ControlFlowView.ID,
                ResourcesView.ID,
                TmfStatisticsView.ID,
                // Standard Eclipse views
                IPageLayout.ID_PROJECT_EXPLORER,
                IPageLayout.ID_PROP_SHEET,
                IPageLayout.ID_BOOKMARKS
        }));
    }
}
