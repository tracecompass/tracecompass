/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfCpuSelectedSignal;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage.CpuUsageView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.swtchart.Chart;

/**
 * SWTBot tests for Resources view
 *
 * @author Matthew Khouzam
 */
public class ResourcesAndCpuViewTest extends KernelTestBase {

    private SWTBotView fViewBotRv;
    private SWTBotView fViewBotCpu;

    /**
     * Before Test
     */
    @Override
    @Before
    public void before() {
        fViewBotRv = fBot.viewByPartName("Resources");
        SWTBotUtils.openView(CpuUsageView.ID);
        fViewBotCpu = fBot.viewById(CpuUsageView.ID);
        fViewBotCpu.show();
        fViewBotRv.show();
        try {
            SWTBotUtils.openTrace(TRACE_PROJECT_NAME, Paths.get(FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL()).toURI()).toString(), KERNEL_TRACE_TYPE);
        } catch (IOException | URISyntaxException e) {
            fail(e.getMessage());
        }
        SWTBotUtils.activateEditor(fBot, "bug446190");
        fViewBotRv.setFocus();
    }

    /**
     * Simple test to check the CPU Usage view after getting signals.
     */
    @Test
    public void testSignals() {
        Widget widget = fViewBotRv.getWidget();
        assertNotNull(widget);
        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(activeTrace);

        // clear everything
        TmfCpuSelectedSignal signal = new TmfCpuSelectedSignal(widget, -1, activeTrace);
        broadcast(signal);
        assertEquals("Before signal - CPU Usage Title", "CPU Usage", getTitle());
        assertEquals("Before signal - Thread Table", 12, getTableCount());
        fViewBotRv.setFocus();

        // select cpu 1
        signal = new TmfCpuSelectedSignal(widget, 1, activeTrace);
        broadcast(signal);
        assertEquals("After signal - CPU Usage Title", "CPU Usage 1", getTitle());
        assertEquals("After signal - Thread Table", 4, getTableCount());

        // select cpu 3 and 1
        signal = new TmfCpuSelectedSignal(widget, 3, activeTrace);
        broadcast(signal);
        assertEquals("After signal 2 - CPU Usage Title", "CPU Usage 1, 3", getTitle());
        assertEquals("After signal 2 - Thread Table", 8, getTableCount());

        // reset
        signal = new TmfCpuSelectedSignal(widget, -1, activeTrace);
        broadcast(signal);
        assertEquals("After signal clear - CPU Usage Title", "CPU Usage", getTitle());
        assertEquals("After signal clear - Thread Table", 12, getTableCount());
    }

    private static void broadcast(TmfCpuSelectedSignal signal) {
        UIThreadRunnable.syncExec(() -> TmfSignalManager.dispatchSignal(signal));
        WaitUtils.waitForJobs();
    }

    private String getTitle() {
        fViewBotCpu.setFocus();
        // Do some basic validation
        Matcher<Chart> matcher = WidgetOfType.widgetOfType(Chart.class);
        Chart chart = fViewBotCpu.bot().widget(matcher);
        return chart.getTitle().getText();
    }

    private int getTableCount() {
        fViewBotCpu.setFocus();
        // Do some basic validation
        Matcher<Tree> matcher = WidgetOfType.widgetOfType(Tree.class);
        SWTBotTree treeBot = new SWTBotTree(fViewBotCpu.bot().widget(matcher));
        int count = 0;
        for (SWTBotTreeItem bot : treeBot.getAllItems()) {
            final String text = bot.getText();
            if (!text.isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
