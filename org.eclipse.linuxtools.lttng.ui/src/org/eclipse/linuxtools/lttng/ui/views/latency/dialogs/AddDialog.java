/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *   Bernd Hufmann - Adapted to new messages file, fixed warnings
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.latency.dialogs;

import java.util.Vector;

import org.eclipse.linuxtools.lttng.core.latency.analyzer.EventMatcher;
import org.eclipse.linuxtools.lttng.core.util.EventsPair;
import org.eclipse.linuxtools.lttng.ui.views.latency.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * <b><u>AddDialog</u></b>
 * <p>
 * Add dialog, lets the user add custom start/end event pairs.
 * 
 * @author Philippe Sawicki
 */
public class AddDialog extends AbstractDialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The dialog's start table.
     */
    protected Table fStartTable;

    /**
     * The dialog's end table.
     */
    protected Table fEndTable;

    /**
     * The dialog's list table.
     */
    protected Table fListTable;

    /**
     * Start table columns.
     */
    protected TableColumn[] fStartColumns;

    /**
     * End table columns.
     */
    protected TableColumn[] fEndColumns;

    /**
     * List table columns.
     */
    protected TableColumn[] fListColumns;

    /**
     * Start table column names (header titles).
     */
    protected static final String[] START_COLUMN_NAMES = { "", Messages.LatencyView_Dialogs_AddEvents_Columns_Start }; //$NON-NLS-1$

    /**
     * End table column names (header titles).
     */
    protected static final String[] END_COLUMN_NAMES = { "", Messages.LatencyView_Dialogs_AddEvents_Columns_End }; //$NON-NLS-1$

    /**
     * List table column names (header titles).
     */
    protected static final String[] LIST_COLUMN_NAMES = {
            "#", //$NON-NLS-1$
            Messages.LatencyView_Dialogs_AddEvents_Columns_List_Trigger,
            Messages.LatencyView_Dialogs_AddEvents_Columns_List_End };

    /**
     * Column widths.
     */
    protected static final int[] COLUMN_WIDTHS = { 25, 250, 250 };

    /**
     * Possible event types.
     */
    protected Vector<String> fEventTypes = new Vector<String>();

    /**
     * Start event types.
     */
    protected Vector<String> fEventStartTypes;

    /**
     * End event types.
     */
    protected Vector<String> fEventEndTypes;

    /**
     * Selected start type.
     */
    protected String fStartType;

    /**
     * Selected end type.
     */
    protected String fEndType;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     * @param parentShell
     *            The parent shell.
     * @param title
     *            The dialog's window title.
     * @param message
     *            The dialog's window message.
     */
    public AddDialog(Shell parentShell, String title, String message) {
        super(parentShell, title, message);

        // Get the possible events from the list
        fEventTypes = EventMatcher.getInstance().getTypeList();

        // Get the list of start and end types from the EventMatcher
        EventsPair pair = getMatchPairs();
        fEventStartTypes = pair.getFirst();
        fEventEndTypes = pair.getSecond();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Creates the start table's columns (i.e. the table header).
     */
    protected void createStartColumns() {
        fStartColumns = new TableColumn[START_COLUMN_NAMES.length];
        for (int i = 0; i < START_COLUMN_NAMES.length; i++) {
            fStartColumns[i] = new TableColumn(fStartTable, SWT.LEFT);
            fStartColumns[i].setText(START_COLUMN_NAMES[i]);
            fStartColumns[i].setWidth(COLUMN_WIDTHS[i]);
        }
    }

    /**
     * Creates the end table's columns (i.e. the table header).
     */
    protected void createEndColumns() {
        fEndColumns = new TableColumn[END_COLUMN_NAMES.length];
        for (int i = 0; i < END_COLUMN_NAMES.length; i++) {
            fEndColumns[i] = new TableColumn(fEndTable, SWT.LEFT);
            fEndColumns[i].setText(END_COLUMN_NAMES[i]);
            fEndColumns[i].setWidth(COLUMN_WIDTHS[i]);
        }
    }

    /**
     * Creates the list table's columns (i.e. the table header).
     */
    protected void createListColumns() {
        fListColumns = new TableColumn[LIST_COLUMN_NAMES.length];
        for (int i = 0; i < LIST_COLUMN_NAMES.length; i++) {
            fListColumns[i] = new TableColumn(fListTable, SWT.LEFT);
            fListColumns[i].setText(LIST_COLUMN_NAMES[i]);
            fListColumns[i].setWidth(COLUMN_WIDTHS[i]);
        }
    }

    /**
     * Creates the start column list.
     * @param parent
     *            The parent composite.
     */
    protected void createStartColumn(Composite parent) {
        final int style = SWT.SINGLE | SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fStartTable = new Table(parent, style);
        fStartTable.setLayoutData(layoutData);

        // Some cosmetic enhancements
        fStartTable.setHeaderVisible(true);
        fStartTable.setLinesVisible(true);

        createStartColumns();

        for (int i = 0; i < fEventTypes.size(); i++) {
            TableItem item = new TableItem(fStartTable, SWT.RIGHT);

            String[] columns = { fEventTypes.get(i), fEventTypes.get(i) };

            item.setText(columns);
        }

        fStartTable.setItemCount(fEventTypes.size());

        fStartTable.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    TableItem[] items = fStartTable.getItems();
                    for (TableItem item : items) {
                        if (item != event.item) {
                            item.setChecked(false);
                        }
                    }
                }
            }
        });
    }

    /**
     * Creates the end column list.
     * @param parent
     *            The parent composite.
     */
    protected void createEndColumn(Composite parent) {
        final int style = SWT.SINGLE | SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fEndTable = new Table(parent, style);
        fEndTable.setLayoutData(layoutData);

        // Some cosmetic enhancements
        fEndTable.setHeaderVisible(true);
        fEndTable.setLinesVisible(true);

        createEndColumns();

        for (int i = 0; i < fEventTypes.size(); i++) {
            TableItem item = new TableItem(fEndTable, SWT.RIGHT);

            String[] columns = { fEventTypes.get(i), fEventTypes.get(i) };

            item.setText(columns);
        }

        fEndTable.setItemCount(fEventTypes.size());

        fEndTable.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    TableItem[] items = fEndTable.getItems();
                    for (TableItem item : items) {
                        if (item != event.item) {
                            item.setChecked(false);
                        }
                    }
                }
            }
        });
    }

    /**
     * Creates the list column for already existing event pairs.
     * @param parent
     *            The parent composite.
     */
    protected void createListColumn(Composite parent) {
        final int style = SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL;
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.horizontalSpan = 2;
        fListTable = new Table(parent, style);
        fListTable.setLayoutData(layoutData);

        // Some cosmetic enhancements
        fListTable.setHeaderVisible(true);
        fListTable.setLinesVisible(true);

        createListColumns();

        for (int i = 0; i < fEventStartTypes.size(); i++) {
            TableItem item = new TableItem(fListTable, SWT.RIGHT);

            String max = String.valueOf(fEventStartTypes.size());
            String number = formatListNumber(i + 1, max.length());

            String[] columns = { number, fEventStartTypes.get(i), fEventEndTypes.get(i) };

            item.setText(columns);
        }

        fListTable.setItemCount(103);
        fListTable.remove(fEventTypes.size(), 103 - 1);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.dialogs.AbstractDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        GridLayout layout = new GridLayout(2, true);
        parent.setLayout(layout);

        createStartColumn(parent);
        createEndColumn(parent);
        createListColumn(parent);

        return parent;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.dialogs.AbstractDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 1;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.RIGHT;

        parent.setLayoutData(gridData);

        // Create the "Add" button
        Button addButton = createButton(parent, ADD, Messages.LatencyView_Dialogs_AddEvents_Buttons_Add, false);
        addButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (isValidInput()) {
                    // Add the event pair to the EventMatcher and save the pairs
                    EventMatcher.getInstance().addMatch(fStartType, fEndType);
                    fEventStartTypes.add(fStartType);
                    fEventEndTypes.add(fEndType);
                    saveMatchPairs(fEventStartTypes, fEventEndTypes);

                    EventsPair pairs = EventMatcher.getInstance().getEvents();
                    fEventStartTypes = pairs.getFirst();
                    fEventEndTypes = pairs.getSecond();

                    fListTable.removeAll();

                    for (int i = 0; i < fEventStartTypes.size(); i++) {
                        TableItem item = new TableItem(fListTable, SWT.RIGHT);

                        String max = String.valueOf(fEventStartTypes.size());
                        String number = formatListNumber(i + 1, max.length());

                        String[] columns = { number, fEventStartTypes.get(i), fEventEndTypes.get(i) };

                        item.setText(columns);
                    }

                    saveMatchPairs(fEventStartTypes, fEventEndTypes);
                }

                fRedrawGraphs = true;
            }
        });

        // Create the "Close" button
        Button closeButton = createButton(parent, CANCEL, Messages.LatencyView_Dialogs_AddEvents_Buttons_Close, false);
        closeButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setReturnCode(CANCEL);

                if (fRedrawGraphs == true)
                    redrawGraphs();

                close();
            }
        });
    }

    /**
     * Validate the list before adding event pairs.
     * @return "true" if the input is valid, "false" otherwise.
     */
    protected boolean isValidInput() {
        // Remove the previous error message
        setErrorMessage(null);

        boolean valid = true;

        // Check if an item from the start list is selected
        TableItem[] items = fStartTable.getItems();
        fStartType = null;
        boolean startHasSelectedItem = false;
        for (int i = 0; i < items.length && !startHasSelectedItem; i++) {
            if (items[i].getChecked() == true) {
                fStartType = items[i].getText();
                startHasSelectedItem = true;
            }
        }

        // Check if an item from the end list is selected
        items = fEndTable.getItems();
        fEndType = null;
        boolean endHasSelectedItem = false;
        for (int i = 0; i < items.length && !endHasSelectedItem; i++) {
            if (items[i].getChecked() == true) {
                fEndType = items[i].getText();
                endHasSelectedItem = true;
            }
        }

        // Print error message if needed.
        if (!startHasSelectedItem && !endHasSelectedItem) {
            setErrorMessage(Messages.LatencyView_Dialogs_AddEvents_Errors_NoSelection);
            valid = false;
        } else if (!startHasSelectedItem) {
            setErrorMessage(Messages.LatencyView_Dialogs_AddEvents_Errors_StartNotSelected);
            valid = false;
        } else if (!endHasSelectedItem) {
            setErrorMessage(Messages.LatencyView_Dialogs_AddEvents_Errors_EndNotSelected);
            valid = false;
        }

        // Check if the same item is selected in both lists
        if (startHasSelectedItem && endHasSelectedItem) {
            if (fStartType.equalsIgnoreCase(fEndType)) {
                setErrorMessage(Messages.LatencyView_Dialogs_AddEvents_Errors_SameSelected);
                valid = false;
            }
        }

        // Check if the selected item is already in the list
        if (startHasSelectedItem && endHasSelectedItem) {
            EventsPair pairs = getMatchPairs();
            Vector<String> startEvents = pairs.getFirst();
            Vector<String> endEvents = pairs.getSecond();

            boolean startAlreadyUsed = false;
            boolean endAlreadyUsed = false;
            boolean startAsEndAlreadyUsed = false;
            boolean endAsStartAlreadyUsed = false;

            if (startEvents.contains(fStartType)) {
                startAlreadyUsed = true;
            }
            if (endEvents.contains(fEndType)) {
                endAlreadyUsed = true;
            }
            if (startEvents.contains(fEndType)) {
                endAsStartAlreadyUsed = true;
            }
            if (endEvents.contains(fStartType)) {
                startAsEndAlreadyUsed = true;
            }

            if (startAlreadyUsed && endAlreadyUsed) {
                setErrorMessage(Messages.LatencyView_Dialogs_AddEvents_Errors_AlreadyMatched);
                valid = false;
            } else if (startAlreadyUsed) {
                setErrorMessage(Messages.LatencyView_Dialogs_AddEvents_Errors_StartAlreadyMatched);
                valid = false;
            } else if (endAlreadyUsed) {
                setErrorMessage(Messages.LatencyView_Dialogs_AddEvents_Errors_EndAlreadyMatched);
                valid = false;
            }

            if (startAsEndAlreadyUsed) {
                setErrorMessage(Messages.LatencyView_Dialogs_AddEvents_Errors_StartAsEnd);
                valid = false;
            }
            if (endAsStartAlreadyUsed) {
                setErrorMessage(Messages.LatencyView_Dialogs_AddEvents_Errors_EndAsStart);
                valid = false;
            }
        }

        return valid;
    }
}