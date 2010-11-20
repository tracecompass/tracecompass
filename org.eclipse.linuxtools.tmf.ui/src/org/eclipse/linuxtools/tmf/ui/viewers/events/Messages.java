package org.eclipse.linuxtools.tmf.ui.viewers.events;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.viewers.events.messages"; //$NON-NLS-1$
    public static String TmfEventsTable_ContentColumnHeader;
    public static String TmfEventsTable_ReferenceColumnHeader;
    public static String TmfEventsTable_SourceColumnHeader;
    public static String TmfEventsTable_TimestampColumnHeader;
    public static String TmfEventsTable_TypeColumnHeader;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
