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

import org.eclipse.tracecompass.internal.tmf.core.Activator;

/**
 * X11Color utils. X11 color name is a standard that maps a color names to RGB
 * colors. See {@linktourl https://en.wikipedia.org/wiki/X11_color_names}
 *
 * @author Simon Delisle
 * @since 5.1
 */
public class X11ColorUtils {

    private static final String X11_COLOR_FILE = "share/rgb.txt"; //$NON-NLS-1$
    private static final Pattern PATTERN = Pattern.compile("\\s*(\\d{1,3})\\s*(\\d{1,3})\\s*(\\d{1,3})\\s*(.*\\S)\\s*"); //$NON-NLS-1$
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
                    String hexColor = String.format("#%02x%02x%02x", r, g, b); //$NON-NLS-1$
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
    public static String toHexColor(String name) {
        return COLORS.get(name.toLowerCase());
    }
}
