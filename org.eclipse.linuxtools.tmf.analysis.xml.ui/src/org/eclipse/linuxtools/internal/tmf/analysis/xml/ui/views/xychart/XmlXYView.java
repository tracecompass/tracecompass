/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.views.xychart;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.views.XmlViewInfo;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.linuxtools.tmf.ui.views.TmfChartView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;

/**
 * This view displays state system data in an xy chart. It uses an XML
 * {@link TmfXmlUiStrings#XY_VIEW} element from an XML file. This element
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

        this.addPartPropertyListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(@Nullable PropertyChangeEvent event) {
                if (event == null) {
                    return;
                }
                if (event.getProperty().equals(TmfXmlUiStrings.XML_OUTPUT_DATA)) {
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
            }

        });
        setViewTitle();
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        setChartViewer(new XmlXYViewer(parent, fViewInfo));
        super.createPartControl(parent);
    }

    @Override
    public void setFocus() {

    }

    private void setViewTitle() {
        /*
         * Get the view element from the XML file. If the element can't be
         * found, return.
         */
        Element viewElement = fViewInfo.getViewElement(TmfXmlUiStrings.XY_VIEW);
        if (viewElement == null) {
            return;
        }

        String title = fViewInfo.getViewTitle(viewElement);
        if (title == null) {
            title = Messages.XmlXYView_DefaultTitle;
        }
        final String viewTitle = title;
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                setPartName(viewTitle);
            }
        });
    }

}
