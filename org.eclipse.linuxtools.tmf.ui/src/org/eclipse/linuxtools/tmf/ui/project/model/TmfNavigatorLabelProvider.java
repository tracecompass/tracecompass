/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.osgi.framework.Bundle;

/**
 * <b><u>TmfNavigatorLabelProvider</u></b>
 * <p>
 */
public class TmfNavigatorLabelProvider implements ICommonLabelProvider {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final Image fFolderIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    private static final String fTraceIconFile = "icons/elcl16/trace.gif"; //$NON-NLS-1$
    private static final String fExperimentIconFile = "icons/elcl16/experiment.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Image fTraceFolderIcon = fFolderIcon;
    private final Image fExperimentFolderIcon = fFolderIcon;

    private final Image fDefaultTraceIcon;
    private final Image fExperimentIcon;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TmfNavigatorLabelProvider() {
        Bundle bundle = Activator.getDefault().getBundle();
        fDefaultTraceIcon = loadIcon(bundle, fTraceIconFile);
        fExperimentIcon = loadIcon(bundle, fExperimentIconFile);
    }

    private Image loadIcon(Bundle bundle, String url) {
        Activator plugin = Activator.getDefault();
        String key = bundle.getSymbolicName() + "/" + url; //$NON-NLS-1$
        Image icon = plugin.getImageRegistry().get(key);
        if (icon == null) {
            URL imageURL = bundle.getResource(url);
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageURL);
            icon = descriptor.createImage();
            plugin.getImageRegistry().put(key, icon);
        }
        return icon;
    }

    // ------------------------------------------------------------------------
    // ICommonLabelProvider
    // ------------------------------------------------------------------------

    @Override
    public Image getImage(Object element) {

        if (element instanceof TmfTraceElement) {
            TmfTraceElement trace = (TmfTraceElement) element;
            String icon = null;
            try {
                String name = trace.getResource().getPersistentProperty(TmfCommonConstants.TRACEBUNDLE);
                icon = trace.getResource().getPersistentProperty(TmfCommonConstants.TRACEICON);
                if (name != null && icon != null) {
                    Bundle bundle = Platform.getBundle(name);
                    return loadIcon(bundle, icon);
                }
            } catch (CoreException e) {
            }
            return fDefaultTraceIcon;
        }

        if (element instanceof TmfExperimentElement)
            return fExperimentIcon;

        if (element instanceof TmfExperimentFolder)
            return fExperimentFolderIcon;

        if (element instanceof TmfTraceFolder)
            return fTraceFolderIcon;

        return null;
    }

    @Override
    public String getText(Object element) {

        if (element instanceof TmfTraceFolder) {
            TmfTraceFolder folder = (TmfTraceFolder) element;
            return folder.getName() + " [" + folder.getTraces().size() + "]"; //$NON-NLS-1$//$NON-NLS-2$
        }

        if (element instanceof TmfExperimentElement) {
            TmfExperimentElement folder = (TmfExperimentElement) element;
            return folder.getName() + " [" + folder.getTraces().size() + "]"; //$NON-NLS-1$//$NON-NLS-2$
        }

        if (element instanceof TmfExperimentFolder) {
            TmfExperimentFolder folder = (TmfExperimentFolder) element;
            return folder.getName() + " [" + folder.getChildren().size() + "]"; //$NON-NLS-1$//$NON-NLS-2$
        }

        // Catch all
        if (element instanceof ITmfProjectModelElement) {
            return ((ITmfProjectModelElement) element).getName();
        }

        return null;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public void restoreState(IMemento aMemento) {
    }

    @Override
    public void saveState(IMemento aMemento) {
    }

    @Override
    public String getDescription(Object anElement) {
        return getText(anElement);
    }

    @Override
    public void init(ICommonContentExtensionSite aConfig) {
    }

}
