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
 *   Bernd Hufmann - Updated for LTTng trace control
 *   
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.ProviderResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TargetResource;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.internal.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.model.TraceAdapterFactory;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends SystemBasePlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.lttng.ui"; //$NON-NLS-1$

    // Icon names
    public static final String ICON_ID_PROVIDER = "ICON_ID_PROVIDER"; //$NON-NLS-1$
    public static final String ICON_ID_TARGET = "ICON_ID_TARGET"; //$NON-NLS-1$
    public static final String ICON_ID_TRACE = "ICON_ID_TRACE"; //$NON-NLS-1$
    public static final String ICON_ID_NEW_TRACE = "ICON_ID_NEW_TRACE"; //$NON-NLS-1$
    public static final String ICON_ID_CONFIG_MARKERS = "ICON_ID_CONFIG_MARKERS"; //$NON-NLS-1$
    public static final String ICON_ID_CONFIG_TRACE = "ICON_ID_CONFIG_TRACE"; //$NON-NLS-1$
    public static final String ICON_ID_CHECKED = "ICON_ID_CHECKED"; //$NON-NLS-1$
    public static final String ICON_ID_UNCHECKED = "ICON_ID_UNCHECKED"; //$NON-NLS-1$
    public static final String ICON_ID_IMPORT_TRACE = "ICON_ID_IMPORT_TRACE"; //$NON-NLS-1$
    public static final String ICON_ID_EDIT = "ICON_ID_EDIT"; //$NON-NLS-1$
	
	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		TraceDebug.init();
		
        // Trace control initialization
        IAdapterManager manager = Platform.getAdapterManager();
        TraceAdapterFactory factory = new TraceAdapterFactory();
        manager.registerAdapters(factory, ProviderResource.class);
        manager.registerAdapters(factory, TargetResource.class);
        manager.registerAdapters(factory, TraceResource.class);
        
        // Assign shared instance
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		TraceDebug.stop();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
    
    /**
     * Create a System Message for given throwable
     * 
     * @param x - The throwable the message is for
     * @return
     */
    public SystemMessage getMessage(Throwable x) {
        String msg = x.getMessage();
        if (msg == null) {
            msg = ""; //$NON-NLS-1$
        }
        if ((x instanceof ExecutionException) && (((ExecutionException)x).getCause() != null)) {
            msg += " (" + ((ExecutionException)x).getCause().getMessage() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (x instanceof TimeoutException) {
            msg += " (" + Messages.Ltt_TimeoutMsg + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return new SimpleSystemMessage(PLUGIN_ID, SystemMessage.ERROR, msg, x);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.SystemBasePlugin#initializeImageRegistry()
     */
    @Override
    protected void initializeImageRegistry() {
        String path = getIconPath();
        putImageInRegistry(ICON_ID_PROVIDER, path + "obj16/providers.gif"); //$NON-NLS-1$
        putImageInRegistry(ICON_ID_TARGET, path + "obj16/targets.gif"); //$NON-NLS-1$
        putImageInRegistry(ICON_ID_TRACE, path + "obj16/trace.gif"); //$NON-NLS-1$
        putImageInRegistry(ICON_ID_NEW_TRACE, path + "elcl16/new_trace.gif"); //$NON-NLS-1$
        putImageInRegistry(ICON_ID_CONFIG_MARKERS, path + "elcl16/configure_markers.gif"); //$NON-NLS-1$ 
        putImageInRegistry(ICON_ID_CONFIG_TRACE, path + "elcl16/configure_trace.gif"); //$NON-NLS-1$ 
        putImageInRegistry(ICON_ID_CHECKED, path + "elcl16/checked.gif"); //$NON-NLS-1$
        putImageInRegistry(ICON_ID_UNCHECKED, path + "elcl16/unchecked.gif"); //$NON-NLS-1$
        putImageInRegistry(ICON_ID_IMPORT_TRACE, path + "elcl16/import_trace.gif"); //$NON-NLS-1$
        putImageInRegistry(ICON_ID_EDIT, path + "elcl16/edit.gif"); //$NON-NLS-1$
    }
}
