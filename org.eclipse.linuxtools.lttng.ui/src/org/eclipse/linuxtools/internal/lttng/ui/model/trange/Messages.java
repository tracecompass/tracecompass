package org.eclipse.linuxtools.internal.lttng.ui.model.trange;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng.ui.model.trange.messages"; //$NON-NLS-1$
    public static String TimeRangeViewerProvider_BadRangeExtraInfo;
    public static String TimeRangeViewerProvider_EndTime;
    public static String TimeRangeViewerProvider_ProcessType;
    public static String TimeRangeViewerProvider_StartTime;
    public static String TimeRangeViewerProvider_UndefinedEndTime;
    public static String TimeRangeViewerProvider_UndefinedStartTime;
    static {
	// initialize resource bundle
	NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
