/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;

/**
 * This interface represents an annotation provider. Data providers can
 * implement this interface to provide annotations.
 *
 * @author Simon Delisle
 */
public interface IOutputAnnotationProvider {

    /**
     * Fetch the annotation categories supported by this data provider.
     *
     * @param fetchParameters
     *            Map of parameters needed for this provider
     * @param monitor
     *            ProgressMonitor to cancel task
     * @return A {@link TmfModelResponse} instance with an
     *         {@link AnnotationCategoriesModel}
     */
    TmfModelResponse<AnnotationCategoriesModel> fetchAnnotationCategories(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor);

    /**
     * Fetch annotations for this data provider.
     *
     * @param fetchParameters
     *            Map of parameters needed for this provider
     * @param monitor
     *            ProgressMonitor to cancel task
     * @return A {@link TmfModelResponse} instance with an
     *         {@link AnnotationModel}
     */
    TmfModelResponse<AnnotationModel> fetchAnnotations(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor);
}
