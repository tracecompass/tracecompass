/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorLabelProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A wizard page for selecting the trace to export when no trace was previously
 * selected.
 *
 * @author Marc-Andre Laperle
 */
public class ExportTracePackageSelectTraceWizardPage extends WizardPage {

    private static final String PAGE_NAME = "ExportTracePackageSelectTraceWizardPage"; //$NON-NLS-1$

    /**
     * Construct the select trace page
     */
    public ExportTracePackageSelectTraceWizardPage() {
        super(PAGE_NAME);
    }

    private IProject fSelectedProject;
    private Table fTraceTable;

    @Override
    public void createControl(Composite parent) {
        Composite projectSelectionGroup = new Composite(parent, SWT.NONE);
        projectSelectionGroup.setLayout(new GridLayout(2, true));
        projectSelectionGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        projectSelectionGroup.setFont(parent.getFont());

        Label projectLabel = new Label(projectSelectionGroup, SWT.NONE);
        projectLabel.setText(Messages.ExportTracePackageSelectTraceWizardPage_ProjectSelection);
        projectLabel.setLayoutData(new GridData());

        Label configLabel = new Label(projectSelectionGroup, SWT.NONE);
        configLabel.setText(Messages.ExportTracePackageSelectTraceWizardPage_TraceSelection);
        configLabel.setLayoutData(new GridData());

        final Table projectTable = new Table(projectSelectionGroup, SWT.SINGLE | SWT.BORDER);
        projectTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableViewer projectViewer = new TableViewer(projectTable);
        projectViewer.setContentProvider(new TmfNavigatorContentProvider() {

            @Override
            public Object[] getElements(Object inputElement) {
                return (IProject[]) inputElement;
            }
        });
        projectViewer.setLabelProvider(new WorkbenchLabelProvider());
        projectViewer.setInput(TraceUtils.getOpenedTmfProjects().toArray(new IProject[] {}));

        fTraceTable = new Table(projectSelectionGroup, SWT.BORDER | SWT.CHECK);
        fTraceTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        final TableViewer traceViewer = new TableViewer(fTraceTable);
        traceViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof TmfTraceElement[]) {
                    return (TmfTraceElement[]) inputElement;
                }
                return null;
            }
        });
        traceViewer.setLabelProvider(new ExportLabelProvider());
        fTraceTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getWizard().getContainer().updateButtons();
                updateNextPageData();
            }
        });

        projectTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = projectTable.getSelection();
                fSelectedProject = (IProject) items[0].getData();

                TmfProjectElement project = TmfProjectRegistry.getProject(fSelectedProject, true);

                TmfTraceFolder tracesFolder = project.getTracesFolder();
                List<TmfTraceElement> traces = tracesFolder.getTraces();
                TmfTraceElement[] array = traces.toArray(new TmfTraceElement[] {});
                traceViewer.setInput(array);
                traceViewer.refresh();
                fTraceTable.select(0);
                fTraceTable.notifyListeners(SWT.Selection, new Event());
                getWizard().getContainer().updateButtons();
            }
        });

        Composite btComp = new Composite(projectSelectionGroup, SWT.NONE);
        btComp.setLayout(new GridLayout(2, true));
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = SWT.RIGHT;
        btComp.setLayoutData(gd);

        final Button selectAll = new Button(btComp, SWT.PUSH);
        selectAll.setText(org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.Messages.Dialog_SelectAll);
        selectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = fTraceTable.getItems();
                for (TableItem item : items) {
                    item.setChecked(true);
                }

                getWizard().getContainer().updateButtons();
                updateNextPageData();
            }
        });

        final Button deselectAll = new Button(btComp, SWT.PUSH);
        deselectAll.setText(org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.Messages.Dialog_DeselectAll);
        deselectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = fTraceTable.getItems();
                for (TableItem item : items) {
                    item.setChecked(false);
                }

                getWizard().getContainer().updateButtons();
                updateNextPageData();
            }
        });

        setControl(projectSelectionGroup);
        setTitle(Messages.ExportTracePackageWizardPage_Title);
        setMessage(Messages.ExportTracePackageSelectTraceWizardPage_ChooseTrace);
    }

    private ArrayList<TmfTraceElement> getCheckedTraces() {
        TableItem[] items = fTraceTable.getItems();
        ArrayList<TmfTraceElement> traces = new ArrayList<>();
        for (TableItem item : items) {
            if (item.getChecked()) {
                TmfTraceElement trace = (TmfTraceElement) item.getData();
                traces.add(trace);
            }
        }
        return traces;
    }

    private void updateNextPageData() {
        ExportTracePackageWizardPage page = (ExportTracePackageWizardPage) getWizard().getPage(ExportTracePackageWizardPage.PAGE_NAME);
        page.setSelectedTraces(getCheckedTraces());
    }

    @Override
    public boolean canFlipToNextPage() {
        return getCheckedTraces().size() > 0;
    }

    private class ExportLabelProvider extends TmfNavigatorLabelProvider {
        @Override
        public String getText(Object element) {

            if (element instanceof TmfTraceElement) {
                TmfTraceElement folder = (TmfTraceElement) element;
                return folder.getElementPath();
            }
            return super.getText(element);
        }
    }


}
