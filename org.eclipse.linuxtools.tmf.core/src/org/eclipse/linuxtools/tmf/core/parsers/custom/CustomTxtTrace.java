/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.parsers.custom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputLine;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;

/**
 * Base class for custom plain text traces.
 *
 * @author Patrick Tassé
 * @since 3.0
 */
public class CustomTxtTrace extends TmfTrace implements ITmfEventParser, ITmfPersistentlyIndexable {

    private static final TmfLongLocation NULL_LOCATION = new TmfLongLocation((Long) null);
    private static final int DEFAULT_CACHE_SIZE = 100;
    private static final int MAX_LINES = 100;
    private static final int MAX_CONFIDENCE = 100;

    private final CustomTxtTraceDefinition fDefinition;
    private final CustomTxtEventType fEventType;
    private BufferedRandomAccessFile fFile;

    /**
     * Basic constructor.
     *
     * @param definition
     *            Text trace definition
     */
    public CustomTxtTrace(final CustomTxtTraceDefinition definition) {
        fDefinition = definition;
        fEventType = new CustomTxtEventType(fDefinition);
        setCacheSize(DEFAULT_CACHE_SIZE);
    }

    /**
     * Full constructor.
     *
     * @param resource
     *            Trace's resource.
     * @param definition
     *            Text trace definition
     * @param path
     *            Path to the trace file
     * @param cacheSize
     *            Cache size to use
     * @throws TmfTraceException
     *             If we couldn't open the trace at 'path'
     */
    public CustomTxtTrace(final IResource resource,
            final CustomTxtTraceDefinition definition, final String path,
            final int cacheSize) throws TmfTraceException {
        this(definition);
        setCacheSize((cacheSize > 0) ? cacheSize : DEFAULT_CACHE_SIZE);
        initTrace(resource, path, CustomTxtEvent.class);
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);
        try {
            fFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if (fFile != null) {
            try {
                fFile.close();
            } catch (IOException e) {
            } finally {
                fFile = null;
            }
        }
    }

    @Override
    public ITmfTraceIndexer getIndexer() {
        return super.getIndexer();
    }

    @Override
    public synchronized TmfContext seekEvent(final ITmfLocation location) {
        final CustomTxtTraceContext context = new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        if (NULL_LOCATION.equals(location) || fFile == null) {
            return context;
        }
        try {
            if (location == null) {
                fFile.seek(0);
            } else if (location.getLocationInfo() instanceof Long) {
                fFile.seek((Long) location.getLocationInfo());
            }
            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                for (final InputLine input : getFirstLines()) {
                    final Matcher matcher = input.getPattern().matcher(line);
                    if (matcher.find()) {
                        context.setLocation(new TmfLongLocation(rawPos));
                        context.firstLineMatcher = matcher;
                        context.firstLine = line;
                        context.nextLineLocation = fFile.getFilePointer();
                        context.inputLine = input;
                        return context;
                    }
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
            return context;
        } catch (final FileNotFoundException e) {
            Activator.logError("Error seeking event. File not found: " + getPath(), e); //$NON-NLS-1$
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return context;
        }

    }

    @Override
    public synchronized TmfContext seekEvent(final double ratio) {
        if (fFile == null) {
            return new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
        try {
            long pos = Math.round(ratio * fFile.length());
            while (pos > 0) {
                fFile.seek(pos - 1);
                if (fFile.read() == '\n') {
                    break;
                }
                pos--;
            }
            final ITmfLocation location = new TmfLongLocation(pos);
            final TmfContext context = seekEvent(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
    }

    @Override
    public synchronized double getLocationRatio(final ITmfLocation location) {
        if (fFile == null) {
            return 0;
        }
        try {
            if (location.getLocationInfo() instanceof Long) {
                return ((Long) location.getLocationInfo()).doubleValue() / fFile.length();
            }
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
        }
        return 0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized CustomTxtEvent parseEvent(final ITmfContext tmfContext) {
        ITmfContext context = seekEvent(tmfContext.getLocation());
        return parse(context);
    }

    @Override
    public synchronized CustomTxtEvent getNext(final ITmfContext context) {
        final ITmfContext savedContext = new TmfContext(context.getLocation(), context.getRank());
        final CustomTxtEvent event = parse(context);
        if (event != null) {
            updateAttributes(savedContext, event.getTimestamp());
            context.increaseRank();
        }
        return event;
    }

    private synchronized CustomTxtEvent parse(final ITmfContext tmfContext) {
        if (fFile == null) {
            return null;
        }
        if (!(tmfContext instanceof CustomTxtTraceContext)) {
            return null;
        }

        final CustomTxtTraceContext context = (CustomTxtTraceContext) tmfContext;
        if (context.getLocation() == null || !(context.getLocation().getLocationInfo() instanceof Long) || NULL_LOCATION.equals(context.getLocation())) {
            return null;
        }

        CustomTxtEvent event = parseFirstLine(context);

        final HashMap<InputLine, Integer> countMap = new HashMap<>();
        InputLine currentInput = null;
        if (context.inputLine.childrenInputs != null && context.inputLine.childrenInputs.size() > 0) {
            currentInput = context.inputLine.childrenInputs.get(0);
            countMap.put(currentInput, 0);
        }

        try {
            if (fFile.getFilePointer() != context.nextLineLocation) {
                fFile.seek(context.nextLineLocation);
            }
            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                boolean processed = false;
                if (currentInput == null) {
                    for (final InputLine input : getFirstLines()) {
                        final Matcher matcher = input.getPattern().matcher(line);
                        if (matcher.find()) {
                            context.setLocation(new TmfLongLocation(rawPos));
                            context.firstLineMatcher = matcher;
                            context.firstLine = line;
                            context.nextLineLocation = fFile.getFilePointer();
                            context.inputLine = input;
                            return event;
                        }
                    }
                } else {
                    if (countMap.get(currentInput) >= currentInput.getMinCount()) {
                        final List<InputLine> nextInputs = currentInput.getNextInputs(countMap);
                        if (nextInputs.size() == 0 || nextInputs.get(nextInputs.size() - 1).getMinCount() == 0) {
                            for (final InputLine input : getFirstLines()) {
                                final Matcher matcher = input.getPattern().matcher(line);
                                if (matcher.find()) {
                                    context.setLocation(new TmfLongLocation(rawPos));
                                    context.firstLineMatcher = matcher;
                                    context.firstLine = line;
                                    context.nextLineLocation = fFile.getFilePointer();
                                    context.inputLine = input;
                                    return event;
                                }
                            }
                        }
                        for (final InputLine input : nextInputs) {
                            final Matcher matcher = input.getPattern().matcher(line);
                            if (matcher.find()) {
                                event.processGroups(input, matcher);
                                currentInput = input;
                                if (countMap.get(currentInput) == null) {
                                    countMap.put(currentInput, 1);
                                } else {
                                    countMap.put(currentInput, countMap.get(currentInput) + 1);
                                }
                                Iterator<InputLine> iter = countMap.keySet().iterator();
                                while (iter.hasNext()) {
                                    final InputLine inputLine = iter.next();
                                    if (inputLine.level > currentInput.level) {
                                        iter.remove();
                                    }
                                }
                                if (currentInput.childrenInputs != null && currentInput.childrenInputs.size() > 0) {
                                    currentInput = currentInput.childrenInputs.get(0);
                                    countMap.put(currentInput, 0);
                                } else if (countMap.get(currentInput) >= currentInput.getMaxCount()) {
                                    if (currentInput.getNextInputs(countMap).size() > 0) {
                                        currentInput = currentInput.getNextInputs(countMap).get(0);
                                        if (countMap.get(currentInput) == null) {
                                            countMap.put(currentInput, 0);
                                        }
                                        iter = countMap.keySet().iterator();
                                        while (iter.hasNext()) {
                                            final InputLine inputLine = iter.next();
                                            if (inputLine.level > currentInput.level) {
                                                iter.remove();
                                            }
                                        }
                                    } else {
                                        currentInput = null;
                                    }
                                }
                                processed = true;
                                break;
                            }
                        }
                    }
                    if (!processed && currentInput != null) {
                        final Matcher matcher = currentInput.getPattern().matcher(line);
                        if (matcher.find()) {
                            event.processGroups(currentInput, matcher);
                            countMap.put(currentInput, countMap.get(currentInput) + 1);
                            if (currentInput.childrenInputs != null && currentInput.childrenInputs.size() > 0) {
                                currentInput = currentInput.childrenInputs.get(0);
                                countMap.put(currentInput, 0);
                            } else if (countMap.get(currentInput) >= currentInput.getMaxCount()) {
                                if (currentInput.getNextInputs(countMap).size() > 0) {
                                    currentInput = currentInput.getNextInputs(countMap).get(0);
                                    if (countMap.get(currentInput) == null) {
                                        countMap.put(currentInput, 0);
                                    }
                                    final Iterator<InputLine> iter = countMap.keySet().iterator();
                                    while (iter.hasNext()) {
                                        final InputLine inputLine = iter.next();
                                        if (inputLine.level > currentInput.level) {
                                            iter.remove();
                                        }
                                    }
                                } else {
                                    currentInput = null;
                                }
                            }
                        }
                        ((StringBuffer) event.getContent().getValue()).append("\n").append(line); //$NON-NLS-1$
                    }
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
        }
        for (final Entry<InputLine, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() < entry.getKey().getMinCount()) {
                event = null;
            }
        }
        context.setLocation(NULL_LOCATION);
        return event;
    }

    /**
     * @return The first few lines of the text file
     */
    public List<InputLine> getFirstLines() {
        return fDefinition.inputs;
    }

    /**
     * Parse the first line of the trace (to recognize the type).
     *
     * @param context
     *            Trace context
     * @return The first event
     */
    public CustomTxtEvent parseFirstLine(final CustomTxtTraceContext context) {
        final CustomTxtEvent event = new CustomTxtEvent(fDefinition, this, TmfTimestamp.ZERO, "", fEventType, ""); //$NON-NLS-1$ //$NON-NLS-2$
        event.processGroups(context.inputLine, context.firstLineMatcher);
        event.setContent(new CustomEventContent(event, new StringBuffer(context.firstLine)));
        return event;
    }

    /**
     * Get the trace definition.
     *
     * @return The trace definition
     */
    public CustomTraceDefinition getDefinition() {
        return fDefinition;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation computes the confidence as the percentage of
     * lines in the first 100 lines of the file which match any of the root
     * input line patterns.
     */
    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CustomTrace_FileNotFound + ": " + path); //$NON-NLS-1$
        }
        int confidence = 0;
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            int matches = 0;
            String line = rafile.getNextLine();
            while ((line != null) && (lineCount++ < MAX_LINES)) {
                for (InputLine inputLine : fDefinition.inputs) {
                    Matcher matcher = inputLine.getPattern().matcher(line);
                    if (matcher.find()) {
                        matches++;
                        break;
                    }
                }
                confidence = MAX_CONFIDENCE * matches / lineCount;
                line = rafile.getNextLine();
            }
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);
    }

    private static int fCheckpointSize = -1;

    @Override
    public synchronized int getCheckpointSize() {
        if (fCheckpointSize == -1) {
            TmfCheckpoint c = new TmfCheckpoint(TmfTimestamp.ZERO, new TmfLongLocation(0L), 0);
            ByteBuffer b = ByteBuffer.allocate(ITmfCheckpoint.MAX_SERIALIZE_SIZE);
            b.clear();
            c.serialize(b);
            fCheckpointSize = b.position();
        }

        return fCheckpointSize;
    }

    @Override
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new TmfLongLocation(bufferIn);
    }

    @Override
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfBTreeTraceIndexer(this, interval);
    }
}
