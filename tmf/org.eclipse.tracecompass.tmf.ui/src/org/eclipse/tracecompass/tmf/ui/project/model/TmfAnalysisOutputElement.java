/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisOutput;
import org.eclipse.tracecompass.tmf.ui.analysis.TmfAnalysisViewOutput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Class for project elements of type analysis output
 *
 * @author Geneviève Bastien
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
     * @since 2.0
     */
    protected TmfAnalysisOutputElement(String name, IResource resource, TmfAnalysisElement parent, IAnalysisOutput output) {
        super(name, resource, parent);
        fOutput = output;
    }

    @Override
    public Image getIcon() {
        if (fOutput instanceof TmfAnalysisViewOutput) {
            IViewDescriptor descr = PlatformUI.getWorkbench().getViewRegistry().find(
                    ((TmfAnalysisViewOutput) fOutput).getViewId());
            if (descr != null) {
                Activator bundle = Activator.getDefault();
                String key = descr.getId();
                Image icon = bundle.getImageRegistry().get(key);
                if (icon == null) {
                    icon = descr.getImageDescriptor().createImage();
                    bundle.getImageRegistry().put(key, icon);
                }
                if (icon != null) {
                    return icon;
                }
            }
        }
        return TmfProjectModelIcons.DEFAULT_VIEW_ICON;
    }

    /**
     * Outputs the analysis
     */
    public void outputAnalysis() {
        ITmfProjectModelElement parent = getParent();
        if (parent instanceof TmfAnalysisElement) {
            ((TmfAnalysisElement) parent).activateParentTrace();
            fOutput.requestOutput();
        }
    }

    @Override
    protected void refreshChildren() {
        /* Nothing to do */
    }

}
