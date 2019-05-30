/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;

/**
 * Generic {@link TableViewer} wrapper with most standard features enabled.
 * <p>
 * It provides the following features: <br>
 * - Sortable columns <br>
 * - Movable columns <br>
 * - Resizable columns <br>
 * - Tracking last clicked columns
 * <p>
 * The user of this class should add columns to the table by using the
 * {@link #createColumn(String, ColumnLabelProvider, Comparator)} method, and
 * set the content provider and input on the supplied {@link TableViewer}.
 *
 * @since 1.1
 */
public class TmfSimpleTableViewer extends TmfViewer {

    /**
     * Viewer comparator that ignores the element label strings and uses the
     * given comparator to compare the elements directly.
     */
    private static class ElementComparator<T> extends ViewerComparator {

        private Comparator<T> elementComparator;

        public ElementComparator(Comparator<T> comparator) {
            elementComparator = comparator;
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            return elementComparator.compare((T) e1, (T) e2);
        }
    }

    /**
     * Comparator that compares the text of a column given by the label
     * provider, using the text's String ordering.
     */
    private static class ColumnLabelComparator implements Comparator<Object> {
        private final ILabelProvider fLabelProvider;

        public ColumnLabelComparator(ILabelProvider labelProvider) {
            fLabelProvider = labelProvider;
        }

        @Override
        public int compare(Object o1, Object o2) {
            String s1 = nullToEmptyString(fLabelProvider.getText(o1));
            String s2 = nullToEmptyString(fLabelProvider.getText(o2));
            return s1.compareTo(s2);
        }
    }

    private final class MouseColumnListener extends MouseAdapter {
        @Override
        public void mouseDown(MouseEvent e) {
            ViewerCell cell = fTableViewer.getCell(new Point(e.x, e.y));
            fSelectedColumn = (cell != null) ? cell.getColumnIndex() : -1;
        }
    }

    private final class ColumnSorter<T> extends SelectionAdapter {
        private final @NonNull TableColumn fColumn;
        private final @NonNull Comparator<T> fComparator;

        private ColumnSorter(@NonNull TableColumn column, @NonNull Comparator<T> comparator) {
            fColumn = column;
            fComparator = comparator;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Table table = fTableViewer.getTable();
            TableColumn prevSortcolumn = table.getSortColumn();
            if (prevSortcolumn == fColumn) {
                flipSortDirection();
            }
            table.setSortDirection(fDirection);
            table.setSortColumn(fColumn);
            Comparator<T> comparator;
            if (fDirection == SWT.DOWN) {
                comparator = fComparator;
            } else {
                comparator = checkNotNull(Collections.reverseOrder(fComparator));
            }
            IContentProvider contentProvider = fTableViewer.getContentProvider();
            if (contentProvider instanceof DeferredContentProvider) {
                DeferredContentProvider deferredContentProvider = (DeferredContentProvider) contentProvider;
                deferredContentProvider.setSortOrder(comparator);
            } else if (contentProvider instanceof ISortingLazyContentProvider) {
                ISortingLazyContentProvider sortingLazyContentProvider = (ISortingLazyContentProvider) contentProvider;
                sortingLazyContentProvider.setSortOrder(comparator);
            } else {
                fTableViewer.setComparator(new ElementComparator<>(comparator));
            }
        }
    }

    private static final int DEFAULT_COL_WIDTH = 200;
    private final TableViewer fTableViewer;

    private int fDirection;
    private int fSelectedColumn;

    private MenuManager fTablePopupMenuManager;

    /**
     * Constructor that initializes the parent of the viewer
     *
     * @param table
     *            the {@link TableViewer} to wrap
     */
    public TmfSimpleTableViewer(TableViewer table) {
        super(table.getControl().getParent());
        fTableViewer = table;

        final Table tableControl = fTableViewer.getTable();
        tableControl.setHeaderVisible(true);
        tableControl.setLinesVisible(true);

        fDirection = SWT.DOWN;
        fTableViewer.setUseHashlookup(true);
        fTableViewer.getControl().addMouseListener(new MouseColumnListener());

        fTablePopupMenuManager = new MenuManager();
        fTablePopupMenuManager.setRemoveAllWhenShown(true);

        fTablePopupMenuManager.addMenuListener((final @Nullable IMenuManager manager) -> {
            TableViewer viewer = getTableViewer();
            ISelection selection = viewer.getSelection();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection sel = (IStructuredSelection) selection;
                if (manager != null) {
                    appendToTablePopupMenu(manager, sel);
                }
            }
        });

        Menu tablePopup = fTablePopupMenuManager.createContextMenu(getTableViewer().getTable());
        getTableViewer().getTable().setMenu(tablePopup);

        tableControl.addDisposeListener((e) -> {
            internalDispose();
        });
    }

    @Override
    public void dispose() {
        if (fTableViewer != null) {
            fTableViewer.getControl().dispose();
        }
    }

    private void internalDispose() {
        if (fTablePopupMenuManager != null) {
            fTablePopupMenuManager.dispose();
        }
        super.dispose();
    }

    /**
     * Method to add commands to the context sensitive menu.
     * @param manager
     *          the menu manager
     * @param sel
     *          the current selection
     * @since 2.0
     */
    protected void appendToTablePopupMenu(@NonNull IMenuManager manager, @NonNull IStructuredSelection sel) {
    }

    /**
     * Create a column for the table. The column will have a default width set,
     * and will be resizable, moveable and sortable.
     *
     * @param name
     *            the name of the column
     * @param provider
     *            the label provider of the column
     * @param comparator
     *            the comparator associated with clicking on the column, if it
     *            is null, a string comparator on the label will be used
     * @return the column that was created
     * @since 2.0
     */
    public final <T> TableColumn createColumn(String name, ColumnLabelProvider provider, @Nullable Comparator<T> comparator) {
        TableViewerColumn col = new TableViewerColumn(fTableViewer, SWT.NONE);
        col.setLabelProvider(provider);
        final TableColumn column = col.getColumn();
        column.setWidth(DEFAULT_COL_WIDTH);
        column.setText(name);
        column.setResizable(true);
        column.setMoveable(true);
        if (comparator == null) {
            column.addSelectionListener(new ColumnSorter<>(column, new ColumnLabelComparator(provider)));
        } else {
            column.addSelectionListener(new ColumnSorter<>(column, comparator));
        }
        return column;
    }

    /**
     * Reverse the sort direction
     */
    private void flipSortDirection() {
        if (fDirection == SWT.DOWN) {
            fDirection = SWT.UP;
        } else {
            fDirection = SWT.DOWN;
        }
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