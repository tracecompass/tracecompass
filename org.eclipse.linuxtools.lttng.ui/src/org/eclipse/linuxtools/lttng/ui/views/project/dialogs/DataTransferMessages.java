package org.eclipse.linuxtools.lttng.ui.views.project.dialogs;

import org.eclipse.osgi.util.NLS;

public class DataTransferMessages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.lttng.ui.views.project.dialogs.messages"; //$NON-NLS-1$
    public static String ImportTraceWizard_LocationError;
    public static String ImportTraceWizard_LocationErrorMsg1;
    public static String ImportTraceWizard_LocationErrorMsg2;
    public static String ImportTraceWizard_LocationErrorMsg3;
    static {
	// initialize resource bundle
	NLS.initializeMessages(BUNDLE_NAME, DataTransferMessages.class);
    }

    private DataTransferMessages() {
    }
}
