/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.log;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class containing methods to support logging in Trace Compass
 *
 * @author Geneviève Bastien
 * @since 2.1
 */
public final class TraceCompassLog {

    private static final String LOGGING_PROPERTY = "org.eclipse.tracecompass.logging"; //$NON-NLS-1$
    private static final Logger TC_PARENT_LOGGER = Logger.getLogger("org.eclipse.tracecompass"); //$NON-NLS-1$

    static {
        /*
         * If Logging is not enabled, to avoid java's default parameters, we set
         * the main TraceCompass logger to not use its parent's logs and set the
         * level at OFF
         */
        String loggingProperty = System.getProperty(LOGGING_PROPERTY);
        if (!"true".equals(loggingProperty)) { //$NON-NLS-1$
            TC_PARENT_LOGGER.setUseParentHandlers(false);
            TC_PARENT_LOGGER.setLevel(Level.OFF);
        }
    }

    private TraceCompassLog() {
    }

    /**
     * Get a logger by name. Calling this method instead of directly
     * {@link Logger#getLogger(String)} insures that the main Trace Compass
     * logger has been initialized to default value instead of using java's
     * default parameters.
     *
     * @param name
     *            The name of the logger to get. It is advised to use something
     *            like {@link Class#getCanonicalName()}, as it will use the full
     *            path of the logged class and the logging can be controlled for
     *            its parent as well.
     * @return The logger
     */
    public static Logger getLogger(String name) {
        return Logger.getLogger(name);
    }

    /**
     * Get a logger for a class
     *
     * @param clazz
     *            The class to get a logger for
     * @return The logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

}
