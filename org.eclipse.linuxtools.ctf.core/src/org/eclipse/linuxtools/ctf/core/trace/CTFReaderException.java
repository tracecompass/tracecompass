/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alexandre Montplaisir - Initial API and implementation
 * Contributors: Matthew Khouzam - Addition to have more descriptive errors
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

import java.lang.reflect.Field;

import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.eclipse.linuxtools.ctf.parser.CTFLexer;

/**
 * General exception that is thrown when there is a problem somewhere with the
 * CTF trace reader.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
public class CTFReaderException extends Exception {

    private static final long serialVersionUID = 2065258365219777672L;
    private int fErrorLine = -1;
    private String fFile = ""; //$NON-NLS-1$
    private String fExpectingName = ""; //$NON-NLS-1$
    private int fExpectedValue = -1;
    private String fActualName = ""; //$NON-NLS-1$
    private int fActualValue = -1;

    /**
     * Default constructor with no message.
     */
    public CTFReaderException() {
        super();
    }

    /**
     * Constructor with an attached message.
     *
     * @param message
     *            The message attached to this exception
     */
    public CTFReaderException(String message) {
        super(message);
    }

    /**
     * Re-throw an exception into this type.
     *
     * @param e
     *            The previous Exception we caught
     */
    public CTFReaderException(Exception e) {
        super(e);
    }

    /**
     * Re-throw the exception but read its data
     *
     * @param e
     *            the previous recognition exception (Antlr specific)
     * @since 2.0
     */
    public CTFReaderException(RecognitionException e) {
        super(e);
        this.fErrorLine = e.line;
        this.fFile = "metadata"; //$NON-NLS-1$ // we're in CTF, the only thing using antlr is metadata
    }

    /**
     * Re-throw the exception but read its data
     *
     * @param e
     *            the previous recognition exception (Antlr specific)
     * @since 2.0
     */
    public CTFReaderException(MismatchedTokenException e){
        super(e);
        this.fErrorLine = e.line;
        this.fFile = "metadata"; //$NON-NLS-1$ // we're in CTF, the only thing using antlr is metadata
        parseMismatchedException(e);
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
