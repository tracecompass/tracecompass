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
 *   Mikael Ferland - Adjust title of preference pages for multiple symbol providers
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.symbols;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.internal.tmf.ui.symbols.BasicSymbolProvider.SourceKind;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.symbols.AbstractSymbolProviderPreferencePage;

/**
 * Preference page that allows the user to configure a
 * {@link BasicSymbolProvider}
 *
 * @author Robert Kiss
 *
 */
public class BasicSymbolProviderPreferencePage extends AbstractSymbolProviderPreferencePage {

    private Button fRadioBinaryFile;
    private Text fTextBinaryFile;
    private Button fButtonBrowseBinary;
    private Button fRadioMappingFile;
    private Text fTextMappingFile;
    private Button fButtonBrowseMapping;

    /**
     * Creates a new object for the given provider
     *
     * @param provider
     *            a non-null provider
     */
    public BasicSymbolProviderPreferencePage(@NonNull BasicSymbolProvider provider) {
        super(provider);
        setDescription(MessageFormat.format(Messages.BasicSymbolProviderPrefPage_description, provider.getTrace().getName()));
        setValid(true);
        setTitle(MessageFormat.format(Messages.BasicSymbolProviderPrefPage_tabTitle, provider.getTrace().getName()));
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(3, false));

        fRadioBinaryFile = new Button(composite, SWT.RADIO);
        fRadioBinaryFile.setText(Messages.BasicSymbolProviderPrefPage_radioBinaryFile_text);
        fRadioBinaryFile.setToolTipText(Messages.BasicSymbolProviderPrefPage_radioBinaryFile_tooltip);
        fRadioBinaryFile.setSelection(true);
        fRadioBinaryFile.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switchToSourceKind(SourceKind.BINARY, true);
            }
        });

        fTextBinaryFile = new Text(composite, SWT.BORDER);
        fTextBinaryFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fTextBinaryFile.setEditable(false);

        fButtonBrowseBinary = new Button(composite, SWT.NONE);
        fButtonBrowseBinary.setText(Messages.BasicSymbolProviderPrefPage_btnBrowse);
        fButtonBrowseBinary.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                browseForFile(fTextBinaryFile, Messages.BasicSymbolProviderPrefPage_ImportBinaryFileDialogTitle);
            }
        });

        fRadioMappingFile = new Button(composite, SWT.RADIO);
        fRadioMappingFile.setText(Messages.BasicSymbolProviderPrefPage_radioMappingFile_text);
        fRadioMappingFile.setToolTipText(Messages.BasicSymbolProviderPrefPage_radioMappingFile_tooltip);
        fRadioMappingFile.setSelection(false);
        fRadioMappingFile.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switchToSourceKind(SourceKind.MAPPING, true);
            }
        });

        fTextMappingFile = new Text(composite, SWT.BORDER);
        fTextMappingFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fTextMappingFile.setEnabled(false);
        fTextMappingFile.setEditable(false);

        fButtonBrowseMapping = new Button(composite, SWT.NONE);
        fButtonBrowseMapping.setText(Messages.BasicSymbolProviderPrefPage_btnBrowse);
        fButtonBrowseMapping.setEnabled(false);
        fButtonBrowseMapping.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                browseForFile(fTextMappingFile, Messages.BasicSymbolProviderPrefPage_ImportMappingDialogTitle);
            }
        });

        loadProviderSettings();

        return composite;
    }

    private void browseForFile(Text fileField, String dialogTitle) {
        FileDialog fileDialog = TmfFileDialogFactory.create(getShell(), SWT.OPEN);
        fileDialog.setText(dialogTitle);
        String filePath = fileDialog.open();
        if (filePath != null) {
            fileField.setText(filePath);
            broadcastChanges();
        }

    }

    private void loadProviderSettings() {
        BasicSymbolProvider provider = (BasicSymbolProvider) getSymbolProvider();
        String source = provider.getConfiguredSource();
        SourceKind sourceKind = provider.getConfiguredSourceKind();
        if (source != null) {
            if (sourceKind == SourceKind.BINARY) {
                fTextBinaryFile.setText(source);
            } else {
                fTextMappingFile.setText(source);
            }
        }

        switchToSourceKind(sourceKind, false);
        broadcastChanges();
    }

    private void switchToSourceKind(@NonNull SourceKind kind, boolean broadcastChanges) {
        fRadioBinaryFile.setSelection(kind == SourceKind.BINARY);
        fTextBinaryFile.setEnabled(kind == SourceKind.BINARY);
        fButtonBrowseBinary.setEnabled(kind == SourceKind.BINARY);

        fRadioMappingFile.setSelection(kind == SourceKind.MAPPING);
        fTextMappingFile.setEnabled(kind == SourceKind.MAPPING);
        fButtonBrowseMapping.setEnabled(kind == SourceKind.MAPPING);
        if (broadcastChanges) {
            broadcastChanges();
        }
    }

    @Override
    public void saveConfiguration() {
        BasicSymbolProvider provider = (BasicSymbolProvider) getSymbolProvider();
        provider.setConfiguredSource(getCurrentSource(), getCurrentSourceKind());
    }

    private @NonNull SourceKind getCurrentSourceKind() {
        if (fRadioBinaryFile.getSelection()) {
            return SourceKind.BINARY;
        }
        return SourceKind.MAPPING;
    }

    private String getCurrentSource() {
        if (fRadioBinaryFile.getSelection()) {
            return fTextBinaryFile.getText();
        }
        return fTextMappingFile.getText();
    }

    private void broadcastChanges() {
        String filePath = getCurrentSource();
        String errorMessage = null;
        if (filePath != null && filePath.length() > 0) {
            File file = new File(filePath);
            if (!file.isFile()) {
                errorMessage = Messages.BasicSymbolProviderPrefPage_errorFileDoesNotExists;
            }
        }
        setErrorMessage(errorMessage);
        setValid(errorMessage == null);
    }

}
