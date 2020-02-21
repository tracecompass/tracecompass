/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.IOnDemandAnalysis;

/**
 * Project element for built-in on-demand analysis.
 *
 * These cannot be deleted by the user.
 *
 * @author Philippe Proulx
 * @since 2.0
 */
public class TmfBuiltInOnDemandAnalysisElement extends TmfOnDemandAnalysisElement {

    /**
     * Constructor
     *
     * @param analysisName
     *            Name of the element
     * @param resource
     *            Workspace resource
     * @param parent
     *            Parent element, should be the "on-demand analyses" one
     * @param analysis
     *            The actual analysis represented by this element
     */
    protected TmfBuiltInOnDemandAnalysisElement(@NonNull String analysisName, @NonNull IResource resource, @NonNull TmfOnDemandAnalysesElement parent, @NonNull IOnDemandAnalysis analysis) {
        super(analysisName, resource, parent, analysis);
    }

    @Override
    public @NonNull Image getIcon() {
        return TmfProjectModelIcons.BUILT_IN_ONDEMAND_ICON;
    }

}
