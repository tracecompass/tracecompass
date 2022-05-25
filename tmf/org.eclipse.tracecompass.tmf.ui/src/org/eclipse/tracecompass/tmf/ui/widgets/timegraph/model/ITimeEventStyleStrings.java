/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.SymbolType;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;

import com.google.common.collect.ImmutableMap;

/**
 * <p>
 * <em>Time event styles</em>, this is for reference purposes. Many values will
 * be unsupported.
 * </p>
 * <p>
 * Special care is needed when populating the map as it is untyped.
 * </p>
 *
 * @author Matthew Khouzam
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.0
 */
@NonNullByDefault
public interface ITimeEventStyleStrings {

    /**
     * Mapping of symbol types.
     *
     * @since 5.2
     * @deprecated DO NOT USE, it is effectively a 1-1 mapping. Use {@link SymbolType} directly.
     */
    @Deprecated
    Map<String, String> SYMBOL_TYPES = ImmutableMap.<String, String>builder()
            .put(StyleProperties.SymbolType.DIAMOND, SymbolType.DIAMOND)
            .put(StyleProperties.SymbolType.CIRCLE, SymbolType.CIRCLE)
            .put(StyleProperties.SymbolType.SQUARE, SymbolType.SQUARE)
            .put(StyleProperties.SymbolType.TRIANGLE, SymbolType.TRIANGLE)
            .put(StyleProperties.SymbolType.INVERTED_TRIANGLE, SymbolType.INVERTED_TRIANGLE)
            .put(StyleProperties.SymbolType.CROSS, SymbolType.CROSS)
            .put(StyleProperties.SymbolType.PLUS, SymbolType.PLUS)
            .build();

    /**
     * Item property. Possible values are
     * {@link ITimeEventStyleStrings#stateType()} or
     * {@link ITimeEventStyleStrings#linkType()}
     *
     * @return The key to get the item property of a state item
     * @since 4.0
     */
    static String itemTypeProperty() {
        return ".type"; //$NON-NLS-1$
    }

    /**
     * Indicate that the item type is a STATE
     *
     * @return The state item type value
     * @since 4.0
     */
    static String stateType() {
        return ".type.state"; //$NON-NLS-1$
    }

    /**
     * Indicate that the item type is a LINK
     *
     * @return The link item type value
     * @since 4.0
     */
    static String linkType() {
        return ".type.link"; //$NON-NLS-1$
    }

    /**
     * The event is annotated. When this is set, the label will not be drawn and
     * {@link ITimeGraphPresentationProvider#postDrawEvent} will not be called
     *
     * @return the key to get the annotated value
     * @since 4.0
     */
    static String annotated() {
        return ".annotated"; //$NON-NLS-1$
    }
}
