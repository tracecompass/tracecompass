/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IUstProviderInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;

/**
 * <p>
 * Implementation of the trace provider group.
 * </p>
 *
 * @author Bernd Hufmann
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
     * @param monitor - a progress monitor
     * @throws ExecutionException If the command fails
     */
    public void getProviderFromNode(IProgressMonitor monitor) throws ExecutionException {

        List<IBaseEventInfo> eventInfos = getControlService().getKernelProvider(monitor);

        if (!eventInfos.isEmpty()) {
            KernelProviderComponent component = new KernelProviderComponent(Messages.TraceControl_KernelProviderDisplayName, this);
            addChild(component);
            component.setEventInfo(eventInfos);
        }

        List<IUstProviderInfo> allProviders = getControlService().getUstProvider(monitor);

        for (Iterator<IUstProviderInfo> iterator = allProviders.iterator(); iterator.hasNext();) {
            IUstProviderInfo ustProviderInfo = iterator.next();
            UstProviderComponent ustComponent = new UstProviderComponent(ustProviderInfo.getName(), this);
            addChild(ustComponent);
            ustComponent.setUstProvider(ustProviderInfo);
        }
    }

    /**
     * Returns whether the kernel provider is available or not
     * @return <code>true</code> if kernel provide is available or <code>false</code>
     */
    public boolean hasKernelProvider() {
        List<ITraceControlComponent> kernelList = getChildren(KernelProviderComponent.class);
        return !kernelList.isEmpty();
    }

    /**
     * Returns if node supports filtering of events
     * @param domain - the domain type ({@link TraceDomainType})
     * @return <code>true</code> if node supports filtering else <code>false</code>
     */
    public boolean isEventFilteringSupported(TraceDomainType domain) {
        return ((TargetNodeComponent)getParent()).isEventFilteringSupported(domain);
    }

    /**
     * Checks if enabling of per syscall event is supported
     *
     * @return <code>true</code> if enabling of per syscall event is supported else <code>false</code>
     */
    public boolean isPerSyscallEventsSupported() {
        return ((TargetNodeComponent) getParent()).isPerSyscallEventsSupported();
    }

    /**
     * Returns if node supports JUL logging or not
     * @return <code>true</code> if it supports JUL logging else <code>false</code>
     *
     */
    public boolean isJulLoggingSupported() {
        return ((TargetNodeComponent) getParent()).isJulLoggingSupported();
    }
    /**
     * Returns if node supports LOG4J logging or not
     * @return <code>true</code> if it supports LOG4J logging else <code>false</code>
     *
     */
    public boolean isLog4jLoggingSupported() {
        return ((TargetNodeComponent) getParent()).isLog4jLoggingSupported();
    }
    /**
     * Returns if node supports Python logging or not
     * @return <code>true</code> if it supports Python logging else <code>false</code>
     *
     */
    public boolean isPythonLoggingSupported() {
        return ((TargetNodeComponent) getParent()).isPythonLoggingSupported();
    }
}

