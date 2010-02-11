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

package org.eclipse.linuxtools.lttng.ui.views.project;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentEntry;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentFolder;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProject;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngTraceEntry;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngTraceFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>LTTngProjectLabelProvider</u></b>
 * <p>
 * TODO: Implement me. Please.
 * TODO: Find proper icons for traces and experiments
 */
public class LTTngProjectLabelProvider implements ILabelProvider {

	private final String fTraceIconFile      = "icons/garland16.png";
//	private final String fExperimentIconFile = "icons/garland16.png";

	private final Image fProjectIcon;
	private final Image fFolderIcon;
	private final Image fTraceIcon;
	private final Image fExperimentIcon;

	/**
	 * 
	 */
	public LTTngProjectLabelProvider() {

		fProjectIcon = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);
		fFolderIcon  = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

		fTraceIcon = loadIcon(fTraceIconFile);
		fExperimentIcon = fFolderIcon; // loadIcon(fExperimentIconFile);
	}

	private Image loadIcon(String url) {
		LTTngUiPlugin plugin = LTTngUiPlugin.getDefault();
		Image icon = plugin.getImageRegistry().get(url);
		if (icon == null) {
			URL imageURL = plugin.getBundle().getEntry(url);
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageURL);
			icon = descriptor.createImage();
			plugin.getImageRegistry().put(url, icon);
		}
		return icon;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {

		if (element instanceof LTTngProject) {
			return fProjectIcon;
		}

		if (element instanceof LTTngTraceFolder) {
			return fFolderIcon;
		}

		if (element instanceof LTTngTraceEntry) {
			return fTraceIcon;
		}

		if (element instanceof LTTngExperimentFolder) {
			return fFolderIcon;
		}

		if (element instanceof LTTngExperimentEntry) {
			return fExperimentIcon;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		
		if (element instanceof LTTngProject) {
			LTTngProject entry = (LTTngProject) element;
			return entry.getName();
		}

		if (element instanceof LTTngTraceFolder) {
			LTTngTraceFolder entry = (LTTngTraceFolder) element;
			return entry.getName();
		}

		if (element instanceof LTTngTraceEntry) {
			LTTngTraceEntry entry = (LTTngTraceEntry) element;
			return entry.getName();
		}

		if (element instanceof LTTngExperimentFolder) {
			LTTngExperimentFolder entry = (LTTngExperimentFolder) element;
			return entry.getName();
		}

		if (element instanceof LTTngExperimentEntry) {
			LTTngExperimentEntry entry = (LTTngExperimentEntry) element;
			return entry.getName();
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

}
