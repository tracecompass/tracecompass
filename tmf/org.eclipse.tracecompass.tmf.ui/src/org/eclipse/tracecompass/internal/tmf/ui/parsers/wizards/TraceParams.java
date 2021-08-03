/*******************************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.parsers.wizards;

import java.io.File;

import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;

/**
 * Helper for trace building
 *
 * @author Matthew Khouzam
 */
class TraceParams {

    private final CustomTraceDefinition fDefinition;
    private final File fFile;
    private final int fCacheSize;

    public TraceParams(CustomTraceDefinition definition, File file, int cacheSize) {
        fDefinition = definition;
        fFile = file;
        fCacheSize = cacheSize;
    }

    /**
     * @return the definition
     */
    public CustomTraceDefinition getDefinition() {
        return fDefinition;
    }

    /**
     * @return the tmpFile
     */
    public File getFile() {
        return fFile;
    }

    /**
     * @return the cacheSize
     */
    public int getCacheSize() {
        return fCacheSize;
    }

}
