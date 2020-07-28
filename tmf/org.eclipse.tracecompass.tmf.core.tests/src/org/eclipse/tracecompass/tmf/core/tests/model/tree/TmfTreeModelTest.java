/**********************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.tests.model.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TableColumnDescriptor;
import org.eclipse.tracecompass.tmf.core.model.ITableColumnDescriptor;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.tests.stubs.model.tree.TmfTreeDataModelStub;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the Tree Model
 */
@NonNullByDefault
public class TmfTreeModelTest {

    private static final int NB_COLUMNS = 4;
    private static final int NB_ROWS = 5;
    private static final long PARENT_ID = 100;
    private static final String HEADER_PREFIX = "header";
    private static final String TOOLTIP_PREFIX = "tooltip";
    private static final String LABEL_PREFIX = "label";
    private static final String TEST_SCOPE = "scope";
    private static List<@NonNull ITmfTreeDataModel> fTestEntries = new ArrayList<>();
    private static List<@NonNull String> fTestHeaders = new ArrayList<>();
    private static List<@NonNull String> fExpectedEmptyTooltips = new ArrayList<>();
    private static List<@NonNull String> fExpectedTooltips = new ArrayList<>();
    private static List<@NonNull ITableColumnDescriptor> fTestDescriptors = new ArrayList<>();

    /**
     * Run once
     */
    @BeforeClass
    public static void initClass() {
        for (int i = 0; i < NB_COLUMNS; i++) {
            String headerText = HEADER_PREFIX + String.valueOf(i);
            String headerTooltip = TOOLTIP_PREFIX + String.valueOf(i);
            fTestHeaders.add(headerText);
            fExpectedEmptyTooltips.add("");
            fExpectedTooltips.add(headerTooltip);

            TableColumnDescriptor.Builder descBuilder = new TableColumnDescriptor.Builder();
            descBuilder.setText(headerText);
            descBuilder.setTooltip(headerTooltip);
            fTestDescriptors.add(descBuilder.build());
        }

        for (int k = 0; k < NB_ROWS; k++) {
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < NB_COLUMNS; i++) {
                labels.add(LABEL_PREFIX + String.valueOf(i));
            }
            ITmfTreeDataModel dataModel = new TmfTreeDataModelStub(k, PARENT_ID, labels);
            fTestEntries.add(dataModel);
        }
    }

    /**
     * Tests constructors {@link TmfTreeModel#TmfTreeModel(List, List)}
     * {@link TmfTreeModel#TmfTreeModel(List, List, String)}
     */
    @Test
    public void testTmfTreeModelConstructor() {
        TmfTreeModel<@NonNull ITmfTreeDataModel> testInstance = new TmfTreeModel<>(fTestHeaders, fTestEntries);
        verifyInstance(testInstance, fExpectedEmptyTooltips, null);

        testInstance = new TmfTreeModel<>(fTestHeaders, fTestEntries, TEST_SCOPE);
        verifyInstance(testInstance, fExpectedEmptyTooltips, TEST_SCOPE);
    }

    private static void verifyInstance(TmfTreeModel<@NonNull ITmfTreeDataModel> testInstance, List<String> tooltips, @Nullable String scope) {
        assertEquals("Incorrect list of entries", fTestEntries, testInstance.getEntries());
        assertEquals("Incorrect list of entries", fTestHeaders, testInstance.getHeaders());

        List<ITableColumnDescriptor> columnDescriptors = testInstance.getColumnDescriptors();

        assertEquals("Incorrect number of column descriptors", fTestHeaders.size(), columnDescriptors.size());
        assertEquals("Incorrect number of column descriptors", tooltips.size(), columnDescriptors.size());
        for (int i = 0; i < fTestHeaders.size(); i++) {
            assertEquals("Incorrect Collumn descriptor header text", fTestHeaders.get(i), columnDescriptors.get(i).getText());
            assertEquals("Incorrect Collumn descriptor header tooltip", tooltips.get(i), columnDescriptors.get(i).getTooltip());
        }
        assertEquals("Incorrect scope", scope, testInstance.getScope());
    }

    /**
     * Tests {@link TmfTreeModel.Builder}
     */
    @SuppressWarnings("javadoc")
    @Test
    public void testTmfTreeModelConstructor2() {
        TmfTreeModel.Builder<@NonNull ITmfTreeDataModel> builder = new TmfTreeModel.Builder<>();
        builder.setColumnDescriptors(fTestDescriptors).setEntries(fTestEntries);
        TmfTreeModel<@NonNull ITmfTreeDataModel> testInstance = builder.build();
        verifyInstance(testInstance, fExpectedTooltips, null);

        builder = new TmfTreeModel.Builder<>();
        builder.setColumnDescriptors(fTestDescriptors).setEntries(fTestEntries).setScope(TEST_SCOPE);

        testInstance = builder.build();
        verifyInstance(testInstance, fExpectedTooltips, TEST_SCOPE);

        builder = new TmfTreeModel.Builder<>();
        testInstance = builder.build();
        assertTrue("Entry list not empty", testInstance.getEntries().isEmpty());
        assertTrue("Column descriptor list not empty", testInstance.getColumnDescriptors().isEmpty());
    }
}
