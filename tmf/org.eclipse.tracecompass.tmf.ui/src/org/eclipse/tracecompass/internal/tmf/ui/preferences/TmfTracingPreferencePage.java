/*******************************************************************************
 * Copyright (c) 2011, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Top Level Preference Page for Tracing.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class TmfTracingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Preferences
     */
    public TmfTracingPreferencePage() {
        super(FieldEditorPreferencePage.GRID);

        // Set the preference store for the preference page.
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to do yet!
    }

    @Override
    protected void createFieldEditors() {
        BooleanFieldEditor explorerRange = new BooleanFieldEditor(ITmfUIPreferences.TRACE_DISPLAY_RANGE_PROJECTEXPLORER,
                Messages.TmfTracingPreferencePage_TraceRangeInProjectExplorer, getFieldEditorParent());
        addField(explorerRange);

        BooleanFieldEditor confirmDeletionSupplementaryFiles = new BooleanFieldEditor(ITmfUIPreferences.CONFIRM_DELETION_SUPPLEMENTARY_FILES,
                Messages.TmfTracingPreferencePage_ConfirmDeletionSupplementaryFiles, getFieldEditorParent());
        addField(confirmDeletionSupplementaryFiles);

        BooleanFieldEditor resourceRefresh = new BooleanFieldEditor(ITmfUIPreferences.ALWAYS_CLOSE_ON_RESOURCE_CHANGE,
                Messages.TmfTracingPreferencePage_AlwaysCloseOnResourceChange, getFieldEditorParent());
        addField(resourceRefresh);

        BooleanFieldEditor treeXyWarning = new BooleanFieldEditor(ITmfUIPreferences.HIDE_MANY_ENTRIES_SELECTED_TOGGLE,
                Messages.TmfTracingPreferencePage_HideManyEntriesSelectedWarning, getFieldEditorParent());
        addField(treeXyWarning);
    }

    @Override
    public boolean performOk() {
        boolean performOK = super.performOk();
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            TmfProjectElement projectElement = TmfProjectRegistry.getProject(project);
            if (projectElement != null) {
                projectElement.refresh();
            }
        }
        return performOK;
    }
}
