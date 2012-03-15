package org.eclipse.linuxtools.internal.lttng.ui.views.timeframe;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng.ui.views.timeframe.messages"; //$NON-NLS-1$
    public static String TimeFrameView_CurrentTime;
    public static String TimeFrameView_WindowEndTime;
    public static String TimeFrameView_WindowRange;
    public static String TimeFrameView_WindowStartTime;
    static {
	// initialize resource bundle
	NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
