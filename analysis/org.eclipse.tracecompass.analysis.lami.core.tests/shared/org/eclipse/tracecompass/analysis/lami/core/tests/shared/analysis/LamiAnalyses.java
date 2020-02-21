/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.lami.core.tests.shared.analysis;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Enumeration of the pre-defined lami results, to be used by various analyses
 *
 * @author Geneviève Bastien
 */
public enum LamiAnalyses {

    /**
     * An analysis with no data
     */
    EMPTY("empty", "stubAnalysis/metadata/metadata.SchedLatencyLog", "stubAnalysis/data/analysisEmpty.data"),
    /**
     * An analysis with multiple row and various data types
     */
    MULTIPLE_ROW("multipleRows", "stubAnalysis/metadata/metadata.SchedLatencyLog", "stubAnalysis/data/analysisMultipleRow.data"),
    /**
     * An analysis returning a few times the same row, with various data types
     */
    MULTIPLE_SIMILAR_ROW("multipleSimilarRows", "stubAnalysis/metadata/metadata.SchedLatencyLog", "stubAnalysis/data/analysisMultipleSimilarRow.data"),
    /**
     * An analysis with only one row of data
     */
    ONE_ROW("oneRow", "stubAnalysis/metadata/metadata.SchedLatencyLog", "stubAnalysis/data/analysisOneRow.data");

    private final @NonNull LamiAnalysisStub fAnalysis;

    LamiAnalyses(@NonNull String name, @NonNull String metadata, @NonNull String data) {
        fAnalysis = new LamiAnalysisStub(name, metadata, data);
    }

    /**
     * Get the LAMI analysis from this enum object
     *
     * @return The LAMI analysis
     */
    public @NonNull LamiAnalysisStub getAnalysis() {
        return fAnalysis;
    }
}
