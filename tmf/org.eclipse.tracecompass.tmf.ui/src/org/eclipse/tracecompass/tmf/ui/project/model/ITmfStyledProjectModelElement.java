/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.StyledString.Styler;

/**
 * This interface can be implemented by elements to a style to their text.
 *
 * @author Geneviève Bastien
 */
public interface ITmfStyledProjectModelElement {

    /**
     * Return the styler who will apply its style to the text string.
     *
     * @return The style object, or 'null' for no special style
     */
    @Nullable
    Styler getStyler();

}
