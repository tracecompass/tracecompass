/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.core.project.model;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypePreferences;

/**
 * Trace type preference initializer
 *
 * @author Jean-Christian Kouame
 * @since 2.3
 *
 */
public class TraceTypePreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        TraceTypePreferences.init();
    }
}
