package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.lttng.ui.views.histogram.messages"; //$NON-NLS-1$
	public static String HistogramView_currentEventLabel;
	public static String HistogramView_windowCenterLabel;
	public static String HistogramView_windowSpanLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
