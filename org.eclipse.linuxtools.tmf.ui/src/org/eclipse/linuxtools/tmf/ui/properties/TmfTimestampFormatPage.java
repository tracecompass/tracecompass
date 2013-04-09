/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.properties;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The TMF timestamp format configuration page. This page is used to select
 * the global timestamp and interval time formats (for display and parsing).
 * The user can either pick a pre-defined format or enter his/her own.
 *
 * @version 1.0
 * @author Francois Chouinard
 * @since 2.0
 */
public class TmfTimestampFormatPage extends PreferencePage implements IWorkbenchPreferencePage, SelectionListener, IPropertyChangeListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // Date and Time formats
    private static final String[][] fDateTimeFormats = new String[][] {
        { TmfTimePreferences.DATE_YEAR_FMT,     TmfTimePreferences.DATE_YEAR_FMT    },
        { TmfTimePreferences.DATE_YEAR2_FMT,    TmfTimePreferences.DATE_YEAR2_FMT   },
        { TmfTimePreferences.DATE_MONTH_FMT,    TmfTimePreferences.DATE_MONTH_FMT   },
        { TmfTimePreferences.DATE_DAY_FMT,      TmfTimePreferences.DATE_DAY_FMT     },
        { TmfTimePreferences.DATE_JDAY_FMT,     TmfTimePreferences.DATE_JDAY_FMT    },
        { TmfTimePreferences.TIME_HOUR_FMT,     TmfTimePreferences.TIME_HOUR_FMT    },
        { TmfTimePreferences.TIME_MINUTE_FMT,   TmfTimePreferences.TIME_MINUTE_FMT  },
        { TmfTimePreferences.TIME_SECOND_FMT,   TmfTimePreferences.TIME_SECOND_FMT  },
        { TmfTimePreferences.TIME_ELAPSED_FMT + " (secs in epoch)",  TmfTimePreferences.TIME_ELAPSED_FMT }, //$NON-NLS-1$
        { "(none)",          TmfTimePreferences.TIME_NO_FMT      }, //$NON-NLS-1$
    };

    // Sub-second formats
    private static final String[][] fSubSecondFormats = new String[][] {
        { TmfTimePreferences.SUBSEC_MILLI_FMT + " (ms)", TmfTimePreferences.SUBSEC_MILLI_FMT }, //$NON-NLS-1$
        { TmfTimePreferences.SUBSEC_MICRO_FMT + " (µs)", TmfTimePreferences.SUBSEC_MICRO_FMT }, //$NON-NLS-1$
        { TmfTimePreferences.SUBSEC_NANO_FMT  + " (ns)", TmfTimePreferences.SUBSEC_NANO_FMT  }, //$NON-NLS-1$
    };

    // Date and Time delimiters
    private static final String[][] fDateTimeDelimiters = new String[][] {
        { "(none)",          TmfTimePreferences.DELIMITER_NONE      }, //$NON-NLS-1$
        { "  (space)",       TmfTimePreferences.DELIMITER_SPACE     }, //$NON-NLS-1$
        { ", (comma)",       TmfTimePreferences.DELIMITER_COMMA     }, //$NON-NLS-1$
        { "- (dash)",        TmfTimePreferences.DELIMITER_DASH      }, //$NON-NLS-1$
        { "_ (underline)",   TmfTimePreferences.DELIMITER_UNDERLINE }, //$NON-NLS-1$
        { ": (colon)",       TmfTimePreferences.DELIMITER_COLON     }, //$NON-NLS-1$
        { "; (semicolon)",   TmfTimePreferences.DELIMITER_SEMICOLON }, //$NON-NLS-1$
        { "/ (slash)",       TmfTimePreferences.DELIMITER_SLASH     }, //$NON-NLS-1$
        { "\" (dbl-quote)",  TmfTimePreferences.DELIMITER_DQUOT     }, //$NON-NLS-1$
    };

    // Sub-Second delimiters
    private static final String[][] fSubSecondDelimiters = new String[][] {
        { "(none)",          TmfTimePreferences.DELIMITER_NONE      }, //$NON-NLS-1$
        { "  (space)",       TmfTimePreferences.DELIMITER_SPACE     }, //$NON-NLS-1$
        { ", (comma)",       TmfTimePreferences.DELIMITER_COMMA     }, //$NON-NLS-1$
        { "- (dash)",        TmfTimePreferences.DELIMITER_DASH      }, //$NON-NLS-1$
        { "_ (underline)",   TmfTimePreferences.DELIMITER_UNDERLINE }, //$NON-NLS-1$
        { ": (colon)",       TmfTimePreferences.DELIMITER_COLON     }, //$NON-NLS-1$
        { "; (semicolon)",   TmfTimePreferences.DELIMITER_SEMICOLON }, //$NON-NLS-1$
        { "/ (slash)",       TmfTimePreferences.DELIMITER_SLASH     }, //$NON-NLS-1$
        { "\" (dbl-quote)",  TmfTimePreferences.DELIMITER_DQUOT     }, //$NON-NLS-1$
        { ". (period)",      TmfTimePreferences.DELIMITER_PERIOD    }, //$NON-NLS-1$
    };

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // General stuff
    private Composite fPage;
    private IPreferenceStore fPreferenceStore;
    private TmfTimePreferences fTimePreference;

    // Example section
    private Composite fExampleSection;
    private Text fPatternDisplay;
    private Text fExampleDisplay;
    private String fTimePattern;

    // Date/Time format section
    private RadioGroupFieldEditor fDateTimeFields;
    private RadioGroupFieldEditor fSSecFields;

    // Delimiters section
    private RadioGroupFieldEditor fDateFieldDelim;
    private RadioGroupFieldEditor fTimeFieldDelim;
    private RadioGroupFieldEditor fSSecFieldDelim;

    // IPropertyChangeListener data
    private String fProperty;
    private String fChangedProperty;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The default constructor
     */
    public TmfTimestampFormatPage() {
        fPreferenceStore = TmfTimePreferences.getPreferenceStore();
        fTimePreference = TmfTimePreferences.getInstance();
    }

    // ------------------------------------------------------------------------
    // IWorkbenchPreferencePage
    // ------------------------------------------------------------------------

    @Override
    public void init(IWorkbench workbench) {
    }

    // ------------------------------------------------------------------------
    // PreferencePage
    // ------------------------------------------------------------------------

    @Override
    protected Control createContents(Composite parent) {

        // Overall preference page layout
        parent.setLayout(new GridLayout());
        fPage = new Composite(parent, SWT.NONE);
        fPage.setLayout(new GridLayout());
        fPage.setLayoutData(new GridData(
                GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL));

        // Example section
        fExampleSection = new Composite(fPage, SWT.NONE);
        fExampleSection.setLayout(new GridLayout(2, false));
        fExampleSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label patternLabel = new Label(fExampleSection, SWT.HORIZONTAL);
        patternLabel.setText("Current Format: "); //$NON-NLS-1$
        fPatternDisplay = new Text(fExampleSection, SWT.BORDER | SWT.READ_ONLY);
        fPatternDisplay.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label exampleLabel = new Label(fExampleSection, SWT.NONE);
        exampleLabel.setText("Sample Display: "); //$NON-NLS-1$
        fExampleDisplay = new Text(fExampleSection, SWT.BORDER | SWT.READ_ONLY);
        fExampleDisplay.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label separator = new Label(fPage, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_NONE);
        separator.setLayoutData(
                new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

        // Date and Time section
        fDateTimeFields = new RadioGroupFieldEditor(
                TmfTimePreferences.DATIME, "Date and Time format", 3, fDateTimeFormats, fPage, true); //$NON-NLS-1$
        fDateTimeFields.setPreferenceStore(fPreferenceStore);
        fDateTimeFields.load();
        fDateTimeFields.setPropertyChangeListener(this);

        // Sub-second section
        fSSecFields = new RadioGroupFieldEditor(
                TmfTimePreferences.SUBSEC, "Sub-second format", 3, fSubSecondFormats, fPage, true); //$NON-NLS-1$
        fSSecFields.setPreferenceStore(fPreferenceStore);
        fSSecFields.load();
        fSSecFields.setPropertyChangeListener(this);

        // Separators section
        fDateFieldDelim = new RadioGroupFieldEditor(
                TmfTimePreferences.DATE_DELIMITER, "Date delimiter", 5, fDateTimeDelimiters, fPage, true); //$NON-NLS-1$
        fDateFieldDelim.setPreferenceStore(fPreferenceStore);
        fDateFieldDelim.load();
        fDateFieldDelim.setPropertyChangeListener(this);

        fTimeFieldDelim = new RadioGroupFieldEditor(
                TmfTimePreferences.TIME_DELIMITER, "Time delimiter", 5, fDateTimeDelimiters, fPage, true); //$NON-NLS-1$
        fTimeFieldDelim.setPreferenceStore(fPreferenceStore);
        fTimeFieldDelim.load();
        fTimeFieldDelim.setPropertyChangeListener(this);

        fSSecFieldDelim = new RadioGroupFieldEditor(
                TmfTimePreferences.SSEC_DELIMITER, "Sub-Second Delimiter", 5, fSubSecondDelimiters, fPage, true); //$NON-NLS-1$
        fSSecFieldDelim.setPreferenceStore(fPreferenceStore);
        fSSecFieldDelim.load();
        fSSecFieldDelim.setPropertyChangeListener(this);

        fTimePreference.initPatterns();
        refresh();
        return fPage;
    }

    @Override
    protected void performDefaults() {
        fDateTimeFields.loadDefault();
        fSSecFields.loadDefault();
        fDateFieldDelim.loadDefault();
        fTimeFieldDelim.loadDefault();
        fSSecFieldDelim.loadDefault();

        fTimePreference.setDefaults();
        fTimePattern = TmfTimePreferences.getTimePattern();
        displayExample();
    }

    @Override
    protected void performApply() {
        fDateTimeFields.store();
        fSSecFields.store();
        fDateFieldDelim.store();
        fTimeFieldDelim.store();
        fSSecFieldDelim.store();

        TmfTimePreferences.setTimePattern(fTimePattern);
        displayExample();
    }

    @Override
    public boolean performOk() {
        performApply();
        return super.performOk();
    }

    // ------------------------------------------------------------------------
    // SelectionListener
    // ------------------------------------------------------------------------

    @Override
    public void widgetSelected(SelectionEvent e) {
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    // ------------------------------------------------------------------------
    // IPropertyChangeListener
    // ------------------------------------------------------------------------

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        Object source = event.getSource();
        Object value = event.getNewValue();
        if (source instanceof RadioGroupFieldEditor && value instanceof String &&
                !(value.equals(fChangedProperty) && source == fProperty))
        {
            fProperty = ((RadioGroupFieldEditor) source).getPreferenceName();
            fChangedProperty = (String) value;
            refresh();
        }
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private void refresh() {
        updatePatterns();
        displayExample();
    }

    void updatePatterns() {
        if (TmfTimePreferences.DATIME.equals(fProperty)) {
            fTimePreference.setDateTimeFormat(fChangedProperty);
        } else if (TmfTimePreferences.SUBSEC.equals(fProperty)) {
            fTimePreference.setSSecFormat(fChangedProperty);
        } else if (TmfTimePreferences.DATE_DELIMITER.equals(fProperty)) {
            fTimePreference.setDateFieldSep(fChangedProperty);
        } else if (TmfTimePreferences.TIME_DELIMITER.equals(fProperty)) {
            fTimePreference.setTimeFieldSep(fChangedProperty);
        } else if (TmfTimePreferences.SSEC_DELIMITER.equals(fProperty)) {
            fTimePreference.setSSecFieldSep(fChangedProperty);
        }
        fTimePreference.updatePatterns();
        fTimePattern = TmfTimePreferences.getTimePattern();
    }

    private void displayExample() {
        long ts = 1332170682500677380L;
        fPatternDisplay.setText(fTimePattern);
        fPatternDisplay.redraw();

        fExampleDisplay.setText(new TmfTimestampFormat(fTimePattern).format(ts));
        fExampleDisplay.redraw();
    }

}
