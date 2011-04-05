/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng;

/**
 * <b><u>LttngConstants</u></b>
 * <p>
 * Declaration of LTTng specific constants.
 * <p>
 */
public class LttngConstants {

    /**
     * <h4>Number of bits of an integer to be used for statistic node identifier. </h4>
     */
    public static final int STATS_ID_SHIFT = 28;
    /**
     * <h4>Maximum number of trace ids to be created, before wrapping around to 0. </h4>
     * Note that there is a tight coupling to STATS_ID_SHIFT, because the trace id is
     * also used for statistics node identification.
     */
    public static final int MAX_NUMBER_OF_TRACES_ID = (1 << STATS_ID_SHIFT) - 1;
    public static final int STATS_ID_MASK = MAX_NUMBER_OF_TRACES_ID;

    /**
     * <h4>Statistic node identifier for unknown/none kernel submode. </h4>
     */
    public static final int STATS_NONE_ID = 0x1 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for IRQ kernel submodes. </h4>
     */
    public static final int STATS_IRQ_NAME_ID = 0x2 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for soft IRQ kernel submodes. </h4>
     */
    public static final int STATS_SOFT_IRQ_NAME_ID = 0x3 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for sys_call kernel submodes.</h4>
     */
    public static final int STATS_SYS_CALL_NAME_ID = 0x4 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for trab kernel submodes. </h4>
     */
    public static final int STATS_TRAP_NAME_ID = 0x5 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the trace. </h4>
     */
    public static final int STATS_TRACE_NAME_ID = 0x6 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the CPU IDs. </h4>
     */
    public static final int STATS_CPU_ID = 0x7 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the kernel modes. </h4>
     */
    public static final int STATS_MODE_ID = 0x8 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the kernel function IDs. </h4>
     */
    public static final int STATS_FUNCTION_ID = 0x9 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the process IDs. </h4>
     */
    public static final int STATS_PROCESS_ID = 0xA << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the event types. </h4>
     */
    public static final int STATS_TYPE_ID = 0xB << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the event types. </h4>
     */
    public static final int STATS_CATEGORY_ID = 0xC << STATS_ID_SHIFT;

    /**
     * <h4>Background requests block size </h4>
     */
    public static final int DEFAULT_BLOCK_SIZE = 50000;
}