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
import org.eclipse.linuxtools.internal.tmf.ui.Activator;

/**
 * TMF Time format preferences
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.0
 * @deprecated Use {@link org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences} instead.
 */
@SuppressWarnings("javadoc")
@Deprecated
public class TmfTimePreferences {

    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss.SSS_CCC_NNN"; //$NON-NLS-1$

    private static TmfTimePreferences fPreferences;

    /**
     * @deprecated Use {@link org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences#init} instead.
     */
    @Deprecated
    public static void init() {
        org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences.init();
    }

    /**
     * @deprecated Use {@link org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences} instead.
     */
    @Deprecated
    public static synchronized IPreferenceStore getPreferenceStore() {
        init();
        return Activator.getDefault().getCorePreferenceStore();
    }

    /**
     * @deprecated Use {@link org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences#getInstance} instead.
     */
    @Deprecated
    public static synchronized TmfTimePreferences getInstance() {
        if (fPreferences == null) {
            fPreferences = new TmfTimePreferences();
        }
        return fPreferences;
    }

    /**
     * @return the timestamp pattern
     * @deprecated Use {@link org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences#getTimePattern} instead.
     */
    @Deprecated
    public static String getTimePattern() {
        return org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences.getInstance().getTimePattern();
    }

}
