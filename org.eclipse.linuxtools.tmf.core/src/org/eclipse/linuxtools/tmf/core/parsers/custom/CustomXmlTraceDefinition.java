/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Matthew Khouzam - Add support for default xml parsers
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.parsers.custom;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Trace definition for custom XML traces.
 *
 * @author Patrick Tassé
 * @since 3.0
 */
public class CustomXmlTraceDefinition extends CustomTraceDefinition {

    /** "ignore" tag */
    public static final String TAG_IGNORE = Messages.CustomXmlTraceDefinition_ignoreTag;

    /** Name of the default XML definitions file */
    protected static final String CUSTOM_XML_TRACE_DEFINITIONS_DEFAULT_FILE_NAME = "custom_xml_default_parsers.xml"; //$NON-NLS-1$

    /** Name of the XML definitions file */
    protected static final String CUSTOM_XML_TRACE_DEFINITIONS_FILE_NAME = "custom_xml_parsers.xml"; //$NON-NLS-1$

    /** Path to the XML definitions file */
    protected static final String CUSTOM_XML_TRACE_DEFINITIONS_DEFAULT_PATH_NAME =
            Platform.getInstallLocation().getURL().getPath() + "templates/org.eclipse.linuxtools.tmf.core/" + //$NON-NLS-1$
                    CUSTOM_XML_TRACE_DEFINITIONS_DEFAULT_FILE_NAME;

    /** Path to the XML definitions file */
    protected static final String CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME =
            Activator.getDefault().getStateLocation().addTrailingSeparator().append(CUSTOM_XML_TRACE_DEFINITIONS_FILE_NAME).toString();

    /**
     * Legacy path to the XML definitions file (in the UI plug-in) TODO Remove
     * once we feel the transition phase is over.
     */
    private static final String CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME_LEGACY =
            Activator.getDefault().getStateLocation().removeLastSegments(1).addTrailingSeparator()
                    .append("org.eclipse.linuxtools.tmf.ui") //$NON-NLS-1$
                    .append(CUSTOM_XML_TRACE_DEFINITIONS_FILE_NAME).toString();

    private static final String CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT = Messages.CustomXmlTraceDefinition_definitionRootElement;
    private static final String DEFINITION_ELEMENT = Messages.CustomXmlTraceDefinition_definition;
    private static final String NAME_ATTRIBUTE = Messages.CustomXmlTraceDefinition_name;
    private static final String LOG_ENTRY_ATTRIBUTE = Messages.CustomXmlTraceDefinition_logEntry;
    private static final String TIME_STAMP_OUTPUT_FORMAT_ELEMENT = Messages.CustomXmlTraceDefinition_timestampOutputFormat;
    private static final String INPUT_ELEMENT_ELEMENT = Messages.CustomXmlTraceDefinition_inputElement;
    private static final String ATTRIBUTE_ELEMENT = Messages.CustomXmlTraceDefinition_attribute;
    private static final String INPUT_DATA_ELEMENT = Messages.CustomXmlTraceDefinition_inputData;
    private static final String ACTION_ATTRIBUTE = Messages.CustomXmlTraceDefinition_action;
    private static final String FORMAT_ATTRIBUTE = Messages.CustomXmlTraceDefinition_format;
    private static final String OUTPUT_COLUMN_ELEMENT = Messages.CustomXmlTraceDefinition_outputColumn;

    /** Top-level input element */
    public InputElement rootInputElement;

    /**
     * Default constructor
     */
    public CustomXmlTraceDefinition() {
        this("", null, new ArrayList<OutputColumn>(), ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Full constructor
     *
     * @param logtype
     *            Type of trace type
     * @param rootElement
     *            The top-level XML element
     * @param outputs
     *            The list of output columns
     * @param timeStampOutputFormat
     *            The timestamp format to use
     */
    public CustomXmlTraceDefinition(String logtype, InputElement rootElement,
            List<OutputColumn> outputs, String timeStampOutputFormat) {
        this.definitionName = logtype;
        this.rootInputElement = rootElement;
        this.outputs = outputs;
        this.timeStampOutputFormat = timeStampOutputFormat;
    }

    /**
     * Wrapper for input XML elements
     */
    public static class InputElement {

        /** Name of the element */
        public String elementName;

        /** Indicates if this is a log entry */
        public boolean logEntry;

        /** Name of the input element */
        public String inputName;

        /** Input action */
        public int inputAction;

        /** Input format */
        public String inputFormat;

        /** XML attributes of this element */
        public List<InputAttribute> attributes;

        /** Parent element */
        public InputElement parentElement;

        /** Following element in the file */
        public InputElement nextElement;

        /** Child elements */
        public List<InputElement> childElements;

        /**
         * Default (empty) constructor
         */
        public InputElement() {
        }

        /**
         * Constructor
         *
         * @param elementName
         *            Element name
         * @param logEntry
         *            If this element is a log entry
         * @param inputName
         *            Name of the the input
         * @param inputAction
         *            Input action
         * @param inputFormat
         *            Input format
         * @param attributes
         *            XML attributes of this element
         */
        public InputElement(String elementName, boolean logEntry,
                String inputName, int inputAction, String inputFormat,
                List<InputAttribute> attributes) {
            this.elementName = elementName;
            this.logEntry = logEntry;
            this.inputName = inputName;
            this.inputAction = inputAction;
            this.inputFormat = inputFormat;
            this.attributes = attributes;
        }

        /**
         * Add a XML attribute to the element
         *
         * @param attribute
         *            The attribute to add
         */
        public void addAttribute(InputAttribute attribute) {
            if (attributes == null) {
                attributes = new ArrayList<>(1);
            }
            attributes.add(attribute);
        }

        /**
         * Add a child element to this one.
         *
         * @param input
         *            The input element to add as child
         */
        public void addChild(InputElement input) {
            if (childElements == null) {
                childElements = new ArrayList<>(1);
            } else if (childElements.size() > 0) {
                InputElement last = childElements.get(childElements.size() - 1);
                last.nextElement = input;
            }
            childElements.add(input);
            input.parentElement = this;
        }

        /**
         * Set the following input element.
         *
         * @param input
         *            The input element to add as next element
         */
        public void addNext(InputElement input) {
            if (parentElement != null) {
                int index = parentElement.childElements.indexOf(this);
                parentElement.childElements.add(index + 1, input);
                InputElement next = nextElement;
                nextElement = input;
                input.nextElement = next;
            }
            input.parentElement = this.parentElement;
        }

        /**
         * Move this element up in its parent's list of children.
         */
        public void moveUp() {
            if (parentElement != null) {
                int index = parentElement.childElements.indexOf(this);
                if (index > 0) {
                    parentElement.childElements.add(index - 1, parentElement.childElements.remove(index));
                    parentElement.childElements.get(index).nextElement = nextElement;
                    nextElement = parentElement.childElements.get(index);
                }
            }
        }

        /**
         * Move this element down in its parent's list of children.
         */
        public void moveDown() {
            if (parentElement != null) {
                int index = parentElement.childElements.indexOf(this);
                if (index < parentElement.childElements.size() - 1) {
                    parentElement.childElements.add(index + 1, parentElement.childElements.remove(index));
                    nextElement = parentElement.childElements.get(index).nextElement;
                    parentElement.childElements.get(index).nextElement = this;
                }
            }
        }

    }

    /**
     * Wrapper for XML element attributes
     */
    public static class InputAttribute {

        /** Name of the XML attribute */
        public String attributeName;

        /** Input name */
        public String inputName;

        /** Input action */
        public int inputAction;

        /** Input format */
        public String inputFormat;

        /**
         * Default (empty) constructor
         */
        public InputAttribute() {
        }

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
        public InputAttribute(String attributeName, String inputName,
                int inputAction, String inputFormat) {
            this.attributeName = attributeName;
            this.inputName = inputName;
            this.inputAction = inputAction;
            this.inputFormat = inputFormat;
        }
    }

    @Override
    public void save() {
        save(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME);
    }

    @Override
    public void save(String path) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            db.setEntityResolver(createEmptyEntityResolver());

            // The following catches xml parsing exceptions
            db.setErrorHandler(createErrorHandler());

            Document doc = null;
            File file = new File(path);
            if (file.canRead()) {
                doc = db.parse(file);
                if (!doc.getDocumentElement().getNodeName().equals(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT)) {
                    return;
                }
            } else {
                doc = db.newDocument();
                Node node = doc.createElement(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT);
                doc.appendChild(node);
            }

            Element root = doc.getDocumentElement();

            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element &&
                        node.getNodeName().equals(DEFINITION_ELEMENT) &&
                        definitionName.equals(((Element) node).getAttribute(NAME_ATTRIBUTE))) {
                    root.removeChild(node);
                }
            }
            Element definitionElement = doc.createElement(DEFINITION_ELEMENT);
            root.appendChild(definitionElement);
            definitionElement.setAttribute(NAME_ATTRIBUTE, definitionName);

            Element formatElement = doc.createElement(TIME_STAMP_OUTPUT_FORMAT_ELEMENT);
            definitionElement.appendChild(formatElement);
            formatElement.appendChild(doc.createTextNode(timeStampOutputFormat));

            if (rootInputElement != null) {
                definitionElement.appendChild(createInputElementElement(rootInputElement, doc));
            }

            if (outputs != null) {
                for (OutputColumn output : outputs) {
                    Element outputColumnElement = doc.createElement(OUTPUT_COLUMN_ELEMENT);
                    definitionElement.appendChild(outputColumnElement);
                    outputColumnElement.setAttribute(NAME_ATTRIBUTE, output.name);
                }
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

            // initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();

            try (FileWriter writer = new FileWriter(file);) {
                writer.write(xmlString);
            }

            TmfTraceType.addCustomTraceType(TmfTraceType.CUSTOM_XML_CATEGORY, definitionName);

        } catch (ParserConfigurationException | TransformerFactoryConfigurationError | TransformerException | IOException | SAXException e) {
            Activator.logError("Error saving CustomXmlTraceDefinition: path=" + path, e); //$NON-NLS-1$
        }
    }

    private Element createInputElementElement(InputElement inputElement, Document doc) {
        Element inputElementElement = doc.createElement(INPUT_ELEMENT_ELEMENT);
        inputElementElement.setAttribute(NAME_ATTRIBUTE, inputElement.elementName);

        if (inputElement.logEntry) {
            inputElementElement.setAttribute(LOG_ENTRY_ATTRIBUTE, Boolean.toString(inputElement.logEntry));
        }

        if (inputElement.parentElement != null) {
            Element inputDataElement = doc.createElement(INPUT_DATA_ELEMENT);
            inputElementElement.appendChild(inputDataElement);
            inputDataElement.setAttribute(NAME_ATTRIBUTE, inputElement.inputName);
            inputDataElement.setAttribute(ACTION_ATTRIBUTE, Integer.toString(inputElement.inputAction));
            if (inputElement.inputFormat != null) {
                inputDataElement.setAttribute(FORMAT_ATTRIBUTE, inputElement.inputFormat);
            }
        }

        if (inputElement.attributes != null) {
            for (InputAttribute attribute : inputElement.attributes) {
                Element inputAttributeElement = doc.createElement(ATTRIBUTE_ELEMENT);
                inputElementElement.appendChild(inputAttributeElement);
                inputAttributeElement.setAttribute(NAME_ATTRIBUTE, attribute.attributeName);
                Element inputDataElement = doc.createElement(INPUT_DATA_ELEMENT);
                inputAttributeElement.appendChild(inputDataElement);
                inputDataElement.setAttribute(NAME_ATTRIBUTE, attribute.inputName);
                inputDataElement.setAttribute(ACTION_ATTRIBUTE, Integer.toString(attribute.inputAction));
                if (attribute.inputFormat != null) {
                    inputDataElement.setAttribute(FORMAT_ATTRIBUTE, attribute.inputFormat);
                }
            }
        }

        if (inputElement.childElements != null) {
            for (InputElement childInputElement : inputElement.childElements) {
                inputElementElement.appendChild(createInputElementElement(childInputElement, doc));
            }
        }

        return inputElementElement;
    }

    /**
     * Load all the XML trace definitions in the default definitions file.
     *
     * @return The loaded trace definitions
     */
    public static CustomXmlTraceDefinition[] loadAll() {
        File defaultFile = new File(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME);
        File legacyFile = new File(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME_LEGACY);

        /*
         * If there is no file at the expected location, check the legacy
         * location instead.
         */
        if (!defaultFile.exists() && legacyFile.exists()) {
            CustomXmlTraceDefinition[] oldDefs = loadAll(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME_LEGACY);
            for (CustomXmlTraceDefinition def : oldDefs) {
                /* Save in the new location */
                def.save();
            }
        }

        Set<CustomXmlTraceDefinition> defs = new TreeSet<>(new Comparator<CustomXmlTraceDefinition>() {

            @Override
            public int compare(CustomXmlTraceDefinition o1, CustomXmlTraceDefinition o2) {
                return o1.definitionName.compareTo(o2.definitionName);
            }
        });
        defs.addAll(Arrays.asList(loadAll(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME)));
        defs.addAll(Arrays.asList(loadAll(CUSTOM_XML_TRACE_DEFINITIONS_DEFAULT_PATH_NAME)));
        return defs.toArray(new CustomXmlTraceDefinition[0]);
    }

    /**
     * Load all the XML trace definitions in the given definitions file.
     *
     * @param path
     *            Path to the definitions file to load
     * @return The loaded trace definitions
     */
    public static CustomXmlTraceDefinition[] loadAll(String path) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            db.setEntityResolver(createEmptyEntityResolver());

            // The following catches xml parsing exceptions
            db.setErrorHandler(createErrorHandler());

            File file = new File(path);
            if (!file.canRead()) {
                return new CustomXmlTraceDefinition[0];
            }
            Document doc = db.parse(file);

            Element root = doc.getDocumentElement();
            if (!root.getNodeName().equals(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT)) {
                return new CustomXmlTraceDefinition[0];
            }

            ArrayList<CustomXmlTraceDefinition> defList = new ArrayList<>();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element && node.getNodeName().equals(DEFINITION_ELEMENT)) {
                    CustomXmlTraceDefinition def = extractDefinition((Element) node);
                    if (def != null) {
                        defList.add(def);
                    }
                }
            }
            return defList.toArray(new CustomXmlTraceDefinition[0]);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError("Error loading all in CustomXmlTraceDefinition: path=" + path, e); //$NON-NLS-1$
        }
        return new CustomXmlTraceDefinition[0];
    }

    /**
     * Load the given trace definition.
     *
     * @param definitionName
     *            Name of the XML trace definition to load
     * @return The loaded trace definition
     */
    public static CustomXmlTraceDefinition load(String definitionName) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            EntityResolver resolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    String empty = ""; //$NON-NLS-1$
                    ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
                    return new InputSource(bais);
                }
            };
            db.setEntityResolver(resolver);

            // The following catches xml parsing exceptions
            db.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException saxparseexception) throws SAXException {
                }

                @Override
                public void warning(SAXParseException saxparseexception) throws SAXException {
                }

                @Override
                public void fatalError(SAXParseException saxparseexception) throws SAXException {
                    throw saxparseexception;
                }
            });

            CustomXmlTraceDefinition value = lookupXmlDefinition(definitionName, db, CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME);
            if (value == null) {
                value = lookupXmlDefinition(definitionName, db, CUSTOM_XML_TRACE_DEFINITIONS_DEFAULT_PATH_NAME);
            }
            return value;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError("Error loading CustomXmlTraceDefinition: definitionName=" + definitionName, e); //$NON-NLS-1$
        }
        return null;
    }

    private static CustomXmlTraceDefinition lookupXmlDefinition(String definitionName, DocumentBuilder db, String source) throws SAXException, IOException {
        File file = new File(source);
        if (!file.exists()) {
            return null;
        }

        Document doc = db.parse(file);

        Element root = doc.getDocumentElement();
        if (!root.getNodeName().equals(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT)) {
            return null;
        }

        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element &&
                    node.getNodeName().equals(DEFINITION_ELEMENT) &&
                    definitionName.equals(((Element) node).getAttribute(NAME_ATTRIBUTE))) {
                return extractDefinition((Element) node);
            }
        }
        return null;
    }

    /**
     * Extract a trace definition from an XML element.
     *
     * @param definitionElement
     *            Definition element
     * @return The extracted trace definition
     */
    public static CustomXmlTraceDefinition extractDefinition(Element definitionElement) {
        CustomXmlTraceDefinition def = new CustomXmlTraceDefinition();

        def.definitionName = definitionElement.getAttribute(NAME_ATTRIBUTE);
        if (def.definitionName == null) {
            return null;
        }

        NodeList nodeList = definitionElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals(TIME_STAMP_OUTPUT_FORMAT_ELEMENT)) {
                Element formatElement = (Element) node;
                def.timeStampOutputFormat = formatElement.getTextContent();
            } else if (nodeName.equals(INPUT_ELEMENT_ELEMENT)) {
                InputElement inputElement = extractInputElement((Element) node);
                if (inputElement != null) {
                    if (def.rootInputElement == null) {
                        def.rootInputElement = inputElement;
                    } else {
                        return null;
                    }
                }
            } else if (nodeName.equals(OUTPUT_COLUMN_ELEMENT)) {
                Element outputColumnElement = (Element) node;
                OutputColumn outputColumn = new OutputColumn();
                outputColumn.name = outputColumnElement.getAttribute(NAME_ATTRIBUTE);
                def.outputs.add(outputColumn);
            }
        }
        return def;
    }

    private static InputElement extractInputElement(Element inputElementElement) {
        InputElement inputElement = new InputElement();
        inputElement.elementName = inputElementElement.getAttribute(NAME_ATTRIBUTE);
        inputElement.logEntry = (Boolean.toString(true).equals(inputElementElement.getAttribute(LOG_ENTRY_ATTRIBUTE))) ? true : false;
        NodeList nodeList = inputElementElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals(INPUT_DATA_ELEMENT)) {
                Element inputDataElement = (Element) node;
                inputElement.inputName = inputDataElement.getAttribute(NAME_ATTRIBUTE);
                inputElement.inputAction = Integer.parseInt(inputDataElement.getAttribute(ACTION_ATTRIBUTE));
                inputElement.inputFormat = inputDataElement.getAttribute(FORMAT_ATTRIBUTE);
            } else if (nodeName.equals(ATTRIBUTE_ELEMENT)) {
                Element attributeElement = (Element) node;
                InputAttribute attribute = new InputAttribute();
                attribute.attributeName = attributeElement.getAttribute(NAME_ATTRIBUTE);
                NodeList attributeNodeList = attributeElement.getChildNodes();
                for (int j = 0; j < attributeNodeList.getLength(); j++) {
                    Node attributeNode = attributeNodeList.item(j);
                    String attributeNodeName = attributeNode.getNodeName();
                    if (attributeNodeName.equals(INPUT_DATA_ELEMENT)) {
                        Element inputDataElement = (Element) attributeNode;
                        attribute.inputName = inputDataElement.getAttribute(NAME_ATTRIBUTE);
                        attribute.inputAction = Integer.parseInt(inputDataElement.getAttribute(ACTION_ATTRIBUTE));
                        attribute.inputFormat = inputDataElement.getAttribute(FORMAT_ATTRIBUTE);
                    }
                }
                inputElement.addAttribute(attribute);
            } else if (nodeName.equals(INPUT_ELEMENT_ELEMENT)) {
                Element childInputElementElement = (Element) node;
                InputElement childInputElement = extractInputElement(childInputElementElement);
                if (childInputElement != null) {
                    inputElement.addChild(childInputElement);
                }
            }
        }
        return inputElement;
    }

    /**
     * Delete the given trace definition from the list of currently loaded ones.
     *
     * @param definitionName
     *            Name of the trace definition to delete
     */
    public static void delete(String definitionName) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            EntityResolver resolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    String empty = ""; //$NON-NLS-1$
                    ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
                    return new InputSource(bais);
                }
            };
            db.setEntityResolver(resolver);

            // The following catches xml parsing exceptions
            db.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException saxparseexception) throws SAXException {
                }

                @Override
                public void warning(SAXParseException saxparseexception) throws SAXException {
                }

                @Override
                public void fatalError(SAXParseException saxparseexception) throws SAXException {
                    throw saxparseexception;
                }
            });

            File file = new File(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME);
            Document doc = db.parse(file);

            Element root = doc.getDocumentElement();
            if (!root.getNodeName().equals(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT)) {
                return;
            }

            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element &&
                        node.getNodeName().equals(DEFINITION_ELEMENT) &&
                        definitionName.equals(((Element) node).getAttribute(NAME_ATTRIBUTE))) {
                    root.removeChild(node);
                }
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

            // initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();

            try (FileWriter writer = new FileWriter(file);) {
                writer.write(xmlString);
            }

            TmfTraceType.removeCustomTraceType(TmfTraceType.CUSTOM_XML_CATEGORY, definitionName);
            // Check if default definition needs to be reloaded
            TmfTraceType.addCustomTraceType(TmfTraceType.CUSTOM_XML_CATEGORY, definitionName);

        } catch (ParserConfigurationException | SAXException | IOException | TransformerFactoryConfigurationError | TransformerException e) {
            Activator.logError("Error deleteing CustomXmlTraceDefinition: definitionName=" + definitionName, e); //$NON-NLS-1$
        }
    }
}
