/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.stream;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.childTypeError;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypeAliasParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypedefParser;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;

/**
 * A stream is a collection of packets with events in them. It will contain data
 * types.
 *
 * @author Matthew Khouzam
 *
 */
public final class StreamParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter Object with a trace and scope
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {

        private final DeclarationScope fCurrentScope;
        private final CTFTrace fTrace;

        /**
         * Constructor
         *
         * @param trace
         *            the trace
         * @param currentScope
         *            the scope
         */
        public Param(CTFTrace trace, DeclarationScope currentScope) {
            fTrace = trace;
            fCurrentScope = currentScope;
        }

    }

    /**
     * The instance
     */
    public static final StreamParser INSTANCE = new StreamParser();

    private StreamParser() {
    }

    /**
     * Parses an stream declaration and returns a stream type
     *
     * @param streamNode
     *            the steam node
     * @param param
     *            the parameter object
     *
     * @return The corresponding enum declaration.
     * @throws ParseException
     *             badly defined stream
     */
    @Override
    public CTFStream parse(CommonTree streamNode, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        Param parameter = (Param) param;
        CTFTrace trace = ((Param) param).fTrace;
        CTFStream stream = new CTFStream(trace);

        List<CommonTree> children = streamNode.getChildren();
        if (children == null) {
            throw new ParseException("Empty stream block"); //$NON-NLS-1$
        }

        DeclarationScope scope = new DeclarationScope(parameter.fCurrentScope, MetadataStrings.STREAM);

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEALIAS:
                TypeAliasParser.INSTANCE.parse(child, new TypeAliasParser.Param(trace, scope));
                break;
            case CTFParser.TYPEDEF:
                TypedefParser.INSTANCE.parse(child, new TypedefParser.Param(trace, scope));
                break;
            case CTFParser.CTF_EXPRESSION_TYPE:
            case CTFParser.CTF_EXPRESSION_VAL:
                StreamDeclarationParser.INSTANCE.parse(child, new StreamDeclarationParser.Param(trace, stream, scope));
                break;
            default:
                throw childTypeError(child);
            }
        }

        if (stream.isIdSet() &&
                (!trace.packetHeaderIsSet() || !trace.getPacketHeader().hasField(MetadataStrings.STREAM_ID))) {
            throw new ParseException("Stream has an ID, but there is no stream_id field in packet header."); //$NON-NLS-1$
        }

        trace.addStream(stream);

        return stream;
    }

}
