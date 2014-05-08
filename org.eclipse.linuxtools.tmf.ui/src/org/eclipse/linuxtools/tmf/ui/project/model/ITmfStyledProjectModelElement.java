/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.StyledString.Styler;

/**
 * This interface can be implemented by elements to a style to their text.
 *
 * @author Geneviève Bastien
 * @since 3.0
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
