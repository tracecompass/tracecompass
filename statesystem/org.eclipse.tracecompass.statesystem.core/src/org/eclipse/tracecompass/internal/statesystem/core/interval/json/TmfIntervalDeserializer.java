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

import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferReader;
import org.eclipse.tracecompass.datastore.core.serialization.SafeByteBufferFactory;
import org.eclipse.tracecompass.internal.provisional.statesystem.core.statevalue.CustomStateValue;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * GSON State Interval serializer
 *
 * @author Matthew Khouzam
 */
public class TmfIntervalDeserializer implements JsonDeserializer<ITmfStateInterval> {

    @Override
    public ITmfStateInterval deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject object = json.getAsJsonObject();
        long start = object.get(TmfIntervalStrings.START).getAsLong();
        long end = object.get(TmfIntervalStrings.END).getAsLong();
        int quark = object.get(TmfIntervalStrings.QUARK).getAsInt();
        String type = object.get(TmfIntervalStrings.TYPE).getAsString();
        if (type.equals(TmfIntervalStrings.NULL)) {
            return new TmfStateInterval(start, end, quark, (Object) null);
        }
        JsonElement value = object.get(TmfIntervalStrings.VALUE);
        try {
            Class<?> typeClass = Class.forName(type);

            if (typeClass.isAssignableFrom(CustomStateValue.class)) {
                String encoded = value.getAsString();
                byte[] serialized = Base64.getDecoder().decode(encoded);
                ByteBuffer buffer = ByteBuffer.wrap(serialized);
                ISafeByteBufferReader sbbr = SafeByteBufferFactory.wrapReader(buffer, serialized.length);
                TmfStateValue sv = CustomStateValue.readSerializedValue(sbbr);
                return new TmfStateInterval(start, end, quark, sv.unboxValue());
            }
            if (typeClass.isAssignableFrom(Integer.class)) {
                return new TmfStateInterval(start, end, quark, value.getAsInt());
            } else if (typeClass.isAssignableFrom(Long.class)) {
                return new TmfStateInterval(start, end, quark, value.getAsLong());
            } else if (typeClass.isAssignableFrom(Double.class)) {
                return new TmfStateInterval(start, end, quark, value.getAsDouble());
            } else if (typeClass.isAssignableFrom(String.class)) {
                return new TmfStateInterval(start, end, quark, value.getAsString());
            }
        } catch (ClassNotFoundException e) {
            // Fall through
        }
        // last ditch attempt
        return new TmfStateInterval(start, end, quark, value.toString());
    }

}
