/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Alvaro Sanchez-Leon - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.common;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngEventContent;
import org.eclipse.linuxtools.lttng.event.LttngEventField;
import org.eclipse.linuxtools.lttng.state.StateStrings.Fields;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.tmf.event.TmfEventField;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;

public abstract class AbsTRangeUpdate implements IEventProcessing {

    private static final long MINORBITS = 20;
    
	// ========================================================================
	// General methods
	// =======================================================================
    
    /**
     * Get the mkdev node id<br>
     * <br>
     * This is an implementation of a KERNEL macro used in Lttv
     * 
     */
    public long getMkdevId(long major, long minor) {
        return (((major) << MINORBITS) | (minor));
    }

	/**
	 * Get the pixels per Nano second, either from active widgets or initialise
	 * with the experiment time range values
	 * 
	 * @param traceSt
	 * @param params
	 * 
	 * @return double
	 */
    protected double getPixelsPerNs(LttngTraceState traceSt, ParamsUpdater params) {
        double pixPerNs = params.getPixelsPerNs();
        if (pixPerNs == 0) {
            TmfTimeRange tsetRange = traceSt.getInputDataRef().getExperimentTimeWindow();
            
            long startTime = tsetRange.getStartTime().getValue();
            long endTime = tsetRange.getEndTime().getValue();
            long delta = endTime - startTime;
            
            if (delta > 0) {
                pixPerNs = (double) params.getWidth() / (double) delta;
                params.setPixelsPerNs(pixPerNs);
            }
        }
        return pixPerNs;
    }
    
    /**
	 * protected method used when only one Field is expected with Type "Long" if
	 * the number of fields is greater, the first field is returned and a
	 * tracing message is sent Null is returned if the value could not be
	 * extracted.
	 * 
	 * @param trcEvent
	 * @param traceSt
	 * @param expectedNumFields
	 * @return
	 */
	protected Long getDField(LttngEvent trcEvent, LttngTraceState traceSt,
			Fields expectedField) {
		Long fieldVal = null;
		TmfEventField[] fields = trcEvent.getContent().getFields();
		String[] fieldLabels = trcEvent.getContent().getFormat().getLabels();

		// Only one field expected
		if (fields.length != 1 || fieldLabels.length != 1) {
			StringBuilder sb = new StringBuilder(
					"Unexpected number of fields received: " + fields.length
							+ " for Event: " + trcEvent.getMarkerName()
							+ "\n\t\tFields: ");

			for (TmfEventField field : fields) {
				sb.append(((LttngEventField) field).getName() + " ");
			}

			TraceDebug.debug(sb.toString());
			if (fields.length == 0) {
				return null;
			}
		}

		LttngEventField field = (LttngEventField) fields[0];
		String fieldname = field.getName();
		String expectedFieldName = expectedField.getInName();
		if (fieldname.equals(expectedFieldName)) {
			Object fieldObj = field.getValue();
			if (fieldObj instanceof Long) {
				// Expected value found
				fieldVal = (Long) field.getValue();
			} else {
				if (TraceDebug.isDEBUG()) {
					TraceDebug
							.debug("Unexpected field Type. Expected: Long, Received: "
									+ fieldObj.getClass().getSimpleName());
				}
				return null;
			}
		} else {
			TraceDebug.debug("Unexpected field received: " + fieldname
					+ " Expected: " + expectedFieldName);
			return null;
		}

		return fieldVal;
	}

	/**
	 * protected method used when a Field is requested among several available
	 * fields and the expected type is Long
	 * 
	 * @param trcEvent
	 * @param traceSt
	 * @param expectedNumFields
	 * @return
	 */
	protected Long getAFieldLong(LttngEvent trcEvent, LttngTraceState traceSt,
			Fields expectedField) {
		Long fieldVal = null;
		TmfEventField[] fields = ((LttngEventContent) trcEvent.getContent())
				.getFields(trcEvent);

		// At least one field expected
		if (fields.length == 0) {
			TraceDebug.debug("Unexpected number of fields received: "
					+ fields.length);
			return null;
		}

		LttngEventField field;
		String fieldname;
		String expectedFieldName = expectedField.getInName();
		for (int i = 0; i < fields.length; i++) {
			field = (LttngEventField) fields[i];
			fieldname = field.getName();
			if (fieldname.equals(expectedFieldName)) {
				Object fieldObj = field.getValue();
				if (fieldObj instanceof Long) {
					// Expected value found
					fieldVal = (Long) field.getValue();
					// if (expectedField == Fields.LTT_FIELD_TYPE) {
					// TraceDebug.debug("Field Type value is: " + fieldVal);
					// }
					break;
				} else {
					if (TraceDebug.isDEBUG()) {
						TraceDebug
								.debug("Unexpected field Type. Expected: Long, Received: "
										+ fieldObj.getClass().getSimpleName());
					}
					return null;
				}
			}
		}

		if (fieldVal == null) {
			if (TraceDebug.isDEBUG()) {
				sendNoFieldFoundMsg(fields, expectedFieldName);
			}
		}
		return fieldVal;
	}

	/**
	 * protected method used when a Field is requested among several available
	 * fields and the expected type is String
	 * 
	 * @param trcEvent
	 * @param traceSt
	 * @param expectedNumFields
	 * @return
	 */
	protected String getAFieldString(LttngEvent trcEvent,
			LttngTraceState traceSt, Fields expectedField) {
		String fieldVal = null;

		TmfEventField[] fields = ((LttngEventContent) trcEvent.getContent())
				.getFields(trcEvent);

		// Only one field expected
		if (fields.length == 0) {
			TraceDebug.debug("Unexpected number of fields received: "
					+ fields.length);
			return null;
		}

		LttngEventField field;
		String fieldname;
		String expectedFieldName = expectedField.getInName();
		for (int i = 0; i < fields.length; i++) {
			field = (LttngEventField) fields[i];
			fieldname = field.getName();
			if (fieldname.equals(expectedFieldName)) {
				Object fieldObj = field.getValue();
				if (fieldObj instanceof String) {
					// Expected value found
					fieldVal = (String) field.getValue();
					break;
				} else {
					if (TraceDebug.isDEBUG()) {
						TraceDebug
								.debug("Unexpected field Type. Expected: String, Received: "
										+ fieldObj.getClass().getSimpleName());
					}
					return null;
				}
			}
		}

		if (fieldVal == null) {
			if (TraceDebug.isDEBUG()) {
				sendNoFieldFoundMsg(fields, expectedFieldName);
			}
		}
		return fieldVal;
	}

	protected void sendNoFieldFoundMsg(TmfEventField[] fields,
			String expectedFieldName) {
		LttngEventField field;
		StringBuilder sb = new StringBuilder("Field not found, requested: "
				+ expectedFieldName);
		sb.append(" number of fields: " + fields.length + "Fields: ");
		for (int i = 0; i < fields.length; i++) {
			field = (LttngEventField) fields[i];
			sb.append(field.getName() + " ");
		}

		TraceDebug.debug(sb.toString(), 5);
	}
	
}