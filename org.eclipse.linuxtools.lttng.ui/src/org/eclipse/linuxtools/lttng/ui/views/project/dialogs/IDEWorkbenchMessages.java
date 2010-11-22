package org.eclipse.linuxtools.lttng.ui.views.project.dialogs;

import org.eclipse.osgi.util.NLS;

public class IDEWorkbenchMessages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.lttng.ui.views.project.dialogs.messages"; //$NON-NLS-1$
    public static String NewExperimentDialog_DialogTitle;
    public static String NewExperimentDialog_ExperimentLabel;
    static {
	// initialize resource bundle
	NLS.initializeMessages(BUNDLE_NAME, IDEWorkbenchMessages.class);
    }

    private IDEWorkbenchMessages() {
    }
}
