/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.colors;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.tmf.core.dataprovider.X11ColorUtils;

/**
 * A registry of named X11 colors. It maps certain strings to RGB color values.
 *
 * @see <a href=
 *      "http://cgit.freedesktop.org/xorg/app/rgb/tree/rgb.txt">http://cgit.freedesktop.org/xorg/app/rgb/tree/rgb.txt</a>
 *
 * @since 3.0
 * @deprecated Use {@link X11ColorUtils} instead
 */
@Deprecated
public class X11Color {

    /**
     * Get the RGB corresponding to a X11 color name.
     *
     * @param name
     *            the X11 color name (case insensitive)
     * @return the corresponding RGB, or null
     */
    public static RGB toRGB(String name) {
        return ColorUtils.fromX11Color(name);
    }
}
