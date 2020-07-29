/**********************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.tests.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.junit.Test;

/**
 * Test the {@link DataProviderDescriptor} class
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class DataProviderDescriptorTest {

    private static final String DESCRIPTION = "Description";
    private static final String ID = "my.data.provider.id";
    private static final String NAME = "Data Provider Name";
    private static final ProviderType TYPE = ProviderType.TIME_GRAPH;

    /**
     * Test the equality methods
     */
    @Test
    public void testEquality() {
        DataProviderDescriptor.Builder builder = new DataProviderDescriptor.Builder();
        builder.setDescription(DESCRIPTION).setId(ID).setName(NAME).setProviderType(TYPE);

        IDataProviderDescriptor baseDescriptor = builder.build();

        // Make sure it is equal to itself
        assertTrue(baseDescriptor.equals(builder.build()));

        // Use an identical ID
        String sameId = "my.data.provider.id";
        builder.setDescription(DESCRIPTION).setId(sameId).setName(NAME).setProviderType(TYPE);
        assertTrue(baseDescriptor.equals(builder.build()));

        // Change each of the variable and make sure result is not equal
        builder.setDescription("Other description").setId(ID).setName(NAME).setProviderType(TYPE);
        assertFalse(baseDescriptor.equals(builder.build()));
        builder.setDescription(DESCRIPTION).setId("other id").setName(NAME).setProviderType(TYPE);
        assertFalse(baseDescriptor.equals(builder.build()));
        builder.setDescription(DESCRIPTION).setId(ID).setName("other name").setProviderType(TYPE);
        assertFalse(baseDescriptor.equals(builder.build()));
        builder.setDescription(DESCRIPTION).setId(ID).setName(NAME).setProviderType(ProviderType.TABLE);
        assertFalse(baseDescriptor.equals(builder.build()));
    }

}
