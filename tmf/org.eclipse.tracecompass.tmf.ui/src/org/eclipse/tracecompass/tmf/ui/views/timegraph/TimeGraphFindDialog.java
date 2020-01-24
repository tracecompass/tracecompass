/*****************************************************************************
 * Copyright (c) 2016 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/
package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView.FindTarget;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Find dialog to search entries into a time graph. This implementation is based
 * on the org.eclipse.ui.texteditor.FindReplaceDialog of Eclipse.
 *
 * @author Jean-Christian Kouame
 */
class TimeGraphFindDialog extends Dialog {

    /**
     * Updates the find dialog on activation changes.
     */
    class ActivationListener extends ShellAdapter {
        /*
         * @see ShellListener#shellActivated(ShellEvent)
         */
        @Override
        public void shellActivated(ShellEvent e) {
            fActiveShell = (Shell) e.widget;
            updateButtonState();

            if (fGiveFocusToFindField && getShell() == fActiveShell && okToUse(fFindField)) {
                fFindField.setFocus();
            }

        }

        /*
         * @see ShellListener#shellDeactivated(ShellEvent)
         */
        @Override
        public void shellDeactivated(ShellEvent e) {
            fGiveFocusToFindField = false;

            storeSettings();

            fActiveShell = null;
            updateButtonState();
        }
    }

    /**
     * Modify listener to update the search result in case of incremental
     * search.
     */
    private class FindModifyListener implements ModifyListener {

        /*
         * @see ModifyListener#modifyText(ModifyEvent)
         */
        @Override
        public void modifyText(ModifyEvent e) {
            updateButtonState();
        }
    }

    /** The size of the dialogs search history. */
    private static final int HISTORY_SIZE = 5;

    private final String WRAP = "wrap"; //$NON-NLS-1$
    private final String CASE_SENSITIVE = "casesensitive"; //$NON-NLS-1$
    private final String WHOLE_WORD = "wholeword"; //$NON-NLS-1$
    private final String IS_REGEX_EXPRESSION = "isRegEx"; //$NON-NLS-1$
    private final String FIND_HISTORY = "findhistory"; //$NON-NLS-1$

    private boolean fWrapInit;
    private boolean fCaseInit;
    private boolean fWholeWordInit;
    private boolean fForwardInit;
    private boolean fIsRegExInit;

    private @NonNull List<String> fFindHistory;

    private static Shell fParentShell;
    private Shell fActiveShell;

    private final ActivationListener fActivationListener = new ActivationListener();
    private final FindModifyListener fFindModifyListener = new FindModifyListener();

    private Label fStatusLabel;
    private Button fForwardRadioButton;
    private Button fCaseCheckBox;
    private Button fWrapCheckBox;
    private Button fWholeWordCheckBox;
    private Button fIsRegExCheckBox;

    private Button fFindNextButton;
    private Combo fFindField;

    /**
     * Find command adapters.
     */
    private ContentAssistCommandAdapter fContentAssistFindField;

    private Rectangle fDialogPositionInit;

    private IDialogSettings fDialogSettings;
    /**
     * <code>true</code> if the find field should receive focus the next time
     * the dialog is activated, <code>false</code> otherwise.
     */
    private boolean fGiveFocusToFindField = true;

    /**
     * Holds the mnemonic/button pairs for all buttons.
     */
    private HashMap<Character, Button> fMnemonicButtonMap = new HashMap<>();

    private @Nullable FindTarget fFindTarget;

    /**
     * Creates a new dialog with the given shell as parent.
     *
     * @param parentShell
     *            the parent shell
     */
    public TimeGraphFindDialog(Shell parentShell) {
        super(parentShell);

        fParentShell = null;
        fFindTarget = null;

        fDialogPositionInit = null;
        fFindHistory = new ArrayList<>(HISTORY_SIZE - 1);

        fWrapInit = true;
        fCaseInit = false;
        fIsRegExInit = false;
        fWholeWordInit = false;
        fForwardInit = true;

        readConfiguration();
        setShellStyle(getShellStyle() & ~SWT.APPLICATION_MODAL);
        setBlockOnOpen(false);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    /**
     * Returns <code>true</code> if control can be used.
     *
     * @param control
     *            the control to be checked
     * @return <code>true</code> if control can be used
     */
    private static boolean okToUse(Control control) {
        return control != null && !control.isDisposed();
    }

    @Override
    public void create() {
        super.create();

        Shell shell = getShell();
        shell.addShellListener(fActivationListener);

        // fill in combo contents
        fFindField.removeModifyListener(fFindModifyListener);
        updateCombo(fFindField, fFindHistory);
        fFindField.addModifyListener(fFindModifyListener);

        // get find string
        initFindStringFromSelection();

        shell.setMinimumSize(shell.getSize());

        // set dialog position
        if (fDialogPositionInit != null) {
            shell.setBounds(fDialogPositionInit);
        }

        shell.setText(Messages.TimeGraphFindDialog_FindTitle);
    }

    /**
     * Creates the options configuration section of the find dialog.
     *
     * @param parent
     *            the parent composite
     * @return the options configuration section
     */
    private Composite createConfigPanel(Composite parent) {

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        panel.setLayout(layout);

        Composite directionGroup = createDirectionGroup(panel);
        setGridData(directionGroup, SWT.FILL, true, SWT.FILL, false);

        Composite optionsGroup = createOptionsGroup(panel);
        setGridData(optionsGroup, SWT.FILL, true, SWT.FILL, true);
        ((GridData) optionsGroup.getLayoutData()).horizontalSpan = 2;

        return panel;
    }

    @Override
    protected Control createContents(Composite parent) {

        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = true;
        panel.setLayout(layout);
        panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite inputPanel = createInputPanel(panel);
        setGridData(inputPanel, SWT.FILL, true, SWT.TOP, false);

        Composite configPanel = createConfigPanel(panel);
        setGridData(configPanel, SWT.FILL, true, SWT.TOP, true);

        Composite statusBar = createStatusAndCloseButton(panel);
        setGridData(statusBar, SWT.FILL, true, SWT.BOTTOM, false);

        panel.addTraverseListener(e -> {
            if (e.detail == SWT.TRAVERSE_RETURN) {
                if (!Util.isMac()) {
                    Control controlWithFocus = getShell().getDisplay().getFocusControl();
                    if (controlWithFocus != null && (controlWithFocus.getStyle() & SWT.PUSH) == SWT.PUSH) {
                        return;
                    }
                }
                Event event1 = new Event();
                event1.type = SWT.Selection;
                event1.stateMask = e.stateMask;
                fFindNextButton.notifyListeners(SWT.Selection, event1);
                e.doit = false;
            } else if (e.detail == SWT.TRAVERSE_MNEMONIC) {
                Character mnemonic = new Character(Character.toLowerCase(e.character));
                Button button = fMnemonicButtonMap.get(mnemonic);
                if (button != null) {
                    if ((fFindField.isFocusControl() || (button.getStyle() & SWT.PUSH) != 0)
                            && button.isEnabled()) {
                        Event event2 = new Event();
                        event2.type = SWT.Selection;
                        event2.stateMask = e.stateMask;
                        if ((button.getStyle() & SWT.RADIO) != 0) {
                            Composite buttonParent = button.getParent();
                            if (buttonParent != null) {
                                Control[] children = buttonParent.getChildren();
                                for (int i = 0; i < children.length; i++) {
                                    ((Button) children[i]).setSelection(false);
                                }
                            }
                            button.setSelection(true);
                        } else {
                            button.setSelection(!button.getSelection());
                        }
                        button.notifyListeners(SWT.Selection, event2);
                        e.detail = SWT.TRAVERSE_NONE;
                        e.doit = true;
                    }
                }
            }
        });

        updateButtonState();

        applyDialogFont(panel);

        return panel;
    }

    private void setContentAssistsEnablement(boolean enable) {
        fContentAssistFindField.setEnabled(enable);
    }

    /**
     * Creates the direction defining part of the options defining section of
     * the find dialog.
     *
     * @param parent
     *            the parent composite
     * @return the direction defining part
     */
    private Composite createDirectionGroup(Composite parent) {

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        panel.setLayout(layout);

        Group group = new Group(panel, SWT.SHADOW_ETCHED_IN);
        group.setText(Messages.TimeGraphFindDialog_Direction);
        GridLayout groupLayout = new GridLayout();
        group.setLayout(groupLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        fForwardRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
        fForwardRadioButton.setText(Messages.TimeGraphFindDialog_ForwardRadioButtonLabel);
        setGridData(fForwardRadioButton, SWT.LEFT, false, SWT.CENTER, false);
        storeButtonWithMnemonicInMap(fForwardRadioButton);

        Button backwardRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
        backwardRadioButton.setText(Messages.TimeGraphFindDialog_BackwardRadioButtonLabel);
        setGridData(backwardRadioButton, SWT.LEFT, false, SWT.CENTER, false);
        storeButtonWithMnemonicInMap(backwardRadioButton);

        backwardRadioButton.setSelection(!fForwardInit);
        fForwardRadioButton.setSelection(fForwardInit);

        return panel;
    }

    /**
     * Creates the panel where the user specifies the text to search for
     *
     * @param parent
     *            the parent composite
     * @return the input panel
     */
    private Composite createInputPanel(Composite parent) {
        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        panel.setLayout(layout);

        Label findLabel = new Label(panel, SWT.LEFT);
        findLabel.setText(Messages.TimeGraphFindDialog_FindLabel);
        setGridData(findLabel, SWT.LEFT, false, SWT.CENTER, false);

        // Create the find content assist field
        ComboContentAdapter contentAdapter = new ComboContentAdapter();
        FindReplaceDocumentAdapterContentProposalProvider findProposer = new FindReplaceDocumentAdapterContentProposalProvider(true);
        fFindField = new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
        fContentAssistFindField = new ContentAssistCommandAdapter(
                fFindField,
                contentAdapter,
                findProposer,
                ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS,
                new char[0],
                true);
        setGridData(fFindField, SWT.FILL, true, SWT.CENTER, false);
        fFindField.addModifyListener(fFindModifyListener);

        return panel;
    }

    /**
     * Creates the functional options part of the options defining section of
     * the find dialog.
     *
     * @param parent
     *            the parent composite
     * @return the options group
     */
    private Composite createOptionsGroup(Composite parent) {

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        panel.setLayout(layout);

        Group group = new Group(panel, SWT.SHADOW_NONE);
        group.setText(Messages.TimeGraphFindDialog_Options);
        GridLayout groupLayout = new GridLayout();
        groupLayout.numColumns = 1;
        groupLayout.makeColumnsEqualWidth = true;
        group.setLayout(groupLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        SelectionListener selectionListener = new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                storeSettings();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }
        };

        fCaseCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
        fCaseCheckBox.setText(Messages.TimeGraphFindDialog_CaseCheckBoxLabel);
        setGridData(fCaseCheckBox, SWT.LEFT, false, SWT.CENTER, false);
        fCaseCheckBox.setSelection(fCaseInit);
        fCaseCheckBox.addSelectionListener(selectionListener);
        storeButtonWithMnemonicInMap(fCaseCheckBox);

        fWrapCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
        fWrapCheckBox.setText(Messages.TimeGraphFindDialog_WrapCheckBoxLabel);
        setGridData(fWrapCheckBox, SWT.LEFT, false, SWT.CENTER, false);
        fWrapCheckBox.setSelection(fWrapInit);
        fWrapCheckBox.addSelectionListener(selectionListener);
        storeButtonWithMnemonicInMap(fWrapCheckBox);

        fWholeWordCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
        fWholeWordCheckBox.setText(Messages.TimeGraphFindDialog_WholeWordCheckBoxLabel);
        setGridData(fWholeWordCheckBox, SWT.LEFT, false, SWT.CENTER, false);
        fWholeWordCheckBox.setSelection(fWholeWordInit);
        fWholeWordCheckBox.addSelectionListener(selectionListener);
        storeButtonWithMnemonicInMap(fWholeWordCheckBox);

        fIsRegExCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
        fIsRegExCheckBox.setText(Messages.TimeGraphFindDialog_REgExCheckBoxLabel);
        setGridData(fIsRegExCheckBox, SWT.LEFT, false, SWT.CENTER, false);
        ((GridData) fIsRegExCheckBox.getLayoutData()).horizontalSpan = 2;
        fIsRegExCheckBox.setSelection(fIsRegExInit);
        fIsRegExCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean newState = fIsRegExCheckBox.getSelection();
                updateButtonState();
                storeSettings();
                setContentAssistsEnablement(newState);
            }
        });
        storeButtonWithMnemonicInMap(fIsRegExCheckBox);
        fWholeWordCheckBox.setEnabled(!isRegExSearch());
        fWholeWordCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonState();
            }
        });
        return panel;
    }

    /**
     * Creates the status and close section of the dialog.
     *
     * @param parent
     *            the parent composite
     * @return the status and close button
     */
    private Composite createStatusAndCloseButton(Composite parent) {

        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        panel.setLayout(layout);

        fStatusLabel = new Label(panel, SWT.LEFT);
        fStatusLabel.setText(Messages.TimeGraphFindDialog_StatusWrappedLabel);
        setGridData(fStatusLabel, SWT.FILL, true, SWT.CENTER, false);
        GridData gd = (GridData) fStatusLabel.getLayoutData();
        gd.widthHint = fStatusLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        fStatusLabel.setText("");  //$NON-NLS-1$

        Composite buttonSection = new Composite(panel, SWT.NULL);
        GridLayout buttonLayout = new GridLayout();
        buttonLayout.numColumns = 2;
        buttonSection.setLayout(buttonLayout);

        String label = Messages.TimeGraphFindDialog_CloseButtonLabel;
        Button closeButton = createButton(buttonSection, 101, label, false);
        setGridData(closeButton, SWT.RIGHT, false, SWT.BOTTOM, false);

        fFindNextButton = makeButton(buttonSection, Messages.TimeGraphFindDialog_FindNextButtonLabel, 102, true, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                performSearch(((e.stateMask & SWT.SHIFT) != 0) ^ isForwardSearch());
                updateFindHistory();
            }
        });
        setGridData(fFindNextButton, SWT.FILL, true, SWT.FILL, false);

        return panel;
    }

    @Override
    protected void buttonPressed(int buttonID) {
        if (buttonID == 101) {
            close();
        }
    }

    /**
     * Update the dialog data (parentShell, listener, input, ...)
     *
     * @param findTarget
     *            the new find target
     */
    public void update(@NonNull FindTarget findTarget) {
        updateTarget(findTarget, true);
    }

    // ------- action invocation ---------------------------------------

    /**
     * Returns the index of the entry that match the specified search string, or
     * <code>-1</code> if the string can not be found when searching using the
     * given options.
     *
     * @param findString
     *            the string to search for
     * @param startIndex
     *            the index at which to start the search
     * @param items
     *            The map of items in the time graph view
     * @param options
     *            The options use for the search
     * @return the index of the find entry following the options or
     *         <code>-1</code> if nothing found
     */
    private int findNext(String findString, int startIndex, BiMap<ITimeGraphEntry, Integer> items, SearchOptions options) {
        int index;
        if (options.forwardSearch) {
            index = startIndex == items.size() - 1 ? -1 : findNext(startIndex + 1, findString, items, options);
        } else {
            index = startIndex == 0 ? -1 : findNext(startIndex - 1, findString, items, options);
        }

        if (index == -1) {
            if (okToUse(getShell())) {
                getShell().getDisplay().beep();
            }
            if (options.wrapSearch) {
                statusMessage(Messages.TimeGraphFindDialog_StatusWrappedLabel);
                index = findNext(-1, findString, items, options);
            }
        }
        return index;
    }

    private int findNext(int startIndex, String findString, BiMap<ITimeGraphEntry, Integer> items, SearchOptions options) {
        FindTarget findTarget = fFindTarget;
        if (findTarget != null) {
            if (findString == null || findString.length() == 0) {
                return -1;
            }

            final @NonNull Pattern pattern = getPattern(findString, options);
            int index = adjustIndex(startIndex, items.size(), options.forwardSearch);
            BiMap<Integer, ITimeGraphEntry> entries = items.inverse();
            while (index >= 0 && index < entries.size()) {
                final @Nullable ITimeGraphEntry entry = entries.get(index);
                if (entry != null) {
                    String[] columnTexts = findTarget.getColumnTexts(entry);
                    for (int i = 0; i < columnTexts.length; i++) {
                        String columnText = columnTexts[i];
                        if (columnText != null && !columnText.isEmpty() && pattern.matcher(columnTexts[i]).find()) {
                            return index;
                        }
                    }
                }
                index = options.forwardSearch ? ++index : --index;
            }
        }
        return -1;
    }

    /**
     * Returns whether the specified search string can be found using the given
     * options.
     *
     * @param findString
     *            the string to search for
     * @param options
     *            The search options
     * @return <code>true</code> if the search string can be found using the
     *         given options
     *
     */
    private boolean findAndSelect(String findString, SearchOptions options) {
        FindTarget findTarget = fFindTarget;
        if (findTarget == null) {
            return false;
        }
        ITimeGraphEntry[] topInput = findTarget.getEntries();
        BiMap<@NonNull ITimeGraphEntry, @NonNull Integer> items = HashBiMap.create();
        for (ITimeGraphEntry entry : topInput) {
            listEntries(items, entry);
        }
        int startPosition = findTarget.getSelection() == null ? 0 : NonNullUtils.checkNotNull(items.get(findTarget.getSelection()));

        int index = findNext(findString, startPosition, items, options);

        if (index == -1) {
            statusMessage(Messages.TimeGraphFindDialog_StatusNoMatchLabel);
            return false;
        }

        if (options.forwardSearch && index >= startPosition || !options.forwardSearch && index <= startPosition) {
            statusMessage(""); //$NON-NLS-1$
        }

        // Send the entry found to target
        findTarget.selectAndReveal(NonNullUtils.checkNotNull(items.inverse().get(index)));
        return true;
    }

    private void listEntries(Map<ITimeGraphEntry, Integer> items, ITimeGraphEntry root) {
        items.put(root, items.size());
        for (ITimeGraphEntry child : root.getChildren()) {
            listEntries(items, child);
        }
    }

    // ------- accessors ---------------------------------------

    /**
     * Retrieves the string to search for from the appropriate text input field
     * and returns it.
     *
     * @return the search string
     */
    private String getFindString() {
        if (okToUse(fFindField)) {
            return fFindField.getText();
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the dialog's boundaries.
     *
     * @return the dialog's boundaries
     */
    private Rectangle getDialogBoundaries() {
        if (okToUse(getShell())) {
            return getShell().getBounds();
        }
        return fDialogPositionInit;
    }

    // ------- init / close ---------------------------------------
    @Override
    public boolean close() {
        handleDialogClose();
        return super.close();
    }

    /**
     * Removes focus changed listener from browser and stores settings for
     * re-open.
     */
    private void handleDialogClose() {

        // remove listeners
        if (okToUse(fFindField)) {
            fFindField.removeModifyListener(fFindModifyListener);
        }
        updateTarget(null, false);

        if (fParentShell != null) {
            fParentShell.removeShellListener(fActivationListener);
            fParentShell = null;
        }

        if (getShell() != null && !getShell().isDisposed()) {
            getShell().removeShellListener(fActivationListener);
        }

        // store current settings in case of re-open
        storeSettings();

        // prevent leaks
        fActiveShell = null;

    }

    /**
     * Writes the current selection to the dialog settings.
     */
    private void writeSelection() {
        final FindTarget input = fFindTarget;
        if (input == null) {
            return;
        }

        IDialogSettings s = getDialogSettings();
        final ITimeGraphEntry selection = input.getSelection();
        if (selection != null) {
            s.put("selection", selection.getName()); //$NON-NLS-1$
        }
    }

    /**
     * Stores the current state in the dialog settings.
     */
    private void storeSettings() {
        fDialogPositionInit = getDialogBoundaries();
        fWrapInit = isWrapSearch();
        fWholeWordInit = isWholeWordSetting();
        fCaseInit = isCaseSensitiveSearch();
        fIsRegExInit = isRegExSearch();
        fForwardInit = isForwardSearch();

        writeConfiguration();
    }

    /**
     * Initializes the string to search for and the appropriate text in the Find
     * field based on the selection found in the timegraph view.
     */
    private void initFindStringFromSelection() {
        FindTarget findTarget = fFindTarget;
        if (findTarget != null && okToUse(fFindField)) {
            final ITimeGraphEntry selection = findTarget.getSelection();
            if (selection != null) {
                String fullSelection = selection.getName();
                fFindField.removeModifyListener(fFindModifyListener);
                if (fullSelection.length() > 0) {
                    fFindField.setText(fullSelection);
                }
            } else {
                if ("".equals(fFindField.getText())) { //$NON-NLS-1$
                    if (!fFindHistory.isEmpty()) {
                        fFindField.setText(fFindHistory.get(0));
                    } else {
                        fFindField.setText(""); //$NON-NLS-1$
                    }
                }
            }
            fFindField.setSelection(new Point(0, fFindField.getText().length()));
            fFindField.addModifyListener(fFindModifyListener);
        }
    }

    // ------- Options ---------------------------------------

    /**
     * Retrieves and returns the option case sensitivity from the appropriate
     * check box.
     *
     * @return <code>true</code> if case sensitive
     */
    private boolean isCaseSensitiveSearch() {
        if (okToUse(fCaseCheckBox)) {
            return fCaseCheckBox.getSelection();
        }
        return fCaseInit;
    }

    /**
     * Retrieves and returns the regEx option from the appropriate check box.
     *
     * @return <code>true</code> if case sensitive
     */
    private boolean isRegExSearch() {
        if (okToUse(fIsRegExCheckBox)) {
            return fIsRegExCheckBox.getSelection();
        }
        return fIsRegExInit;
    }

    /**
     * Retrieves and returns the option search direction from the appropriate
     * check box.
     *
     * @return <code>true</code> if searching forward
     */
    private boolean isForwardSearch() {
        if (okToUse(fForwardRadioButton)) {
            return fForwardRadioButton.getSelection();
        }
        return fForwardInit;
    }

    /**
     * Retrieves and returns the option search whole words from the appropriate
     * check box.
     *
     * @return <code>true</code> if searching for whole words
     */
    private boolean isWholeWordSetting() {
        if (okToUse(fWholeWordCheckBox)) {
            return fWholeWordCheckBox.getSelection();
        }
        return fWholeWordInit;
    }

    /**
     * Returns <code>true</code> if searching should be restricted to entire
     * words, <code>false</code> if not. This is the case if the respective
     * checkbox is turned on, regex is off, and the checkbox is enabled, i.e.
     * the current find string is an entire word.
     *
     * @return <code>true</code> if the search is restricted to whole words
     */
    private boolean isWholeWordSearch() {
        return isWholeWordSetting() && !isRegExSearch() && (okToUse(fWholeWordCheckBox) ? fWholeWordCheckBox.isEnabled() : true);
    }

    /**
     * Retrieves and returns the option wrap search from the appropriate check
     * box.
     *
     * @return <code>true</code> if wrapping while searching
     */
    private boolean isWrapSearch() {
        if (okToUse(fWrapCheckBox)) {
            return fWrapCheckBox.getSelection();
        }
        return fWrapInit;
    }

    /**
     * Creates a button.
     *
     * @param parent
     *            the parent control
     * @param label
     *            the button label
     * @param id
     *            the button id
     * @param dfltButton
     *            is this button the default button
     * @param listener
     *            a button pressed listener
     * @return the new button
     */
    private Button makeButton(Composite parent, String label, int id, boolean dfltButton, SelectionListener listener) {
        Button button = createButton(parent, id, label, dfltButton);
        button.addSelectionListener(listener);
        storeButtonWithMnemonicInMap(button);
        return button;
    }

    /**
     * Stores the button and its mnemonic in {@link #fMnemonicButtonMap}.
     *
     * @param button
     *            button whose mnemonic has to be stored
     */
    private void storeButtonWithMnemonicInMap(Button button) {
        char mnemonic = LegacyActionTools.extractMnemonic(button.getText());
        if (mnemonic != LegacyActionTools.MNEMONIC_NONE) {
            fMnemonicButtonMap.put(new Character(Character.toLowerCase(mnemonic)), button);
        }
    }

    /**
     * Sets the given status message in the status line.
     *
     * @param message
     *            the message
     */
    private void statusMessage(String message) {
        fStatusLabel.setText(message);
        getShell().getDisplay().beep();
    }

    /**
     * Locates the user's findString in the entries information of the time
     * graph view.
     *
     * @param forwardSearch
     *            the search direction
     */
    private void performSearch(boolean forwardSearch) {

        String findString = getFindString();
        if (findString != null && findString.length() > 0) {
            findAndSelect(findString, getSearchOptions(forwardSearch));
        }
        writeSelection();
        updateButtonState();
    }

    private SearchOptions getSearchOptions(boolean forwardSearch) {
        SearchOptions options = new SearchOptions();
        options.forwardSearch = forwardSearch;
        options.caseSensitive = isCaseSensitiveSearch();
        options.wrapSearch = isWrapSearch();
        options.wholeWord = isWholeWordSearch();
        options.regExSearch = isRegExSearch();
        return options;
    }

    // ------- UI creation ---------------------------------------

    /**
     * Attaches the given layout specification to the <code>component</code>.
     *
     * @param component
     *            the component
     * @param horizontalAlignment
     *            horizontal alignment
     * @param grabExcessHorizontalSpace
     *            grab excess horizontal space
     * @param verticalAlignment
     *            vertical alignment
     * @param grabExcessVerticalSpace
     *            grab excess vertical space
     */
    private static void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace, int verticalAlignment, boolean grabExcessVerticalSpace) {
        GridData gd;
        if (component instanceof Button && (((Button) component).getStyle() & SWT.PUSH) != 0) {
            gd = (GridData) component.getLayoutData();
            gd.horizontalAlignment = GridData.FILL;
            gd.widthHint = 100;
        } else {
            gd = new GridData();
            component.setLayoutData(gd);
            gd.horizontalAlignment = horizontalAlignment;
            gd.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
        }
        gd.verticalAlignment = verticalAlignment;
        gd.grabExcessVerticalSpace = grabExcessVerticalSpace;
    }

    /**
     * Updates the enabled state of the buttons.
     */
    private void updateButtonState() {
        if (okToUse(getShell()) && okToUse(fFindNextButton)) {

            boolean enable = fFindTarget != null && (fActiveShell == fParentShell || fActiveShell == getShell());
            String str = getFindString();
            boolean findString = str != null && str.length() > 0;

            fWholeWordCheckBox.setEnabled(isWord(str) && !isRegExSearch());

            fFindNextButton.setEnabled(enable && findString);
        }
    }

    /**
     * Tests whether each character in the given string is a letter.
     *
     * @param str
     *            the string to check
     * @return <code>true</code> if the given string is a word
     */
    private static boolean isWord(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isJavaIdentifierPart(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the given combo with the given content.
     *
     * @param combo
     *            combo to be updated
     * @param content
     *            to be put into the combo
     */
    private static void updateCombo(Combo combo, List<String> content) {
        combo.removeAll();
        for (int i = 0; i < content.size(); i++) {
            combo.add(content.get(i).toString());
        }
    }

    // ------- open / reopen ---------------------------------------

    /**
     * Called after executed find action to update the history.
     */
    private void updateFindHistory() {
        if (okToUse(fFindField)) {
            fFindField.removeModifyListener(fFindModifyListener);

            updateHistory(fFindField, fFindHistory);
            fFindField.addModifyListener(fFindModifyListener);
        }
    }

    /**
     * Updates the combo with the history.
     *
     * @param combo
     *            to be updated
     * @param history
     *            to be put into the combo
     */
    private static void updateHistory(Combo combo, List<String> history) {
        String findString = combo.getText();
        int index = history.indexOf(findString);
        if (index != 0) {
            if (index != -1) {
                history.remove(index);
            }
            history.add(0, findString);
            Point selection = combo.getSelection();
            updateCombo(combo, history);
            combo.setText(findString);
            combo.setSelection(selection);
        }
    }

    /**
     * Sets the parent shell of this dialog to be the given shell.
     *
     * @param shell
     *            the new parent shell
     */
    @Override
    public void setParentShell(Shell shell) {
        if (shell != fParentShell) {

            if (fParentShell != null) {
                fParentShell.removeShellListener(fActivationListener);
            }

            fParentShell = shell;
            fParentShell.addShellListener(fActivationListener);
        }

        fActiveShell = shell;
    }

    // --------------- configuration handling --------------

    /**
     * Returns the dialog settings object used to share state of the find
     * dialog.
     *
     * @return the dialog settings to be used
     */
    private IDialogSettings getDialogSettings() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        fDialogSettings = settings.getSection(getClass().getName());
        if (fDialogSettings == null) {
            fDialogSettings = settings.addNewSection(getClass().getName());
        }
        return fDialogSettings;
    }

    /**
     * Initializes itself from the dialog settings with the same state as at the
     * previous invocation.
     */
    private void readConfiguration() {
        IDialogSettings s = getDialogSettings();

        fWrapInit = s.get(WRAP) == null || s.getBoolean(WRAP);
        fCaseInit = s.getBoolean(CASE_SENSITIVE);
        fWholeWordInit = s.getBoolean(WHOLE_WORD);
        fIsRegExInit = s.getBoolean(IS_REGEX_EXPRESSION);

        String[] findHistory = s.getArray(FIND_HISTORY);
        if (findHistory != null) {
            fFindHistory.clear();
            for (int i = 0; i < findHistory.length; i++) {
                fFindHistory.add(findHistory[i]);
            }
        }
    }

    /**
     * Stores its current configuration in the dialog store.
     */
    private void writeConfiguration() {
        IDialogSettings s = getDialogSettings();

        s.put(WRAP, fWrapInit);
        s.put(CASE_SENSITIVE, fCaseInit);
        s.put(WHOLE_WORD, fWholeWordInit);
        s.put(CASE_SENSITIVE, fIsRegExInit);

        String findString = getFindString();
        if (findString.length() > 0) {
            fFindHistory.add(0, findString);
        }
        writeHistory(fFindHistory, s, FIND_HISTORY);
    }

    /**
     * Writes the given history into the given dialog store.
     *
     * @param history
     *            the history
     * @param settings
     *            the dialog settings
     * @param sectionName
     *            the section name
     */
    private static void writeHistory(List<String> history, IDialogSettings settings, String sectionName) {
        int itemCount = history.size();
        Set<String> distinctItems = new HashSet<>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            String item = history.get(i);
            if (distinctItems.contains(item)) {
                history.remove(i--);
                itemCount--;
            } else {
                distinctItems.add(item);
            }
        }

        while (history.size() > 8) {
            history.remove(8);
        }

        String[] names = new String[history.size()];
        history.toArray(names);
        settings.put(sectionName, names);

    }

    /**
     * Update the time graph viewer this dialog search in
     *
     * @param findTarget
     *            The find target. Can be <code>null</code>.
     * @param initFindString
     *            This value should be true if the find text field needs to be
     *            populated with selected entry in the find viewer, false
     *            otherwise
     */
    public void updateTarget(@Nullable FindTarget findTarget, boolean initFindString) {
        fFindTarget = findTarget;
        if (initFindString) {
            initFindStringFromSelection();
        }
        updateButtonState();
    }

    /**
     * Test if the shell to test is the parent shell of the dialog
     *
     * @param shell
     *            The shell to test
     * @return <code>true</code> if the shell to test is the parent shell,
     *         <code>false</code> otherwise
     */
    public boolean isDialogParentShell(Shell shell) {
        return shell == getParentShell();
    }

    /**
     * Adjust the index where to start the search
     *
     * @param previousIndex
     *            The previous index
     * @param count
     *            The number of items to search into
     * @param forwardSearch
     *            The search direction
     * @return The adjusted index
     */
    private static int adjustIndex(int previousIndex, int count, boolean forwardSearch) {
        int index = previousIndex;
        if (forwardSearch) {
            index = index >= count || index < 0 ? 0 : index;
        } else {
            index = index >= count || index < 0 ? count - 1 : index;
        }
        return index;
    }

    /**
     * Create a pattern from the string to find and with options provided
     *
     * This implementation is drawn from the jface implementation of
     * org.eclipse.jface.text.FindReplaceDocumentAdapter
     *
     * @param findString
     *            The string to find
     * @param caseSensitive
     *            Tells if the pattern will activate the case sensitive flag
     * @param wholeWord
     *            Tells if the pattern will activate the whole word flag
     * @param regExSearch
     *            Tells if the pattern will activate the regEx flag
     * @return The created pattern
     */
    private static @NonNull Pattern getPattern(String findString, SearchOptions options) {
        String toFind = findString;

        int patternFlags = 0;

        if (options.regExSearch) {
            toFind = substituteLinebreak(toFind);
        }

        if (!options.caseSensitive) {
            patternFlags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }

        if (!options.regExSearch) {
            toFind = asRegPattern(toFind);
        }

        if (options.wholeWord) {
            toFind = "\\b" + toFind + "\\b"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        Pattern pattern = Pattern.compile(toFind, patternFlags);
        return pattern;
    }

    /**
     * Substitutes \R in a regex find pattern with (?>\r\n?|\n)
     *
     * This implementation is drawn from the jface implementation of
     * org.eclipse.jface.text.FindReplaceDocumentAdapter
     *
     * @param findString
     *            the original find pattern
     * @return the transformed find pattern
     */
    private static String substituteLinebreak(String findString) {
        int length = findString.length();
        StringBuffer buf = new StringBuffer(length);

        int inCharGroup = 0;
        int inBraces = 0;
        boolean inQuote = false;
        for (int i = 0; i < length; i++) {
            char ch = findString.charAt(i);
            switch (ch) {
            case '[':
                buf.append(ch);
                if (!inQuote) {
                    inCharGroup++;
                }
                break;

            case ']':
                buf.append(ch);
                if (!inQuote) {
                    inCharGroup--;
                }
                break;

            case '{':
                buf.append(ch);
                if (!inQuote && inCharGroup == 0) {
                    inBraces++;
                }
                break;

            case '}':
                buf.append(ch);
                if (!inQuote && inCharGroup == 0) {
                    inBraces--;
                }
                break;

            case '\\':
                if (i + 1 < length) {
                    char ch1 = findString.charAt(i + 1);
                    if (inQuote) {
                        if (ch1 == 'E') {
                            inQuote = false;
                        }
                        buf.append(ch).append(ch1);
                        i++;

                    } else if (ch1 == 'R') {
                        if (inCharGroup > 0 || inBraces > 0) {
                            String msg = "TimeGrahViewer.illegalLinebreak"; //$NON-NLS-1$
                            throw new PatternSyntaxException(msg, findString, i);
                        }
                        buf.append("(?>\\r\\n?|\\n)"); //$NON-NLS-1$
                        i++;

                    } else {
                        if (ch1 == 'Q') {
                            inQuote = true;
                        }
                        buf.append(ch).append(ch1);
                        i++;
                    }
                } else {
                    buf.append(ch);
                }
                break;

            default:
                buf.append(ch);
                break;
            }

        }
        return buf.toString();
    }

    /**
     * Converts a non-regex string to a pattern that can be used with the regex
     * search engine.
     *
     * This implementation is drawn from the jface implementation of
     * org.eclipse.jface.text.FindReplaceDocumentAdapter
     *
     * @param string
     *            the non-regex pattern
     * @return the string converted to a regex pattern
     */
    private static String asRegPattern(String string) {
        StringBuffer out = new StringBuffer(string.length());
        boolean quoting = false;

        for (int i = 0, length = string.length(); i < length; i++) {
            char ch = string.charAt(i);
            if (ch == '\\') {
                if (quoting) {
                    out.append("\\E"); //$NON-NLS-1$
                    quoting = false;
                }
                out.append("\\\\"); //$NON-NLS-1$
                continue;
            }
            if (!quoting) {
                out.append("\\Q"); //$NON-NLS-1$
                quoting = true;
            }
            out.append(ch);
        }
        if (quoting) {
            out.append("\\E"); //$NON-NLS-1$
        }

        return out.toString();
    }

    private class SearchOptions {
        boolean forwardSearch;
        boolean caseSensitive;
        boolean wrapSearch;
        boolean wholeWord;
        boolean regExSearch;
    }
}
