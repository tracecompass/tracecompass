/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Francois Chouinard - Refactoring, slider support, bug fixing
 *   Patrick Tasse - Improvements and bug fixing
 *   Xavier Raynaud - Improvements
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.virtualtable;

import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>TmfVirtualTable</u></b>
 * <p>
 * TmfVirtualTable allows for the tabular display of arbitrarily large data sets
 * (well, up to Integer.MAX_VALUE or ~2G rows).
 *
 * It is essentially a Composite of Table and Slider, where the number of rows
 * in the table is set to fill the table display area. The slider is rank-based.
 *
 * It differs from Table with the VIRTUAL style flag where an empty entry is
 * created for each virtual row. This does not scale well for very large data sets.
 *
 * Styles:
 * H_SCROLL, V_SCROLL, SINGLE, MULTI, CHECK, FULL_SELECTION, HIDE_SELECTION, NO_SCROLL
 * @author Matthew Khouzam, Francois Chouinard, Patrick Tasse, Xavier Raynaud
 * @version $Revision: 1.0
 */
public class TmfVirtualTable extends Composite {

    // The table
    private Table   fTable;
    private int     fTableRows         = 0;      // Number of table rows
    private int     fFullyVisibleRows  = 0;      // Number of fully visible table rows
    private int     fFrozenRowCount    = 0;      // Number of frozen table rows at top of table

    private int     fTableTopEventRank = 0;      // Global rank of the first entry displayed
    private int     fSelectedEventRank = -1;     // Global rank of the selected event
    private int     fSelectedBeginRank = -1;     // Global rank of the selected begin event
    private boolean fPendingSelection  = false;  // Pending selection update

    private int     fTableItemCount    = 0;

    // The slider
    private Slider fSlider;

    private int fLinuxItemHeight = 0;            // Calculated item height for Linux workaround
    private TooltipProvider tooltipProvider = null;
    private IDoubleClickListener doubleClickListener = null;

    private boolean fResetTopIndex = false;      // Flag to trigger reset of top index
    private ControlAdapter fResizeListener;      // Resize listener to update visible rows

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param parent
     *            The parent composite object
     * @param style
     *            The style to use
     */
    public TmfVirtualTable(Composite parent, int style) {
        super(parent, style & (~SWT.H_SCROLL) & (~SWT.V_SCROLL) & (~SWT.SINGLE) & (~SWT.MULTI) & (~SWT.FULL_SELECTION) & (~SWT.HIDE_SELECTION) & (~SWT.CHECK));

        // Create the controls
        createTable(style & (SWT.H_SCROLL | SWT.SINGLE | SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.CHECK));
        createSlider(style & SWT.V_SCROLL);

        // Prevent the slider from being traversed
        setTabList(new Control[] { fTable });

        // Set the layout
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing   = 0;
        gridLayout.marginWidth  = 0;
        gridLayout.marginHeight = 0;
        setLayout(gridLayout);

        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fTable.setLayoutData(tableGridData);

        GridData sliderGridData = new GridData(SWT.FILL, SWT.FILL, false, true);
        fSlider.setLayoutData(sliderGridData);

        // Add the listeners
        fTable.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent event) {
                if (fTableItemCount <= fFullyVisibleRows) {
                    return;
                }
                fTableTopEventRank -= event.count;
                if (fTableTopEventRank < 0) {
                    fTableTopEventRank = 0;
                }
                int latestFirstRowOffset = fTableItemCount - fFullyVisibleRows;
                if (fTableTopEventRank > latestFirstRowOffset) {
                    fTableTopEventRank = latestFirstRowOffset;
                }

                fSlider.setSelection(fTableTopEventRank);
                refreshTable();
            }
        });

        fTable.addListener(SWT.MouseWheel, new Listener() {
            // disable mouse scroll of horizontal scroll bar
            @Override
            public void handleEvent(Event event) {
                event.doit = false;
            }
        });

        fResizeListener = new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent event) {
                int tableHeight = Math.max(0, fTable.getClientArea().height - fTable.getHeaderHeight());
                fFullyVisibleRows = tableHeight / getItemHeight();
                if (fTableItemCount > 0) {
                    fSlider.setThumb(Math.max(1, Math.min(fTableRows, fFullyVisibleRows)));
                }
            }
        };
        fTable.addControlListener(fResizeListener);

        // Implement a "fake" tooltip
        final String TOOLTIP_DATA_KEY = "_TABLEITEM"; //$NON-NLS-1$
        final Listener labelListener = new Listener() {
            @Override
            public void handleEvent (Event event) {
                Label label = (Label) event.widget;
                Shell shell = label.getShell();
                switch (event.type) {
                case SWT.MouseDown:
                    Event e = new Event();
                    e.item = (TableItem) label.getData(TOOLTIP_DATA_KEY);
                    // Assuming table is single select, set the selection as if
                    // the mouse down event went through to the table
                    fTable.setSelection(new TableItem [] {(TableItem) e.item});
                    fTable.notifyListeners(SWT.Selection, e);
                    shell.dispose();
                    fTable.setFocus();
                    break;
                case SWT.MouseExit:
                case SWT.MouseWheel:
                    shell.dispose();
                    break;
                default:
                    break;
                }
            }
        };

        Listener tableListener = new Listener() {
            Shell tip = null;
            Label label = null;
            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Dispose:
                case SWT.KeyDown:
                case SWT.MouseMove: {
                    if (tip == null) {
                        break;
                    }
                    tip.dispose();
                    tip = null;
                    label = null;
                    break;
                }
                case SWT.MouseHover: {
                    TableItem item = fTable.getItem(new Point(event.x, event.y));
                    if (item != null) {
                        for (int i = 0; i < fTable.getColumnCount(); i++) {
                            Rectangle bounds = item.getBounds(i);
                            if (bounds.contains(event.x, event.y)) {
                                if (tip != null && !tip.isDisposed()) {
                                    tip.dispose();
                                }
                                if (tooltipProvider == null) {
                                    return;
                                }
                                String tooltipText = tooltipProvider.getTooltip(i, item.getData());
                                if (tooltipText == null) {
                                    return;
                                }
                                tip = new Shell(fTable.getShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
                                tip.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                                FillLayout layout = new FillLayout();
                                layout.marginWidth = 2;
                                tip.setLayout(layout);
                                label = new Label(tip, SWT.WRAP);
                                label.setForeground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                                label.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                                label.setData(TOOLTIP_DATA_KEY, item);
                                label.setText(tooltipText);

                                label.addListener(SWT.MouseExit, labelListener);
                                label.addListener(SWT.MouseDown, labelListener);
                                label.addListener(SWT.MouseWheel, labelListener);
                                Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                                Point pt = fTable.toDisplay(bounds.x, bounds.y);
                                tip.setBounds(pt.x, pt.y, size.x, size.y);
                                tip.setVisible(true);

                                // Item found, leave loop.
                                break;
                            }
                        }
                    }
                    break;
                }
                default:
                    break;
                }
            }
        };
        fTable.addListener(SWT.Dispose, tableListener);
        fTable.addListener(SWT.KeyDown, tableListener);
        fTable.addListener(SWT.MouseMove, tableListener);
        fTable.addListener(SWT.MouseHover, tableListener);
        addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent event) {
                resize();
                if (fTableItemCount > 0) {
                    fSlider.setThumb(Math.max(1, Math.min(fTableRows, fFullyVisibleRows)));
                }
            }
        });

        // And display
        refresh();
    }

    // ------------------------------------------------------------------------
    // Table handling
    // ------------------------------------------------------------------------

    /**
     * Create the table and add listeners
     * @param style int can be H_SCROLL, SINGLE, MULTI, FULL_SELECTION, HIDE_SELECTION, CHECK
     */
    private void createTable(int style) {
        fTable = new Table(this, style | SWT.NO_SCROLL);

        fTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (event.item == null) {
                    // Override table selection from Select All action
                    refreshSelection();
                }
            }
        });

        fTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                handleTableMouseEvent(e);
            }
        });

        fTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                handleTableKeyEvent(event);
            }
        });

        fTable.addListener(
                SWT.MouseDoubleClick, new Listener() {
                    @Override
                    public void handleEvent(Event event) {
                        if (doubleClickListener != null) {
                            TableItem item = fTable.getItem(new Point (event.x, event.y));
                            if (item != null) {
                                for (int i = 0; i < fTable.getColumnCount(); i++){
                                    Rectangle bounds = item.getBounds(i);
                                    if (bounds.contains(event.x, event.y)){
                                        doubleClickListener.handleDoubleClick(TmfVirtualTable.this, item, i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                );

        /*
         * Feature in Windows. When a partially visible table item is selected,
         * after ~500 ms the top index is changed to ensure the selected item is
         * fully visible. This leaves a blank space at the bottom of the virtual
         * table. The workaround is to reset the top index to 0 if it is not 0.
         * Also reset the top index to 0 if indicated by the flag that was set
         * at table selection of a partially visible table item.
         */
        fTable.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                if (fTable.getTopIndex() != 0 || fResetTopIndex) {
                    fTable.setTopIndex(0);
                }
                fResetTopIndex = false;
            }
        });
    }

    /**
     * Handle mouse-based selection in table.
     *
     * @param event the mouse event
     */
    private void handleTableMouseEvent(MouseEvent event) {
        TableItem item = fTable.getItem(new Point(event.x, event.y));
        if (item == null) {
            return;
        }
        int selectedRow = indexOf(item);
        if (event.button == 1 || (event.button == 3 &&
                (selectedRow < Math.min(fSelectedBeginRank, fSelectedEventRank) ||
                        selectedRow > Math.max(fSelectedBeginRank, fSelectedEventRank)))) {
            if (selectedRow >= 0) {
                fSelectedEventRank = selectedRow;
            } else {
                fSelectedEventRank = -1;
            }
            if ((event.stateMask & SWT.SHIFT) == 0 || (fTable.getStyle() & SWT.MULTI) == 0 || fSelectedBeginRank == -1) {
                fSelectedBeginRank = fSelectedEventRank;
            }
        }
        refreshSelection();

        /*
         * Feature in Linux. When a partially visible table item is selected,
         * the origin is changed to ensure the selected item is fully visible.
         * This makes the first row partially visible. The solution is to force
         * reset the origin by setting the top index to 0. This should happen
         * only once at the next redraw by the paint listener.
         */
        if (selectedRow >= fFullyVisibleRows) {
            fResetTopIndex = true;
        }
    }

    /**
     * Handle key-based navigation in table.
     *
     * @param event the key event
     */
    private void handleTableKeyEvent(KeyEvent event) {

        int lastEventRank        = fTableItemCount - 1;
        int lastPageTopEntryRank = Math.max(0, fTableItemCount - fFullyVisibleRows);

        int previousSelectedEventRank = fSelectedEventRank;
        int previousSelectedBeginRank = fSelectedBeginRank;
        boolean needsRefresh = false;

        // In all case, perform the following steps:
        // - Update the selected entry rank (within valid range)
        // - Update the selected row
        // - Update the page's top entry if necessary (which also adjusts the selected row)
        // - If the top displayed entry was changed, table refresh is needed
        switch (event.keyCode) {

        case SWT.ARROW_DOWN: {
            event.doit = false;
            if (fSelectedEventRank < lastEventRank) {
                fSelectedEventRank++;
                int selectedRow = fSelectedEventRank - fTableTopEventRank;
                if (selectedRow == fFullyVisibleRows) {
                    fTableTopEventRank++;
                    needsRefresh = true;
                } else if (selectedRow < fFrozenRowCount || selectedRow > fFullyVisibleRows) {
                    fTableTopEventRank = Math.max(0, Math.min(fSelectedEventRank - fFrozenRowCount, lastPageTopEntryRank));
                    needsRefresh = true;
                }
            }
            break;
        }

        case SWT.ARROW_UP: {
            event.doit = false;
            if (fSelectedEventRank > 0) {
                fSelectedEventRank--;
                int selectedRow = fSelectedEventRank - fTableTopEventRank;
                if (selectedRow == fFrozenRowCount - 1 && fTableTopEventRank > 0) {
                    fTableTopEventRank--;
                    needsRefresh = true;
                } else if (selectedRow < fFrozenRowCount || selectedRow > fFullyVisibleRows) {
                    fTableTopEventRank = Math.max(0, Math.min(fSelectedEventRank - fFrozenRowCount, lastPageTopEntryRank));
                    needsRefresh = true;
                }
            }
            break;
        }

        case SWT.END: {
            event.doit = false;
            fTableTopEventRank = lastPageTopEntryRank;
            fSelectedEventRank = lastEventRank;
            needsRefresh = true;
            break;
        }

        case SWT.HOME: {
            event.doit = false;
            fSelectedEventRank = fFrozenRowCount;
            fTableTopEventRank = 0;
            needsRefresh = true;
            break;
        }

        case SWT.PAGE_DOWN: {
            event.doit = false;
            if (fSelectedEventRank < lastEventRank) {
                fSelectedEventRank += fFullyVisibleRows;
                if (fSelectedEventRank > lastEventRank) {
                    fSelectedEventRank = lastEventRank;
                }
                int selectedRow = fSelectedEventRank - fTableTopEventRank;
                if (selectedRow > fFullyVisibleRows + fFrozenRowCount - 1 && selectedRow < 2 * fFullyVisibleRows) {
                    fTableTopEventRank += fFullyVisibleRows;
                    if (fTableTopEventRank > lastPageTopEntryRank) {
                        fTableTopEventRank = lastPageTopEntryRank;
                    }
                    needsRefresh = true;
                } else if (selectedRow < fFrozenRowCount || selectedRow >= 2 * fFullyVisibleRows) {
                    fTableTopEventRank = Math.max(0, Math.min(fSelectedEventRank - fFrozenRowCount, lastPageTopEntryRank));
                    needsRefresh = true;
                }
            }
            break;
        }

        case SWT.PAGE_UP: {
            event.doit = false;
            if (fSelectedEventRank > 0) {
                fSelectedEventRank -= fFullyVisibleRows;
                if (fSelectedEventRank < fFrozenRowCount) {
                    fSelectedEventRank = fFrozenRowCount;
                }
                int selectedRow = fSelectedEventRank - fTableTopEventRank;
                if (selectedRow < fFrozenRowCount && selectedRow > -fFullyVisibleRows) {
                    fTableTopEventRank -= fFullyVisibleRows;
                    if (fTableTopEventRank < 0) {
                        fTableTopEventRank = 0;
                    }
                    needsRefresh = true;
                } else if (selectedRow <= -fFullyVisibleRows || selectedRow >= fFullyVisibleRows) {
                    fTableTopEventRank = Math.max(0, Math.min(fSelectedEventRank - fFrozenRowCount, lastPageTopEntryRank));
                    needsRefresh = true;
                }
            }
            break;
        }
        default: {
            return;
        }
        }

        if ((event.stateMask & SWT.SHIFT) == 0 || (fTable.getStyle() & SWT.MULTI) == 0 || fSelectedBeginRank == -1) {
            fSelectedBeginRank = fSelectedEventRank;
        }

        boolean done = true;
        if (needsRefresh) {
            done = refreshTable(); // false if table items not updated yet in this thread
        } else {
            refreshSelection();
        }

        if (fFullyVisibleRows < fTableItemCount) {
            fSlider.setSelection(fTableTopEventRank);
        }

        if (fSelectedEventRank != previousSelectedEventRank || fSelectedBeginRank != previousSelectedBeginRank) {
            if (done) {
                Event e = new Event();
                e.item = fTable.getItem(fSelectedEventRank - fTableTopEventRank);
                fTable.notifyListeners(SWT.Selection, e);
            } else {
                fPendingSelection = true;
            }
        }
    }

    /**
     * Method setDataItem.
     * @param index int
     * @param item TableItem
     * @return boolean
     */
    private boolean setDataItem(int index, TableItem item) {
        if (index != -1) {
            Event event = new Event();
            event.item  = item;
            if (index < fFrozenRowCount) {
                event.index = index;
            } else {
                event.index = index + fTableTopEventRank;
            }
            event.doit  = true;
            fTable.notifyListeners(SWT.SetData, event);
            return event.doit; // false if table item not updated yet in this thread
        }
        return true;
    }

    // ------------------------------------------------------------------------
    // Slider handling
    // ------------------------------------------------------------------------

    /**
     * Method createSlider.
     * @param style int
     */
    private void createSlider(int style) {
        fSlider = new Slider(this, SWT.VERTICAL | SWT.NO_FOCUS);
        fSlider.setMinimum(0);
        fSlider.setMaximum(0);
        if ((style & SWT.V_SCROLL) == 0) {
            fSlider.setVisible(false);
        }

        fSlider.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                switch (event.detail) {
                case SWT.ARROW_DOWN:
                case SWT.ARROW_UP:
                case SWT.END:
                case SWT.HOME:
                case SWT.PAGE_DOWN:
                case SWT.PAGE_UP: {
                    fTableTopEventRank = fSlider.getSelection();
                    refreshTable();
                    break;
                }
                // Not handled because of bug on Linux described below.
                case SWT.NONE:
                default:
                    break;
                }
            }
        });

        /*
         * In Linux, the selection event above has event.detail set to SWT.NONE
         * instead of SWT.DRAG during dragging of the thumb. To prevent refresh
         * overflow, only update the table when the mouse button is released.
         */
        fSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                fTableTopEventRank = fSlider.getSelection();
                refreshTable();
            }
        });

    }

    // ------------------------------------------------------------------------
    // Simulated Table API
    // ------------------------------------------------------------------------

    /**
     * Method setHeaderVisible.
     * @param b boolean
     */
    public void setHeaderVisible(boolean b) {
        fTable.setHeaderVisible(b);
    }

    /**
     * Method setLinesVisible.
     * @param b boolean
     */
    public void setLinesVisible(boolean b) {
        fTable.setLinesVisible(b);
    }

    /**
     * Returns an array of <code>TableItem</code>s that are currently selected
     * in the receiver. The order of the items is unspecified. An empty array
     * indicates that no items are selected.
     * <p>
     * Note: This array only contains the visible selected items in the virtual
     * table. To get information about the full selection range, use
     * {@link #getSelectionIndices()}.
     * </p>
     *
     * @return an array representing the selection
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public TableItem[] getSelection() {
        return fTable.getSelection();
    }

    /**
     * Method addListener.
     * @param eventType int
     * @param listener Listener
     */
    @Override
    public void addListener(int eventType, Listener listener) {
        fTable.addListener(eventType, listener);
    }

    /**
     * Method addKeyListener.
     * @param listener KeyListener
     */
    @Override
    public void addKeyListener(KeyListener listener) {
        fTable.addKeyListener(listener);
    }

    /**
     * Method addMouseListener.
     * @param listener MouseListener
     */
    @Override
    public void addMouseListener(MouseListener listener) {
        fTable.addMouseListener(listener);
    }

    /**
     * Method addSelectionListener.
     * @param listener SelectionListener
     */
    public void addSelectionListener(SelectionListener listener) {
        fTable.addSelectionListener(listener);
    }

    /**
     * Method setMenu sets the menu
     * @param menu Menu the menu
     */
    @Override
    public void setMenu(Menu menu) {
        fTable.setMenu(menu);
    }

    /**
     * Gets the menu of this table
     * @return a Menu
     */
    @Override
    public Menu getMenu() {
        return fTable.getMenu();
    }

    /**
     * Method clearAll empties a table.
     */
    public void clearAll() {
        setItemCount(0);
    }

    /**
     * Method setItemCount
     * @param nbItems int the number of items in the table
     *
     */
    public void setItemCount(int nbItems) {
        final int nb = Math.max(0, nbItems);

        if (nb != fTableItemCount) {
            fTableItemCount = nb;
            fTable.remove(fTableItemCount, fTable.getItemCount() - 1);
            fSlider.setMaximum(nb);
            resize();
            int tableHeight = Math.max(0, fTable.getClientArea().height - fTable.getHeaderHeight());
            fFullyVisibleRows = tableHeight / getItemHeight();
            if (fTableItemCount > 0) {
                fSlider.setThumb(Math.max(1, Math.min(fTableRows, fFullyVisibleRows)));
            }
        }
    }

    /**
     * Method getItemCount.
     * @return int the number of items in the table
     */
    public int getItemCount() {
        return fTableItemCount;
    }

    /**
     * Method getItemHeight.
     * @return int the height of a table item in pixels. (may vary from one os to another)
     */
    public int getItemHeight() {
        /*
         * Bug in Linux.  The method getItemHeight doesn't always return the correct value.
         */
        if (fLinuxItemHeight >= 0 && System.getProperty("os.name").contains("Linux")) { //$NON-NLS-1$ //$NON-NLS-2$
            if (fLinuxItemHeight != 0) {
                return fLinuxItemHeight;
            }
            if (fTable.getItemCount() > 1) {
                int itemHeight = fTable.getItem(1).getBounds().y - fTable.getItem(0).getBounds().y;
                if (itemHeight > 0) {
                    fLinuxItemHeight = itemHeight;
                    return fLinuxItemHeight;
                }
            }
        } else {
            fLinuxItemHeight = -1; // Not Linux, don't perform os.name check anymore
        }
        return fTable.getItemHeight();
    }

    /**
     * Method getHeaderHeight.
     * @return int get the height of the header in pixels.
     */
    public int getHeaderHeight() {
        return fTable.getHeaderHeight();
    }

    /**
     * Method getTopIndex.
     * @return int get the first data item index, if you have a header it is offset.
     */
    public int getTopIndex() {
        return fTableTopEventRank + fFrozenRowCount;
    }

    /**
     * Method setTopIndex.
     * @param index int suggested top index for the table.
     */
    public void setTopIndex(int index) {
        if (fTableItemCount > 0) {
            int i = Math.min(index, fTableItemCount - 1);
            i = Math.max(i, fFrozenRowCount);

            fTableTopEventRank = i - fFrozenRowCount;
            if (fFullyVisibleRows < fTableItemCount) {
                fSlider.setSelection(fTableTopEventRank);
            }

            refreshTable();
        }
    }

    /**
     * Method indexOf. Return the index of a table item
     * @param ti TableItem the table item to search for in the table
     * @return int the index of the first match. (there should only be one match)
     */
    public int indexOf(TableItem ti) {
        int index = fTable.indexOf(ti);
        if (index < fFrozenRowCount) {
            return index;
        }
        return (index - fFrozenRowCount) + getTopIndex();
    }

    /**
     * Method getColumns.
     * @return TableColumn[] the table columns
     */
    public TableColumn[] getColumns() {
        return fTable.getColumns();
    }

    /**
     * Method getItem.
     * @param point Point the coordinates in the table
     * @return TableItem the corresponding table item
     */
    public TableItem getItem(Point point) {
        return fTable.getItem(point);
    }

    /**
     * Method resize.
     */
    private void resize() {
        // Compute the numbers of rows that fit the new area
        int tableHeight = Math.max(0, getSize().y - fTable.getHeaderHeight());
        int itemHeight = getItemHeight();
        fTableRows = Math.min((tableHeight + itemHeight - 1) / itemHeight, fTableItemCount);

        if (fTableTopEventRank + fFullyVisibleRows > fTableItemCount) {
            // If we are at the end, get elements before to populate
            fTableTopEventRank = Math.max(0, fTableItemCount - fFullyVisibleRows);
            refreshTable();
        } else if (fTableRows > fTable.getItemCount() || fTableItemCount < fTable.getItemCount()) {
            // Only refresh if new table items are needed or if table items need to be deleted
            refreshTable();
        }

    }

    // ------------------------------------------------------------------------
    // Controls interactions
    // ------------------------------------------------------------------------

    /**
     * Method setFocus.
     * @return boolean is this visible?
     */
    @Override
    public boolean setFocus() {
        boolean isVisible = isVisible();
        if (isVisible) {
            fTable.setFocus();
        }
        return isVisible;
    }

    /**
     * Method refresh.
     */
    public void refresh() {
        boolean done = refreshTable();
        if (!done) {
            return;
        }
        if (fPendingSelection) {
            fPendingSelection = false;
            TableItem item = null;
            if (fSelectedEventRank >= 0 && fSelectedEventRank < fFrozenRowCount) {
                item = fTable.getItem(fSelectedEventRank);
            } else if (fSelectedEventRank >= fTableTopEventRank + fFrozenRowCount && fSelectedEventRank - fTableTopEventRank < fTable.getItemCount()) {
                item = fTable.getItem(fSelectedEventRank - fTableTopEventRank);
            }
            if (item != null) {
                Event e = new Event();
                e.item = item;
                fTable.notifyListeners(SWT.Selection, e);
            }
        }
    }

    /**
     * Method setColumnHeaders.
     * @param columnData ColumnData[] the columndata array.
     */
    public void setColumnHeaders(ColumnData columnData[]) {
        for (int i = 0; i < columnData.length; i++) {
            TableColumn column = new TableColumn(fTable, columnData[i].alignment, i);
            column.setText(columnData[i].header);
            /*
             * In Linux the table does not receive a control resized event when
             * a table column resize causes the horizontal scroll bar to become
             * visible or invisible, so a resize listener must be added to every
             * table column to properly update the number of fully visible rows.
             */
            column.addControlListener(fResizeListener);
            if (columnData[i].width > 0) {
                column.setWidth(columnData[i].width);
            } else {
                column.pack();
            }
        }
    }

    /**
     * Method removeAll.
     * @return int 0 the number of elements in the table
     */
    public int removeAll() {
        setItemCount(0);
        fSlider.setMaximum(0);
        fTable.removeAll();
        fSelectedEventRank = -1;
        fSelectedBeginRank = fSelectedEventRank;
        return 0;
    }

    /**
     * Method refreshTable.
     * @return true if all table items have been refreshed, false otherwise
     */
    private boolean refreshTable() {
        boolean done = true;
        for (int i = 0; i < fTableRows; i++) {
            if (i + fTableTopEventRank < fTableItemCount) {
                TableItem tableItem;
                if (i < fTable.getItemCount()) {
                    tableItem = fTable.getItem(i);
                } else {
                    tableItem = new TableItem(fTable, SWT.NONE);
                }
                done &= setDataItem(i, tableItem); // false if table item not updated yet in this thread
            } else {
                if (fTable.getItemCount() > fTableItemCount - fTableTopEventRank) {
                    fTable.remove(fTableItemCount - fTableTopEventRank);
                }
            }
        }
        if (done) {
            refreshSelection();
        } else {
            fTable.deselectAll();
        }
        return done;
    }

    private void refreshSelection() {
        int lastRowOffset = fTableTopEventRank + fTableRows - 1;
        int startRank = Math.min(fSelectedBeginRank, fSelectedEventRank);
        int endRank = Math.max(fSelectedBeginRank, fSelectedEventRank);
        int start = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        if (startRank < fFrozenRowCount) {
            start = startRank;
        } else if (startRank < fTableTopEventRank + fFrozenRowCount) {
            start = fFrozenRowCount;
        } else if (startRank <= lastRowOffset) {
            start = startRank - fTableTopEventRank;
        }
        if (endRank < fFrozenRowCount) {
            end = endRank;
        } else if (endRank < fTableTopEventRank + fFrozenRowCount) {
            end = fFrozenRowCount - 1;
        } else if (endRank <= lastRowOffset) {
            end = endRank - fTableTopEventRank;
        } else {
            end = fTableRows - 1;
        }
        if (start <= end) {
            fTable.setSelection(start, end);
            if (startRank == fSelectedEventRank) {
                fTable.select(start);
            } else {
                fTable.select(end);
            }
        } else {
            fTable.deselectAll();
        }
    }

    /**
     * Selects the item at the given zero-relative index in the receiver.
     * The current selection is first cleared, then the new item is selected,
     * and if necessary the receiver is scrolled to make the new selection visible.
     *
     * @param index the index of the item to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(int index) {
        if (fTableItemCount > 0) {
            int i = Math.min(index, fTableItemCount - 1);
            i = Math.max(i, 0);

            fSelectedEventRank = i;
            fSelectedBeginRank = fSelectedEventRank;
            if ((i < fTableTopEventRank + fFrozenRowCount && i >= fFrozenRowCount) ||
                    (i >= fTableTopEventRank + fFullyVisibleRows)) {
                int lastPageTopEntryRank = Math.max(0, fTableItemCount - fFullyVisibleRows);
                fTableTopEventRank = Math.max(0, Math.min(lastPageTopEntryRank, i - fFrozenRowCount - fFullyVisibleRows / 2));
            }
            if (fFullyVisibleRows < fTableItemCount) {
                fSlider.setSelection(fTableTopEventRank);
            }

            refreshTable();
        }
    }

    /**
     * Returns the zero-relative index of the item which is currently
     * selected in the receiver, or -1 if no item is selected.
     *
     * @return the index of the selected item
     */
    public int getSelectionIndex() {
        return fSelectedEventRank;
    }

    /**
     * Returns an index array representing the selection range. If there is a
     * single item selected the array holds one index. If there is a selected
     * range the first item in the array is the start index of the selection and
     * the second item is the end index of the selection, which is the item most
     * recently selected. The array is empty if no items are selected.
     * <p>
     * @return the array of indices of the selected items
     * @since 2.1
     */
    public int[] getSelectionIndices() {
        if (fSelectedEventRank < 0 || fSelectedBeginRank < 0) {
            return new int[] {};
        } else if (fSelectedEventRank == fSelectedBeginRank) {
            return new int[] { fSelectedEventRank };
        }
        return new int[] { fSelectedBeginRank, fSelectedEventRank };
    }

    /**
     * Method setFrozenRowCount.
     * @param count int the number of rows to freeze from the top row
     */
    public void setFrozenRowCount(int count) {
        fFrozenRowCount = count;
        refreshTable();
    }

    /**
     * Method createTableEditor.
     * @return a TableEditor of the table
     */
    public TableEditor createTableEditor() {
        return new TableEditor(fTable);
    }

    /**
     * Method createTableEditorControl.
     * @param control Class<? extends Control>
     * @return Control
     */
    public Control createTableEditorControl(Class<? extends Control> control) {
        try {
            return control.getConstructor(Composite.class, int.class).newInstance(new Object[] {fTable, SWT.NONE});
        } catch (Exception e) {
            Activator.getDefault().logError("Error creating table editor control", e); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * @return the tooltipProvider
     */
    public TooltipProvider getTooltipProvider() {
        return tooltipProvider;
    }

    /**
     * @param tooltipProvider the tooltipProvider to set
     */
    public void setTooltipProvider(TooltipProvider tooltipProvider) {
        this.tooltipProvider = tooltipProvider;
    }

    /**
     * @return the doubleClickListener
     */
    public IDoubleClickListener getDoubleClickListener() {
        return doubleClickListener;
    }

    /**
     * @param doubleClickListener the doubleClickListener to set
     */
    public void setDoubleClickListener(IDoubleClickListener doubleClickListener) {
        this.doubleClickListener = doubleClickListener;
    }

}
