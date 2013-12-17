/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Tasse - Initial API and implementation
 *     Bernd Hufmann - Added call site and model URI properties
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfCustomAttributes;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.lookup.ITmfModelLookup;
import org.eclipse.linuxtools.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Property source for events
 *
 * @since 2.0
 */
public class TmfEventPropertySource implements IPropertySource {

    private static final String ID_TIMESTAMP = "event_timestamp"; //$NON-NLS-1$
    private static final String ID_SOURCE = "event_source"; //$NON-NLS-1$
    private static final String ID_TYPE = "event_type"; //$NON-NLS-1$
    private static final String ID_REFERENCE = "event_reference"; //$NON-NLS-1$
    private static final String ID_CONTENT = "event_content"; //$NON-NLS-1$
    private static final String ID_SOURCE_LOOKUP = "event_lookup"; //$NON-NLS-1$
    private static final String ID_MODEL_URI = "model_uri"; //$NON-NLS-1$
    private static final String ID_CUSTOM_ATTRIBUTE = "custom_attribute"; //$NON-NLS-1$

    private static final String NAME_TIMESTAMP = "Timestamp"; //$NON-NLS-1$
    private static final String NAME_SOURCE = "Source"; //$NON-NLS-1$
    private static final String NAME_TYPE = "Type"; //$NON-NLS-1$
    private static final String NAME_REFERENCE = "Reference"; //$NON-NLS-1$
    private static final String NAME_CONTENT = "Content"; //$NON-NLS-1$
    private static final String NAME_SOURCE_LOOKUP = "Source Lookup"; //$NON-NLS-1$
    private static final String NAME_MODEL_URI = "Model URI"; //$NON-NLS-1$
    private static final String NAME_CUSTOM_ATTRIBUTES = "Custom Attributes"; //$NON-NLS-1$

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private ITmfEvent fEvent;

    private class TimestampPropertySource implements IPropertySource {
        private static final String ID_TIMESTAMP_VALUE = "timestamp_value"; //$NON-NLS-1$
        private static final String ID_TIMESTAMP_SCALE = "timestamp_scale"; //$NON-NLS-1$
        private static final String ID_TIMESTAMP_PRECISION = "timestamp_precision"; //$NON-NLS-1$
        private static final String NAME_TIMESTAMP_VALUE = "value"; //$NON-NLS-1$
        private static final String NAME_TIMESTAMP_SCALE = "scale"; //$NON-NLS-1$
        private static final String NAME_TIMESTAMP_PRECISION = "precision"; //$NON-NLS-1$

        private ITmfTimestamp fTimestamp;

        public TimestampPropertySource(ITmfTimestamp timestamp) {
            fTimestamp = timestamp;
        }

        @Override
        public Object getEditableValue() {
            return fTimestamp.toString();
        }

        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            IPropertyDescriptor[] descriptors = new IPropertyDescriptor[3];
            descriptors[0] = new ReadOnlyTextPropertyDescriptor(ID_TIMESTAMP_VALUE, NAME_TIMESTAMP_VALUE);
            descriptors[1] = new ReadOnlyTextPropertyDescriptor(ID_TIMESTAMP_SCALE, NAME_TIMESTAMP_SCALE);
            descriptors[2] = new ReadOnlyTextPropertyDescriptor(ID_TIMESTAMP_PRECISION, NAME_TIMESTAMP_PRECISION);
            return descriptors;
        }

        @Override
        public Object getPropertyValue(Object id) {
            if (id.equals(ID_TIMESTAMP_VALUE)) {
                return Long.toString(fTimestamp.getValue());
            } else if (id.equals(ID_TIMESTAMP_SCALE)) {
                return Integer.toString(fTimestamp.getScale());
            } else if (id.equals(ID_TIMESTAMP_PRECISION)) {
                return Integer.toString(fTimestamp.getPrecision());
            }
            return null;
        }

        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }

        @Override
        public void resetPropertyValue(Object id) {
        }

        @Override
        public void setPropertyValue(Object id, Object value) {
        }
    }

    private class ContentPropertySource implements IPropertySource {
        private ITmfEventField fContent;

        public ContentPropertySource(ITmfEventField content) {
            fContent = content;
        }

        @Override
        public Object getEditableValue() {
            return fContent.toString();
        }

        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            List<IPropertyDescriptor> descriptors= new ArrayList<>(fContent.getFields().length);
            for (ITmfEventField field : fContent.getFields()) {
                if (field != null) {
                    descriptors.add(new ReadOnlyTextPropertyDescriptor(field, field.getName()));
                }
            }
            return descriptors.toArray(new IPropertyDescriptor[0]);
        }

        @Override
        public Object getPropertyValue(Object id) {
            ITmfEventField field = (ITmfEventField) id;
            if (field.getFields() != null && field.getFields().length > 0) {
                return new ContentPropertySource(field);
            }
            return field.getFormattedValue();
        }

        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }

        @Override
        public void resetPropertyValue(Object id) {
        }

        @Override
        public void setPropertyValue(Object id, Object value) {
        }
    }

    private class SourceLookupPropertySource implements IPropertySource {

        private static final String ID_FILE_NAME = "callsite_file"; //$NON-NLS-1$
        private static final String ID_FUNCTION_NAME = "callsite_function"; //$NON-NLS-1$
        private static final String ID_LINE_NUMBER = "callsite_line"; //$NON-NLS-1$

        private static final String NAME_FILE_NAME = "File"; //$NON-NLS-1$
        private static final String NAME_FUNCTION_NAME = "Function"; //$NON-NLS-1$
        private static final String NAME_LINE_NUMBER = "Line"; //$NON-NLS-1$

        final private ITmfSourceLookup fSourceLookup;

        public SourceLookupPropertySource(ITmfSourceLookup lookup) {
            fSourceLookup = lookup;
        }

        @Override
        public Object getEditableValue() {
            if (fSourceLookup.getCallsite() != null) {
                return fSourceLookup.getCallsite().toString();
            }
            return null;
        }

        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            List<IPropertyDescriptor> descriptors= new ArrayList<>();
            if (fSourceLookup.getCallsite() != null) {
                descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_FILE_NAME, NAME_FILE_NAME));
                descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_LINE_NUMBER, NAME_LINE_NUMBER));
                // only display function if available
                if (fSourceLookup.getCallsite().getFunctionName() != null) {
                    descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_FUNCTION_NAME, NAME_FUNCTION_NAME));
                }
            }
            return descriptors.toArray(new IPropertyDescriptor[0]);
        }

        @Override
        public Object getPropertyValue(Object id) {
            if  (id.equals(ID_FILE_NAME)) {
                return fSourceLookup.getCallsite().getFileName();
            } else if (id.equals(ID_FUNCTION_NAME)) {
                return fSourceLookup.getCallsite().getFunctionName();
            } else if (id.equals(ID_LINE_NUMBER)) {
                return Long.valueOf(fSourceLookup.getCallsite().getLineNumber());
            }
            return null;
        }

        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }

        @Override
        public void resetPropertyValue(Object id) {

        }

        @Override
        public void setPropertyValue(Object id, Object value) {
        }
    }

    private class CustomAttributePropertySource implements IPropertySource {

        private final ITmfCustomAttributes event;

        public CustomAttributePropertySource(ITmfCustomAttributes event) {
            this.event = event;
        }

        @Override
        public Object getEditableValue() {
            return EMPTY_STRING;
        }

        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            List<IPropertyDescriptor> descriptors = new ArrayList<>();

            for (String customAttribute : event.listCustomAttributes()) {
                descriptors.add(new ReadOnlyTextPropertyDescriptor(customAttribute, customAttribute));
            }

            return descriptors.toArray(new IPropertyDescriptor[0]);
        }

        @Override
        public Object getPropertyValue(Object id) {
            return event.getCustomAttribute((String) id);
        }

        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }

        @Override
        public void resetPropertyValue(Object id) {
        }

        @Override
        public void setPropertyValue(Object id, Object value) {
        }
    }

    /**
     * Default constructor
     *
     * @param event the event
     */
    public TmfEventPropertySource(ITmfEvent event) {
        super();
        this.fEvent = event;
    }

    @Override
    public Object getEditableValue() {
        return null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> descriptors= new ArrayList<>();

        /* Display basic event information */
        descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_TIMESTAMP, NAME_TIMESTAMP));
        descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_SOURCE, NAME_SOURCE));
        descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_TYPE, NAME_TYPE));
        descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_REFERENCE, NAME_REFERENCE));

        /* Display event fields */
        descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_CONTENT, NAME_CONTENT));

        /* Display source lookup information, if the event supplies it */
        if ((fEvent instanceof ITmfSourceLookup) && (((ITmfSourceLookup)fEvent).getCallsite() != null)) {
            descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_SOURCE_LOOKUP, NAME_SOURCE_LOOKUP));
        }

        /* Display Model URI information, if the event supplies it */
        if ((fEvent instanceof ITmfModelLookup) && (((ITmfModelLookup) fEvent).getModelUri() != null)) {
            descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_MODEL_URI, NAME_MODEL_URI));
        }

        /* Display custom attributes, if available */
        if (fEvent instanceof ITmfCustomAttributes) {
            ITmfCustomAttributes event = (ITmfCustomAttributes) fEvent;
            if (event.listCustomAttributes() != null && !event.listCustomAttributes().isEmpty()) {
                descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_CUSTOM_ATTRIBUTE, NAME_CUSTOM_ATTRIBUTES));
            }
        }

        return descriptors.toArray(new IPropertyDescriptor[0]);
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals(ID_TIMESTAMP) && fEvent.getTimestamp() != null) {
            return new TimestampPropertySource(fEvent.getTimestamp());
        } else if (id.equals(ID_SOURCE) && fEvent.getSource() != null) {
            return fEvent.getSource().toString();
        } else if (id.equals(ID_TYPE) && fEvent.getType() != null) {
            return fEvent.getType().toString();
        } else if (id.equals(ID_REFERENCE) && fEvent.getReference() != null) {
            return fEvent.getReference().toString();
        } else if (id.equals(ID_MODEL_URI)) {
            return ((ITmfModelLookup)fEvent).getModelUri();
        } else if (id.equals(ID_SOURCE_LOOKUP)) {
            return new SourceLookupPropertySource(((ITmfSourceLookup)fEvent));
        } else if (id.equals(ID_CONTENT) && fEvent.getContent() != null) {
            return new ContentPropertySource(fEvent.getContent());
        } else if (id.equals(ID_CUSTOM_ATTRIBUTE)) {
            return new CustomAttributePropertySource((ITmfCustomAttributes) fEvent);
        }
        return null;
    }

    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
    }

}
