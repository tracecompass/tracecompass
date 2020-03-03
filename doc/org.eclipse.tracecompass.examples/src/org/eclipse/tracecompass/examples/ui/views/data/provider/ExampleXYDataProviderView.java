/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.examples.ui.views.data.provider;

import java.util.Comparator;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.examples.core.data.provider.ExampleXYDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

import com.google.common.collect.ImmutableList;

/**
 * An example of a data provider XY view
 *
 * This class is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Geneviève Bastien
 */
public class ExampleXYDataProviderView extends TmfChartView {

    /** View ID. */
    public static final String ID = "org.eclipse.tracecompass.examples.dataprovider.xyview"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public ExampleXYDataProviderView() {
        super("Example Tree XY View"); //$NON-NLS-1$
    }

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        TmfXYChartSettings settings = new TmfXYChartSettings(null, null, null, 1);
        return new TmfFilteredXYChartViewer(parent, settings, ExampleXYDataProvider.ID);
    }

    private static final class TreeXyViewer extends AbstractSelectTreeViewer {

        private final class TreeXyLabelProvider extends AbstractSelectTreeViewer.TreeLabelProvider {
            @Override
            public @Nullable Image getColumnImage(@Nullable Object element, int columnIndex) {
                if (columnIndex == 1 && element instanceof TmfTreeViewerEntry && isChecked(element)) {
                    return getLegendImage(String.valueOf(((TmfTreeViewerEntry) element).getName()));
                }
                return null;
            }
        }

        public TreeXyViewer(Composite parent) {
            super(parent, 1, ExampleXYDataProvider.ID);
            setLabelProvider(new TreeXyLabelProvider());
        }

        @Override
        protected ITmfTreeColumnDataProvider getColumnDataProvider() {
            return () -> ImmutableList.of(createColumn("Name", Comparator.comparing(TmfTreeViewerEntry::getName)), //$NON-NLS-1$
                    new TmfTreeColumnData("Legend")); //$NON-NLS-1$
        }
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(@Nullable Composite parent) {
        return new TreeXyViewer(Objects.requireNonNull(parent));
    }
}
