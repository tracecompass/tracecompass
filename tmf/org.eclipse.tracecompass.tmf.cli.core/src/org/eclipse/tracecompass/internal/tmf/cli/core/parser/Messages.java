/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.cli.core.parser;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the tracing RCP.
 *
 * @author Bernd Hufmann
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.cli.core.parser.messages"; //$NON-NLS-1$

    /** Help intro text */
    public static @Nullable String CliParser_HelpTextIntro;
    /** Error parsing arguments text */
    public static @Nullable String CliParser_ErrorParsingArguments;
    /** Help description */
    public static @Nullable String CliParser_HelpDescription;
    /** Warning for legacy --open option */
    public static @Nullable String CliParser_WarningCliPrefix;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
