/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlPatternCu;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * State system analysis for pattern matching analysis described in XML. This
 * module will parse the XML description of the analyses and execute it against
 * the trace and will execute all required action
 *
 * @author Jean-Christian Kouame
 */
public class XmlPatternStateSystemModule extends TmfStateSystemAnalysisModule {

    private final @NonNull ISegmentListener fListener;
    private final TmfXmlPatternCu fPatternCu;

    /**
     * Constructor
     *
     * @param listener
     *            Listener for segments that will be created
     * @param patternCu
     *            The pattern compilation unit
     */
    public XmlPatternStateSystemModule(@NonNull ISegmentListener listener, TmfXmlPatternCu patternCu) {
        super();
        fListener = listener;
        fPatternCu = patternCu;
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        return fPatternCu.generate(checkNotNull(getTrace()), fListener);
    }

}
