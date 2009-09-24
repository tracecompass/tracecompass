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

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>LTTngLabelProvider</u></b>
 * <p>
 *
 * TODO: Implement me. Please.
 */
@SuppressWarnings("restriction")
public class ProjectLabelProvider implements ILabelProvider {

	@SuppressWarnings("unused")
	private final String fLTTngTraceIconFile = "icons/garland16.png";
	private final String fLTTngExperimentIconFile = "icons/garland16.png";

	@SuppressWarnings("unused")
	private final Image fLTTngTraceIcon;
	private final Image fLTTngExperimentIcon;

    public ProjectLabelProvider() {
        LTTngUiPlugin plugin = LTTngUiPlugin.getDefault();
        Image image = plugin.getImageRegistry().get(fLTTngExperimentIconFile);
        if (image == null) {
            URL url = plugin.getBundle().getEntry(fLTTngExperimentIconFile);
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
            image = descriptor.createImage();
        }
        
        fLTTngExperimentIcon = image;
        fLTTngTraceIcon = null;
    }

	public Image getImage(Object element) {
		if (element instanceof Project) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);
		}
		if (element instanceof Folder) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		if (element instanceof File) {
			return fLTTngExperimentIcon;
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof Project) {
			Project project = (Project) element;
			return project.getName();
		}
		if (element instanceof Folder) {
			Folder folder = (Folder) element;
			return folder.getName();
		}
		if (element instanceof File) {
			File file = (File) element;
			return file.getName();
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

}
