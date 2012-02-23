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
package org.eclipse.linuxtools.lttng.ui.views.control.model.impl;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.lttng.ui.LTTngUiPlugin;
import org.eclipse.linuxtools.lttng.ui.views.control.Messages;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IDomainInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ISessionInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TraceSessionState;
import org.eclipse.linuxtools.lttng.ui.views.control.property.TraceSessionPropertySource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <b><u>TraceSessionComponent</u></b>
 * <p>
 * Implementation of the trace session component.
 * </p>
 */
public class TraceSessionComponent extends TraceControlComponent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component (inactive state).
     */
    public static final String TRACE_SESSION_ICON_FILE_INACTIVE = "icons/obj16/session_inactive.gif"; //$NON-NLS-1$
    /**
     * Path to icon file for this component (active state).
     */
    public static final String TRACE_SESSION_ICON_FILE_ACTIVE = "icons/obj16/session_active.gif"; //$NON-NLS-1$
    /**
     * Path to icon file for this component (destroyed state).
     */
    public static final String TRACE_SESSION_ICON_FILE_DESTROYED = "icons/obj16/session_destroyed.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The session information.
     */
    private ISessionInfo fSessionInfo = null;
    /**
     * A flag to indicate if session has been destroyed.
     */
    private boolean fIsDestroyed = false;
    /**
     * The image to be displayed in state active.
     */
    private Image fActiveImage = null;
    /**
     * The image to be displayed in state destroyed
     */
    private Image fDestroyedImage = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor 
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */    
    public TraceSessionComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_SESSION_ICON_FILE_INACTIVE);
        setToolTip(Messages.TraceControl_SessionDisplayName);
        fSessionInfo = new SessionInfo(name);
        fActiveImage = LTTngUiPlugin.getDefault().loadIcon(TRACE_SESSION_ICON_FILE_ACTIVE);
        fDestroyedImage = LTTngUiPlugin.getDefault().loadIcon(TRACE_SESSION_ICON_FILE_DESTROYED);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceControlComponent#getImage()
     */
    @Override
    public Image getImage() {
        if (fIsDestroyed) {
            return fDestroyedImage;
        }

        if (fSessionInfo.getSessionState() == TraceSessionState.INACTIVE) {
            return super.getImage();
        }
        
        return fActiveImage;
    }

    /**
     * @return the whether the session is destroyed or not.
     */
    public boolean isDestroyed() {
        return fIsDestroyed;
    }

    /**
     * Sets the session destroyed state to the given value.
     * @param destroyed - value to set.
     */
    public void setDestroyed(boolean destroyed) {
        fIsDestroyed = destroyed;
    }

    /**
     * @return the session state state (active or inactive).
     */
    public TraceSessionState getSessionState() {
        return fSessionInfo.getSessionState();
    }

    /**
     * Sets the session state  to the given value.
     * @param state - state to set.
     */
    public void setSessionState(TraceSessionState state) {
        fSessionInfo.setSessionState(state);
    }

    /**
     * Sets the event state to the value specified by the given name.
     * @param stateName - state to set.
     */
    public void setSessionState(String stateName) {
        fSessionInfo.setSessionState(stateName);
    }

    /**
     * @return path string where session is located.
     */
    public String getSessionPath() {
        return fSessionInfo.getSessionPath();
    }

    /**
     * Sets the path string (where session is located) to the given value.
     * @param path - session path to set.
     */
    public void setSessionPath(String sessionPath) {
        fSessionInfo.setSessionPath(sessionPath);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceControlComponent#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new TraceSessionPropertySource(this);
        }
        return null;
    } 

    /**
     * @return all available domains of this session.
     */
    public TraceDomainComponent[] getDomains() {
        List<ITraceControlComponent> sessions = getChildren(TraceDomainComponent.class);
        return (TraceDomainComponent[])sessions.toArray(new TraceDomainComponent[sessions.size()]);
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Retrieves the session configuration from the node. 
     * @throws ExecutionException
     */
    public void getConfigurationFromNode() throws ExecutionException {
        getConfigurationFromNode(new NullProgressMonitor());
    }

    /**
     * Retrieves the session configuration from the node. 
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void getConfigurationFromNode(IProgressMonitor monitor) throws ExecutionException {
        removeAllChildren();
        fSessionInfo = getControlService().getSession(getName(), monitor);
        IDomainInfo[] domains = fSessionInfo.getDomains();
        for (int i = 0; i < domains.length; i++) {
            TraceDomainComponent domainComponent = new TraceDomainComponent(domains[i].getName(), this);
            addChild(domainComponent);
            domainComponent.setDomainInfo(domains[i]);
        }
    }
    
    /**
     * Starts the session. 
     * throws ExecutionExecption
     */
    public void startSession() throws ExecutionException {
        startSession(new NullProgressMonitor());
    }
    
    /**
     * Starts the session.
     * @param monitor - a progress monitor
     * throws ExecutionExecption
     */
    public void startSession(IProgressMonitor monitor) throws ExecutionException {
        getControlService().startSession(getName(), monitor);
    }
    
    /**
     * Starts the session. 
     * throws ExecutionExecption
     */
    public void stopSession() throws ExecutionException {
        startSession(new NullProgressMonitor());
    }
    
    /**
     * Starts the session.
     * @param monitor - a progress monitor
     * throws ExecutionExecption
     */
    public void stopSession(IProgressMonitor monitor) throws ExecutionException {
        getControlService().stopSession(getName(), monitor);
    }

    /**
     * Enables a list of events with no additional parameters.
     * @param eventNames - a list of event names to enabled.
     * @param isKernel -  a flag for indicating kernel or UST.
     * @throws ExecutionException
     */
    public void enableEvent(List<String> eventNames, boolean isKernel) throws ExecutionException {
        enableEvent(eventNames, isKernel, new NullProgressMonitor());
    }

    /**
     * Enables a list of events with no additional parameters.
     * @param eventNames - a list of event names to enabled.
     * @param isKernel -  a flag for indicating kernel or UST.
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void enableEvent(List<String> eventNames, boolean isKernel, IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableEvent(getName(), null, eventNames, isKernel, monitor);
    }
}
