/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Matthew Khouzam - Pulled out class
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.parsers.custom;

/**
 * Wrapper for XML element attributes
 */
public final class CustomXmlInputAttribute {

    /** Name of the XML attribute */
    private final String fAttributeName;

    /** Input name */
    private final String fInputName;

    /** Input action */
    private final int fInputAction;

    /** Input format */
    private final String fInputFormat;

    /**
     * Constructor
     *
     * @param attributeName
     *            Name of the XML attribute
     * @param inputName
     *            Input name
     * @param inputAction
     *            Input action
     * @param inputFormat
     *            Input format
     */
    public CustomXmlInputAttribute(String attributeName, String inputName,
            int inputAction, String inputFormat) {
        fAttributeName = attributeName;
        fInputName = inputName;
        fInputAction = inputAction;
        fInputFormat = inputFormat;
    }

    /**
     * @return the attributeName
     */
    public String getAttributeName() {
        return fAttributeName;
    }

    /**
     * @return the inputName
     */
    public String getInputName() {
        return fInputName;
    }

     /**
     * @return the inputAction
     */
    public int getInputAction() {
        return fInputAction;
    }

    /**
     * @return the inputFormat
     */
    public String getInputFormat() {
        return fInputFormat;
    }

}