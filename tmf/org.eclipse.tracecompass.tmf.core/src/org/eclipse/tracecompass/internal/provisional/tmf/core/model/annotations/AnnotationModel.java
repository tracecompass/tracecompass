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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data provider annotation model
 *
 * @author Simon Delisle
 */
public class AnnotationModel {

    private final Map<String, Collection<Annotation>> fAnnotations;

    /**
     * Constructor
     *
     * @param annotations
     *            Map of annotations per category
     */
    public AnnotationModel(Map<String, Collection<Annotation>> annotations) {
        fAnnotations = annotations;
    }

    /**
     * Annotations for the model
     *
     * @return Map of annotations per category
     */
    public Map<String, Collection<Annotation>> getAnnotations() {
        return fAnnotations;
    }

    /**
     * Creates a new aggregated {@link AnnotationModel} from an array of
     * {@link AnnotationModel}. Categories will be combined.
     *
     * @param models
     *            array of {@link AnnotationModel}
     * @return a new aggregated {@link AnnotationModel}
     */
    public static AnnotationModel of(AnnotationModel... models) {
        Map<String, Collection<Annotation>> annotations = new LinkedHashMap<>();
        for (AnnotationModel annotationModel : models) {
            if (annotationModel != null) {
                for (Entry<String, Collection<Annotation>> annotation:  annotationModel.getAnnotations().entrySet()) {
                    Collection<Annotation> elements = annotations.computeIfAbsent(annotation.getKey(), unused -> new ArrayList<>());
                    elements.addAll(annotation.getValue());
                }
            }
        }
        return new AnnotationModel(annotations);
    }
}
