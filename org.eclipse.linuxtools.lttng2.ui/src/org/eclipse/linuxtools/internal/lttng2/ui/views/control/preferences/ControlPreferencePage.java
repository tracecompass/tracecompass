/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.logging.ControlCommandLogger;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
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
    // Attributes
    // ------------------------------------------------------------------------
    RadioGroupFieldEditor fVerboseLevel;
    BooleanFieldEditor  fIsAppend;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ControlPreferencePage() {
        super(FieldEditorPreferencePage.GRID);

        // Set the preference store for the preference page.
        IPreferenceStore store = ControlPreferences.getInstance().getPreferenceStore();
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

        StringFieldEditor tracingGroup = new StringFieldEditor(ControlPreferences.TRACE_CONTROL_TRACING_GROUP_PREF, Messages.TraceControl_TracingGroupPreference, getFieldEditorParent());
        addField(tracingGroup);

        BooleanFieldEditor logCommand = new BooleanFieldEditor(ControlPreferences.TRACE_CONTROL_LOG_COMMANDS_PREF, Messages.TraceControl_LoggingPreference, getFieldEditorParent());
        addField(logCommand);

        StringFieldEditor logfile = new StringFieldEditor(ControlPreferences.TRACE_CONTROL_LOG_FILE_PATH_PREF, Messages.TraceControl_LogfilePath, getFieldEditorParent());
        addField(logfile);

        fIsAppend = new BooleanFieldEditor(ControlPreferences.TRACE_CONTROL_LOG_APPEND_PREF, Messages.TraceControl_AppendLogfilePreference, getFieldEditorParent());
        addField(fIsAppend);

        fVerboseLevel = new RadioGroupFieldEditor (
                ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_PREF,
                Messages.TraceControl_VerboseLevelsPreference,
                4,
                new String[][] {
                    {
                        Messages.TraceControl_VerboseLevelNonePreference,
                        ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_NONE,
                    },
                    {
                        Messages.TraceControl_VerboseLevelVerbosePreference,
                        ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_VERBOSE
                    },
                    {
                        Messages.TraceControl_VerboseLevelVeryVerbosePreference,
                        ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_V_VERBOSE
                    },
                    {
                        Messages.TraceControl_VerboseLevelVeryVeryVerbosePreference,
                        ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_V_V_VERBOSE
                    }
                },
                getFieldEditorParent(),
                true);

        addField(fVerboseLevel);

        Boolean enabled = ControlPreferences.getInstance().isLoggingEnabled();
        fVerboseLevel.setEnabled(enabled, getFieldEditorParent());
        fIsAppend.setEnabled(enabled, getFieldEditorParent());
        logfile.setEnabled(false, getFieldEditorParent());

        IntegerFieldEditor commandTimeout = new IntegerFieldEditor(ControlPreferences.TRACE_CONTROL_COMMAND_TIMEOUT_PREF, Messages.TraceControl_CommandTimeout, getFieldEditorParent());
        commandTimeout.setValidRange(ControlPreferences.TRACE_CONTROL_MIN_TIMEOUT_VALUE, ControlPreferences.TRACE_CONTROL_MAX_TIMEOUT_VALUE);
        addField(commandTimeout);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getProperty().equals(FieldEditor.VALUE)) {
            if (event.getSource() instanceof FieldEditor) {
                FieldEditor editor = (FieldEditor) event.getSource();
                if (editor.getPreferenceName().equals(ControlPreferences.TRACE_CONTROL_LOG_COMMANDS_PREF)) {
                    Boolean enabled = (Boolean)event.getNewValue();
                    fVerboseLevel.setEnabled(enabled, getFieldEditorParent());
                    fIsAppend.setEnabled(enabled, getFieldEditorParent());
                }
            }
        }
        super.propertyChange(event);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        fVerboseLevel.setEnabled(false, getFieldEditorParent());
        fIsAppend.setEnabled(false, getFieldEditorParent());
    }

    @Override
    public boolean performOk() {
        boolean ret =  super.performOk();
        // open or close log file
        if (ControlPreferences.getInstance().isLoggingEnabled()) {
            ControlCommandLogger.init(ControlPreferences.getInstance().getLogfilePath(), ControlPreferences.getInstance().isAppend());
        } else {
            ControlCommandLogger.close();
        }
        return ret;
    }
}
