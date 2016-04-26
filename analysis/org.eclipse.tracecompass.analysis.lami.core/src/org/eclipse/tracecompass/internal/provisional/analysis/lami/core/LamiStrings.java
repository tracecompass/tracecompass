/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core;

/**
 * Strings used in the LAMI v1.0 protocol.
 *
 * The full spec is documented <a href=
 * "https://github.com/lttng/lami-spec/blob/d6129206184988b3fd7cccb76deace4a69c1443e/lami.md">here</a>.
 *
 * @author Alexandre Montplaisir
 * @noimplement This interface is not intended to be implemented by clients.
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface LamiStrings {

    /* Metadata elements */
    String MI_VERSION = "mi-version";
    String TITLE = "title";
    String UNIT = "unit";
    String TABLE_CLASSES = "table-classes";
    String COLUMN_DESCRIPTIONS = "column-descriptions";
    String RESULTS = "results";
    String TIME_RANGE = "time-range";
    String CLASS = "class";
    String DATA = "data";
    String INHERIT = "inherit";

    /* Data types */
    String VALUE = "value";
    String ID = "id";
    String FD = "fd";
    String NAME = "name";
    String PATH = "path";

    /* Time ranges */
    String BEGIN = "begin";
    String END = "end";

    /* Process info */
    String PID = "pid";
    String TID = "tid";

    /* IRQ stuff */
    String NR = "nr";
    String HARD = "hard";

    /* Version object */
    String MAJOR = "major";
    String MINOR = "minor";
    String PATCH = "patch";
    String EXTRA = "extra";

    /* Data classes */
    String DATA_CLASS_UNKNOWN = "unknown";
    String DATA_CLASS_RATIO = "ratio";
    String DATA_CLASS_TIMESTAMP = "timestamp";
    String DATA_CLASS_TIME_RANGE = "time-range";
    String DATA_CLASS_DURATION = "duration";
    String DATA_CLASS_SIZE = "size";
    String DATA_CLASS_BITRATE = "bitrate";
    String DATA_CLASS_SYSCALL = "syscall";
    String DATA_CLASS_PROCESS = "process";
    String DATA_CLASS_PATH = "path";
    String DATA_CLASS_FD = "fd";
    String DATA_CLASS_IRQ = "irq";
    String DATA_CLASS_CPU = "cpu";
    String DATA_CLASS_DISK = "disk";
    String DATA_CLASS_PART = "part";
    String DATA_CLASS_NETIF = "netif";
}
