/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add methods to get attribute paths
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.callstack;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * The base classes for analyses who want to populate the CallStack View.
 *
 * @author Alexandre Montplaisir
 */
/*
 * FIXME: deprecate at next release when we can move the callstack view to an
 * extension point
 */
@NonNullByDefault
public abstract class AbstractCallStackAnalysis extends CallStackAnalysis {

    /**
     * Abstract constructor (should only be called via the sub-classes'
     * constructors.
     */
    protected AbstractCallStackAnalysis() {
        super();
        registerOutput(new TmfAnalysisViewOutput(CallStackView.ID));
    }
}
