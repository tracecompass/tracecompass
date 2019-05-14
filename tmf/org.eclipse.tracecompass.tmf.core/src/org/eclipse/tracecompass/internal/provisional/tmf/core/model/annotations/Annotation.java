/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;

/**
 * Annotations returned by data provider that implement
 * {@link IOutputAnnotationProvider}. A chart annotation is used to mark an
 * interesting area at a given time or range, or to add a symbol to the chart. A
 * tree annotation is used to add a mark to a specific tree entry.
 *
 * @author Simon Delisle
 */
public class Annotation {

    /**
     * Annotation type
     */
    public enum AnnotationType {
        /**
         * If the annotation should be in the chart
         */
        CHART,

        /**
         * If the annotation should be in the tree
         */
        TREE
    }

    private final @Nullable String fLabel;
    private final long fTime;
    private final long fDuration;
    private final long fEntryId;
    private final AnnotationType fType;
    private final OutputElementStyle fStyle;

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
        fTime = time;
        fDuration = duration;
        fEntryId = entryId;
        fType = type;
        fLabel = label;
        fStyle = style;
    }

    /**
     * Get the annotation time, for chart annotations.
     *
     * @return Annotation time
     */
    public long getTime() {
        return fTime;
    }

    /**
     * Get the annotation duration, for chart annotations.
     *
     * @return Annotation duration
     */
    public long getDuration() {
        return fDuration;
    }

    /**
     * Get the entry model ID.
     *
     * @return Entry model ID associated to this annotation or -1 if this
     *         annotation is not attached to a single entry
     */
    public long getEntryId() {
        return fEntryId;
    }

    /**
     * Get the annotation type.
     *
     * @return Annotation type
     */
    public AnnotationType getType() {
        return fType;
    }

    /**
     * Get the annotation label.
     *
     * @return Annotation label or null
     */
    public @Nullable String getLabel() {
        return fLabel;
    }

    /**
     * @return Annotation style
     */
    public OutputElementStyle getStyle() {
        return fStyle;
    }
}
