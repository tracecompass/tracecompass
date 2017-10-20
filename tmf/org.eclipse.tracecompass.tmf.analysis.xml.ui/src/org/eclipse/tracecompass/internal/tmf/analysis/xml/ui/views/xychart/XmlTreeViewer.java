/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.xychart;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlViewInfo;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlDataProviderManager;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlXYDataProvider;
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
                TmfGenericTreeEntry<TmfTreeDataModel> genericEntry = (TmfGenericTreeEntry<TmfTreeDataModel>) element;
                if (genericEntry.getModel().getParentId() < 0) {
                    // do not show the legend for the trace entries.
                    return null;
                }
                return getLegendImage(Long.toString(genericEntry.getModel().getId()));
            }
            return null;
        }
    }

    private final XmlViewInfo fViewInfo;

    public XmlTreeViewer(Composite parent, XmlViewInfo viewInfo) {
        super(parent, 1, XmlXYDataProvider.ID);
        fViewInfo = viewInfo;
        setLabelProvider(new XmlLabelProvider());
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> {
            return ImmutableList.of(
                    createColumn(Messages.XmlTree_Name, Comparator.comparing(TmfGenericTreeEntry::getName)),
                    new TmfTreeColumnData(Messages.XmlTree_Legend));
        };
    }

    @Override
    protected @Nullable ITmfTreeDataProvider<TmfTreeDataModel> getProvider() {
        ITmfTrace trace = getTrace();
        Element viewElement = fViewInfo.getViewElement(TmfXmlStrings.XY_VIEW);
        if (trace == null || viewElement == null) {
            return null;
        }
        return XmlDataProviderManager.getInstance().getXyProvider(trace, viewElement);
    }

}
