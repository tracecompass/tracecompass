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

package org.eclipse.linuxtools.tmf.ui.views.project.model;

import java.net.URL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.TmfUiPreferenceInitializer;
import org.eclipse.linuxtools.tmf.ui.parsers.ParserProviderManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>TmfProjectLabelProvider</u></b>
 * <p>
 * TODO: Implement me. Please.
 * TODO: Find proper icons for traces and experiments
 */
public class TmfProjectLabelProvider implements ILabelProvider {

	private final String fTraceIconFile      = "icons/events_view.gif";
//	private final String fExperimentIconFile = "icons/garland16.png";

	private final Image fOpenedProjectIcon;
	private final Image fClosedProjectIcon;
	private final Image fFolderIcon;
    private final Image fTraceIcon;
    private final Image fUnknownTraceIcon;
	private final Image fExperimentIcon;

	/**
	 * 
	 */
	public TmfProjectLabelProvider() {

		fOpenedProjectIcon = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);
		fClosedProjectIcon = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED);
		fFolderIcon  = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

		fTraceIcon = loadIcon(fTraceIconFile);
		fUnknownTraceIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_LCL_LINKTO_HELP);
		fExperimentIcon = fFolderIcon; // loadIcon(fExperimentIconFile);
	}

	private Image loadIcon(String url) {
		TmfUiPlugin plugin = TmfUiPlugin.getDefault();
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

		if (element instanceof TmfProjectNode) {
			TmfProjectNode project = (TmfProjectNode) element;
			if (project.isOpen()) {
		        IEclipsePreferences node = new InstanceScope().getNode(TmfUiPlugin.PLUGIN_ID);
		        if (node.get(TmfUiPreferenceInitializer.ACTIVE_PROJECT_PREFERENCE, TmfUiPreferenceInitializer.ACTIVE_PROJECT_DEFAULT)
		                .equals(project.getName())) {
		            return fTraceIcon; //PATA placeholder image should be a highlighted open project
		        } else {
		            return fOpenedProjectIcon;
		        }
			} else {
			    return fClosedProjectIcon;
			}
		}

		if (element instanceof TmfTraceFolderNode) {
			return fFolderIcon;
		}

		if (element instanceof TmfTraceNode) {
		    try {
		        TmfTraceNode trace = (TmfTraceNode) element;
		        IResource resource = trace.getResource();
		        if (trace.getParent() instanceof TmfExperimentNode) {
		            TmfExperimentNode experiment = (TmfExperimentNode)trace.getParent();
		            TmfTraceFolderNode traceFolder = experiment.getProject().getTracesFolder();
		            resource = traceFolder.getFolder().findMember(resource.getName());
		        }

                if (resource != null && resource.getPersistentProperty(ParserProviderManager.PARSER_PROPERTY) != null) {
                    return fTraceIcon;
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
            return fUnknownTraceIcon;
		}

		if (element instanceof TmfExperimentFolderNode) {
			return fFolderIcon;
		}

		if (element instanceof TmfExperimentNode) {
			return fExperimentIcon;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		ITmfProjectTreeNode node = (ITmfProjectTreeNode) element;
		String label = node.getName();
		if (node instanceof TmfTraceFolderNode      || 
			node instanceof TmfExperimentFolderNode ||
			node instanceof TmfExperimentNode)
		{
			label += " [" + node.getChildren().size() + "]";
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
