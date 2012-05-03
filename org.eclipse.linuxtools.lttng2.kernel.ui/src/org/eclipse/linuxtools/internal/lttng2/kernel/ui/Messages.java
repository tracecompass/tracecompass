package org.eclipse.linuxtools.internal.lttng2.kernel.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.kernel.ui.messages"; //$NON-NLS-1$

    public static String ControlFlowView_birthNsecColumn;
    public static String ControlFlowView_birthSecColumn;
    public static String ControlFlowView_brandColumn;
    public static String ControlFlowView_cpuColumn;
    public static String ControlFlowView_tidColumn;
    public static String ControlFlowView_ppidColumn;
    public static String ControlFlowView_processColumn;
    public static String ControlFlowView_tgidColumn;
    public static String ControlFlowView_traceColumn;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
