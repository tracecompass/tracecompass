/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.ITmfXmlSchemaParser;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Class containing some utilities for the XML plug-in packages: for example, it
 * manages the XML files and validates them
 *
 * @author Geneviève Bastien
 */
public class XmlUtils {

    /**
     * Enum to match the name of an output's XML element to its output ID.
     */
    public static enum OutputType {
        /**
         * Time graph output element
         */
        TIME_GRAPH(TmfXmlStrings.TIME_GRAPH_VIEW),
        /**
         * XY chart output element
         */
        XY(TmfXmlStrings.XY_VIEW);

        private final @NonNull String fXmlElem;

        private OutputType(@NonNull String xmlElem) {
            fXmlElem = xmlElem;
        }

        /**
         * Get the XML element corresponding to this output type
         *
         * @return The XML element corresponding to this type
         */
        public @NonNull String getXmlElem() {
            return fXmlElem;
        }
    }

    /** Sub-directory of the plug-in where XML files are stored */
    private static final String XML_DIRECTORY = "xml_files"; //$NON-NLS-1$

    /** Name of the XSD schema file */
    private static final String XSD = "xmlDefinition.xsd"; //$NON-NLS-1$

    /** Extension point ID and attributes */
    private static final String TMF_XML_BUILTIN_ID = "org.eclipse.linuxtools.tmf.analysis.xml.core.files"; //$NON-NLS-1$
    private static final String XML_FILE_ELEMENT = "xmlfile"; //$NON-NLS-1$
    private static final String XML_FILE_ATTRIB = "file"; //$NON-NLS-1$

    /** Extension point ID and attributes for extra XSD files */
    private static final String TMF_XSD_ID = "org.eclipse.tracecompass.tmf.analysis.xml.core.xsd"; //$NON-NLS-1$
    private static final String XSD_FILE_ELEMENT = "xsdfile"; //$NON-NLS-1$
    private static final String XSD_FILE_ATTRIB = "file"; //$NON-NLS-1$
    private static final String XSD_SCHEMA_PARSER_ELEMENT = "schemaParser"; //$NON-NLS-1$
    private static final String XSD_PARSER_CLASS_ATTRIB = "class"; //$NON-NLS-1$
    private static final @NonNull Multimap<String, XmlOutputElement> XML_OUTPUT_ELEMENTS = HashMultimap.create();
    private static Multimap<String, XmlOutputElement> fCachedOutputElement;

    /**
     * Extension for XML files
     */
    public static final String XML_EXTENSION = "xml"; //$NON-NLS-1$

    /** Make this class non-instantiable */
    private XmlUtils() {

    }

    /**
     * Get the path where the XML files are stored. Create it if it does not
     * exist
     *
     * @return path to XML files
     */
    public static IPath getXmlFilesPath() {
        IPath path = Activator.getDefault().getStateLocation();
        path = path.addTrailingSeparator().append(XML_DIRECTORY);

        /* Check if directory exists, otherwise create it */
        File dir = path.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }

        return path;
    }

    /**
     * Validate the XML file input with the XSD schema
     *
     * @param xmlFile
     *            XML file to validate
     * @return True if the XML validates
     */
    public static IStatus xmlValidate(File xmlFile) {
        URL url = TmfXmlUtils.class.getResource(XSD);
        List<@NonNull URL> xsdFiles = getExtraXsdFiles();
        Validator validator = null;
        Schema schema = null;

        Source[] sources = new Source[xsdFiles.size() + 1];
        sources[0] = new StreamSource(url.toExternalForm());
        for (int i = 0; i < xsdFiles.size(); i++) {
            sources[i + 1] = new StreamSource(xsdFiles.get(i).toExternalForm());
        }
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try {
            /*
             * Even though the XSDs do not define a namespace, there is one
             * default namespace of null and to allow multiple XSDs to be parsed
             * together, we must allow namespace growth
             */
            schemaFactory.setFeature("http://apache.org/xml/features/namespace-growth", true); //$NON-NLS-1$
            schema = schemaFactory.newSchema(sources);
        } catch (SAXException e) {
            // There was an error setting up the schema, log the error
            String error = NLS.bind(Messages.XmlUtils_XsdValidationError, e.getLocalizedMessage());
            Activator.logError(error);
            try {
                // and fallback to the builtin schema only
                schema = schemaFactory.newSchema(url);
            } catch (SAXException e1) {
                error = NLS.bind(Messages.XmlUtils_XsdValidationError, e1.getLocalizedMessage());
                Activator.logError(error);
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e1);
            }
        }
        validator = schema.newValidator();
        Source xmlSource = new StreamSource(xmlFile);
        try {
            validator.validate(xmlSource);
        } catch (SAXParseException e) {
            String error = NLS.bind(Messages.XmlUtils_XmlParseError, e.getLineNumber(), e.getLocalizedMessage());
            Activator.logError(error);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        } catch (SAXException e) {
            String error = NLS.bind(Messages.XmlUtils_XmlValidationError, e.getLocalizedMessage());
            Activator.logError(error);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        } catch (IOException e) {
            String error = Messages.XmlUtils_XmlValidateError;
            Activator.logError("IO exception occurred", e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        }
        return Status.OK_STATUS;
    }

    /**
     * Adds an XML file to the plugin's path. The XML file should have been
     * validated using the {@link XmlUtils#xmlValidate(File)} method before
     * calling this method.
     *
     * @param fromFile
     *            The XML file to add
     * @return Whether the file was successfully added
     */
    public static IStatus addXmlFile(File fromFile) {

        /* Copy file to path */
        File toFile = getXmlFilesPath().addTrailingSeparator().append(fromFile.getName()).toFile();

        IStatus status = copyXmlFile(fromFile, toFile);

        if (status.isOK()) {
            preloadXmlAnalysesOutput(toFile);
        }

        return status;
    }

    /**
     * List all files under the XML analysis files path. It returns a map where
     * the key is the file name.
     *
     * @return A map with all the XML analysis files
     */
    public static synchronized @NonNull Map<@NonNull String, @NonNull File> listFiles() {
        IPath pathToFiles = XmlUtils.getXmlFilesPath();
        File folder = pathToFiles.toFile();

        Map<@NonNull String, @NonNull File> fileMap = new HashMap<>();
        if ((folder.isDirectory() && folder.exists())) {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    IPath path = new Path(file.getName());
                    if (path.getFileExtension().equals(XML_EXTENSION)) {
                        fileMap.put(file.getName(), file);
                    }
                }
            } else {
                Activator.logError("I/O error occured while accessing files in folder " + folder.getPath()); //$NON-NLS-1$
            }
        }
        return Collections.unmodifiableMap(fileMap);
    }

    /**
     * List all files advertised through the builtin extension point. It returns
     * a map where the key is the file name.
     *
     * @return A map with all the XMl analysis builtin files
     */
    public static synchronized @NonNull Map<String, IPath> listBuiltinFiles() {
        List<URL> urls = getUrlsFromConfiguration(TMF_XML_BUILTIN_ID, XML_FILE_ELEMENT, XML_FILE_ATTRIB);

        Map<String, IPath> map = new HashMap<>();
        urls.forEach(url -> map.put(FilenameUtils.getName(url.getPath()), new Path(url.getPath())));
        return map;
    }

    /**
     * List all the additional files advertised through the XSD extension point
     *
     * @return A map with all the XMl analysis builtin files
     */
    private static synchronized @NonNull List<@NonNull URL> getExtraXsdFiles() {
        return getUrlsFromConfiguration(TMF_XSD_ID, XSD_FILE_ELEMENT, XSD_FILE_ATTRIB);
    }

    private static synchronized @NonNull List<@NonNull URL> getUrlsFromConfiguration(String extensionName, String elementName, String attribName) {
        /* Get the XSD files advertised through the extension point */
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(extensionName);
        List<@NonNull URL> list = new ArrayList<>();
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(elementName)) {
                final String filename = element.getAttribute(attribName);
                final String name = element.getContributor().getName();
                // Run this in a safe runner in case there is an exception
                // (IOException, FileNotFoundException, NPE, etc).
                // This makes sure other extensions are not prevented from
                // working if one is faulty.
                SafeRunner.run(new ISafeRunnable() {

                    @Override
                    public void run() throws IOException {
                        if (name != null) {
                            Bundle bundle = Platform.getBundle(name);
                            if (bundle != null) {
                                URL xmlUrl = bundle.getResource(filename);
                                if (xmlUrl == null) {
                                    throw new FileNotFoundException(filename);
                                }
                                URL locatedURL = FileLocator.toFileURL(xmlUrl);
                                list.add(NonNullUtils.checkNotNull(locatedURL));
                            }
                        }
                    }

                    @Override
                    public void handleException(Throwable exception) {
                        // Handled sufficiently in SafeRunner
                    }
                });
            }
        }
        return list;
    }

    /**
     * List all the additional files advertised through the XSD extension point
     *
     * @return A map with all the XMl analysis builtin files
     */
    public static synchronized @NonNull Collection<ITmfXmlSchemaParser> getExtraSchemaParsers() {
        /* Get the XSD files advertised through the extension point */
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_XSD_ID);
        List<org.eclipse.tracecompass.tmf.analysis.xml.core.module.ITmfXmlSchemaParser> list = new ArrayList<>();
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(XSD_SCHEMA_PARSER_ELEMENT)) {
                try {
                    ITmfXmlSchemaParser parser = NonNullUtils.checkNotNull((ITmfXmlSchemaParser) element.createExecutableExtension(XSD_PARSER_CLASS_ATTRIB));
                    list.add(parser);
                } catch (CoreException e) {
                    Activator.logError("Error getting analysis modules from configuration files", e); //$NON-NLS-1$
                }
            }
        }
        return list;
    }

    /**
     * Delete an XML analysis file
     *
     * @param name
     *            The XML file to delete
     */
    public static void deleteFile(String name) {
        Map<String, File> files = listFiles();
        File file = files.get(name);
        if (file == null) {
            return;
        }
        removeXmlOutput(file.getAbsolutePath());
        file.delete();
    }

    /**
     * Export an XML analysis file to an external path
     *
     * @param from
     *            The name of the file to export
     * @param to
     *            The full path of the file to write to
     * @return Whether the file was successfully exported
     */
    public static IStatus exportXmlFile(String from, String to) {

        /* Copy file to path */
        File fromFile = getXmlFilesPath().addTrailingSeparator().append(from).toFile();

        if (!fromFile.exists()) {
            Activator.logError("Failed to find XML analysis file " + fromFile.getName()); //$NON-NLS-1$
            return Status.CANCEL_STATUS;
        }

        File toFile = new File(to);

        return copyXmlFile(fromFile, toFile);
    }

    private static IStatus copyXmlFile(File fromFile, File toFile) {
        try {
            if (!toFile.exists()) {
                toFile.createNewFile();
            }
        } catch (IOException e) {
            String error = Messages.XmlUtils_ErrorCopyingFile;
            Activator.logError(error, e);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        }

        try (FileInputStream fis = new FileInputStream(fromFile);
                FileOutputStream fos = new FileOutputStream(toFile);
                FileChannel source = fis.getChannel();
                FileChannel destination = fos.getChannel();) {
            destination.transferFrom(source, 0, source.size());
        } catch (IOException e) {
            String error = Messages.XmlUtils_ErrorCopyingFile;
            Activator.logError(error, e);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        }
        return Status.OK_STATUS;
    }

    /**
     * Get the IDs of all the analysis described in a single file
     *
     * @param fileName
     *            The file name
     * @return The list of IDs
     */
    public static List<String> getAnalysisIdsFromFile(String fileName) {
        List<String> ids = new ArrayList<>();
        File file = getXmlFilesPath().addTrailingSeparator().append(fileName).toFile();
        if (file.exists()) {
            try {
                Document doc = getDocumentFromFile(file);

                /* get State Providers modules */
                NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);
                for (int i = 0; i < stateproviderNodes.getLength(); i++) {
                    ids.add(nullToEmptyString(((Element) stateproviderNodes.item(i)).getAttribute(TmfXmlStrings.ID)));
                }

                /* get patterns modules */
                NodeList patternNodes = doc.getElementsByTagName(TmfXmlStrings.PATTERN);
                for (int i = 0; i < patternNodes.getLength(); i++) {
                    ids.add(nullToEmptyString(((Element) patternNodes.item(i)).getAttribute(TmfXmlStrings.ID)));
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                Activator.logError("Failed to get analyses IDs from " + fileName); //$NON-NLS-1$
            }
        }
        return ids;
    }

    /**
     * Load the XML File
     *
     * @param file
     *            The XML file
     * @return The document representing the XML file
     * @throws ParserConfigurationException
     *             if a DocumentBuilder cannot be created
     * @throws SAXException
     *             If any parse errors occur.
     * @throws IOException
     *             If any IO errors occur.
     */
    public static Document getDocumentFromFile(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = dbFactory.newDocumentBuilder().parse(file);
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Get only the XML element children of an XML element.
     *
     * @param parent
     *            The parent element to get children from
     * @return The list of children Element of the parent
     */
    public static @NonNull List<@Nullable Element> getChildElements(Element parent) {
        NodeList childNodes = parent.getChildNodes();
        List<@Nullable Element> childElements = new ArrayList<>();
        for (int index = 0; index < childNodes.getLength(); index++) {
            if (childNodes.item(index).getNodeType() == Node.ELEMENT_NODE) {
                childElements.add((Element) childNodes.item(index));
            }
        }
        return childElements;
    }

    /**
     * preload all the xml analyses output for the existing xml files
     */
    public static void initOutputElements() {
        // Get all the xml files, builtin and not builtin
        Set<File> files = listBuiltinFiles().values().stream()
                .map(p -> p.toFile())
                .collect(Collectors.toSet());
        listFiles().values().stream()
                .forEach(f -> files.add(f));

        for (File xmlFile : files) {
            preloadXmlAnalysesOutput(xmlFile);
        }
    }

    /**
     * Preload the xml analyses file output data
     *
     * @param file
     *            The xml file
     */
    public static void updateXmlFile(File file) {
        preloadXmlAnalysesOutput(file);
    }

    /**
     * preload all the xml analyses output in the given file.
     *
     * @param xmlFile
     *            The xml file
     */
    private static void preloadXmlAnalysesOutput(File xmlFile) {

        if (!xmlValidate(xmlFile).isOK()) {
            return;
        }

        removeXmlOutput(xmlFile.getAbsolutePath());
        try {
            Document doc = XmlUtils.getDocumentFromFile(xmlFile);

                for (OutputType outputType : OutputType.values()) {
                    NodeList outputNodes = doc.getElementsByTagName(outputType.getXmlElem());
                    for (int i = 0; i < outputNodes.getLength(); i++) {
                        Set<String> analysesId = new HashSet<>();
                        Element node = (Element) outputNodes.item(i);

                        /* Check if analysis is the right one */
                        List<Element> headNodes = TmfXmlUtils.getChildElements(node, TmfXmlStrings.HEAD);
                        if (headNodes.size() != 1) {
                            return;
                        }

                        Element headElement = headNodes.get(0);
                        List<Element> analysisNodes = TmfXmlUtils.getChildElements(headElement, TmfXmlStrings.ANALYSIS);
                        for (Element analysis : analysisNodes) {
                            String analysisId = analysis.getAttribute(TmfXmlStrings.ID);
                            analysesId.add(analysisId);
                        }
                        String outputId = node.getAttribute(TmfXmlStrings.ID);

                        List<Element> label = TmfXmlUtils.getChildElements(headNodes.get(0), TmfXmlStrings.LABEL);
                        if (label.isEmpty()) {
                            return;
                        }
                        Element labelElement = label.get(0);
                        String outputLabel = labelElement.getAttribute(TmfXmlStrings.VALUE);

                        XmlOutputElement output = new XmlOutputElement(xmlFile.getAbsolutePath(), outputType.getXmlElem(), outputId, outputLabel, analysesId);
                        addXmlOutput(output);
                    }
                }
                updateCachedOuputElements();

        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError("Error opening XML file", e); //$NON-NLS-1$
        }
    }

    private static void updateCachedOuputElements() {
        fCachedOutputElement = ImmutableMultimap.copyOf(XML_OUTPUT_ELEMENTS);
    }

    /**
     * Update or add new xml output
     *
     * @param output
     *            the output
     */
    private static void addXmlOutput(XmlOutputElement output) {
        XML_OUTPUT_ELEMENTS.put(output.getPath(), output);
    }

    /**
     * Remove all the ouput for the specific xml file
     *
     * @param path
     *            The xml file path
     */
    private static void removeXmlOutput(String path) {
        XML_OUTPUT_ELEMENTS.removeAll(path);
    }

    /**
     * Clear all the preloaded xml output elements
     */
    public static void clearOutputElements() {
        XML_OUTPUT_ELEMENTS.clear();
    }

    /**
     * Get all the xml output element
     *
     * @return A table of xml output elements
     */
    public static Multimap<String, XmlOutputElement> getXmlOutputElements() {
        return fCachedOutputElement;
    }
}
