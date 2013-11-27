/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Addition to have more descriptive errors
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions;

import java.lang.reflect.Field;

import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.RewriteCardinalityException;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.parser.CTFLexer;

/**
 * CTF Reader exception but dealing with Antlr-specific parsing problems.
 *
 * It is separated from the main {@link CTFReaderException} - and is not part of
 * the API - to isolate the Antlr-specific classes and avoid pushing that
 * dependency to the users of this plugin.
 *
 * @author Matthew Khouzam
 */
public class CtfAntlrException extends CTFReaderException {

    private static final long serialVersionUID = -7078624493350073777L;

    private int fErrorLine = -1;
    private String fFile = ""; //$NON-NLS-1$
    private String fExpectingName = ""; //$NON-NLS-1$
    private int fExpectedValue = -1;
    private String fActualName = ""; //$NON-NLS-1$
    private int fActualValue = -1;

    /**
     * Re-throw the exception but read its data
     *
     * @param e
     *            the previous recognition exception (Antlr specific)
     */
    public CtfAntlrException(RecognitionException e) {
        super(e);
        this.fErrorLine = e.line;
        this.fFile = "metadata"; //$NON-NLS-1$ // we're in CTF, the only thing using antlr is metadata
    }

    /**
     * Re-throw the exception but read its data
     *
     * @param e
     *            the previous recognition exception (Antlr specific)
     */
    public CtfAntlrException(MismatchedTokenException e) {
        super(e);
        this.fErrorLine = e.line;
        this.fFile = "metadata"; //$NON-NLS-1$ // we're in CTF, the only thing using antlr is metadata
        parseMismatchedException(e);
    }

    /**
     * Re-throw the exception but read its data
     *
     * @param e
     *            the previous rewrite exception (Antlr specific)
     */
    public CtfAntlrException(RewriteCardinalityException e) {
        super(e);
        this.fErrorLine = -1;
        this.fFile = "metadata"; //$NON-NLS-1$ // we're in CTF, the only thing using antlr is metadata
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
                        this.fExpectingName = name;
                        this.fExpectedValue = value;
                    }
                    if (value == m.c) {
                        this.fActualName = name;
                        this.fActualValue = value;
                    }
                }
            } catch (NullPointerException e1) {
                // Pokemon, gotta catch em all!
                // actually useful since f may not have a
                // value
            } catch (IllegalArgumentException e1) {
                // Catch these exceptions (reflexion)
            } catch (IllegalAccessException e1) {
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
        String expected = "" + this.fExpectedValue; //$NON-NLS-1$
        String actual = "" + this.fActualValue; //$NON-NLS-1$
        String newMessage = message.replaceAll(expected, this.fExpectingName);
        newMessage = newMessage.replaceAll(actual, this.fActualName);
        return newMessage + " at " + fFile + ":" + fErrorLine; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
