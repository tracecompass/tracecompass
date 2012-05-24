package org.eclipse.linuxtools.internal.lttng2.kernel.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.kernel.ui.messages"; //$NON-NLS-1$

    public static String ControlFlowView_birthTimeColumn;
    public static String ControlFlowView_tidColumn;
    public static String ControlFlowView_ppidColumn;
    public static String ControlFlowView_processColumn;
    public static String ControlFlowView_traceColumn;
    
    public static String ControlFlowView_stateTypeName;
    public static String ControlFlowView_nextProcessActionNameText;
    public static String ControlFlowView_nextProcessActionToolTipText;
    public static String ControlFlowView_previousProcessActionNameText;
    public static String ControlFlowView_previousProcessActionToolTipText;
    
    public static String ControlFlowView_attributeSyscallName;

    public static String ResourcesView_stateTypeName;
    public static String ResourcesView_nextResourceActionNameText;
    public static String ResourcesView_nextResourceActionToolTipText;
    public static String ResourcesView_previousResourceActionNameText;
    public static String ResourcesView_previousResourceActionToolTipText;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
