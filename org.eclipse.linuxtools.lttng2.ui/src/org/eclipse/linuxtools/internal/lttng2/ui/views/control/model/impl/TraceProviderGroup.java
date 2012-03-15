/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IUstProviderInfo;

/**
 * <b><u>TraceProviderGroup</u></b>
 * <p>
 * Implementation of the trace provider group.
 * </p>
 */
public class TraceProviderGroup extends TraceControlComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */
    public static final String TRACE_PROVIDERS_ICON_FILE = "icons/obj16/providers.gif"; //$NON-NLS-1$
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor 
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */    
    public TraceProviderGroup(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_PROVIDERS_ICON_FILE);
    }
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /**
     * Gets the provider information from the target node.
     * @throws ExecutionException
     */
    public void getProviderFromNode() throws ExecutionException {
        getProviderFromNode(new NullProgressMonitor());
    }

    /**
     * Gets the provider information from the target node.
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void getProviderFromNode(IProgressMonitor monitor) throws ExecutionException {
        
        List<IBaseEventInfo> eventInfos = getControlService().getKernelProvider(monitor);
        KernelProviderComponent component = new KernelProviderComponent(Messages.TraceControl_KernelProviderDisplayName, this);
        addChild(component);
        component.setEventInfo(eventInfos);
        
        List<IUstProviderInfo> allProviders = getControlService().getUstProvider(monitor);
        
        for (Iterator<IUstProviderInfo> iterator = allProviders.iterator(); iterator.hasNext();) {
            IUstProviderInfo ustProviderInfo = (IUstProviderInfo) iterator.next();
            UstProviderComponent ustComponent = new UstProviderComponent(ustProviderInfo.getName(), this);
            addChild(ustComponent);
            ustComponent.setUstProvider(ustProviderInfo);
        }
    }
}

