/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentsStatisticsView;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlLatencyViewInfo;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;

/**
 * The statistic view for pattern latency analysis
 *
 * @author Jean-Christian Kouame
 */
public class PatternStatisticsView extends AbstractSegmentsStatisticsView {

    private PatternStatisticsViewer fViewer;
    /** The view's ID */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.statistics"; //$NON-NLS-1$

    private final XmlLatencyViewInfo fViewInfo = new XmlLatencyViewInfo(ID);

    /**
     * Constructor
     */
    public PatternStatisticsView() {
        this.addPartPropertyListener(event -> {
            if (event.getProperty().equals(TmfXmlStrings.XML_LATENCY_OUTPUT_DATA)) {
                Object newValue = event.getNewValue();
                if (newValue instanceof String) {
                    String data = (String) newValue;
                    fViewInfo.setViewData(data);
                    setPartName(fViewInfo.getLabel());
                    loadStatisticView();
                }
            }
        });
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        String name = getViewSite().getSecondaryId();
        if (name != null) {
            /* must initialize view info before calling super */
            fViewInfo.setName(name);
        }
        super.createPartControl(parent);
        Display.getDefault().asyncExec(() -> setPartName(fViewInfo.getLabel()));
    }

    private void loadStatisticView() {
        if (fViewer != null) {
            fViewer.updateViewer(fViewInfo.getViewAnalysisId());
        }
    }

    @Override
    protected @NonNull AbstractTmfTreeViewer createSegmentStoreStatisticsViewer(@NonNull Composite parent) {
        PatternStatisticsViewer viewer = new PatternStatisticsViewer(parent);
        fViewer = viewer;
        loadStatisticView();
        return viewer;
    }
}
