/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *      Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *          activated and used by other components.
 *      Lubomir Marinov <lubomir.marinov@gmail.com> - Fix for bug 182122 -[Dialogs]
 *          CheckedTreeSelectionDialog#createSelectionButtons(Composite) fails to
 *          align the selection buttons to the right
 *      François Rajotte - Support for multiple columns + selection control
 *      Patrick Tasse - Fix Sonar warnings
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * Filter dialog for the time graphs
 * This class is derived from the CheckedTreeSelectionDialog
 * It was necessary to develop this similar dialog to allow multiple columns
 *
 * @version 1.0
 * @since 2.0
 * @author François Rajotte
 */
public class TimeGraphFilterDialog extends SelectionStatusDialog {
    private static final int BUTTON_CHECK_SELECTED_ID = IDialogConstants.CLIENT_ID;
    private static final int BUTTON_UNCHECK_SELECTED_ID = IDialogConstants.CLIENT_ID + 1;
    private static final int BUTTON_CHECK_SUBTREE_ID = IDialogConstants.CLIENT_ID + 2;
    private static final int BUTTON_UNCHECK_SUBTREE_ID = IDialogConstants.CLIENT_ID + 3;

    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 18;

    private CheckboxTreeViewer fViewer;

    private IBaseLabelProvider fLabelProvider;

    private ITreeContentProvider fContentProvider;

    private String[] fColumnNames;

    private ISelectionStatusValidator fValidator = null;

    private ViewerComparator fComparator;

    private String fEmptyListMessage = ""; //$NON-NLS-1$

    private IStatus fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID,
            0, "", null); //$NON-NLS-1$

    private List<ViewerFilter> fFilters;

    private Object fInput;

    private boolean fIsEmpty;

    private int fWidth = DEFAULT_WIDTH;

    private int fHeight = DEFAULT_HEIGHT;

    private Object[] fExpandedElements;

    /**
     * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
     *
     * @param parent
     *            The shell to parent from.
     */
    public TimeGraphFilterDialog(Shell parent) {
        super(parent);
        setResult(new ArrayList<Object>(0));
        setStatusLineAboveButtons(true);
        setHelpAvailable(false);
        fExpandedElements = null;
    }

    /**
     * Sets the initial selection. Convenience method.
     *
     * @param selection
     *            the initial selection.
     */
    public void setInitialSelection(Object selection) {
        setInitialSelections(new Object[] { selection });
    }

    /**
     * Sets the message to be displayed if the list is empty.
     *
     * @param message
     *            the message to be displayed.
     */
    public void setEmptyListMessage(String message) {
        fEmptyListMessage = message;
    }

    /**
     * Sets the comparator used by the tree viewer.
     *
     * @param comparator
     *            The comparator
     */
    public void setComparator(ViewerComparator comparator) {
        fComparator = comparator;
    }

    /**
     * Adds a filter to the tree viewer.
     *
     * @param filter
     *            a filter.
     */
    public void addFilter(ViewerFilter filter) {
        if (fFilters == null) {
            fFilters = new ArrayList<ViewerFilter>();
        }
        fFilters.add(filter);
    }

    /**
     * Sets an optional validator to check if the selection is valid. The
     * validator is invoked whenever the selection changes.
     *
     * @param validator
     *            the validator to validate the selection.
     */
    public void setValidator(ISelectionStatusValidator validator) {
        fValidator = validator;
    }

    /**
     * Sets the tree input.
     *
     * @param input
     *            the tree input.
     */
    public void setInput(Object input) {
        fInput = input;
    }

    /**
     * Expands elements in the tree.
     *
     * @param elements
     *            The elements that will be expanded.
     */
    public void setExpandedElements(Object[] elements) {
        if (elements != null) {
            fExpandedElements = Arrays.copyOf(elements, elements.length);
        } else {
            fExpandedElements = null;
        }
    }

    /**
     * Sets the size of the tree in unit of characters.
     *
     * @param width
     *            the width of the tree.
     * @param height
     *            the height of the tree.
     */
    public void setSize(int width, int height) {
        fWidth = width;
        fHeight = height;
    }

    /**
     * @param contentProvider The content provider for the table
     */
    public void setContentProvider(ITreeContentProvider contentProvider) {
        fContentProvider = contentProvider;
    }

    /**
     * @param labelProvider The label provider for the table
     */
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        fLabelProvider = labelProvider;
    }

    /**
     * @param columnNames An array of column names to display
     */
    public void setColumnNames(String[] columnNames) {
        if (columnNames != null) {
            fColumnNames = Arrays.copyOf(columnNames, columnNames.length);
        } else {
            fColumnNames = null;
        }
    }

    /**
     * Validate the receiver and update the status with the result.
     *
     */
    protected void updateOKStatus() {
        if (!fIsEmpty) {
            if (fValidator != null) {
                fCurrStatus = fValidator.validate(fViewer.getCheckedElements());
                updateStatus(fCurrStatus);
            } else if (!fCurrStatus.isOK()) {
                fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID,
                        IStatus.OK, "", //$NON-NLS-1$
                        null);
            }
        } else {
            fCurrStatus = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID,
                    IStatus.OK, fEmptyListMessage, null);
        }
        updateStatus(fCurrStatus);
    }

    @Override
    public int open() {
        fIsEmpty = evaluateIfTreeEmpty(fInput);
        super.open();
        return getReturnCode();
    }

    @Override
    protected void cancelPressed() {
        setResult(null);
        super.cancelPressed();
    }

    @Override
    protected void computeResult() {
        setResult(Arrays.asList(fViewer.getCheckedElements()));
    }

    @Override
    public void create() {
        BusyIndicator.showWhile(null, new Runnable() {
            @Override
            public void run() {
                TimeGraphFilterDialog.super.create();
                fViewer.setCheckedElements(getInitialElementSelections()
                        .toArray());
                if (fExpandedElements != null) {
                    fViewer.setExpandedElements(fExpandedElements);
                }
                updateOKStatus();
            }
        });
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        Label messageLabel = createMessageArea(composite);
        CheckboxTreeViewer treeViewer = createTreeViewer(composite);
        Control buttonComposite = createSelectionButtons(composite);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(fWidth);
        data.heightHint = convertHeightInCharsToPixels(fHeight);
        Tree treeWidget = treeViewer.getTree();
        treeWidget.setLayoutData(data);
        treeWidget.setFont(parent.getFont());
        if (fIsEmpty) {
            messageLabel.setEnabled(false);
            treeWidget.setEnabled(false);
            buttonComposite.setEnabled(false);
        }
        return composite;
    }

    /**
     * Creates the tree viewer.
     *
     * @param parent
     *            the parent composite
     * @return the tree viewer
     */
    protected CheckboxTreeViewer createTreeViewer(Composite parent) {
        fViewer = new CheckboxTreeViewer(parent, SWT.BORDER | SWT.MULTI);

        Tree tree = fViewer.getTree();
        tree.setHeaderVisible(true);
        for (String columnName : fColumnNames) {
            TreeColumn column = new TreeColumn(tree, SWT.LEFT);
            column.setText(columnName);
            column.pack();
        }

        fViewer.setContentProvider(fContentProvider);
        fViewer.setLabelProvider(fLabelProvider);
        fViewer.addCheckStateListener(new CheckStateListener());
        fViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                updateOKStatus();
            }
        });
        fViewer.setComparator(fComparator);
        if (fFilters != null) {
            for (int i = 0; i != fFilters.size(); i++) {
                fViewer.addFilter(fFilters.get(i));
            }
        }
        fViewer.setInput(fInput);

        //pack the columns again for a nice view...
        for (TreeColumn column : tree.getColumns()) {
            column.pack();
        }
        return fViewer;
    }

    /**
     * Returns the tree viewer.
     *
     * @return the tree viewer
     */
    protected CheckboxTreeViewer getTreeViewer() {
        return fViewer;
    }

    /**
     * Adds the selection and deselection buttons to the dialog.
     *
     * @param composite
     *            the parent composite
     * @return Composite the composite the buttons were created in.
     */
    protected Composite createSelectionButtons(Composite composite) {
        Composite buttonComposite = new Composite(composite, SWT.RIGHT);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        buttonComposite.setLayout(layout);
        buttonComposite.setFont(composite.getFont());
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
                | GridData.GRAB_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        buttonComposite.setLayoutData(data);

        /* Create the buttons in the good order to place them as we want */
        Button checkSelectedButton = createButton(buttonComposite,
                BUTTON_CHECK_SELECTED_ID, Messages.TmfTimeFilterDialog_CHECK_SELECTED,
                false);
        Button checkSubtreeButton = createButton(buttonComposite,
                BUTTON_CHECK_SUBTREE_ID, Messages.TmfTimeFilterDialog_CHECK_SUBTREE,
                false);
        Button checkAllButton = createButton(buttonComposite,
                IDialogConstants.SELECT_ALL_ID, Messages.TmfTimeFilterDialog_CHECK_ALL,
                false);

        Button uncheckSelectedButton = createButton(buttonComposite,
                BUTTON_UNCHECK_SELECTED_ID, Messages.TmfTimeFilterDialog_UNCHECK_SELECTED,
                false);
        Button uncheckSubtreeButton = createButton(buttonComposite,
                BUTTON_UNCHECK_SUBTREE_ID, Messages.TmfTimeFilterDialog_UNCHECK_SUBTREE,
                false);
        Button uncheckAllButton = createButton(buttonComposite,
                IDialogConstants.DESELECT_ALL_ID, Messages.TmfTimeFilterDialog_UNCHECK_ALL,
                false);


        /*
         * Apply the layout again after creating the buttons to override
         * createButton messing with the columns
         */
        layout.numColumns = 3;
        buttonComposite.setLayout(layout);

        /* Add a listener to each button */
        checkSelectedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeSelection selection = (TreeSelection) fViewer.getSelection();

                for (Object element : selection.toArray()) {
                    checkElement(element);
                }

                updateOKStatus();
            }
        });

        checkSubtreeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeSelection selection = (TreeSelection) fViewer.getSelection();

                for (Object element : selection.toArray()) {
                    checkElementAndSubtree(element);
                }
            }
        });

        checkAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Object[] viewerElements = fContentProvider.getElements(fInput);

                for (int i = 0; i < viewerElements.length; i++) {
                    fViewer.setSubtreeChecked(viewerElements[i], true);
                }

                updateOKStatus();
            }
        });

        uncheckSelectedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeSelection selection = (TreeSelection) fViewer.getSelection();

                for (Object element : selection.toArray()) {
                    uncheckElement(element);
                }

                updateOKStatus();
            }
        });

        uncheckSubtreeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeSelection selection = (TreeSelection) fViewer.getSelection();

                for (Object element : selection.toArray()) {
                    uncheckElement(element);
                }

                updateOKStatus();
            }
        });

        uncheckAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fViewer.setCheckedElements(new Object[0]);
                updateOKStatus();
            }
        });

        return buttonComposite;
    }

    /**
     * Check an element and all its parents.
     *
     * @param element
     *            The element to check.
     */
    private void checkElement(Object element) {
        fViewer.setChecked(element, true);

        Object parent = fContentProvider.getParent(element);

        if (parent != null) {
            checkElement(parent);
        }
    }

    /**
     * Check an element, all its parents and all its children.
     *
     * @param element
     *            The element to check.
     */
    private void checkElementAndSubtree(Object element) {
        checkElement(element);

        for (Object child : fContentProvider.getChildren(element)) {
            checkElementAndSubtree(child);
        }
    }

    /**
     * Uncheck an element and all its children.
     *
     * @param element
     *            The element to uncheck.
     */
    private void uncheckElement(Object element) {
        fViewer.setChecked(element, false);

        for (Object child : fContentProvider.getChildren(element)) {
            uncheckElement(child);
        }
    }

    private boolean evaluateIfTreeEmpty(Object input) {
        Object[] elements = fContentProvider.getElements(input);
        if (elements.length > 0 && fFilters != null) {
            for (int i = 0; i < fFilters.size(); i++) {
                ViewerFilter curr = fFilters.get(i);
                elements = curr.filter(fViewer, input, elements);
            }
        }
        return elements.length == 0;
    }

    /**
     * Private classes
     */

    private class CheckStateListener implements ICheckStateListener {

        CheckStateListener() {
        }

        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            try {
                ITimeGraphEntry entry = (ITimeGraphEntry) event.getElement();
                boolean checked = event.getChecked();
                if (checked) {
                    checkElement(entry);
                } else {
                    uncheckElement(entry);
                }
            } catch (ClassCastException e) {
                return;
            }
        }
    }
}
