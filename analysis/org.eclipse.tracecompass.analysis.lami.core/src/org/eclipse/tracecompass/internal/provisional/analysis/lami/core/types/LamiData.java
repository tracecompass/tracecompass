/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.LamiStrings;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;

/**
 * Base class for data types allowed in LAMI analysis scripts JSON output.
 *
 * @author Alexandre Montplaisir
 * @author Philippe Proulx
 */
public abstract class LamiData {

    /**
     * Enum of all the valid data types
     */
    @SuppressWarnings("javadoc")
    public enum DataType {

        /* Generic JSON types */
        STRING("string", "Value", false, null, LamiString.class), //$NON-NLS-1$ //$NON-NLS-2$
        INT("int", "Value", true, null, LamiInteger.class), //$NON-NLS-1$ //$NON-NLS-2$
        FLOAT("float", "Value", true, null, LamiNumber.class), //$NON-NLS-1$ //$NON-NLS-2$
        NUMBER("number", "Value", true, null, LamiNumber.class), //$NON-NLS-1$ //$NON-NLS-2$
        BOOL("bool", "Value", false, null, LamiBoolean.class), //$NON-NLS-1$ //$NON-NLS-2$

        /* Lami-specific data types */
        RATIO("ratio", "Ratio", true, "%", LamiRatio.class), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        TIMESTAMP("timestamp", "Timestamp", true, "ns", LamiTimestamp.class), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        TIME_RANGE("time-range", "Time range", true, null, LamiTimeRange.class), //$NON-NLS-1$ //$NON-NLS-2$
        DURATION("duration", "Duration", true, "ns", LamiDuration.class), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SIZE("size", "Size", true, Messages.LamiData_UnitBytes, LamiSize.class), //$NON-NLS-1$ //$NON-NLS-2$
        BITRATE("bitrate", "Bitrate", true, Messages.LamiData_UnitBitsPerSecond, LamiBitrate.class), //$NON-NLS-1$ //$NON-NLS-2$
        SYSCALL("syscall", "System call", false, null, LamiSystemCall.class), //$NON-NLS-1$ //$NON-NLS-2$
        PROCESS("process", "Process", false, null, LamiProcess.class), //$NON-NLS-1$ //$NON-NLS-2$
        PATH("path", "Path", false, null, LamiPath.class), //$NON-NLS-1$ //$NON-NLS-2$
        FD("fd", "File descriptor", false, null, LamiFileDescriptor.class), //$NON-NLS-1$ //$NON-NLS-2$
        IRQ("irq", "IRQ", false, null, LamiIRQ.class), //$NON-NLS-1$ //$NON-NLS-2$
        CPU("cpu", "CPU", false, null, LamiCPU.class), //$NON-NLS-1$ //$NON-NLS-2$
        DISK("disk", "Disk", false, null, LamiDisk.class), //$NON-NLS-1$ //$NON-NLS-2$
        PART("part", "Disk partition", false, null, LamiDiskPartition.class), //$NON-NLS-1$ //$NON-NLS-2$
        NETIF("netif", "Network interface", false, null, LamiNetworkInterface.class), //$NON-NLS-1$ //$NON-NLS-2$
        UNKNOWN("unknown", "Value", false, null, LamiUnknown.class), //$NON-NLS-1$ //$NON-NLS-2$
        MIXED("mixed", "Value", false, null, null); //$NON-NLS-1$ //$NON-NLS-2$

        private final String fName;
        private final String fTitle;
        private final boolean fIsContinuous;
        private final @Nullable String fUnits;
        private final @Nullable Class<?> fClass;

        private DataType(String name, String title, boolean isContinous, @Nullable String units, @Nullable Class<?> cls) {
            fName = name;
            fTitle = title;
            fIsContinuous = isContinous;
            fUnits = units;
            fClass = cls;
        }

        /**
         * Indicates if this data type represents a continuous numerical value.
         *
         * For example, time or bitrates are continuous values, but CPU or IRQ
         * numbers are not (you can't have CPU 1.5!)
         *
         * @return If this aspect is continuous
         */
        public boolean isContinuous() {
            return fIsContinuous;
        }

        /**
         * Get the units of this data type, if any.
         *
         * @return The units, or <code>null</code> if there are no units
         */
        public @Nullable String getUnits() {
            return fUnits;
        }

        /**
         * The default title for columns containing these units.
         *
         * @return The data type's column title
         */
        public String getTitle() {
            return fTitle;
        }

        /**
         * Get the data type from its JSON string representation.
         *
         * @param value
         *            The string
         * @return The corresponding data type
         */
        public static DataType fromString(String value) {
            for (DataType type : DataType.values()) {
                if (type.fName.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unrecognized type: " + value);
        }

        /**
         * Get the date type enum element from its implementation Class.
         *
         * @param cls
         *            The data type class
         * @return The data type
         */
        public static @Nullable DataType fromClass(Class<? extends LamiData> cls) {
            for (DataType type : DataType.values()) {
                if (cls.equals(type.fClass)) {
                    return type;
                }
            }

            return null;
        }
    }

    @Override
    public abstract @Nullable String toString();

    // ------------------------------------------------------------------------
    // Convenience methods
    // ------------------------------------------------------------------------

    /**
     * Convenience method to get the "value" field from a JSON object. Many LAMI
     * types have a "value" field.
     *
     * @param obj
     *            The JSON object
     * @return The read value
     * @throws JSONException
     *             If the object does not actually have a "value" field.
     */
    private static final long getJSONObjectLongValue(JSONObject obj) throws JSONException {
        return obj.getLong(LamiStrings.VALUE);
    }

    /**
     * Convenience method to get the "name" field from a JSON object. Many LAMI
     * types have a "nam" field.
     *
     * @param obj
     *            The JSON object
     * @return The read name
     * @throws JSONException
     *             If the object does not actually have a "name" field.
     */
    private static final String getJSONObjectStringName(JSONObject obj) throws JSONException {
        return checkNotNull(obj.getString(LamiStrings.NAME));
    }

    // ------------------------------------------------------------------------
    // "Factory" methods and helpers
    // ------------------------------------------------------------------------

    /**
     * Factory method to build a new LamiData object from either a
     * {@link JSONObject} or a standard Java {@link Object} representing a
     * primitive type.
     *
     * @param obj
     *            The source object
     * @return The corresponding LamiData object
     * @throws JSONException
     *             If the object type is not supported
     */
    public static LamiData createFromObject(Object obj) throws JSONException {
        if (obj instanceof JSONObject) {
            return createFromJsonObject((JSONObject) obj);
        } else if (obj.equals(JSONObject.NULL)) {
            return LamiEmpty.INSTANCE;
        } else {
            return createFromPrimitiveObject(obj);
        }
    }

    @FunctionalInterface
    private static interface CheckedJSONExceptionFunction<T, R> {
       R apply(T t) throws JSONException;
    }

    /**
     * Map returning the Functions to build new LAMI objects for JSON primitive
     * types
     */
    private static final Map<Class<?>, Function<Object, LamiData>> PRIMITIVE_TYPE_GENERATOR;
    static {
        ImmutableMap.Builder<Class<?>, Function<Object, LamiData>> primitiveTypeGenBuilder = ImmutableMap.builder();
        primitiveTypeGenBuilder.put(Boolean.class, (o) -> LamiBoolean.instance((Boolean) o));
        primitiveTypeGenBuilder.put(Integer.class, (o) -> new LamiInteger(((Integer) o).longValue()));
        primitiveTypeGenBuilder.put(Long.class, (o) -> new LamiInteger((Long) o));
        primitiveTypeGenBuilder.put(Double.class, (o) -> new LamiNumber((Double) o));
        primitiveTypeGenBuilder.put(String.class, (o) -> new LamiString((String) o));
        PRIMITIVE_TYPE_GENERATOR = primitiveTypeGenBuilder.build();
    }

    /**
     * Map returning the Functions to build new LAMI objects for LAMI-specific
     * types
     */
    private static final Map<String, CheckedJSONExceptionFunction<JSONObject, LamiData>> COMPLEX_TYPE_GENERATOR;
    static {
        ImmutableMap.Builder<String, CheckedJSONExceptionFunction<JSONObject, LamiData>> complexTypeGenBuilder = ImmutableMap.builder();
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_BITRATE, (obj) -> new LamiBitrate(getJSONObjectLongValue(obj)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_CPU, (obj) -> new LamiCPU(obj.getLong(LamiStrings.ID)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_DISK, (obj) -> new LamiDisk(getJSONObjectStringName(obj)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_DURATION, (obj) -> new LamiDuration(getJSONObjectLongValue(obj)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_PART, (obj) -> new LamiDiskPartition(getJSONObjectStringName(obj)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_FD, (obj) -> new LamiFileDescriptor(obj.getLong(LamiStrings.FD)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_NETIF, (obj) -> new LamiNetworkInterface(getJSONObjectStringName(obj)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_PATH, (obj) -> new LamiPath(checkNotNull(obj.getString(LamiStrings.PATH))));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_PROCESS, (obj) -> {
            String name = obj.optString(LamiStrings.NAME);
            Long pid = (obj.has(LamiStrings.PID) ? obj.getLong(LamiStrings.PID) : null);
            Long tid = (obj.has(LamiStrings.TID) ? obj.getLong(LamiStrings.TID) : null);

            return new LamiProcess(name, pid, tid);
        });
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_RATIO, (obj) -> new LamiRatio(obj.getDouble(LamiStrings.VALUE)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_IRQ, (obj) -> {
            LamiIRQ.Type irqType = LamiIRQ.Type.HARD;

            if (obj.has(LamiStrings.HARD)) {
                boolean isHardIrq = obj.getBoolean(LamiStrings.HARD);
                irqType = (isHardIrq ? LamiIRQ.Type.HARD : LamiIRQ.Type.SOFT);
            }

            int nr = obj.getInt(LamiStrings.NR);
            String name = obj.optString(LamiStrings.NAME);

            return new LamiIRQ(irqType, nr, name);
        });
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_SIZE, (obj) -> new LamiSize(getJSONObjectLongValue(obj)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_SYSCALL, (obj) -> new LamiSystemCall(getJSONObjectStringName(obj)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_TIME_RANGE, (obj) -> {
            long begin = obj.getLong(LamiStrings.BEGIN);
            long end = obj.getLong(LamiStrings.END);

            return new LamiTimeRange(begin, end);
        });
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_TIMESTAMP, (obj) -> new LamiTimestamp(getJSONObjectLongValue(obj)));
        complexTypeGenBuilder.put(LamiStrings.DATA_CLASS_UNKNOWN, (obj) -> LamiUnknown.INSTANCE);
        COMPLEX_TYPE_GENERATOR = complexTypeGenBuilder.build();
    }

    /**
     * Create a new LamiData for a primitive type (Integer, String, etc.)
     *
     * @param obj
     *            The source object
     * @return A new corresponding LamiData object
     * @throws JSONException
     *             If the object type is not supported
     */
    private static LamiData createFromPrimitiveObject(Object obj) throws JSONException {
        Function<Object, LamiData> func = PRIMITIVE_TYPE_GENERATOR.get(obj.getClass());
        if (func == null) {
            throw new JSONException("Unhandled type: " + obj.toString() + " of type " + obj.getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        /* We never return null in the implementations */
        return checkNotNull(func.apply(obj));
    }

    /**
     * Create a new LamiData for a LAMI-specific type from a {@link JSONObject}.
     *
     * @param obj
     *            The source object
     * @return A new corresponding LamiData object
     * @throws JSONException
     *             If the object type is not supported
     */
    private static LamiData createFromJsonObject(JSONObject obj) throws JSONException {
        String dataClass = obj.optString(LamiStrings.CLASS);

        if (dataClass == null) {
            throw new JSONException("Cannot find data class"); //$NON-NLS-1$
        }

        CheckedJSONExceptionFunction<JSONObject, LamiData> func = COMPLEX_TYPE_GENERATOR.get(dataClass);

        if (func == null) {
            throw new JSONException(String.format("Unsupported data class \"%s\"", dataClass)); //$NON-NLS-1$
        }

        return func.apply(obj);
    }
}
