/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.project.dialogs;

import java.util.Vector;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentEntry;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProject;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngTraceEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * <b><u>AddTraceWizardPage</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class AddTraceWizardPage extends WizardPage {

	private LTTngProject fProject;
	@SuppressWarnings("unused")
	private LTTngExperimentEntry fExperiment;
	private CheckboxTableViewer fCheckboxTableViewer;
	
	protected AddTraceWizardPage(LTTngProject project, String pageName) {
		super(pageName);
		setTitle("Select traces");
		setDescription("Select the traces to add to the experiment");
		fProject = project;
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		setControl(container);

		fCheckboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		fCheckboxTableViewer.setContentProvider(new LTTngTraceContentProvider());
		fCheckboxTableViewer.setLabelProvider(new LTTngTraceLabelProvider());

		final Table table = fCheckboxTableViewer.getTable();
		final FormData formData = new FormData();
		formData.bottom = new FormAttachment(100, 0);
		formData.right  = new FormAttachment(100, 0);
		formData.top    = new FormAttachment(0, 0);
		formData.left   = new FormAttachment(0, 0);
		table.setLayoutData(formData);
		table.setHeaderVisible(true);

		final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(200);
		tableColumn.setText("Trace");

        fCheckboxTableViewer.setInput(fProject.getTracesFolder());
	}

	public void init(IStructuredSelection selection) {
		Object sel = selection.getFirstElement();
		if (sel instanceof LTTngExperimentEntry) {
			fExperiment = (LTTngExperimentEntry) sel;
		}
	}

	public LTTngTraceEntry[] getSelection() {
		Vector<LTTngTraceEntry> traces = new Vector<LTTngTraceEntry>();
		Object[] selection = fCheckboxTableViewer.getCheckedElements();
		for (Object sel : selection) {
			if (sel instanceof LTTngTraceEntry)
				traces.add((LTTngTraceEntry) sel);
		}
		LTTngTraceEntry[] result = new LTTngTraceEntry[traces.size()];
		traces.toArray(result); 
		return result;
	}

}
