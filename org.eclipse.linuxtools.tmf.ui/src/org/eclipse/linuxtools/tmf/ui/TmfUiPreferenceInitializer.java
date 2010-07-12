/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class TmfUiPreferenceInitializer extends AbstractPreferenceInitializer {

    public static final String ACTIVE_PROJECT_PREFERENCE = "ACTIVE_PROJECT";
    public static final String ACTIVE_PROJECT_DEFAULT = "";
    
    public TmfUiPreferenceInitializer() {
        super();
    }

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences node = new InstanceScope().getNode(TmfUiPlugin.PLUGIN_ID);
        node.put(ACTIVE_PROJECT_PREFERENCE, ACTIVE_PROJECT_DEFAULT);
    }

}
