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
 * <b><u>TmfEventContent</u></b>
 * <p>
 * The event content.
 */
public class TmfEventContent {

    // ========================================================================
    // Attributes
    // ========================================================================

	private final TmfEventFormat fFormat;
	private final String fContent;
	private final int fNbFields;
	private       TmfEventField[] fFields = null;

    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * @param content
	 * @param format
	 */
	public TmfEventContent(Object content, TmfEventFormat format) {
		fFormat = format;
		fContent = content.toString();
		fNbFields = fFormat.getLabels().length;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return
	 */
	public String getContent() {
		return fContent;
	}

	/**
	 * @return
	 */
	public TmfEventFormat getFormat() {
		return fFormat;
	}

    /**
     * @return
     */
    public int getNbFields() {
        return fNbFields;
    }

	/**
	 * @return
	 */
	public TmfEventField[] getFields() {
	    if (fFields == null) {
	        fFields = fFormat.parse(fContent);
	    }
		return fFields;
	}

	/**
	 * @param id
	 * @return
	 */
	public TmfEventField getField(int id) {
        assert id >= 0 && id < fNbFields;
        if (fFields == null) {
            fFields = fFormat.parse(fContent);
        }
		return fFields[id];
	}

}
