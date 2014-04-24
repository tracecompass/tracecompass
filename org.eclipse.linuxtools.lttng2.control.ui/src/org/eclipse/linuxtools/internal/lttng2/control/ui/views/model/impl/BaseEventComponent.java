/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IFieldInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.EventInfo;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.property.BaseEventPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <p>
 * Implementation of the base trace event component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class BaseEventComponent extends TraceControlComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */
    public static final String TRACE_EVENT_ICON_FILE_ENABLED = "icons/obj16/event_enabled.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The Event information implementation.
     */
    private IBaseEventInfo fEventInfo;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public BaseEventComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_EVENT_ICON_FILE_ENABLED);
        fEventInfo = new EventInfo(name);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Sets the event information.
     * @param eventInfo - the event info to set.
     */
    public void setEventInfo(IBaseEventInfo eventInfo) {
        fEventInfo = eventInfo;
    }

    /**
     * @return the event type.
     */
    public TraceEventType getEventType() {
        return fEventInfo.getEventType();
    }

    /**
     * Sets the event type to the given value.
     * @param type - type to set.
     */
    public void setEventType(TraceEventType type) {
        fEventInfo.setEventType(type);
    }

    /**
     * Sets the event type to the value specified by the give name.
     * @param typeName - the type name.
     */
    public void setEventType(String typeName) {
        fEventInfo.setEventType(typeName);
    }

    /**
     * @return the trace event log level
     */
    public TraceLogLevel getLogLevel() {
        return fEventInfo.getLogLevel();
    }

    /**
     * Sets the trace event log level to the given level
     * @param level - event log level to set
     */
    public void setLogLevel(TraceLogLevel level) {
        fEventInfo.setLogLevel(level);
    }

    /**
     * Sets the trace event log level to the level specified by the given name.
     * @param levelName - event log level name
     */
    public void setLogLevel(String levelName) {
        fEventInfo.setLogLevel(levelName);
    }

    /**
     * @return a String containing pairs if field name and data type
     */
    public String getFieldString() {
        IFieldInfo[] fields = fEventInfo.getFields();
        if ((fields != null) && (fields.length > 0)) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < fields.length; i++) {
                buffer.append(fields[i].getName());
                buffer.append("="); //$NON-NLS-1$
                buffer.append(fields[i].getFieldType());
                if (i != fields.length-1) {
                    buffer.append(";"); //$NON-NLS-1$
                }
            }
            return buffer.toString();
        }
        return null;
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new BaseEventPropertySource(this);
        }
        return null;
    }

    /**
     * @return target node component.
     */
    public TargetNodeComponent getTargetNode() {
        return (TargetNodeComponent) getParent().getParent();
    }

    /**
     * @return if provider kernel or UST
     */
    public boolean isKernel() {
        return getParent() instanceof KernelProviderComponent;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
}
