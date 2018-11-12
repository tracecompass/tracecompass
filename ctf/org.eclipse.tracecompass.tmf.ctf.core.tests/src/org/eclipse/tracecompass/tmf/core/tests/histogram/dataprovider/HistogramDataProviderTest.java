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
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.model.SeriesModel;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
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
    private static final long @NonNull [] EXPECTED_COMMON_XDATA = new long[] {
            1376592664828559410l, 1376592664865168602l, 1376592664901777794l, 1376592664938386985l, 1376592664974996177l, 1376592665011605369l, 1376592665048214561l, 1376592665084823753l, 1376592665121432945l, 1376592665158042136l,
            1376592665194651328l, 1376592665231260520l, 1376592665267869712l, 1376592665304478904l, 1376592665341088095l, 1376592665377697287l, 1376592665414306479l, 1376592665450915671l, 1376592665487524863l, 1376592665524134055l,
            1376592665560743246l, 1376592665597352438l, 1376592665633961630l, 1376592665670570822l, 1376592665707180014l, 1376592665743789205l, 1376592665780398397l, 1376592665817007589l, 1376592665853616781l, 1376592665890225973l,
            1376592665926835165l, 1376592665963444356l, 1376592666000053548l, 1376592666036662740l, 1376592666073271932l, 1376592666109881124l, 1376592666146490315l, 1376592666183099507l, 1376592666219708699l, 1376592666256317891l,
            1376592666292927083l, 1376592666329536275l, 1376592666366145466l, 1376592666402754658l, 1376592666439363850l, 1376592666475973042l, 1376592666512582234l, 1376592666549191425l, 1376592666585800617l, 1376592666622409809l,
            1376592666659019001l, 1376592666695628193l, 1376592666732237385l, 1376592666768846576l, 1376592666805455768l, 1376592666842064960l, 1376592666878674152l, 1376592666915283344l, 1376592666951892535l, 1376592666988501727l,
            1376592667025110919l, 1376592667061720111l, 1376592667098329303l, 1376592667134938495l, 1376592667171547686l, 1376592667208156878l, 1376592667244766070l, 1376592667281375262l, 1376592667317984454l, 1376592667354593645l,
            1376592667391202837l, 1376592667427812029l, 1376592667464421221l, 1376592667501030413l, 1376592667537639605l, 1376592667574248796l, 1376592667610857988l, 1376592667647467180l, 1376592667684076372l, 1376592667720685564l,
            1376592667757294755l, 1376592667793903947l, 1376592667830513139l, 1376592667867122331l, 1376592667903731523l, 1376592667940340715l, 1376592667976949906l, 1376592668013559098l, 1376592668050168290l, 1376592668086777482l,
            1376592668123386674l, 1376592668159995865l, 1376592668196605057l, 1376592668233214249l, 1376592668269823441l, 1376592668306432633l, 1376592668343041825l, 1376592668379651016l, 1376592668416260208l, 1376592668452869400l };
    private static final Map<String, ISeriesModel> EXPECTED_YDATA = ImmutableMap.of("hello-lost/Total",
            new SeriesModel(1, "hello-lost/Total", EXPECTED_COMMON_XDATA,
                    new double[] { 1.0, 1101.0, 342.0, 1520.0, 7182.0, 6802.0, 3002.0, 3616.0, 8734.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }),
            "hello-lost/Lost",
            new SeriesModel(2, "hello-lost/Lost", EXPECTED_COMMON_XDATA,
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
        module.setName("Statistics");
        assertTrue("Statistics Analysis should apply to this trace", module.setTrace(trace));
        assertEquals("Statistics Analysis shouls be schedulable", Status.OK_STATUS, module.schedule());
        assertTrue("Statistics Analysis should run successfully", module.waitForCompletion());
        try {
            HistogramDataProvider provider = new HistogramDataProvider(trace, module);
            TmfModelResponse<@NonNull TmfTreeModel<@NonNull TmfTreeDataModel>> treeResponse = provider.fetchTree(FetchParametersUtils.timeQueryToMap(new TimeQueryFilter(START, END, 2)), null);
            assertEquals("Response Status should be COMPLETED, as we waited for the analysis to complete",
                    ITmfResponse.Status.COMPLETED, treeResponse.getStatus());
            TmfTreeModel<@NonNull TmfTreeDataModel> treeModel = treeResponse.getModel();
            assertNotNull(treeModel);
            assertEquals(EXPECTED_FULL_PATHS, getFullPaths(treeModel.getEntries()));

            List<Long> ids = Lists.transform(treeModel.getEntries(), TmfTreeDataModel::getId);
            SelectionTimeQueryFilter selectionFilter = new SelectionTimeQueryFilter(START, END, 100, ids);
            TmfModelResponse<@NonNull ITmfXyModel> xyResponse = provider.fetchXY(FetchParametersUtils.selectionTimeQueryToMap(selectionFilter), null);
            assertEquals("Response Status should be COMPLETED, as we waited for the analysis to complete",
                    ITmfResponse.Status.COMPLETED, xyResponse.getStatus());
            ITmfXyModel xyModel = xyResponse.getModel();
            assertNotNull(xyModel);
            assertEquals(EXPECTED_YDATA, xyModel.getData());
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
