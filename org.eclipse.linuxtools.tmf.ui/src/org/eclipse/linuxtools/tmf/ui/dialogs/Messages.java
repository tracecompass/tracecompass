package org.eclipse.linuxtools.tmf.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.dialogs.messages"; //$NON-NLS-1$
    public static String ManageCustomParsersDialog_DeleteButtonLabel;
    public static String ManageCustomParsersDialog_DeleteConfirmation;
    public static String ManageCustomParsersDialog_DeleteParserDialogHeader;
    public static String ManageCustomParsersDialog_DialogHeader;
    public static String ManageCustomParsersDialog_EditButtonLabel;
    public static String ManageCustomParsersDialog_ExportButtonLabel;
    public static String ManageCustomParsersDialog_ExportParserSelection;
    public static String ManageCustomParsersDialog_ImportButtonLabel;
    public static String ManageCustomParsersDialog_ImportParserSelection;
    public static String ManageCustomParsersDialog_NewButtonLabel;
    public static String ManageCustomParsersDialog_ParseButtonLabel;
    public static String ManageCustomParsersDialog_TextButtonLabel;
    public static String ManageCustomParsersDialog_TraceSelection;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
