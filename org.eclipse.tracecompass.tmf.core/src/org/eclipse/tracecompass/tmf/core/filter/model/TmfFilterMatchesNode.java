/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
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

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Filter node for the regex match
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterMatchesNode extends TmfFilterAspectNode {

    /** matches node name */
    public static final String NODE_NAME = "MATCHES"; //$NON-NLS-1$
    /** not attribute name */
    public static final String NOT_ATTR = "not"; //$NON-NLS-1$
    /** regex attribute name */
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

    /**
     * @return the regex pattern
     */
    protected Pattern getPattern() {
        return fPattern;
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        Pattern pattern = getPattern();
        boolean isNot = isNot();

        if (pattern == null || event == null || fEventAspect == null) {
            return false ^ isNot;
        }
        Object value = fEventAspect.resolve(event);
        if (value == null) {
            return false ^ isNot;
        }
        return pattern.matcher(value.toString()).matches() ^ isNot;
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
    public String toString() {
        return getAspectLabel() + (isNot() ? " not" : "") + " matches \"" + getRegex() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
