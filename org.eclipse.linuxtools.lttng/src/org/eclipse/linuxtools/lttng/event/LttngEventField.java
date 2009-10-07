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

package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.tmf.event.TmfEventField;

/**
 * <b><u>LttngEventField</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventField.<p>
 * 
 * LttngEventField add a "name" attribute to the Tmf implementation This
 * mean the fields will have a name and a value.
 */
public class LttngEventField extends TmfEventField {
	private String fieldName = "";
	
	/**
	 * Constructor with parameters.<p>
	 * 
	 * @param name       Name of the field
	 * @param newContent ParsedContent we want to populate the field with.
	 * 
	 * @see org.eclipse.linuxtools.lttng.jni.JniParser
	 */
	public LttngEventField(String name, Object newContent) {
		super(newContent);
		
		fieldName = name;
	}
	
	/**
	 * Copy constructor.<p>
	 * 
	 * @param oldField     the field to copy from
	 */
	public LttngEventField(LttngEventField oldField) {
		this(oldField.fieldName, oldField.getValue());
	}
	
	
	public String getName() {
		return fieldName;
	}

	/**
	 * toString() method.<p>
	 * 
	 * Print both field name and value (i.e. NAME:VALUE ).
	 */
	@Override
	public String toString() {
		return fieldName + ":" + getValue().toString();
	}
}
