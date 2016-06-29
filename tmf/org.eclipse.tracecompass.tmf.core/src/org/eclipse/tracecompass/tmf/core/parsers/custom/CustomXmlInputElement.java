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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;

/**
 * Wrapper for input XML elements
 */
public final class CustomXmlInputElement {

    /** Name of the element */
    private String fElementName;

    /** Indicates if this is a log entry */
    private boolean fLogEntry;

    /** Tag of the input element */
    private Tag fInputTag;

    /** Name of the input element */
    private String fInputName;

    /** Input action */
    private int fInputAction;

    /** Input format */
    private String fInputFormat;

    /** XML attributes of this element */
    private List<CustomXmlInputAttribute> fAttributes;

    /** Parent element */
    private CustomXmlInputElement fParentElement;

    /** Following element in the file */
    private CustomXmlInputElement fNextElement;

    /** Child elements */
    private List<CustomXmlInputElement> fChildElements;

    /** Event type associated with this input element */
    private String fEventType;

    /**
     * Default (empty) constructor
     */
    public CustomXmlInputElement() {
    }

    /**
     * Constructor
     *
     * @param elementName
     *            Element name
     * @param logEntry
     *            If this element is a log entry
     * @param inputName
     *            Name of the input
     * @param inputAction
     *            Input action
     * @param inputFormat
     *            Input format
     * @param attributes
     *            XML attributes of this element
     * @deprecated Use
     *             {@link #CustomXmlInputElement(String, boolean, Tag, String, int, String, List)}
     *             instead.
     */
    @Deprecated
    public CustomXmlInputElement(String elementName, boolean logEntry,
            String inputName, int inputAction, String inputFormat,
            List<CustomXmlInputAttribute> attributes) {
        fElementName = elementName;
        fLogEntry = logEntry;
        fInputName = inputName;
        fInputAction = inputAction;
        fInputFormat = inputFormat;
        fAttributes = attributes;
    }

    /**
     * Constructor
     *
     * @param elementName
     *            Element name
     * @param logEntry
     *            If this element is a log entry
     * @param inputTag
     *            Tag of the input
     * @param inputName
     *            Name of the input
     * @param inputAction
     *            Input action
     * @param inputFormat
     *            Input format
     * @param attributes
     *            XML attributes of this element
     * @since 2.1
     */
    public CustomXmlInputElement(String elementName, boolean logEntry, Tag inputTag,
            String inputName, int inputAction, String inputFormat,
            List<CustomXmlInputAttribute> attributes) {
        fElementName = elementName;
        fLogEntry = logEntry;
        fInputTag = inputTag;
        fInputName = inputName;
        fInputAction = inputAction;
        fInputFormat = inputFormat;
        fAttributes = attributes;
    }

    /**
     * Add a XML attribute to the element
     *
     * @param attribute
     *            The attribute to add
     */
    public void addAttribute(CustomXmlInputAttribute attribute) {
        if (getAttributes() == null) {
            fAttributes = new ArrayList<>(1);
        }
        getAttributes().add(attribute);
    }

    /**
     * Add a child element to this one.
     *
     * @param input
     *            The input element to add as child
     */
    public void addChild(CustomXmlInputElement input) {
        if (getChildElements() == null) {
            fChildElements = new ArrayList<>(1);
        } else if (getChildElements().size() > 0) {
            CustomXmlInputElement last = getChildElements().get(getChildElements().size() - 1);
            last.fNextElement = input;
        }
        getChildElements().add(input);
        input.setParentElement(this);
    }

    /**
     * Set the following input element.
     *
     * @param input
     *            The input element to add as next element
     */
    public void addNext(CustomXmlInputElement input) {
        if (getParentElement() != null) {
            int index = getParentElement().getChildElements().indexOf(this);
            getParentElement().getChildElements().add(index + 1, input);
            CustomXmlInputElement next = getNextElement();
            fNextElement = input;
            input.fNextElement = next;
        }
        input.setParentElement(getParentElement());
    }

    /**
     * Move this element up in its parent's list of children.
     */
    public void moveUp() {
        if (getParentElement() != null) {
            int index = getParentElement().getChildElements().indexOf(this);
            if (index > 0) {
                getParentElement().getChildElements().add(index - 1, getParentElement().getChildElements().remove(index));
                getParentElement().getChildElements().get(index).fNextElement = fNextElement;
                fNextElement = getParentElement().getChildElements().get(index);
            }
        }
    }

    /**
     * Move this element down in its parent's list of children.
     */
    public void moveDown() {
        if (getParentElement() != null) {
            int index = getParentElement().getChildElements().indexOf(this);
            if (index < getParentElement().getChildElements().size() - 1) {
                getParentElement().getChildElements().add(index + 1, getParentElement().getChildElements().remove(index));
                fNextElement = getParentElement().getChildElements().get(index).getNextElement();
                getParentElement().getChildElements().get(index).fNextElement = this;
            }
        }
    }

    /**
     * Get the element name
     *
     * @return the element name
     */
    public String getElementName() {
        return fElementName;
    }

    /**
     * Set the element name
     *
     * @param elementName
     *            the element name
     */
    public void setElementName(String elementName) {
        fElementName = elementName;
    }

    /**
     * @return the logEntry
     */
    public boolean isLogEntry() {
        return fLogEntry;
    }

    /**
     * @param logEntry
     *            the logEntry to set
     */
    public void setLogEntry(boolean logEntry) {
        fLogEntry = logEntry;
    }

    /**
     * @return the inputTag
     * @since 2.1
     */
    public Tag getInputTag() {
        return fInputTag;
    }

    /**
     * @param inputTag
     *            the inputTag to set
     * @since 2.1
     */
    public void setInputTag(Tag inputTag) {
        fInputTag = inputTag;
    }

    /**
     * @return the inputName
     */
    public String getInputName() {
        return fInputName;
    }

    /**
     * @param inputName
     *            the inputName to set
     */
    public void setInputName(String inputName) {
        fInputName = inputName;
    }

    /**
     * @return the eventType, or null
     * @since 2.1
     */
    public String getEventType() {
        return fEventType;
    }

    /**
     * @param eventType
     *            the eventType to set, or null
     * @since 2.1
     */
    public void setEventType(String eventType) {
        fEventType = eventType;
    }

    /**
     * @return the inputAction
     */
    public int getInputAction() {
        return fInputAction;
    }

    /**
     * @param inputAction
     *            the inputAction to set
     */
    public void setInputAction(int inputAction) {
        fInputAction = inputAction;
    }

    /**
     * @return the inputFormat
     */
    public String getInputFormat() {
        return fInputFormat;
    }

    /**
     * @param inputFormat
     *            the inputFormat to set
     */
    public void setInputFormat(String inputFormat) {
        fInputFormat = inputFormat;
    }

    /**
     * @return the attributes
     */
    public List<CustomXmlInputAttribute> getAttributes() {
        return fAttributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setAttributes(List<CustomXmlInputAttribute> attributes) {
        fAttributes = attributes;
    }

    /**
     * @return the parentElement
     */
    public CustomXmlInputElement getParentElement() {
        return fParentElement;
    }

    /**
     * @param parentElement
     *            the parentElement to set
     */
    public void setParentElement(CustomXmlInputElement parentElement) {
        fParentElement = parentElement;
    }

    /**
     * @return the nextElement
     */
    public CustomXmlInputElement getNextElement() {
        return fNextElement;
    }

    /**
     * @param nextElement
     *            the nextElement to set
     */
    public void setNextElement(CustomXmlInputElement nextElement) {
        fNextElement = nextElement;
    }

    /**
     * @return the childElements
     */
    public List<CustomXmlInputElement> getChildElements() {
        return fChildElements;
    }

    /**
     * @param childElements
     *            the childElements to set
     */
    public void setChildElements(List<CustomXmlInputElement> childElements) {
        fChildElements = childElements;
    }

}