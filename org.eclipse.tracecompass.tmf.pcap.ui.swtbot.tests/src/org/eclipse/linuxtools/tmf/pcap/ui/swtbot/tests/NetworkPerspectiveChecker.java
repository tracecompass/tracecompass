/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.pcap.ui.swtbot.tests;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.linuxtools.internal.tmf.pcap.ui.NetworkingPerspectiveFactory;
import org.eclipse.linuxtools.internal.tmf.pcap.ui.stream.StreamListView;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.AbstractPerspectiveChecker;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorsView;
import org.eclipse.linuxtools.tmf.ui.views.filter.FilterView;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramView;
import org.eclipse.linuxtools.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.ui.IPageLayout;
import org.junit.Before;

/**
 * Tracing perspective view checker
 *
 * @author Matthew Khouzam
 */
public class NetworkPerspectiveChecker extends AbstractPerspectiveChecker {

    /**
     * Set up arrays for test
     */
    @Before
    public void init() {
        fPerspectiveId = NetworkingPerspectiveFactory.ID;
        fViewIds = new ArrayList<>();
        fViewIds.addAll(Arrays.asList(new String[] {
                // TMF views
                HistogramView.ID,
                TmfStatisticsView.ID,
                FilterView.ID,
                ColorsView.ID,
                // PCAP
                StreamListView.ID,
                // Standard Eclipse views
                IPageLayout.ID_PROJECT_EXPLORER,
                IPageLayout.ID_PROP_SHEET,
                IPageLayout.ID_BOOKMARKS
        }));
    }
}
