/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Addition to have more descriptive errors
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.metadata;

import java.lang.reflect.Field;

import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.parser.CTFLexer;

/**
 * CTF Reader exception but dealing with Antlr-specific parsing problems.
 *
 * It is separated from the main {@link CTFException} - and is not part of the
 * API - to isolate the Antlr-specific classes and avoid pushing that dependency
 * to the users of this plugin.
 *
 * @author Matthew Khouzam
 */
public class CtfAntlrException extends CTFException {

    private static final long serialVersionUID = -7078624493350073777L;

    private final int fErrorLine;
    private final String fFile;
    private String fExpectingName = ""; //$NON-NLS-1$
    private int fExpectedValue = -1;
    private String fActualName = ""; //$NON-NLS-1$
    private int fActualValue = -1;

    private final int fCharPositionInLine;

    /**
     * Re-throw the exception but read its data
     *
     * @param e
     *            the previous recognition exception (Antlr specific)
     */
    public CtfAntlrException(MismatchedTokenException e) {
        super(e);
        fErrorLine = e.line;
        fCharPositionInLine = e.charPositionInLine;
        fFile = "metadata"; //$NON-NLS-1$ // we're in CTF, the only thing using antlr is metadata
        parseMismatchedException(e);
    }

    /**
     * Re-throw the exception but read its data
     *
     * @param e
     *            the previous recognition exception (Antlr specific)
     */
    public CtfAntlrException(RecognitionException e) {
        super(e);
        fErrorLine = e.line;
        fCharPositionInLine = e.charPositionInLine;
        fFile = "metadata"; //$NON-NLS-1$ // we're in CTF, the only thing using antlr is metadata
    }

    /**
     * Re-throw the exception but read its data
     *
     * @param e
     *            the previous rewrite exception (Antlr specific)
     */
    public CtfAntlrException(Exception e) {
        super(e);
        fErrorLine = -1;
        fCharPositionInLine = -1;
        fFile = "metadata"; //$NON-NLS-1$ // we're in CTF, the only thing using antlr is metadata
    }

    private void parseMismatchedException(MismatchedTokenException m) {
        // Iterate through the tokens that are hidden in the CTFLexer
        // They are private static final int fields.
        for (Field f : CTFLexer.class.getDeclaredFields()) {
            f.setAccessible(true);
            String name;
            int value;
            try {
                name = f.getName();
                final boolean isInt = (f.getType().isPrimitive());
                if (isInt) {
                    value = ((Integer) f.get(null)).intValue();
                    if (value == m.expecting) {
                        fExpectingName = name;
                        fExpectedValue = value;
                    }
                    if (value == m.getUnexpectedType()) {
                        fActualName = name;
                        fActualValue = value;
                    }
                }
            } catch (NullPointerException e1) {
                // Pokemon, gotta catch em all!
                // actually useful since f may not have a
                // value
            } catch (IllegalArgumentException | IllegalAccessException e1) {
                // Catch these exceptions (reflexion)
            }
            if (!this.fExpectingName.isEmpty() && !this.fActualName.isEmpty()) {
                return;
            }
        }
    }

    @Override
    public String getMessage() {
        final String message = super.getMessage();
        if (fErrorLine == -1) {
            return message;
        }
        String expected = Integer.toString(fExpectedValue);
        String actual = Integer.toString(fActualValue);
        String newMessage = message.replaceAll(expected, fExpectingName);
        newMessage = newMessage.replaceAll(actual, fActualName);
        return newMessage + " at " + fFile + ":" + fErrorLine + ":" + fCharPositionInLine; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
