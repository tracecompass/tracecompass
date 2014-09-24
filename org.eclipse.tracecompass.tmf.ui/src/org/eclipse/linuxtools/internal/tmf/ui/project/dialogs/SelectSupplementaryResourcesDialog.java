/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francois Chouinard - Copied and adapted from NewFolderDialog
 *     Marc-Andre Laperle - Add select/deselect all
 *     Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Multimap;

/**
 * SelectSupplementaryResourcesDialog
 */
public class SelectSupplementaryResourcesDialog extends Dialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final Image EXPERIMENT_IMAGE = Activator.getDefault().getImageFromPath("icons/elcl16/experiment.gif"); //$NON-NLS-1$
    private static final Image TRACE_IMAGE = Activator.getDefault().getImageFromPath("icons/elcl16/trace.gif"); //$NON-NLS-1$
    private static final Image RESOURCE_IMAGE = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------
    private CheckboxTreeViewer fTreeViewer;
    private final Multimap<TmfCommonProjectElement, IResource> fResourceMap;
    private IResource[] fReturnedResources;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param shell
     *            Parent shell of this dialog
     * @param resourceMap
     *            Map of element to supplementary resources
     */
    public SelectSupplementaryResourcesDialog(Shell shell, Multimap<TmfCommonProjectElement, IResource> resourceMap) {
        super(shell);
        fResourceMap = resourceMap;
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Getters/Setters
    // ------------------------------------------------------------------------

    /**
     * @return A copy of the selected resources
     */
    public IResource[] getResources() {
        return Arrays.copyOf(fReturnedResources, fReturnedResources.length);
    }

    // ------------------------------------------------------------------------
    // Dialog
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.SelectSpplementaryResources_DialogTitle);
        newShell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_DELETE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group contextGroup = new Group(composite, SWT.SHADOW_NONE);
        contextGroup.setText(Messages.SelectSpplementaryResources_ResourcesGroupTitle);
        contextGroup.setLayout(new GridLayout(2, false));
        contextGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        fTreeViewer = new CheckboxTreeViewer(contextGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData data = new GridData(GridData.FILL_BOTH);
        Tree tree = fTreeViewer.getTree();
        tree.setLayoutData(data);
        fTreeViewer.setContentProvider(new ITreeContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public boolean hasChildren(Object element) {
                return element instanceof TmfCommonProjectElement;
            }

            @Override
            public Object getParent(Object element) {
                if (element instanceof IResource) {
                    getParentElement((IResource) element);
                }
                return null;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof Object[]) {
                    return (Object[]) inputElement;
                }
                return null;
            }

            @Override
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof TmfCommonProjectElement) {
                    return fResourceMap.get((TmfCommonProjectElement) parentElement).toArray();
                }
                return null;
            }
        });

        fTreeViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof IResource) {
                    IResource resource = (IResource) element;
                    TmfCommonProjectElement projectElement = getParentElement(resource);
                    // remove .tracing/<supplementary folder> segments
                    IPath suppFolderPath = projectElement.getTraceSupplementaryFolder(projectElement.getElementPath()).getFullPath();
                    return resource.getFullPath().removeFirstSegments(suppFolderPath.segmentCount()).toString();
                } else if (element instanceof TmfCommonProjectElement) {
                    TmfCommonProjectElement projectElement = (TmfCommonProjectElement) element;
                    return projectElement.getElementPath();
                }
                return super.getText(element);
            }

            @Override
            public Image getImage(Object element) {
                if (element instanceof IResource) {
                    return RESOURCE_IMAGE;
                } else if (element instanceof TmfTraceElement) {
                    return TRACE_IMAGE;
                } else if (element instanceof TmfExperimentElement) {
                    return EXPERIMENT_IMAGE;
                }
                return null;
            }

        });

        fTreeViewer.setInput(fResourceMap.keySet().toArray());

        fTreeViewer.expandAll();
        setAllChecked(true);

        fTreeViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getElement() instanceof TmfCommonProjectElement) {
                    fTreeViewer.setSubtreeChecked(event.getElement(), event.getChecked());
                    fTreeViewer.setGrayed(event.getElement(), false);
                } else if (event.getElement() instanceof IResource) {
                    TmfCommonProjectElement projectElement  = getParentElement((IResource) event.getElement());
                    int checkedCount = 0;
                    Collection<IResource> resources = fResourceMap.get(projectElement);
                    for (IResource resource : resources) {
                        if (fTreeViewer.getChecked(resource)) {
                            checkedCount++;
                        }
                    }
                    if (checkedCount == resources.size()) {
                        fTreeViewer.setChecked(projectElement, true);
                        fTreeViewer.setGrayed(projectElement, false);
                    } else if (checkedCount > 0) {
                        fTreeViewer.setGrayChecked(projectElement, true);
                    } else {
                        fTreeViewer.setGrayChecked(projectElement, false);
                    }
                }
            }
        });

        fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateOKButtonEnablement();
            }
        });

        Composite btComp = new Composite(contextGroup, SWT.NONE);
        FillLayout layout = new FillLayout(SWT.VERTICAL);
        layout.spacing = 4;
        btComp.setLayout(layout);

        GridData gd = new GridData();
        gd.verticalAlignment = SWT.CENTER;
        btComp.setLayoutData(gd);

        final Button selectAll = new Button(btComp, SWT.PUSH);
        selectAll.setText(Messages.Dialog_SelectAll);
        selectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setAllChecked(true);

                updateOKButtonEnablement();
            }
        });

        final Button deselectAll = new Button(btComp, SWT.PUSH);
        deselectAll.setText(Messages.Dialog_DeselectAll);
        deselectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setAllChecked(false);

                updateOKButtonEnablement();
            }
        });

        getShell().setMinimumSize(new Point(300, 150));

        return composite;
    }

    private TmfCommonProjectElement getParentElement(IResource resource) {
        for (Entry<TmfCommonProjectElement, IResource> entry : fResourceMap.entries()) {
            if (entry.getValue().equals(resource)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void setAllChecked(boolean state) {
        for (Object element : fResourceMap.keySet()) {
            fTreeViewer.setSubtreeChecked(element, state);
            fTreeViewer.setGrayed(element, false);
        }
    }

    private void updateOKButtonEnablement() {
        Object[] checked = fTreeViewer.getCheckedElements();
        getButton(IDialogConstants.OK_ID).setEnabled(checked.length > 0);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control control = super.createButtonBar(parent);
        updateOKButtonEnablement();
        return control;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected void okPressed() {
        Object[] checkedElements = fTreeViewer.getCheckedElements();
        List<IResource> checkedResources = new ArrayList<>(checkedElements.length);
        for (Object checked : checkedElements) {
            if (checked instanceof IResource) {
                checkedResources.add((IResource) checked);
            }
        }
        fReturnedResources = checkedResources.toArray(new IResource[0]);
        super.okPressed();
    }

}
