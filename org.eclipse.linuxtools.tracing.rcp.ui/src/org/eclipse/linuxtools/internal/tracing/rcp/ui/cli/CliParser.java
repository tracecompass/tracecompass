/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam- Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.tracing.rcp.ui.cli;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.tracing.rcp.ui.messages.Messages;

/**
 * Command line parser
 *
 * @author Matthew Khouzam
 */
public class CliParser {

    private static final String NOUI_ARG = "--NOUI"; //$NON-NLS-1$

    private static final String OPEN_ARG = "--open"; //$NON-NLS-1$

    private final Map<String, String> params = new HashMap<>();

    /** Open key     */
    public static final String OPEN_FILE_LOCATION = ".,-=open=-,."; //$NON-NLS-1$
    /** No ui key    */
    public static final String NO_UI = ".,-=noui=-,."; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param args
     *            the command line arguments
     * @throws TracingRCPCliException
     *             an error occurred parsing the cli
     */
    public CliParser(final String[] args) throws TracingRCPCliException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(OPEN_ARG)) {
                put(OPEN_FILE_LOCATION, args, i);
                // skip since we have two args
                i++;
            }
            else if (args[i].equals(NOUI_ARG)) {
                params.put(NO_UI, new String());
            }
        }
    }

    private void put(String key, String[] args, int pos) throws TracingRCPCliException {
        if (args.length <= pos) {
            throw new TracingRCPCliException(Messages.CliParser_MalformedCommand + ':' + ' ' + args[pos]);
        }
        params.put(key, args[pos + 1]);
    }

    /**
     * Get a parameter from the parsed command line
     * @param key OPEN_FILE_LOCATION or NO_UI
     * @return the value of the parameter, can be null
     */
    public String getArgument(String key) {
        return params.get(key);
    }

}
