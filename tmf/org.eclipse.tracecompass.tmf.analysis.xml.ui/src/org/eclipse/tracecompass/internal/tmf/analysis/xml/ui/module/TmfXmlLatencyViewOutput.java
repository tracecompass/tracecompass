/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.module;

import org.eclipse.tracecompass.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * Class overriding the default analysis view output for XML pattern analysis
 * latency views
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlLatencyViewOutput extends TmfAnalysisViewOutput {

    private String fLabel;

    /**
     * @param viewid
     *            The ID of the view
     * @param label
     *            The label of view
     */
    public TmfXmlLatencyViewOutput(String viewid, String label) {
        super(viewid, label);
        fLabel = label;
    }

    @Override
    public String getName() {
        return fLabel;
    }
}
