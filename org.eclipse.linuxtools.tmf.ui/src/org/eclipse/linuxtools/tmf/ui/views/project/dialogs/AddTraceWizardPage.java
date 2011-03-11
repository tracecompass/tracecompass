/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.project.dialogs;

import java.util.Vector;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.tmf.ui.internal.Messages;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfTraceNode;
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

	private TmfProjectNode fProject;
	private CheckboxTableViewer fCheckboxTableViewer;
	
	protected AddTraceWizardPage(TmfProjectNode project, String pageName) {
		super(pageName);
		setTitle(Messages.AddTraceWizardPage_WindowTitle);
		setDescription(Messages.AddTraceWizardPage_Description);
		fProject = project;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FormLayout());
		setControl(container);

		fCheckboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		fCheckboxTableViewer.setContentProvider(new DialogTraceContentProvider());
		fCheckboxTableViewer.setLabelProvider(new DialogTraceLabelProvider());

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
		tableColumn.setText(Messages.AddTraceWizardPage_TraceColumnHeader);

        fCheckboxTableViewer.setInput(fProject.getTracesFolder());
	}

	public TmfTraceNode[] getSelection() {
		Vector<TmfTraceNode> traces = new Vector<TmfTraceNode>();
		Object[] selection = fCheckboxTableViewer.getCheckedElements();
		for (Object sel : selection) {
			if (sel instanceof TmfTraceNode)
				traces.add((TmfTraceNode) sel);
		}
		TmfTraceNode[] result = new TmfTraceNode[traces.size()];
		traces.toArray(result); 
		return result;
	}

}
