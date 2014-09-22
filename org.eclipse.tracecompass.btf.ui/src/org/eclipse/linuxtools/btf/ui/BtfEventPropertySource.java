/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.btf.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.btf.core.event.BtfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventPropertySource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Btf event property source
 *
 * @author Matthew Khouzam
 */
public class BtfEventPropertySource extends TmfEventPropertySource {

    private static final String ID_EVENT_EVENT = "event_event"; //$NON-NLS-1$
    private static final String ID_EVENT_TIMESTAMP = "event_timestamp"; //$NON-NLS-1$
    private static final String ID_EVENT_SOURCE = "event_source"; //$NON-NLS-1$
    private static final String ID_EVENT_TYPE = "event_type"; //$NON-NLS-1$
    private static final String ID_EVENT_TARGET = "event_target"; //$NON-NLS-1$
    private final BtfEvent fEvent;

    /**
     * Btf Event property source
     *
     * @param event
     *            the event
     */
    public BtfEventPropertySource(BtfEvent event) {
        super(event);
        fEvent = event;

    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new ReadOnlyTextPropertyDescriptor(ID_EVENT_TIMESTAMP, "Timestamp"), //$NON-NLS-1$
                new ReadOnlyTextPropertyDescriptor(ID_EVENT_SOURCE, "Source"), //$NON-NLS-1$
                new ReadOnlyTextPropertyDescriptor(ID_EVENT_TYPE, "Type"), //$NON-NLS-1$
                new ReadOnlyTextPropertyDescriptor(ID_EVENT_TARGET, "Target"), //$NON-NLS-1$
                new ReadOnlyTextPropertyDescriptor(ID_EVENT_EVENT, "event") //$NON-NLS-1$
        };
    }

    private class TargetPropertySource implements IPropertySource {
        private static final String INSTANCE = "Instance"; //$NON-NLS-1$
        private final String fTarget;
        private final String fInstance;

        public TargetPropertySource(String target, String instance) {
            fTarget = target;
            fInstance = instance;
        }

        @Override
        public Object getEditableValue() {
            return fTarget;
        }

        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            return new IPropertyDescriptor[] {
                    new ReadOnlyTextPropertyDescriptor(INSTANCE, INSTANCE)
            };
        }

        @Override
        public Object getPropertyValue(Object id) {
            if (INSTANCE.equals(id)) {
                return fInstance;
            }
            return null;
        }

        @Override
        public boolean isPropertySet(Object id) {
            return INSTANCE.equals(id);
        }

        @Override
        public void resetPropertyValue(Object id) {
        }

        @Override
        public void setPropertyValue(Object id, Object value) {
        }

    }

    private class TypePropertySource implements IPropertySource {
        private static final String DESCRIPTION = "Description"; //$NON-NLS-1$
        private final String fType;
        private final String fDescr;

        public TypePropertySource(String type, String descr) {
            fType = type;
            fDescr = descr;
        }

        @Override
        public Object getEditableValue() {
            return fType;
        }

        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            IPropertyDescriptor[] descriptors = new IPropertyDescriptor[1];
            descriptors[0] = new ReadOnlyTextPropertyDescriptor(DESCRIPTION, DESCRIPTION);
            return descriptors;
        }

        @Override
        public Object getPropertyValue(Object id) {
            if (DESCRIPTION.equals(id)) {
                return fDescr;
            }
            return null;
        }

        @Override
        public boolean isPropertySet(Object id) {
            return DESCRIPTION.equals(id);
        }

        @Override
        public void resetPropertyValue(Object id) {
        }

        @Override
        public void setPropertyValue(Object id, Object value) {
        }
    }

    private class EventPropertySource implements IPropertySource {
        private static final String DESCRIPTION = "Description"; //$NON-NLS-1$
        private static final String NOTE = "Note"; //$NON-NLS-1$
        final String fEventName;
        final String fNote;
        final String fEventDescription;

        public EventPropertySource(String event, String note) {
            fEventName = event;
            fNote = note;

            ITmfEventField content = fEvent.getContent();
            String first = content.getFieldNames().iterator().next();
            content = content.getField(first);
            first = content.getFieldNames().iterator().next();
            fEventDescription = content.getField(first).getValue().toString();
        }

        @Override
        public Object getEditableValue() {
            return fEventName;
        }

        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            if (fNote == null) {
                return new IPropertyDescriptor[] {
                new ReadOnlyTextPropertyDescriptor(DESCRIPTION, DESCRIPTION)
                };
            }
            return new IPropertyDescriptor[] {
                    new ReadOnlyTextPropertyDescriptor(DESCRIPTION, DESCRIPTION),
                    new ReadOnlyTextPropertyDescriptor(NOTE, NOTE)
            };
        }

        @Override
        public Object getPropertyValue(Object id) {
            if (NOTE.equals(id)) {
                return fNote;
            } else if (DESCRIPTION.equals(id)) {
                return fEventDescription;
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

    @Override
    public Object getPropertyValue(Object id) {
        if (id instanceof String) {
            String id2 = (String) id;
            final ITmfEventField content = fEvent.getContent();
            List<String> fieldNames = new ArrayList<>(content.getFieldNames());
            switch (id2) {
            case ID_EVENT_TARGET:
                return new TargetPropertySource(fEvent.getReference(), content.getField(fieldNames.get(2)).toString());
            case ID_EVENT_SOURCE:
                return new TargetPropertySource(fEvent.getSource(), content.getField(fieldNames.get(1)).toString());
            case ID_EVENT_TYPE:
                return new TypePropertySource(fEvent.getType().getName(), fEvent.getEventDescription());
            case ID_EVENT_EVENT:
                ITmfEventField noteField = (fieldNames.size() > 3) ? content.getField(fieldNames.get(3)) : null;
                return new EventPropertySource(content.getField(fieldNames.get(0)).getValue().toString(), (noteField == null) ? null : noteField.getValue().toString());
            default:
                break;
            }
        }
        return super.getPropertyValue(id);
    }
}
