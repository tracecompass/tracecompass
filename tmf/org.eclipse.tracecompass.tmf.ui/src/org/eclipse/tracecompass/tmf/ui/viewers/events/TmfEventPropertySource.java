/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.viewers.events;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.StreamUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfCustomAttributes;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfModelLookup;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Property source for events
 */
public class TmfEventPropertySource implements IPropertySource {

    private static final String ID_CONTENT = "event_content"; //$NON-NLS-1$
    private static final String ID_SOURCE_LOOKUP = "event_lookup"; //$NON-NLS-1$
    private static final String ID_MODEL_URI = "model_uri"; //$NON-NLS-1$
    private static final String ID_CUSTOM_ATTRIBUTE = "custom_attribute"; //$NON-NLS-1$

    private static final String NAME_CONTENT = "Content"; //$NON-NLS-1$
    private static final String NAME_SOURCE_LOOKUP = "Source Lookup"; //$NON-NLS-1$
    private static final String NAME_MODEL_URI = "Model URI"; //$NON-NLS-1$
    private static final String NAME_CUSTOM_ATTRIBUTES = "Custom Attributes"; //$NON-NLS-1$

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private final @NonNull ITmfEvent fEvent;

    private static class ContentPropertySource implements IPropertySource {
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
            List<IPropertyDescriptor> descriptors = new ArrayList<>(fContent.getFields().size());
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
            if (!field.getFields().isEmpty()) {
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

    private static class SourceLookupPropertySource implements IPropertySource {

        private static final String ID_FILE_NAME = "callsite_file"; //$NON-NLS-1$
        private static final String ID_LINE_NUMBER = "callsite_line"; //$NON-NLS-1$

        private static final String NAME_FILE_NAME = "File"; //$NON-NLS-1$
        private static final String NAME_LINE_NUMBER = "Line"; //$NON-NLS-1$

        private final ITmfSourceLookup fSourceLookup;

        public SourceLookupPropertySource(ITmfSourceLookup lookup) {
            fSourceLookup = lookup;
        }

        @Override
        public Object getEditableValue() {
            ITmfCallsite cs = fSourceLookup.getCallsite();
            if (cs != null) {
                return cs.toString();
            }
            return null;
        }

        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            List<IPropertyDescriptor> descriptors= new ArrayList<>();
            ITmfCallsite cs = fSourceLookup.getCallsite();
            if (cs != null) {
                descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_FILE_NAME, NAME_FILE_NAME));
                descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_LINE_NUMBER, NAME_LINE_NUMBER));
            }
            return descriptors.toArray(new IPropertyDescriptor[0]);
        }

        @Override
        public String getPropertyValue(Object id) {
            ITmfCallsite cs = fSourceLookup.getCallsite();
            if (cs == null) {
                /*
                 * The callsite should not be null here, we would not have
                 * created the descriptors otherwise
                 */
                throw new IllegalStateException();
            }

            if (!(id instanceof String)) {
                return null;
            }

            switch ((String) id) {
            case ID_FILE_NAME:
                return cs.getFileName();
            case ID_LINE_NUMBER:
                return nullToEmptyString(cs.getLineNo());
            default:
                return null;
            }
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
            if (!(id instanceof String)) {
                return null;
            }
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
    public TmfEventPropertySource(@NonNull ITmfEvent event) {
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

        /* Display properties for event aspects */
        getTraceAspects().forEach(aspect -> {
            /*
             * Contents has its special property source, which puts the fields
             * in a sub-tree.
             */
            if (aspect == TmfBaseAspects.getContentsAspect()) {
                descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_CONTENT, NAME_CONTENT));
            } else {
                String name = aspect.getName();
                descriptors.add(new ReadOnlyTextPropertyDescriptor(name, name));
            }
        });

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
            if (!event.listCustomAttributes().isEmpty()) {
                descriptors.add(new ReadOnlyTextPropertyDescriptor(ID_CUSTOM_ATTRIBUTE, NAME_CUSTOM_ATTRIBUTES));
            }
        }

        return descriptors.toArray(new IPropertyDescriptor[0]);
    }

    @Override
    public Object getPropertyValue(Object objectId) {
        if (!(objectId instanceof String)) {
            return null;
        }

        String id = (String) objectId;

        if (id.equals(ID_MODEL_URI)) {
            return ((ITmfModelLookup)fEvent).getModelUri();
        } else if (id.equals(ID_SOURCE_LOOKUP)) {
            return new SourceLookupPropertySource(((ITmfSourceLookup)fEvent));
        } else if (id.equals(ID_CONTENT) && fEvent.getContent() != null) {
            return new ContentPropertySource(fEvent.getContent());
        } else if (id.equals(ID_CUSTOM_ATTRIBUTE)) {
            return new CustomAttributePropertySource((ITmfCustomAttributes) fEvent);
        }

        /* Look for the ID in the aspect names */
        Optional<ITmfEventAspect<?>> potentialAspect = getTraceAspects()
            .filter(aspect -> aspect.getName().equals(id))
            .findFirst();

        if (potentialAspect.isPresent()) {
            Object res = potentialAspect.get().resolve(fEvent);
            return (res == null ? "" : res.toString()); //$NON-NLS-1$
        }

        return null;
    }

    private Stream<ITmfEventAspect<?>> getTraceAspects() {
        return StreamUtils.getStream(fEvent.getTrace().getEventAspects());
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
