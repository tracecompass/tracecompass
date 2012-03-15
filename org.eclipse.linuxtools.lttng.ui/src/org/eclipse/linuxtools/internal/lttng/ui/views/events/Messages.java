package org.eclipse.linuxtools.internal.lttng.ui.views.events;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng.ui.views.events.messages"; //$NON-NLS-1$
	public static String EventsTable_traceColumn;
	public static String EventsTable_timestampColumn;
	public static String EventsTable_markerColumn;
    public static String EventsTable_contentColumn;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
