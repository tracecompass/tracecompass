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

package org.eclipse.linuxtools.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;


public class TmfFilterContainsNode extends TmfFilterTreeNode {

	public static final String NODE_NAME = "CONTAINS"; //$NON-NLS-1$
	public static final String NOT_ATTR = "not"; //$NON-NLS-1$
	public static final String FIELD_ATTR = "field"; //$NON-NLS-1$
	public static final String VALUE_ATTR = "value"; //$NON-NLS-1$
	public static final String IGNORECASE_ATTR = "ignorecase"; //$NON-NLS-1$
	
	private boolean fNot = false;
	private String fField;
	private String fValue;
	private String fValueUpperCase;
	private boolean fIgnoreCase = false;
	
	public TmfFilterContainsNode(ITmfFilterTreeNode parent) {
		super(parent);
	}

	public boolean isNot() {
		return fNot;
	}
	
	public void setNot(boolean not) {
		this.fNot = not;
	}
	
	public String getField() {
		return fField;
	}

	public void setField(String field) {
		this.fField = field;
	}

	public String getValue() {
		return fValue;
	}

	public void setValue(String value) {
		this.fValue = value;
		fValueUpperCase = value.toUpperCase();
	}

	public boolean isIgnoreCase() {
		return fIgnoreCase;
	}
	
	public void setIgnoreCase(boolean ignoreCase) {
		this.fIgnoreCase = ignoreCase;
	}
	
	@Override
	public String getNodeName() {
		return NODE_NAME;
	}

	@Override
	public boolean matches(ITmfEvent event) {
        Object value = getFieldValue(event, fField);
        if (value == null) {
            return false ^ fNot;
        }
        String valueString = value.toString();
        if (fIgnoreCase) {
            return valueString.toUpperCase().contains(fValueUpperCase) ^ fNot;
        } else {
            return valueString.contains(fValue) ^ fNot;
        }
	}

	@Override
	public List<String> getValidChildren() {
		return new ArrayList<String>(0);
	}

	@Override
	public String toString() {
		return fField + (fNot ? " not" : "") + " contains \"" + fValue + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	@Override
	public ITmfFilterTreeNode clone() {
		TmfFilterContainsNode clone = (TmfFilterContainsNode) super.clone();
		clone.fField = fField;
		clone.setValue(fValue);
		return clone;
	}
}
