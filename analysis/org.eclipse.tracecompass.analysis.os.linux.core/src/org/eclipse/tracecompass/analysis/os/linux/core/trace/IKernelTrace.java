/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.trace;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;

/**
 * Trace type that represents a Linux kernel trace.
 *
 * Any trace implementing the interface should be able to run the different
 * Linux kernel analyses in this plugin.
 *
 * @author Alexandre Montplaisir
 */
public interface IKernelTrace extends ITmfTrace {

    /**
     * Get the event layout of this trace. Many known concepts from the Linux
     * kernel may be exported under different names, depending on the tracer.
     *
     * @return The event layout
     */
    IKernelAnalysisEventLayout getKernelEventLayout();

    /** @since 2.0 */
    @Override
    default TmfTraceContext createTraceContext(TmfTimeRange selection, TmfTimeRange windowRange, @Nullable IFile editorFile, @Nullable ITmfFilter filter) {
        return new LinuxTraceContext(selection, windowRange, editorFile, filter, this);
    }
}
