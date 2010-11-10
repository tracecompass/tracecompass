package org.eclipse.linuxtools.lttng.ui.views.project.dialogs;

/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque - Initial API and implementation
 *******************************************************************************/

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

public class TraceErrorDialog extends SelectionStatusDialog {
	
	private String[] messages = null; 
	
	public TraceErrorDialog(Shell parentShell, String[] newMessages) {
		super(parentShell);
		
		messages = newMessages;
		setTitle("Trace Selection");
		setStatusLineAboveButtons(true);
	}
	
	@Override
	protected void computeResult() {
	}
	
	@Override
	public void create() {
		super.create();
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		getButton(IDialogConstants.OK_ID).setAlignment(GridData.CENTER);
		getButton(IDialogConstants.CANCEL_ID).setVisible(false);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createFolderNameGroup(composite);
		return composite;
	}
	
	private void createFolderNameGroup(Composite parent) {
		Font font = parent.getFont();
		Composite folderGroup = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 3;
		folderGroup.setLayout(layout);
		folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		// Position grid for the messages
		GridData positionningData = new GridData(GridData.FILL_VERTICAL);
		
		// New message label
		Label lblMessage = new Label(folderGroup, SWT.NONE);
		lblMessage.setFont(font);
		lblMessage.setText("Error while opening the trace! Error message was : ");
		lblMessage.setLayoutData(positionningData);
		
		// The actual messages
		GC graphicContext = new GC(parent);
		String longuestLine = ""; //$NON-NLS-1$
		int msgSize = 0;
		
		// *** Font and FontData are nowhere near elegant to use.
		Font errorFont = new Font(graphicContext.getDevice(), font.getFontData()[0].getName(), font.getFontData()[0].getHeight(), SWT.ITALIC);
		
		Label[] lblErrorMessages = new Label[messages.length];
		for(int x=0; x<messages.length; x++) {
			lblErrorMessages[x] = new Label(folderGroup, SWT.NONE);
			lblErrorMessages[x].setFont(errorFont);
			
			lblErrorMessages[x].setForeground(new Color(lblErrorMessages[x].getForeground().getDevice(), 255, 0, 0));
			lblErrorMessages[x].setText(messages[x]);
			lblErrorMessages[x].setLayoutData(positionningData);
			
			if ( messages[x].length() > longuestLine.length() ) {
				longuestLine = messages[x];
			}
		}
		
		for ( int pos=0; pos<longuestLine.length(); pos++ ) {
			msgSize += graphicContext.getAdvanceWidth( longuestLine.charAt(pos) );
		}
		// Seems we need to count the final \n of the line otherwise we miss some space
		msgSize += graphicContext.getAdvanceWidth( '\n' );
		
		positionningData.widthHint = msgSize;
		positionningData.grabExcessHorizontalSpace = true;
		
		graphicContext.dispose();
		
	}
	
	
	@Override
	protected void updateStatus(IStatus status) {
		if (status != null) {
			Status newStatus = new Status(IStatus.OK, status.getPlugin(),
					status.getCode(), status.getMessage(), status.getException());
			super.updateStatus(newStatus);
		} else {
			super.updateStatus(status);
		}
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
	}
}
