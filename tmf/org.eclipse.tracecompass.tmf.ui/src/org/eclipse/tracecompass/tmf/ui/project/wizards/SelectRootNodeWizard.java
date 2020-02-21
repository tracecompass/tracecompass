/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Cédric Biancheri - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;

/**
 * IWizard implementation to select a root node for synchronization.
 *
 * @author Cedric Biancheri
 * @since 2.0
 *
 */
public class SelectRootNodeWizard extends Wizard {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final TmfExperimentElement fExperiment;
    private SelectRootNodeWizardPage fSelectRootNodeWizardPage;
    private TmfTraceElement rootNode;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param experiment
     *            The experiment model element
     */
    public SelectRootNodeWizard(TmfExperimentElement experiment) {
        fExperiment = experiment;
        setWindowTitle(Messages.SelectRootNodeWizard_WindowTitle);
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        fSelectRootNodeWizardPage = new SelectRootNodeWizardPage(fExperiment);
        addPage(fSelectRootNodeWizardPage);
        // TODO Add pages for new features (select synchronization algorithm)
    }

    @Override
    public boolean performFinish() {
        boolean finishRootNodeWizardPage = fSelectRootNodeWizardPage.performFinish();
        setRootNode(fSelectRootNodeWizardPage.getRootNode());
        return finishRootNodeWizardPage;
    }

    /**
     * Gets the root node.
     *
     * @return The root node
     */
    public TmfTraceElement getRootNode() {
        return rootNode;
    }

    /**
     * Sets the root node
     *
     * @param rootNode
     *            The root node
     */
    private void setRootNode(TmfTraceElement rootNode) {
        this.rootNode = rootNode;
    }

}
