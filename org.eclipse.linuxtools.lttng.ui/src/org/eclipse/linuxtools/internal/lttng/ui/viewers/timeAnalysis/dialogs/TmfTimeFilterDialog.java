/*****************************************************************************
 * Copyright (c) 2008 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *
 * $Id: ThreadFilterDialog.java,v 1.2 2008/03/05 17:31:07 ewchan Exp $ 
 *****************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.dialogs;

import java.util.Arrays;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class TmfTimeFilterDialog extends TitleAreaDialog {
	private CheckboxTableViewer viewer;
	private Object[] threads;
	private boolean[] filter;
	private ViewContentProvider viewContentProvider;
	private TraceComparator viewTraceComparator;
	private ViewLabelProvider viewViewLabelProvider;

	public TmfTimeFilterDialog(Shell parentShell, Object[] threads,
			boolean[] filter) {
		super(parentShell);

		this.threads = (threads != null) ? Arrays.copyOf(threads, threads.length) : null;
		if (filter != null)
			this.filter = (boolean[]) filter.clone();
		
		viewContentProvider = new ViewContentProvider();
		viewTraceComparator = new TraceComparator();
		viewViewLabelProvider = new ViewLabelProvider();
	}

	public static boolean getTraceFilter(Shell parentShell, Object[] threads,
			boolean[] filter) {
		TmfTimeFilterDialog dlg = new TmfTimeFilterDialog(parentShell, threads,
				filter);
		if (dlg.open() != Window.OK)
			return false;

		boolean f[] = dlg.getFilter();
		if (java.util.Arrays.equals(f, filter))
			return false;
		for (int i = f.length - 1; i >= 0; i--)
			filter[i] = f[i];

		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER
				| SWT.V_SCROLL);

		Table table = viewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setBackground(parent.getBackground());
		createColumns(table);

		//Assign providers to the viewer.
		viewer.setContentProvider(viewContentProvider);
		viewer.setComparator(viewTraceComparator);
		viewer.setLabelProvider(viewViewLabelProvider);
		
		viewer.setInput(new Object());

		viewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object o = event.getElement();
				for (int i = threads.length - 1; i >= 0; i--) {
					if (threads[i].equals(o))
						filter[i] = event.getChecked();
				}
			}
		});

		if (filter != null) {
			for (int i = 0; i < filter.length; i++)
				viewer.setChecked(threads[i], filter[i]);
		}

		setMessage(Messages.TmfTimeFilterDialog_TRACE_FILTER_DESC);
		setTitle(Messages.TmfTimeFilterDialog_TRACE_FILTER);
		setDialogHelpAvailable(false);
		setHelpAvailable(false);

//		setTitleImage(org.eclipse.hyades.trace.internal.ui.PDPluginImages.DESC_IMG_UI_WZ_EDITPROFSET
//				.createImage());

		return composite;
	}

	private void createColumns(Table table) {
		table.setHeaderVisible(true);

		String headers[] = { "", Messages.TmfTimeFilterDialog_TRACE_ID, Messages.TmfTimeFilterDialog_TRACE_NAME }; //$NON-NLS-1$
		int width[] = { 20, 80, 400 };

		for (int i = 0; i < headers.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(true);
			tc.setText(headers[i]);
			tc.setWidth(width[i]);
		}
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.TmfTimeFilterDialog_EDIT_PROFILING_OPTIONS);
	}

	public boolean[] getFilter() {
		return (filter != null) ? Arrays.copyOf(filter, filter.length) : null;
	}

	/**
	 * @param viewContentProvider
	 */
	public void setViewContentProvider(ViewContentProvider viewContentProvider) {
		this.viewContentProvider = viewContentProvider;
	}

	/**
	 * @param viewThreadComparator
	 */
	public void setViewThreadComparator(TraceComparator viewThreadComparator) {
		this.viewTraceComparator = viewThreadComparator;
	}

	/**
	 * @param viewViewLabelProvider
	 */
	public void setViewViewLabelProvider(ViewLabelProvider viewViewLabelProvider) {
		this.viewViewLabelProvider = viewViewLabelProvider;
	}
	
	class ViewContentProvider implements IStructuredContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object input) {
			return threads;
		}
	}

	private static class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		@Override
		public Image getImage(Object obj) {
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			// TODO: AA: Provide Label Provider externally
			ITmfTimeAnalysisEntry t = (ITmfTimeAnalysisEntry) element;

			if (columnIndex == 1)
				return String.valueOf(t.getId());
			else if (columnIndex == 2)
				return t.getName();
			else
				return ""; //$NON-NLS-1$
		}
	}

	private static class TraceComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			// TODO: AA: Provide comparator externally
			int id1 = ((ITmfTimeAnalysisEntry) e1).getId();
			int id2 = ((ITmfTimeAnalysisEntry) e2).getId();

			if (id1 == id2)
				return 0;

			return (id1 < id2) ? -1 : 1;
			// return 0;
		}
	}
}
