/**
 * 
 */
package org.eclipse.linuxtools.lttng.core.state.evProcessor.state;

import org.eclipse.linuxtools.lttng.core.TraceDebug;
import org.eclipse.linuxtools.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.lttng.core.event.LttngEventContent;
import org.eclipse.linuxtools.lttng.core.event.LttngEventField;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.Fields;
import org.eclipse.linuxtools.lttng.core.state.model.LttngProcessState;
import org.eclipse.linuxtools.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

/**
 * Common utility methods for all state processing handlers, not intended to be
 * instantiated on its own
 * 
 * @author alvaro
 * 
 */
public abstract class AbsStateProcessing {

	/**
	 * protected method used when a Field is requested among several available
	 * fields and the expected type is Long
	 * 
	 * @param trcEvent
	 * @param traceSt
	 * @param expectedNumFields
	 * @return
	 */
	protected Long getAFieldLong(LttngEvent trcEvent, LttngTraceState traceSt, Fields expectedField) {
		Long fieldVal = 0L;
		
        String fieldname = expectedField.getInName();
		LttngEventField field = (LttngEventField) ((LttngEventContent) trcEvent.getContent()).getField(fieldname);
		
		if ( field == null ) {
			TraceDebug.debug("***************** CONTENT : " + ((LttngEventContent) trcEvent.getContent()).toString()); //$NON-NLS-1$
		}
		else {
            Object fieldObj = field.getValue();
            if ( (fieldObj instanceof Long) || (fieldObj instanceof Integer) ) {
                // Expected numeric value found
                fieldVal = (Long) field.getValue();
            } 
            else {
                if (TraceDebug.isDEBUG()) {
                    TraceDebug.debug("Unexpected field Type. Expected: Long, Received: "+ fieldObj.getClass().getSimpleName()); //$NON-NLS-1$
                }
            }
		}
		
		/*
		// TmfEventField[] fields = trcEvent.getContent().getFields();
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
		*/
	
//		if (fieldVal == null) {
//			if (TraceDebug.isDEBUG()) {
//				sendNoFieldFoundMsg(((LttngEventContent) trcEvent.getContent()).getFields(), fieldname);
//			}
//		}
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
		String fieldVal = ""; //$NON-NLS-1$
		
		String fieldname = expectedField.getInName();
        LttngEventField field = (LttngEventField) ((LttngEventContent) trcEvent.getContent()).getField(fieldname);
        
		if ( field == null ) {
			TraceDebug.debug("***************** CONTENT : " + ((LttngEventContent) trcEvent.getContent()).toString()); //$NON-NLS-1$
		}
		else {
	        Object fieldObj = field.getValue();
	        if (fieldObj instanceof String) {
	            // Expected numeric value found
	            fieldVal = (String) field.getValue();
	        } 
	        else {
	            if (TraceDebug.isDEBUG()) {
	                TraceDebug.debug("Unexpected field Type. Expected: String, Received: "+ fieldObj.getClass().getSimpleName()); //$NON-NLS-1$
	            }
	        }
		}
		
		/*
		// TmfEventField[] fields = trcEvent.getContent().getFields();
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
	    */
	    
//        if (fieldVal == null) {
//            if (TraceDebug.isDEBUG()) {
//                sendNoFieldFoundMsg(((LttngEventContent) trcEvent.getContent()).getFields(), fieldname);
//            }
//        }
		return fieldVal;
	}

	/**
	 * Find the process matching the given pid and cpu
	 * 
	 * If cpu is 0, the cpu value is not matched and the selection is based on
	 * pid value only
	 * 
	 * @param traceState
	 * @param cpu
	 * @param pid
	 * @return
	 */
	protected LttngProcessState lttv_state_find_process(
							LttngTraceState traceState, Long cpu, Long pid) {

		return traceState.findProcessState(pid, cpu, traceState.getTraceId());
	}

	@SuppressWarnings("nls")
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
