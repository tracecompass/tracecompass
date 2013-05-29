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

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IFont;

/**
 * Interface for accessing sequence diagram preferences.
 *
 * @version 1.0
 * @author sveyrier
 */
public interface ISDPreferences {

    /**
     * The link font with zoom preference name
     */
    static final String PREF_LINK_FONT = "PREF_LINK_FONT"; //$NON-NLS-1$
    /**
     * The exclude preference time preference name
     */
    static final String PREF_EXCLUDE_EXTERNAL_TIME = "PREF_EXCLUDE_EXTERNAL_TIME"; //$NON-NLS-1$
    /**
     * The use gradient color preferences name
     */
    static final String PREF_USE_GRADIENT = "PREF_USE_GRADIENT"; //$NON-NLS-1$
    /**
     * The lifeline spacing width preference name
     */
    static final String PREF_LIFELINE_WIDTH = "PREF_LIFELINE_WIDTH"; //$NON-NLS-1$
    /**
     * The time compression bar font preference name
     */
    static final String PREF_TIME_COMP = "PREF_TIME_COMP"; //$NON-NLS-1$
    /**
     * The lifeline font preference name
     */
    static final String PREF_LIFELINE = "PREF_LIFELINE"; //$NON-NLS-1$
    /**
     * The frame font preference name
     */
    static final String PREF_FRAME = "PREF_FRAME"; //$NON-NLS-1$
    /**
     * The frame name font preference name
     */
    static final String PREF_FRAME_NAME = "PREF_FRAME_NAME"; //$NON-NLS-1$
    /**
     * The execution occurrence font preference name
     */
    static final String PREF_EXEC = "PREF_EXEC"; //$NON-NLS-1$
    /**
     * The synchronous message font preference name
     */
    static final String PREF_SYNC_MESS = "PREF_SYNC_MESS"; //$NON-NLS-1$
    /**
     * The synchronous message return font preference name
     */
    static final String PREF_SYNC_MESS_RET = "PREF_SYNC_MESS_RET"; //$NON-NLS-1$
    /**
     * The asynchronous message font preference name
     */
    static final String PREF_ASYNC_MESS = "PREF_ASYNC_MESS"; //$NON-NLS-1$
    /**
     * The asynchronous message return font preference name
     */
    static final String PREF_ASYNC_MESS_RET = "PREF_ASYNC_MESS_RET"; //$NON-NLS-1$
    /**
     * The lifeline header font (header = the always visible part of a lifeline)
     */
    static final String PREF_LIFELINE_HEADER = "PREF_LIFELINE_HEADER"; //$NON-NLS-1$
    /**
     * The enable tooltip preference name
     */
    static final String PREF_TOOLTIP = "PREF_TOOLTIP"; //$NON-NLS-1$

    /**
     * Returns the background color for the given preference name (font preference name)
     *
     * @param prefId The preference name
     * @return the color
     */
    IColor getBackGroundColor(String prefId);

    /**
     * Returns the foreground color for the given preference name (font preference name)
     *
     * @param prefId A preference name
     * @return the color
     */
    IColor getForeGroundColor(String prefId);

    /**
     * Returns the font color for the given preference name (font preference name)
     *
     * @param prefId A preference name
     * @return the color
     */
    IColor getFontColor(String prefId);

    /**
     * Returns the font for the given preference name
     *
     * @param prefId the preference name
     * @return the font
     */
    IFont getFont(String prefId);

    /**
     * Returns the time compression bar selection color
     *
     * @return the time compression bar selection color
     */
    IColor getTimeCompressionSelectionColor();

    /**
     * Returns the background color used to draw selection
     *
     * @return the background color
     */
    IColor getBackGroundColorSelection();

    /**
     * Returns the foreground color used to draw selection
     *
     * @return the foreground color
     */
    IColor getForeGroundColorSelection();

    /**
     * Returns whether to use gradient color or not
     *
     * @return whether to use gradient color or not
     */
    boolean useGradienColor();

}
