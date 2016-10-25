/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.callsite;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.CTFCallsite;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;

/**
 * Callsite as described in section 7.4 of the TSDL spec in CTF 1.8.2
 *
 * <pre>
 *
 * callsite {
 *    name = "event_name";
 *    func = "func_name";
 *    file = "myfile.c";
 *    line = 39;
 *    ip = 0x40096c;
 * };
 * </pre>
 *
 * @author Matthew Khouzam
 *
 */
public final class CallSiteParser implements ICommonTreeParser {

    /**
     * Instance of the parser
     */
    public static CallSiteParser INSTANCE = new CallSiteParser();

    private static final @NonNull String LINE = "line"; //$NON-NLS-1$
    private static final @NonNull String FILE = "file"; //$NON-NLS-1$
    private static final @NonNull String IP = "ip"; //$NON-NLS-1$
    private static final @NonNull String FUNC = "func"; //$NON-NLS-1$
    private static final @NonNull String NAME = "name"; //$NON-NLS-1$

    private CallSiteParser() {
        // do nothing
    }

    @Override
    public @NonNull CTFCallsite parse(CommonTree tree, ICommonTreeParserParameter param) throws ParseException {
        List<CommonTree> children = tree.getChildren();
        String name = null;
        String funcName = null;
        long lineNumber = -1;
        long ip = -1;
        String fileName = null;

        for (CommonTree child : children) {
            String left;
            /* this is a regex to find the leading and trailing quotes */
            final String regex = "^\"|\"$"; //$NON-NLS-1$
            /*
             * this is to replace the previous quotes with nothing...
             * effectively deleting them
             */
            final String nullString = ""; //$NON-NLS-1$
            left = child.getChild(0).getChild(0).getChild(0).getText();
            if (left.equals(NAME)) {
                name = child.getChild(1).getChild(0).getChild(0).getText().replaceAll(regex, nullString);
            } else if (left.equals(FUNC)) {
                funcName = child.getChild(1).getChild(0).getChild(0).getText().replaceAll(regex, nullString);
            } else if (left.equals(IP)) {
                ip = Long.decode(child.getChild(1).getChild(0).getChild(0).getText());
            } else if (left.equals(FILE)) {
                fileName = child.getChild(1).getChild(0).getChild(0).getText().replaceAll(regex, nullString);
            } else if (left.equals(LINE)) {
                lineNumber = Long.parseLong(child.getChild(1).getChild(0).getChild(0).getText());
            }
        }
        return new CTFCallsite(name, funcName, ip, fileName, lineNumber);
    }

}
