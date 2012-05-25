package org.eclipse.linuxtools.internal.tmf.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;

@SuppressWarnings("nls")
public class Tracer {

    private static String pluginID = Activator.PLUGIN_ID;

    static Boolean ERROR = Boolean.FALSE;
    static Boolean WARNING = Boolean.FALSE;
    static Boolean INFO = Boolean.FALSE;

    static Boolean COMPONENT = Boolean.FALSE;
    static Boolean REQUEST = Boolean.FALSE;
    static Boolean SIGNAL = Boolean.FALSE;
    static Boolean EVENT = Boolean.FALSE;

    private static String LOGNAME = "trace.log";
    private static BufferedWriter fTraceLog = new BufferedWriter(new OutputStreamWriter(System.out));

    private static BufferedWriter openLogFile(String filename) {
        BufferedWriter outfile = null;
        try {
            outfile = new BufferedWriter(new FileWriter(filename));
        } catch (IOException e) {
            Activator.getDefault().logError("Error opening log file " + filename, e);
        }
        return outfile;
    }

    public static void init() {

        String traceKey;
        boolean isTracing = false;

        traceKey = Platform.getDebugOption(pluginID + "/error");
        if (traceKey != null) {
            ERROR = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= ERROR;
        }

        traceKey = Platform.getDebugOption(pluginID + "/warning");
        if (traceKey != null) {
            WARNING = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= WARNING;
        }

        traceKey = Platform.getDebugOption(pluginID + "/info");
        if (traceKey != null) {
            INFO = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= INFO;
        }

        traceKey = Platform.getDebugOption(pluginID + "/component");
        if (traceKey != null) {
            COMPONENT = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= COMPONENT;
        }

        traceKey = Platform.getDebugOption(pluginID + "/request");
        if (traceKey != null) {
            REQUEST = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= REQUEST;
        }

        traceKey = Platform.getDebugOption(pluginID + "/signal");
        if (traceKey != null) {
            SIGNAL = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= SIGNAL;
        }

        traceKey = Platform.getDebugOption(pluginID + "/event");
        if (traceKey != null) {
            EVENT = (Boolean.valueOf(traceKey)).booleanValue();
            isTracing |= EVENT;
        }

        // Create trace log file if needed
        if (isTracing) {
            fTraceLog = openLogFile(LOGNAME);
        }
    }

    public static void stop() {
        if (fTraceLog == null) {
            return;
        }

        try {
            fTraceLog.close();
            fTraceLog = null;
        } catch (IOException e) {
            Activator.getDefault().logError("Error closing log file", e);
        }
    }

    // Predicates
    public static boolean isErrorTraced() {
        return ERROR;
    }

    public static boolean isComponentTraced() {
        return COMPONENT;
    }

    public static boolean isRequestTraced() {
        return REQUEST;
    }

    public static boolean isSignalTraced() {
        return SIGNAL;
    }

    public static boolean isEventTraced() {
        return EVENT;
    }

    // Tracers
    public static synchronized void trace(String msg) {
        long currentTime = System.currentTimeMillis();
        StringBuilder message = new StringBuilder("[");
        message.append(currentTime / 1000);
        message.append(".");
        message.append(String.format("%1$03d", currentTime % 1000));
        message.append("] ");

        message.append("[Th=");
        message.append(String.format("%1$03d", Thread.currentThread().getId()));
        message.append("] ");

        message.append(msg);

        if (fTraceLog != null) {
            try {
                fTraceLog.write(message.toString());
                fTraceLog.newLine();
                fTraceLog.flush();
            } catch (IOException e) {
                Activator.getDefault().logError("Error writing to log file", e);
            }
        }
    }

    public static void traceComponent(ITmfComponent component, String msg) {
        String message = ("[CMP] Cmp=" + component.getName() + " " + msg);
        trace(message);
    }

    public static void traceRequest(ITmfDataRequest<?> request, String msg) {
        String message = ("[REQ] Req=" + request.getRequestId() + " " + msg);
        trace(message);
    }

    public static void traceSignal(TmfSignal signal, String msg) {
        String message = ("[SIG] Sig=" + signal.getClass().getSimpleName() + " Target=" + msg);
        trace(message);
    }

    public static void traceEvent(ITmfDataProvider<?> provider, ITmfDataRequest<?> request, ITmfEvent event) {
        String message = ("[EVT] Provider=" + provider.toString() + ", Req=" + request.getRequestId() + ", Event=" + event.getTimestamp());
        trace(message);
    }

    public static void traceError(String msg) {
        String message = ("[ERR] Err=" + msg);
        trace(message);
    }

    public static void traceInfo(String msg) {
        String message = ("[INF] " + msg);
        trace(message);
    }

}
