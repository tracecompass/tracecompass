/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

/**
 * An abstract wizard page containing common code useful for both import and
 * export trace package wizard pages
 *
 * @author Marc-Andre Laperle
 */
abstract public class AbstractTracePackageWizardPage extends WizardPage {

    private static final int COMBO_HISTORY_LENGTH = 5;
    private static final String STORE_FILE_PATHS_ID = ".STORE_FILEPATHS_ID"; //$NON-NLS-1$

    private final String fStoreFilePathId;
    private final IStructuredSelection fSelection;

    private CheckboxTreeViewer fElementViewer;
    private Button fSelectAllButton;
    private Button fDeselectAllButton;
    private Combo fFilePathCombo;
    private Button fBrowseButton;

    /**
     * Create the trace package wizard page
     *
     * @param pageName
     *            the name of the page
     * @param title
     *            the title for this wizard page, or null if none
     * @param titleImage
     *            the image descriptor for the title of this wizard page, or
     *            null if none
     * @param selection
     *            the current object selection
     */
    protected AbstractTracePackageWizardPage(String pageName, String title, ImageDescriptor titleImage, IStructuredSelection selection) {
        super(pageName, title, titleImage);
        fStoreFilePathId = getName() + STORE_FILE_PATHS_ID;
        fSelection = selection;
    }

    /**
     * Create the element viewer
     *
     * @param compositeParent
     *            the parent composite
     */
    protected void createElementViewer(Composite compositeParent) {
        fElementViewer = new CheckboxTreeViewer(compositeParent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.CHECK);

        fElementViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                TracePackageElement element = (TracePackageElement) event.getElement();
                if (!element.isEnabled()) {
                    fElementViewer.setChecked(element, element.isChecked());
                } else {
                    setSubtreeChecked(fElementViewer, element, true, event.getChecked());
                }
                maintainCheckIntegrity(element);

                if (element.getParent() != null) {
                    // Uncheck everything in this trace if Trace files are unchecked
                    if (element instanceof TracePackageFilesElement) {
                        if (!element.isChecked()) {
                            setSubtreeChecked(fElementViewer, element.getParent(), false, false);
                        }
                    // Check Trace files if anything else is selected
                    } else if (element.isChecked()) {
                        TracePackageElement parent = element.getParent();
                        while (parent != null) {
                            for (TracePackageElement e : parent.getChildren()) {
                                if (e instanceof TracePackageFilesElement) {
                                    setSubtreeChecked(fElementViewer, e, false, true);
                                    break;
                                }
                            }
                            parent = parent.getParent();
                        }
                    }
                }


                updateApproximateSelectedSize();
                updatePageCompletion();
            }

            private void maintainCheckIntegrity(final TracePackageElement element) {
                TracePackageElement parentElement = element.getParent();
                boolean allChecked = true;
                boolean oneChecked = false;
                if (parentElement != null) {
                    if (parentElement.getChildren() != null) {
                        for (TracePackageElement child : parentElement.getChildren()) {
                            boolean checked = fElementViewer.getChecked(child) && !fElementViewer.getGrayed(child);
                            oneChecked |= checked;
                            allChecked &= checked;
                        }
                    }
                    if (oneChecked && !allChecked) {
                        fElementViewer.setGrayChecked(parentElement, true);
                    } else {
                        fElementViewer.setGrayed(parentElement, false);
                        fElementViewer.setChecked(parentElement, allChecked);
                    }
                    maintainCheckIntegrity(parentElement);
                }
            }
        });
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        fElementViewer.getTree().setLayoutData(layoutData);
        fElementViewer.setContentProvider(new TracePackageContentProvider());
        fElementViewer.setLabelProvider(new TracePackageLabelProvider());
    }

    /**
     * Create the input for the element viewer
     *
     * @return the input for the element viewer
     */
    protected abstract Object createElementViewerInput();

    /**
     * Create the file path group that allows the user to type or browse for a
     * file path
     *
     * @param parent
     *            the parent composite
     * @param label
     *            the label to describe the file path (i.e. import/export)
     * @param fileDialogStyle
     *            SWT.OPEN or SWT.SAVE
     */
    protected void createFilePathGroup(Composite parent, String label, final int fileDialogStyle) {

        Composite filePathSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        filePathSelectionGroup.setLayout(layout);
        filePathSelectionGroup.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

        Label destinationLabel = new Label(filePathSelectionGroup, SWT.NONE);
        destinationLabel.setText(label);

        fFilePathCombo = new Combo(filePathSelectionGroup, SWT.SINGLE
                | SWT.BORDER);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        fFilePathCombo.setLayoutData(data);

        fBrowseButton = new Button(filePathSelectionGroup,
                SWT.PUSH);
        fBrowseButton.setText(org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_Browse);
        fBrowseButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                handleFilePathBrowseButtonPressed(fileDialogStyle);
            }
        });
        setButtonLayoutData(fBrowseButton);
    }

    /**
     * Update the page with the file path the current file path selection
     */
    abstract protected void updateWithFilePathSelection();

    /**
     * Creates the buttons for selecting all or none of the elements.
     *
     * @param parent
     *            the parent control
     * @return the button group
     */
    protected Composite createButtonsGroup(Composite parent) {

        // top level group
        Composite buttonComposite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        buttonComposite.setLayout(layout);
        buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));

        fSelectAllButton = new Button(buttonComposite, SWT.PUSH);
        fSelectAllButton.setText(org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_SelectAll);

        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setAllChecked(fElementViewer, true, true);
                updateApproximateSelectedSize();
                updatePageCompletion();
            }
        };
        fSelectAllButton.addSelectionListener(listener);

        fDeselectAllButton = new Button(buttonComposite, SWT.PUSH);
        fDeselectAllButton.setText(org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_DeselectAll);

        listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setAllChecked(fElementViewer, true, false);
                updateApproximateSelectedSize();
                updatePageCompletion();
            }
        };
        fDeselectAllButton.addSelectionListener(listener);

        return buttonComposite;
    }

    /**
     * Restore widget values to the values that they held last time this wizard
     * was used to completion.
     */
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(fStoreFilePathId);
            if (directoryNames == null || directoryNames.length == 0) {
                return;
            }

            for (int i = 0; i < directoryNames.length; i++) {
                fFilePathCombo.add(directoryNames[i]);
            }
        }
    }

    /**
     * Save widget values to Dialog settings
     */
    protected void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            // update directory names history
            String[] directoryNames = settings.getArray(fStoreFilePathId);
            if (directoryNames == null) {
                directoryNames = new String[0];
            }

            directoryNames = addToHistory(directoryNames, getFilePathValue());
            settings.put(fStoreFilePathId, directoryNames);
        }
    }

    /**
     * Determine if the page is complete and update the page appropriately.
     */
    protected void updatePageCompletion() {
        boolean pageComplete = determinePageCompletion();
        setPageComplete(pageComplete);
        if (pageComplete) {
            setErrorMessage(null);
        }
    }

    /**
     * Determine if the page is completed or not
     *
     * @return true if the page is completed, false otherwise
     */
    protected boolean determinePageCompletion() {
        return fElementViewer.getCheckedElements().length > 0 && !getFilePathValue().isEmpty();
    }

    /**
     * Handle error status
     *
     * @param status
     *            the error status
     */
    protected void handleErrorStatus(IStatus status) {

        Throwable exception = status.getException();
        String message = status.getMessage().length() > 0 ? status.getMessage() : org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorOperation;

        if (!status.isMultiStatus()) {
            handleError(message, exception);
            return;
        }

        // Build a string with all the children status messages, exception
        // messages and stack traces
        StringBuilder sb = new StringBuilder();
        for (IStatus childStatus : status.getChildren()) {
            StringBuilder childSb = new StringBuilder();
            if (!childStatus.getMessage().isEmpty()) {
                childSb.append(childStatus.getMessage() + '\n');
            }

            Throwable childException = childStatus.getException();
            if (childException != null) {
                String reason = childException.getMessage();
                // Some system exceptions have no message
                if (reason == null) {
                    reason = childException.toString();
                }

                String stackMessage = getExceptionStackMessage(childException);
                if (stackMessage == null) {
                    stackMessage = reason;
                }

                childSb.append(stackMessage);
            }

            if (childSb.length() > 0) {
                childSb.insert(0, '\n');
                sb.append(childSb.toString());
            }
        }

        // ErrorDialog only prints the call stack for a CoreException
        exception = new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, sb.toString(), null));
        final Status statusWithException = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_ErrorMultipleProblems, exception);

        Activator.getDefault().logError(message, exception);
        ErrorDialog.openError(getContainer().getShell(), org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_InternalErrorTitle, message, statusWithException);
    }

    /**
     * Handle errors occurring in the wizard operations
     *
     * @param message
     *            the error message
     * @param exception
     *            the exception attached to the message
     */
    protected void handleError(String message, Throwable exception) {
        Activator.getDefault().logError(message, exception);
        displayErrorDialog(message, exception);
    }

    private static String getExceptionStackMessage(Throwable exception) {
        String stackMessage = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        exception.printStackTrace(ps);
        ps.flush();
        try {
            baos.flush();
            stackMessage = baos.toString();
        } catch (IOException e) {
        }

        return stackMessage;
    }

    private void displayErrorDialog(String message, Throwable exception) {
        if (exception == null) {
            final Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
            ErrorDialog.openError(getContainer().getShell(), org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_InternalErrorTitle, null, s);
            return;
        }

        String reason = exception.getMessage();
        // Some system exceptions have no message
        if (reason == null) {
            reason = exception.toString();
        }

        String stackMessage = getExceptionStackMessage(exception);
        if (stackMessage == null || stackMessage.isEmpty()) {
            stackMessage = reason;
        }

        // ErrorDialog only prints the call stack for a CoreException
        CoreException coreException = new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, stackMessage, exception));
        final Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, reason, coreException);
        ErrorDialog.openError(getContainer().getShell(), org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.Messages.TracePackage_InternalErrorTitle, message, s);
    }

    /**
     * A version of setSubtreeChecked that is aware of isEnabled
     *
     * @param viewer
     *            the viewer
     * @param element
     *            the element
     * @param enabledOnly
     *            if only enabled elements should be considered
     * @param checked
     *            true if the item should be checked, and false if it should be
     *            unchecked
     */
    protected static void setSubtreeChecked(CheckboxTreeViewer viewer, TracePackageElement element, boolean enabledOnly, boolean checked) {
        if (!enabledOnly || element.isEnabled()) {
            viewer.setChecked(element, checked);
            if (checked) {
                viewer.setGrayed(element, false);
            }
            element.setChecked(checked);
            if (element.getChildren() != null) {
                for (TracePackageElement child : element.getChildren()) {
                    setSubtreeChecked(viewer, child, enabledOnly, checked);
                }
            }
        }
    }

    /**
     * Sets all items in the element viewer to be checked or unchecked
     *
     * @param viewer
     *            the viewer
     * @param enabledOnly
     *            if only enabled elements should be considered
     * @param checked
     *            whether or not items should be checked
     */
    protected static void setAllChecked(CheckboxTreeViewer viewer, boolean enabledOnly, boolean checked) {
        TreeItem[] items = viewer.getTree().getItems();
        for (int i = 0; i < items.length; i++) {
            Object element = items[i].getData();
            setSubtreeChecked(viewer, (TracePackageElement) element, enabledOnly, checked);
        }
    }

    private static void addToHistory(List<String> history, String newEntry) {
        history.remove(newEntry);
        history.add(0, newEntry);

        // since only one new item was added, we can be over the limit
        // by at most one item
        if (history.size() > COMBO_HISTORY_LENGTH) {
            history.remove(COMBO_HISTORY_LENGTH);
        }
    }

    private static String[] addToHistory(String[] history, String newEntry) {
        ArrayList<String> l = new ArrayList<>(Arrays.asList(history));
        addToHistory(l, newEntry);
        String[] r = new String[l.size()];
        l.toArray(r);
        return r;
    }

    /**
     * Open an appropriate file dialog so that the user can specify a file to
     * import/export
     * @param fileDialogStyle
     */
    private void handleFilePathBrowseButtonPressed(int fileDialogStyle) {
        FileDialog dialog = new FileDialog(getContainer().getShell(), fileDialogStyle | SWT.SHEET);
        dialog.setFilterExtensions(new String[] { "*.zip;*.tar.gz;*.tar;*.tgz", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setText(Messages.TracePackage_FileDialogTitle);
        String currentSourceString = getFilePathValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
        }
        String selectedFileName = dialog.open();

        if (selectedFileName != null) {
            setFilePathValue(selectedFileName);
            updateWithFilePathSelection();
        }
    }

    /**
     * Get the current file path value
     *
     * @return the current file path value
     */
    protected String getFilePathValue() {
        return fFilePathCombo.getText().trim();
    }

    /**
     * Set the file path value
     *
     * @param value
     *            file path value
     */
    protected void setFilePathValue(String value) {
        fFilePathCombo.setText(value);
        updatePageCompletion();
    }

    /**
     * Update the approximate size of the selected elements
     */
    protected void updateApproximateSelectedSize() {
    }

    /**
     * Get the element tree viewer
     *
     * @return the element tree viewer
     */
    protected CheckboxTreeViewer getElementViewer() {
        return fElementViewer;
    }

    /**
     * Get the file path combo box
     *
     * @return the file path combo box
     */
    protected Combo getFilePathCombo() {
        return fFilePathCombo;
    }

    /**
     * Get the object selection when the wizard was created
     *
     * @return the object selection
     */
    protected IStructuredSelection getSelection() {
        return fSelection;
    }
}
