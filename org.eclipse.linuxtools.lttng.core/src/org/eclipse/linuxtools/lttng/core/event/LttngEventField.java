/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.core.event;

import org.eclipse.linuxtools.tmf.core.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

/**
 * <b><u>LttngEventField</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventField.<p>
 * 
 * LttngEventField add a "name" attribute to the Tmf implementation This
 * mean the fields will have a name and a value.
 */
public class LttngEventField extends TmfEventField {
	
    /**
     * Constructor with parameters.<p>
     * 
     * @param parent   Parent content for this field
     * @param id       Name (label) of this field
     */
    public LttngEventField(TmfEventContent parent, String id) {
        super(parent, id, null);
    }
    
	/**
	 * Constructor with parameters with optional value.<p>
	 * 
	 * @param parent   Parent content for this field
	 * @param id       Name (label) of this field
	 * @param value    Parsed value (payload) of this field
	 */
	public LttngEventField(TmfEventContent parent, String id, Object value) {
		super(parent, id, value);
	}
	
	/**
	 * Copy constructor.<p>
	 * 
	 * @param oldField     the field to copy from
	 */
	public LttngEventField(LttngEventField oldField) {
		this(oldField.getContent(), oldField.getId(), oldField.getValue());
	}
	
	@Override
    @SuppressWarnings("nls")
	public String toString() {
		Object value = getValue();
	    return getId() + ":" + ((value != null) ? value.toString() : "null");
	}
	
	@Override
	public LttngEventField clone() {
		LttngEventField clone = (LttngEventField) super.clone();
		clone.fValue = fValue;
		return clone;
	}
	
}
