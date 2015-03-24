/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.remote.ui.messages.RemoteMessages;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Wizard for remote fetching of logs and traces.
 */
public class RemoteFetchLogWizard extends Wizard implements IImportWizard {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String FETCH_LOG_WIZARD = "RemoteFetchLogWizard"; //$NON-NLS-1$
    static private final String PLUGIN_ID = Activator.PLUGIN_ID;
    static private final String ICON_PATH = "icons/elcl16/fetch_log_wiz.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private IStructuredSelection fSelection;
    private RemoteFetchLogWizardPage fFetchLogWizardPage;
    private RemoteFetchLogWizardRemotePage fFetchLogRemotePage;

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
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH));
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        fFetchLogWizardPage = new RemoteFetchLogWizardPage(RemoteMessages.RemoteFetchLogWizardPage_Title, fSelection);
        addPage(fFetchLogWizardPage);
        fFetchLogRemotePage = new RemoteFetchLogWizardRemotePage(RemoteMessages.RemoteFetchLogWizardRemotePage_Title, fSelection);
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
        return fFetchLogWizardPage.canFlipToNextPage();
    }

}
