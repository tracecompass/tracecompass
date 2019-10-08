/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Model of an output element
 *
 * @author Patrick Tasse
 * @since 5.2
 */
public abstract class OutputElement implements IOutputElement {

    private @Nullable Multimap<String, Object> fMetadata = null;
    private int fActiveProperties = 0;
    private final @Nullable OutputElementStyle fStyle;

    /**
     * Constructor
     *
     * @param style
     *            Style
     */
    public OutputElement(@Nullable OutputElementStyle style) {
        fStyle = style;
    }

    @Override
    public synchronized Multimap<String, Object> getMetadata() {
        Multimap<String, Object> metadata = fMetadata;
        if (metadata == null) {
            metadata = HashMultimap.create();
            fMetadata = metadata;
        }
        return metadata;
    }

    @Override
    public int getActiveProperties() {
        return fActiveProperties;
    }

    @Override
    public void setActiveProperties(int activeProperties) {
        fActiveProperties = activeProperties;
    }

    @Override
    public @Nullable OutputElementStyle getStyle() {
        return fStyle;
    }
}
