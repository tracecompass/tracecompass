/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
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

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
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
 */
public class CustomXmlTraceDefinition extends CustomTraceDefinition {

    /** "ignore" tag */
    public static final String TAG_IGNORE = Messages.CustomXmlTraceDefinition_ignoreTag;

    /**
     * Custom XML label used internally and therefore should not be externalized
     */
    public static final String CUSTOM_XML_CATEGORY = "Custom XML"; //$NON-NLS-1$


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
     * Legacy path to the XML definitions file (in the UI plug-in of linux tools) TODO Remove
     * once we feel the transition phase is over.
     */
    private static final String CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME_LEGACY_UI =
            Activator.getDefault().getStateLocation().removeLastSegments(1).addTrailingSeparator()
                    .append("org.eclipse.linuxtools.tmf.ui") //$NON-NLS-1$
                    .append(CUSTOM_XML_TRACE_DEFINITIONS_FILE_NAME).toString();

    /**
     * Legacy path to the XML definitions file (in the core plug-in of linux tools) TODO Remove
     * once we feel the transition phase is over.
     */
    private static final String CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME_LEGACY_CORE =
            Activator.getDefault().getStateLocation().removeLastSegments(1).addTrailingSeparator()
                    .append("org.eclipse.linuxtools.tmf.core") //$NON-NLS-1$
                    .append(CUSTOM_XML_TRACE_DEFINITIONS_FILE_NAME).toString();

    // TODO: These strings should not be externalized
    private static final String CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT = Messages.CustomXmlTraceDefinition_definitionRootElement;
    private static final String DEFINITION_ELEMENT = Messages.CustomXmlTraceDefinition_definition;
    private static final String CATEGORY_ATTRIBUTE = Messages.CustomXmlTraceDefinition_category;
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
    public CustomXmlInputElement rootInputElement;

    /**
     * Default constructor
     */
    public CustomXmlTraceDefinition() {
        this(CUSTOM_XML_CATEGORY, "", null, new ArrayList<OutputColumn>(), ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Full constructor
     *
     * @param category
     *            Category of the trace type
     * @param traceType
     *            Name of the trace type
     * @param rootElement
     *            The top-level XML element
     * @param outputs
     *            The list of output columns
     * @param timeStampOutputFormat
     *            The timestamp format to use
     */
    public CustomXmlTraceDefinition(String category, String traceType, CustomXmlInputElement rootElement,
            List<OutputColumn> outputs, String timeStampOutputFormat) {
        this.categoryName = category;
        this.definitionName = traceType;
        this.rootInputElement = rootElement;
        this.outputs = outputs;
        this.timeStampOutputFormat = timeStampOutputFormat;
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

            Element oldDefinitionElement = findDefinitionElement(root, categoryName, definitionName);
            if (oldDefinitionElement != null) {
                root.removeChild(oldDefinitionElement);
            }
            Element definitionElement = doc.createElement(DEFINITION_ELEMENT);
            root.appendChild(definitionElement);
            definitionElement.setAttribute(CATEGORY_ATTRIBUTE, categoryName);
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

            TmfTraceType.addCustomTraceType(CustomXmlTrace.class, categoryName, definitionName);

        } catch (ParserConfigurationException | TransformerFactoryConfigurationError | TransformerException | IOException | SAXException e) {
            Activator.logError("Error saving CustomXmlTraceDefinition: path=" + path, e); //$NON-NLS-1$
        }
    }

    private Element createInputElementElement(CustomXmlInputElement inputElement, Document doc) {
        Element inputElementElement = doc.createElement(INPUT_ELEMENT_ELEMENT);
        inputElementElement.setAttribute(NAME_ATTRIBUTE, inputElement.getElementName());

        if (inputElement.isLogEntry()) {
            inputElementElement.setAttribute(LOG_ENTRY_ATTRIBUTE, Boolean.toString(inputElement.isLogEntry()));
        }

        if (inputElement.getParentElement() != null) {
            Element inputDataElement = doc.createElement(INPUT_DATA_ELEMENT);
            inputElementElement.appendChild(inputDataElement);
            inputDataElement.setAttribute(NAME_ATTRIBUTE, inputElement.getInputName());
            inputDataElement.setAttribute(ACTION_ATTRIBUTE, Integer.toString(inputElement.getInputAction()));
            if (inputElement.getInputFormat() != null) {
                inputDataElement.setAttribute(FORMAT_ATTRIBUTE, inputElement.getInputFormat());
            }
        }

        if (inputElement.getAttributes() != null) {
            for (CustomXmlInputAttribute attribute : inputElement.getAttributes()) {
                Element inputAttributeElement = doc.createElement(ATTRIBUTE_ELEMENT);
                inputElementElement.appendChild(inputAttributeElement);
                inputAttributeElement.setAttribute(NAME_ATTRIBUTE, attribute.getAttributeName());
                Element inputDataElement = doc.createElement(INPUT_DATA_ELEMENT);
                inputAttributeElement.appendChild(inputDataElement);
                inputDataElement.setAttribute(NAME_ATTRIBUTE, attribute.getInputName());
                inputDataElement.setAttribute(ACTION_ATTRIBUTE, Integer.toString(attribute.getInputAction()));
                if (attribute.getInputFormat() != null) {
                    inputDataElement.setAttribute(FORMAT_ATTRIBUTE, attribute.getInputFormat());
                }
            }
        }

        if (inputElement.getChildElements() != null) {
            for (CustomXmlInputElement childInputElement : inputElement.getChildElements()) {
                inputElementElement.appendChild(createInputElementElement(childInputElement, doc));
            }
        }

        return inputElementElement;
    }

    /**
     * Load all custom XML trace definitions, including the user-defined and
     * default (built-in) parsers.
     *
     * @return The loaded trace definitions
     */
    public static CustomXmlTraceDefinition[] loadAll() {
        return loadAll(true);
    }

    /**
     * Load all custom XML trace definitions, including the user-defined and,
     * optionally, the default (built-in) parsers.
     *
     * @param includeDefaults
     *            if true, the default (built-in) parsers are included
     *
     * @return The loaded trace definitions
     */
    public static CustomXmlTraceDefinition[] loadAll(boolean includeDefaults) {
        File defaultFile = new File(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME);
        File legacyFileUI = new File(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME_LEGACY_UI);
        File legacyFileCore = new File(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME_LEGACY_CORE);

        /*
         * If there is no file at the expected location, check the legacy
         * locations instead.
         */
        if (!defaultFile.exists()) {
            if (legacyFileCore.exists()) {
                transferDefinitions(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME_LEGACY_CORE);
            } else if (legacyFileUI.exists()) {
                transferDefinitions(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME_LEGACY_UI);
            }
        }

        Set<CustomXmlTraceDefinition> defs = new TreeSet<>(new Comparator<CustomXmlTraceDefinition>() {
            @Override
            public int compare(CustomXmlTraceDefinition o1, CustomXmlTraceDefinition o2) {
                int result = o1.categoryName.compareTo(o2.categoryName);
                if (result != 0) {
                    return result;
                }
                return o1.definitionName.compareTo(o2.definitionName);
            }
        });
        defs.addAll(Arrays.asList(loadAll(CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME)));
        if (includeDefaults) {
            defs.addAll(Arrays.asList(loadAll(CUSTOM_XML_TRACE_DEFINITIONS_DEFAULT_PATH_NAME)));
        }
        return defs.toArray(new CustomXmlTraceDefinition[0]);
    }

    private static void transferDefinitions(String defFile) {
        CustomXmlTraceDefinition[] oldDefs = loadAll(defFile);
        for (CustomXmlTraceDefinition def : oldDefs) {
            /* Save in the new location */
            def.save();
        }
    }


    /**
     * Load all the XML trace definitions in the given definitions file.
     *
     * @param path
     *            Path to the definitions file to load
     * @return The loaded trace definitions
     */
    public static CustomXmlTraceDefinition[] loadAll(String path) {
        File file = new File(path);
        if (!file.canRead()) {
            return new CustomXmlTraceDefinition[0];
        }
        try (FileInputStream fis = new FileInputStream(file);) {
            return loadAll(fis);
        } catch (IOException e) {
            Activator.logError("Error loading all in CustomXmlTraceDefinition: path=" + path, e); //$NON-NLS-1$
        }
        return new CustomXmlTraceDefinition[0];
    }

    /**
     * Load all the XML trace definitions from the given stream
     *
     * @param stream
     *            An input stream from which to read the definitions
     * @return The loaded trace definitions
     */
    public static CustomXmlTraceDefinition[] loadAll(InputStream stream) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            db.setEntityResolver(createEmptyEntityResolver());

            // The following catches xml parsing exceptions
            db.setErrorHandler(createErrorHandler());

            Document doc = db.parse(stream);
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
            Activator.logError("Error loading all in CustomXmlTraceDefinition: path=" + stream, e); //$NON-NLS-1$
        }
        return new CustomXmlTraceDefinition[0];
    }

    /**
     * Load the given trace definition.
     *
     * @param categoryName
     *            Category of the definition to load
     * @param definitionName
     *            Name of the XML trace definition to load
     * @return The loaded trace definition
     */
    public static CustomXmlTraceDefinition load(String categoryName, String definitionName) {
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

            CustomXmlTraceDefinition value = lookupXmlDefinition(categoryName, definitionName, db, CUSTOM_XML_TRACE_DEFINITIONS_PATH_NAME);
            if (value == null) {
                value = lookupXmlDefinition(categoryName, definitionName, db, CUSTOM_XML_TRACE_DEFINITIONS_DEFAULT_PATH_NAME);
            }
            return value;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError("Error loading CustomXmlTraceDefinition: definitionName=" + definitionName, e); //$NON-NLS-1$
        }
        return null;
    }

    private static CustomXmlTraceDefinition lookupXmlDefinition(String categoryName, String definitionName, DocumentBuilder db, String source) throws SAXException, IOException {
        File file = new File(source);
        if (!file.exists()) {
            return null;
        }

        Document doc = db.parse(file);

        Element root = doc.getDocumentElement();
        if (!root.getNodeName().equals(CUSTOM_XML_TRACE_DEFINITION_ROOT_ELEMENT)) {
            return null;
        }

        Element definitionElement = findDefinitionElement(root, categoryName, definitionName);
        if (definitionElement != null) {
            return extractDefinition(definitionElement);
        }
        return null;
    }

    private static Element findDefinitionElement(Element root, String categoryName, String definitionName) {
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element && node.getNodeName().equals(DEFINITION_ELEMENT)) {
                Element element = (Element) node;
                String categoryAttribute = element.getAttribute(CATEGORY_ATTRIBUTE);
                if (categoryAttribute.isEmpty()) {
                    categoryAttribute = CUSTOM_XML_CATEGORY;
                }
                String nameAttribute = element.getAttribute(NAME_ATTRIBUTE);
                if (categoryName.equals(categoryAttribute) &&
                        definitionName.equals(nameAttribute)) {
                    return element;
                }
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

        def.categoryName = definitionElement.getAttribute(CATEGORY_ATTRIBUTE);
        if (def.categoryName.isEmpty()) {
            def.categoryName = CUSTOM_XML_CATEGORY;
        }
        def.definitionName = definitionElement.getAttribute(NAME_ATTRIBUTE);
        if (def.definitionName.isEmpty()) {
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
                CustomXmlInputElement inputElement = extractInputElement((Element) node);
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

    private static CustomXmlInputElement extractInputElement(Element inputElementElement) {
        CustomXmlInputElement inputElement = new CustomXmlInputElement();
        inputElement.setElementName(inputElementElement.getAttribute(NAME_ATTRIBUTE));
        inputElement.setLogEntry((Boolean.toString(true).equals(inputElementElement.getAttribute(LOG_ENTRY_ATTRIBUTE))) ? true : false);
        NodeList nodeList = inputElementElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals(INPUT_DATA_ELEMENT)) {
                Element inputDataElement = (Element) node;
                inputElement.setInputName(inputDataElement.getAttribute(NAME_ATTRIBUTE));
                inputElement.setInputAction(Integer.parseInt(inputDataElement.getAttribute(ACTION_ATTRIBUTE)));
                inputElement.setInputFormat(inputDataElement.getAttribute(FORMAT_ATTRIBUTE));
            } else if (nodeName.equals(ATTRIBUTE_ELEMENT)) {
                Element attributeElement = (Element) node;

                String attributeName = attributeElement.getAttribute(NAME_ATTRIBUTE);
                String inputName = null;
                int inputAction = 0;
                String inputFormat = null;
                NodeList attributeNodeList = attributeElement.getChildNodes();
                for (int j = 0; j < attributeNodeList.getLength(); j++) {
                    Node attributeNode = attributeNodeList.item(j);
                    String attributeNodeName = attributeNode.getNodeName();
                    if (attributeNodeName.equals(INPUT_DATA_ELEMENT)) {
                        Element inputDataElement = (Element) attributeNode;
                        inputName = inputDataElement.getAttribute(NAME_ATTRIBUTE);
                        inputAction = Integer.parseInt(inputDataElement.getAttribute(ACTION_ATTRIBUTE));
                        inputFormat = inputDataElement.getAttribute(FORMAT_ATTRIBUTE);
                    }
                }
                inputElement.addAttribute(new CustomXmlInputAttribute(attributeName, inputName, inputAction, inputFormat));
            } else if (nodeName.equals(INPUT_ELEMENT_ELEMENT)) {
                Element childInputElementElement = (Element) node;
                CustomXmlInputElement childInputElement = extractInputElement(childInputElementElement);
                if (childInputElement != null) {
                    inputElement.addChild(childInputElement);
                }
            }
        }
        return inputElement;
    }

    /**
     * Delete a definition from the currently loaded ones.
     *
     * @param categoryName
     *            The category of the definition to delete
     * @param definitionName
     *            The name of the definition to delete
     */
    public static void delete(String categoryName, String definitionName) {
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

            Element definitionElement = findDefinitionElement(root, categoryName, definitionName);
            if (definitionElement != null) {
                root.removeChild(definitionElement);
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

            TmfTraceType.removeCustomTraceType(CustomXmlTrace.class, categoryName, definitionName);
            // Check if default definition needs to be reloaded
            TmfTraceType.addCustomTraceType(CustomXmlTrace.class, categoryName, definitionName);

        } catch (ParserConfigurationException | SAXException | IOException | TransformerFactoryConfigurationError | TransformerException e) {
            Activator.logError("Error deleteing CustomXmlTraceDefinition: definitionName=" + definitionName, e); //$NON-NLS-1$
        }
    }
}
