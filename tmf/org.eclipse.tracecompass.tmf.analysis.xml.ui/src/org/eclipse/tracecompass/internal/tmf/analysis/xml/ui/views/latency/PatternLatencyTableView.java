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
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlLatencyViewInfo;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;

/**
 * The latency table view for pattern analysis
 *
 * @author Jean-Christian Kouame
 */
public class PatternLatencyTableView extends AbstractSegmentStoreTableView {

    /** The view's ID */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latencytable"; //$NON-NLS-1$

    private final XmlLatencyViewInfo fViewInfo = new XmlLatencyViewInfo(ID);

    private PatternLatencyTableViewer fViewer;

    /**
     * Constructor
     */
    public PatternLatencyTableView() {
        this.addPartPropertyListener(event -> {
            if (event.getProperty().equals(TmfXmlStrings.XML_LATENCY_OUTPUT_DATA)) {
                Object newValue = event.getNewValue();
                if (newValue instanceof String) {
                    String data = (String) newValue;
                    fViewInfo.setViewData(data);
                    setPartName(fViewInfo.getLabel());
                    loadLatencyView();
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

    private void loadLatencyView() {
        if (fViewer != null) {
            fViewer.updateViewer(fViewInfo.getViewAnalysisId());
        }
    }

    @Override
    protected @NonNull AbstractSegmentStoreTableViewer createSegmentStoreViewer(@NonNull TableViewer tableViewer) {
        PatternLatencyTableViewer viewer = new PatternLatencyTableViewer(tableViewer);
        fViewer = viewer;
        loadLatencyView();
        return viewer;
    }

}
