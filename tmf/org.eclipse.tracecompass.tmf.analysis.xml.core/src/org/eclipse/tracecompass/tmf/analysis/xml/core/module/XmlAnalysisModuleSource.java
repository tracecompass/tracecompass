/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfAnalysisModuleHelperXml.XmlAnalysisModuleType;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleSource;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Analysis module source who creates helpers for the analysis modules described
 * in the imported XML files
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public class XmlAnalysisModuleSource implements IAnalysisModuleSource {

    /** Extension point ID */
    private static final String TMF_XML_BUILTIN_ID = "org.eclipse.linuxtools.tmf.analysis.xml.core.files"; //$NON-NLS-1$
    private static final String XML_FILE_ELEMENT = "xmlfile"; //$NON-NLS-1$

    private static final String XML_FILE_ATTRIB = "file"; //$NON-NLS-1$

    /*
     * Legacy (Linux Tools) XML directory.
     * TODO Remove once we feel the transition phase is over.
     */
    private static final IPath XML_DIRECTORY_LEGACY =
            Activator.getDefault().getStateLocation().removeLastSegments(1)
            .append("org.eclipse.linuxtools.tmf.analysis.xml.core") //$NON-NLS-1$
            .append("xml_files"); //$NON-NLS-1$

    private static List<@NonNull IAnalysisModuleHelper> fModules = null;

    /**
     * Constructor. It adds the new module listener to the analysis manager.
     */
    public XmlAnalysisModuleSource() {

    }

    @Override
    public synchronized Iterable<IAnalysisModuleHelper> getAnalysisModules() {
        List<@NonNull IAnalysisModuleHelper> modules = fModules;
        if (modules == null) {
            modules = new ArrayList<>();
            fModules = modules;
            populateBuiltinModules();
            populateAnalysisModules();
        }
        return modules;
    }

    private static void processFile(File xmlFile) {
        if (!XmlUtils.xmlValidate(xmlFile).isOK()) {
            return;
        }

        try {
            /* Load the XML File */
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            /* get State Providers modules */
            NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);
            for (int i = 0; i < stateproviderNodes.getLength(); i++) {
                Element node = (Element) stateproviderNodes.item(i);

                IAnalysisModuleHelper helper = new TmfAnalysisModuleHelperXml(xmlFile, node, XmlAnalysisModuleType.STATE_SYSTEM);
                fModules.add(helper);
            }

            /* get pattern modules */
            NodeList patternNodes = doc.getElementsByTagName(TmfXmlStrings.PATTERN);
            for (int i = 0; i < patternNodes.getLength(); i++) {
                Element node = (Element) patternNodes.item(i);

                IAnalysisModuleHelper helper = new TmfAnalysisModuleHelperXml(xmlFile, node, XmlAnalysisModuleType.PATTERN);
                fModules.add(helper);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError("Error opening XML file", e); //$NON-NLS-1$
        }
    }

    private static void populateBuiltinModules() {
        /* Get the XML files advertised through the extension point */
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_XML_BUILTIN_ID);
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(XML_FILE_ELEMENT)) {
                final String filename = element.getAttribute(XML_FILE_ATTRIB);
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
                                processFile(new File(locatedURL.getFile()));
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
    }

    private static void populateAnalysisModules() {
        IPath pathToFiles = XmlUtils.getXmlFilesPath();
        File folder = pathToFiles.toFile();
        if (!(folder.isDirectory() && folder.exists())) {
            return;
        }
        /*
         * Transfer files from Linux Tools directory.
         */
        File oldFolder = XML_DIRECTORY_LEGACY.toFile();
        final File[] oldAnalysisFiles = oldFolder.listFiles();
        if (oldAnalysisFiles != null) {
            for (File fromFile : oldAnalysisFiles) {
                File toFile = pathToFiles.append(fromFile.getName()).toFile();
                if (!toFile.exists() && !fromFile.isDirectory()) {
                    try (FileInputStream fis = new FileInputStream(fromFile);
                            FileOutputStream fos = new FileOutputStream(toFile);
                            FileChannel source = fis.getChannel();
                            FileChannel destination = fos.getChannel();) {
                        destination.transferFrom(source, 0, source.size());
                    } catch (IOException e) {
                        String error = Messages.XmlUtils_ErrorCopyingFile;
                        Activator.logError(error, e);
                    }
                }
            }
        }
        Map<String, File> files = XmlUtils.listFiles();
        for (File xmlFile : files.values()) {
            processFile(xmlFile);
        }
    }

    /**
     * Notifies the main XML analysis module that the executable modules list
     * may have changed and needs to be refreshed.
     */
    public static void notifyModuleChange() {
        fModules = null;
        TmfAnalysisManager.refreshModules();
    }

}
