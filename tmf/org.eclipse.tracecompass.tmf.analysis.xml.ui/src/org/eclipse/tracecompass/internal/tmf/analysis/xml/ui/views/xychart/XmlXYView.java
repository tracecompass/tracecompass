/*******************************************************************************
 * Copyright (c) 2014, 2017 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.xychart.TmfChartView;
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

                    TmfViewer left = getLeftChildViewer();
                    if (left instanceof XmlTreeViewer) {
                        ((XmlTreeViewer) left).reset();
                    }

                    TmfXYChartViewer chart = getChartViewer();
                    if (chart instanceof XmlXYViewer) {
                        ((XmlXYViewer) chart).reset();
                    }

                    loadTrace();

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
    }

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        TmfXYChartSettings settings = new TmfXYChartSettings(Messages.XmlXYViewer_DefaultViewerTitle,
                Messages.XmlXYViewer_DefaultXAxis, Messages.XmlXYViewer_DefaultYAxis, 1);
        return new XmlXYViewer(parent, settings, fViewInfo);
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(@Nullable Composite parent) {
        return new XmlTreeViewer(Objects.requireNonNull(parent), fViewInfo);
    }

}
