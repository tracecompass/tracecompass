/*******************************************************************************
 * Copyright (c) 2018 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.trim;

import java.nio.file.Path;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

/**
 * Interface to augment
 * {@link org.eclipse.tracecompass.tmf.core.trace.ITmfTrace} implementations
 * that offer trimming capabilities. This means creating a copy of the trace
 * that contains only the events in a given time range.
 *
 * @author Alexandre Montplaisir
 * @since 3.4
 */
public interface ITmfTrimmableTrace {

    /**
     * Perform trim operation on the current trace, keeping only the area
     * overlapping the passed time range. The new trace will be created in the
     * destination path.
     *
     * @param range
     *            The time range outside of which to trim. Will be clamped to the
     *            original trace's own time range.
     * @param destinationPath
     *            The location of the existing empty directory where the new trace
     *            will be created. The {@link ITmfTrimmableTrace} will create a
     *            {@link Path} if it is not created. If the path exists, it shall be
     *            used. If the path is a file instead of a directory, it will fail
     * @param monitor
     *            Progress monitor for cases where the operation is ran from inside
     *            a Job. You can use a
     *            {@link org.eclipse.core.runtime.NullProgressMonitor} if none is
     *            available.
     * @return The path of the trace must be either the destination path or an
     *         immediate child of the destination path. Null if failed.
     * @throws CoreException
     *             Optional exception indicating an error during the execution of
     *             the operation. Will be reported to the user inside an error
     *             dialog.
     */
    @Nullable Path trim(TmfTimeRange range, Path destinationPath, IProgressMonitor monitor) throws CoreException;
}
