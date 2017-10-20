package org.eclipse.tracecompass.internal.lttng2.ust.ui.views.memusage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.memory.MemoryUsageTreeModel;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.memory.UstMemoryUsageDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

/**
 * Tree viewer to select which process to display in the UST memory usage
 * chart.
 *
 * @author Loic Prieur-Drevon
 */
public class MemoryUsageTreeViewer extends AbstractSelectTreeViewer {

    private class MemoryLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof TmfGenericTreeEntry) {
                TmfGenericTreeEntry<MemoryUsageTreeModel> genericEntry = (TmfGenericTreeEntry<MemoryUsageTreeModel>) element;
                int tid = genericEntry.getModel().getTid();
                if (columnIndex == 0) {
                    return genericEntry.getName();
                } else if (columnIndex == 1 && tid >= 0) {
                    // do not display the dummy TID for the trace entry
                    return Integer.toString(tid);
                }
            }
            return null;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 2 && element instanceof TmfGenericTreeEntry && isChecked(element)) {
                TmfGenericTreeEntry<MemoryUsageTreeModel> genericEntry = (TmfGenericTreeEntry<MemoryUsageTreeModel>) element;
                if (genericEntry.hasChildren()) {
                    return null;
                }
                return getLegendImage(Integer.toString(genericEntry.getModel().getTid()));
            }
            return null;
        }
    }

    public MemoryUsageTreeViewer(Composite parent, TriStateFilteredCheckboxTree checkboxTree) {
        super(parent, checkboxTree, 2, UstMemoryUsageDataProvider.ID);
        setLabelProvider(new MemoryLabelProvider());
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> {
            List<TmfTreeColumnData> columns = new ArrayList<>(3);
            TmfTreeColumnData column = new TmfTreeColumnData(Messages.MemoryUsageTree_ColumnName);
            column.setComparator(new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    TmfGenericTreeEntry<MemoryUsageTreeModel> n1 = (TmfGenericTreeEntry<MemoryUsageTreeModel>) e1;
                    TmfGenericTreeEntry<MemoryUsageTreeModel> n2 = (TmfGenericTreeEntry<MemoryUsageTreeModel>) e2;

                    return Integer.compare(n1.getModel().getTid(), n2.getModel().getTid());
                }
            });
            columns.add(column);
            column = new TmfTreeColumnData(Messages.MemoryUsageTree_ColumnProcess);
            column.setComparator(new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    TmfGenericTreeEntry<MemoryUsageTreeModel> n1 = (TmfGenericTreeEntry<MemoryUsageTreeModel>) e1;
                    TmfGenericTreeEntry<MemoryUsageTreeModel> n2 = (TmfGenericTreeEntry<MemoryUsageTreeModel>) e2;

                    return n1.getName().compareTo(n2.getName());
                }
            });
            columns.add(column);
            column = new TmfTreeColumnData(Messages.MemoryUsageTree_Legend);
            columns.add(column);
            return columns;
        };
    }

}
