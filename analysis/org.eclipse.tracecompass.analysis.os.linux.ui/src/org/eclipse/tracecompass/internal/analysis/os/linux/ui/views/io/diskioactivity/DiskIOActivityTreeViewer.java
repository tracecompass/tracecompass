/*******************************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.io.diskioactivity;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.DisksIODataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer2;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;

import com.google.common.collect.ImmutableList;

/**
 * Disk IO Activity Tree Viewer
 *
 * @author Loic Prieur-Drevon
 */
public class DiskIOActivityTreeViewer extends AbstractSelectTreeViewer2 {

    private final class DiskTreeLabelProvider extends DataProviderTreeLabelProvider {

        @Override
        public @Nullable Image getColumnImage(@Nullable Object element, int columnIndex) {
            if (columnIndex == 1 && element instanceof TmfGenericTreeEntry && isChecked(element)) {
                TmfGenericTreeEntry<TmfTreeDataModel> entry = (TmfGenericTreeEntry<TmfTreeDataModel>) element;
                if (!entry.hasChildren()) {
                    // ensures that only leaf nodes return images
                    return getLegendImage(entry.getModel().getId());
                }
            }
            return null;
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     */
    public DiskIOActivityTreeViewer(Composite parent) {
        super(parent, 1, DisksIODataProvider.ID);
        setLabelProvider(new DiskTreeLabelProvider());
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> {
            return ImmutableList.of(
                    createColumn(Messages.DiskIOActivityTreeViewer_DiskName, Comparator.comparing(TmfGenericTreeEntry::getName)),
                    new TmfTreeColumnData(Messages.DiskIOActivityTreeViewer_Legend));
        };
    }

}
