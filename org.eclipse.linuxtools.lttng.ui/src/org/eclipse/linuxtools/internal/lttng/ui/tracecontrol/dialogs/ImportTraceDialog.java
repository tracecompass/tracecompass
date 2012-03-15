/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.internal.lttng.core.LTTngProjectNature;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource.TraceState;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>ImportTraceDialog</u></b>
 * <p>
 * Dialog box to import a trace a LTTng project.
 * </p>
 */
public class ImportTraceDialog extends Dialog {

	// ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
	private TraceResource fTrace;
	private Table fTable;
	private Text fNameText;
	private Button fLinkOnlyButton;
	private IProject fProject;
	private String fTraceName;
	private boolean fLinkOnly = false;
	
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	/**
	 * Constructor
	 *  
	 * @param parentShell The parent shell
	 * @param trace The trace to import
	 */
	public ImportTraceDialog(Shell parentShell, TraceResource trace) {
		super(parentShell);
		fTrace = trace;
	}

	// ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.ImportTraceDialog_Title);
		getShell().setImage(Activator.getDefault().getImage(Activator.ICON_ID_IMPORT_TRACE));
		
		Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(1, false));
        
        Label tableLabel = new Label(composite, SWT.NONE);
        tableLabel.setText(Messages.ImportTraceDialog_TableLabel);
		
        fTable = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        fTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fTable.setHeaderVisible(true);
        fTable.setLinesVisible(true);

        TableColumn column = new TableColumn(fTable, SWT.CENTER);
        column.setText(Messages.ImportTraceDialog_ProjectColumn);

        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
        	try {
				if (project.isOpen() && project.hasNature(LTTngProjectNature.ID)) {
		            TableItem item = new TableItem(fTable, SWT.LEFT);
		            item.setText(0, project.getName());
		            item.setData(project);
				}
			} catch (CoreException e) {
			    SystemBasePlugin.logError("ImportTraceDialog", e); //$NON-NLS-1$
			}
        }
        
        for (int i = 0; i < fTable.getColumnCount(); i++) {
        	fTable.getColumn(i).pack();
        }

        Composite nameComposite = new Composite(composite, SWT.NONE);
        nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        nameComposite.setLayout(gl);
        
        Label nameLabel = new Label(nameComposite, SWT.NONE);
        nameLabel.setText(Messages.ImportTraceDialog_NameLabel);

        fNameText = new Text(nameComposite, SWT.BORDER);
        fNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fNameText.setText(fTrace.getName());

        if (fTrace.getTraceConfig().isNetworkTrace()) {
        	fLinkOnlyButton = new Button(composite, SWT.CHECK);
        	fLinkOnlyButton.setText(Messages.ImportTraceDialog_LinkOnly);
        	fLinkOnlyButton.setSelection(true);
        	fLinkOnly = true;
        	if (fTrace.getTraceState() != TraceState.STOPPED) {
        		// if the trace is not stopped, link is the only allowed option
        		fLinkOnlyButton.setEnabled(false);
        	}
        }
        
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setText(Messages.ImportTraceDialog_ImportButton);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
	protected void okPressed() {
    	TableItem[] selection = fTable.getSelection();
    	if (selection.length > 0) {
    		fProject = (IProject) selection[0].getData();
    	}
    	fTraceName = fNameText.getText();
    	if (fLinkOnlyButton != null) {
    		fLinkOnly = fLinkOnlyButton.getSelection();
    	}
    	super.okPressed();
	}

    /**
     * Returns the project to import to.
     * 
     * @return project to import to.
     */
	public IProject getProject() {
    	return fProject;
    }
    
	/**
	 * Gets the name of the trace in the LTTng project. 
	 * 
	 * @return
	 */
    public String getTraceName() {
    	return fTraceName;
    }
    
    /**
     * Returns if trace should be linked or copied to the project.
     * Only applicable for traces residing on local host.
     * 
     * @return true if trace should be linked to the project, 
     *         false if it should be copied 
     */
    public boolean getLinkOnly() {
    	return fLinkOnly;
    }
}
