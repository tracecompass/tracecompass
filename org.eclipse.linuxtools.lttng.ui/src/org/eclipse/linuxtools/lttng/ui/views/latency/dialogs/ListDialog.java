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

import org.eclipse.jface.dialogs.MessageDialog;
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
 * <b><u>ListDialog</u></b>
 * <p>
 * List dialog, shows the list of start/end event pairs.
 * 
 * @author Philippe Sawicki
 */
public class ListDialog extends AbstractDialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The dialog's table.
     */
    protected Table fTable;

    /**
     * Start event types.
     */
    protected Vector<String> fEventStartTypes;

    /**
     * End event types.
     */
    protected Vector<String> fEventEndTypes;

    /**
     * Table columns
     */
    protected TableColumn[] fColumns;

    /**
     * Column names (header titles).
     */
    protected static final String[] COLUMN_NAMES = { "#", Messages.LatencyView_Dialogs_ListEvents_Columns_Trigger, Messages.LatencyView_Dialogs_ListEvents_Columns_End }; //$NON-NLS-1$

    /**
     * Column widths.
     */
    protected static final int[] COLUMN_WIDTHS = { 25, 250, 250 };

    /**
     * The table style.
     */
    protected int fStyle;

    // ------------------------------------------------------------------------
    // Constructor
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
    public ListDialog(Shell parentShell, String title, String message) {
        super(parentShell, title, message);

        // Set the table style
        fStyle = SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL;

        // Get the list of start and end types from the EventMatcher
        EventsPair pair = getMatchPairs();
        fEventStartTypes = pair.getFirst();
        fEventEndTypes = pair.getSecond();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /**
     * Creates the table's column (i.e. the table header).
     */
    protected void createColumns() {
        fColumns = new TableColumn[COLUMN_NAMES.length];
        for (int i = 0; i < COLUMN_NAMES.length; i++) {
            fColumns[i] = new TableColumn(fTable, SWT.LEFT);
            fColumns[i].setText(COLUMN_NAMES[i]);
            fColumns[i].setWidth(COLUMN_WIDTHS[i]);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.dialogs.AbstractDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        GridLayout layout = new GridLayout(1, true);
        parent.setLayout(layout);

        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fTable = new Table(parent, fStyle);
        fTable.setLayoutData(layoutData);

        // Some cosmetic enhancements
        fTable.setHeaderVisible(true);
        fTable.setLinesVisible(true);

        createColumns();

        for (int i = 0; i < fEventStartTypes.size(); i++) {
            TableItem item = new TableItem(fTable, SWT.RIGHT);

            String max = String.valueOf(fEventStartTypes.size());
            String number = formatListNumber(i + 1, max.length());

            String[] columns = { number, fEventStartTypes.get(i), fEventEndTypes.get(i) };

            item.setText(columns);
        }

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

        // Create the "Reset" button
        Button resetButton = createButton(parent, RESET, Messages.LatencyView_Dialogs_ListEvents_Buttons_Reset, false);
        resetButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                boolean confirmDeletion = MessageDialog.openQuestion(getShell(), Messages.LatencyView_Dialogs_ListEvents_Confirm_Title,
                        Messages.LatencyView_Dialogs_ListEvents_Confirm_Message);

                if (confirmDeletion) {
                    EventMatcher.getInstance().resetMatches();

                    fTable.removeAll();

                    Vector<String> defaultStarts = new Vector<String>();
                    Vector<String> defaultEnds = new Vector<String>();

                    defaultStarts.add(EventMatcher.PAGE_FAULT_GET_USER_ENTRY);
                    defaultEnds.add(EventMatcher.PAGE_FAULT_GET_USER_EXIT);
                    defaultStarts.add(EventMatcher.TASKLET_LOW_ENTRY);
                    defaultEnds.add(EventMatcher.TASKLET_LOW_EXIT);
                    defaultStarts.add(EventMatcher.PAGE_FAULT_ENTRY);
                    defaultEnds.add(EventMatcher.PAGE_FAULT_EXIT);
                    defaultStarts.add(EventMatcher.SYSCALL_ENTRY);
                    defaultEnds.add(EventMatcher.SYSCALL_EXIT);
                    defaultStarts.add(EventMatcher.IRQ_ENTRY);
                    defaultEnds.add(EventMatcher.IRQ_EXIT);
                    defaultStarts.add(EventMatcher.READ);
                    defaultEnds.add(EventMatcher.WRITE);
                    defaultStarts.add(EventMatcher.OPEN);
                    defaultEnds.add(EventMatcher.CLOSE);
                    defaultStarts.add(EventMatcher.BUFFER_WAIT_START);
                    defaultEnds.add(EventMatcher.BUFFER_WAIT_END);
                    defaultStarts.add(EventMatcher.START_COMMIT);
                    defaultEnds.add(EventMatcher.END_COMMIT);
                    defaultStarts.add(EventMatcher.WAIT_ON_PAGE_START);
                    defaultEnds.add(EventMatcher.WAIT_ON_PAGE_END);

                    saveMatchPairs(defaultStarts, defaultEnds);

                    for (int i = 0; i < defaultStarts.size(); i++) {
                        EventMatcher.getInstance().addMatch(defaultStarts.get(i), defaultEnds.get(i));
                    }

                    // Get the list of start and end types from the EventMatcher
                    EventsPair pair = getMatchPairs();
                    fEventStartTypes = pair.getFirst();
                    fEventEndTypes = pair.getSecond();

                    for (int i = 0; i < fEventStartTypes.size(); i++) {
                        TableItem item = new TableItem(fTable, SWT.RIGHT);

                        String max = String.valueOf(fEventStartTypes.size());
                        String number = formatListNumber(i + 1, max.length());

                        String[] columns = { number, fEventStartTypes.get(i), fEventEndTypes.get(i) };

                        item.setText(columns);
                    }

                    fTable.setItemCount(fEventStartTypes.size());

                    fRedrawGraphs = true;
                }
            }
        });

        // Create the "Close" button
        Button closeButton = createButton(parent, CANCEL, Messages.LatencyView_Dialogs_ListEvents_Buttons_Close, false);
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
}