/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.module;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlPatternCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlStateProviderCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.DataDrivenAnalysisModule;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.Messages;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.project.model.ITmfPropertiesProvider;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;

/**
 * Analysis module helpers for modules provided by XML files
 *
 * @author Geneviève Bastien
 * @since 2.2
 */
public class TmfAnalysisModuleHelperXml implements IAnalysisModuleHelper, ITmfPropertiesProvider {

    private static final String ICON_ANALYSIS = "/icons/analysis.png"; //$NON-NLS-1$

    /**
     * The types of analysis that can be XML-defined
     */
    public enum XmlAnalysisModuleType {
        /** Analysis will be of type {@link DataDrivenAnalysisModule} */
        STATE_SYSTEM,

        /**
         * Analysis will be of type XmlPatternAnalysisModule
         */
        PATTERN,
        /**
         * Analysis is of type other.
         */
        OTHER
    }

    private final File fSourceFile;
    private final Element fSourceElement;
    private final XmlAnalysisModuleType fType;

    /**
     * Constructor
     *
     * @param xmlFile
     *            The XML file containing the details of this analysis
     * @param node
     *            The XML node element
     * @param type
     *            The type of analysis
     */
    public TmfAnalysisModuleHelperXml(File xmlFile, Element node, XmlAnalysisModuleType type) {
        fSourceFile = xmlFile;
        fSourceElement = node;
        fType = type;
    }

    @Override
    public String getId() {
        /*
         * The attribute ID cannot be null because the XML has been validated
         * and it is mandatory
         */
        return fSourceElement.getAttribute(TmfXmlStrings.ID);
    }

    @Override
    public String getName() {
        String name = null;
        /* Label may be available in XML header */
        List<Element> head = TmfXmlUtils.getChildElements(fSourceElement, TmfXmlStrings.HEAD);
        if (head.size() == 1) {
            List<Element> labels = TmfXmlUtils.getChildElements(head.get(0), TmfXmlStrings.LABEL);
            if (!labels.isEmpty()) {
                name = labels.get(0).getAttribute(TmfXmlStrings.VALUE);
            }
        }

        if (name == null) {
            name = getId();
        }
        return name;
    }

    /**
     * Get the XML view prefix label
     *
     * @return XML view prefix label or empty string if the value is missing in
     *         the XML element
     */
    public @NonNull String getViewLabelPrefix() {
        if (!fType.equals(XmlAnalysisModuleType.PATTERN)) {
            return TmfXmlStrings.EMPTY_STRING;
        }
        String viewLabel = TmfXmlStrings.EMPTY_STRING;
        List<Element> head = TmfXmlUtils.getChildElements(fSourceElement, TmfXmlStrings.HEAD);
        if (head.size() == 1) {
            List<Element> labels = TmfXmlUtils.getChildElements(head.get(0), TmfXmlStrings.VIEW_LABEL_PREFIX);
            if (!labels.isEmpty()) {
                viewLabel = labels.get(0).getAttribute(TmfXmlStrings.VALUE);
            }
        }
        return viewLabel;
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public boolean appliesToExperiment() {
        return false;
    }

    @Override
    public String getHelpText() {
        return TmfXmlStrings.EMPTY_STRING;
    }

    @Override
    public String getHelpText(@NonNull ITmfTrace trace) {
        return TmfXmlStrings.EMPTY_STRING;
    }

    @Override
    public String getIcon() {
        return ICON_ANALYSIS;
    }

    @Override
    public Bundle getBundle() {
        return Activator.getDefault().getBundle();
    }

    @Override
    public boolean appliesToTraceType(Class<? extends ITmfTrace> traceClass) {
        /* Trace types may be available in XML header */
        List<Element> head = TmfXmlUtils.getChildElements(fSourceElement, TmfXmlStrings.HEAD);
        if (head.size() != 1) {
            return true;
        }
        /*
         * TODO: Test with custom trace types
         */
        List<Element> elements = TmfXmlUtils.getChildElements(head.get(0), TmfXmlStrings.TRACETYPE);
        if (elements.isEmpty()) {
            return true;
        }

        for (Element element : elements) {
            String traceTypeId = element.getAttribute(TmfXmlStrings.ID);
            traceTypeId = TmfTraceType.buildCompatibilityTraceTypeId(traceTypeId);
            TraceTypeHelper helper = TmfTraceType.getTraceType(traceTypeId);
            if ((helper != null) && helper.getTrace().getClass().isAssignableFrom(traceClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterable<Class<? extends ITmfTrace>> getValidTraceTypes() {
        return Collections.emptySet();
    }

    @Override
    public Iterable<TmfAbstractAnalysisRequirement> getAnalysisRequirements() {
        return Collections.emptySet();
    }

    @Override
    public final @Nullable IAnalysisModule newModule(ITmfTrace trace) throws TmfAnalysisException {
        String analysisid = getId();
        IAnalysisModule module = null;
        switch (fType) {
        case STATE_SYSTEM:
            TmfXmlStateProviderCu compile = TmfXmlStateProviderCu.compile(fSourceFile.toPath(), analysisid);
            if (compile == null) {
                return null;
            }
            module = new DataDrivenAnalysisModule(analysisid, compile);
            module.setName(getName());
            break;
        case PATTERN:
            TmfXmlPatternCu patternCu = TmfXmlPatternCu.compile(fSourceFile.toPath(), analysisid);
            if (patternCu == null) {
                return null;
            }
            module = new XmlPatternAnalysis(analysisid, patternCu);
            module.setName(getName());
            XmlPatternAnalysis paModule = (XmlPatternAnalysis) module;
            paModule.setViewLabelPrefix(getViewLabelPrefix());

            break;
        case OTHER:
            String name = getName();
            module = createOtherModule(analysisid, name);
            break;
        default:
            break;

        }
        if (module != null) {
            if (module.setTrace(trace)) {
                TmfAnalysisManager.analysisModuleCreated(module);
            } else {
                /*
                 * The analysis does not apply to the trace, dispose of the
                 * module
                 */
                module.dispose();
                module = null;
            }
        }

        return module;
    }

    /**
     * Create an analysis module from a type not provided by the main XML code.
     * Typically a plugin that provides new schema information through the xsd
     * extension point will also provide a schema parser. The schema parser may
     * create a module helper for a module of type OTHER that will override this
     * method.
     *
     * The returned module should have its name and id initialized and any other
     * specific information. Values for analysisid and name are provided in
     * parameter using the pattern of mandatory ID attribute in the main element
     * and an optional {@link TmfXmlStrings#HEAD} element in the sequence. But
     * the analysis is free to override those. The trace will be set later.
     *
     * @param analysisid
     *            The analysis ID, as found in the ID attribute of the analysis
     *            element
     * @param name
     *            The name of the analysis as obtained from calling the
     *            {@link #getName()} method.
     *
     * @return The newly created module
     */
    protected IAnalysisModule createOtherModule(@NonNull String analysisid, @NonNull String name) {
        throw new UnsupportedOperationException("Other modules should be implemented by their own helper classes"); //$NON-NLS-1$
    }

    /**
     * Get the source file where this XML element was found
     *
     * @return The source file
     */
    protected Path getSourceFile() {
        return fSourceFile.toPath();
    }

    /**
     * Get the source element for this module
     *
     * @return The source element for this module
     */
    protected Element getSourceElement() {
        return fSourceElement;
    }

    // ------------------------------------------------------------------------
    // ITmfPropertiesProvider
    // ------------------------------------------------------------------------

    @Override
    public @NonNull Map<@NonNull String, @NonNull String> getProperties() {
        Map<@NonNull String, @NonNull String> properties = new HashMap<>();
        properties.put(NonNullUtils.checkNotNull(Messages.XmlModuleHelper_PropertyFile), fSourceFile.getName());
        properties.put(NonNullUtils.checkNotNull(Messages.XmlModuleHelper_PropertyType), fType.name());
        return properties;
    }

}
