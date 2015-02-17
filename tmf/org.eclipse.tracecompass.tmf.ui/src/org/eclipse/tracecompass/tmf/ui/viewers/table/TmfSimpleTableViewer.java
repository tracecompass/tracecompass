/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.table;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;

/**
 * Generic {@link TableViewer} wrapper with most standard features enabled
 *
 * <pre>
 * It provides the following features:
 *   - Sortable columns
 *   - Movable columns
 *   - Resizable columns
 *   - Tracking last clicked columns
 * </pre>
 *
 * The person extending this class must implement the {@link #createColumns()},
 * they must also supply a content provider of the {@link TableViewer} obtained
 * by {@link #getTableViewer()},
 * {@link TableViewer#setContentProvider(IContentProvider)} along with an input
 * to {@link TableViewer#setInput(Object)}. They can also add selection
 * listeners to the {@link Table} obtained from the {@link TableViewer} by
 * calling {@link TableViewer#getTable()} with
 * {@link Table#addSelectionListener(SelectionListener)}
 *
 * @since 1.0
 */
public class TmfSimpleTableViewer extends TmfViewer {

    private final class MouseColumnListener extends MouseAdapter {
        @Override
        public void mouseDown(MouseEvent e) {
            ViewerCell cell = fTableViewer.getCell(new Point(e.x, e.y));
            fSelectedColumn = (cell != null) ? cell.getColumnIndex() : -1;
        }
    }

    private final class ColumnSorter extends SelectionAdapter {
        private final TableColumn fColumn;

        private ColumnSorter(TableColumn column) {
            fColumn = column;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {

            Table table = fTableViewer.getTable();
            table.setSortDirection(getSortDirection());
            TableColumn prevSortcolumn = table.getSortColumn();
            if (prevSortcolumn == fColumn) {
                flipSortDirection();
            }
            table.setSortColumn(fColumn);
            ViewerCompoundComparator comparator = fComparators.get(fColumn.getText());
            if (fDirection == SWT.DOWN) {
                fTableViewer.setComparator(comparator);
            } else {
                fTableViewer.setComparator(new InvertSorter(comparator));
            }
        }
    }

    private class InvertSorter extends ViewerCompoundComparator {
        private final ViewerComparator fViewerComparator;

        public InvertSorter(ViewerComparator vc) {
            fViewerComparator = vc;
        }

        @Override
        public int compare(Object e1, Object e2) {
            return -fViewerComparator.compare(null, e1, e2);
        }

    }

    private static final int DEFAULT_COL_WIDTH = 200;
    private final TableViewer fTableViewer;
    private final Map<String, ViewerCompoundComparator> fComparators = new HashMap<>();

    private int fDirection;
    private int fSelectedColumn;

    /**
     * Constructor that initializes the parent of the viewer
     *
     * @param table
     *            the {@link TableViewer} to wrap
     */
    public TmfSimpleTableViewer(TableViewer table) {
        super(table.getControl().getParent());
        fTableViewer = table;
        createColumns();

        final Table tableControl = fTableViewer.getTable();
        tableControl.setHeaderVisible(true);
        tableControl.setLinesVisible(true);

        fDirection = SWT.DOWN;
        fTableViewer.setUseHashlookup(true);
        fTableViewer.getControl().addMouseListener(new MouseColumnListener());
        refresh();
    }

    /**
     * Create a column for the table
     *
     * @param name
     *            the name of the column (must be unique)
     * @param provider
     *            the provider of the column
     * @param viewerComparator
     *            the comparator associated with clicking on the column, if it
     *            is null, a string comparator will be used
     */
    protected final void createColumn(String name, ColumnLabelProvider provider, ViewerCompoundComparator viewerComparator) {
        if (fComparators.containsKey(name)) {
            throw new IllegalArgumentException("Cannot have two columns with the same name"); //$NON-NLS-1$
        }
        TableViewerColumn col = new TableViewerColumn(fTableViewer, SWT.NONE);
        col.setLabelProvider(provider);
        final TableColumn column = col.getColumn();
        column.setWidth(DEFAULT_COL_WIDTH);
        column.setText(name);
        column.setResizable(true);
        column.setMoveable(true);
        column.addSelectionListener(new ColumnSorter(column));
        final ViewerCompoundComparator comparator = (viewerComparator == null) ? ViewerCompoundComparator.STRING_COMPARATOR : viewerComparator;
        fComparators.put(name, comparator);
    }

    /**
     * Column initializer, called in the constructor. This needs to be
     * overridden. Use the
     * {@link #createColumn(String, ColumnLabelProvider, ViewerCompoundComparator)}
     * method to help create columns.
     */
    protected void createColumns() {
        // override me!
    }

    private void flipSortDirection() {
        if (fDirection == SWT.DOWN) {
            fDirection = SWT.UP;
        } else {
            fDirection = SWT.DOWN;
        }

    }

    private int getSortDirection() {
        return fDirection;
    }

    @Override
    public final Control getControl() {
        return fTableViewer.getControl();
    }

    /**
     * Gets the wrapped table viewer
     *
     * @return the table viewer
     */
    public final TableViewer getTableViewer() {
        return fTableViewer;
    }

    /**
     * Get the selected column index
     *
     * @return the selected column index or -1
     */
    public final int getColumnIndex() {
        return fSelectedColumn;
    }

    @Override
    public final void refresh() {
        fTableViewer.refresh();
    }
}
