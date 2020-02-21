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

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isUnaryString;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryStringParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.event.EventScopeParser;

/**
 * The stream scope parser, this parses a scope of a given stream. It can get
 * the scope of the stream like "stream.packet.header".
 *
 * @author Matthew Khouzam - Initial API and implementation
 *
 */
public final class StreamScopeParser implements ICommonTreeParser {

    /**
     * Parameter object
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final List<CommonTree> fList;

        /**
         * Constructor
         *
         * @param list
         *            a list of commonTrees to parse
         */
        public Param(List<CommonTree> list) {
            fList = list;
        }

    }

    /**
     * Instance
     */
    public static final StreamScopeParser INSTANCE = new StreamScopeParser();

    private StreamScopeParser() {
    }

    /**
     * Parses the scope of the stream and returns a concatenated string of the
     * elements
     *
     * @param unused
     *            unused
     * @param param
     *            the parameter containing a list of ASTs describing the stream
     *            scope
     * @return a string of the scope like "stream.context"
     * @throws ParseException
     *             if the ASTs are malformed
     */
    @Override
    public String parse(CommonTree unused, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        List<@NonNull CommonTree> lengthChildren = ((Param) param).fList;
        final List<@NonNull CommonTree> sublist = lengthChildren.subList(1, lengthChildren.size());

        CommonTree nextElem = (CommonTree) lengthChildren.get(1).getChild(0);
        String lengthName = null;
        if (isUnaryString(nextElem)) {
            lengthName = UnaryStringParser.INSTANCE.parse(nextElem, null);
        }

        int type = nextElem.getType();
        if ((CTFParser.tokenNames[CTFParser.EVENT]).equals(lengthName)) {
            type = CTFParser.EVENT;
        }
        switch (type) {
        case CTFParser.IDENTIFIER:
            lengthName = concatenateUnaryStrings(sublist);
            break;
        case CTFParser.EVENT:
            lengthName = EventScopeParser.INSTANCE.parse(null, new EventScopeParser.Param(sublist));
            break;
        default:
            if (lengthName == null) {
                throw new ParseException("Unsupported scope stream." + nextElem); //$NON-NLS-1$
            }
        }
        return MetadataStrings.STREAM + '.' + lengthName;
    }

}
