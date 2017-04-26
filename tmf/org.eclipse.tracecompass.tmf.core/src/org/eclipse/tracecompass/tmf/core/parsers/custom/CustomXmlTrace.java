/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Add trace type id handling
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.parsers.custom.CustomEventAspects;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Trace object for custom XML trace parsers.
 *
 * @author Patrick Tassé
 */
public class CustomXmlTrace extends TmfTrace implements ITmfPersistentlyIndexable {

    private static final TmfLongLocation NULL_LOCATION = new TmfLongLocation(-1L);
    private static final int DEFAULT_CACHE_SIZE = 100;
    private static final int MAX_LINES = 100;
    private static final int CONFIDENCE = 100;

    private final CustomXmlTraceDefinition fDefinition;
    private final ITmfEventField fRootField;
    private final CustomXmlInputElement fRecordInputElement;
    private BufferedRandomAccessFile fFile;
    private final @NonNull String fTraceTypeId;

    private static final char SEPARATOR = ':';
    private static final String CUSTOM_XML_TRACE_TYPE_PREFIX = "custom.xml.trace" + SEPARATOR; //$NON-NLS-1$
    private static final String LINUX_TOOLS_CUSTOM_XML_TRACE_TYPE_PREFIX = "org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTrace" + SEPARATOR; //$NON-NLS-1$
    private static final String EARLY_TRACE_COMPASS_CUSTOM_XML_TRACE_TYPE_PREFIX = "org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace" + SEPARATOR; //$NON-NLS-1$

    /**
     * Basic constructor
     *
     * @param definition
     *            Trace definition
     */
    public CustomXmlTrace(final CustomXmlTraceDefinition definition) {
        fDefinition = definition;
        fRootField = CustomEventType.getRootField(definition);
        fRecordInputElement = getRecordInputElement(fDefinition.rootInputElement);
        fTraceTypeId = buildTraceTypeId(definition.categoryName, definition.definitionName);
        setCacheSize(DEFAULT_CACHE_SIZE);
    }

    /**
     * Full constructor
     *
     * @param resource
     *            Trace resource
     * @param definition
     *            Trace definition
     * @param path
     *            Path to the trace/log file
     * @param pageSize
     *            Page size to use
     * @throws TmfTraceException
     *             If the trace/log couldn't be opened
     */
    public CustomXmlTrace(final IResource resource,
            final CustomXmlTraceDefinition definition, final String path,
            final int pageSize) throws TmfTraceException {
        this(definition);
        setCacheSize((pageSize > 0) ? pageSize : DEFAULT_CACHE_SIZE);
        initTrace(resource, path, CustomXmlEvent.class);
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);
        initFile();
    }

    private void initFile() throws TmfTraceException {
        closeFile();
        try {
            fFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        closeFile();
    }

    private void closeFile() {
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
    public Iterable<ITmfEventAspect<?>> getEventAspects() {
        return CustomEventAspects.generateAspects(fDefinition);
    }

    @Override
    public synchronized TmfContext seekEvent(final ITmfLocation location) {
        final CustomXmlTraceContext context = new CustomXmlTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
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
                final int idx = indexOfElement(fRecordInputElement.getElementName(), line, 0);
                if (idx != -1) {
                    context.setLocation(new TmfLongLocation(rawPos + idx));
                    return context;
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
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
            return new CustomXmlTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
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
            Activator.logError("Error getting location ration. File: " + getPath(), e); //$NON-NLS-1$
        }
        return 0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized CustomXmlEvent parseEvent(final ITmfContext tmfContext) {
        ITmfContext context = seekEvent(tmfContext.getLocation());
        return parse(context);
    }

    @Override
    public synchronized CustomXmlEvent getNext(final ITmfContext context) {
        final ITmfContext savedContext = new TmfContext(context.getLocation(), context.getRank());
        final CustomXmlEvent event = parse(context);
        if (event != null) {
            updateAttributes(savedContext, event);
            context.increaseRank();
        }
        return event;
    }

    private synchronized CustomXmlEvent parse(final ITmfContext tmfContext) {
        if (fFile == null) {
            return null;
        }
        if (!(tmfContext instanceof CustomXmlTraceContext)) {
            return null;
        }

        final CustomXmlTraceContext context = (CustomXmlTraceContext) tmfContext;
        ITmfLocation location = context.getLocation();
        if (location == null || !(location.getLocationInfo() instanceof Long) || NULL_LOCATION.equals(location)) {
            return null;
        }

        CustomXmlEvent event = null;
        try {
            // Below +1 for the <
            if (fFile.getFilePointer() != (Long) location.getLocationInfo() + 1) {
                fFile.seek((Long) location.getLocationInfo() + 1);
            }
            final StringBuffer elementBuffer = new StringBuffer("<"); //$NON-NLS-1$
            readElement(elementBuffer, fFile);
            final Element element = parseElementBuffer(elementBuffer);

            event = extractEvent(element, fRecordInputElement);
            ((StringBuffer) event.getContentValue()).append(elementBuffer);

            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                final int idx = indexOfElement(fRecordInputElement.getElementName(), line, 0);
                if (idx != -1) {
                    context.setLocation(new TmfLongLocation(rawPos + idx));
                    return event;
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
        } catch (final IOException e) {
            Activator.logError("Error parsing event. File: " + getPath(), e); //$NON-NLS-1$

        }
        context.setLocation(NULL_LOCATION);
        return event;
    }

    private Element parseElementBuffer(final StringBuffer elementBuffer) {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            final EntityResolver resolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(final String publicId, final String systemId) {
                    final String empty = ""; //$NON-NLS-1$
                    final ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
                    return new InputSource(bais);
                }
            };
            db.setEntityResolver(resolver);

            // The following catches xml parsing exceptions
            db.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(final SAXParseException saxparseexception) throws SAXException {
                }

                @Override
                public void warning(final SAXParseException saxparseexception) throws SAXException {
                }

                @Override
                public void fatalError(final SAXParseException saxparseexception) throws SAXException {
                    throw saxparseexception;
                }
            });

            final Document doc = db.parse(new ByteArrayInputStream(elementBuffer.toString().getBytes()));
            return doc.getDocumentElement();
        } catch (final ParserConfigurationException e) {
            Activator.logError("Error parsing element buffer. File:" + getPath(), e); //$NON-NLS-1$
        } catch (final SAXException e) {
            Activator.logError("Error parsing element buffer. File:" + getPath(), e); //$NON-NLS-1$
        } catch (final IOException e) {
            Activator.logError("Error parsing element buffer. File: " + getPath(), e); //$NON-NLS-1$
        }
        return null;
    }

    private static int indexOfElement(String elementName, String line, int fromIndex) {
        final String recordElementStart = '<' + elementName;
        int index = line.indexOf(recordElementStart, fromIndex);
        if (index == -1) {
            return index;
        }
        int nextCharIndex = index + recordElementStart.length();
        if (nextCharIndex < line.length()) {
            char c = line.charAt(nextCharIndex);
            // Check that the match is not just a substring of another element
            if (Character.isLetterOrDigit(c)) {
                return indexOfElement(elementName, line, nextCharIndex);
            }
        }
        return index;
    }

    private void readElement(final StringBuffer buffer, final RandomAccessFile raFile) {
        try {
            int numRead = 0;
            boolean startTagClosed = false;
            int i;
            while ((i = raFile.read()) != -1) {
                numRead++;
                final char c = (char) i;
                buffer.append(c);
                if (c == '"') {
                    readQuote(buffer, raFile, '"');
                } else if (c == '\'') {
                    readQuote(buffer, raFile, '\'');
                } else if (c == '<') {
                    readElement(buffer, raFile);
                } else if (c == '/' && numRead == 1) {
                    break; // found "</"
                } else if (c == '-' && numRead == 3 && buffer.substring(buffer.length() - 3, buffer.length() - 1).equals("!-")) { //$NON-NLS-1$
                    readComment(buffer, raFile); // found "<!--"
                } else if (i == '>') {
                    if (buffer.charAt(buffer.length() - 2) == '/') {
                        break; // found "/>"
                    } else if (startTagClosed) {
                        break; // found "<...>...</...>"
                    }
                    else {
                        startTagClosed = true; // found "<...>"
                    }
                }
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    private static void readQuote(final StringBuffer buffer,
            final RandomAccessFile raFile, final char eq) {
        try {
            int i;
            while ((i = raFile.read()) != -1) {
                final char c = (char) i;
                buffer.append(c);
                if (c == eq)
                {
                    break; // found matching end-quote
                }
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    private static void readComment(final StringBuffer buffer,
            final RandomAccessFile raFile) {
        try {
            int numRead = 0;
            int i;
            while ((i = raFile.read()) != -1) {
                numRead++;
                final char c = (char) i;
                buffer.append(c);
                if (c == '>' && numRead >= 2 && buffer.substring(buffer.length() - 3, buffer.length() - 1).equals("--")) //$NON-NLS-1$
                {
                    break; // found "-->"
                }
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    /**
     * Parse an XML element.
     *
     * @param parentElement
     *            The parent element
     * @param buffer
     *            The contents to parse
     * @return The parsed content
     */
    public static StringBuffer parseElement(final Element parentElement, final StringBuffer buffer) {
        final NodeList nodeList = parentElement.getChildNodes();
        String separator = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            short nodeType = node.getNodeType();
            if (nodeType == Node.ELEMENT_NODE) {
                if (separator == null) {
                    separator = " | "; //$NON-NLS-1$
                } else {
                    buffer.append(separator);
                }
                final Element element = (Element) node;
                if (!element.hasChildNodes()) {
                    buffer.append(element.getNodeName());
                } else if (element.getChildNodes().getLength() == 1 && element.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    buffer.append(element.getNodeName());
                    buffer.append(':');
                    buffer.append(element.getFirstChild().getNodeValue().trim());
                } else {
                    buffer.append(element.getNodeName());
                    buffer.append(" [ "); //$NON-NLS-1$
                    parseElement(element, buffer);
                    buffer.append(" ]"); //$NON-NLS-1$
                }
            } else if ((nodeType == Node.TEXT_NODE) && (!node.getNodeValue().trim().isEmpty())) {
                buffer.append(node.getNodeValue().trim());
            }
        }
        return buffer;
    }

    /**
     * Get an input element if it is a valid record input. If not, we will look
     * into its children for valid inputs.
     *
     * @param inputElement
     *            The main element to check for.
     * @return The record element
     */
    public CustomXmlInputElement getRecordInputElement(final CustomXmlInputElement inputElement) {
        if (inputElement.isLogEntry()) {
            return inputElement;
        } else if (inputElement.getChildElements() != null) {
            for (final CustomXmlInputElement childInputElement : inputElement.getChildElements()) {
                final CustomXmlInputElement recordInputElement = getRecordInputElement(childInputElement);
                if (recordInputElement != null) {
                    return recordInputElement;
                }
            }
        }
        return null;
    }

    /**
     * Extract a trace event from an XML element.
     *
     * @param element
     *            The element
     * @param inputElement
     *            The input element
     * @return The extracted event
     */
    public CustomXmlEvent extractEvent(final Element element, final CustomXmlInputElement inputElement) {
        CustomXmlEventType eventType = new CustomXmlEventType(checkNotNull(fDefinition.definitionName), fRootField);
        final CustomXmlEvent event = new CustomXmlEvent(fDefinition, this, TmfTimestamp.ZERO, eventType);
        event.setContent(new CustomEventContent(event, new StringBuffer()));
        parseElement(element, event, inputElement);
        return event;
    }

    private void parseElement(final Element element, final CustomXmlEvent event, final CustomXmlInputElement inputElement) {
        String eventType = inputElement.getEventType();
        if (eventType != null && event.getType() instanceof CustomEventType) {
            ((CustomEventType) event.getType()).setName(eventType);
        }
        if (!inputElement.getInputTag().equals(Tag.IGNORE)) {
            event.parseInput(parseElement(element, new StringBuffer()).toString(), inputElement.getInputTag(), inputElement.getInputName(), inputElement.getInputAction(), inputElement.getInputFormat());
        }
        if (inputElement.getAttributes() != null) {
            for (final CustomXmlInputAttribute attribute : inputElement.getAttributes()) {
                event.parseInput(element.getAttribute(attribute.getAttributeName()), attribute.getInputTag(), attribute.getInputName(), attribute.getInputAction(), attribute.getInputFormat());
            }
        }
        final NodeList childNodes = element.getChildNodes();
        if (inputElement.getChildElements() != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node node = childNodes.item(i);
                if (node instanceof Element) {
                    for (final CustomXmlInputElement child : inputElement.getChildElements()) {
                        if (node.getNodeName().equals(child.getElementName())) {
                            parseElement((Element) node, event, child);
                            break;
                        }
                    }
                }
            }
        }
        return;
    }

    /**
     * Retrieve the trace definition.
     *
     * @return The trace definition
     */
    public CustomTraceDefinition getDefinition() {
        return fDefinition;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation sets the confidence to 100 if any of the first
     * 100 lines of the file contains a valid record input element, and 0
     * otherwise.
     */
    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CustomTrace_FileNotFound + ": " + path); //$NON-NLS-1$
        }
        try {
            if (!TmfTraceUtils.isText(file)) {
                return new TraceValidationStatus(0, Activator.PLUGIN_ID);
            }
        } catch (IOException e) {
            Activator.logError("Error validating file: " + path, e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            long rawPos = 0;
            String line = rafile.getNextLine();
            while ((line != null) && (lineCount++ < MAX_LINES)) {
                final int idx = indexOfElement(fRecordInputElement.getElementName(), line, 0);
                if (idx != -1) {
                    rafile.seek(rawPos + idx + 1); // +1 is for the <
                    final StringBuffer elementBuffer = new StringBuffer("<"); //$NON-NLS-1$
                    readElement(elementBuffer, rafile);
                    final Element element = parseElementBuffer(elementBuffer);
                    if (element != null) {
                        rafile.close();
                        return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
                    }
                }
                rawPos = rafile.getFilePointer();
                line = rafile.getNextLine();
            }
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        return new TraceValidationStatus(0, Activator.PLUGIN_ID);
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

    @Override
    public String getTraceTypeId() {
        return fTraceTypeId;
    }

    /**
     * Build the trace type id for a custom XML trace
     *
     * @param category
     *            the category
     * @param definitionName
     *            the definition name
     * @return the trace type id
     */
    public static @NonNull String buildTraceTypeId(String category, String definitionName) {
        return CUSTOM_XML_TRACE_TYPE_PREFIX + category + SEPARATOR + definitionName;
    }

    /**
     * Checks whether the given trace type ID is a custom XML trace type ID
     *
     * @param traceTypeId
     *                the trace type ID to check
     * @return <code>true</code> if it's a custom text trace type ID else <code>false</code>
     */
    public static boolean isCustomTraceTypeId(@NonNull String traceTypeId) {
        return traceTypeId.startsWith(CUSTOM_XML_TRACE_TYPE_PREFIX);
    }

    /**
     * This methods builds a trace type ID from a given ID taking into
     * consideration any format changes that were done for the IDs of custom
     * XML traces. For example, such format change took place when moving to
     * Trace Compass. Trace type IDs that are part of the plug-in extension for
     * trace types won't be changed.
     *
     * This method is useful for IDs that were persisted in the workspace before
     * the format changes (e.g. in the persistent properties of a trace
     * resource).
     *
     * It ensures backwards compatibility of the workspace for custom XML
     * traces.
     *
     * @param traceTypeId
     *            the legacy trace type ID
     * @return the trace type id in Trace Compass format
     */
    public static @NonNull String buildCompatibilityTraceTypeId(@NonNull String traceTypeId) {
        // Handle early Trace Compass custom XML trace type IDs
        if (traceTypeId.startsWith(EARLY_TRACE_COMPASS_CUSTOM_XML_TRACE_TYPE_PREFIX)) {
            return CUSTOM_XML_TRACE_TYPE_PREFIX + traceTypeId.substring(EARLY_TRACE_COMPASS_CUSTOM_XML_TRACE_TYPE_PREFIX.length());
        }

        // Handle Linux Tools custom XML trace type IDs (with and without category)
        int index = traceTypeId.lastIndexOf(SEPARATOR);
        if ((index != -1) && (traceTypeId.startsWith(LINUX_TOOLS_CUSTOM_XML_TRACE_TYPE_PREFIX))) {
            String definitionName = index < traceTypeId.length() ? traceTypeId.substring(index + 1) : ""; //$NON-NLS-1$
            if (traceTypeId.contains(CustomXmlTrace.class.getSimpleName() + SEPARATOR) && traceTypeId.indexOf(SEPARATOR) == index) {
                return buildTraceTypeId(CustomXmlTraceDefinition.CUSTOM_XML_CATEGORY, definitionName);
            }
            return CUSTOM_XML_TRACE_TYPE_PREFIX + traceTypeId.substring(LINUX_TOOLS_CUSTOM_XML_TRACE_TYPE_PREFIX.length());
        }
        return traceTypeId;
    }

    @TmfSignalHandler
    @Override
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {
        if (signal.getTrace() == this) {
            try {
                synchronized (this) {
                    // Reset the file handle in case it has reached the end of the
                    // file already. Otherwise, it will not be able to read new data
                    // pass the previous end.
                    initFile();
                }
            } catch (TmfTraceException e) {
                Activator.logError(e.getLocalizedMessage(), e);
            }
        }
        super.traceRangeUpdated(signal);
    }

    /**
     * @since 3.0
     */
    @Override
    public synchronized ITmfTimestamp readEnd() {
        byte[] inputNameBytes = ("<" + fRecordInputElement.getElementName()).getBytes(); //$NON-NLS-1$
        byte[] testBytes = new byte[inputNameBytes.length];
        try {
            Long pos = fFile.length() - inputNameBytes.length;
            /* Outer loop to find the position of a matcher group. */
            while (pos >= 0) {
                /* Inner loop to find matching tag */
                while (pos >= 0) {
                    fFile.seek(pos);
                    /* Make sure we have the right tag. */
                    fFile.read(testBytes, 0, testBytes.length);
                    if (Arrays.equals(inputNameBytes, testBytes)) {
                        break;
                    }
                    pos--;
                }
                ITmfLocation location = new TmfLongLocation(pos);
                ITmfContext context = seekEvent(location);
                ITmfEvent event = parseEvent(context);
                context.dispose();
                if (event != null) {
                    /* The last event in the trace was successfully parsed. */
                    return event.getTimestamp();
                }
                /*
                 * pos was after the beginning of the tag of the last event.
                 */
                pos -= inputNameBytes.length;
            }
        } catch (IOException e) {
            Activator.logError("Error seeking last event. File: " + getPath(), e); //$NON-NLS-1$
        }

        /* Empty trace */
        return null;
    }
}
