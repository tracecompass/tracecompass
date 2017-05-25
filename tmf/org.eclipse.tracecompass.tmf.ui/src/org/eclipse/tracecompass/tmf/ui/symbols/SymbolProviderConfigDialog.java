/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert Kiss - Initial API and implementation
 *   Mikael Ferland - Enable resizing of symbol provider dialogs
 *
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.symbols;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * This class shall be used to configure one or more {@link ISymbolProvider}. It
 * receives an array of {@link ISymbolProviderPreferencePage} and creates a
 * dialog that can be used to configure the corresponding providers. If the
 * {@link #open()} method exits with {@link IDialogConstants#OK_ID} the caller
 * shall assume that the corresponding {@link ISymbolProvider}'s have a new
 * configuration.
 *
 *
 * @author Robert Kiss
 * @since 2.0
 *
 */
public class SymbolProviderConfigDialog extends TitleAreaDialog implements IPreferencePageContainer {

    private ISymbolProviderPreferencePage[] fPreferencePages;
    private CTabItem[] fPageTabs;
    private CTabFolder fTabFolder;

    private IRunnableWithProgress configJob = (monitor) -> {
        // saving the configuration is fast and need UI access
        SymbolProviderConfigDialog.this.getContents().getDisplay().syncExec(() -> {
            for (int i = 0; i < fPreferencePages.length; i++) {
                ISymbolProviderPreferencePage page = fPreferencePages[i];
                page.saveConfiguration();
            }
        });
        monitor.beginTask(Messages.SymbolProviderConfigDialog_loadingConfigurations, fPreferencePages.length * 100);
        try {
            for (int i = 0; i < fPreferencePages.length; i++) {
                ISymbolProviderPreferencePage page = fPreferencePages[i];
                page.getSymbolProvider().loadConfiguration(monitor);
                monitor.worked(100);
            }
        } finally {
            monitor.done();
        }
    };

    /**
     * Create a new dialog that will use the given shall and preference pages.
     *
     * @param parentShell
     *            The parent shell
     * @param pages
     *            the pages that provides the configuration UI for
     *            {@link ISymbolProvider}'s. The array shall not be empty and
     *            shall not contain null elements.
     *
     */
    public SymbolProviderConfigDialog(Shell parentShell, ISymbolProviderPreferencePage... pages) {
        super(parentShell);
        fPreferencePages = pages;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.SymbolProviderConfigDialog_title);
        setTitle(Messages.SymbolProviderConfigDialog_title);
        setMessage(Messages.SymbolProviderConfigDialog_message);

        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());

        // if we have one single provider that we don't need a tab
        if (fPreferencePages.length == 1) {
            attachPreference(composite, fPreferencePages[0]);
            updateMessage(0);
        } else {
            fTabFolder = new CTabFolder(composite, SWT.NONE);
            fTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
            fPageTabs = new CTabItem[fPreferencePages.length];
            for (int i = 0; i < fPreferencePages.length; i++) {
                ISymbolProviderPreferencePage page = fPreferencePages[i];
                fPageTabs[i] = new CTabItem(fTabFolder, SWT.NONE);
                fPageTabs[i].setText(page.getTitle());
                Composite child = new Composite(fTabFolder, SWT.NONE);
                child.setLayout(new GridLayout());
                child.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
                fPageTabs[i].setControl(child);
                attachPreference(child, page);
                updateMessage(i);
            }
            fTabFolder.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateMessage(fTabFolder.indexOf((CTabItem) e.item));
                }
            });
        }
        return composite;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    public boolean isHelpAvailable() {
        return false;
    }

    private void attachPreference(Composite composite, ISymbolProviderPreferencePage page) {
        page.setContainer(this);
        page.createControl(composite);
        page.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        // not used
        return null;
    }

    @Override
    public void updateTitle() {
        // not used
    }

    @Override
    public void updateButtons() {
        // nothing to do
    }

    @Override
    protected void okPressed() {
        boolean cancel = false;
        try {
            new ProgressMonitorDialog(getShell()).run(true, false, configJob);
        } catch (InvocationTargetException e) {
            setMessage(e.getMessage(), IMessageProvider.ERROR);
            cancel = true;
        } catch (InterruptedException e) {
            // ignore
        }
        if (!cancel) {
            super.okPressed();
        }
        TmfSignalManager.dispatchSignal(new TmfSymbolProviderUpdatedSignal(this));
    }

    @Override
    public void updateMessage() {
        if (fTabFolder == null) {
            updateMessage(0);
            return;
        }
        int curSelectionIndex = fTabFolder.getSelectionIndex();
        if (curSelectionIndex >= 0) {
            updateMessage(curSelectionIndex);
        }
    }

    private void updateMessage(int pageIndex) {
        ISymbolProviderPreferencePage currentPage = fPreferencePages[pageIndex];
        String message = currentPage.getMessage();
        String errorMessage = currentPage.getErrorMessage();
        int messageType = IMessageProvider.NONE;

        if (errorMessage != null) {
            message = errorMessage;
            messageType = IMessageProvider.ERROR;
        }
        setMessage(message, messageType);

        if (fPreferencePages.length > 1) {
            // update the corresponding tab icon
            if (messageType == IMessageProvider.ERROR) {
                fPageTabs[pageIndex].setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
            } else {
                fPageTabs[pageIndex].setImage(null);
            }
        }
    }

}
