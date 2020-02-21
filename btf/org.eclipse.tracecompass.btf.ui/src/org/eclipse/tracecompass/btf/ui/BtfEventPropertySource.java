/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Update properties
 *******************************************************************************/

package org.eclipse.tracecompass.btf.ui;

import java.util.Arrays;

import org.eclipse.tracecompass.btf.core.event.BTFPayload;
import org.eclipse.tracecompass.btf.core.event.BtfEvent;
import org.eclipse.tracecompass.btf.core.trace.BtfColumnNames;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventPropertySource;
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
    private static final String ID_EVENT_NOTES = "event_notes"; //$NON-NLS-1$
    private static final IPropertyDescriptor[] DESCRIPTORS = new IPropertyDescriptor[] {
        new ReadOnlyTextPropertyDescriptor(ID_EVENT_TIMESTAMP, "Timestamp"), //$NON-NLS-1$
        new ReadOnlyTextPropertyDescriptor(ID_EVENT_SOURCE, "Source"), //$NON-NLS-1$
        new ReadOnlyTextPropertyDescriptor(ID_EVENT_TYPE, "Type"), //$NON-NLS-1$
        new ReadOnlyTextPropertyDescriptor(ID_EVENT_TARGET, "Target"), //$NON-NLS-1$
        new ReadOnlyTextPropertyDescriptor(ID_EVENT_EVENT, "Event"), //$NON-NLS-1$
        new ReadOnlyTextPropertyDescriptor(ID_EVENT_NOTES, "Notes") //$NON-NLS-1$
    };
    private static final IPropertyDescriptor[] DESCRIPTORS_WITHOUT_NOTES = Arrays.copyOf(DESCRIPTORS, DESCRIPTORS.length - 1);
    private static final String DESCRIPTION = "Description"; //$NON-NLS-1$
    private static final String INSTANCE = "Instance"; //$NON-NLS-1$

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
        if (fEvent.getContent().getField(BtfColumnNames.NOTES.toString()) == null) {
            return DESCRIPTORS_WITHOUT_NOTES;
        }
        return DESCRIPTORS;
    }

    private static class EntityPropertySource implements IPropertySource {
        private final String fName;
        private final String fInstance;

        public EntityPropertySource(String name, String instance) {
            fName = name;
            fInstance = instance;
        }

        @Override
        public Object getEditableValue() {
            return fName;
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
            return false;
        }

        @Override
        public void resetPropertyValue(Object id) {
            // Do nothing
        }

        @Override
        public void setPropertyValue(Object id, Object value) {
            // Do nothing
        }

    }

    private static class TypePropertySource implements IPropertySource {
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
            return false;
        }

        @Override
        public void resetPropertyValue(Object id) {
            // Do nothing
        }

        @Override
        public void setPropertyValue(Object id, Object value) {
            // Do nothing
        }
    }

    private static class EventPropertySource implements IPropertySource {
        private final ITmfEventField fEventField;

        public EventPropertySource(ITmfEventField eventField) {
            fEventField = eventField;
        }

        @Override
        public Object getEditableValue() {
            return fEventField.getValue();
        }

        @Override
        public IPropertyDescriptor[] getPropertyDescriptors() {
            return new IPropertyDescriptor[] {
                    new ReadOnlyTextPropertyDescriptor(DESCRIPTION, DESCRIPTION)
            };
        }

        @Override
        public Object getPropertyValue(Object id) {
            if (DESCRIPTION.equals(id)) {
                ITmfEventField description = fEventField.getField(BTFPayload.DESCRIPTION);
                return description == null ? null : description.getValue();
            }
            return null;
        }

        @Override
        public boolean isPropertySet(Object id) {
            return false;
        }

        @Override
        public void resetPropertyValue(Object id) {
            // Do nothing
        }

        @Override
        public void setPropertyValue(Object id, Object value) {
            // Do nothing
        }

    }

    @Override
    public Object getPropertyValue(Object id) {
        if (id instanceof String) {
            String id2 = (String) id;
            final ITmfEventField content = fEvent.getContent();
            switch (id2) {
            case ID_EVENT_SOURCE:
                String source = fEvent.getSource();
                ITmfEventField sourceInstance = content.getField(BtfColumnNames.SOURCE_INSTANCE.toString());
                return new EntityPropertySource(source, sourceInstance.getValue().toString());
            case ID_EVENT_TYPE:
                return new TypePropertySource(fEvent.getType().getName(), fEvent.getEventDescription());
            case ID_EVENT_TARGET:
                String target = fEvent.getTarget();
                ITmfEventField targetInstance = content.getField(BtfColumnNames.TARGET_INSTANCE.toString());
                return new EntityPropertySource(target, targetInstance.getValue().toString());
            case ID_EVENT_EVENT:
                ITmfEventField event = content.getField(BtfColumnNames.EVENT.toString());
                return event == null ? null : new EventPropertySource(event);
            case ID_EVENT_NOTES:
                ITmfEventField notes = content.getField(BtfColumnNames.NOTES.toString());
                return notes == null ? null : notes.getValue();
            default:
                break;
            }
        }
        return super.getPropertyValue(id);
    }
}
