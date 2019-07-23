/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.dataprovider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;

/**
 * X11Color utils. X11 color name is a standard that maps a color names to RGB
 * colors. See {@linktourl https://en.wikipedia.org/wiki/X11_color_names}
 *
 * @author Simon Delisle
 * @since 5.2
 */
@NonNullByDefault
public class X11ColorUtils {

    private static final String X11_COLOR_FILE = "share/rgb.txt"; //$NON-NLS-1$
    private static final Pattern PATTERN = Pattern.compile("\\s*(\\d{1,3})\\s*(\\d{1,3})\\s*(\\d{1,3})\\s*(.*\\S)\\s*"); //$NON-NLS-1$
    private static final String HEX_COLOR_FORMAT = "#%02x%02x%02x"; //$NON-NLS-1$
    private static final Map<String, String> COLORS = new HashMap<>();
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
                    String hexColor = String.format(HEX_COLOR_FORMAT, r, g, b);
                    COLORS.put(matcher.group(4).toLowerCase(), hexColor);
                }
            }
        } catch (IOException e) {
            Activator.logError("Cannot read the file from URL", e); //$NON-NLS-1$
        }
    }

    /**
     * Get the RGB hex string corresponding to a X11 color name.
     *
     * @param name
     *            the X11 color name (case insensitive)
     * @return the corresponding RGB hex string, or null
     */
    public static @Nullable String toHexColor(String name) {
        return COLORS.get(name.toLowerCase());
    }

    /**
     * Get the RGB hex string for the rgb values
     *
     * @param red
     *            The red value, should be between 0 and 255
     * @param green
     *            The green value, should be between 0 and 255
     * @param blue
     *            The blue value, should be between 0 and 255
     * @return The hexadecimal string for the color
     */
    public static String toHexColor(int red, int green, int blue) {
        return String.format(HEX_COLOR_FORMAT, Math.abs(red % 256), Math.abs(green % 256), Math.abs(blue % 256));
    }
}
