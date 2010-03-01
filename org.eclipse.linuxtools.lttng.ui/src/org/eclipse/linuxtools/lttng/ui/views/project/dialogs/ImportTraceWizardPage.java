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

package org.eclipse.linuxtools.lttng.ui.views.project.dialogs;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProjectNode;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceImportPage1;

/**
 * <b><u>ImportTraceWizardPage</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("restriction")
public class ImportTraceWizardPage extends WizardFileSystemResourceImportPage1 {

	public ImportTraceWizardPage(IWorkbench workbench, IStructuredSelection selection) {
		super(workbench, selection);
		LTTngProjectNode folder = (LTTngProjectNode) selection.getFirstElement();
		String path = folder.getTracesFolder().getFolder().getFullPath().toOSString();
		setContainerFieldValue(path);
	}

}
