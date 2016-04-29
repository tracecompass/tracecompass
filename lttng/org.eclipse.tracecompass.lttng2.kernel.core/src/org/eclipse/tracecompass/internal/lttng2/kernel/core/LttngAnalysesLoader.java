/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.Lttng27EventLayout;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysis;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysisFactoryException;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysisFactoryFromConfigFile;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.OnDemandAnalysisManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Loader of LTTng analyses.
 *
 * @author Philippe Proulx
 */
final class LttngAnalysesLoader {

    private static final String CONFIG_DIR_NAME = "lttng-analyses-configs"; //$NON-NLS-1$

    private LttngAnalysesLoader() {
    }

    private static boolean appliesTo(ITmfTrace trace) {
        /* LTTng-Analysis is supported only on LTTng >= 2.7 kernel traces */
        if (trace instanceof LttngKernelTrace) {
            final LttngKernelTrace kernelTrace = (LttngKernelTrace) trace;
            final IKernelAnalysisEventLayout layout = kernelTrace.getKernelEventLayout();

            if (layout instanceof Lttng27EventLayout) {
                return true;
            }
        }

        return false;
    }

    private static String[] getAnalysisNames() throws IOException {
        final ClassLoader loader = LttngAnalysesLoader.class.getClassLoader();
        final String path = "/" + CONFIG_DIR_NAME + "/index.properties"; //$NON-NLS-1$ //$NON-NLS-2$
        final String[] names = new String[0];
        final Properties indexProps = new Properties();

        try (final InputStream in = loader.getResourceAsStream(path)) {
            if (in == null) {
                return names;
            }

            indexProps.load(in);
        }

        String analyses = indexProps.getProperty("analyses"); //$NON-NLS-1$

        if (analyses == null) {
            return names;
        }

        analyses = analyses.trim();
        return analyses.split("\\s+"); //$NON-NLS-1$
    }

    public static void load() throws LamiAnalysisFactoryException, IOException {
        final String[] names = getAnalysisNames();
        final ClassLoader loader = LttngAnalysesLoader.class.getClassLoader();

        for (final String name : names) {
            final String path = String.format("/%s/%s.properties", CONFIG_DIR_NAME, name); //$NON-NLS-1$

            try (final InputStream in = loader.getResourceAsStream(path)) {
                if (in == null) {
                    continue;
                }

                final LamiAnalysis analysis = LamiAnalysisFactoryFromConfigFile.buildFromInputStream(in, false, LttngAnalysesLoader::appliesTo);
                OnDemandAnalysisManager.getInstance().registerAnalysis(analysis);
            }
        }
    }

}
