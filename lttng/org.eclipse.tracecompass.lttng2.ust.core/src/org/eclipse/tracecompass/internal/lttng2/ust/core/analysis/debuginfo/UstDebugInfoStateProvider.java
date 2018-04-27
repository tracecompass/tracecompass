/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.LttngUst28EventLayout;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.util.Pair;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;

/**
 * State provider for the debuginfo analysis. It tracks the layout of shared
 * libraries loaded in memory by the application.
 *
 * The layout of the generated attribute tree will look like this:
 *
 * <pre>
 * Key                       Value
 * /vpid                     null
 * /vpid/<baddr>             Integer 1 if the memory mapping active at this
 *                           point in time, null otherwise.
 * /vpid/<baddr>/build_id    Build ID of the binary as an hex string, e.g.
 *                           "0123456789abcdef", or null if it doesn't have a
 *                           build id.
 * /vpid/<baddr>/debug_link  Path to the separate debug info of the binary, e.g.
 *                           "/usr/lib/libhello.so.debug", or null if it doesn't
 *                           have separate debug info.
 * /vpid/<baddr>/memsz       Size of the memory mapping in bytes.
 * /vpid/<baddr>/path        Path to the binary, e.g. "/usr/lib/libhello.so".
 * /vpid/<baddr>/is_pic      Integer 1 if the binary contains position
 *                           independent code, 0 otherwise.
 * </pre>
 *
 * The "baddr" attribute name represents the memory mapping base address a
 * string (in decimal).
 *
 * @author Alexandre Montplaisir
 * @author Simon Marchi
 */
public class UstDebugInfoStateProvider extends AbstractTmfStateProvider {

    /** State system attribute name for the in-memory binary size */
    public static final String MEMSZ_ATTRIB = "memsz"; //$NON-NLS-1$

    /** State system attribute name for the binary path */
    public static final String PATH_ATTRIB = "path"; //$NON-NLS-1$

    /** State system attribute name for the PICness of the binary */
    public static final String IS_PIC_ATTRIB = "is_pic"; //$NON-NLS-1$

    /** State system attribute name for the build ID of the binary */
    public static final String BUILD_ID_ATTRIB = "build_id"; //$NON-NLS-1$

    /** State system attribute name for the debug link of the binary */
    public static final String DEBUG_LINK_ATTRIB = "debug_link"; //$NON-NLS-1$

    /** Version of this state provider */
    private static final int VERSION = 5;

    private static final Logger LOGGER = TraceCompassLog.getLogger(UstDebugInfoStateProvider.class);

    private static final int DL_DLOPEN_INDEX = 1;
    private static final int DL_BUILD_ID_INDEX = 2;
    private static final int DL_DEBUG_LINK_INDEX = 3;
    private static final int DL_DLCLOSE_INDEX = 4;
    private static final int STATEDUMP_BIN_INFO_INDEX = 5;
    private static final int STATEDUMP_BUILD_ID_INDEX = 6;
    private static final int STATEDUMP_DEBUG_LINK_INDEX = 7;
    private static final int STATEDUMP_START_INDEX = 8;

    private final LttngUst28EventLayout fLayout;
    private final Map<String, Integer> fEventNames;

    /**
     * Map of the latest statedump's timestamps, per VPID: Map<vpid, timestamp>
     */
    private final Map<Long, Long> fLatestStatedumpStarts = new HashMap<>();

    /*
     * Store for data that is incomplete, for which we are waiting for some
     * upcoming events (build_id or debug_link). Maps <vpid, baddr> to
     * PendingBinInfo object.
     */
    private final Map<Pair<Long, Long>, PendingBinInfo> fPendingBinInfos = new HashMap<>();

    private class PendingBinInfo {

        /* The event data, saved here until we put everything in the state system. */
        private final long fVpid;
        private final long fBaddr;
        private final long fMemsz;
        private final String fPath;
        private final boolean fIsPIC;

        private @Nullable String fBuildId = null;
        private @Nullable String fDebugLink = null;

        /* Which info are we waiting for? */
        private boolean fBuildIdPending;
        private boolean fDebugLinkPending;

        public PendingBinInfo(boolean hasBuildId, boolean hasDebugLink,
                long vpid, long baddr, long memsz, String path, boolean isPIC) {
            fVpid = vpid;
            fBaddr = baddr;
            fMemsz = memsz;
            fPath = path;
            fIsPIC = isPIC;

            fBuildIdPending = hasBuildId;
            fDebugLinkPending = hasDebugLink;
        }

        boolean done() {
            return !(fBuildIdPending || fDebugLinkPending);
        }

        public void setBuildId(String buildId) {
            fBuildIdPending = false;
            fBuildId = buildId;
        }

        public @Nullable String getBuildId() {
            return fBuildId;
        }

        public void setDebugLink(String debugLink) {
            fDebugLinkPending = false;
            fDebugLink = debugLink;
        }

        public @Nullable String getDebugLink() {
            return fDebugLink;
        }
    }

    /**
     * Constructor
     *
     * @param trace
     *            trace
     */
    public UstDebugInfoStateProvider(LttngUstTrace trace) {
        super(trace, "Ust:DebugInfo"); //$NON-NLS-1$
        ILttngUstEventLayout layout = trace.getEventLayout();
        if (!(layout instanceof LttngUst28EventLayout)) {
            /* This analysis only support UST 2.8+ traces */
            throw new IllegalStateException("Debug info analysis was started with an incompatible trace."); //$NON-NLS-1$
        }
        fLayout = (LttngUst28EventLayout) layout;
        fEventNames = buildEventNames(fLayout);
    }

    private static Map<String, Integer> buildEventNames(LttngUst28EventLayout layout) {
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        builder.put(layout.eventDlOpen(), DL_DLOPEN_INDEX);
        builder.put(layout.eventDlBuildId(), DL_BUILD_ID_INDEX);
        builder.put(layout.eventDlDebugLink(), DL_DEBUG_LINK_INDEX);
        builder.put(layout.eventDlClose(), DL_DLCLOSE_INDEX);
        builder.put(layout.eventStatedumpBinInfo(), STATEDUMP_BIN_INFO_INDEX);
        builder.put(layout.eventStateDumpBuildId(), STATEDUMP_BUILD_ID_INDEX);
        builder.put(layout.eventStateDumpDebugLink(), STATEDUMP_DEBUG_LINK_INDEX);
        builder.put(layout.eventStatedumpStart(), STATEDUMP_START_INDEX);
        return builder.build();
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        /*
         * We require the "vpid" context to build the state system. The rest of
         * the analysis also needs the "ip" context, but the state provider part
         * does not.
         */
        final Long vpid = event.getContent().getFieldValue(Long.class, fLayout.contextVpid());
        if (vpid == null) {
            return;
        }

        final @NonNull ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());

        String name = event.getName();
        Integer index = fEventNames.get(name);
        if (index == null) {
            /* Untracked event type */
            return;
        }
        int intIndex = index.intValue();

        switch (intIndex) {
        case STATEDUMP_START_INDEX: {
            handleStatedumpStart(event, vpid, ss);
            break;
        }

        case STATEDUMP_BIN_INFO_INDEX:
            handleBinInfo(event, vpid, ss, true);
            break;
        case DL_DLOPEN_INDEX:
            handleBinInfo(event, vpid, ss, false);
            break;

        case STATEDUMP_BUILD_ID_INDEX:
            handleBuildId(event, vpid, ss, true);
            break;
        case DL_BUILD_ID_INDEX:
            handleBuildId(event, vpid, ss, false);
            break;

        case STATEDUMP_DEBUG_LINK_INDEX:
            handleDebugLink(event, vpid, ss, true);
            break;
        case DL_DEBUG_LINK_INDEX:
            handleDebugLink(event, vpid, ss, false);
            break;

        case DL_DLCLOSE_INDEX: {
            handleClose(event, vpid, ss);
            break;
        }

        default:
            /* Ignore other events */
            break;
        }
    }

    /**
     * Commit the binary information contained in pending to the state system.
     *
     * This method should only be called when there is no more pending
     * information for that binary.
     */
    private static void commitPendingToStateSystem(PendingBinInfo pending,
            long ts, ITmfStateSystemBuilder ss) {
        if (!pending.done()) {
            throw new IllegalStateException();
        }

        long vpid = pending.fVpid;
        long baddr = pending.fBaddr;
        long memsz = pending.fMemsz;
        String path = pending.fPath;
        String buildId = pending.getBuildId();
        String debugLink = pending.getDebugLink();
        boolean isPIC = pending.fIsPIC;

        /* Create the "top-level" attribute for this object. */
        int baddrQuark = ss.getQuarkAbsoluteAndAdd(Long.toString(vpid), Long.toString(baddr));

        /* Create the attributes that contain actual data. */
        int memszQuark = ss.getQuarkRelativeAndAdd(baddrQuark, MEMSZ_ATTRIB);
        int pathQuark = ss.getQuarkRelativeAndAdd(baddrQuark, PATH_ATTRIB);
        int isPICQuark = ss.getQuarkRelativeAndAdd(baddrQuark, IS_PIC_ATTRIB);
        int buildIdQuark = ss.getQuarkRelativeAndAdd(baddrQuark, BUILD_ID_ATTRIB);
        int debugLinkQuark = ss.getQuarkRelativeAndAdd(baddrQuark, DEBUG_LINK_ATTRIB);
        try {
            ss.modifyAttribute(ts, 1, baddrQuark);
            ss.modifyAttribute(ts, memsz, memszQuark);
            ss.modifyAttribute(ts, path, pathQuark);
            ss.modifyAttribute(ts, isPIC ? 1 : 0, isPICQuark);
            if (buildId != null) {
                ss.modifyAttribute(ts, buildId, buildIdQuark);
            } else {
                ss.modifyAttribute(ts, (Object) null, buildIdQuark);
            }

            if (debugLink != null) {
                ss.modifyAttribute(ts,  debugLink, debugLinkQuark);
            } else {
                ss.modifyAttribute(ts, (Object) null, debugLinkQuark);
            }
        } catch (StateValueTypeException e) {
            /* Something went very wrong. */
            throw new IllegalStateException(e);
        }
    }

    /**
     * Locate a PendingBinInfo object in the map of pending binary informations
     * with the key <vpid, baddr>. Remove it from the map and return it if it is
     * found, return null otherwise.
     */
    private @Nullable PendingBinInfo retrievePendingBinInfo(long vpid, long baddr) {
        Pair<Long, Long> key = new Pair<>(vpid, baddr);

        return fPendingBinInfos.remove(key);
    }

    /**
     * Check whether we know everything there is to know about the binary
     * described by pending, and if so, commit it to the state system.
     * Otherwise, put it in the map of pending binary informations.
     */
    private void processPendingBinInfo(PendingBinInfo pending, long ts,
            ITmfStateSystemBuilder ss) {
        if (pending.done()) {
            commitPendingToStateSystem(pending, ts, ss);
        } else {
            /* We are expecting more data for this binary, put in the pending map. */
            Pair<Long, Long> key = new Pair<>(pending.fVpid, pending.fBaddr);

            fPendingBinInfos.put(key, pending);
        }
    }

    /**
     * Handle the start of a statedump.
     *
     * When a process does an exec, a new statedump is done and all previous
     * mappings are now invalid.
     */
    private void handleStatedumpStart(ITmfEvent event, final Long vpid, final ITmfStateSystemBuilder ss) {
        long ts = event.getTimestamp().getValue();
        fLatestStatedumpStarts.put(vpid, ts);

        try {
            int vpidQuark = ss.getQuarkAbsolute(vpid.toString());
            ss.removeAttribute(ts, vpidQuark);
        } catch (AttributeNotFoundException e) {
            /* We didn't know anything about this vpid yet, so there is nothing to remove. */
        }
    }

    /**
     * Handle a bin_info event, which gives information about a binary or shared
     * library loaded in the process's memory space.
     *
     *
     * Uses fields: Long baddr, Long memsz, String path, Long is_pic
     *
     * @param statedump
     *            Indicates if it comes from a statedump event, or a
     *            dlopen/lib:load event.
     */
    private void handleBinInfo(ITmfEvent event, final Long vpid,
            final ITmfStateSystemBuilder ss, boolean statedump) {
        Long baddr = event.getContent().getFieldValue(Long.class, fLayout.fieldBaddr());
        Long memsz = event.getContent().getFieldValue(Long.class, fLayout.fieldMemsz());
        String path = event.getContent().getFieldValue(String.class, fLayout.fieldPath());
        Long hasBuildIdValue = event.getContent().getFieldValue(Long.class, fLayout.fieldHasBuildId());
        Long hasDebugLinkValue = event.getContent().getFieldValue(Long.class, fLayout.fieldHasDebugLink());
        Long isPicValue = event.getContent().getFieldValue(Long.class, fLayout.fieldIsPic());

        if (baddr == null ||
                memsz == null ||
                path == null ||
                hasBuildIdValue == null ||
                hasDebugLinkValue == null ||
                (statedump && isPicValue == null)) {
            TraceCompassLogUtils.traceInstant(LOGGER, Level.CONFIG, "UstDebugInfoStateProvider:InvalidBinInfoEvent", "event", event); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        /*
         * Dlopen/load events do not have an is_pic field, it is taken for
         * granted.
         */
        boolean isPic = (statedump ? (checkNotNull(isPicValue).longValue() != 0) : true);
        long ts = getBinInfoTimeStamp(event, vpid, statedump);

        boolean hasBuildId = (hasBuildIdValue != 0);
        boolean hasDebugLink = (hasDebugLinkValue != 0);

        PendingBinInfo p = new PendingBinInfo(hasBuildId, hasDebugLink, vpid, baddr, memsz, path, isPic);
        processPendingBinInfo(p, ts, ss);
    }

    /**
     * Handle shared library build id
     *
     * Uses fields: Long baddr, long[] build_id
     *
     * @param statedump
     *            Indicates if it comes from a statedump event, or a
     *            dlopen/lib:build_id event.
     */
    private void handleBuildId(ITmfEvent event, final Long vpid,
            final ITmfStateSystemBuilder ss, boolean statedump) {
        long[] buildIdArray = event.getContent().getFieldValue(long[].class, fLayout.fieldBuildId());
        Long baddr = event.getContent().getFieldValue(Long.class, fLayout.fieldBaddr());

        if (buildIdArray == null || baddr == null) {
            TraceCompassLogUtils.traceInstant(LOGGER, Level.CONFIG, "UstDebugInfoStateProvider:InvalidBinIdEvent", "event", event); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        /*
         * Decode the buildID from the byte array in the trace field.
         * Use lower-case encoding, since this is how eu-readelf
         * displays it.
         */
        String buildId = checkNotNull(BaseEncoding.base16().encode(longArrayToByteArray(buildIdArray)).toLowerCase());

        long ts = getBinInfoTimeStamp(event, vpid, statedump);
        PendingBinInfo p = retrievePendingBinInfo(vpid, baddr);

        /*
         * We have never seen the bin_info event this event is related to,
         * there's nothing much we can do.
         */
        if (p == null) {
            return;
        }

        p.setBuildId(buildId);
        processPendingBinInfo(p, ts, ss);
    }

    private void handleDebugLink(ITmfEvent event, final Long vpid,
            final ITmfStateSystemBuilder ss, boolean statedump) {
        Long baddr = event.getContent().getFieldValue(Long.class, fLayout.fieldBaddr());
        String debugLink = event.getContent().getFieldValue(String.class, fLayout.fieldDebugLinkFilename());

        if (baddr == null || debugLink == null) {
            TraceCompassLogUtils.traceInstant(LOGGER, Level.CONFIG, "UstDebugInfoStateProvider:InvalidDebugLinkEvent", "event", event); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        long ts = getBinInfoTimeStamp(event, vpid, statedump);

        PendingBinInfo pendingBinInfo = retrievePendingBinInfo(vpid, baddr);
        if (pendingBinInfo == null) {
            return;
        }

        pendingBinInfo.setDebugLink(debugLink);
        processPendingBinInfo(pendingBinInfo, ts, ss);
    }

    /**
     * Handle shared library being closed
     *
     * Uses fields: Long baddr
     */
    private void handleClose(ITmfEvent event, final Long vpid, final ITmfStateSystemBuilder ss) {
        Long baddr = event.getContent().getFieldValue(Long.class, fLayout.fieldBaddr());

        if (baddr == null) {
            TraceCompassLogUtils.traceInstant(LOGGER, Level.CONFIG, "UstDebugInfoStateProvider:InvalidDlCloseEvent", "event", event); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        try {
            int quark = ss.getQuarkAbsolute(vpid.toString(), baddr.toString());
            long ts = event.getTimestamp().getValue();
            ss.removeAttribute(ts, quark);
        } catch (AttributeNotFoundException e) {
            /*
             * We have never seen a matching dlopen() for this
             * dlclose(). Possible that it happened before the start of
             * the trace, or that it was lost through lost events.
             */
        }
    }

    /**
     * Get the effective timestamp of a bin_info/dlopen/lib:load event.
     *
     * This is used to consider bin_info events to all virtually happen at the
     * statedump start, so that symbol mappings become available as soon as
     * possible. The UST statedump happens while a libdl lock is taken, so no
     * mapping change can happen while it is ongoing.
     *
     * dlopen/lib:load events directly use their own timestamp, since they
     * happen independently of the statedump.
     *
     * @param event
     *            The event, its timestamp will be used if we do not find a
     *            statedump start
     * @param vpid
     *            The pid of the process
     * @param statedump
     *            True if the event is a statedump (aka bin_info) event, false
     *            if it is a dlopen/lib:load event.
     * @return The timestamp to use
     */
    private long getBinInfoTimeStamp(ITmfEvent event, final Long vpid, boolean statedump) {
        if (statedump) {
            Long statedumpStartTime = fLatestStatedumpStarts.get(vpid);
            if (statedumpStartTime != null) {
                return statedumpStartTime;
            }
        }

        return event.getTimestamp().getValue();
    }

    /**
     * FIXME: Until we can use Java 8 IntStream, see
     * http://stackoverflow.com/a/28008477/4227853.
     */
    private static byte[] longArrayToByteArray(long[] array) {
        byte[] ret = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            ret[i] = (byte) array[i];
        }
        return ret;
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new UstDebugInfoStateProvider(getTrace());
    }

    @Override
    public LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    public int getVersion() {
        return VERSION;
    }
}
