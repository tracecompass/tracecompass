/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.inputoutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput.IoTestCase;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput.IoTestCase.DiskActivity;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput.IoTestFactory;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.DisksIODataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * A test for the IO data provider
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class InputOutputDataProviderTest extends AbstractTestInputOutput {

    private static final IProgressMonitor PROGRESS_MONITOR = new NullProgressMonitor();
    private final IoTestCase fTestCase;

    /**
     * Constructor
     *
     * @param test
     *            A test case parameter for this test
     */
    public InputOutputDataProviderTest(IoTestCase test) {
        super();
        fTestCase = test;
    }

    /**
     * Clean up
     */
    @After
    public void tearDown() {
        super.deleteTrace();
    }

    @Override
    protected @NonNull InputOutputAnalysisModule setUp(String fileName) {
        InputOutputAnalysisModule module = super.setUp(fileName);
        TmfTestHelper.executeAnalysis(module);
        return module;
    }

    private @NonNull DisksIODataProvider getProvider() {
        InputOutputAnalysisModule module = setUp(fTestCase.getTraceFileName());
        ITmfTrace trace = module.getTrace();
        assertNotNull(trace);
        DisksIODataProvider provider = DisksIODataProvider.create(trace);
        assertNotNull(provider);
        return provider;
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { IoTestFactory.SIMPLE_REQUESTS }
        });
    }

    /**
     * Test the data provider
     */
    @Test
    public void testDiskActivity() {
        DisksIODataProvider provider = getProvider();
        Collection<@NonNull DiskActivity> diskActivity = fTestCase.getDiskActivity();
        for (DiskActivity test : diskActivity) {
            Map<@NonNull String, @NonNull Object> parameters = test.getTimeQuery();
            TmfModelResponse<@NonNull TmfTreeModel<@NonNull TmfTreeDataModel>> response = provider.fetchTree(parameters, PROGRESS_MONITOR);
            assertEquals(ITmfResponse.Status.COMPLETED, response.getStatus());
            TmfTreeModel<@NonNull TmfTreeDataModel> model = response.getModel();
            assertNotNull(model);
            parameters = test.getTimeQueryForModel(model);
            TmfModelResponse<@NonNull ITmfXyModel> yResponse = provider.fetchXY(parameters, PROGRESS_MONITOR);
            assertEquals(ITmfResponse.Status.COMPLETED, yResponse.getStatus());
            ITmfXyModel yModel = yResponse.getModel();
            assertNotNull(yModel);
            Map<@NonNull String, @NonNull ISeriesModel> data = yModel.getData();
            assertEquals(1, data.size());
            ISeriesModel ySeries = data.values().iterator().next();
            double[] expected = test.getActivity();
            double[] actual = ySeries.getData();
            for (int i = 0; i < expected.length; i++) {
                assertTrue(String.format("No actual value at position %d for %s", i, test), actual.length > i);
                assertEquals(String.format("Value at position %d for %s", i, test), expected[i], actual[i], 0.001);
            }
            assertEquals(String.format("More values than expected for %s", test), expected.length, actual.length);
        }

    }

}
