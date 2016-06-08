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
 * Interface for retrieval of basic trace logger information.
 *
 * @author Bruno Roy
 */
public interface IBaseLoggerInfo extends ITraceInfo {

    /**
     * @return the trace logger log level
     */
    TraceJulLogLevel getLogLevel();

    /**
     * Sets the trace logger log level to the given level
     *
     * @param level
     *            logger log level to set
     */
    void setLogLevel(TraceJulLogLevel level);

    /**
     * Sets the trace logger log level to the level specified by the given name.
     *
     * @param levelName
     *            logger log level name
     */
    void setLogLevel(String levelName);

    /**
     * @return the domain type ({@link TraceDomainType})
     */
    TraceDomainType getDomain();

    /**
     * Sets the domain type ({@link TraceDomainType})
     *
     * @param domain
     *            the domain type
     */
    void setDomain(TraceDomainType domain);
}
