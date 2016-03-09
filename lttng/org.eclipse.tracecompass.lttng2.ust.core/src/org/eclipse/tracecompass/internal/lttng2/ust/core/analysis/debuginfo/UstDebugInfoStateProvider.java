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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.ust.core.Activator;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.LttngUst28EventLayout;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;

/**
 * State provider for the debuginfo analysis. It tracks the layout of shared
 * libraries loaded in memory by the application.
 *
 * The layout of the generated attribute tree will look like this:
 *
 * <pre>
 *  * [root]
 *   +-- 1000
 *   +-- 2000
 *  ...
 *   +-- 3000 (VPIDs)
 *        +-- baddr (value = addr range end, long)
 *        |     +-- buildId (value = /path/to/library (sopath), string)
 *        +-- baddr
 *        |     +-- buildId1
 *        |     +-- buildId2 (if the same address is re-used later)
 *       ...
 * </pre>
 *
 * The "baddr" attribute name will represent the range start as a string, and
 * its value will be range end. If null, it means this particular library is not
 * loaded at this location at the moment.
 *
 * This sits under the mtime (modification time of the file) attribute. This is
 * to handle cases like multiple concurrent dlopen's of the same library, or the
 * very mind-blowing edge case of a file being modified and being reloaded later
 * on, possibly side-by-side with its previous version.
 *
 * Since the state system is not a spatial database, it's not really worth
 * indexing by memory ranges, and since the amount of loaded libraries is
 * usually small, we should afford to iterate through all mappings to find each
 * match.
 *
 * Still, for better scalability (and for science), it could be interesting to
 * look into storing the memory-model-over-time in something like an R-Tree.
 *
 * @author Alexandre Montplaisir
 */
public class UstDebugInfoStateProvider extends AbstractTmfStateProvider {

    /* Version of this state provider */
    private static final int VERSION = 1;

    private static final int DL_DLOPEN_INDEX = 1;
    private static final int DL_BUILD_ID_INDEX = 2;
    private static final int DL_DEBUG_LINK_INDEX = 3;
    private static final int DL_DLCLOSE_INDEX = 4;
    private static final int STATEDUMP_SOINFO_INDEX = 5;
    private static final int STATEDUMP_BUILD_ID_INDEX = 6;
    private static final int STATEDUMP_DEBUG_LINK_INDEX = 7;

    private final LttngUst28EventLayout fLayout;
    private final Map<String, Integer> fEventNames;

    /**
     * We need both the soinfo/dlopen event AND the matching build_id/debug_link
     * event to get all the information about a particular binary.
     *
     * Between these two events, we will store the <baddr, sopath> in here.
     */
    private final Map<Long, String> fPendingEntries = new HashMap<>();

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
        builder.put(layout.eventStatedumpSoInfo(), STATEDUMP_SOINFO_INDEX);
        builder.put(layout.eventStateDumpBuildId(), STATEDUMP_BUILD_ID_INDEX);
        builder.put(layout.eventStateDumpDebugLink(), STATEDUMP_DEBUG_LINK_INDEX);
        return builder.build();
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        /*
         * We require the "vpid" context to build the state system. The rest of
         * the analysis also needs the "ip" context, but the state provider part
         * does not.
         */
        ITmfEventField vpidCtx = event.getContent().getField(fLayout.contextVpid());
        if (vpidCtx == null) {
            return;
        }
        final Long vpid = (Long) vpidCtx.getValue();
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

        try {
            switch (intIndex) {
            case DL_DLOPEN_INDEX:
            case STATEDUMP_SOINFO_INDEX:
            {
                handleOpen(event, vpid, ss);
                break;
            }

            case DL_BUILD_ID_INDEX:
            case STATEDUMP_BUILD_ID_INDEX:
            {
                handleBuildId(event, vpid, ss);
                break;
            }

            case DL_DEBUG_LINK_INDEX:
            case STATEDUMP_DEBUG_LINK_INDEX:
            /* Fields: Long baddr, Long crc, String filename */
            {
                // TODO NYI
                break;
            }

            case DL_DLCLOSE_INDEX:
            {
                handleClose(event, vpid, ss);
                break;
            }

            default:
                /* Ignore other events */
                break;
            }
        } catch (AttributeNotFoundException e) {
            Activator.getDefault().logError("Unexpected exception in UstDebugInfoStateProvider", e); //$NON-NLS-1$
        }
    }

    /**
     * Handle opening a shared library.
     *
     * Uses fields: Long baddr, Long memsz, String sopath
     */
    private void handleOpen(ITmfEvent event, final Long vpid, final ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        Long baddr = (Long) event.getContent().getField(fLayout.fieldBaddr()).getValue();
        Long memsz = (Long) event.getContent().getField(fLayout.fieldMemsz()).getValue();
        String sopath = (String) event.getContent().getField(fLayout.fieldSopath()).getValue();

        long endAddr = baddr.longValue() + memsz.longValue();
        int addrQuark = ss.getQuarkAbsoluteAndAdd(vpid.toString(), baddr.toString());

        long ts = event.getTimestamp().getValue();
        ss.modifyAttribute(ts, TmfStateValue.newValueLong(endAddr), addrQuark);

        /*
         * Add this library to the pending entries, the matching
         * build_id/debug_link event will finish updating this attribute
         */
        fPendingEntries.put(baddr, checkNotNull(sopath));
    }

    /**
     * Handle shared library build id
     *
     * Uses fields: Long baddr, long[] build_id
     */
    private void handleBuildId(ITmfEvent event, final Long vpid, final ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        Long baddr = (Long) event.getContent().getField(fLayout.fieldBaddr()).getValue();
        long[] buildIdArray = checkNotNull((long[]) event.getContent().getField(fLayout.fieldBuildId()).getValue());
        /*
         * Decode the buildID from the byte array in the trace field.
         * Use lower-case encoding, since this is how eu-readelf
         * displays it.
         */
        String buildId = BaseEncoding.base16().encode(longArrayToByteArray(buildIdArray)).toLowerCase();

        /* Retrieve the matching sopath from the pending entries */
        String sopath = fPendingEntries.remove(baddr);
        if (sopath == null) {
            /*
             * We did not previously handle the initial event for this
             * library. Lost events?
             */
            Activator.getDefault().logWarning("UstDebugInfoStateProvider: Received a build_id event without a matching soinfo/dlopen one."); //$NON-NLS-1$
            return;
        }
        /* addrQuark should already exist */
        int addrQuark = ss.getQuarkAbsolute(vpid.toString(), baddr.toString());
        int buildIdQuark = ss.getQuarkRelativeAndAdd(addrQuark, buildId);
        long ts = event.getTimestamp().getValue();
        ss.modifyAttribute(ts, TmfStateValue.newValueString(sopath), buildIdQuark);
    }

    /**
     * Handle shared library being closed
     *
     * Uses fields: Long baddr
     */
    private void handleClose(ITmfEvent event, final Long vpid, final ITmfStateSystemBuilder ss) {
        Long baddr = (Long) event.getContent().getField(fLayout.fieldBaddr()).getValue();

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
     * Until we can use Java 8 IntStream, see
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
