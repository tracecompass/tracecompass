/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;

/**
 * Project model element for the "On-Demand Analyses" element, which goes under
 * individual trace and experiment elements.
 *
 * It will list the available implementations of IOnDemandAnalysis that can be
 * executed on this particular trace.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfOnDemandAnalysesElement extends TmfProjectModelElement {

    /**
     * Element of the resource path
     */
    public static final String PATH_ELEMENT = ".ondemand-analyses"; //$NON-NLS-1$

    private static final String ELEMENT_NAME = Messages.TmfOnDemandAnalysesElement_Name;

    /**
     * Constructor
     *
     * @param resource
     *            The resource to be associated with this element
     * @param parent
     *            The parent element
     */
    protected TmfOnDemandAnalysesElement(IResource resource, TmfCommonProjectElement parent) {
        super(ELEMENT_NAME, resource, parent);
    }

    @Override
    public TmfCommonProjectElement getParent() {
        /* Type enforced at constructor */
        return (TmfCommonProjectElement) super.getParent();
    }

    @Override
    public Image getIcon() {
        return TmfProjectModelIcons.ONDEMAND_ANALYSES_ICON;
    }

    @Override
    protected void refreshChildren() {
        /* No children at the moment */
    }

}
