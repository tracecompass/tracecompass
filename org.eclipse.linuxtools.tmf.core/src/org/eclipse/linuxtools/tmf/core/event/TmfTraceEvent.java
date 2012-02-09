/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

/**
 * <b><u>TmfTraceEvent</u></b>
 * <p>
 * A trace event associates a source code line to an event. The intent is to
 * provide the capability to open an editor at the line of code that produced
 * the event.
 * <p>
 * TODO: Concept is still a bit vague and should be aligned with the CDT
 * source lookup service.
 * TODO: Consider composition instead of extension
 */
public class TmfTraceEvent extends TmfEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final String fSourcePath;
    private final String fFileName;
    private final int    fLineNumber;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	/**
	 * @param timestamp
	 * @param source
	 * @param type
	 * @param content
	 * @param reference
	 * @param path
	 * @param file
	 * @param line
	 */
	public TmfTraceEvent(TmfTimestamp timestamp, String source, TmfEventType type,
			String reference, String path, String file, int line)
	{
		super(timestamp, source, type, reference);
		fSourcePath = path;
		fFileName   = file;
		fLineNumber = line;
	}

	/**
	 * @param other
	 */
	public TmfTraceEvent(TmfTraceEvent other) {
		super(other);
		fSourcePath = other.fSourcePath;
		fFileName   = other.fFileName;
		fLineNumber = other.fLineNumber;
	}

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return
     */
    public String getSourcePath() {
        return fSourcePath;
    }

    /**
     * @return
     */
    public String getFileName() {
        return fFileName;
    }

    /**
     * @return
     */
    public int getLineNumber() {
        return fLineNumber;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
		int result = super.hashCode();
		result = 37 * result + ((fSourcePath != null) ? fSourcePath.hashCode() : 0);
		result = 37 * result + ((fFileName   != null) ? fFileName.hashCode()   : 0);
		result = 37 * result + fLineNumber;
        return result;
    }

    @Override
	public boolean equals(Object other) {
    	if (other instanceof TmfEvent) {
    		return super.equals(other); 
    	}
    	if (!(other instanceof TmfTraceEvent)) {
    		return false; 
    	}
		TmfTraceEvent o = (TmfTraceEvent) other;
        return super.equals((TmfEvent) o) && fSourcePath.equals(o.fSourcePath) &&
        		fFileName.equals(o.fFileName) && fLineNumber == o.fLineNumber;

    }

    // TODO: Proper format
    @Override
    @SuppressWarnings("nls")
    public String toString() {
		return "[TmfTraceEvent(" + fSourcePath + "," + fFileName + "," + fLineNumber + ")]";
    }

}
