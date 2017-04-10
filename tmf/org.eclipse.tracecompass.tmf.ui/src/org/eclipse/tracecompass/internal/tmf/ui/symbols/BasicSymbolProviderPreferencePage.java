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
 *   Mikael Ferland - Refactor API to support multiple symbol files
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.symbols;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.tracecompass.internal.tmf.core.callstack.FunctionNameMapper;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.symbols.AbstractSymbolProviderPreferencePage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.collect.ImmutableList;

/**
 * Preference page that allows the user to configure a
 * {@link BasicSymbolProvider}
 *
 * @author Robert Kiss
 * @author Mikael Ferland
 *
 */
public class BasicSymbolProviderPreferencePage extends AbstractSymbolProviderPreferencePage {

    private static final ImageDescriptor BIN_ICON = getImageDescriptor("icons/obj16/binary_mapping_file.gif"); //$NON-NLS-1$
    private static final ImageDescriptor TXT_ICON = getImageDescriptor("icons/obj16/text_mapping_file.gif"); //$NON-NLS-1$

    // Typical magic numbers
    private static final byte @NonNull[] ELF_HEADER = { 0x7f, 'E', 'L', 'F' }; // ...for Linux executables
    private static final byte @NonNull[] MZ_HEADER = { 'M', 'Z' }; // ...for Windows executables
    private static final byte @NonNull[] COMPILED_JAVA_CLASS_HEADER = { 'C', 'A', 'F', 'E', 'B', 'A', 'B', 'E' }; // ...for compiled Java class files
    private static final ImmutableList<byte[]> BINARY_HEADERS = ImmutableList.of(ELF_HEADER, MZ_HEADER, COMPILED_JAVA_CLASS_HEADER);

    private BasicSymbolProvider fSymbolProvider;

    private Button fRemoveFile;
    private Button fPriorityUp;
    private Button fPriorityDown;
    private TableViewer fMappingTable;
    private final @NonNull List<@NonNull MappingFile> fMappingFiles = new ArrayList<>();

    ColumnLabelProvider clp = new ColumnLabelProvider() {

        @Override
        public String getText(Object element) {
            if (element instanceof MappingFile) {
                return ((MappingFile) element).getFullPath();
            }
            return null;
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof MappingFile) {
                // Add binary/mapping icons to the image registry if needed
                ImageRegistry registry = Activator.getDefault().getImageRegistry();
                Image binImage = registry.get(BIN_ICON.toString());
                if (binImage == null) {
                    registry.put(BIN_ICON.toString(), BIN_ICON);
                    binImage = registry.get(BIN_ICON.toString());
                }
                Image txtImage = registry.get(TXT_ICON.toString());
                if (txtImage == null) {
                    registry.put(TXT_ICON.toString(), TXT_ICON);
                    txtImage = registry.get(TXT_ICON.toString());
                }

                return ((MappingFile) element).isBinaryFile() ? binImage : txtImage;
            }

            return null;
        }
    };

    /**
     * Creates a new object for the given provider
     *
     * @param provider
     *            a non-null provider
     */
    public BasicSymbolProviderPreferencePage(@NonNull BasicSymbolProvider provider) {
        super(provider);
        fSymbolProvider = provider;
        setDescription(MessageFormat.format(Messages.BasicSymbolProviderPrefPage_description, provider.getTrace().getName()));
        setValid(true);
        setTitle(MessageFormat.format(Messages.BasicSymbolProviderPrefPage_tabTitle, provider.getTrace().getName()));
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(2, false));

        fMappingTable = new TableViewer(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        fMappingTable.setContentProvider(ArrayContentProvider.getInstance());
        Table table = fMappingTable.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        table.setHeaderVisible(false);
        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Enable/disable buttons (except "Add...")
                int selectionCount = table.getSelectionCount();
                boolean enablePriorityButtons = (table.getItemCount() >= 2 && selectionCount == 1);
                fPriorityUp.setEnabled(enablePriorityButtons);
                fPriorityDown.setEnabled(enablePriorityButtons);
                fRemoveFile.setEnabled(selectionCount >= 1);
            }
        });
        TableViewerColumn col = new TableViewerColumn(fMappingTable, SWT.None);
        col.setLabelProvider(clp);
        fMappingFiles.addAll(fSymbolProvider.getMappingFiles());
        fMappingTable.setInput(fMappingFiles);
        col.getColumn().pack();

        Composite buttonContainer = new Composite(composite, SWT.NULL);
        buttonContainer.setLayout(new GridLayout());
        buttonContainer.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        Button fAddFile = new Button(buttonContainer, SWT.NONE);
        fAddFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fAddFile.setText(Messages.BasicSymbolProviderPrefPage_addFile_text);
        fAddFile.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = TmfFileDialogFactory.create(Display.getCurrent().getActiveShell(), SWT.OPEN | SWT.MULTI);
                dialog.open();
                addSelectedMappingFiles(dialog.getFilterPath(), dialog.getFileNames());
            }
        });

        fRemoveFile = new Button(buttonContainer, SWT.NONE);
        fRemoveFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fRemoveFile.setText(Messages.BasicSymbolProviderPrefPage_removeFile_text);
        fRemoveFile.setEnabled(false);
        fRemoveFile.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Files must be removed in reversed order to prevent
                // runtime errors
                int[] indices = fMappingTable.getTable().getSelectionIndices();
                ArrayUtils.reverse(indices);
                for (int index : indices) {
                    fMappingFiles.remove(index);
                }
                fMappingTable.refresh();

                fPriorityUp.setEnabled(false);
                fPriorityDown.setEnabled(false);
                fRemoveFile.setEnabled(false);
            }
        });

        Composite priorityContainer = new Composite(buttonContainer, SWT.NONE);
        GridLayout priorityContainerLayout = new GridLayout();
        priorityContainerLayout.marginTop = 5;
        priorityContainer.setLayout(priorityContainerLayout);
        priorityContainer.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        fPriorityUp = new Button(buttonContainer, SWT.NONE);
        fPriorityUp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fPriorityUp.setText(Messages.BasicSymbolProviderPrefPage_priorityUp_text);
        fPriorityUp.setToolTipText(Messages.BasicSymbolProviderPrefPage_priorityUp_tooltip);
        fPriorityUp.setEnabled(false);
        fPriorityUp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = fMappingTable.getTable().getSelectionIndex();
                if (index > 0) {
                    Collections.swap(fMappingFiles, index, index - 1);
                    fMappingTable.refresh();
                }
            }
        });

        fPriorityDown = new Button(buttonContainer, SWT.NONE);
        fPriorityDown.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fPriorityDown.setText(Messages.BasicSymbolProviderPrefPage_priorityDown_text);
        fPriorityDown.setToolTipText(Messages.BasicSymbolProviderPrefPage_priorityDown_tooltip);
        fPriorityDown.setEnabled(false);
        fPriorityDown.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                if (index < table.getItemCount() - 1) {
                    Collections.swap(fMappingFiles, index, index + 1);
                    fMappingTable.refresh();
                }
            }
        });

        return composite;
    }

    @Override
    public void saveConfiguration() {
        fSymbolProvider.setMappingFiles(fMappingFiles);
    }

    /**
     * Retrieve the image descriptor of an icon
     *
     * @param file
     *            Path leading to the icon image
     */
    private static ImageDescriptor getImageDescriptor(String file) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, file);
    }

    /**
     * Add valid mapping file(s) to the table viewer
     *
     * @param filterPath
     *            Directory in which the mapping files are located
     * @param fileNames
     *            Names of the selected mapping files
     */
    private void addSelectedMappingFiles(String filterPath, String[] fileNames) {
        List<String> invalidFiles = new ArrayList<>();

        for (String fileName : fileNames) {
            String fullPath = filterPath + File.separator + fileName;
            File file = new File(fullPath);

            Map<Long, String> results = null;
            boolean isBinaryFile = isBinaryFile(fullPath);
            if (isBinaryFile) {
                results = FunctionNameMapper.mapFromBinaryFile(file);
            } else if ("txt".equals(FilenameUtils.getExtension(fileName))) { //$NON-NLS-1$
                results = FunctionNameMapper.mapFromNmTextFile(file);
            }

            // results is null if mapping file is invalid
            if (results != null) {
                MappingFile mf = new MappingFile(file.getAbsolutePath(), isBinaryFile, results);
                if (!fMappingFiles.contains(mf)) {
                    fMappingFiles.add(mf);
                }
            } else {
                invalidFiles.add(fullPath);
            }
        }

        if (!invalidFiles.isEmpty()) {
            displayErrorMessage(invalidFiles);
        }
        fMappingTable.refresh();
    }

    /**
     * Determine if a mapping file is binary
     *
     * @param fullPath
     *            Path to the mapping file
     */
    private static boolean isBinaryFile(String fullPath) {
        // 1- Retrieve the first 8 bytes of the file
        byte[] firstBytes = new byte[8];
        try (FileInputStream input = new FileInputStream(fullPath)) {
            input.read(firstBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2- Verify if the bytes correspond to a known magic number for binary
        // files
        for (byte[] header : BINARY_HEADERS) {
            if (firstBytes.length >= header.length && startsWith(header, firstBytes)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if an array begins with the elements of another specified array
     */
    private static boolean startsWith(byte[] a, byte[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Display dialog when the user attempts to import invalid mapping file(s)
     *
     * @param invalidFiles
     *            List of all the invalid mapping files
     */
    private void displayErrorMessage(@NonNull List<String> invalidFiles) {
        StringBuilder errorMessageContent = new StringBuilder();
        errorMessageContent.append(System.lineSeparator()).append(System.lineSeparator());
        invalidFiles.forEach(file -> errorMessageContent.append("•  ").append(file).append(System.lineSeparator())); //$NON-NLS-1$
        MessageDialog.openInformation(
                getShell(),
                Messages.BasicSymbolProviderPrefPage_invalidMappingFileDialogHeader,
                Messages.BasicSymbolProviderPrefPage_invalidMappingFileMessage + errorMessageContent.toString());
    }
}
