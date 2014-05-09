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
package org.eclipse.tracecompass.internal.tmf.remote.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * A class to initialize the preferences.
 *
 * @author Bernd Hufmann
 */
public class TmfRemotePreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        TmfRemotePreferences.init();
    }
}
