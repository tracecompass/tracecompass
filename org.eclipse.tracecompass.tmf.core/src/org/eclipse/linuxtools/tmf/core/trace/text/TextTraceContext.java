/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.text;

import java.util.regex.Matcher;

import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * Implementation of a TmfContext for text traces.
 *
 * @since 3.0
 */
public class TextTraceContext extends TmfContext {

    /** The Matcher object for the first line. */
    public Matcher firstLineMatcher;
    /** The first line string */
    public String firstLine;
    /** The location of the next line */
    public long nextLineLocation;

    /**
     * Constructor
     *
     * @param location
     *            Trace location
     * @param rank
     *            Event rank
     */
    public TextTraceContext(final ITmfLocation location, final long rank) {
        super(location, rank);
    }

    /**
     * Copy Constructor
     *
     * @param other
     *            the other TextTraceContext
     */
    public TextTraceContext(TextTraceContext other) {
        this(other.getLocation(), other.getRank());
        firstLine = other.firstLine;
        firstLineMatcher = other.firstLineMatcher;
        nextLineLocation = other.nextLineLocation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((firstLine == null) ? 0 : firstLine.hashCode());
        result = prime * result + ((firstLineMatcher == null) ? 0 : firstLineMatcher.hashCode());
        result = prime * result + (int) (nextLineLocation ^ (nextLineLocation >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TextTraceContext other = (TextTraceContext) obj;
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
        if (nextLineLocation != other.nextLineLocation) {
            return false;
        }
        return true;
    }
}