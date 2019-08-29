/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.model;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;

/**
 * Filter node for the comparison operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterCompareNode extends TmfFilterAspectNode {

    /** compare node name */
    public static final String NODE_NAME = "COMPARE"; //$NON-NLS-1$
    /** not attribute name */
    public static final String NOT_ATTR = "not"; //$NON-NLS-1$
    /** result attribute name */
    public static final String RESULT_ATTR = "result"; //$NON-NLS-1$
    /** type attribute name */
    public static final String TYPE_ATTR = "type"; //$NON-NLS-1$
    /** value attribute name */
    public static final String VALUE_ATTR = "value"; //$NON-NLS-1$

    /**
     * Supported comparison types
     */
    public static enum Type {
        /** numerical comparison type */
        NUM,
        /** alphanumerical comparison type */
        ALPHA,
        /** timestamp comparison type */
        TIMESTAMP
    }


    private int fResult;
    private Type fType = Type.NUM;
    private String fValue;
    private transient Number fValueNumber;
    private transient ITmfTimestamp fValueTimestamp;
    private transient TmfTimestampFormat fTimestampFormat = new TmfTimestampFormat("T.SSSSSSSSS"); //$NON-NLS-1$

    /**
     * @param parent the parent node
     */
    public TmfFilterCompareNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    /**
     * @return the compare result (-1, 0 or 1)
     */
    public int getResult() {
        return fResult;
    }

    /**
     * @param result the compare result (-1, 0 or 1)
     */
    public void setResult(int result) {
        this.fResult = (int) Math.signum(result);
    }

    /**
     * @return the comparison type
     */
    public Type getType() {
        return fType;
    }

    /**
     * @param type the comparison type
     */
    public void setType(Type type) {
        this.fType = type;
        setValue(fValue);
    }

    /**
     * @return the comparison value (in seconds for the TIMESTAMP type)
     */
    public String getValue() {
        return fValue;
    }

    /**
     * @param value the comparison value (in seconds for the TIMESTAMP type)
     */
    public void setValue(String value) {
        this.fValue = value;
        fValueNumber = null;
        fValueTimestamp = null;
        if (value == null) {
            return;
        }
        if (fType == Type.NUM) {
            fValueNumber = toNumber(value);
        } else if (fType == Type.TIMESTAMP) {
            fValueTimestamp = toTimestamp(value);
        }
    }

    /**
     * @return true if the value is valid for the comparison type
     */
    public boolean hasValidValue() {
        if (fType == Type.NUM) {
            return fValueNumber != null;
        } else if (fType == Type.TIMESTAMP) {
            return fValue != null && !fValue.isEmpty() && fValueTimestamp != null;
        }
        return fValue != null;
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        if (event == null || fEventAspect == null) {
            return false;
        }
        Object value = fEventAspect.resolve(event);
        if (value == null) {
            return false;
        }
        if (fType == Type.NUM) {
            if (fValueNumber instanceof Double) {
                Number valueNumber = toNumber(value);
                if (valueNumber != null) {
                    return (Double.compare(valueNumber.doubleValue(), fValueNumber.doubleValue()) == fResult) ^ isNot();
                }
            } else if (fValueNumber != null) {
                Number valueNumber = toNumber(value);
                if (valueNumber instanceof Double || valueNumber instanceof Float) {
                    return (Double.compare(valueNumber.doubleValue(), fValueNumber.doubleValue()) == fResult) ^ isNot();
                } else if (valueNumber != null) {
                    return (Long.compare(valueNumber.longValue(), fValueNumber.longValue()) == fResult) ^ isNot();
                }
            }
        } else if (fType == Type.ALPHA) {
            String valueString = value.toString();
            int comp = (int) Math.signum(valueString.compareTo(fValue));
            return (comp == fResult) ^ isNot();
        } else if (fType == Type.TIMESTAMP) {
            if (fValueTimestamp != null) {
                ITmfTimestamp valueTimestamp = toTimestamp(value);
                if (valueTimestamp != null) {
                    int comp = (int) Math.signum(valueTimestamp.compareTo(fValueTimestamp));
                    return (comp == fResult) ^ isNot();
                }
            }
        }
        return false;
    }

    private static Number toNumber(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        }
        try {
            return Long.decode(value.toString());
        } catch (NumberFormatException e) {
        }
        try {
            return NumberFormat.getInstance().parse(value.toString());
        } catch (ParseException e) {
        }
        return null;
    }

    private ITmfTimestamp toTimestamp(Object value) {
        if (value instanceof ITmfTimestamp) {
            return (ITmfTimestamp) value;
        }
        try {
            return TmfTimestamp.fromNanos(fTimestampFormat.parseValue(value.toString()));
        } catch (ParseException e) {
        }
        return null;
    }

    @Override
    public List<String> getValidChildren() {
        return new ArrayList<>(0);
    }

    @Override
    public String toString(boolean explicit) {
        String result = (fResult == 0 ? "= " : fResult < 0 ? "< " : "> "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String open = (fType == Type.NUM ? "" : fType == Type.ALPHA ? "\"" : "["); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String close = (fType == Type.NUM ? "" : fType == Type.ALPHA ? "\"" : "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return getAspectLabel(explicit) + (isNot() ? " not " : " ") + result + open + fValue + close; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterCompareNode clone = (TmfFilterCompareNode) super.clone();
        clone.setValue(fValue);
        return clone;
    }
}
