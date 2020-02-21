/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.xychart;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenXYDataProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.XmlDataProviderManager;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlViewInfo;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;

/**
 * Tree Viewer for the {@link XmlXYView}
 *
 * @author Loic Prieur-Drevon
 */
public class XmlTreeViewer extends AbstractSelectTreeViewer {

    private class XmlLabelProvider extends TreeLabelProvider {

        @Override
        public @Nullable Image getColumnImage(@Nullable Object element, int columnIndex) {
            if (columnIndex == 1 && element instanceof TmfGenericTreeEntry && isChecked(element)) {
                TmfTreeDataModel model = ((TmfGenericTreeEntry<TmfTreeDataModel>) element).getModel();
                if (model.getParentId() < 0) {
                    // do not show the legend for the trace entries.
                    return null;
                }
                return getLegendImage(String.valueOf(model.getName()));
            }
            return null;
        }
    }

    private final XmlViewInfo fViewInfo;

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     * @param viewInfo
     *            {@link XmlViewInfo} to manage the info on the class
     */
    public XmlTreeViewer(Composite parent, XmlViewInfo viewInfo) {
        super(parent, 1, DataDrivenXYDataProvider.ID);
        fViewInfo = viewInfo;
        setLabelProvider(new XmlLabelProvider());
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> ImmutableList.of(
                createColumn(Messages.XmlTree_Name, Comparator.comparing(TmfGenericTreeEntry::getName)),
                new TmfTreeColumnData(Messages.XmlTree_Legend));
    }

    @Override
    protected @Nullable ITmfTreeDataProvider<ITmfTreeDataModel> getProvider(ITmfTrace trace) {
        Element viewElement = fViewInfo.getViewElement(TmfXmlStrings.XY_VIEW);
        if (viewElement == null) {
            return null;
        }
        return XmlDataProviderManager.getInstance().getXyProvider(trace, viewElement);
    }

}
