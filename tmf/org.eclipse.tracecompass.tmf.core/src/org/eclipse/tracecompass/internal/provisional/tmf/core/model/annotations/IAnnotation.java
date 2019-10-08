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
import org.eclipse.tracecompass.tmf.core.model.ITimeElement;

/**
 * Model of an annotation. A chart annotation is used to mark an interesting
 * area at a given time or range, or to add a symbol to the chart. A tree
 * annotation is used to add a mark to a specific tree entry.
 */
public interface IAnnotation extends ITimeElement {

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

    @Override
    default long getStartTime() {
        return getTime();
    }

    /**
     * Get the annotation time, for chart annotations.
     *
     * @return Annotation time
     */
    public long getTime();

    /**
     * Get the entry model ID.
     *
     * @return Entry model ID associated to this annotation or -1 if this
     *         annotation is not attached to a single entry
     */
    public long getEntryId();

    /**
     * Get the annotation type.
     *
     * @return Annotation type
     */
    public AnnotationType getType();

    /**
     * Get the annotation label.
     *
     * @return Annotation label or null
     */
    public @Nullable String getLabel();
}
