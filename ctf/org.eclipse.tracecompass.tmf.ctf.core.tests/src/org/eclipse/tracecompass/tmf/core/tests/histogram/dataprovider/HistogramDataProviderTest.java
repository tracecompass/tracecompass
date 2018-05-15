/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.histogram.dataprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.histogram.HistogramDataProvider;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Test the {@link HistogramDataProvider}.
 *
 * @author Loic Prieur-Drevon
 */
public class HistogramDataProviderTest {

    private static final long START = 1376592664828559410L;
    private static final long END = 1376592668452869400L;

    private static final List<String> EXPECTED_FULL_PATHS = ImmutableList.of("hello-lost", "hello-lost/Total", "hello-lost/Lost");
    private static final Map<String, IYModel> EXPECTED_YDATA = ImmutableMap.of("hello-lost/Total",
            new YModel(1, "hello-lost/Total",
                    new double[] { 1.0, 1101.0, 342.0, 1520.0, 7182.0, 6802.0, 3002.0, 3616.0, 8734.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }),
            "hello-lost/Lost",
            new YModel(2, "hello-lost/Lost",
                    new double[] { 859.0, 91775.0, 152692.53033367038, 163867.2477369654, 144965.73090892372, 161976.6598828061, 168719.01789009458, 139270.93218317698, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892,
                            1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892,
                            1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892,
                            1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892,
                            1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892,
                            1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892,
                            1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892,
                            1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892,
                            1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 1.4303668261140892, 0.0, 0.0 }));

    /**
     * Test the {@link HistogramDataProvider} with the
     * {@link CtfTestTrace#HELLO_LOST} trace. Ensure that the expected tree and xy
     * models are returned
     *
     * @throws TmfAnalysisException
     *             if the trace is set more that once
     */
    @Test
    public void testHelloLost() throws TmfAnalysisException {
        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.HELLO_LOST);
        TmfStatisticsModule module = new TmfStatisticsModule();
        assertTrue("Statistics Analysis should apply to this trace", module.setTrace(trace));
        assertEquals("Statistics Analysis shouls be schedulable", Status.OK_STATUS, module.schedule());
        assertTrue("Statistics Analysis should run successfully", module.waitForCompletion());
        try {
            HistogramDataProvider provider = new HistogramDataProvider(trace, module);
            TmfModelResponse<@NonNull List<@NonNull TmfTreeDataModel>> treeResponse = provider.fetchTree(new TimeQueryFilter(START, END, 2), null);
            assertEquals("Response Status should be COMPLETED, as we waited for the analysis to complete",
                    ITmfResponse.Status.COMPLETED, treeResponse.getStatus());
            List<@NonNull TmfTreeDataModel> treeModel = treeResponse.getModel();
            assertNotNull(treeModel);
            assertEquals(EXPECTED_FULL_PATHS, getFullPaths(treeModel));

            List<Long> ids = Lists.transform(treeModel, TmfTreeDataModel::getId);
            SelectionTimeQueryFilter selectionFilter = new SelectionTimeQueryFilter(START, END, 100, ids);
            TmfModelResponse<@NonNull ITmfXyModel> xyResponse = provider.fetchXY(selectionFilter, null);
            assertEquals("Response Status should be COMPLETED, as we waited for the analysis to complete",
                    ITmfResponse.Status.COMPLETED, xyResponse.getStatus());
            ITmfXyModel xyModel = xyResponse.getModel();
            assertTrue(xyModel instanceof ITmfCommonXAxisModel);
            ITmfCommonXAxisModel commonXModel = (ITmfCommonXAxisModel) xyModel;
            assertEquals(EXPECTED_YDATA, commonXModel.getYData());
        } finally {
            module.dispose();
            CtfTmfTestTraceUtils.dispose(CtfTestTrace.HELLO_LOST);
        }
    }

    private static List<String> getFullPaths(List<@NonNull TmfTreeDataModel> treeModel) {
        Map<Long, TmfTreeDataModel> map = Maps.uniqueIndex(treeModel, TmfTreeDataModel::getId);
        return Lists.transform(treeModel, m -> getFullPath(map, m));
    }

    private static String getFullPath(Map<Long, TmfTreeDataModel> map, TmfTreeDataModel model) {
        StringBuilder builder = new StringBuilder(model.getName());
        TmfTreeDataModel parent = map.get(model.getParentId());
        while (parent != null) {
            builder.insert(0, parent.getName() + '/');
            parent = map.get(parent.getParentId());
        }
        return builder.toString();
    }

}
