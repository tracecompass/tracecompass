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

package org.eclipse.tracecompass.tmf.core.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IElementResolver;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IPropertyCollection;

/**
 * Interface for an output element. An output element represents a graphical
 * element. It can have an explicit style, or a value that can be used to
 * determine a style. It can have metadata and properties that can be used to
 * store the input and output of element-based filtering.
 *
 * @author Patrick Tasse
 * @since 5.2
 */
public interface IOutputElement extends IElementResolver, IPropertyCollection {

    /**
     * Get the value, may be unused if the element has a style
     *
     * @return Value
     */
    default int getValue() {
        return Integer.MIN_VALUE;
    }

    /**
     * Get the style associated with this element
     *
     * @return {@link OutputElementStyle} describing the style of this element
     * @since 5.2
     */
    default @Nullable OutputElementStyle getStyle() {
        return null;
    }
}
