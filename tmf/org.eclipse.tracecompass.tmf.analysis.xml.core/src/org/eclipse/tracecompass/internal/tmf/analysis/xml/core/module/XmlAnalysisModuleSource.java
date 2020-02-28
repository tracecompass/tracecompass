/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.XmlDataProviderManager;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.ITmfXmlSchemaParser;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfAnalysisModuleHelperXml;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfAnalysisModuleHelperXml.XmlAnalysisModuleType;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleSource;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
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
        // Do nothing
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

    private static void processFile(@NonNull File xmlFile) {
        if (!XmlUtils.xmlValidate(xmlFile).isOK()) {
            return;
        }

        try {
            Document doc = XmlUtils.getDocumentFromFile(xmlFile);

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

            Iterable<ITmfXmlSchemaParser> extraSchemaParsers = XmlUtils.getExtraSchemaParsers();
            for (ITmfXmlSchemaParser parser : extraSchemaParsers) {
                fModules.addAll(parser.getModuleHelpers(xmlFile, doc));
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError("Error opening XML file", e); //$NON-NLS-1$
        }
    }

    private static void populateBuiltinModules() {
        Map<String, IPath> files = XmlUtils.listBuiltinFiles();
        for (IPath xmlPath : files.values()) {
            processFile(NonNullUtils.checkNotNull(xmlPath.toFile()));
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
        Map<String, @NonNull File> files = XmlUtils.getEnabledFiles();
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
        XmlDataProviderManager.getInstance().refreshDataProviderFactories();
    }

}
