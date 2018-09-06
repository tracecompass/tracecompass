/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.colors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;

/**
 * A registry of named X11 colors. It maps certain strings to RGB color values.
 *
 * @see <a href=
 *      "http://cgit.freedesktop.org/xorg/app/rgb/tree/rgb.txt">http://cgit.freedesktop.org/xorg/app/rgb/tree/rgb.txt</a>
 *
 * @since 3.0
 */
public class X11Color {

    private static final String X11_COLOR_FILE = "share/rgb.txt"; //$NON-NLS-1$
    private static final Pattern PATTERN = Pattern.compile("\\s*(\\d{1,3})\\s*(\\d{1,3})\\s*(\\d{1,3})\\s*(.*\\S)\\s*"); //$NON-NLS-1$
    private static final Map<String, RGB> COLORS = new HashMap<>();
    static {
        URL url = Activator.getDefault().getBundle().getEntry(X11_COLOR_FILE);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = PATTERN.matcher(line);
                if (matcher.matches()) {
                    int r = Integer.parseInt(matcher.group(1));
                    int g = Integer.parseInt(matcher.group(2));
                    int b = Integer.parseInt(matcher.group(3));
                    RGB rgb = new RGB(r, g, b);
                    COLORS.put(matcher.group(4).toLowerCase(), rgb);
                }
            }
        } catch (IOException e) {
            Activator.getDefault().logError("Cannot read the file from URL", e); //$NON-NLS-1$
        }
    }

    /**
     * Get the RGB corresponding to a X11 color name.
     *
     * @param name
     *            the X11 color name (case insensitive)
     * @return the corresponding RGB, or null
     */
    public static RGB toRGB(String name) {
        return COLORS.get(name.toLowerCase());
    }
}
