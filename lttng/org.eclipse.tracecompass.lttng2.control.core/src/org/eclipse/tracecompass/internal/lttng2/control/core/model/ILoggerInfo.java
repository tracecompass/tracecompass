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
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

/**
 * Interface for retrieval of basic logger information.
 *
 * @author Bruno Roy
 */
public interface ILoggerInfo extends IBaseLoggerInfo {

    /**
     * @return the logger state (enabled or disabled).
     */
    TraceEnablement getState();

    /**
     * Sets the logger state (enablement) to the given value.
     *
     * @param state
     *            state to set.
     */
    void setState(TraceEnablement state);

    /**
     * Sets the logger state (enablement) to the value specified by the given
     * name.
     *
     * @param stateName
     *            state to set.
     */
    void setState(String stateName);

    /**
     * Returns the log level type.
     *
     * @return log level type.
     */
    LogLevelType getLogLevelType();

    /**
     * Sets the log level type.
     *
     * @param type
     *            log level type
     */
    void setLogLevelType(LogLevelType type);

    /**
     * Sets the log level type based on given short name.
     *
     * @param shortName
     *            short name of type
     */
    void setLogLevelType(String shortName);
}
