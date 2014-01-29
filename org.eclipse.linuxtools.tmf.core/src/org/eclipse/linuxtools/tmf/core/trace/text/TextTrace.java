/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.text;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;

/**
 * Extension of TmfTrace for handling of line-based text traces parsed using
 * regular expressions. Each line that matches the first line pattern indicates
 * the start of a new event. The subsequent lines can contain additional
 * information that is added to the current event.
 *
 * @param <T>
 *            TmfEvent class returned by this trace
 *
 * @since 3.0
 */
public abstract class TextTrace<T extends TmfEvent> extends TmfTrace implements ITmfEventParser {

    private static final TmfLongLocation NULL_LOCATION = new TmfLongLocation((Long) null);
    private static final int MAX_LINES = 100;
    private static final int MAX_CONFIDENCE = 100;

    /** The default separator used for multi-line fields */
    protected static final String SEPARATOR = " | "; //$NON-NLS-1$

    /** The text file */
    protected BufferedRandomAccessFile fFile;

    /**
     * Constructor
     */
    public TextTrace() {
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation computes the confidence as the percentage of
     * lines in the first 100 lines of the file which match the first line
     * pattern.
     */
    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "File not found: " + path); //$NON-NLS-1$
        }
        if (!file.isFile()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Not a file. It's a directory: " + path); //$NON-NLS-1$
        }
        int confidence = 0;
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            int matches = 0;
            String line = rafile.getNextLine();
            while ((line != null) && (lineCount++ < MAX_LINES)) {
                Matcher matcher = getFirstLinePattern().matcher(line);
                if (matcher.find()) {
                    matches++;
                }
                confidence = MAX_CONFIDENCE * matches / lineCount;
                line = rafile.getNextLine();
            }
        } catch (IOException e) {
            Activator.logError("Error validating file: " + path, e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }

        return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);

    }

    @Override
    public void initTrace(IResource resource, String path, Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initTrace(resource, path, type);
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
    public synchronized TmfContext seekEvent(ITmfLocation location) {
        TextTraceContext context = new TextTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
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
                Matcher matcher = getFirstLinePattern().matcher(line);
                if (matcher.find()) {
                    context.setLocation(new TmfLongLocation(rawPos));
                    context.firstLineMatcher = matcher;
                    context.firstLine = line;
                    context.nextLineLocation = fFile.getFilePointer();
                    return context;
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
            return context;
        } catch (IOException e) {
            Activator.logError("Error seeking file: " + getPath(), e); //$NON-NLS-1$
            return context;
        }
    }

    @Override
    public synchronized TmfContext seekEvent(double ratio) {
        if (fFile == null) {
            return new TextTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
        try {
            long pos = (long) (ratio * fFile.length());
            while (pos > 0) {
                fFile.seek(pos - 1);
                if (fFile.read() == '\n') {
                    break;
                }
                pos--;
            }
            ITmfLocation location = new TmfLongLocation(Long.valueOf(pos));
            TmfContext context = seekEvent(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (IOException e) {
            Activator.logError("Error seeking file: " + getPath(), e); //$NON-NLS-1$
            return new TextTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        if (fFile == null) {
            return 0;
        }
        try {
            long length = fFile.length();
            if (length == 0) {
                return 0;
            }
            if (location.getLocationInfo() instanceof Long) {
                return (double) ((Long) location.getLocationInfo()) / length;
            }
        } catch (IOException e) {
            Activator.logError("Error reading file: " + getPath(), e); //$NON-NLS-1$
        }
        return 0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        return null;
    }

    @Override
    public ITmfEvent parseEvent(ITmfContext tmfContext) {
        ITmfContext context = seekEvent(tmfContext.getLocation());
        return parse(context);
    }

    @Override
    public synchronized T getNext(ITmfContext context) {
        ITmfContext savedContext = new TmfContext(context.getLocation(), context.getRank());
        T event = parse(context);
        if (event != null) {
            updateAttributes(savedContext, event.getTimestamp());
            context.increaseRank();
        }
        return event;
    }

    /**
     * Parse the next event. The context is advanced.
     *
     * @param tmfContext
     *            the context
     * @return the next event or null
     */
    protected synchronized T parse(ITmfContext tmfContext) {
        if (fFile == null) {
            return null;
        }
        if (!(tmfContext instanceof TextTraceContext)) {
            return null;
        }
        TextTraceContext context = (TextTraceContext) tmfContext;
        if (context.getLocation() == null || !(context.getLocation().getLocationInfo() instanceof Long) || NULL_LOCATION.equals(context.getLocation())) {
            return null;
        }

        T event = parseFirstLine(context.firstLineMatcher, context.firstLine);

        try {
            if (fFile.getFilePointer() != context.nextLineLocation) {
                fFile.seek(context.nextLineLocation);
            }
            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                Matcher matcher = getFirstLinePattern().matcher(line);
                if (matcher.find()) {
                    context.setLocation(new TmfLongLocation(rawPos));
                    context.firstLineMatcher = matcher;
                    context.firstLine = line;
                    context.nextLineLocation = fFile.getFilePointer();
                    return event;
                }
                parseNextLine(event, line);
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
        } catch (IOException e) {
            Activator.logError("Error reading file: " + getPath(), e); //$NON-NLS-1$
        }

        context.setLocation(NULL_LOCATION);
        return event;
    }

    /**
     * Gets the first line pattern.
     *
     * @return The first line pattern
     */
    protected abstract Pattern getFirstLinePattern();

    /**
     * Parses the first line data and returns a new event.
     *
     * @param matcher
     *            The matcher
     * @param line
     *            The line to parse
     * @return The parsed event
     */
    protected abstract T parseFirstLine(Matcher matcher, String line);

    /**
     * Parses the next line data for the current event.
     *
     * @param event
     *            The current event being parsed
     * @param line
     *            The line to parse
     */
    protected abstract void parseNextLine(T event, String line);

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Strip quotes surrounding a string
     *
     * @param input
     *            The input string
     * @return The string without quotes
     */
    protected static String replaceQuotes(String input) {
        String out = input.replaceAll("^\"|(\"\\s*)$", "");  //$NON-NLS-1$//$NON-NLS-2$
        return out;
    }

    /**
     * Strip brackets surrounding a string
     *
     * @param input
     *            The input string
     * @return The string without brackets
     */
    protected static String replaceBrackets(String input) {
        String out = input.replaceAll("^\\{|(\\}\\s*)$", "");  //$NON-NLS-1$//$NON-NLS-2$
        return out;
    }

}
