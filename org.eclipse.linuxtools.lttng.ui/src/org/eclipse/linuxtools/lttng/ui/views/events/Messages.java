package org.eclipse.linuxtools.lttng.ui.views.events;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.lttng.ui.views.events.messages"; //$NON-NLS-1$
	public static String EventsTable_contentColumn;
	public static String EventsTable_referenceColumn;
	public static String EventsTable_sourceColumn;
	public static String EventsTable_timestampColumn;
	public static String EventsTable_typeColumn;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
