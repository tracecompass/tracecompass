/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

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
    private static final String NAME_TIMESTAMP = "Timestamp"; //$NON-NLS-1$
    private static final String NAME_SOURCE = "Source"; //$NON-NLS-1$
    private static final String NAME_TYPE = "Type"; //$NON-NLS-1$
    private static final String NAME_REFERENCE = "Reference"; //$NON-NLS-1$
    private static final String NAME_CONTENT = "Content"; //$NON-NLS-1$

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
            descriptors[0] = new PropertyDescriptor(ID_TIMESTAMP_VALUE, NAME_TIMESTAMP_VALUE);
            descriptors[1] = new PropertyDescriptor(ID_TIMESTAMP_SCALE, NAME_TIMESTAMP_SCALE);
            descriptors[2] = new PropertyDescriptor(ID_TIMESTAMP_PRECISION, NAME_TIMESTAMP_PRECISION);
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
            List<IPropertyDescriptor> descriptors= new ArrayList<IPropertyDescriptor>(fContent.getFields().length);
            for (ITmfEventField field : fContent.getFields()) {
                if (field != null) {
                    descriptors.add(new PropertyDescriptor(field, field.getName()));
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
            return field.getValue();
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
        IPropertyDescriptor[] descriptors = new IPropertyDescriptor[5];
        descriptors[0] = new PropertyDescriptor(ID_TIMESTAMP, NAME_TIMESTAMP);
        descriptors[1] = new PropertyDescriptor(ID_SOURCE, NAME_SOURCE);
        descriptors[2] = new PropertyDescriptor(ID_TYPE, NAME_TYPE);
        descriptors[3] = new PropertyDescriptor(ID_REFERENCE, NAME_REFERENCE);
        descriptors[4] = new PropertyDescriptor(ID_CONTENT, NAME_CONTENT);
        return descriptors;
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
        } else if (id.equals(ID_CONTENT) && fEvent.getContent() != null) {
            return new ContentPropertySource(fEvent.getContent());
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
