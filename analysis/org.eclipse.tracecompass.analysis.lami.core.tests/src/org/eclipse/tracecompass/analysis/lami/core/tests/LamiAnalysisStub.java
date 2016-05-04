/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Michael Jeanson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.lami.core.tests;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysis;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Extension of {@link LamiAnalysis} used for tests.
 */
public class LamiAnalysisStub extends LamiAnalysis {

    private final String fMetaDatafilename;
    private final String fResultFilename;

    /**
     * Constructor.
     *
     * @param metaDatafilename
     *          Filename of the JSON metadata file.
     * @param resultFilename
     *          Filename of the JSON results file.
     */
    protected LamiAnalysisStub(String metaDatafilename, String resultFilename) {
        super("Stub Analysis", false, o -> true, Collections.singletonList("StubExecutable"));
        fMetaDatafilename = metaDatafilename;
        fResultFilename = resultFilename;
    }

    @Override
    public @NonNull String getName() {
        return "StubName";
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

    private static String readLamiFile(String filename) {
        String fileContent = "";
        try {
            URL url = new URL("platform:/plugin/org.eclipse.tracecompass.analysis.lami.core.tests/testfiles/" + filename);

            try (InputStream inputStream = url.openConnection().getInputStream()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                fileContent = in.lines().collect(Collectors.joining());
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }

        return fileContent;
    }
}
