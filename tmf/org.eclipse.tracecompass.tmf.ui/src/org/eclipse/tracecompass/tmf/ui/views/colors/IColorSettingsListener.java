/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.colors;

/**
 * A color change listener
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public interface IColorSettingsListener {

    /**
     * Notify the listener that the color settings have changed.
     *
     * @param colorSettings
     *            The new color settings
     */
    void colorSettingsChanged(ColorSetting[] colorSettings);
}
