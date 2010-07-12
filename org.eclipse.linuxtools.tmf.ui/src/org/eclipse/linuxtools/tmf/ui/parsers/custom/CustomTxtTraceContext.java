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

import java.io.RandomAccessFile;
import java.util.regex.Matcher;

import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTxtTraceDefinition.InputLine;

public class CustomTxtTraceContext extends TmfContext {
    public RandomAccessFile raFile;
    public Matcher firstLineMatcher;
    public long nextLineLocation;
    public InputLine inputLine;

    public CustomTxtTraceContext(ITmfLocation<?> location, long rank) {
        super(location, rank);
    }
}