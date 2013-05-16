/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.colors;

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
