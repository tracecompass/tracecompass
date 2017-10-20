/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.counters.core.CounterDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

import com.google.common.collect.Lists;

/**
 * Display the state system as a filtered checkbox tree:
 *
 * <pre>
 * {trace name}
 *   +- Grouped
 *   |   +- {group id}
 *   |   |   +- {group element}
 *   |   |       +- ...
 *   |   +- {group id}
 *   |       +- ...
 *   +- Ungrouped
 *       +- {counter}
 *       +- ...
 * </pre>
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public class CounterTreeViewer extends AbstractSelectTreeViewer {

    private final class CounterTreeLabelProvider extends TreeLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 1 && element instanceof TmfGenericTreeEntry && isChecked(element)) {
                TmfGenericTreeEntry<TmfTreeDataModel> genericEntry = (TmfGenericTreeEntry<TmfTreeDataModel>) element;
                if (genericEntry.hasChildren()) {
                    return null;
                }
                TmfTreeDataModel model = genericEntry.getModel();
                return getLegendImage(String.valueOf(model.getId()));
            }
            return null;
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     * @param checkboxTree
     *            <code>TriStateFilteredTree</code> wrapping a
     *            <code>CheckboxTreeViewer</code>
     */
    public CounterTreeViewer(Composite parent, TriStateFilteredCheckboxTree checkboxTree) {
        super(parent, checkboxTree, 1, CounterDataProvider.ID);
        setLabelProvider(new CounterTreeLabelProvider());
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> Lists.newArrayList(new TmfTreeColumnData("Counters"), new TmfTreeColumnData("Legend")); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
