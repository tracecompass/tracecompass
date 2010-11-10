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

package org.eclipse.linuxtools.tmf.ui.views.project.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfExperimentFolderNode;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.dialogs.CreateLinkedResourceGroup;

/**
 * NewExperimentDialog
 * 
 * This is stripped down version of NewFolderDialog.
 */
@SuppressWarnings("restriction")
public class NewExperimentDialog extends SelectionStatusDialog {

	private Text folderNameField;
	private IContainer container;
	private boolean firstLinkCheck = true;
	private CreateLinkedResourceGroup linkedResourceGroup;

	/**
	 * Creates a NewFolderDialog
	 * 
	 * @param parentShell parent of the new dialog
	 * @param container parent of the new folder
	 */
	public NewExperimentDialog(Shell parentShell, TmfExperimentFolderNode experimentFolder) {
		super(parentShell);
		this.container = experimentFolder.getFolder();
		setTitle("Tmf Experiment");
		setStatusLineAboveButtons(true);
	}

	/**
	 * Creates the folder using the name and link target entered by the user.
	 * Sets the dialog result to the created folder.  
	 */
	@Override
	protected void computeResult() {
	}

	/**
	 * @see org.eclipse.jface.window.Window#create()
	 */
	@Override
	public void create() {
		super.create();
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	/**
	 * Creates the widget for advanced options.
	 *  
	 * @param parent the parent composite
	 */
	protected void createLinkResourceGroup(Composite parent) {
		linkedResourceGroup = new CreateLinkedResourceGroup(IResource.FOLDER,
				new Listener() {
					@Override
					public void handleEvent(Event e) {
						validateLinkedResource();
						firstLinkCheck = false;
					}
				}, new CreateLinkedResourceGroup.IStringValue() {
					@Override
					public void setValue(String string) {
						folderNameField.setText(string);
					}

					@Override
					public String getValue() {
						return folderNameField.getText();
					}

					@Override
					public IResource getResource() {
						// TODO Auto-generated method stub
						return null;
					}
				});
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createFolderNameGroup(composite);
		createLinkResourceGroup(composite);
		return composite;
	}

	/**
	 * Creates the folder name specification controls.
	 *
	 * @param parent the parent composite
	 */
	private void createFolderNameGroup(Composite parent) {
		Font font = parent.getFont();
		Composite folderGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		folderGroup.setLayout(layout);
		folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new folder label
		Label folderLabel = new Label(folderGroup, SWT.NONE);
		folderLabel.setFont(font);
		folderLabel.setText("Experiment name: ");

		// new folder name entry field
		folderNameField = new Text(folderGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		folderNameField.setLayoutData(data);
		folderNameField.setFont(font);
		folderNameField.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				validateLinkedResource();
			}
		});
	}

	/**
	 * Creates a folder resource handle for the folder with the given name.
	 * The folder handle is created relative to the container specified during 
	 * object creation. 
	 *
	 * @param folderName the name of the folder resource to create a handle for
	 * @return the new folder resource handle
	 */
	private IFolder createFolderHandle(String folderName) {
		IWorkspaceRoot workspaceRoot = container.getWorkspace().getRoot();
		IPath folderPath = container.getFullPath().append(folderName);
		IFolder folderHandle = workspaceRoot.getFolder(folderPath);

		return folderHandle;
	}

	/**
	 * Creates a new folder with the given name and optionally linking to
	 * the specified link target.
	 * 
	 * @param folderName name of the new folder
	 * @param linkTarget name of the link target folder. may be null.
	 * @return IFolder the new folder
	 */
	private IFolder createNewFolder(String folderName, final URI linkTarget) {
		final IFolder folderHandle = createFolderHandle(folderName);

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
			@Override
			public void execute(IProgressMonitor monitor) throws CoreException {
				try {
					monitor.beginTask(IDEWorkbenchMessages.NewFolderDialog_progress, 2000);
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					if (linkTarget == null) {
						folderHandle.create(false, true, monitor);
					} else {
						folderHandle.createLink(linkTarget, IResource.ALLOW_MISSING_LOCAL, monitor);
					}
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				} finally {
					monitor.done();
				}
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
		} catch (InterruptedException exception) {
			return null;
		} catch (InvocationTargetException exception) {
			if (exception.getTargetException() instanceof CoreException) {
				ErrorDialog.openError(getShell(),
						IDEWorkbenchMessages.NewFolderDialog_errorTitle, null, // no special message
						((CoreException) exception.getTargetException()).getStatus());
			} else {
				// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
				IDEWorkbenchPlugin.log(getClass(),
						"createNewExperiment", exception.getTargetException());
				MessageDialog.openError(getShell(),
						IDEWorkbenchMessages.NewFolderDialog_errorTitle,
						NLS.bind(IDEWorkbenchMessages.NewFolderDialog_internalError,
							exception.getTargetException().getMessage()));
			}
			return null;
		}
		return folderHandle;
	}

	/**
	 * Update the dialog's status line to reflect the given status. It is safe to call
	 * this method before the dialog has been opened.
	 */
	@Override
	protected void updateStatus(IStatus status) {
		if (firstLinkCheck && status != null) {
			Status newStatus = new Status(IStatus.OK, status.getPlugin(),
					status.getCode(), status.getMessage(), status.getException());
			super.updateStatus(newStatus);
		} else {
			super.updateStatus(status);
		}
	}

	/**
	 * Update the dialog's status line to reflect the given status. It is safe to call
	 * this method before the dialog has been opened.
	 * @param severity
	 * @param message
	 */
	private void updateStatus(int severity, String message) {
		updateStatus(new Status(severity, IDEWorkbenchPlugin.IDE_WORKBENCH, severity, message, null));
	}

	/**
	 * Checks whether the folder name and link location are valid.
	 * Disable the OK button if the folder name and link location are valid.
	 * a message that indicates the problem otherwise.
	 */
	private void validateLinkedResource() {
		boolean valid = validateFolderName();

		if (valid) {
			IFolder linkHandle = createFolderHandle(folderNameField.getText());
			IStatus status = linkedResourceGroup.validateLinkLocation(linkHandle);

			if (status.getSeverity() != IStatus.ERROR) {
				getOkButton().setEnabled(true);
			} else {
				getOkButton().setEnabled(false);
			}

			if (status.isOK() == false) {
				updateStatus(status);
			}
		} else {
			getOkButton().setEnabled(false);
		}
	}

	/**
	 * Checks if the folder name is valid.
	 *
	 * @return null if the new folder name is valid.
	 * 	a message that indicates the problem otherwise.
	 */
	private boolean validateFolderName() {
		String name = folderNameField.getText();
		IWorkspace workspace = container.getWorkspace();
		IStatus nameStatus = workspace.validateName(name, IResource.FOLDER);

		if ("".equals(name)) { //$NON-NLS-1$
			updateStatus(IStatus.ERROR, "Experiment name is empty"); //$NON-NLS-1$
			return false;
		}
		if (nameStatus.isOK() == false) {
			updateStatus(nameStatus);
			return false;
		}
		IPath path = new Path(name);
		if (container.getFolder(path).exists()
				|| container.getFile(path).exists()) {
			updateStatus(IStatus.ERROR, NLS.bind("Experiment already exists", name)); //$NON-NLS-1$
			return false;
		}
		updateStatus(IStatus.OK, ""); //$NON-NLS-1$
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		URI linkTarget = linkedResourceGroup.getLinkTargetURI();
		IFolder folder = createNewFolder(folderNameField.getText(), linkTarget);
		if (folder == null) {
			return;
		}

		setSelectionResult(new IFolder[] { folder });

		super.okPressed();
	}
}