/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisOutput;
import org.eclipse.linuxtools.tmf.ui.analysis.TmfAnalysisViewOutput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Class for project elements of type analysis output
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfAnalysisOutputElement extends TmfProjectModelElement {

    private final IAnalysisOutput fOutput;

    /**
     * Constructor
     *
     * @param name
     *            Name of the view
     * @param resource
     *            Resource for the view
     * @param parent
     *            Parent analysis of the view
     * @param output
     *            The output object
     */
    protected TmfAnalysisOutputElement(String name, IResource resource, ITmfProjectModelElement parent, IAnalysisOutput output) {
        super(name, resource, parent);
        fOutput = output;
        parent.addChild(this);
    }

    @Override
    public TmfProjectElement getProject() {
        return getParent().getProject();
    }

    /**
     * Gets the icon of the view, if applicable
     *
     * @return The view icon or null if output is not a view
     */
    public Image getIcon() {
        if (fOutput instanceof TmfAnalysisViewOutput) {
            IViewDescriptor descr = PlatformUI.getWorkbench().getViewRegistry().find(
                    ((TmfAnalysisViewOutput) fOutput).getViewId());
            if (descr != null) {
                return descr.getImageDescriptor().createImage();
            }
        }
        return null;
    }

    /**
     * Outputs the analysis
     */
    public void outputAnalysis() {
        ITmfProjectModelElement parent = getParent();
        if (parent instanceof TmfAnalysisElement) {
            ((TmfAnalysisElement) parent).activateParent();
            fOutput.requestOutput();
        }
    }

}
