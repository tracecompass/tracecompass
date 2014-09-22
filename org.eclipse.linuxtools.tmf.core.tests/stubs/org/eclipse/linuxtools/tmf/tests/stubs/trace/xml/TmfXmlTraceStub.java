/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomEventContent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlEvent;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.SAXException;

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
public class TmfXmlTraceStub extends TmfTrace {

    private static final String DEVELOPMENT_TRACE_PARSER_PATH = "TmfXmlDevelopmentTrace.xml"; //$NON-NLS-1$
    private static final String DEVELOPMENT_TRACE_XSD = "TmfXmlDevelopmentTrace.xsd"; //$NON-NLS-1$
    private static final String EMPTY = ""; //$NON-NLS-1$

    /* XML elements and attributes names */
    private static final String EVENT_NAME_FIELD = "Message"; //$NON-NLS-1$
    private static final String FIELD_NAMES_FIELD = "fields"; //$NON-NLS-1$
    private static final String SOURCE_FIELD = "source"; //$NON-NLS-1$
    private static final String VALUES_FIELD = "values"; //$NON-NLS-1$
    private static final String TYPES_FIELD = "type"; //$NON-NLS-1$
    private static final String VALUES_SEPARATOR = " \\| "; //$NON-NLS-1$
    private static final String TYPE_INTEGER = "int"; //$NON-NLS-1$
    private static final String TYPE_LONG = "long"; //$NON-NLS-1$

    private final CustomXmlTrace fTrace;

    /**
     * Constructor. Constructs the custom XML trace with the appropriate
     * definition.
     */
    public TmfXmlTraceStub() {

        /* Load custom XML definition */
        try (InputStream in = TmfXmlTraceStub.class.getResourceAsStream(DEVELOPMENT_TRACE_PARSER_PATH);) {
            CustomXmlTraceDefinition[] definitions = CustomXmlTraceDefinition.loadAll(in);
            if (definitions.length == 0) {
                throw new IllegalStateException("The custom trace definition does not exist"); //$NON-NLS-1$
            }
            fTrace = new CustomXmlTrace(definitions[0]);
            /* Deregister the custom XML trace */
            TmfSignalManager.deregister(fTrace);
            this.setParser(fTrace);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open the trace parser for development traces"); //$NON-NLS-1$
        }

    }

    @Override
    public void initTrace(IResource resource, String path, Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initTrace(resource, path, type);
        fTrace.initTrace(resource, path, type);
        ITmfContext ctx;
        /* Set the start and (current) end times for this trace */
        ctx = seekEvent(0L);
        ITmfEvent event = getNext(ctx);
        if (event != null) {
            final ITmfTimestamp curTime = event.getTimestamp();
            this.setStartTime(curTime);
            this.setEndTime(curTime);
        }
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        return fTrace.getCurrentLocation();
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        return fTrace.getLocationRatio(location);
    }

    @Override
    public ITmfContext seekEvent(ITmfLocation location) {
        return fTrace.seekEvent(location);
    }

    @Override
    public ITmfContext seekEvent(double ratio) {
        return fTrace.seekEvent(ratio);
    }

    @Override
    public IStatus validate(IProject project, String path) {
        File xmlFile = new File(path);
        if (!xmlFile.exists() || !xmlFile.isFile() || !xmlFile.canRead()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(org.eclipse.linuxtools.tmf.tests.stubs.trace.xml.Messages.TmfDevelopmentTrace_FileNotFound, path));
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
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(org.eclipse.linuxtools.tmf.tests.stubs.trace.xml.Messages.TmfDevelopmentTrace_ValidationError, path), e);
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(org.eclipse.linuxtools.tmf.tests.stubs.trace.xml.Messages.TmfDevelopmentTrace_IoError, path), e);
        }
        return Status.OK_STATUS;
    }

    private static String getStringValue(@NonNull ITmfEventField content, String fieldName) {
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
    public synchronized ITmfEvent getNext(ITmfContext context) {
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
                default:
                    break;
                }
            }
            fieldsArray[i] = new TmfEventField(fields[i], val, null);
        }

        /* Create a new event with new fields and name */
        ITmfEventType customEventType = event.getType();
        TmfEventType eventType = new TmfEventType(customEventType.getContext(), getStringValue(content, EVENT_NAME_FIELD), customEventType.getRootField());
        ITmfEventField eventFields = new CustomEventContent(content.getName(), content.getValue(), fieldsArray);
        TmfEvent newEvent = new TmfEvent(this, event.getTimestamp(), getStringValue(content, SOURCE_FIELD), eventType, eventFields, event.getReference());
        updateAttributes(savedContext, event.getTimestamp());
        context.increaseRank();

        return newEvent;
    }

}
