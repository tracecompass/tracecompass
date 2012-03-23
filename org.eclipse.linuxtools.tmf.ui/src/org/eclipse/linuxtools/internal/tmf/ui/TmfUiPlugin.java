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

package org.eclipse.linuxtools.internal.tmf.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * <b><u>TmfUiPlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle.
 */
public class TmfUiPlugin extends AbstractUIPlugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.tmf.ui"; //$NON-NLS-1$

	// The shared instance
	private static TmfUiPlugin plugin;
	
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	public TmfUiPlugin() {
	}

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

	public static TmfUiPlugin getDefault() {
		return plugin;
	}

    // ------------------------------------------------------------------------
    // AbstractUIPlugin
    // ------------------------------------------------------------------------

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		TmfUiTracer.init();
		TmfTraceElement.init();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	    TmfUiTracer.stop();
		plugin = null;
		super.stop(context);
	}

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public Image getImageFromPath(String path){
        return getImageDescripterFromPath(path).createImage();
    }
    
    public ImageDescriptor getImageDescripterFromPath(String path){
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    public Image getImageFromImageRegistry(String path) {
        Image icon = getImageRegistry().get(path);
        if (icon == null) {
            icon = getImageDescripterFromPath(path).createImage();
            plugin.getImageRegistry().put(path, icon);
        }
        return icon;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        reg.put(ITmfImageConstants.IMG_UI_ZOOM, getImageFromPath(ITmfImageConstants.IMG_UI_ZOOM));
        reg.put(ITmfImageConstants.IMG_UI_ZOOM_IN, getImageFromPath(ITmfImageConstants.IMG_UI_ZOOM_IN));
        reg.put(ITmfImageConstants.IMG_UI_ZOOM_OUT, getImageFromPath(ITmfImageConstants.IMG_UI_ZOOM_OUT));
        reg.put(ITmfImageConstants.IMG_UI_SEQ_DIAGRAM_OBJ, getImageFromPath(ITmfImageConstants.IMG_UI_SEQ_DIAGRAM_OBJ));
        reg.put(ITmfImageConstants.IMG_UI_ARROW_COLLAPSE_OBJ, getImageFromPath(ITmfImageConstants.IMG_UI_ARROW_COLLAPSE_OBJ));
        reg.put(ITmfImageConstants.IMG_UI_ARROW_UP_OBJ, getImageFromPath(ITmfImageConstants.IMG_UI_ARROW_UP_OBJ));
    }
    
    
}
