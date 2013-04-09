/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IFont;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.impl.ColorImpl;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.impl.FontImpl;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * This is the Sequence Diagram preference handler. This class is responsible for accessing the current user preferences
 * selection This class also provider getters for each modifiable preferences.
 *
 * @version 1.0
 * @author sveyrier
 */
public class SDViewPref implements ISDPreferences, IPropertyChangeListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Postfix string for background color property
     */
    public static final String BACK_COLOR_POSTFIX = "_BACK_COLOR";//$NON-NLS-1$
    /**
     * Postfix string for foreground color property
     */
    public static final String FORE_COLOR_POSTFIX = "_FORE_COLOR";//$NON-NLS-1$
    /**
     * Postfix string for text color property
     */
    public static final String TEXT_COLOR_POSTFIX = "_TEXT_COLOR";//$NON-NLS-1$
    /**
     * Array of preference names
     */
    private static final String[] FONT_LIST = { PREF_LIFELINE, PREF_EXEC, PREF_SYNC_MESS, PREF_SYNC_MESS_RET, PREF_ASYNC_MESS, PREF_ASYNC_MESS_RET, PREF_FRAME, PREF_LIFELINE_HEADER, PREF_FRAME_NAME };
    /**
     * A 2nd array of preference names
     */
    private static final String[] FONT_LIST2 = { SDMessages._88, SDMessages._89, SDMessages._90, SDMessages._91, SDMessages._92, SDMessages._93, SDMessages._94, SDMessages._95, SDMessages._96 };
    /**
     * Array of background color preference names
     */
    private static final String[] PREF_BACK_COLOR_LIST = { PREF_LIFELINE, PREF_EXEC, PREF_FRAME, PREF_LIFELINE_HEADER, PREF_FRAME_NAME };
    /**
     * Array of foreground color preference names
     */
    private static final String[] PREF_FORE_COLOR_LIST = { PREF_LIFELINE, PREF_EXEC, PREF_SYNC_MESS, PREF_SYNC_MESS_RET, PREF_ASYNC_MESS, PREF_ASYNC_MESS_RET, PREF_FRAME, PREF_LIFELINE_HEADER, PREF_FRAME_NAME };
    /**
     * Array of text color preference names
     */
    private static final String[] PREF_TEXT_COLOR_LIST = { PREF_LIFELINE, PREF_SYNC_MESS, PREF_SYNC_MESS_RET, PREF_ASYNC_MESS, PREF_ASYNC_MESS_RET, PREF_LIFELINE_HEADER, PREF_FRAME_NAME };
    /**
     * Temporary tag
     */
    protected static final String TEMP_TAG = "_TEMP";//$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The sequence diagram preferences singleton instance
     */
    private static SDViewPref fHandle = null;
    /**
     * Hashtable for font preferences
     */
    protected Map<String, IFont> fFontPref;
    /**
     * Hashtable for foreground color preferences
     */
    protected Map<String, IColor> fForeColorPref;
    /**
     * Hashtable for background color preferences
     */
    protected Map<String, IColor> fBackColorPref;
    /**
     * Hashtable for text color preferences
     */
    protected Map<String, IColor> fTextColorPref;
    /**
     * The reference to the preference store.
     */
    protected IPreferenceStore fPrefStore = null;
    /**
     * Color for the time compression selection
     */
    protected IColor fTimeCompressionSelectionColor = null;
    /**
     * Flag whether no focus selection or not.
     */
    protected boolean fNoFocusSelection = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Builds the Sequence Diagram preference handler: - Define the preference default values. - Load the currently used
     * preferences setting
     */
    protected SDViewPref() {
        fPrefStore = Activator.getDefault().getPreferenceStore();

        fPrefStore.setDefault(PREF_LINK_FONT, true);
        fPrefStore.setDefault(PREF_EXCLUDE_EXTERNAL_TIME, true);
        fPrefStore.setDefault(PREF_LIFELINE_WIDTH, 200);
        fPrefStore.setDefault(PREF_USE_GRADIENT, true);
        fPrefStore.setDefault(PREF_TOOLTIP, true);

        fFontPref = new Hashtable<String, IFont>();
        fForeColorPref = new Hashtable<String, IColor>();
        fBackColorPref = new Hashtable<String, IColor>();
        fTextColorPref = new Hashtable<String, IColor>();

        for (int i = 0; i < FONT_LIST.length; i++) {
            if (FONT_LIST[i].equals(PREF_FRAME_NAME)) {
                FontData[] data = Display.getDefault().getSystemFont().getFontData();
                data[0].setStyle(SWT.BOLD);
                PreferenceConverter.setDefault(fPrefStore, FONT_LIST[i], data[0]);
                PreferenceConverter.setDefault(fPrefStore, FONT_LIST[i] + TEMP_TAG, data[0]);
            } else {
                PreferenceConverter.setDefault(fPrefStore, FONT_LIST[i], Display.getDefault().getSystemFont().getFontData());
                PreferenceConverter.setDefault(fPrefStore, FONT_LIST[i] + TEMP_TAG, Display.getDefault().getSystemFont().getFontData());
            }
        }

        for (int i = 0; i < PREF_BACK_COLOR_LIST.length; i++) {
            IColor color;
            if ((PREF_BACK_COLOR_LIST[i].equals(PREF_EXEC)) || PREF_BACK_COLOR_LIST[i].equals(PREF_FRAME_NAME)) {
                color = new ColorImpl(Display.getDefault(), 201, 222, 233);
            } else if (PREF_BACK_COLOR_LIST[i].equals(PREF_LIFELINE)) {
                color = new ColorImpl(Display.getDefault(), 220, 220, 220);
            } else if (PREF_BACK_COLOR_LIST[i].equals(PREF_LIFELINE_HEADER)) {
                color = new ColorImpl(Display.getDefault(), 245, 244, 244);
            } else {
                color = new ColorImpl(Display.getDefault(), 255, 255, 255);
            }
            PreferenceConverter.setDefault(fPrefStore, PREF_BACK_COLOR_LIST[i] + BACK_COLOR_POSTFIX, ((Color) color.getColor()).getRGB());
            PreferenceConverter.setDefault(fPrefStore, PREF_BACK_COLOR_LIST[i] + BACK_COLOR_POSTFIX + TEMP_TAG, ((Color) color.getColor()).getRGB());
            color.dispose();
        }

        for (int i = 0; i < PREF_FORE_COLOR_LIST.length; i++) {
            IColor color;
            if (PREF_FORE_COLOR_LIST[i].equals(PREF_LIFELINE)) {
                color = new ColorImpl(Display.getDefault(), 129, 129, 129);
            } else if (PREF_FORE_COLOR_LIST[i].equals(PREF_FRAME_NAME)) {
                color = new ColorImpl(Display.getDefault(), 81, 153, 200);
            } else if (PREF_FORE_COLOR_LIST[i].equals(PREF_LIFELINE_HEADER)) {
                color = new ColorImpl(Display.getDefault(), 129, 127, 137);
            } else {
                color = new ColorImpl(Display.getDefault(), 134, 176, 212);
            }
            PreferenceConverter.setDefault(fPrefStore, PREF_FORE_COLOR_LIST[i] + FORE_COLOR_POSTFIX, ((Color) color.getColor()).getRGB());
            PreferenceConverter.setDefault(fPrefStore, PREF_FORE_COLOR_LIST[i] + FORE_COLOR_POSTFIX + TEMP_TAG, ((Color) color.getColor()).getRGB());
            color.dispose();
        }

        for (int i = 0; i < PREF_TEXT_COLOR_LIST.length; i++) {
            IColor color;
            if (PREF_TEXT_COLOR_LIST[i].equals(PREF_LIFELINE)) {
                color = new ColorImpl(Display.getDefault(), 129, 129, 129);
            } else if (PREF_TEXT_COLOR_LIST[i].equals(PREF_FRAME_NAME)) {
                color = new ColorImpl(Display.getDefault(), 0, 0, 0);
            } else if (PREF_TEXT_COLOR_LIST[i].equals(PREF_LIFELINE_HEADER)) {
                color = new ColorImpl(Display.getDefault(), 129, 127, 137);
            } else {
                color = new ColorImpl(Display.getDefault(), 134, 176, 212);
            }
            PreferenceConverter.setDefault(fPrefStore, PREF_TEXT_COLOR_LIST[i] + TEXT_COLOR_POSTFIX, ((Color) color.getColor()).getRGB());
            PreferenceConverter.setDefault(fPrefStore, PREF_TEXT_COLOR_LIST[i] + TEXT_COLOR_POSTFIX + TEMP_TAG, ((Color) color.getColor()).getRGB());
            color.dispose();
        }

        IColor color = new ColorImpl(Display.getDefault(), 218, 232, 238);
        PreferenceConverter.setDefault(fPrefStore, PREF_TIME_COMP, ((Color) color.getColor()).getRGB());
        color.dispose();

        buildFontsAndColors();

        fPrefStore.addPropertyChangeListener(this);
    }

    /**
     * Returns the PreferenceStore
     *
     * @return the PreferenceStore
     */
    public IPreferenceStore getPreferenceStore() {
        return fPrefStore;
    }

    /**
     * Apply the preferences in the preferences handler
     */
    public void apply() {
        buildFontsAndColors();
        fPrefStore.firePropertyChangeEvent("PREFOK", null, null); //$NON-NLS-1$
    }

    /**
     * Returns an unique instance of the Sequence Diagram preference handler
     *
     * @return the preference handler instance
     */
    public static synchronized SDViewPref getInstance() {
        if (fHandle == null) {
            fHandle = new SDViewPref();
        }
        return fHandle;
    }

    @Override
    public IColor getForeGroundColor(String prefName) {
        if ((fForeColorPref.get(prefName + FORE_COLOR_POSTFIX) != null) && (fForeColorPref.get(prefName + FORE_COLOR_POSTFIX) instanceof ColorImpl)) {
            return fForeColorPref.get(prefName + FORE_COLOR_POSTFIX);
        }
        return ColorImpl.getSystemColor(SWT.COLOR_BLACK);
    }

    @Override
    public IColor getBackGroundColor(String prefName) {
        if ((fBackColorPref.get(prefName + BACK_COLOR_POSTFIX) != null) && (fBackColorPref.get(prefName + BACK_COLOR_POSTFIX) instanceof ColorImpl)) {
            return fBackColorPref.get(prefName + BACK_COLOR_POSTFIX);
        }
        return ColorImpl.getSystemColor(SWT.COLOR_WHITE);
    }

    @Override
    public IColor getFontColor(String prefName) {
        if ((fTextColorPref.get(prefName + TEXT_COLOR_POSTFIX) != null) && (fTextColorPref.get(prefName + TEXT_COLOR_POSTFIX) instanceof ColorImpl)) {
            return fTextColorPref.get(prefName + TEXT_COLOR_POSTFIX);
        }
        return ColorImpl.getSystemColor(SWT.COLOR_BLACK);
    }

    @Override
    public IColor getForeGroundColorSelection() {
        if (fNoFocusSelection) {
            return ColorImpl.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
        }
        return ColorImpl.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
    }

    @Override
    public IColor getBackGroundColorSelection() {
        if (fNoFocusSelection) {
            return ColorImpl.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        }
        return ColorImpl.getSystemColor(SWT.COLOR_LIST_SELECTION);
    }

    @Override
    public IFont getFont(String prefName) {
        if (fFontPref.get(prefName) != null) {
            return fFontPref.get(prefName);
        }
        return FontImpl.getSystemFont();
    }

    /**
     * Returns the SwimLane width chosen
     *
     * @return the SwimLane width
     */
    public int getLifelineWidth() {
        return fPrefStore.getInt(PREF_LIFELINE_WIDTH);
    }

    /**
     * Returns if font linkage with zoom has been chosen
     *
     * @return true if checked false otherwise
     */
    public boolean fontLinked() {
        return fPrefStore.getBoolean(PREF_LINK_FONT);
    }

    /**
     * Returns the tooltip enablement
     *
     * @return true if checked false otherwise
     */
    public boolean tooltipEnabled() {
        return fPrefStore.getBoolean(PREF_TOOLTIP);
    }

    /**
     * Return true if the user do not want to take external time (basically found and lost messages with time) into
     * account in the min max computation
     *
     * @return true if checked false otherwise
     */
    public boolean excludeExternalTime() {
        return fPrefStore.getBoolean(PREF_EXCLUDE_EXTERNAL_TIME);
    }

    @Override
    public boolean useGradienColor() {
        return fPrefStore.getBoolean(PREF_USE_GRADIENT);
    }

    @Override
    public IColor getTimeCompressionSelectionColor() {
        return fTimeCompressionSelectionColor;
    }

    /**
     * Builds the new colors and fonts according the current user selection when the OK or Apply button is clicked
     */
    private void buildFontsAndColors() {

        Display display = Display.getDefault();

        for (int i = 0; i < FONT_LIST.length; i++) {
            FontData fontData = PreferenceConverter.getFontData(fPrefStore, FONT_LIST[i]);
            if (fFontPref.get(FONT_LIST[i]) != null) {
                fFontPref.get(FONT_LIST[i]).dispose();
            }
            fFontPref.put(FONT_LIST[i], new FontImpl(display, fontData));
        }

        for (int i = 0; i < PREF_BACK_COLOR_LIST.length; i++) {
            RGB rgb = PreferenceConverter.getColor(fPrefStore, PREF_BACK_COLOR_LIST[i] + BACK_COLOR_POSTFIX);
            if (fBackColorPref.get(PREF_BACK_COLOR_LIST[i] + BACK_COLOR_POSTFIX) != null) {
                fBackColorPref.get(PREF_BACK_COLOR_LIST[i] + BACK_COLOR_POSTFIX).dispose();
            }
            fBackColorPref.put(PREF_BACK_COLOR_LIST[i] + BACK_COLOR_POSTFIX, new ColorImpl(display, rgb.red, rgb.green, rgb.blue));
        }

        for (int i = 0; i < PREF_FORE_COLOR_LIST.length; i++) {
            RGB rgb = PreferenceConverter.getColor(fPrefStore, PREF_FORE_COLOR_LIST[i] + FORE_COLOR_POSTFIX);
            if (fForeColorPref.get(PREF_FORE_COLOR_LIST[i] + FORE_COLOR_POSTFIX) != null) {
                fForeColorPref.get(PREF_FORE_COLOR_LIST[i] + FORE_COLOR_POSTFIX).dispose();
            }
            fForeColorPref.put(PREF_FORE_COLOR_LIST[i] + FORE_COLOR_POSTFIX, new ColorImpl(display, rgb.red, rgb.green, rgb.blue));
        }

        for (int i = 0; i < PREF_TEXT_COLOR_LIST.length; i++) {
            RGB rgb = PreferenceConverter.getColor(fPrefStore, PREF_TEXT_COLOR_LIST[i] + TEXT_COLOR_POSTFIX);
            if (fTextColorPref.get(PREF_TEXT_COLOR_LIST[i] + TEXT_COLOR_POSTFIX) != null) {
                fTextColorPref.get(PREF_TEXT_COLOR_LIST[i] + TEXT_COLOR_POSTFIX).dispose();
            }
            fTextColorPref.put(PREF_TEXT_COLOR_LIST[i] + TEXT_COLOR_POSTFIX, new ColorImpl(display, rgb.red, rgb.green, rgb.blue));
        }

        RGB rgb = PreferenceConverter.getColor(fPrefStore, PREF_TIME_COMP);
        if (fTimeCompressionSelectionColor != null) {
            fTimeCompressionSelectionColor.dispose();
        }
        fTimeCompressionSelectionColor = new ColorImpl(display, rgb.red, rgb.green, rgb.blue);
    }

    /**
     * Add a property-change listener
     *
     * @param listener
     *            The listener to add
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        fPrefStore.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property-change listener
     *
     * @param listener
     *            The listerner to remove
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        fPrefStore.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!event.getProperty().equals("PREFOK")) { //$NON-NLS-1$
            buildFontsAndColors();
            fPrefStore.firePropertyChangeEvent("PREFOK", null, null); //$NON-NLS-1$
        }
    }

    /**
     * Set the "no focus selection" preference
     *
     * @param v
     *            New value to use
     */
    public void setNoFocusSelection(boolean v) {
        fNoFocusSelection = v;
    }

    /**
     * Returns the static font list.
     *
     * @return static font list
     */
    public static String[] getFontList() {
        return Arrays.copyOf(FONT_LIST, FONT_LIST.length);
    }

    /**
     * Returns the 2nd static font list.
     *
     * @return 2nd static font list
     */
    public static String[] getFontList2() {
        return Arrays.copyOf(FONT_LIST2, FONT_LIST2.length);
    }

    /**
     * Returns the preference background color list.
     *
     * @return preference background color list
     */
    public static String[] getPrefBackColorList() {
        return Arrays.copyOf(PREF_BACK_COLOR_LIST, PREF_BACK_COLOR_LIST.length);
    }

    /**
     * Returns the preference foreground color list.
     *
     * @return preference foreground color list
     */
    public static String[] getPrefForeColorList() {
        return Arrays.copyOf(PREF_FORE_COLOR_LIST, PREF_FORE_COLOR_LIST.length);
    }

    /**
     * Returns the preference text color list color list.
     *
     * @return preference text color list color list
     */
    public static String[] getPrefTextColorList() {
        return Arrays.copyOf(PREF_TEXT_COLOR_LIST, PREF_TEXT_COLOR_LIST.length);
    }
}
