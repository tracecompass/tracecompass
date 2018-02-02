/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;

/**
 * Data Provider description, used to list the available providers for a trace
 * without triggering the analysis or creating the providers. Supplies
 * information such as the extension point ID, type of provider and help text.
 *
 * @author Loic Prieur-Drevon
 * @author Bernd Hufmann
 * @since 4.3
 */
public class DataProviderDescriptor implements IDataProviderDescriptor {

    private final String fId;
    private final String fName;
    private final String fDescription;
    private final ProviderType fType;

    /**
     * Constructor
     *
     * @param bulider
     *            the builder object to create the descriptor
     */
    private DataProviderDescriptor(Builder builder) {
        fId = builder.fId;
        fName = builder.fName;
        fDescription = builder.fDescription;
        fType = Objects.requireNonNull(builder.fType);
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public String getId() {
        return fId;
    }

    @Override
    public ProviderType getType() {
        return fType;
    }

    @Override
    public String getDescription() {
        return fDescription;
    }

    /**
     * A builder class to build instances implementing interface {@link IDataProviderDescriptor}
     */
    public static class Builder {
        private String fId = ""; //$NON-NLS-1$
        private String fName = ""; //$NON-NLS-1$
        private String fDescription = ""; //$NON-NLS-1$
        private @Nullable ProviderType fType = null;

        /**
         * Constructor
         */
        public Builder() {
            // Empty constructor
        }

        /**
         * Sets the data provider ID
         *
         * @param id
         *            the ID of the data provider
         * @return the builder instance.
         */
        public Builder setId(String id) {
            fId = id;
            return this;
        }

        /**
         * Sets the name of the data provider
         *
         * @param name
         *            the name to set
         * @return the builder instance.
         */
        public Builder setName(String name) {
            fName = name;
            return this;
        }

        /**
         * Sets the description of the data provider
         *
         * @param description
         *            the description text to set
         * @return the builder instance.
         */
        public Builder setDescription(String description) {
            fDescription = description;
            return this;
        }

        /**
         * Sets the data provider type
         *
         * @param type
         *            the data provider type to set
         * @return the builder instance.
         */
        public Builder setProviderType(ProviderType type) {
            fType = type;
            return this;
        }

        /**
         * The method to construct an instance of
         * {@link IDataProviderDescriptor}
         *
         * @return a {@link IDataProviderDescriptor} instance
         */
        public IDataProviderDescriptor build() {
            if (fType == null) {
                throw new IllegalStateException("Data provider type not set"); //$NON-NLS-1$
            }
            if (fId.isEmpty()) {
                throw new IllegalStateException("Empty data provider ID"); //$NON-NLS-1$
            }
            return new DataProviderDescriptor(this);
        }

    }
}
