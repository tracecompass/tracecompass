/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations;

import java.util.List;

/**
 * Data provider annotation categories model
 */
public class AnnotationCategoriesModel {

    private final List<String> fAnnotationCategories;

    /**
     * Constructor
     *
     * @param annotationCategories
     *            List of categories
     */
    public AnnotationCategoriesModel(List<String> annotationCategories) {
        fAnnotationCategories = annotationCategories;
    }

    /**
     * Annotation categories for the model
     *
     * @return List of categories
     */
    public List<String> getAnnotationCategories() {
        return fAnnotationCategories;
    }
}
