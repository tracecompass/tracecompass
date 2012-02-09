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

package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTxtTraceDefinition.InputLine;

public class CustomTxtTrace extends TmfTrace<CustomTxtEvent> {

    private static final TmfLocation<Long> NULL_LOCATION = new TmfLocation<Long>((Long) null);
    
    private CustomTxtTraceDefinition fDefinition;
    private CustomTxtEventType fEventType;

    public CustomTxtTrace(CustomTxtTraceDefinition definition) {
        fDefinition = definition;
        fEventType = new CustomTxtEventType(fDefinition);
    }

    public CustomTxtTrace(String name, CustomTxtTraceDefinition definition, String path, int cacheSize) throws FileNotFoundException {
        super(name, CustomTxtEvent.class, path, cacheSize);
        fDefinition = definition;
        fEventType = new CustomTxtEventType(fDefinition);
    }

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ITmfTrace copy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TmfContext seekLocation(ITmfLocation<?> location) {
        //System.out.println(Thread.currentThread().getName() + "::" + getName() + " seekLocation(" + ((location == null || location.getLocation() == null) ? "null" : location) + ")");
        //new Throwable().printStackTrace();
        CustomTxtTraceContext context = new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.INITIAL_RANK);
        if (NULL_LOCATION.equals(location) || !new File(getPath()).isFile()) {
            return context;
        }
        try {
            BufferedRandomAccessFile raFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
            if (location != null && location.getLocation() instanceof Long) {
                raFile.seek((Long)location.getLocation());
            }
            String line;
            long rawPos = raFile.getFilePointer();
            while ((line = raFile.getNextLine()) != null) {
                for (InputLine input : getFirstLines()) {
                    Matcher matcher = input.getPattern().matcher(line);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return context;
        } catch (IOException e) {
            e.printStackTrace();
            return context;
        }
        
    }

    @Override
    public TmfContext seekLocation(double ratio) {
        try {
            BufferedRandomAccessFile raFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
            long pos = (long) (ratio * raFile.length());
            while (pos > 0) {
                raFile.seek(pos - 1);
                if (raFile.read() == '\n') break;
                pos--;
            }
            ITmfLocation<?> location = new TmfLocation<Long>(new Long(pos));
            TmfContext context = seekLocation(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.INITIAL_RANK);
        } catch (IOException e) {
            e.printStackTrace();
            return new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.INITIAL_RANK);
        }
    }

    @Override
    public double getLocationRatio(ITmfLocation<?> location) {
        try {
            if (location.getLocation() instanceof Long) {
            	BufferedRandomAccessFile raFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
                return (double) ((Long) location.getLocation()) / raFile.length();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public ITmfLocation<?> getCurrentLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized TmfEvent getNextEvent(TmfContext context) {
        ITmfContext savedContext = context.clone();
        TmfEvent event = parseEvent(context);
        if (event != null) {
            updateIndex(savedContext, savedContext.getRank(), event.getTimestamp());
            context.updateRank(1);
        }
        return event;
    }

    @Override
    public TmfEvent parseEvent(TmfContext tmfContext) {
        //System.out.println(Thread.currentThread().getName() + ":: " + getName() + " parseEvent(" + tmfContext.getRank() + " @ " + (tmfContext.getLocation().getLocation() == null ? "null" : tmfContext.getLocation()));
        if (!(tmfContext instanceof CustomTxtTraceContext)) {
            return null;
        }
        
        CustomTxtTraceContext context = (CustomTxtTraceContext) tmfContext;
        if (!(context.getLocation().getLocation() instanceof Long) || NULL_LOCATION.equals(context.getLocation())) {
            return null;
        }

        CustomTxtEvent event = parseFirstLine(context);

        HashMap<InputLine, Integer> countMap = new HashMap<InputLine, Integer>();
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
                        for (InputLine input : getFirstLines()) {
                            Matcher matcher = input.getPattern().matcher(line);
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
                            List<InputLine> nextInputs = currentInput.getNextInputs(countMap);
                            if (nextInputs.size() == 0 || nextInputs.get(nextInputs.size() - 1).getMinCount() == 0) {
                                for (InputLine input : getFirstLines()) {
                                    Matcher matcher = input.getPattern().matcher(line);
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
                            for (InputLine input : nextInputs) {
                                Matcher matcher = input.getPattern().matcher(line);
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
                                        InputLine inputLine = iter.next();
                                        if (inputLine.level > currentInput.level) {
                                            iter.remove();
                                        }
                                    }
                                    if (currentInput.childrenInputs != null && currentInput.childrenInputs.size() > 0) {
                                        currentInput = currentInput.childrenInputs.get(0);
                                        countMap.put(currentInput, 0);
                                    } else {
                                        if (countMap.get(currentInput) >= currentInput.getMaxCount()) {
                                            if (currentInput.getNextInputs(countMap).size() > 0) {
                                                currentInput = currentInput.getNextInputs(countMap).get(0);
                                                if (countMap.get(currentInput) == null) {
                                                    countMap.put(currentInput, 0);
                                                }
                                                iter = countMap.keySet().iterator();
                                                while (iter.hasNext()) {
                                                    InputLine inputLine = iter.next();
                                                    if (inputLine.level > currentInput.level) {
                                                        iter.remove();
                                                    }
                                                }
                                            } else {
                                                currentInput = null;
                                            }
                                        }
                                    }
                                    processed = true;
                                    break;
                                }
                            }
                        }
                        if (! processed) {
                            Matcher matcher = currentInput.getPattern().matcher(line);
                            if (matcher.find()) {
                                event.processGroups(currentInput, matcher);
                                countMap.put(currentInput, countMap.get(currentInput) + 1);
                                if (currentInput.childrenInputs != null && currentInput.childrenInputs.size() > 0) {
                                    currentInput = currentInput.childrenInputs.get(0);
                                    countMap.put(currentInput, 0);
                                } else {
                                    if (countMap.get(currentInput) >= currentInput.getMaxCount()) {
                                        if (currentInput.getNextInputs(countMap).size() > 0) {
                                            currentInput = currentInput.getNextInputs(countMap).get(0);
                                            if (countMap.get(currentInput) == null) {
                                                countMap.put(currentInput, 0);
                                            }
                                            Iterator<InputLine> iter = countMap.keySet().iterator();
                                            while (iter.hasNext()) {
                                                InputLine inputLine = iter.next();
                                                if (inputLine.level > currentInput.level) {
                                                    iter.remove();
                                                }
                                            }
                                        } else {
                                            currentInput = null;
                                        }
                                    }
                                }
                            }
                            ((StringBuffer) event.getContent().getContent()).append("\n").append(line); //$NON-NLS-1$
                        }
                    }
                    rawPos = context.raFile.getFilePointer();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(Entry<InputLine, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() < entry.getKey().getMinCount()) {
                event = null;
            }
        }
        context.setLocation(NULL_LOCATION);
        return event;
    }

    public List<InputLine> getFirstLines() {
        return fDefinition.inputs;
    }
    
    public CustomTxtEvent parseFirstLine(CustomTxtTraceContext context) {
        CustomTxtEvent event = new CustomTxtEvent(fDefinition, this, TmfTimestamp.Zero, "", fEventType, ""); //$NON-NLS-1$ //$NON-NLS-2$
        event.processGroups(context.inputLine, context.firstLineMatcher);
        event.setContent(new CustomEventContent(event, new StringBuffer(context.firstLine)));
        return event;
    }
    
    public CustomTraceDefinition getDefinition() {
        return fDefinition;
    }
}
