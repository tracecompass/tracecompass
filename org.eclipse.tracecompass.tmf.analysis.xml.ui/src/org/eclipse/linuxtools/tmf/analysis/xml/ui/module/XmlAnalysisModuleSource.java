/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.ui.module;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.ui.module.TmfAnalysisModuleHelperXml.XmlAnalysisModuleType;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleSource;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
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
 * @since 3.0
 */
public class XmlAnalysisModuleSource implements IAnalysisModuleSource {

    /** Extension point ID */
    private static final String TMF_XML_BUILTIN_ID = "org.eclipse.linuxtools.tmf.analysis.xml.core.files"; //$NON-NLS-1$
    private static final String XML_FILE_ELEMENT = "xmlfile"; //$NON-NLS-1$

    private static final String XML_FILE_ATTRIB = "file"; //$NON-NLS-1$

    private static List<IAnalysisModuleHelper> fModules = null;

    /**
     * Constructor. It adds the new module listener to the analysis manager.
     */
    public XmlAnalysisModuleSource() {
        TmfAnalysisManager.addNewModuleListener(new TmfXmlAnalysisOutputSource());
    }

    @Override
    public synchronized Iterable<IAnalysisModuleHelper> getAnalysisModules() {
        if (fModules == null) {
            fModules = new ArrayList<>();
            populateBuiltinModules();
            populateAnalysisModules();
        }
        return fModules;
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
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError("Error opening XML file", e); //$NON-NLS-1$
        }
    }

    private static void populateBuiltinModules() {
        /* Get the XML files advertised through the extension point */
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_XML_BUILTIN_ID);
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(XML_FILE_ELEMENT)) {
                String filename = element.getAttribute(XML_FILE_ATTRIB);
                String name = element.getContributor().getName();
                if (name != null) {
                    Bundle bundle = Platform.getBundle(name);
                    if (bundle != null) {
                        try {
                            URL xmlUrl = bundle.getResource(filename);
                            URL locatedURL = FileLocator.toFileURL(xmlUrl);
                            processFile(new File(locatedURL.getFile()));
                        } catch (IOException e) {
                            /* ignore */
                        }
                    }
                }
            }
        }
    }

    private static void populateAnalysisModules() {
        IPath pathToFiles = XmlUtils.getXmlFilesPath();
        File fFolder = pathToFiles.toFile();
        if (!(fFolder.isDirectory() && fFolder.exists())) {
            return;
        }
        for (File xmlFile : fFolder.listFiles()) {
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
