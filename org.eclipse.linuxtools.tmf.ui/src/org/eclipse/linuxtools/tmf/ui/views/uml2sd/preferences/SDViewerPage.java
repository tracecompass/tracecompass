/**********************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 * 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The Sequence Diagram preferences page implementation.
 * 
 * @version 1.0
 * @author sveyrier
 */
public class SDViewerPage extends PreferencePage implements IWorkbenchPreferencePage, SelectionListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Temporary preferences tag
     */
    protected static final String TEMP_TAG = SDViewPref.TEMP_TAG;
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The preference handler used to access the PreferenceStore
     */
    protected SDViewPref pref = null;
    /**
     * BackGround color selector
     */
    protected ColorFieldEditor lineColor = null;
    /**
     * Foreground color selector
     */
    protected ColorFieldEditor backGroundColor = null;
    /**
     * Font color selector
     */
    protected ColorFieldEditor textColor = null;
    /**
     * List which display all modifiable sequence Diagram font
     */
    protected List classItemList = null;
    /**
     * Font selector (The same is used for each modifiable font)
     */
    protected FontFieldEditor font = null;
    /**
     * Link font when zooming selector
     */
    protected BooleanFieldEditor link = null;
    /**
     * Enable tooltip selector
     */
    protected BooleanFieldEditor tooltip = null;
    /**
     * Do not take external time into account in the min max computation
     */
    protected BooleanFieldEditor noExternalTime = null;
    /**
     * Use gradient color selector
     */
    protected BooleanFieldEditor useGrad = null;
    /**
     * A button area.
     */
    protected Composite buttonArea;
    /**
     * SwimLane width selector
     */
    protected IntegerFieldEditor lifelineWidth = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public SDViewerPage() {
        super();
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        parent.setLayout(new GridLayout());
        Composite page = new Composite(parent, SWT.NONE);
        GridLayout pageLayout = new GridLayout();
        pageLayout.numColumns = 2;
        GridData pageLayoutdata = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        page.setLayoutData(pageLayoutdata);
        page.setLayout(pageLayout);

        tooltip = new BooleanFieldEditor(SDViewPref.PREF_TOOLTIP, SDMessages._97, page);
        tooltip.setPreferenceStore(pref.getPreferenceStore());
        tooltip.load();

        // link font with zoom pref
        link = new BooleanFieldEditor(SDViewPref.PREF_LINK_FONT, SDMessages._82, page);
        link.setPreferenceStore(pref.getPreferenceStore());
        link.load();

        noExternalTime = new BooleanFieldEditor(SDViewPref.PREF_EXCLUDE_EXTERNAL_TIME, SDMessages._83, page);
        noExternalTime.setPreferenceStore(pref.getPreferenceStore());
        noExternalTime.load();

        // use gradient color pref
        useGrad = new BooleanFieldEditor(SDViewPref.PREF_USE_GRADIENT, SDMessages._84, page);
        useGrad.setPreferenceStore(pref.getPreferenceStore());
        useGrad.load();

        Label separator = new Label(page, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_NONE);
        GridData sepData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        separator.setLayoutData(sepData);

        Composite prefPage = new Composite(page, SWT.NONE);
        GridLayout prefPageLayout = new GridLayout();
        prefPage.setLayoutData(pageLayoutdata);
        prefPageLayout.numColumns = 1;
        prefPage.setLayout(prefPageLayout);

        // swimLane width pref
        lifelineWidth = new IntegerFieldEditor(SDViewPref.PREF_LIFELINE_WIDTH, SDMessages._80, prefPage);
        lifelineWidth.setPreferenceStore(pref.getPreferenceStore());
        lifelineWidth.setValidRange(119, 500);
        lifelineWidth.load();

        // not very nice
        new Label(prefPage, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_NONE);
        new Label(prefPage, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_NONE);

        // Font list pref
        classItemList = new List(prefPage, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData tabItemLayoutdata = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        classItemList.setLayoutData(tabItemLayoutdata);

        String[] fontList2 = SDViewPref.getFontList2();
        for (int i = 0; i < fontList2.length; i++) {
            classItemList.add(fontList2[i]);
        }
        classItemList.setSelection(0);
        classItemList.addSelectionListener(this);
        buttonArea = new Composite(prefPage, SWT.NONE);
        GridData tabItemLayoutdata2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL/* |GridData.GRAB_HORIZONTAL */| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        buttonArea.setLayoutData(tabItemLayoutdata2);
        GridLayout buttonAreaLayout = new GridLayout();
        buttonAreaLayout.numColumns = 1;
        buttonArea.setLayout(buttonAreaLayout);

        // font selector initialise for the lifeline font pref
        String[] fontList = SDViewPref.getFontList();
        font = new FontFieldEditor(fontList[0], "",//$NON-NLS-1$
                SDMessages._81, buttonArea);
        font.getPreviewControl().setSize(500, 500);
        font.setPreferenceStore(pref.getPreferenceStore());
        font.load();

        backGroundColor = new ColorFieldEditor(fontList[0] + SDViewPref.BACK_COLOR_POSTFIX, SDMessages._85, buttonArea);
        backGroundColor.setPreferenceStore(pref.getPreferenceStore());
        backGroundColor.load();

        lineColor = new ColorFieldEditor(fontList[0] + SDViewPref.FORE_COLOR_POSTFIX, SDMessages._86, buttonArea);
        lineColor.setPreferenceStore(pref.getPreferenceStore());
        lineColor.load();

        textColor = new ColorFieldEditor(fontList[0] + SDViewPref.TEXT_COLOR_POSTFIX, SDMessages._87, buttonArea);
        textColor.setPreferenceStore(pref.getPreferenceStore());
        textColor.load();
        swapPref(true);
        Dialog.applyDialogFont(page);

        return page;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        pref = SDViewPref.getInstance();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        // Store the prefrences in the PreferenceStore
        if (!lifelineWidth.isValid()) {
            lifelineWidth.showErrorMessage();
            return;
        }
        font.store();
        backGroundColor.store();
        lineColor.store();
        link.store();
        tooltip.store();
        noExternalTime.store();
        textColor.store();
        useGrad.store();
        lifelineWidth.store();
        swapPref(false);
        // then save them in the preference file
        pref.apply();
        swapPref(true);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        performApply();
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        link.loadDefault();
        tooltip.loadDefault();
        noExternalTime.loadDefault();
        useGrad.loadDefault();
        lifelineWidth.loadDefault();

        // and all the fonts and colors
        // fonts and colors are stored for each time because
        // we are using only one FontFieldEditor
        Set<String> keySet = SDViewPref.getInstance().fontPref.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                font.setPreferenceName((String) prefName);
                font.loadDefault();
                font.setPreferenceName((String) prefName + TEMP_TAG);
                font.store();
            }
        }

        keySet = SDViewPref.getInstance().backColorPref.keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                backGroundColor.setPreferenceName((String) prefName);
                backGroundColor.loadDefault();
                backGroundColor.setPreferenceName((String) prefName + TEMP_TAG);
                backGroundColor.store();
            }

        }

        String[] fontList = SDViewPref.getFontList();
        backGroundColor.setPreferenceName(fontList[classItemList.getSelectionIndex()] + SDViewPref.BACK_COLOR_POSTFIX + TEMP_TAG);
        backGroundColor.load();

        keySet = SDViewPref.getInstance().foreColorPref.keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                lineColor.setPreferenceName((String) prefName);
                lineColor.loadDefault();
                lineColor.setPreferenceName((String) prefName + TEMP_TAG);
                lineColor.store();
            }
        }

        lineColor.setPreferenceName(fontList[classItemList.getSelectionIndex()] + SDViewPref.FORE_COLOR_POSTFIX + TEMP_TAG);
        lineColor.load();

        keySet = SDViewPref.getInstance().textColorPref.keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                textColor.setPreferenceName((String) prefName);
                textColor.loadDefault();
                textColor.setPreferenceName((String) prefName + TEMP_TAG);
                textColor.store();
            }
        }
        textColor.setPreferenceName(fontList[classItemList.getSelectionIndex()] + SDViewPref.TEXT_COLOR_POSTFIX + TEMP_TAG);
        textColor.load();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        // Store the past set font preference or else the
        // FontFieldEditor reassignment will make us loose the current modification
        font.store();
        lineColor.store();
        backGroundColor.store();
        textColor.store();

        String[] fontList = SDViewPref.getFontList();
        
        // set the FontFieldEditor for the new selected graphNode font
        font.setPreferenceName(fontList[classItemList.getSelectionIndex()] + TEMP_TAG);
        font.load();

        backGroundColor.setPreferenceName(fontList[classItemList.getSelectionIndex()] + SDViewPref.BACK_COLOR_POSTFIX + TEMP_TAG);
        backGroundColor.load();

        lineColor.setPreferenceName(fontList[classItemList.getSelectionIndex()] + SDViewPref.FORE_COLOR_POSTFIX + TEMP_TAG);
        lineColor.load();

        textColor.setPreferenceName(fontList[classItemList.getSelectionIndex()] + SDViewPref.TEXT_COLOR_POSTFIX + TEMP_TAG);
        textColor.load();

        // No Background for message graphNodes
        if ((fontList[classItemList.getSelectionIndex()].equals(SDViewPref.PREF_SYNC_MESS)) || (fontList[classItemList.getSelectionIndex()].equals(SDViewPref.PREF_SYNC_MESS_RET))
                || (fontList[classItemList.getSelectionIndex()].equals(SDViewPref.PREF_ASYNC_MESS)) || (fontList[classItemList.getSelectionIndex()].equals(SDViewPref.PREF_ASYNC_MESS_RET))) {
            backGroundColor.setEnabled(false, buttonArea);
        } else {
            backGroundColor.setEnabled(true, buttonArea);
        }

        // No font used for execution occurrence and global frame
        if ((fontList[classItemList.getSelectionIndex()].equals(SDViewPref.PREF_EXEC)) || (fontList[classItemList.getSelectionIndex()].equals(SDViewPref.PREF_FRAME))) {
            textColor.setEnabled(false, buttonArea);
        } else {
            textColor.setEnabled(true, buttonArea);
        }

        if (fontList[classItemList.getSelectionIndex()].equals(SDViewPref.PREF_FRAME)) {
            font.setEnabled(false, buttonArea);
        } else {
            font.setEnabled(true, buttonArea);
        }
    }

    protected void swapPref(boolean toTemp) {
        String TAG1 = "";//$NON-NLS-1$
        String TAG2 = TEMP_TAG;
        if (!toTemp) {
            TAG1 = TEMP_TAG;
            TAG2 = "";//$NON-NLS-1$
        }
        Set<String> keySet = SDViewPref.getInstance().fontPref.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                font.setPreferenceName((String) prefName + TAG1);
                font.load();
                font.setPreferenceName((String) prefName + TAG2);
                font.store();
            }
        }

        keySet = SDViewPref.getInstance().backColorPref.keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                backGroundColor.setPreferenceName((String) prefName + TAG1);
                backGroundColor.load();
                backGroundColor.setPreferenceName((String) prefName + TAG2);
                backGroundColor.store();
            }
        }

        keySet = SDViewPref.getInstance().foreColorPref.keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                lineColor.setPreferenceName((String) prefName + TAG1);
                lineColor.load();
                lineColor.setPreferenceName((String) prefName + TAG2);
                lineColor.store();
            }
        }

        keySet = SDViewPref.getInstance().textColorPref.keySet();
        it = keySet.iterator();
        while (it.hasNext()) {
            Object prefName = it.next();
            if (prefName instanceof String) {
                textColor.setPreferenceName((String) prefName + TAG1);
                textColor.load();
                textColor.setPreferenceName((String) prefName + TAG2);
                textColor.store();
            }
        }
        String[] fontList = SDViewPref.getFontList();
        if (toTemp) {
            // set the FontFieldEditor for the new selected graphNode font
            font.setPreferenceName(fontList[classItemList.getSelectionIndex()] + TEMP_TAG);
            font.load();

            backGroundColor.setPreferenceName(fontList[classItemList.getSelectionIndex()] + SDViewPref.BACK_COLOR_POSTFIX + TEMP_TAG);
            backGroundColor.load();

            lineColor.setPreferenceName(fontList[classItemList.getSelectionIndex()] + SDViewPref.FORE_COLOR_POSTFIX + TEMP_TAG);
            lineColor.load();

            textColor.setPreferenceName(fontList[classItemList.getSelectionIndex()] + SDViewPref.TEXT_COLOR_POSTFIX + TEMP_TAG);
            textColor.load();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }
}
