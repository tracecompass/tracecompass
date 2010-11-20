package org.eclipse.linuxtools.tmf.ui.views.project.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.project.dialogs.messages"; //$NON-NLS-1$
	public static String AddTraceWizard_WindowTitle;
	public static String AddTraceWizardPage_TraceColumnHeader;
	public static String AddTraceWizardPage_WindowTitle;
	public static String AddTraceWizardPage_Description;
    public static String NewProjectWizard_DialogHeader;
    public static String NewProjectWizard_DialogMessage;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
