/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.preferences;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for perspective preferences.
 */
public class PerspectivesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public PerspectivesPreferencePage() {
        super(FieldEditorPreferencePage.GRID);

        // Set the preference store for the preference page.
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {

        RadioGroupFieldEditor switchToPerspective = new RadioGroupFieldEditor(
                ITmfUIPreferences.SWITCH_TO_PERSPECTIVE,
                Messages.PerspectivesPreferencePage_SwitchToPerspectiveGroupText,
                3,
                new String[][] {
                    { Messages.PerspectivesPreferencePage_SwitchToPerspectiveAlways, MessageDialogWithToggle.ALWAYS },
                    { Messages.PerspectivesPreferencePage_SwitchToPerspectiveNever, MessageDialogWithToggle.NEVER },
                    { Messages.PerspectivesPreferencePage_SwitchToPerspectivePrompt, MessageDialogWithToggle.PROMPT }},
                getFieldEditorParent(),
                true);
        addField(switchToPerspective);
    }
}