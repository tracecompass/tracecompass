/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Michael Jeanson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.lami.core.tests.shared.analysis;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.lami.core.tests.Activator;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysis;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Extension of {@link LamiAnalysis} used for tests.
 */
public class LamiAnalysisStub extends LamiAnalysis {

    private final @NonNull String fMetaDatafilename;
    private final @NonNull String fResultFilename;
    private @Nullable List<LamiResultTable> fResults = null;

    /**
     * Constructor.
     *
     * @param name
     *            The name of this analysis
     * @param metaDatafilename
     *            Filename of the JSON metadata file.
     * @param resultFilename
     *            Filename of the JSON results file.
     */
    public LamiAnalysisStub(@NonNull String name, @NonNull String metaDatafilename, @NonNull String resultFilename) {
        super(name, false, o -> true, Collections.singletonList("StubExecutable"));
        fMetaDatafilename = metaDatafilename;
        fResultFilename = resultFilename;
    }

    @Override
    public @NonNull String getName() {
        return fResultFilename;
    }

    @Override
    protected @NonNull Multimap<@NonNull String, @NonNull LamiChartModel> getPredefinedCharts() {
        return ImmutableMultimap.of();
    }

    @Override
    protected String getResultsFromCommand(List<String> command, IProgressMonitor monitor)
            throws CoreException {
        return readLamiFile(fResultFilename);
    }

    @Override
    protected @Nullable String getOutputFromCommand(List<String> command) {
        return readLamiFile(fMetaDatafilename);
    }

    @Override
    public boolean canExecute(ITmfTrace trace) {
        initialize();
        return true;
    }

    @Override
    protected synchronized void initialize() {
        checkMetadata();
    }

    /**
     * Get the result table at a specified index for the last trace the analysis
     * was run on.
     *
     * @param index
     *            The index of the table to get
     * @return The result table
     */
    public LamiResultTable getResultTable(int index) {
        List<LamiResultTable> results = fResults;
        if (results == null) {
            throw new NullPointerException("Results are null. The analysis hasn't been run yet?");
        }
        return results.get(index);
    }

    private static @NonNull String readLamiFile(String filename) {
        String fileContent = "";
        try {
            Activator plugin = Activator.instance();
            if (plugin == null) {
                /*
                 * Shouldn't happen but at least throw something to get the test to
                 * fail early
                 */
                throw new IllegalStateException();
            }
            URL url = FileLocator.find(plugin.getBundle(), new Path("testfiles/" + filename), null);
            if (url == null) {
                throw new IllegalArgumentException("lami file " + filename + " cannot be found");
            }

            try (InputStream inputStream = url.openConnection().getInputStream()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                fileContent = in.lines().collect(Collectors.joining());
                if (fileContent == null) {
                    fileContent = "";
                }
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }

        return fileContent;
    }

    @Override
    public @NonNull List<@NonNull LamiResultTable> execute(@NonNull ITmfTrace trace, @Nullable TmfTimeRange timeRange, @NonNull String extraParamsString, @NonNull IProgressMonitor monitor) throws CoreException {
        // Cache the results of the analysis
        @NonNull List<@NonNull LamiResultTable> results = super.execute(trace, timeRange, extraParamsString, monitor);
        fResults = results;
        return results;
    }


}
