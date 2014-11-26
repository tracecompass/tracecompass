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

package org.eclipse.tracecompass.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Filter node for the regex match
 *
 * @version 1.0
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public abstract class TmfFilterMatchesNode extends TmfFilterTreeNode {

    public static final String NOT_ATTR = "not"; //$NON-NLS-1$
    public static final String REGEX_ATTR = "regex"; //$NON-NLS-1$

    private boolean fNot = false;

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

    protected Pattern getPattern() {
        return fPattern;
    }

    @Override
    public List<String> getValidChildren() {
        return new ArrayList<>(0);
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterMatchesNode clone = (TmfFilterMatchesNode) super.clone();
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
