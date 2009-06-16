/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;

/**
 * <b><u>TmfTraceEvent</u></b>
 * <p>
 * A trace event associates a source code line to an event. The intent is to
 * provide the capability to open an editor at the line of code that produced
 * the event.
 * <p>
 * TODO: Concept is still a bit vague and should be aligned with the CDT
 * source lookup service.
 */
public class TmfTraceEvent extends TmfEvent {

    // ========================================================================
    // Attributes
    // ========================================================================

    private final String fSourcePath;
    private final String fFileName;
    private final int fLineNumber;

    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * The constructor.
	 * 
	 * @param timestamp
	 * @param source
	 * @param type
	 * @param content
	 * @param reference
	 */
	public TmfTraceEvent(TmfTimestamp timestamp, TmfEventSource source, TmfEventType type,
			TmfEventContent content, TmfEventReference reference,
			String path, String file, int line)
	{
		super(timestamp, source, type,content, reference);
		fSourcePath = path;
		fFileName = file;
		fLineNumber = line;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

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

}
