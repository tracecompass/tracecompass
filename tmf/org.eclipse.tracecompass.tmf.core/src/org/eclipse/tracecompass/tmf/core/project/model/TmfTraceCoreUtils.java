/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.project.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Utility class for common tmf.core functionalities
 */
public class TmfTraceCoreUtils {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows") ? true : false; //$NON-NLS-1$ //$NON-NLS-2$
    private static final String INVALID_RESOURCE_CHARACTERS_WIN = "[\\\\/:*?\\\"<>\\|]|\\.$"; //$NON-NLS-1$
    private static final String INVALID_RESOURCE_CHARACTERS_OTHER = "[/\0]"; //$NON-NLS-1$
    private static final char BACKSLASH_PUA = 0xF000 + '\\';
    private static final char COLON_PUA = 0xF000 + ':';

    /**
     * Validates whether the given input file or folder string is a valid
     * resource string for one of the given types. It replaces invalid
     * characters by '_' and prefixes the name with '_' if needed.
     *
     * @param input
     *            a input name to validate
     * @return valid name
     */
    public static String validateName(String input) {
        String output = input;
        String pattern;
        if (IS_WINDOWS) {
            pattern = INVALID_RESOURCE_CHARACTERS_WIN;
        } else {
            pattern = INVALID_RESOURCE_CHARACTERS_OTHER;
        }

        output = output.replaceAll(pattern, String.valueOf('_'));
        if(!ResourcesPlugin.getWorkspace().validateName(output, IResource.FILE | IResource.FOLDER).isOK()) {
            output = '_' + output;
        }
        return output;
    }

    /**
     * Creates a new safe path, replacing any invalid Windows file name
     * characters '\' and ':' with characters in the Unicode Private Use Area.
     *
     * @param path
     *            a string path
     * @return a safe path
     */
    public static Path newSafePath(String path) {
        return new Path(path.replace('\\', BACKSLASH_PUA).replace(':', COLON_PUA));
    }

    /**
     * Returns the string representation of a safe path, replacing characters
     * in the Unicode Private Use Area back to their original value.
     *
     * @param path
     *            a safe path string
     * @return a string representation of this path
     */
    public static String safePathToString(String path) {
        return path.replace(BACKSLASH_PUA, '\\').replace(COLON_PUA, ':');
    }

}
