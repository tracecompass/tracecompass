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
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.event;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isAnyUnaryString;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStream;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypeSpecifierListParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryIntegerParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryStringParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.stream.StreamIdParser;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;

/**
 * The declaration of an event
 *
 * @author Matthew Khouzam - Initial API and implementation
 *
 */
public final class EventDeclarationParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter object, contains a trace, an event and a scope
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final EventDeclaration fEvent;
        private final CTFTrace fTrace;
        private final DeclarationScope fDeclarationScope;

        /**
         * Constructor
         *
         * @param trace
         *            the trace
         * @param event
         *            the event to populate
         * @param scope
         *            the current scope
         */
        public Param(CTFTrace trace, EventDeclaration event, DeclarationScope scope) {
            fTrace = trace;
            fEvent = event;
            fDeclarationScope = scope;
        }

    }

    /**
     * Instance
     */
    public static final EventDeclarationParser INSTANCE = new EventDeclarationParser();

    private EventDeclarationParser() {
    }

    @Override
    public EventDeclaration parse(CommonTree eventDecl, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        DeclarationScope scope = ((Param) param).fDeclarationScope;
        EventDeclaration event = ((Param) param).fEvent;
        CTFTrace fTrace = ((Param) param).fTrace;

        /* There should be a left and right */

        CommonTree leftNode = (CommonTree) eventDecl.getChild(0);
        CommonTree rightNode = (CommonTree) eventDecl.getChild(1);

        List<CommonTree> leftStrings = leftNode.getChildren();

        if (!isAnyUnaryString(leftStrings.get(0))) {
            throw new ParseException("Left side of CTF assignment must be a string"); //$NON-NLS-1$
        }

        String left = concatenateUnaryStrings(leftStrings);

        if (left.equals(MetadataStrings.NAME2)) {
            if (event.nameIsSet()) {
                throw new ParseException("name already defined"); //$NON-NLS-1$
            }

            String name = EventNameParser.INSTANCE.parse(rightNode, null);

            event.setName(name);
        } else if (left.equals(MetadataStrings.ID)) {
            if (event.idIsSet()) {
                throw new ParseException("id already defined"); //$NON-NLS-1$
            }

            long id = EventIDParser.INSTANCE.parse(rightNode, null);
            if (id > Integer.MAX_VALUE) {
                throw new ParseException("id is greater than int.maxvalue, unsupported. id : " + id); //$NON-NLS-1$
            }
            if (id < 0) {
                throw new ParseException("negative id, unsupported. id : " + id); //$NON-NLS-1$
            }
            event.setId((int) id);
        } else if (left.equals(MetadataStrings.STREAM_ID)) {
            if (event.streamIsSet()) {
                throw new ParseException("stream id already defined"); //$NON-NLS-1$
            }

            long streamId = StreamIdParser.INSTANCE.parse(rightNode, null);

            /*
             * If the event has a stream and it is defined, look up the stream,
             * if it is the available, assign it. If not, the event is
             * malformed.
             */
            ICTFStream iStream = fTrace.getStream(streamId);
            if (!(iStream instanceof CTFStream)) {
                throw new ParseException("Event specified stream with ID " + streamId + ". But no stream with that ID was defined"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            CTFStream ctfStream = (CTFStream) iStream;
            event.setStream(ctfStream);
        } else if (left.equals(MetadataStrings.CONTEXT)) {
            if (event.contextIsSet()) {
                throw new ParseException("context already defined"); //$NON-NLS-1$
            }

            CommonTree typeSpecifier = (CommonTree) rightNode.getChild(0);

            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("context expects a type specifier"); //$NON-NLS-1$
            }

            IDeclaration contextDecl = TypeSpecifierListParser.INSTANCE.parse(typeSpecifier, new TypeSpecifierListParser.Param(fTrace, null, null, scope));

            if (!(contextDecl instanceof StructDeclaration)) {
                throw new ParseException("context expects a struct"); //$NON-NLS-1$
            }

            event.setContext((StructDeclaration) contextDecl);
        } else if (left.equals(MetadataStrings.FIELDS_STRING)) {
            if (event.fieldsIsSet()) {
                throw new ParseException("fields already defined"); //$NON-NLS-1$
            }

            CommonTree typeSpecifier = (CommonTree) rightNode.getChild(0);

            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("fields expects a type specifier"); //$NON-NLS-1$
            }

            IDeclaration fieldsDecl;
            fieldsDecl = TypeSpecifierListParser.INSTANCE.parse(typeSpecifier, new TypeSpecifierListParser.Param(fTrace, null, null, scope));

            if (!(fieldsDecl instanceof StructDeclaration)) {
                throw new ParseException("fields expects a struct"); //$NON-NLS-1$
            }
            /*
             * The underscores in the event names. These underscores were added
             * by the LTTng tracer.
             */
            final StructDeclaration fields = (StructDeclaration) fieldsDecl;
            event.setFields(fields);
        } else if (left.equals(MetadataStrings.LOGLEVEL2)) {
            long logLevel = UnaryIntegerParser.INSTANCE.parse((CommonTree) rightNode.getChild(0), null);
            event.setLogLevel(logLevel);
        } else {
            /* Custom event attribute, we'll add it to the attributes map */
            String right = UnaryStringParser.INSTANCE.parse((CommonTree) rightNode.getChild(0), null);
            event.setCustomAttribute(left, right);
        }
        return event;
    }
}
