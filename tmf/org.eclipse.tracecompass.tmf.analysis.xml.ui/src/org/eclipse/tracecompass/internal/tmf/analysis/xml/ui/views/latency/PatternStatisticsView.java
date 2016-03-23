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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentStoreStatisticsView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentStoreStatisticsViewer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlLatencyViewInfo;

/**
 * The statistic view for pattern latency analysis
 *
 * @author Jean-Christian Kouame
 */
public class PatternStatisticsView extends AbstractSegmentStoreStatisticsView {

    private PatternStatisticsViewer fViewer;
    /** The view's ID */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.statistics"; //$NON-NLS-1$

    private final XmlLatencyViewInfo fViewInfo = new XmlLatencyViewInfo(ID);

    /**
     * Constructor
     */
    public PatternStatisticsView() {
        this.addPartPropertyListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(TmfXmlUiStrings.XML_LATENCY_OUTPUT_DATA)) {
                    Object newValue = event.getNewValue();
                    if (newValue instanceof String) {
                        String data = (String) newValue;
                        fViewInfo.setViewData(data);
                        loadStatisticView();
                    }
                }
            }
        });
    }

    private void loadStatisticView() {
        if (fViewer != null) {
            fViewer.updateViewer(fViewInfo.getViewAnalysisId());
        }
    }

    @Override
    protected @NonNull AbstractSegmentStoreStatisticsViewer createSegmentStoreStatisticsViewer(@NonNull Composite parent) {
        PatternStatisticsViewer viewer = new PatternStatisticsViewer(parent);
        fViewer = viewer;
        loadStatisticView();
        return viewer;
    }
}
