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
import java.util.Iterator;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.KernelProviderPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <p>
 * Implementation of the Kernel provider component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class KernelProviderComponent extends TraceControlComponent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Path to icon file for this component.
     */
    public static final String KERNEL_PROVIDER_ICON_FILE = "icons/obj16/targets.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public KernelProviderComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setToolTip(Messages.TraceControl_ProviderDisplayName);
        setImage(KERNEL_PROVIDER_ICON_FILE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Sets the events information for this component.
     * @param eventInfos - events information to set.
     */
    public void setEventInfo(List<IBaseEventInfo> eventInfos) {
        List<ITraceControlComponent> eventComponents = new ArrayList<>();
        for (Iterator<IBaseEventInfo> iterator = eventInfos.iterator(); iterator.hasNext();) {
            IBaseEventInfo baseEventInfo = iterator.next();
            BaseEventComponent component = new BaseEventComponent(baseEventInfo.getName(), this);
            component.setEventInfo(baseEventInfo);
            eventComponents.add(component);
        }
        setChildren(eventComponents);
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IPropertySource.class) {
            return adapter.cast(new KernelProviderPropertySource(this));
        }
        return null;
    }

}
