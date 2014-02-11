/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.callstack;

import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * The base classes for analyses who want to populate the CallStack View.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public abstract class AbstractCallStackAnalysis extends TmfStateSystemAnalysisModule {

    /**
     * Abstract constructor (should only be called via the sub-classes'
     * constructors.
     */
    public AbstractCallStackAnalysis() {
        super();
        registerOutput(new TmfAnalysisViewOutput(CallStackView.ID));
    }
}
