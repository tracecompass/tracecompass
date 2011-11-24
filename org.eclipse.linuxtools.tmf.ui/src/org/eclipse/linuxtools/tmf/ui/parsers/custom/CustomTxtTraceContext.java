/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import java.io.IOException;
import java.util.regex.Matcher;

import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTxtTraceDefinition.InputLine;

public class CustomTxtTraceContext extends TmfContext {
    public BufferedRandomAccessFile raFile;
    public Matcher firstLineMatcher;
    public String firstLine;
    public long nextLineLocation;
    public InputLine inputLine;

    public CustomTxtTraceContext(ITmfLocation<?> location, long rank) {
        super(location, rank);
    }

    @Override
    public void dispose() {
        if (raFile != null) {
            try {
                raFile.close();
            } catch (IOException e) {
            }
        }
        super.dispose();
    }

}