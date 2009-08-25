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
 * <b><u>TmfEventFormat</u></b>
 * <p>
 * The basic event content format.
 */
public class TmfEventFormat implements ITmfContentParser {

    // ========================================================================
    // Attributes
    // ========================================================================

	private final String[] fLabels;

    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * 
	 */
	public TmfEventFormat() {
	    this(new String[] { "Content" });
	}

    /**
     * @param labels
     */
    public TmfEventFormat(String[] labels) {
        fLabels = labels;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return
	 */
	public String[] getLabels() {
		return fLabels;
	}

    // ========================================================================
    // Operators
    // ========================================================================

	/**
	 * The default content parser: returns a single field containing the whole
	 * content.
	 * 
	 * @param content
	 * @return
	 */
	public TmfEventField[] parse(Object content) {
        return new TmfEventField[] { new TmfEventField(content.toString()) };
    }

}
