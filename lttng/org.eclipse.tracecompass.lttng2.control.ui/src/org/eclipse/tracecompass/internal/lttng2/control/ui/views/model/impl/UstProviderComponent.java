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
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ILoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IUstProviderInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.UstProviderInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.UstProviderPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <p>
 * Implementation of the UST provider component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class UstProviderComponent extends TraceControlComponent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */
    private static final String USTL_PROVIDER_ICON_FILE = "icons/obj16/targets.gif"; //$NON-NLS-1$
    /**
     * UST domain event created by a Java application.
     */
    private static final String LTTNG_JUL_USER_EVENT = "lttng_jul:user_event"; //$NON-NLS-1$
    /**
     * UST domain event created by a Java application.
     */
    private static final String LTTNG_JUL_SYS_EVENT = "lttng_jul:sys_event"; //$NON-NLS-1$
    /**
     * UST domain event created by a Java application.
     */
    private static final String LTTNG_LOG4J_USER_EVENT = "lttng_log4j:user_event"; //$NON-NLS-1$
    /**
     * UST domain event created by a Java application.
     */
    private static final String LTTNG_LOG4J_SYS_EVENT = "lttng_log4j:sys_event"; //$NON-NLS-1$
    /**
     * UST domain event created by a Java application.
     */
    private static final String LTTNG_JUL_EVENT = "lttng_jul:event"; //$NON-NLS-1$
    /**
     * UST domain event created by a Java application.
     */
    private static final String LTTNG_LOG4J_EVENT = "lttng_log4j:event"; //$NON-NLS-1$
    /**
     * UST domain event created by a Python application.
     */
    private static final String LTTNG_PYTHON_EVENT = "lttng_python:event"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The UST provider information.
     */
    private IUstProviderInfo fProviderInfo = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public UstProviderComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(USTL_PROVIDER_ICON_FILE);
        setToolTip(Messages.TraceControl_ProviderDisplayName);
        fProviderInfo = new UstProviderInfo(name);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Sets the UST provider information to the given value.
     * @param providerInfo - the provider information to set
     */
    public void setUstProvider(IUstProviderInfo providerInfo) {
        fProviderInfo = providerInfo;
        IBaseEventInfo[] events = providerInfo.getEvents();
        List<ITraceControlComponent> eventComponents = new ArrayList<>();
        for (int i = 0; i < events.length; i++) {
            BaseEventComponent component  = new BaseEventComponent(events[i].getName(), this);
            component.setEventInfo(events[i]);

            // Only add the events that are useful for the user, no JUL and log4j events
            if ( !events[i].getName().equals(LTTNG_JUL_USER_EVENT) &&
                    !events[i].getName().equals(LTTNG_JUL_SYS_EVENT) &&
                    !events[i].getName().equals(LTTNG_LOG4J_USER_EVENT) &&
                    !events[i].getName().equals(LTTNG_LOG4J_SYS_EVENT) &&
                    !events[i].getName().equals(LTTNG_JUL_EVENT) &&
                    !events[i].getName().equals(LTTNG_LOG4J_EVENT) &&
                    !events[i].getName().equals(LTTNG_PYTHON_EVENT)) {
                eventComponents.add(component);
            }
        }
        setChildren(eventComponents);

        // Adding loggers
        List<ILoggerInfo> loggers = providerInfo.getLoggers();
        List<ITraceControlComponent> loggerComponents = new ArrayList<>();

        for (ILoggerInfo logger : loggers) {
            BaseLoggerComponent component = new BaseLoggerComponent(logger.getName(), this);
            component.setLoggerInfo(logger);

            // Only add the loggers that are useful for the user, not global
            if (!logger.getName().equals("global")) { //$NON-NLS-1$
                loggerComponents.add(component);
            }
        }
        setChildren(loggerComponents);

        StringBuilder providerName = new StringBuilder();
        providerName.append(getName() + " [PID=" + fProviderInfo.getPid() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

        // If the UST provider contains logger(s)
        if (!loggerComponents.isEmpty()) {
            providerName.append(" (With logger)"); //$NON-NLS-1$
        }
        setName(providerName.toString());
    }

    /**
     * @return the process ID of the UST provider.
     */
    public int getPid() {
        return fProviderInfo.getPid();
    }

    /**
     * Sets the process ID of the UST provider to the given value.
     * @param pid - process ID to set
     */
    public void setPid(int pid) {
        fProviderInfo.setPid(pid);
    }

    /**
     * Gets all logger components of a certain domain
     *
     * @param domain
     *            the logger domain type
     *
     * @return all logger components of a certain domain
     */
    public List<ITraceControlComponent> getLoggerComponents(TraceDomainType domain) {
        return getChildren(BaseLoggerComponent.class).stream()
                .filter(loggerComp -> domain.equals(((BaseLoggerComponent) loggerComp).getDomain()))
                .collect(Collectors.toList());
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IPropertySource.class) {
            return adapter.cast(new UstProviderPropertySource(this));
        }
        return null;
    }

}
