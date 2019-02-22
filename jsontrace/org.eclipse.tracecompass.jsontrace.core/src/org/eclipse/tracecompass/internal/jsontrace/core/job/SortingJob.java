/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.jsontrace.core.job;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.internal.jsontrace.core.Activator;
import org.eclipse.tracecompass.internal.jsontrace.core.Messages;
import org.eclipse.tracecompass.internal.provisional.jsontrace.core.trace.JsonTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * On-disk sorting job. It splits a trace into tracelets. Each tracelet is
 * sorted in ram and written to disk, then the tracelets are merged into a big
 * trace.
 *
 * @author Matthew Khouzam
 */
public abstract class SortingJob extends Job {

    private static final char CLOSE_BRACKET = ']';
    private static final char OPEN_BRACKET = '[';
    private static final int CHARS_PER_LINE_ESTIMATE = 50;
    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(SortingJob.class);
    private static final int CHUNK_SIZE = 65535;

    private static final Comparator<PartiallyParsedEvent> EVENT_COMPARATOR = Comparator
            .comparing(PartiallyParsedEvent::getTs);

    private static final class PartiallyParsedEvent {
        private static final @NonNull BigDecimal MINUS_ONE = BigDecimal.valueOf(-1);

        private final BigDecimal fTs;
        private String fLine;
        private final int fPos;

        public PartiallyParsedEvent(String key, String string, int i) {
            fLine = string;
            int indexOf = string.indexOf(key);
            if (indexOf < 0) {
                fTs = MINUS_ONE;
                fPos = -1;
            } else {
                int index = indexOf + key.length();
                int end = string.indexOf(',', index);
                if (end == -1) {
                    end = string.indexOf('}', index);
                }
                BigDecimal ts;
                String number = string.substring(index, end).trim().replace("\"", "");
                if (!number.isEmpty()) {
                    try {
                        // This may be a bit slow, it can be optimized if need be.
                        ts = new BigDecimal(number);
                    } catch (NumberFormatException e) {
                        // Cannot be parsed as a number, set to -1
                        ts = MINUS_ONE;
                    }
                } else {
                    ts = MINUS_ONE;
                }
                fTs = ts;
                fPos = i;
            }
        }

        public BigDecimal getTs() {
            return fTs;
        }
    }

    private final Integer fBracketsToSkip;
    private final String fTsKey;
    private final String fPath;
    private final ITmfTrace fTrace;

    /**
     * Constructor
     *
     * @param trace
     *            Trace to sort
     * @param path
     *            Trace path
     * @param tsKey
     *            Timestamp key, represent the json object key. The value associated
     *            to this key is the timestamp that will be use to sort
     * @param bracketsToSkip
     *            Number of bracket to skip
     */
    public SortingJob(ITmfTrace trace, String path, String tsKey, int bracketsToSkip) {
        super(Messages.SortingJob_description);
        fTrace = trace;
        fPath = path;
        fTsKey = tsKey;
        fBracketsToSkip = bracketsToSkip;
    }

    /**
     * Getter for the trace path
     *
     * @return the path
     */
    public String getPath() {
        return fPath;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        ITmfTrace trace = fTrace;
        IProgressMonitor subMonitor = SubMonitor.convert(monitor, 3);
        if (trace == null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Trace cannot be null"); //$NON-NLS-1$
        }
        String dir = TmfTraceManager.getSupplementaryFileDir(trace);
        subMonitor.beginTask(Messages.SortingJob_sorting, (int) (new File(fPath).length() / CHARS_PER_LINE_ESTIMATE));
        subMonitor.subTask(Messages.SortingJob_splitting);
        File tempDir = new File(dir + ".tmp"); //$NON-NLS-1$
        tempDir.mkdirs();
        List<File> tracelings = new ArrayList<>();
        try (BufferedInputStream parser = new BufferedInputStream(new FileInputStream(fPath))) {
            int data = 0;
            for (int nbBracket = 0; nbBracket < fBracketsToSkip; nbBracket++) {
                data = parser.read();
                while (data != '[') {
                    data = parser.read();
                    if (data == -1) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                "Missing symbol \'[\' or \']\' in " + fPath); //$NON-NLS-1$
                    }
                }
            }
            List<PartiallyParsedEvent> events = new ArrayList<>(CHUNK_SIZE);
            String eventString = JsonTrace.readNextEventString(() -> (char) parser.read());
            if (eventString == null) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Empty event in " + fPath); //$NON-NLS-1$
            }
            PartiallyParsedEvent line = new PartiallyParsedEvent(fTsKey, eventString, 0);
            line.fLine = data + '"' + line.fLine;
            int cnt = 0;
            int filen = 0;
            while (eventString != null) {
                while (cnt < CHUNK_SIZE) {
                    events.add(line);
                    subMonitor.worked(1);
                    if (subMonitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    eventString = JsonTrace.readNextEventString(() -> (char) parser.read());
                    if (eventString == null) {
                        break;
                    }
                    line = new PartiallyParsedEvent(fTsKey, eventString, 0);
                    cnt++;
                }
                events.sort(EVENT_COMPARATOR);
                cnt = 0;
                File traceling = new File(tempDir + File.separator + "test" + filen + ".json"); //$NON-NLS-1$ //$NON-NLS-2$
                tracelings.add(traceling);
                boolean success = traceling.createNewFile();
                if (!success) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            "Could not create partial file " + traceling.getAbsolutePath()); //$NON-NLS-1$
                }
                try (PrintWriter fs = new PrintWriter(traceling)) {
                    fs.println(OPEN_BRACKET);
                    for (PartiallyParsedEvent sortedEvent : events) {
                        fs.println(sortedEvent.fLine + ',');
                    }
                    fs.println(CLOSE_BRACKET);
                }
                events.clear();
                filen++;
                subMonitor.worked(1);
                if (subMonitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }

            }
            subMonitor.subTask(Messages.SortingJob_merging);
            PriorityQueue<PartiallyParsedEvent> evs = new PriorityQueue<>(EVENT_COMPARATOR);
            List<BufferedInputStream> parsers = new ArrayList<>();
            int i = 0;
            for (File traceling : tracelings) {

                /*
                 * This resource is added to a priority queue and then removed at the very end.
                 */
                BufferedInputStream createParser = new BufferedInputStream(new FileInputStream(traceling));
                while (data != '{') {
                    data = (char) parser.read();
                    if (data == (char) -1) {
                        break;
                    }
                }
                eventString = JsonTrace.readNextEventString(() -> (char) createParser.read());
                PartiallyParsedEvent parse = new PartiallyParsedEvent(fTsKey, eventString, i);
                evs.add(parse);
                i++;
                parsers.add(createParser);
                subMonitor.worked(1);
                if (subMonitor.isCanceled()) {
                    break;
                }
            }
            if (subMonitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            processMetadata(trace, dir);

            File file = new File(dir + File.separator + new File(trace.getPath()).getName());
            boolean success = file.createNewFile();
            if (!success) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        "Could not create file " + file.getAbsolutePath()); //$NON-NLS-1$
            }
            try (PrintWriter tempWriter = new PrintWriter(file)) {
                tempWriter.println('[');
                while (!evs.isEmpty()) {
                    PartiallyParsedEvent sortedEvent = evs.poll();
                    PartiallyParsedEvent parse = readNextEvent(parsers.get(sortedEvent.fPos), fTsKey, sortedEvent.fPos);
                    if (parse != null) {
                        tempWriter.println(sortedEvent.fLine.trim() + ',');
                        evs.add(parse);
                    } else {
                        tempWriter.println(sortedEvent.fLine.trim() + (evs.isEmpty() ? "" : ',')); //$NON-NLS-1$
                    }
                    subMonitor.worked(1);
                    if (subMonitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                }
                tempWriter.println(']');
            }
            for (BufferedInputStream tmpParser : parsers) {
                tmpParser.close();
            }
        } catch (IOException e) {
            TraceCompassLogUtils.traceInstant(LOGGER, Level.WARNING, "IOException in sorting job", "trace", fPath, //$NON-NLS-1$ //$NON-NLS-2$
                    "exception", e); //$NON-NLS-1$
        } finally {
            try {
                for (File tl : tracelings) {
                    Files.delete(tl.toPath());
                }
                Files.delete(tempDir.toPath());
            } catch (IOException e) {
                Activator.getInstance().logError(e.getMessage(), e);
            }

            subMonitor.done();
        }
        return Status.OK_STATUS;

    }

    /**
     * Process whatever metadata that can be found after the event list in the trace
     * file file
     *
     * @param trace
     *            the trace to be sort
     * @param dir
     *            the path to the trace file
     * @throws IOException
     */
    protected abstract void processMetadata(ITmfTrace trace, String dir) throws IOException;

    private static @Nullable PartiallyParsedEvent readNextEvent(BufferedInputStream parser, String key, int i)
            throws IOException {
        String event = JsonTrace.readNextEventString(() -> (char) parser.read());
        return event == null ? null : new PartiallyParsedEvent(key, event, i);

    }
}