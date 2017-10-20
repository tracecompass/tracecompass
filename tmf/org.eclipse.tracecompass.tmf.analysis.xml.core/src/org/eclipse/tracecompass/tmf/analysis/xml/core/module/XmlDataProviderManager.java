/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.w3c.dom.Element;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Class to manage instances of XML data providers which cannot be handled by
 * extension points as there are possibly several instances of XML providers per
 * trace.
 *
 * @since 2.4
 * @author Loic Prieur-Drevon
 */
public class XmlDataProviderManager {

    private static @Nullable XmlDataProviderManager INSTANCE;

    private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
    private final Table<ITmfTrace, String, ITmfTreeXYDataProvider<@NonNull TmfTreeDataModel>> fXyProviders = HashBasedTable.create();

    /**
     * Get the instance of the manager
     *
     * @return the singleton instance
     */
    public static synchronized XmlDataProviderManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new XmlDataProviderManager();
        }
        return INSTANCE;
    }

    /**
     * Create (if necessary) and get the {@link XmlXYDataProvider} for the specified
     * trace and viewElement.
     *
     * @param trace
     *            trace for which we are querying a provider
     * @param viewElement
     *            the XML XY view for which we are querying a provider
     * @return the unique instance of an XY provider for the queried parameters
     */
    public synchronized ITmfTreeXYDataProvider<@NonNull TmfTreeDataModel> getXyProvider(@NonNull ITmfTrace trace, @NonNull Element viewElement) {
        if (!viewElement.hasAttribute(ID_ATTRIBUTE)) {
            return null;
        }
        String viewId = viewElement.getAttribute(ID_ATTRIBUTE);
        ITmfTreeXYDataProvider<@NonNull TmfTreeDataModel> provider = fXyProviders.get(trace, viewId);
        if (provider != null) {
            return provider;
        }
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            Set<@NonNull String> analysisIds = TmfXmlUtils.getViewAnalysisIds(viewElement);
            Element entry = TmfXmlUtils.getChildElements(viewElement, TmfXmlStrings.ENTRY_ELEMENT).get(0);

            provider = XmlXYDataProvider.create(trace, analysisIds, entry);
        } else {
            provider = generateExperimentProvider(traces, viewElement);
        }

        if (provider != null) {
            fXyProviders.put(trace, viewId, provider);
        }
        return provider;
    }

    private static ITmfTreeXYDataProvider<@NonNull TmfTreeDataModel> generateExperimentProvider(Collection<ITmfTrace> traces, Element viewElement) {
        List<ITmfTreeXYDataProvider<@NonNull TmfTreeDataModel>> providers = new ArrayList<>();
        for (ITmfTrace child : traces) {
            ITmfTreeXYDataProvider<@NonNull TmfTreeDataModel> childProvider = getInstance().getXyProvider(child, viewElement);
            if (childProvider != null) {
                providers.add(childProvider);
            }
        }
        if (providers.isEmpty()) {
            return null;
        } else if (providers.size() == 1) {
            return providers.get(0);
        }
        return new TmfTreeXYCompositeDataProvider<>(providers, XmlXYDataProvider.ID, XmlXYDataProvider.ID);
    }
}
