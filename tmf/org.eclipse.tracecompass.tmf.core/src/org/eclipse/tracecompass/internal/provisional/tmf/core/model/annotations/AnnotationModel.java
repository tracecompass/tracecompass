/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations;

import java.util.Collection;
import java.util.Map;

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
}
