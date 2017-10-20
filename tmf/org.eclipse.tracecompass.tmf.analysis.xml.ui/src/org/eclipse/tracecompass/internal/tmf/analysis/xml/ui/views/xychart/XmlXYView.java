/*******************************************************************************
 * Copyright (c) 2014, 2017 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.xychart;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlViewInfo;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.XYChartLegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
import org.eclipse.tracecompass.tmf.ui.views.TmfViewFactory;
import org.w3c.dom.Element;

/**
 * This view displays state system data in an xy chart. It uses an XML
 * {@link TmfXmlStrings#XY_VIEW} element from an XML file. This element
 * defines which entries from the state system will be shown and also gives
 * additional information on the presentation of the view.
 *
 * @author Geneviève Bastien
 */
public class XmlXYView extends TmfChartView {

    /** View ID. */
    public static final String ID = "org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.views.xyview"; //$NON-NLS-1$

    private final XmlViewInfo fViewInfo = new XmlViewInfo(ID);

    /**
     * Default constructor
     */
    public XmlXYView() {
        super(Messages.XmlXYView_DefaultTitle);

        addPartPropertyListener((@Nullable PropertyChangeEvent event) -> {
            if (event == null) {
                return;
            }
            if (event.getProperty().equals(TmfXmlStrings.XML_OUTPUT_DATA)) {
                Object newValue = event.getNewValue();
                if (newValue instanceof String) {
                    fViewInfo.setViewData((String) newValue);
                    setViewTitle();
                    TmfXYChartViewer viewer = getChartViewer();
                    if (viewer instanceof XmlXYViewer) {
                        ((XmlXYViewer) viewer).viewInfoUpdated();
                    }
                }
            }
        });
    }

    private void setViewTitle() {
        /*
         * Get the view element from the XML file. If the element can't be
         * found, return.
         */
        Element viewElement = fViewInfo.getViewElement(TmfXmlStrings.XY_VIEW);
        if (viewElement == null) {
            return;
        }

        String title = fViewInfo.getViewTitle(viewElement);
        Display.getDefault().asyncExec(() -> setPartName(title != null ? title : Messages.XmlXYView_DefaultTitle));
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        String name = getViewSite().getSecondaryId();
        if (name != null) {
            name = TmfViewFactory.getBaseSecId(name);
        }
        if (name != null) {
            /* must initialize view info before calling super */
            fViewInfo.setName(name);
        }
        super.createPartControl(parent);
        setViewTitle();

        TmfViewer tree = getLeftChildViewer();
        TmfXYChartViewer chart = getChartViewer();
        if (tree instanceof XmlTreeViewer && chart instanceof TmfFilteredXYChartViewer) {
            ILegendImageProvider legendImageProvider = new XYChartLegendImageProvider((TmfCommonXAxisChartViewer) chart);
            XmlTreeViewer kernelMemoryTree = (XmlTreeViewer) tree;
            kernelMemoryTree.setTreeListener((TmfFilteredXYChartViewer) chart);
            kernelMemoryTree.setLegendImageProvider(legendImageProvider);
        }
    }

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        TmfXYChartSettings settings = new TmfXYChartSettings(Messages.XmlXYViewer_DefaultViewerTitle,
                Messages.XmlXYViewer_DefaultXAxis, Messages.XmlXYViewer_DefaultYAxis, 1);
        return new XmlXYViewer(parent, settings, fViewInfo);
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(@Nullable Composite parent) {
        XmlTreeViewer treeViewer = new XmlTreeViewer(Objects.requireNonNull(parent), fViewInfo);

        /* Initialize the viewers with the currently selected trace */
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            treeViewer.traceSelected(new TmfTraceSelectedSignal(this, trace));
        }

        return treeViewer;
    }

}
