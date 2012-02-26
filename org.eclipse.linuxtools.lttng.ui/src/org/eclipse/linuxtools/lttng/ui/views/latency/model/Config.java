/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactor code
 *   Bernd Hufmann - Added and updated constants
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.latency.model;

/**
 * <b><u>Config</u></b>
 * <p>
 * Configuration class, holds some application constants.
 * 
 * @author Philippe Sawicki
 */
public class Config {

    /**
     * Private constructor to defeat instantiation.
     */
    private Config() {
    }

    /**
     * Time scale for TMF events;
     */
    public static final byte TIME_SCALE = -9;
    
    /**
     * Size of the point buffer holding point values before sending them to the view.
     */
    public static final int POINT_BUFFER_SIZE = 10000;

    /**
     * Histogram bar width.
     */
    public static final int DEFAULT_HISTOGRAM_BAR_WIDTH = 2;
    
    /**
     * Histogram bar width increase step.
     */
    public static final int MIN_HISTOGRAM_BAR_WIDTH = 1;

    /**
     * Histogram bar width increase step.
     */
    public static final int MAX_HISTOGRAM_BAR_WIDTH = 16;

    
    /**
     * Diameter of a point drawn on the chart (in pixels).
     */
    public static final int GRAPH_POINT_DIAMETER = 1;

    /**
     * Graph padding.
     */
    public static final int GRAPH_PADDING = 10;

    /**
     * Default number of buckets used in data models
     */
    public static final int DEFAULT_NUMBER_OF_BUCKETS = 2 * 1000;
    
    /**
     * Invalid event time
     */
    public static final long INVALID_EVENT_TIME = -1;
}