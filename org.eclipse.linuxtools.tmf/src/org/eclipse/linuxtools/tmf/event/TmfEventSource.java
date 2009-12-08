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

package org.eclipse.linuxtools.tmf.event;

/**
 * <b><u>TmfEventSource</u></b>
 * <p>
 * The event source.
 */
public class TmfEventSource implements Cloneable {

    // ========================================================================
    // Attributes
    // ========================================================================

	protected Object fSourceId;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * 
     */
    public TmfEventSource() {
        this(null);
    }

	/**
	 * @param sourceId
	 */
	public TmfEventSource(Object sourceId) {
		fSourceId = sourceId;
	}

	/**
	 * @param other
	 */
	public TmfEventSource(TmfEventSource other) {
		this((other != null) ? other.fSourceId : null);
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return
	 */
	public Object getSourceId() {
		return fSourceId;
	}

	// ========================================================================
    // Operators
    // ========================================================================

	@Override
	public TmfEventSource clone() {
		return new TmfEventSource(this);
	}

	@Override
    public String toString() {
        return "[TmfEventSource(" + ((fSourceId != null) ? fSourceId.toString() : "null") + ")]";
    }

}
