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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.internal.tmf.remote.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.remote.ui.messages.RemoteMessages;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportConnectionNodeElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model.RemoteImportProfileElement;
import org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.preferences.RemoteProfilesPreferencePage;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * Wizard page for selection and managing remote profiles.
 */
public class RemoteFetchLogWizardPage extends ImportTraceWizardPage {

    // ------------------------------------------------------------------------
    // Constant(s)
    // ------------------------------------------------------------------------
    private static final String PAGE_NAME = "org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.RemoteFetchLogWizardPage"; //$NON-NLS-1$
    private static final String ICON_PATH = "icons/elcl16/fetch_log_wiz.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private Combo fProfileNameCombo;
    private Text fNodesText;

    // Button to overwrite existing resources or not
    private Button fOverwriteExistingResourcesCheckbox;

    private List<RemoteImportProfileElement> fProfiles = new ArrayList<>();
    private RemoteImportProfileElement fProfile;

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param title
     *              Name of page
     * @param selection
     *              Current selection
     */
    public RemoteFetchLogWizardPage(String title, IStructuredSelection selection) {
        super(PAGE_NAME, selection);
        setTitle(title);
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, ICON_PATH));
        setDescription(RemoteMessages.RemoteFetchLogWizardPage_Description);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public boolean finish() {
        // Nothing to do for this page
        return true;
    }

    @Override
    public boolean canFlipToNextPage() {
        return fProfile != null;
    }

    // ------------------------------------------------------------------------
    // Source Group
    // ------------------------------------------------------------------------

    @Override
    protected void createSourceGroup(final Composite parent) {
        Composite directoryContainerGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        directoryContainerGroup.setLayout(layout);
        directoryContainerGroup.setFont(parent.getFont());
        directoryContainerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label profileLabel = new Label(directoryContainerGroup, SWT.NONE);
        profileLabel.setText(RemoteMessages.RemoteProfilesPreferencePage_ProfileNameLabel);
        profileLabel.setFont(parent.getFont());

        fProfileNameCombo = new Combo(directoryContainerGroup, SWT.BORDER | SWT.READ_ONLY);
        GridData pdata = new GridData(SWT.FILL, SWT.FILL, true, false);
        pdata.widthHint = SIZING_TEXT_FIELD_WIDTH;
        fProfileNameCombo.setLayoutData(pdata);
        fProfileNameCombo.setFont(parent.getFont());

        Button manageProfilesButton = new Button(directoryContainerGroup, SWT.NONE);
        manageProfilesButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(parent.getShell(),
                        RemoteProfilesPreferencePage.ID,
                        new String[] { RemoteProfilesPreferencePage.ID },
                        null);
                RemoteProfilesPreferencePage page = (RemoteProfilesPreferencePage) dialog.getSelectedPage();
                page.setSelectedProfile(fProfileNameCombo.getText());
                if (dialog.open() == Window.OK) {
                    fProfiles.clear();
                    fProfile = null;
                    updateProfileData();
                    if (page.getSelectedProfile() != null) {
                        int index = fProfileNameCombo.indexOf(page.getSelectedProfile());
                        fProfileNameCombo.select(index);
                    }
                    updateFromSourceField();
                }
            }
        });

        manageProfilesButton.setText(RemoteMessages.RemoteFetchLogWizardPage_ManageProfileLabel);

        Label nodesLabel = new Label(directoryContainerGroup, SWT.NONE);
        nodesLabel.setText(RemoteMessages.RemoteFetchLogWizardPage_NodesLabel);
        nodesLabel.setFont(parent.getFont());

        fNodesText = new Text(directoryContainerGroup, SWT.NONE);
        GridData gd_nodeText = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd_nodeText.horizontalSpan = 2;
        gd_nodeText.widthHint = 0;
        fNodesText.setLayoutData(gd_nodeText);
        fNodesText.setEditable(false);
        fNodesText.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                updateNodesText();
            }
        });

        updateProfileData();
        updateFromSourceField();

        fProfileNameCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFromSourceField();
            }
        });

        setErrorMessage(null);
        setPageComplete(true);
    }

    @Override
    public boolean validateSourceGroup() {
        return true;
    }

    // ------------------------------------------------------------------------
    // Options
    // ------------------------------------------------------------------------
    @Override
    protected void createOptionsGroupButtons(Group optionsGroup) {
        // Overwrite checkbox
        fOverwriteExistingResourcesCheckbox = new Button(optionsGroup, SWT.CHECK);
        fOverwriteExistingResourcesCheckbox.setFont(optionsGroup.getFont());
        fOverwriteExistingResourcesCheckbox.setText(Messages.ImportTraceWizard_OverwriteExistingTrace);
        fOverwriteExistingResourcesCheckbox.setSelection(false);
        fOverwriteExistingResourcesCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateNextPage();
                setPageComplete(fProfile != null);
            }
        });

        updateWidgetEnablements();
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    private void updateProfileData() {
        fProfileNameCombo.removeAll();
        fProfiles = RemoteProfilesPreferencePage.getRemoteProfiles(new NullProgressMonitor());
        int i = 0;
        for (RemoteImportProfileElement profile : fProfiles) {
            fProfileNameCombo.add(profile.getProfileName(), i++);
        }
        if (i > 0) {
            fProfileNameCombo.select(0);
        }
    }

    private void updateFromSourceField() {
        int index = fProfileNameCombo.getSelectionIndex();
        if (index < 0) {
            updateNodesText();
            updateNextPage();
            setPageComplete(false);
            return;
        }
        fProfile = fProfiles.get(index);

        updateNodesText();
        updateNextPage();

        setPageComplete(true);
    }

    private void updateNodesText() {
        if (fProfile == null) {
            fNodesText.setText(""); //$NON-NLS-1$
            fNodesText.setToolTipText(null);
            return;
        }

        StringBuilder text = new StringBuilder();
        StringBuilder tooltip = new StringBuilder();
        for (TracePackageElement element : fProfile.getChildren()) {
            if (element instanceof RemoteImportConnectionNodeElement) {
                RemoteImportConnectionNodeElement node = (RemoteImportConnectionNodeElement) element;
                if (text.length() != 0) {
                    text.append(", "); //$NON-NLS-1$
                    tooltip.append('\n');
                }
                String nodeInfo = node.getName() + " (" + //$NON-NLS-1$
                        node.getURI().toString()+ ')';
                text.append(nodeInfo);
                tooltip.append(nodeInfo);
            }
        }
        fNodesText.setText(text.toString());
        fNodesText.setToolTipText(null);
        while (fNodesText.computeSize(SWT.DEFAULT, SWT.DEFAULT).x > fNodesText.getSize().x && text.length() > 0) {
            text.deleteCharAt(text.length() - 1);
            fNodesText.setText(text.toString() + "..."); //$NON-NLS-1$
            fNodesText.setToolTipText(tooltip.toString());
        }
    }

    private void updateNextPage() {
        IWizardPage nextPage = getNextPage();
        if (nextPage instanceof RemoteFetchLogWizardRemotePage) {
            ((RemoteFetchLogWizardRemotePage) nextPage).setPageData(
                    fProfile,
                    fOverwriteExistingResourcesCheckbox != null ?
                            fOverwriteExistingResourcesCheckbox.getSelection() : false);
        }
    }
}
