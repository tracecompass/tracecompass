/*******************************************************************************
 * Copyright (c) 2017, 2018 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simon Delisle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.model.TmfImportHelper;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.SelectTracesOperation;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimePreferences;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.tests.shared.ProjectModelTestData;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link SelectTracesOperation} class
 */
public class SelectTracesOperationTest {

    private TmfExperimentElement fExperiment;
    @SuppressWarnings("null") // Suppress warning for NonNull annotation problem
    private @NonNull TmfTraceElement[] fTraces = new TmfTraceElement[6];
    private TmfTraceElement[] fExpectedTimeRangeTraces = new TmfTraceElement[4];
    private final String EXPERIMENT_NAME = "test_select_trace";
    private static TmfProjectElement fProjectElement;

    private ITmfTimestamp fStartTimeRange;
    private ITmfTimestamp fEndTimeRange;

    /**
     * Setup before test
     *
     * @throws ParseException
     *             Exception if unable to parse TimeStamps
     * @throws CoreException
     *             Core Exception
     */
    @Before
    public void setUp() throws ParseException, CoreException {
        IProject project = TmfProjectRegistry.createProject("Experiment Select Trace Test Project", null, null);
        fProjectElement = TmfProjectRegistry.getProject(project, true);

        TmfTraceFolder tmfTracesFolder = fProjectElement.getTracesFolder();
        assertNotNull(tmfTracesFolder);

        IFolder traceFolder = tmfTracesFolder.getResource();

        List<IPath> tracePaths = new ArrayList<>();
        tracePaths.add(new Path(TmfTestTrace.SYSLOG_1.getFullPath()));
        tracePaths.add(new Path(TmfTestTrace.SYSLOG_2.getFullPath()));
        tracePaths.add(new Path(TmfTestTrace.SYSLOG_3.getFullPath()));
        tracePaths.add(new Path(TmfTestTrace.SYSLOG_4.getFullPath()));
        tracePaths.add(new Path(TmfTestTrace.SYSLOG_5.getFullPath()));
        tracePaths.add(new Path(TmfTestTrace.SYSLOG_6.getFullPath()));

        for (IPath tracePath : tracePaths) {
            IResource traceResource = TmfImportHelper.createLink(traceFolder, tracePath, tracePath.lastSegment());
            traceResource.setPersistentProperty(TmfCommonConstants.TRACETYPE, "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog");
        }

        for (TmfTraceElement trace : tmfTracesFolder.getTraces()) {
            trace.refreshTraceType();
        }

        fProjectElement.refresh();
        for (int i = 0; i < fTraces.length; i++) {
            fTraces[i] = Objects.requireNonNull(tmfTracesFolder.getTraces().get(i));
        }

        for (int i = 0; i < fExpectedTimeRangeTraces.length; i++) {
            fExpectedTimeRangeTraces[i] = fTraces[i];
        }

        fStartTimeRange = parse("Jan 1 02:00:00");
        fEndTimeRange = parse("Jan 1 05:05:00");

        fExperiment = ProjectModelTestData.addExperiment(fProjectElement, EXPERIMENT_NAME);
    }

    /**
     * Parse the time stamps with the same approach as the syslog trace stub
     *
     * @param time
     *            timestamp string
     * @return the parsed {@link ITmfTimestamp}
     * @throws ParseException
     *             if the timestamp string does not match the expected format.
     */
    private static ITmfTimestamp parse(String time) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MMM dd HH:mm:ss", TmfTimePreferences.getLocale());
        dateFormat.setTimeZone(TmfTimestampFormat.getDefaulTimeFormat().getTimeZone());
        Date date = dateFormat.parse(time);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        if (calendar.after(Calendar.getInstance())) {
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 1);
        }
        return TmfTimestamp.fromMillis(calendar.getTimeInMillis());
    }

    /**
     * Delete the experiment after each tests.
     */
    @After
    public void cleanUp() {
        TmfExperimentFolder experimentsFolder = fProjectElement.getExperimentsFolder();
        if (experimentsFolder != null) {
            IResource experimentResource = fExperiment.getResource();
            IPath path = experimentResource.getLocation();
            if (path != null) {
                fExperiment.deleteSupplementaryFolder();
            }

            try {
                experimentResource.delete(true, null);
            } catch (CoreException e) {
                Activator.getDefault().logError("Unable to delete experiment: " + fExperiment.getName(), e);
            }
        }
    }

    /**
     * Test the operation by importing the given traces
     *
     * @throws Exception
     *             if an exception occurs
     */
    @Test
    public void testBasicOperation() throws Exception {
        assertNotNull(fExperiment);
        assertNotNull(fTraces);
        SelectTracesOperation selectTracesOperation = new SelectTracesOperation(Objects.requireNonNull(fExperiment), fTraces, new HashMap<String, TmfTraceElement>());
        PlatformUI.getWorkbench().getProgressService().run(true, true, selectTracesOperation);
        assertExperimentTraces(fTraces, fExperiment.getTraces());
    }

    /**
     * Test the operation by using the time range filtering mechanism
     *
     * @throws Exception
     *             if an exception occurs
     */
    @Test
    public void testOperationTimeRange() throws Exception {
        assertNotNull(fExperiment);
        assertNotNull(fTraces);
        SelectTracesOperation selectTracesOperation = new SelectTracesOperation(Objects.requireNonNull(fExperiment), fTraces, new HashMap<String, TmfTraceElement>(), fStartTimeRange, fEndTimeRange);
        PlatformUI.getWorkbench().getProgressService().run(true, true, selectTracesOperation);
        assertExperimentTraces(fExpectedTimeRangeTraces, fExperiment.getTraces());
    }

    /**
     * Check if the actual traces that are in the experiment are the same as the
     * expected one
     *
     * @param expectedTraces
     *            Expected traces
     * @param actualTraces
     *            Actual traces in the experiment
     */
    private static void assertExperimentTraces(TmfTraceElement[] expectedTraces, List<TmfTraceElement> actualTraces) {
        assertEquals(expectedTraces.length, actualTraces.size());
        for (int i = 0; i < expectedTraces.length; i++) {
            assertEquals(expectedTraces[i], actualTraces.get(i).getElementUnderTraceFolder());
        }
    }
}
