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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

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

    /**
     * Creates a new aggregated {@link AnnotationCategoriesModel} from an array of
     * {@link AnnotationCategoriesModel}.
     *
     * @param models
     *            array of {@link AnnotationCategoriesModel}
     * @return a new aggregated {@link AnnotationCategoriesModel}
     */
    public static AnnotationCategoriesModel of(AnnotationCategoriesModel... models) {
        Set<String> categories = new HashSet<>();
        for (AnnotationCategoriesModel categoryModel : models) {
            if (categoryModel != null) {
                categories.addAll(categoryModel.getAnnotationCategories());
            }
        }
        return new AnnotationCategoriesModel(ImmutableList.copyOf(categories));
    }
}
