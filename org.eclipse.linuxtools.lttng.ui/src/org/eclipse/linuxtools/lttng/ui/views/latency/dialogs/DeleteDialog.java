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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.lttng.core.latency.analyzer.EventMatcher;
import org.eclipse.linuxtools.lttng.ui.views.latency.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

/**
 * <b><u>DeleteDialog</u></b>
 * <p>
 * Remove dialog, lets the user remove start/end event pairs.
 * 
 * @author Philippe Sawicki
 */
public class DeleteDialog extends ListDialog {

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
    public DeleteDialog(Shell parentShell, String title, String message) {
        super(parentShell, title, message);

        // Set the table style
        fStyle = SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.dialogs.ListDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
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

        // Create the "Delete" button
        Button deleteButton = createButton(parent, DELETE, Messages.LatencyView_Dialogs_DeleteEvents_Buttons_Delete, false);
        deleteButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                TableItem selectedItem = fTable.getSelection()[0];
                if (selectedItem == null)
                    return;

                int[] selectedIndices = fTable.getSelectionIndices();

                String deletePairs = ""; //$NON-NLS-1$
                for (int i = 0; i < selectedIndices.length; i++) {
                    int index = selectedIndices[i];
                    deletePairs += "\t* " + fEventStartTypes.get(index) + " / " + fEventEndTypes.get(index); //$NON-NLS-1$ //$NON-NLS-2$

                    if (i < selectedIndices.length - 1) {
                        deletePairs += "\n"; //$NON-NLS-1$
                    }
                }

                boolean confirmDeletion = MessageDialog.openQuestion(getShell(), Messages.LatencyView_Dialogs_DeleteEvents_Confirm_Title,
                        Messages.LatencyView_Dialogs_DeleteEvents_Confirm_Message + "\n\n" + deletePairs); //$NON-NLS-1$

                if (confirmDeletion) {
                    // Remove the events starting from the end of the list, otherwise the TableItem elements will lose
                    // their index from the table and may trigger an exception when removing an index that is no longer
                    // valid.
                    for (int i = selectedIndices.length - 1; i >= 0; i--) {
                        int selectedIndex = selectedIndices[i];
                        EventMatcher.getInstance().removeMatch(fEventStartTypes.get(selectedIndex), fEventEndTypes.get(selectedIndex));

                        fTable.remove(selectedIndex);

                        // Update the list of events
                        fEventStartTypes.remove(selectedIndex);
                        fEventEndTypes.remove(selectedIndex);
                    }

                    // Save the events pairs in the settings file so it can be retrieved in the next session
                    saveMatchPairs(fEventStartTypes, fEventEndTypes);

                    fTable.setItemCount(fEventStartTypes.size());

                    TableItem[] newItems = fTable.getItems();
                    fTable.removeAll();
                    for (int i = 0; i < newItems.length; i++) {
                        TableItem item = new TableItem(fTable, SWT.RIGHT);

                        String max = String.valueOf(fEventStartTypes.size());
                        String number = formatListNumber(i + 1, max.length());

                        String[] columns = { number, fEventStartTypes.get(i), fEventEndTypes.get(i) };

                        item.setText(columns);
                    }

                    fRedrawGraphs = true;
                }
            }
        });

        // Create the "Close" button
        Button closeButton = createButton(parent, CANCEL, Messages.LatencyView_Dialogs_AddEvents_Buttons_Close, false);
        closeButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // Remember the user's list
                saveMatchPairs(fEventStartTypes, fEventEndTypes);

                setReturnCode(CANCEL);

                if (fRedrawGraphs == true)
                    redrawGraphs();

                close();
            }
        });
    }
}