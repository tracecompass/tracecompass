/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *   Patrick Tasse - Dispose wrapped trace
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.trace.xml;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfContentFieldAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomEventContent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * An XML development trace using a custom XML trace definition and schema.
 *
 * This class will typically be used to build custom traces to unit test more
 * complex functionalities like analyzes or to develop and test data-driven
 * analyzes.
 *
 * This class wraps a custom XML trace and rewrites the returned events in the
 * getNext() method so that event's fields are the ones defined in <field ... />
 * elements instead of those defined in the custom XML parser. This way, each
 * event can have a different set of fields. This class can, for example, mimic
 * a CTF trace.
 *
 * @author Geneviève Bastien
 */
public abstract class TmfXmlTraceStub extends TmfTrace {

    private static final String DEVELOPMENT_TRACE_XSD = "TmfXmlDevelopmentTrace.xsd"; //$NON-NLS-1$
    private static final String EMPTY = ""; //$NON-NLS-1$

    /* XML elements and attributes names */
    private static final String EVENT_NAME_FIELD = "Message"; //$NON-NLS-1$
    private static final String FIELD_NAMES_FIELD = "fields"; //$NON-NLS-1$
    private static final String VALUES_FIELD = "values"; //$NON-NLS-1$
    private static final String TYPES_FIELD = "type"; //$NON-NLS-1$
    private static final String VALUES_SEPARATOR = " \\| "; //$NON-NLS-1$
    private static final String TYPE_INTEGER = "int"; //$NON-NLS-1$
    private static final String TYPE_LONG = "long"; //$NON-NLS-1$
    private static final String TYPE_LONG_ARRAY = "longArray"; //$NON-NLS-1$
    private static final String TYPE_INT_ARRAY = "intArray"; //$NON-NLS-1$
    private static final String ASPECT_CPU = "cpu";

    private final CustomXmlTraceDefinition fDefinition;
    private CustomXmlTrace fTrace;

    private Collection<ITmfEventAspect<?>> fAspects = TmfTrace.BASE_ASPECTS;
    private final Collection<ITmfEventAspect<?>> fAdditionalAspects = new HashSet<>();
    private final Collection<IAnalysisModule> fAdditionalModules = new HashSet<>();

    /**
     * Constructor. Constructs the custom XML trace with the appropriate
     * definition.
     */
    public TmfXmlTraceStub() {
        boolean nonNullTraceIsNonNull = false;
        /* Load custom XML definition */
        try (InputStream in = TmfXmlTraceStub.class.getResourceAsStream(getParserFileName());) {
            CustomXmlTraceDefinition[] definitions = CustomXmlTraceDefinition.loadAll(in);
            if (definitions.length < 2) {
                throw new IllegalStateException("The custom trace definition does not exist"); //$NON-NLS-1$
            }
            /* The first definition parses the 'set_aspects' event */
            fTrace = new CustomXmlTrace(definitions[0]) {
                @Override
                protected ITmfTraceIndexer createIndexer(int interval) {
                    /* Use the in-memory checkpoint indexer */
                    return new TmfCheckpointIndexer(this, interval);
                }
            };
            TmfSignalManager.deregister(fTrace);
            nonNullTraceIsNonNull = true;
            /* The second definition parses 'event' trace events */
            fDefinition = checkNotNull(definitions[1]);
        } catch (IOException e) {
            if (nonNullTraceIsNonNull) {
                fTrace.dispose();
            }
            throw new IllegalStateException("Cannot open the trace parser for development traces"); //$NON-NLS-1$
        }

    }

    /**
     * Get the name of the file that contains the XML parser for this trace
     * stub. The file should be located in this class's package.
     *
     * @return The name of the file
     */
    protected abstract String getParserFileName();

    @Override
    @NonNullByDefault({DefaultLocation.TYPE_ARGUMENT})
    public void initTrace(@Nullable IResource resource, @Nullable String path, @Nullable Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initTrace(resource, path, type);
        ITmfContext ctx;

        /* Initialize and read the trace with the 'set_aspects' definition */
        TmfSignalManager.deregister(fTrace);
        fTrace.initTrace(resource, path, type);
        ctx = seekEvent(0L);
        /* If a set_aspects event exists, getNext() will process it */
        getNext(ctx);
        ctx.dispose();
        fTrace.dispose();

        /* Initialize a new trace with the trace events definition */
        fTrace = new CustomXmlTrace(fDefinition);
        TmfSignalManager.deregister(fTrace);
        fTrace.initTrace(resource, path, type);
        /* Set the start and (current) end times for this trace */
        ctx = seekEvent(0L);
        if (ctx == null) {
            return;
        }
        ITmfEvent event = getNext(ctx);
        if (event != null) {
            final ITmfTimestamp curTime = event.getTimestamp();
            this.setStartTime(curTime);
            this.setEndTime(curTime);
        }
        ctx.dispose();
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        fTrace.dispose();
    }

    @Override
    public @Nullable ITmfEvent parseEvent(@Nullable ITmfContext context) {
        return fTrace.parseEvent(context);
    }

    @Override
    public @Nullable ITmfLocation getCurrentLocation() {
        return fTrace.getCurrentLocation();
    }

    @Override
    public double getLocationRatio(@Nullable ITmfLocation location) {
        return fTrace.getLocationRatio(location);
    }

    @Override
    public @Nullable ITmfContext seekEvent(@Nullable ITmfLocation location) {
        return fTrace.seekEvent(location);
    }

    @Override
    public @Nullable ITmfContext seekEvent(double ratio) {
        return fTrace.seekEvent(ratio);
    }

    @Override
    public IStatus validate(@Nullable IProject project, @Nullable String path) {
        File xmlFile = new File(path);
        if (!xmlFile.exists() || !xmlFile.isFile() || !xmlFile.canRead()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.Messages.TmfDevelopmentTrace_FileNotFound, path));
        }
        /* Does the XML file validate with the XSD */
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source xmlSource = new StreamSource(xmlFile);

        try {
            URL url = TmfXmlTraceStub.class.getResource(DEVELOPMENT_TRACE_XSD);
            Schema schema = schemaFactory.newSchema(url);

            Validator validator = schema.newValidator();
            validator.validate(xmlSource);
        } catch (SAXException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.Messages.TmfDevelopmentTrace_ValidationError, path), e);
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.Messages.TmfDevelopmentTrace_IoError, path), e);
        }
        return Status.OK_STATUS;
    }

    private static String getStringValue(ITmfEventField content, String fieldName) {
        ITmfEventField field = content.getField(fieldName);
        if (field == null) {
            return EMPTY;
        }
        Object val = field.getValue();
        if (!(val instanceof String)) {
            return EMPTY;
        }
        return (String) val;
    }

    @Override
    public synchronized @Nullable ITmfEvent getNext(@Nullable ITmfContext context) {
        if (context == null) {
            return null;
        }
        final ITmfContext savedContext = new TmfContext(context.getLocation(), context.getRank());
        CustomXmlEvent event = fTrace.getNext(context);
        if (event == null) {
            return null;
        }

        /* Translate the content of the event */
        /* The "fields" field contains a | separated list of field names */
        /* The "values" field contains a | separated list of field values */
        /* the "type" field contains a | separated list of field types */
        ITmfEventField content = event.getContent();
        if (content == null) {
            return null;
        }

        String fieldString = getStringValue(content, FIELD_NAMES_FIELD);
        String valueString = getStringValue(content, VALUES_FIELD);
        String typeString = getStringValue(content, TYPES_FIELD);

        String[] fields = fieldString.split(VALUES_SEPARATOR);
        String[] values = valueString.split(VALUES_SEPARATOR);
        String[] types = typeString.split(VALUES_SEPARATOR);
        ITmfEventField[] fieldsArray = new TmfEventField[fields.length];

        for (int i = 0; i < fields.length; i++) {
            String value = EMPTY;
            if (values.length > i) {
                value = values[i];
            }
            String type = null;
            if (types.length > i) {
                type = types[i];
            }
            Object val = value;
            if (type != null) {
                switch (type) {
                case TYPE_INTEGER: {
                    try {
                        val = Integer.valueOf(value);
                    } catch (NumberFormatException e) {
                        Activator.logError(String.format("Get next XML event: cannot cast value %s to integer", value), e); //$NON-NLS-1$
                        val = 0;
                    }
                    break;
                }
                case TYPE_LONG: {
                    try {
                        val = Long.valueOf(value);
                    } catch (NumberFormatException e) {
                        Activator.logError(String.format("Get next XML event: cannot cast value %s to long", value), e); //$NON-NLS-1$
                        val = 0L;
                    }
                    break;
                }
                case TYPE_LONG_ARRAY: {
                    try {
                        String[] split = value.split(",");
                        long[] arr = new long[split.length];
                        for (int j = 0; j < split.length; j++) {
                            arr[j] = Long.valueOf(split[j]);
                        }
                        val = arr;
                    } catch (NumberFormatException e) {
                        Activator.logError(String.format("Get next XML event: cannot cast one of the comma-separated values of %s to long", value), e); //$NON-NLS-1$
                        val = new long[0];
                    }
                    break;
                }
                case TYPE_INT_ARRAY: {
                    try {
                        String[] split = value.split(",");
                        int[] arr = new int[split.length];
                        for (int j = 0; j < split.length; j++) {
                            arr[j] = Integer.valueOf(split[j]);
                        }
                        val = arr;
                    } catch (NumberFormatException e) {
                        Activator.logError(String.format("Get next XML event: cannot cast one of the comma-separated values of %s to int", value), e); //$NON-NLS-1$
                        val = new int[0];
                    }
                    break;
                }
                default:
                    break;
                }
            }
            fieldsArray[i] = new TmfEventField(checkNotNull(fields[i]), val, null);
        }

        /*
         * Generate the aspects for this trace if it is the 'set_aspects'
         * definition
         */
        if (fTrace.getDefinition() != fDefinition) {
            generateAspects(fieldsArray);
            return null;
        }

        /* Create a new event with new fields and name */
        ITmfEventType customEventType = event.getType();
        String eventName = getStringValue(content, EVENT_NAME_FIELD);
        TmfEventType eventType = new TmfEventType(eventName, customEventType.getRootField());
        ITmfEventField eventFields = new CustomEventContent(content.getName(), content.getValue(), fieldsArray);
        /*
         * TODO: Timestamps for these traces are in nanos, but since the
         * CustomXmlTrace does not support this format, the timestamp of the
         * original is in second and we need to convert it. We should do that at
         * the source when it is supported
         */
        TmfEvent newEvent = new TmfEvent(this, ITmfContext.UNKNOWN_RANK, event.getTimestamp(), eventType, eventFields);
        updateAttributes(savedContext, event);
        return newEvent;
    }

    private void generateAspects(ITmfEventField[] fieldsArray) {
        ImmutableList.Builder<ITmfEventAspect<?>> builder = new ImmutableList.Builder<>();

        /* Initialize the first default trace aspects */
        builder.add(TmfBaseAspects.getTimestampAspect());
        builder.add(TmfBaseAspects.getEventTypeAspect());

        /* Add custom aspects in between */
        for (ITmfEventField field : fieldsArray) {
            String name = field.getName();
            final ITmfEventAspect<?> aspect = new TmfContentFieldAspect(name, name);
            if (name.equals(ASPECT_CPU)) {
                builder.add(new TmfCpuAspect() {
                    @Override
                    public @Nullable Integer resolve(ITmfEvent event) {
                        Object result = aspect.resolve(event);
                        if (result instanceof Number) {
                            return ((Number) result).intValue();
                        }
                        return null;
                    }
                });
            } else {
                builder.add(aspect);
            }
        }

        /* Add the big content aspect */
        builder.add(TmfBaseAspects.getContentsAspect());
        /* Add the additional aspects */
        builder.addAll(fAdditionalAspects);
        fAspects = builder.build();
    }

    @Override
    public Iterable<ITmfEventAspect<?>> getEventAspects() {
        return fAspects;
    }

    /**
     * Adds a new event aspect to this type of trace. Since this trace type is
     * used to build custom traces that mimic the behavior of real traces, the
     * required aspects may be missing and this method allows to add them. This
     * method should be called before calling
     * {@link #initTrace(IResource, String, Class)} otherwise the additional
     * aspects will not be picked up when generating the aspects.
     *
     * @param aspect
     *            The aspect to have
     */
    public void addEventAspect(ITmfEventAspect<?> aspect) {
        fAdditionalAspects.add(aspect);
    }

    /**
     * Add an additional new module
     *
     * @param module
     *            The new module
     */
    public void addAnalysisModule(IAnalysisModule module) {
        fAdditionalModules.add(module);
    }

    @Override
    public Iterable<@NonNull IAnalysisModule> getAnalysisModules() {
        @NonNull Iterable<IAnalysisModule> modules = super.getAnalysisModules();
        return checkNotNull(Iterables.concat(modules, fAdditionalModules));
    }

    @Override
    public @Nullable IAnalysisModule getAnalysisModule(@Nullable String analysisId) {
        Iterable<@NonNull IAnalysisModule> modules = getAnalysisModules();
        Optional<IAnalysisModule> opt = StreamSupport.stream(modules.spliterator(), false).filter(analysis -> analysis.getId().equals(analysisId)).findFirst();
        return opt.isPresent() ? opt.get() : null;
    }
}
