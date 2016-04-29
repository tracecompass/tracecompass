/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Static methods to convert from a shell-like command string to individual
 * arguments and vice versa. Not all shell parsing features are implemented.
 *
 * @author Philippe Proulx
 */
public class ShellUtils {

    /**
     * Converts the string {@code command} to a list of individual arguments.
     * {@code command} is split on one or more spaces, but double-quoted
     * arguments are supported, as well as escaped backslashes ({@code \\})
     * and double quotes ({@code \"}).
     *
     * @param command Command string
     * @return List of arguments extracted from {@code command}
     */
    public static List<String> commandStringToArgs(String command) {
        int index = 0;
        boolean inQuotes = false;
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        while (index < command.length()) {
            char ch = command.charAt(index);

            if (ch == '\\' && index < command.length() - 1) {
                char escaped = command.charAt(index + 1);

                if (escaped == '\\' || escaped == '"') {
                    // Valid escaped character
                    sb.append(escaped);
                    index += 2;
                    continue;
                }
            }

            if (ch == '"') {
                if (inQuotes) {
                    // Quoted string ends: keep the quoted string, even if empty
                    args.add(sb.toString());
                    sb.setLength(0);
                } else {
                    // Quoted string begins
                    if (sb.length() > 0) {
                        args.add(sb.toString());
                        sb.setLength(0);
                    }
                }

                inQuotes = !inQuotes;
                index++;
                continue;
            }

            if (!inQuotes && ch == ' ') {
                // Argument delimiter
                if (sb.length() > 0) {
                    args.add(sb.toString());
                    sb.setLength(0);
                }

                index++;
                continue;
            }

            sb.append(ch);
            index++;
        }

        // Last argument, if any
        if (sb.length() > 0) {
            args.add(sb.toString());
        }

        return args;
    }

}
