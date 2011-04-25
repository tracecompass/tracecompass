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

package org.eclipse.linuxtools.lttng.ui.views.project.model;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
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

	private final String fTraceIconFile      = "icons/obj16/garland16.png"; //$NON-NLS-1$
//	private final String fExperimentIconFile = "icons/obj16/garland16.png"; //$NON-NLS-1$

	private final Image fOpenedProjectIcon;
	private final Image fClosedProjectIcon;
	private final Image fFolderIcon;
	private final Image fTraceIcon;
	private final Image fExperimentIcon;

	/**
	 * 
	 */
	public LTTngProjectLabelProvider() {

		fOpenedProjectIcon = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);
		fClosedProjectIcon = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED);
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
	@Override
	public Image getImage(Object element) {

		if (element instanceof LTTngProjectNode) {
			LTTngProjectNode project = (LTTngProjectNode) element;
			return (project.isOpen()) ? fOpenedProjectIcon : fClosedProjectIcon;
		}

		if (element instanceof LTTngTraceFolderNode) {
			return fFolderIcon;
		}

		if (element instanceof LTTngTraceNode) {
			return fTraceIcon;
		}

		if (element instanceof LTTngExperimentFolderNode) {
			return fFolderIcon;
		}

		if (element instanceof LTTngExperimentNode) {
			return fExperimentIcon;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		ILTTngProjectTreeNode node = (ILTTngProjectTreeNode) element;
		String label = node.getName();
		if (node instanceof LTTngTraceFolderNode      || 
			node instanceof LTTngExperimentFolderNode ||
			node instanceof LTTngExperimentNode)
		{
			label += " [" + node.getChildren().size() + "]";  //$NON-NLS-1$//$NON-NLS-2$
		}
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

}
