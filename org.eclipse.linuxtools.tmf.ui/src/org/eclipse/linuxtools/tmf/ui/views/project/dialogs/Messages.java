package org.eclipse.linuxtools.tmf.ui.views.project.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.project.dialogs.messages"; //$NON-NLS-1$
	public static String AddTraceWizard_invalidTraceLocation;
	public static String AddTraceWizard_windowTitle;
	public static String AddTraceWizardPage_columnHeader;
	public static String AddTraceWizardPage_description;
	public static String AddTraceWizardPage_windowTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
