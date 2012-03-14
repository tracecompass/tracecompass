package org.eclipse.linuxtools.internal.lttng.ui.views.statistics;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng.ui.views.statistics.messages"; //$NON-NLS-1$
    public static String StatisticsView_CPUTimeColumn;
    public static String StatisticsView_CPUTimeTip;
    public static String StatisticsView_CumCPUTimeColumn;
    public static String StatisticsView_CumCPUTimeTip;
    public static String StatisticsView_ElapsedTimeColumn;
    public static String StatisticsView_ElapsedTimeTip;
    public static String StatisticsView_LevelColumn;
    public static String StatisticsView_LevelColumnTip;
    public static String StatisticsView_NbEventsColumn;
    public static String StatisticsView_NbEventsTip;
    static {
	// initialize resource bundle
	NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
