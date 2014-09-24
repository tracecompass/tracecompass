/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * Filter node for the regex match
 *
 * @version 1.0
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TmfFilterMatchesNode extends TmfFilterTreeNode {

    public static final String NODE_NAME = "MATCHES"; //$NON-NLS-1$
    public static final String NOT_ATTR = "not"; //$NON-NLS-1$
    public static final String FIELD_ATTR = "field"; //$NON-NLS-1$
    public static final String REGEX_ATTR = "regex"; //$NON-NLS-1$

    private boolean fNot = false;
    private String fField;
    private String fRegex;
    private transient Pattern fPattern;

    /**
     * @param parent
     *            the parent node
     */
    public TmfFilterMatchesNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    /**
     * @return the NOT state
     */
    public boolean isNot() {
        return fNot;
    }

    /**
     * @param not
     *            the NOT state
     */
    public void setNot(boolean not) {
        this.fNot = not;
    }

    /**
     * @return the field name
     */
    public String getField() {
        return fField;
    }

    /**
     * @param field
     *            the field name
     */
    public void setField(String field) {
        this.fField = field;
    }

    /**
     * @return the regular expression
     */
    public String getRegex() {
        return fRegex;
    }

    /**
     * @param regex
     *            the regular expression
     */
    public void setRegex(String regex) {
        this.fRegex = regex;
        if (regex != null) {
            try {
                this.fPattern = Pattern.compile(regex, Pattern.DOTALL);
            } catch (PatternSyntaxException e) {
                this.fPattern = null;
            }
        }
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        if (fPattern == null) {
            return false ^ fNot;
        }

        Object value = getFieldValue(event, fField);
        if (value == null) {
            return false ^ fNot;
        }
        String valueString = value.toString();

        return fPattern.matcher(valueString).matches() ^ fNot;
    }

    @Override
    public List<String> getValidChildren() {
        return new ArrayList<>(0);
    }

    @Override
    public String toString() {
        return fField + (fNot ? " not" : "") + " matches \"" + fRegex + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterMatchesNode clone = (TmfFilterMatchesNode) super.clone();
        clone.fField = fField;
        clone.setRegex(fRegex);
        return clone;
    }

    /**
     * @param pattern
     *            the rough regex pattern
     * @return the compliant regex
     */
    public static String regexFix(String pattern) {
        String ret = pattern;
        // if the pattern does not contain one of the expressions .* !^
        // (at the beginning) $ (at the end), then a .* is added at the
        // beginning and at the end of the pattern
        if (!(ret.indexOf(".*") >= 0 || ret.charAt(0) == '^' || ret.charAt(ret.length() - 1) == '$')) { //$NON-NLS-1$
            ret = ".*" + ret + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return ret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fField == null) ? 0 : fField.hashCode());
        result = prime * result + (fNot ? 1231 : 1237);
        result = prime * result + ((fRegex == null) ? 0 : fRegex.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfFilterMatchesNode other = (TmfFilterMatchesNode) obj;
        if (fField == null) {
            if (other.fField != null) {
                return false;
            }
        } else if (!fField.equals(other.fField)) {
            return false;
        }
        if (fNot != other.fNot) {
            return false;
        }
        if (fRegex == null) {
            if (other.fRegex != null) {
                return false;
            }
        } else if (!fRegex.equals(other.fRegex)) {
            return false;
        }
        return true;
    }
}
