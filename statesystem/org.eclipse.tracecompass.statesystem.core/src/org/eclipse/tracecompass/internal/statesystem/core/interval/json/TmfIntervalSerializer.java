/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.interval.json;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Base64;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;
import org.eclipse.tracecompass.datastore.core.serialization.SafeByteBufferFactory;
import org.eclipse.tracecompass.internal.provisional.statesystem.core.statevalue.CustomStateValue;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * GSON State Interval serializer
 *
 * @author Matthew Khouzam
 */
public class TmfIntervalSerializer implements JsonSerializer<ITmfStateInterval> {

    @Override
    public JsonElement serialize(ITmfStateInterval src, @Nullable Type typeOfSrc, @Nullable JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TmfIntervalStrings.START, src.getStartTime());
        jsonObject.addProperty(TmfIntervalStrings.END, src.getEndTime());
        jsonObject.addProperty(TmfIntervalStrings.QUARK, src.getAttribute());
        Object value = src.getValue();
        if (value == null) {
            jsonObject.addProperty(TmfIntervalStrings.TYPE, TmfIntervalStrings.NULL);
            return jsonObject;
        }
        if (value.getClass().isInstance(CustomStateValue.class)) {
            jsonObject.addProperty(TmfIntervalStrings.TYPE, CustomStateValue.class.getName());
        } else {
            jsonObject.addProperty(TmfIntervalStrings.TYPE, value.getClass().getName());
        }
        if (value instanceof CustomStateValue) {
            CustomStateValue customValue = (CustomStateValue) value;
            int size = customValue.getSerializedSize();
            ByteBuffer buffer = ByteBuffer.allocate(size);
            buffer.clear();
            ISafeByteBufferWriter sbbw = SafeByteBufferFactory.wrapWriter(buffer, size);
            customValue.serialize(sbbw);
            byte[] serializedValue = buffer.array();
            jsonObject.addProperty(TmfIntervalStrings.VALUE, Base64.getEncoder().encodeToString(serializedValue));
        } else if (value instanceof Number) {
            Number number = (Number) value;
            jsonObject.addProperty(TmfIntervalStrings.VALUE, number);
        } else if (value instanceof String) {
            jsonObject.addProperty(TmfIntervalStrings.VALUE, (String) value);
        } else {
            jsonObject.addProperty(TmfIntervalStrings.VALUE, value.toString());
        }
        return jsonObject;
    }

}
