/**********************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Output element style object for one style key. This class supports style
 * inheritance. To avoid creating new styles the element style can have a parent
 * style and will have all the same style properties values as the parent and
 * can add or override style properties.
 *
 * @author Simon Delisle
 * @since 5.2
 */
public class OutputElementStyle {

    private @Nullable String fParentStyleKey;
    private Map<String, Object> fValues;

    /**
     * Constructor. Use this constructor if the goal is to use an existing style
     * without overriding any properties.
     *
     * @param styleKey
     *            Style key. It can be a comma-separated list of parent keys for
     *            multiple inheritance.
     */
    public OutputElementStyle(String styleKey) {
        fParentStyleKey = styleKey;
        fValues = Collections.emptyMap();
    }

    /**
     * Constructor. Use this constructor to specify style values.
     *
     * @param parentStyleKey
     *            Parent style key or <code>null</code> if there is no parent.
     *            The parent key is a style key of comma-separated list of style
     *            key that should each match another existing style key. It is
     *            used for style inheritance.
     * @param values
     *            Style values or empty map if there is no values. Use to define
     *            different style properties. Properties to use can be found in
     *            {@link StyleProperties}.
     */
    public OutputElementStyle(@Nullable String parentStyleKey, Map<String, Object> values) {
        fParentStyleKey = parentStyleKey;
        fValues = values;
    }

    /**
     * Get the parent style key. It can be a comma-separated list of parent
     * style keys, where the right-most style has precedence over the left ones.
     * For instance, if styles 'A' and 'B' are defined, then parent style 'A,B'
     * means properties will be searched for first in B, then in A.
     *
     * @return Parent key or <code>null</code> if there is no parent
     */
    public @Nullable String getParentKey() {
        return fParentStyleKey;
    }

    /**
     * Get the style values. The map keys and values are defined in
     * {@link StyleProperties}.
     *
     * @return Map of values or empty if there is no given style
     */
    public Map<String, Object> getStyleValues() {
        return fValues;
    }

    @Override
    public String toString() {
        return String.format("Style [%s, %s]", fParentStyleKey, fValues); //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return Objects.hash(fParentStyleKey, fValues);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OutputElementStyle other = (OutputElementStyle) obj;
        return Objects.equals(fParentStyleKey, other.fParentStyleKey) && Objects.equals(fValues, other.fValues);
    }
}
