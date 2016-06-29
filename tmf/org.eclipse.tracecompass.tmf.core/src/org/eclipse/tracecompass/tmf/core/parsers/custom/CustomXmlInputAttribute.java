/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson
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

import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;

/**
 * Wrapper for XML element attributes
 */
public final class CustomXmlInputAttribute {

    /** Name of the XML attribute */
    private final String fAttributeName;

    /** Input tag */
    private final Tag fInputTag;

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
     * @deprecated Use
     *             {@link #CustomXmlInputAttribute(String, Tag, String, int, String)}
     *             instead.
     */
    @Deprecated
    public CustomXmlInputAttribute(String attributeName, String inputName,
            int inputAction, String inputFormat) {
        fAttributeName = attributeName;
        fInputTag = Tag.IGNORE;
        fInputName = inputName;
        fInputAction = inputAction;
        fInputFormat = inputFormat;
    }

    /**
     * Constructor
     *
     * @param attributeName
     *            Name of the XML attribute
     * @param inputTag
     *            Input tag
     * @param inputName
     *            Input name
     * @param inputAction
     *            Input action
     * @param inputFormat
     *            Input format
     * @since 2.1
     */
    public CustomXmlInputAttribute(String attributeName, Tag inputTag,
            String inputName, int inputAction, String inputFormat) {
        fAttributeName = attributeName;
        fInputTag = inputTag;
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
     * @return the inputTag
     * @since 2.1
     */
    public Tag getInputTag() {
        return fInputTag;
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