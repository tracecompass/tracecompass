package org.eclipse.linuxtools.internal.lttng2.kernel.ui.viewers.events;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.kernel.ui.viewers.events.messages"; //$NON-NLS-1$
	public static String EventsTable_channelColumn;
	public static String EventsTable_timestampColumn;
	public static String EventsTable_typeColumn;
    public static String EventsTable_contentColumn;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
