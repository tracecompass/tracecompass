/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.remote.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.tracecompass.internal.tmf.remote.core.preferences.TmfRemotePreferences;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * <p>
 * Preference page implementation for configuring LTTng tracer control preferences.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ControlPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ControlPreferencePage() {
        super(FieldEditorPreferencePage.GRID);

        // Set the preference store for the preference page.
        IPreferenceStore store = Activator.getDefault().getCorePreferenceStore();
        setPreferenceStore(store);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void init(IWorkbench workbench) {
        // Do nothing
    }

    @Override
    protected void createFieldEditors() {

        IntegerFieldEditor commandTimeout = new IntegerFieldEditor(TmfRemotePreferences.TRACE_CONTROL_COMMAND_TIMEOUT_PREF, Messages.TraceControl_CommandTimeout, getFieldEditorParent());
        commandTimeout.setValidRange(TmfRemotePreferences.TRACE_CONTROL_MIN_TIMEOUT_VALUE, TmfRemotePreferences.TRACE_CONTROL_MAX_TIMEOUT_VALUE);
        addField(commandTimeout);
    }
}