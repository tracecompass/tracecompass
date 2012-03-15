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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;

/**
 * <b><u>TraceSessionGroup</u></b>
 * <p>
 * Implementation of the trace session group.
 * </p>
 */
public class TraceSessionGroup extends TraceControlComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */    
    public static final String TRACE_SESSIONS_ICON_FILE = "icons/obj16/sessions.gif"; //$NON-NLS-1$
    
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
    public TraceSessionGroup(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_SESSIONS_ICON_FILE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the parent target node
     */
    public TargetNodeComponent getTargetNode() {
        return (TargetNodeComponent)getParent();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Retrieves the sessions information from the node.
     * @throws ExecutionException
     */
    public void getSessionsFromNode() throws ExecutionException {
        getSessionsFromNode(new NullProgressMonitor());
    }

    /**
     * Retrieves the sessions information from the node.
     * @param monitor - a progress monitor
     * @throws ExecutionException
     */
    public void getSessionsFromNode(IProgressMonitor monitor) throws ExecutionException {
        String[] sessionNames = getControlService().getSessionNames(monitor);
        for (int i = 0; i < sessionNames.length; i++) {
            TraceSessionComponent session = new TraceSessionComponent(sessionNames[i], this);
            addChild(session);
            session.getConfigurationFromNode(monitor);
        }
    }

    /**
     * Creates a session with given session name and location. 
     * @param sessionName - a session name to create
     * @param sessionPath - a path for storing the traces (use null for default)
     * @return the session information
     * throws ExecutionExecption
     */
    public void createSession(String sessionName, String sessionPath) throws ExecutionException {
        createSession(sessionName, sessionPath, new NullProgressMonitor());
    }
    
    /**
     * Creates a session with given session name and location. 
     * @param sessionName - a session name to create
     * @param sessionPath - a path for storing the traces (use null for default)
     * @Param monitor - a progress monitor 
     * @return the session information
     * throws ExecutionExecption
     */
    public void createSession(String sessionName, String sessionPath, IProgressMonitor monitor) throws ExecutionException {
        ISessionInfo sessionInfo = getControlService().createSession(sessionName, sessionPath, monitor);
        if (sessionInfo != null) {
            TraceSessionComponent session = new TraceSessionComponent(sessionInfo.getName(), TraceSessionGroup.this);
            addChild(session);
            session.getConfigurationFromNode(monitor);
        }
    }

    /**
     * Destroys a session with given session name. 
     * @param session - a session component to destroy
     * throws ExecutionExecption
     */
    public void destroySession(TraceSessionComponent session) throws ExecutionException {
        destroySession(session, new NullProgressMonitor());
    }
    
    /**
     * Destroys a session with given session name. 
     * @param session - a session component to destroy
     * @param monitor - a progress monitor
     * throws ExecutionExecption
     */
    public void destroySession(TraceSessionComponent session, IProgressMonitor monitor) throws ExecutionException {
        getControlService().destroySession(session.getName(), monitor);
        session.removeAllChildren();
        removeChild(session);
    }
}
