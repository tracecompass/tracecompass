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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((firstLine == null) ? 0 : firstLine.hashCode());
        result = prime * result + ((firstLineMatcher == null) ? 0 : firstLineMatcher.hashCode());
        result = prime * result + ((inputLine == null) ? 0 : inputLine.hashCode());
        result = prime * result + (int) (nextLineLocation ^ (nextLineLocation >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CustomTxtTraceContext)) {
            return false;
        }
        CustomTxtTraceContext other = (CustomTxtTraceContext) obj;
        if (firstLine == null) {
            if (other.firstLine != null) {
                return false;
            }
        } else if (!firstLine.equals(other.firstLine)) {
            return false;
        }
        if (firstLineMatcher == null) {
            if (other.firstLineMatcher != null) {
                return false;
            }
        } else if (!firstLineMatcher.equals(other.firstLineMatcher)) {
            return false;
        }
        if (inputLine == null) {
            if (other.inputLine != null) {
                return false;
            }
        } else if (!inputLine.equals(other.inputLine)) {
            return false;
        }
        if (nextLineLocation != other.nextLineLocation) {
            return false;
        }
        return true;
    }

}