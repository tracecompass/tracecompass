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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.linuxtools.lttng.core.latency.analyzer.EventMatcher;
import org.eclipse.linuxtools.lttng.core.util.EventsPair;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.views.latency.model.LatencyController;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * <b><u>AbstractDialog</u></b>
 * <p> 
 * Includes the main functions shared by all the different dialogs.
 * 
 * @author Philippe Sawicki
 */
public abstract class AbstractDialog extends TitleAreaDialog {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    /**
     * The dialog window title.
     */
    protected String fDialogTitle;
    /**
     * The dialog window message.
     */
    protected String fDialogMessage;

    /**
     * The code returned by the dialog when the user closes the "Add" dialog.
     */
    public static final int ADD = 53445;
    /**
     * The code returned by the dialog when the user closes the "Delete" dialog.
     */
    public static final int DELETE = ADD + 1;
    /**
     * The code returned by the dialog when the user resets the latency pair to default.
     */
    public static final int RESET = DELETE + 1;

    /**
     * String ID of the number of pairs saved in the settings file.
     */
    protected static final String LATENCY_NB_MATCH_PAIRS = "NB_LATENCY_MATCH_PAIRS"; //$NON-NLS-1$
    /**
     * String ID of the start event pairs saved in the settings file.
     */
    protected static final String LATENCY_PAIRS_START = "LATENCY_PAIRS_START"; //$NON-NLS-1$
    /**
     * String ID of the end event pairs saved in the settings file.
     */
    protected static final String LATENCY_PAIRS_END = "LATENCY_PAIRS_END"; //$NON-NLS-1$

    /**
     * Dialog settings, saves the event pairs across sessions.
     */
    protected IDialogSettings fSettings;

    /**
     * Do the graphs canvas need to be redrawn due to latency pairs changes ?
     */
    protected boolean fRedrawGraphs = false;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     * @param parentShell
     *            The parent shell.
     * @param title
     *            The dialog window's title.
     * @param message
     *            The dialog window's message.
     */
    public AbstractDialog(Shell parentShell, String title, String message) {
        super(parentShell);
        fDialogTitle = title;
        fDialogMessage = message;

        fSettings = LTTngUiPlugin.getDefault().getDialogSettings();
    }

    /**
     * Constructor
     * @param parentShell
     *            The parent shell.
     * @param title
     *            The dialog window's title.
     */
    @SuppressWarnings("nls")
    public AbstractDialog(Shell parentShell, String title) {
        this(parentShell, title, "");
    }

    /**
     * Constructor.
     * @param parentShell
     *            The parent shell.
     */
    @SuppressWarnings("nls")
    public AbstractDialog(Shell parentShell) {
        this(parentShell, "", "");
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Creates the dialog.
     * 
     * <b>Note :</b> Since there is an issue with the table's vertical scroll bar, this dialog "resize" is necessary to
     * ensure a minimal height for the window.
     */
    @Override
    public void create() {
        super.create();
        // Set the title
        setTitle(fDialogTitle);
        // Set the message
        setMessage(fDialogMessage, IMessageProvider.INFORMATION);

        // Position the dialog at the center of the screen
        int windowWidth = Display.getCurrent().getPrimaryMonitor().getBounds().width;
        int windowHeight = Display.getCurrent().getPrimaryMonitor().getBounds().height;
        int dialogWidth = getShell().getSize().x;
        int dialogHeight = windowHeight / 2;

        int x = (windowWidth - dialogWidth) / 2;
        int y = (windowHeight - dialogHeight) / 2;

        getShell().setSize(getShell().getSize().x, dialogHeight);
        getShell().setLocation(x, y);
    }

    /**
     * Formats the "#" of the event in the table by adding "00" before it.
     * @param number
     *            The number to format.
     * @param max
     *            The maximum number of event pairs in the list.
     * @return The formatted string.
     */
    @SuppressWarnings("nls")
    protected String formatListNumber(int number, int max) {
        return String.format("%0" + max + "d", number);
    }

    /**
     * Returns the match pairs saved in the settings file.
     * @return The match pairs saved in the settings file.
     */
    protected EventsPair getMatchPairs() {
        try {
            // Check if the settings file has already some data (i.e. try provoking an exception)
            fSettings.getInt(LATENCY_NB_MATCH_PAIRS);

            String[] starts = fSettings.getArray(LATENCY_PAIRS_START);
            String[] ends = fSettings.getArray(LATENCY_PAIRS_END);

            EventMatcher.getInstance().resetMatches();
            for (int i = 0; i < starts.length; i++) {
                EventMatcher.getInstance().addMatch(starts[i], ends[i]);
            }

            return EventMatcher.getInstance().getEvents();
        } catch (NumberFormatException e) {
            return EventMatcher.getInstance().getEvents();
        }
    }

    /**
     * Saves the event match pairs to a settings file.
     * @param start
     *            The start event types.
     * @param end
     *            The end event types.
     */
    protected void saveMatchPairs(Vector<String> start, Vector<String> end) {
        fSettings.put(LATENCY_NB_MATCH_PAIRS, start.size());
        fSettings.put(LATENCY_PAIRS_START, start.toArray(new String[] {}));
        fSettings.put(LATENCY_PAIRS_END, end.toArray(new String[] {}));
    }

    /**
     * Ask the LatencyView to send a new analysis request to the views, so that they can be redrawn.
     */
    protected void redrawGraphs() {
        LatencyController.getInstance().refreshModels();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected abstract Control createDialogArea(Composite parent);

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected abstract void createButtonsForButtonBar(Composite parent);
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return true;
    }
}