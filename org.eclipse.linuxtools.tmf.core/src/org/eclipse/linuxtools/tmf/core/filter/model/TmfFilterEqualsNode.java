/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
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


/**
 * Filter node for the '==' operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TmfFilterEqualsNode extends TmfFilterTreeNode {

    public static final String NODE_NAME = "EQUALS"; //$NON-NLS-1$
	public static final String NOT_ATTR = "not"; //$NON-NLS-1$
	public static final String FIELD_ATTR = "field"; //$NON-NLS-1$
	public static final String VALUE_ATTR = "value"; //$NON-NLS-1$
	public static final String IGNORECASE_ATTR = "ignorecase"; //$NON-NLS-1$

	private boolean fNot = false;
	private String fField;
	private String fValue;
	private boolean fIgnoreCase = false;

	/**
	 * @param parent the aprent node
	 */
	public TmfFilterEqualsNode(ITmfFilterTreeNode parent) {
		super(parent);
	}

	/**
	 * @return the NOT state
	 */
	public boolean isNot() {
		return fNot;
	}

	/**
	 * @param not the NOT state
	 */
	public void setNot(boolean not) {
		this.fNot = not;
	}

    /**
     * @return the field name
     */
	public String getField() {
		return fField;
	}

	/**
	 * @param field the field name
	 */
	public void setField(String field) {
		this.fField = field;
	}

	/**
	 * @return the equals value
	 */
	public String getValue() {
		return fValue;
	}

	/**
	 * @param value the equals value
	 */
	public void setValue(String value) {
		this.fValue = value;
	}

	/**
	 * @return the ignoreCase state
	 */
	public boolean isIgnoreCase() {
		return fIgnoreCase;
	}

	/**
	 * @param ignoreCase the ignoreCase state
	 */
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
        if (valueString == null) {
            return false ^ fNot;
        }
        if (fIgnoreCase) {
            return valueString.equalsIgnoreCase(fValue) ^ fNot;
        }
        return valueString.equals(fValue) ^ fNot;
	}

	@Override
	public List<String> getValidChildren() {
		return new ArrayList<>(0);
	}

	@Override
	public String toString() {
		return fField + (fNot ? " not" : "") + " equals \"" + fValue + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	@Override
	public ITmfFilterTreeNode clone() {
		TmfFilterEqualsNode clone = (TmfFilterEqualsNode) super.clone();
		clone.fField = fField;
		clone.fValue = fValue;
		return clone;
	}
}
