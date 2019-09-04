/*******************************************************************************
 * Copyright (c) 2016, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.remote.ui.messages.RemoteMessages;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfileElement;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;

/**
 * Wizard for remote fetching of logs and traces.
 */
public class RemoteFetchLogWizard extends Wizard implements IImportWizard {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String FETCH_LOG_WIZARD = "RemoteFetchLogWizard"; //$NON-NLS-1$
    private static final String PLUGIN_ID = Activator.PLUGIN_ID;
    private static final String ICON_PATH = "icons/elcl16/fetch_log_wiz.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private IStructuredSelection fSelection;
    private RemoteFetchLogWizardPage fFetchLogWizardPage;
    private RemoteFetchLogWizardRemotePage fFetchLogRemotePage;

    private @Nullable RemoteImportProfileElement fRemoteProfile = null;
    private @Nullable String fExperimentName = null;

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
    /**
     * Standard constructor
     */
    public RemoteFetchLogWizard() {
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings
                .getSection(FETCH_LOG_WIZARD);
        if (section == null) {
            section = workbenchSettings.addNewSection(FETCH_LOG_WIZARD);
        }
        setDialogSettings(section);
    }

    /**
     * Create wizard with pre-defined remote profile
     * @param profile
     *              a remote profile
     * @param experimentName
     *          A name of an experiment to create and add traces to, or null
     *          for no experiment
     */
    public RemoteFetchLogWizard(@NonNull RemoteImportProfileElement profile, @Nullable String experimentName) {
        this();
        fRemoteProfile = profile;
        fExperimentName = experimentName;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        fSelection = selection;

        List<?> selectedResources = IDE.computeSelectedResources(selection);
        if (!selectedResources.isEmpty()) {
            fSelection = new StructuredSelection(selectedResources);
        }

        setWindowTitle(RemoteMessages.RemoteFetchLogWizard_Title);
        setDefaultPageImageDescriptor(ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, ICON_PATH).orElse(null));
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        if (fRemoteProfile == null) {
            fFetchLogWizardPage = new RemoteFetchLogWizardPage(RemoteMessages.RemoteFetchLogWizardPage_Title, fSelection);
            addPage(fFetchLogWizardPage);
        }
        fFetchLogRemotePage = new RemoteFetchLogWizardRemotePage(RemoteMessages.RemoteFetchLogWizardRemotePage_Title, fSelection, fRemoteProfile, fExperimentName);
        addPage(fFetchLogRemotePage);
    }

    @Override
    public boolean performFinish() {
        return fFetchLogRemotePage.finish();
    }

    @Override
    public boolean performCancel() {
        fFetchLogRemotePage.cancel();
        return super.performCancel();
    }

    @Override
    public boolean canFinish() {
        if (fFetchLogWizardPage != null) {
            return fFetchLogWizardPage.canFlipToNextPage();
        }
        return super.canFinish();
    }
}
