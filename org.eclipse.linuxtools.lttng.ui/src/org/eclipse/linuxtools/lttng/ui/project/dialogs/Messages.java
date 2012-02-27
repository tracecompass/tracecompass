package org.eclipse.linuxtools.lttng.ui.project.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.lttng.ui.project.dialogs.messages"; //$NON-NLS-1$
    public static String NewProjectWizard_Title;
	public static String NewProjectWizard_Description;
	public static String TraceLibraryPath_label;
	public static String TraceLibraryPath_browseBtn;
	public static String TraceLibraryPathWizardPage_SpecifiedTraceLibraryLocation_notExists;
	public static String TraceLibraryPathWizardPage_TraceLoaderLibrary_notExists;
	public static String TraceLibraryPathWizardPage_Title;
	public static String TraceLibraryPathWizardPage_Description;
	public static String TraceLibraryPathWizard_Message;
	public static String TraceLibraryPathProperty_Message;
	public static String TraceLibraryPath_Note;
	public static String TraceLibraryPath_Message;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

    private Messages() {
    }
}
