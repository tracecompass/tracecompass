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

import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ILoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceJulLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.LoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.TraceLoggerPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Implementation of the trace Logger component.
 *
 * @author Bruno Roy
 */
public class TraceLoggerComponent extends TraceControlComponent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component (enabled state).
     */
    public static final String TRACE_LOGGER_ICON_FILE_ENABLED = "icons/obj16/logger_enabled.gif"; //$NON-NLS-1$
    /**
     * Path to icon file for this component (disabled state).
     */
    public static final String TRACE_LOGGER_ICON_FILE_DISABLED = "icons/obj16/logger_disabled.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The logger information.
     */
    protected ILoggerInfo fLoggerInfo = null;
    /**
     * The image to be displayed when in disabled state.
     */
    private Image fDisabledImage = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param name
     *            the name of the component.
     * @param parent
     *            the parent component.
     */
    public TraceLoggerComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_LOGGER_ICON_FILE_ENABLED);
        setToolTip(Messages.TraceControl_LoggerDisplayName);
        fLoggerInfo = new LoggerInfo(name);
        fDisabledImage = Activator.getDefault().loadIcon(TRACE_LOGGER_ICON_FILE_DISABLED);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IPropertySource.class) {
            return adapter.cast(new TraceLoggerPropertySource(this));
        }
        return null;
    }

    /**
     * Sets the logger information.
     *
     * @param loggerInfo
     *            the logger information to set.
     */
    public void setLoggerInfo(ILoggerInfo loggerInfo) {
        fLoggerInfo = loggerInfo;
    }

    @Override
    public Image getImage() {
        if (fLoggerInfo.getState() == TraceEnablement.DISABLED) {
            return fDisabledImage;
        }
        return super.getImage();
    }

    /**
     * @return the logger state (enabled or disabled).
     */
    public TraceEnablement getState() {
        return fLoggerInfo.getState();
    }

    /**
     * Sets the logger state (enablement) to the given value.
     *
     * @param state
     *            state to set.
     */
    public void setState(TraceEnablement state) {
        fLoggerInfo.setState(state);
    }

    /**
     * Sets the logger state (enablement) to the value specified by the given
     * name.
     *
     * @param stateName
     *            state to set.
     */
    public void setState(String stateName) {
        fLoggerInfo.setState(stateName);
    }

    /**
     * @return the trace logger log level
     */
    public TraceJulLogLevel getLogLevel() {
        return fLoggerInfo.getLogLevel();
    }

    /**
     * Sets the trace logger log level to the given level
     *
     * @param level
     *            logger log level to set
     */
    public void setLogLevel(TraceJulLogLevel level) {
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

    /**
     * Returns the log level type
     *
     * @return logger log level type
     */
    public LogLevelType getLogLevelType() {
        return fLoggerInfo.getLogLevelType();
    }

    /**
     * Sets the trace logger log level type to the given level type
     *
     * @param levelType
     *            logger log level type to set
     */
    public void setLogLevelType(LogLevelType levelType) {
        fLoggerInfo.setLogLevelType(levelType);
    }

    /**
     * @return target node component.
     */
    public TargetNodeComponent getTargetNode() {
        return ((TraceDomainComponent) getParent()).getTargetNode();
    }

    /**
     * @return session name from parent
     */
    public String getSessionName() {
       return ((TraceDomainComponent) getParent()).getSessionName();
    }

    /**
     * @return session from parent
     */
    public TraceSessionComponent getSession() {
       return ((TraceDomainComponent) getParent()).getSession();
    }

    /**
     * @return the domain type ({@link TraceDomainType})
     */
    public TraceDomainType getDomain() {
        return ((TraceDomainComponent) getParent()).getDomain();
    }
}
