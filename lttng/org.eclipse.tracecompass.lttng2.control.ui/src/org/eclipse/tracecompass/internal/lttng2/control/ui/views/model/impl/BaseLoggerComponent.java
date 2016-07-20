/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseLoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ILoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.LoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.BaseLoggerPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Implementation of the base trace logger component.
 *
 * @author Bruno Roy
 */
public class BaseLoggerComponent extends TraceControlComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */
    public static final String TRACE_LOGGER_ICON_FILE_ENABLED = "icons/obj16/logger_enabled.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The logger information implementation.
     */
    private IBaseLoggerInfo fLoggerInfo;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param name
     *            the name of the component.
     * @param parent
     *            the parent of this component.
     */
    public BaseLoggerComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_LOGGER_ICON_FILE_ENABLED);
        fLoggerInfo = new LoggerInfo(name);
    }


    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Sets the logger information.
     *
     * @param loggerInfo
     *            the logger info to set.
     */
    public void setLoggerInfo(IBaseLoggerInfo loggerInfo) {
        fLoggerInfo = loggerInfo;
    }

    /**
     * @return the trace logger log level
     */
    public ITraceLogLevel getLogLevel() {
        return fLoggerInfo.getLogLevel();
    }

    /**
     * Sets the trace logger log level to the given level
     *
     * @param level
     *            event log level to set
     */
    public void setLogLevel(ITraceLogLevel level) {
        fLoggerInfo.setLogLevel(level);
    }

    /**
     * Sets the trace logger log level to the level specified by the given name.
     *
     * @param levelName
     *            logger log level name
     */
    public void setLogLevel(String levelName) {
        fLoggerInfo.setLogLevel(levelName);
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IPropertySource.class) {
            return adapter.cast(new BaseLoggerPropertySource(this));
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
     * @return the domain type ({@link TraceDomainType})
     */
    public TraceDomainType getDomain() {
        return fLoggerInfo.getDomain();
    }

    /**
     * Sets the logger information.
     * @param loggerInfo - the logger information to set.
     */
    public void setLoggerInfo(ILoggerInfo loggerInfo) {
        fLoggerInfo = loggerInfo;
    }
}
