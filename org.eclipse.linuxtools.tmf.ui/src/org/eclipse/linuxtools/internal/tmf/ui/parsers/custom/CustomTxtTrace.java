/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTraceDefinition.InputLine;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

public class CustomTxtTrace extends TmfTrace<CustomTxtEvent> implements ITmfEventParser<CustomTxtEvent> {

    private static final TmfLocation<Long> NULL_LOCATION = new TmfLocation<Long>((Long) null);
    private static final int DEFAULT_CACHE_SIZE = 100;

    private final CustomTxtTraceDefinition fDefinition;
    private final CustomTxtEventType fEventType;

    public CustomTxtTrace(final CustomTxtTraceDefinition definition) {
        fDefinition = definition;
        fEventType = new CustomTxtEventType(fDefinition);
    }

    public CustomTxtTrace(final IResource resource, final CustomTxtTraceDefinition definition, final String path, final int pageSize) throws TmfTraceException {
        super(resource, CustomTxtEvent.class, path, (pageSize > 0) ? pageSize : DEFAULT_CACHE_SIZE);
        fDefinition = definition;
        fEventType = new CustomTxtEventType(fDefinition);
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<CustomTxtEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);
    }

    @Override
    public TmfContext seekEvent(final ITmfLocation<?> location) {
        final CustomTxtTraceContext context = new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        if (NULL_LOCATION.equals(location) || !new File(getPath()).isFile())
            return context;
        try {
            BufferedRandomAccessFile raFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
            if (location != null && location.getLocation() instanceof Long) {
                raFile.seek((Long)location.getLocation());
            }
            String line;
            long rawPos = raFile.getFilePointer();
            while ((line = raFile.getNextLine()) != null) {
                for (final InputLine input : getFirstLines()) {
                    final Matcher matcher = input.getPattern().matcher(line);
                    if (matcher.find()) {
                        context.setLocation(new TmfLocation<Long>(rawPos));
                        context.raFile = raFile;
                        context.firstLineMatcher = matcher;
                        context.firstLine = line;
                        context.nextLineLocation = raFile.getFilePointer();
                        context.inputLine = input;
                        return context;
                    }
                }
                rawPos = raFile.getFilePointer();
            }
            return context;
        } catch (final FileNotFoundException e) {
            TmfUiPlugin.getDefault().logError("Error seeking event. File not found: " + getPath(), e); //$NON-NLS-1$
            return context;
        } catch (final IOException e) {
            TmfUiPlugin.getDefault().logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return context;
        }

    }

    @Override
    public TmfContext seekEvent(final double ratio) {
        BufferedRandomAccessFile raFile = null;
        try {
            raFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
            long pos = (long) (ratio * raFile.length());
            while (pos > 0) {
                raFile.seek(pos - 1);
                if (raFile.read() == '\n') {
                    break;
                }
                pos--;
            }
            final ITmfLocation<?> location = new TmfLocation<Long>(pos);
            final TmfContext context = seekEvent(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (final FileNotFoundException e) {
            TmfUiPlugin.getDefault().logError("Error seeking event. File not found: " + getPath(), e); //$NON-NLS-1$
            return new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        } catch (final IOException e) {
            TmfUiPlugin.getDefault().logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        } finally {
            if (raFile != null) {
                try {
                    raFile.close();
                } catch (final IOException e) {
                }
            }
        }
    }

    @Override
    public double getLocationRatio(final ITmfLocation<?> location) {
        BufferedRandomAccessFile raFile = null;
        try {
            if (location.getLocation() instanceof Long) {
                raFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
                return (double) ((Long) location.getLocation()) / raFile.length();
            }
        } catch (final FileNotFoundException e) {
            TmfUiPlugin.getDefault().logError("Error seeking event. File not found: " + getPath(), e); //$NON-NLS-1$
        } catch (final IOException e) {
            TmfUiPlugin.getDefault().logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
        } finally {
            if (raFile != null) {
                try {
                    raFile.close();
                } catch (final IOException e) {
                }
            }
        }
        return 0;
    }

    @Override
    public ITmfLocation<?> getCurrentLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized CustomTxtEvent getNext(final ITmfContext context) {
        final ITmfContext savedContext = context.clone();
        final CustomTxtEvent event = parseEvent(context);
        if (event != null) {
            updateAttributes(savedContext, event.getTimestamp());
            context.increaseRank();
        }
        return event;
    }

    @Override
    public CustomTxtEvent parseEvent(final ITmfContext tmfContext) {
        if (!(tmfContext instanceof CustomTxtTraceContext))
            return null;

        final CustomTxtTraceContext context = (CustomTxtTraceContext) tmfContext;
        if (!(context.getLocation().getLocation() instanceof Long) || NULL_LOCATION.equals(context.getLocation()))
            return null;

        CustomTxtEvent event = parseFirstLine(context);

        final HashMap<InputLine, Integer> countMap = new HashMap<InputLine, Integer>();
        InputLine currentInput = null;
        if (context.inputLine.childrenInputs != null && context.inputLine.childrenInputs.size() > 0) {
            currentInput = context.inputLine.childrenInputs.get(0);
            countMap.put(currentInput, 0);
        }

        synchronized (context.raFile) {
            try {
                if (context.raFile.getFilePointer() != context.nextLineLocation) {
                    context.raFile.seek(context.nextLineLocation);
                }
                String line;
                long rawPos = context.raFile.getFilePointer();
                while ((line = context.raFile.getNextLine()) != null) {
                    boolean processed = false;
                    if (currentInput == null) {
                        for (final InputLine input : getFirstLines()) {
                            final Matcher matcher = input.getPattern().matcher(line);
                            if (matcher.find()) {
                                context.setLocation(new TmfLocation<Long>(rawPos));
                                context.firstLineMatcher = matcher;
                                context.firstLine = line;
                                context.nextLineLocation = context.raFile.getFilePointer();
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
                                        context.setLocation(new TmfLocation<Long>(rawPos));
                                        context.firstLineMatcher = matcher;
                                        context.firstLine = line;
                                        context.nextLineLocation = context.raFile.getFilePointer();
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
                                    } else if (countMap.get(currentInput) >= currentInput.getMaxCount())
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
                                    processed = true;
                                    break;
                                }
                            }
                        }
                        if (! processed) {
                            final Matcher matcher = currentInput.getPattern().matcher(line);
                            if (matcher.find()) {
                                event.processGroups(currentInput, matcher);
                                countMap.put(currentInput, countMap.get(currentInput) + 1);
                                if (currentInput.childrenInputs != null && currentInput.childrenInputs.size() > 0) {
                                    currentInput = currentInput.childrenInputs.get(0);
                                    countMap.put(currentInput, 0);
                                } else if (countMap.get(currentInput) >= currentInput.getMaxCount())
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
                            ((StringBuffer) event.getContent().getValue()).append("\n").append(line); //$NON-NLS-1$
                        }
                    }
                    rawPos = context.raFile.getFilePointer();
                }
            } catch (final IOException e) {
                TmfUiPlugin.getDefault().logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            }
        }
        for(final Entry<InputLine, Integer> entry : countMap.entrySet())
            if (entry.getValue() < entry.getKey().getMinCount()) {
                event = null;
            }
        context.setLocation(NULL_LOCATION);
        return event;
    }

    public List<InputLine> getFirstLines() {
        return fDefinition.inputs;
    }

    public CustomTxtEvent parseFirstLine(final CustomTxtTraceContext context) {
        final CustomTxtEvent event = new CustomTxtEvent(fDefinition, this, TmfTimestamp.ZERO, "", fEventType, ""); //$NON-NLS-1$ //$NON-NLS-2$
        event.processGroups(context.inputLine, context.firstLineMatcher);
        event.setContent(new CustomEventContent(event, context.firstLine));
        return event;
    }

    public CustomTraceDefinition getDefinition() {
        return fDefinition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#validate(org.eclipse.core.resources.IProject, java.lang.String)
     */
    @Override
    public boolean validate(IProject project, String path) {
        return fileExists(path);
    }
}
