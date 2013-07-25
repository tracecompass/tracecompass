/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francois Chouinard - Initial API and implementation
 *     Marc-Andre Laperle - Add time zone preference
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.properties;

import java.util.Map;
import java.util.TimeZone;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences;
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
 * The TMF timestamp format configuration page. This page is used to select the
 * global timestamp and interval time formats (for display and parsing). The
 * user can either pick a pre-defined format or enter his/her own.
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
            { ITmfTimePreferencesConstants.DATE_YEAR_FMT, ITmfTimePreferencesConstants.DATE_YEAR_FMT },
            { ITmfTimePreferencesConstants.DATE_YEAR2_FMT, ITmfTimePreferencesConstants.DATE_YEAR2_FMT },
            { ITmfTimePreferencesConstants.DATE_MONTH_FMT, ITmfTimePreferencesConstants.DATE_MONTH_FMT },
            { ITmfTimePreferencesConstants.DATE_DAY_FMT, ITmfTimePreferencesConstants.DATE_DAY_FMT },
            { ITmfTimePreferencesConstants.DATE_JDAY_FMT, ITmfTimePreferencesConstants.DATE_JDAY_FMT },
            { ITmfTimePreferencesConstants.TIME_HOUR_FMT, ITmfTimePreferencesConstants.TIME_HOUR_FMT },
            { ITmfTimePreferencesConstants.TIME_MINUTE_FMT, ITmfTimePreferencesConstants.TIME_MINUTE_FMT },
            { ITmfTimePreferencesConstants.TIME_SECOND_FMT, ITmfTimePreferencesConstants.TIME_SECOND_FMT },
            { ITmfTimePreferencesConstants.TIME_ELAPSED_FMT + " (secs in epoch)", ITmfTimePreferencesConstants.TIME_ELAPSED_FMT }, //$NON-NLS-1$
            { "(none)", ITmfTimePreferencesConstants.TIME_NO_FMT }, //$NON-NLS-1$
    };

    // Sub-second formats
    private static final String[][] fSubSecondFormats = new String[][] {
            { ITmfTimePreferencesConstants.SUBSEC_MILLI_FMT + " (ms)", ITmfTimePreferencesConstants.SUBSEC_MILLI_FMT }, //$NON-NLS-1$
            { ITmfTimePreferencesConstants.SUBSEC_MICRO_FMT + " (µs)", ITmfTimePreferencesConstants.SUBSEC_MICRO_FMT }, //$NON-NLS-1$
            { ITmfTimePreferencesConstants.SUBSEC_NANO_FMT + " (ns)", ITmfTimePreferencesConstants.SUBSEC_NANO_FMT }, //$NON-NLS-1$
    };

    // Date and Time delimiters
    private static final String[][] fDateTimeDelimiters = new String[][] {
            { "(none)", ITmfTimePreferencesConstants.DELIMITER_NONE }, //$NON-NLS-1$
            { "  (space)", ITmfTimePreferencesConstants.DELIMITER_SPACE }, //$NON-NLS-1$
            { ", (comma)", ITmfTimePreferencesConstants.DELIMITER_COMMA }, //$NON-NLS-1$
            { "- (dash)", ITmfTimePreferencesConstants.DELIMITER_DASH }, //$NON-NLS-1$
            { "_ (underline)", ITmfTimePreferencesConstants.DELIMITER_UNDERLINE }, //$NON-NLS-1$
            { ": (colon)", ITmfTimePreferencesConstants.DELIMITER_COLON }, //$NON-NLS-1$
            { "; (semicolon)", ITmfTimePreferencesConstants.DELIMITER_SEMICOLON }, //$NON-NLS-1$
            { "/ (slash)", ITmfTimePreferencesConstants.DELIMITER_SLASH }, //$NON-NLS-1$
            { "\" (dbl-quote)", ITmfTimePreferencesConstants.DELIMITER_DQUOT }, //$NON-NLS-1$
    };

    // Sub-Second delimiters
    private static final String[][] fSubSecondDelimiters = new String[][] {
            { "(none)", ITmfTimePreferencesConstants.DELIMITER_NONE }, //$NON-NLS-1$
            { "  (space)", ITmfTimePreferencesConstants.DELIMITER_SPACE }, //$NON-NLS-1$
            { ", (comma)", ITmfTimePreferencesConstants.DELIMITER_COMMA }, //$NON-NLS-1$
            { "- (dash)", ITmfTimePreferencesConstants.DELIMITER_DASH }, //$NON-NLS-1$
            { "_ (underline)", ITmfTimePreferencesConstants.DELIMITER_UNDERLINE }, //$NON-NLS-1$
            { ": (colon)", ITmfTimePreferencesConstants.DELIMITER_COLON }, //$NON-NLS-1$
            { "; (semicolon)", ITmfTimePreferencesConstants.DELIMITER_SEMICOLON }, //$NON-NLS-1$
            { "/ (slash)", ITmfTimePreferencesConstants.DELIMITER_SLASH }, //$NON-NLS-1$
            { "\" (dbl-quote)", ITmfTimePreferencesConstants.DELIMITER_DQUOT }, //$NON-NLS-1$
            { ". (period)", ITmfTimePreferencesConstants.DELIMITER_PERIOD }, //$NON-NLS-1$
    };

    // Time zones
    @SuppressWarnings("nls")
    private static final String[] timeZones = new String[] {
            Messages.TmfTimestampFormatPage_LocalTime,
            "GMT-12",
            "GMT-11",
            "GMT-10",
            "GMT-9:30",
            "GMT-9",
            "GMT-7",
            "GMT-6",
            "GMT-5",
            "GMT-4",
            "GMT-3:30",
            "GMT-3",
            "GMT-2",
            "GMT-1",
            "GMT",
            "GMT+1",
            "GMT+2",
            "GMT+3",
            "GMT+3:30",
            "GMT+4",
            "GMT+4:30",
            "GMT+5",
            "GMT+5:30",
            "GMT+6",
            "GMT+7",
            "GMT+8",
            "GMT+9",
            "GMT+9:30",
            "GMT+10",
            "GMT+10:30",
            "GMT+11",
            "GMT+11:30",
            "GMT+12",
            "GMT+13:00",
            "GMT+14:00"
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

    // Timezone section
    private ComboFieldEditor fCombo;

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

    private Map<String, String> fPreferenceMap;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The default constructor
     */
    public TmfTimestampFormatPage() {
        fPreferenceStore = getPreferenceStore();
        fTimePreference = TmfTimePreferences.getInstance();
        fPreferenceMap = fTimePreference.getPreferenceMap();
    }

    // ------------------------------------------------------------------------
    // IWorkbenchPreferencePage
    // ------------------------------------------------------------------------

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return Activator.getDefault().getCorePreferenceStore();
    }

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

        // Time Zones
        String[][] timeZoneIntervals = new String[timeZones.length][2];
        timeZoneIntervals[0][0] = timeZones[0];
        timeZoneIntervals[0][1] = fPreferenceStore.getDefaultString(ITmfTimePreferencesConstants.TIME_ZONE);
        for (int i = 1; i < timeZones.length; i++) {
            TimeZone tz = null;
            try {
                tz = TimeZone.getTimeZone(timeZones[i]);
                timeZoneIntervals[i][0] = tz.getDisplayName();
                timeZoneIntervals[i][1] = tz.getID();
            } catch (NullPointerException e) {
                System.out.println("TimeZone " + timeZones[i] + " does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        fCombo = new ComboFieldEditor(ITmfTimePreferencesConstants.TIME_ZONE, "Time Zone", timeZoneIntervals, fPage); //$NON-NLS-1$
        fCombo.setPreferenceStore(fPreferenceStore);
        fCombo.load();
        fCombo.setPropertyChangeListener(this);

        // Date and Time section
        fDateTimeFields = new RadioGroupFieldEditor(
                ITmfTimePreferencesConstants.DATIME, "Date and Time format", 3, fDateTimeFormats, fPage, true); //$NON-NLS-1$
        fDateTimeFields.setPreferenceStore(fPreferenceStore);
        fDateTimeFields.load();
        fDateTimeFields.setPropertyChangeListener(this);

        // Sub-second section
        fSSecFields = new RadioGroupFieldEditor(
                ITmfTimePreferencesConstants.SUBSEC, "Sub-second format", 3, fSubSecondFormats, fPage, true); //$NON-NLS-1$
        fSSecFields.setPreferenceStore(fPreferenceStore);
        fSSecFields.load();
        fSSecFields.setPropertyChangeListener(this);

        // Separators section
        fDateFieldDelim = new RadioGroupFieldEditor(
                ITmfTimePreferencesConstants.DATE_DELIMITER, "Date delimiter", 5, fDateTimeDelimiters, fPage, true); //$NON-NLS-1$
        fDateFieldDelim.setPreferenceStore(fPreferenceStore);
        fDateFieldDelim.load();
        fDateFieldDelim.setPropertyChangeListener(this);

        fTimeFieldDelim = new RadioGroupFieldEditor(
                ITmfTimePreferencesConstants.TIME_DELIMITER, "Time delimiter", 5, fDateTimeDelimiters, fPage, true); //$NON-NLS-1$
        fTimeFieldDelim.setPreferenceStore(fPreferenceStore);
        fTimeFieldDelim.load();
        fTimeFieldDelim.setPropertyChangeListener(this);

        fSSecFieldDelim = new RadioGroupFieldEditor(
                ITmfTimePreferencesConstants.SSEC_DELIMITER, "Sub-Second Delimiter", 5, fSubSecondDelimiters, fPage, true); //$NON-NLS-1$
        fSSecFieldDelim.setPreferenceStore(fPreferenceStore);
        fSSecFieldDelim.load();
        fSSecFieldDelim.setPropertyChangeListener(this);

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
        fCombo.loadDefault();

        fPreferenceMap = TmfTimePreferences.getInstance().getDefaultPreferenceMap();
        displayExample();
    }

    @Override
    protected void performApply() {
        fDateTimeFields.store();
        fSSecFields.store();
        fDateFieldDelim.store();
        fTimeFieldDelim.store();
        fSSecFieldDelim.store();
        fCombo.store();

        TmfTimestampFormat.updateDefaultFormats();
        TmfSignalManager.dispatchSignal(new TmfTimestampFormatUpdateSignal(null));
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
        if (ITmfTimePreferencesConstants.DATIME.equals(fProperty) ||
                ITmfTimePreferencesConstants.SUBSEC.equals(fProperty) ||
                ITmfTimePreferencesConstants.DATE_DELIMITER.equals(fProperty) ||
                ITmfTimePreferencesConstants.TIME_DELIMITER.equals(fProperty) ||
                ITmfTimePreferencesConstants.SSEC_DELIMITER.equals(fProperty)) {
            fPreferenceMap.put(fProperty, fChangedProperty);
        }
    }

    private void displayExample() {
        long ts = 1332170682500677380L;
        String timePattern = fTimePreference.computeTimePattern(fPreferenceMap);
        fPatternDisplay.setText(timePattern);
        fPatternDisplay.redraw();

        fExampleDisplay.setText(new TmfTimestampFormat(timePattern).format(ts));
        fExampleDisplay.redraw();
    }

}
