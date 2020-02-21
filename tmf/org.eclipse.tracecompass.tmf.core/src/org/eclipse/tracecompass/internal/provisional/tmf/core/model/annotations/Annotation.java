/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.OutputElement;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;

/**
 * Annotations returned by data providers that implement
 * {@link IOutputAnnotationProvider}. A chart annotation is used to mark an
 * interesting area at a given time or range, or to add a symbol to the chart. A
 * tree annotation is used to add a mark to a specific tree entry.
 *
 * @author Simon Delisle
 */
public class Annotation extends OutputElement implements IAnnotation {

    private final @Nullable String fLabel;
    private final long fTime;
    private final long fDuration;
    private final long fEntryId;
    private final AnnotationType fType;

    /**
     * Constructor for a chart annotation
     *
     * @param time
     *            Annotation start time
     * @param duration
     *            Annotation duration
     * @param entryId
     *            EntryId to position the annotation or -1 if it is not attached
     *            to a single entry
     * @param label
     *            Annotation label for display purposes
     * @param style
     *            Style to use for this annotation
     */
    public Annotation(long time, long duration, long entryId, @Nullable String label, OutputElementStyle style) {
        this(time, duration, entryId, AnnotationType.CHART, label, style);
    }

    /**
     * Constructor for a tree annotation
     *
     * @param entryId
     *            EntryId to position the annotation or -1 if it is not attached
     *            to a single entry
     * @param label
     *            Annotation label for display purposes
     * @param style
     *            Style to use for this annotation
     */
    public Annotation(long entryId, @Nullable String label, OutputElementStyle style) {
        this(Long.MIN_VALUE, 0, entryId, AnnotationType.TREE, label, style);
    }

    /**
     * Constructor
     *
     * @param time
     *            Annotation start time
     * @param duration
     *            Annotation duration
     * @param entryId
     *            EntryId to position the annotation or -1 if it is not attached
     *            to a single entry
     * @param type
     *            Annotation type
     * @param label
     *            Annotation label for display purposes
     * @param style
     *            Style to use for this annotation
     */
    public Annotation(long time, long duration, long entryId, AnnotationType type, @Nullable String label, OutputElementStyle style) {
        super(style);
        fTime = time;
        fDuration = duration;
        fEntryId = entryId;
        fType = type;
        fLabel = label;
    }

    @Override
    public long getTime() {
        return fTime;
    }

    @Override
    public long getDuration() {
        return fDuration;
    }

    @Override
    public long getEntryId() {
        return fEntryId;
    }

    @Override
    public AnnotationType getType() {
        return fType;
    }

    @Override
    public @Nullable String getLabel() {
        return fLabel;
    }
}
