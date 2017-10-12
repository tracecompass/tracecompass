/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.module;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
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

    private final Table<ITmfTrace, Element, XmlXYDataProvider> fXyProviders;

    private XmlDataProviderManager() {
        fXyProviders = HashBasedTable.create();
    }

    /**
     * initialization-on-demand holder
     */
    private static class LazyHolder {
        private LazyHolder() {
        }

        public static final XmlDataProviderManager INSTANCE = new XmlDataProviderManager();
    }

    /**
     * Get the instance of the manager
     *
     * @return the singleton instance
     */
    public static XmlDataProviderManager getInstance() {
        return LazyHolder.INSTANCE;
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
    public synchronized XmlXYDataProvider getXyProvider(@NonNull ITmfTrace trace, @NonNull Element viewElement) {
        if (fXyProviders.contains(trace, viewElement)) {
            return fXyProviders.get(trace, viewElement);
        }
        @NonNull Set<@NonNull String> analysisIds = TmfXmlUtils.getViewAnalysisIds(viewElement);
        Element entry = TmfXmlUtils.getChildElements(viewElement, TmfXmlStrings.ENTRY_ELEMENT).get(0);

        XmlXYDataProvider provider = XmlXYDataProvider.create(trace, analysisIds, entry);
        fXyProviders.put(trace, viewElement, provider);
        return provider;
    }
}
