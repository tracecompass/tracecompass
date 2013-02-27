/**********************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDGraphNodeSupporter;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;

/**
 * This class describes the find or filter criteria selected by the user in the find or filter dialog box
 *
 * @version 1.0
 * @author sveyrier
 * @author Bernd Hufmann
 */
public class Criteria {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Flag whether lifeline is selected or not.
     */
    protected boolean fLifeLineSelected = false;
    /**
     * Flag whether synchronous message is selected or not.
     */
    protected boolean fSyncMessageSelected = false;
    /**
     * Flag whether synchronous message return is selected or not.
     */
    protected boolean fSyncMessageReturnSelected = false;
    /**
     * Flag whether asynchronous message is selected or not.
     */
    protected boolean fAsyncMessageSelected = false;
    /**
     * Flag whether asynchronous message return is selected or not.
     */
    protected boolean fAsyncMessageReturnSelected = false;
    /**
     * Flag whether case sensitive find is required or not.
     */
    protected boolean fCaseSenstiveSelected = false;
    /**
     * Flag whether stop graph node is selected or not.
     */
    protected boolean fStopSelected = false;
    /**
     * The find expression.
     */
    protected String  fExpression = null;
    /**
     * The find pattern as regular expression.
     */
    protected Pattern pattern = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public Criteria () {
    }

    /**
     * Copy constructor
     *
     * @param other Criteria to create new criteria
     */
    public Criteria (Criteria other) {
         this.fLifeLineSelected = other.fLifeLineSelected;
         this.fSyncMessageSelected = other.fSyncMessageSelected;
         this.fSyncMessageReturnSelected = other.fSyncMessageReturnSelected;
         this.fAsyncMessageSelected = other.fAsyncMessageSelected;
         this.fAsyncMessageReturnSelected = other.fAsyncMessageReturnSelected;
         this.fCaseSenstiveSelected = other.fCaseSenstiveSelected;
         this.fStopSelected = other.fStopSelected;
         fExpression = other.fExpression;
         updatePattern();
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Returns true if the AsyncMessageReturn is selected, false otherwise.
     *
     * @return true if the AsyncMessageReturn is selected, false otherwise
     */
    public boolean isAsyncMessageReturnSelected() {
        return fAsyncMessageReturnSelected;
    }

    /**
     * Returns true if the AsyncMessage is selected, false otherwise.
     *
     * @return true if the AsyncMessage is selected, false otherwise
     */
    public boolean isAsyncMessageSelected() {
        return fAsyncMessageSelected;
    }

    /**
     * Returns the text enter by the user.
     *
     * @return the expression text
     */
    public String getExpression() {
        return fExpression;
    }

    /**
     * Returns the regular expression pattern.
     *
     * @return the regular expression pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Sets the regular expression pattern.
     *
     * @param pattern
     *            The pattern to set
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Returns true if the LifeLine is selected, false otherwise.
     *
     * @return true if the LifeLine is selected, false otherwise
     */
    public boolean isLifeLineSelected() {
        return fLifeLineSelected;
    }

    /**
     * Returns true if the Stop is selected, false otherwise.
     *
     * @return true if the Stop is selected, false otherwise
     */
    public boolean isStopSelected() {
        return fStopSelected;
    }

    /**
     * Returns true if the SyncMessageReturn is selected, false otherwise.
     *
     * @return true if the SyncMessageReturn is selected, false otherwise
     */
    public boolean isSyncMessageReturnSelected() {
        return fSyncMessageReturnSelected;
    }

    /**
     * Returns true if the SyncMessage is selected, false otherwise.
     *
     * @return true if the SyncMessage is selected, false otherwise
     */
    public boolean isSyncMessageSelected() {
        return fSyncMessageSelected;
    }

    /**
     * Sets the AsyncMessageReturn selection state.
     *
     * @param b true if selected, false otherwise
     */
    public void setAsyncMessageReturnSelected(boolean b) {
        fAsyncMessageReturnSelected = b;
    }

    /**
     * Sets the AsyncMessage selection state.
     *
     * @param b true if selected, false otherwise
     */
    public void setAsyncMessageSelected(boolean b) {
        fAsyncMessageSelected = b;
    }

    /**
     * Sets the text entered by the user and compiles the regular expression.
     *
     * @param string the text
     */
    public void setExpression(String string) {
        fExpression = string;
        updatePattern();
    }

    /**
     * Sets the Stop selection state.
     *
     * @param b true if selected, false otherwise
     */
    public void setLifeLineSelected(boolean b) {
        fLifeLineSelected = b;
    }

    /**
     * Set Stop selection state.
     *
     * @param b true if selected, false otherwise
     */
    public void setStopSelected(boolean b) {
        fStopSelected = b;
    }

    /**
     * Sets the SyncMessageReturn selection state.
     *
     * @param b true if selected, false otherwise
     */
    public void setSyncMessageReturnSelected(boolean b) {
        fSyncMessageReturnSelected = b;
    }

    /**
     * Sets the SyncMessage selection state.
     *
     * @param b true if selected, false otherwise
     */
    public void setSyncMessageSelected(boolean b) {
        fSyncMessageSelected = b;
    }

    /**
     * Returns true if the case sensitive is selected, false otherwise.
     *
     * @return true if the case sensitive is selected, false otherwise
     */
    public boolean isCaseSenstiveSelected() {
        return fCaseSenstiveSelected;
    }

    /**
     * Set case sensitive selection state.
     *
     * @param b true if selected, false otherwise
     */
    public void setCaseSenstiveSelected(boolean b) {
        fCaseSenstiveSelected = b;
        // Make sure that pattern is set
        setExpression(fExpression);
    }

    /**
     * Compares this criteria with a given criteria.
     *
     * @param to The criteria to compare
     * @return usual comparison result (< 0, 0, > 0)
     */
    public boolean compareTo(Criteria to) {
        boolean retVal = true;
        if (getExpression() != null) {
            retVal = getExpression().equals(to.getExpression());
        } else if (to.getExpression() != null) {
            retVal = to.getExpression().equals(getExpression());
        }
        return retVal && isCaseSenstiveSelected() == to.isCaseSenstiveSelected() && isAsyncMessageReturnSelected() == to.isAsyncMessageReturnSelected() && isAsyncMessageSelected() == to.isAsyncMessageSelected()
                && isLifeLineSelected() == to.isLifeLineSelected() && isStopSelected() == to.isStopSelected() && isSyncMessageReturnSelected() == to.isSyncMessageReturnSelected() && isSyncMessageSelected() == to.isSyncMessageSelected();
    }

    /**
     * Saves current criteria attributes in the dialog settings.
     *
     * @param settings The dialog settings
     */
    public void save(DialogSettings settings) {
        settings.put("expression", getExpression()); //$NON-NLS-1$
        settings.put("isCaseSenstiveSelected", isCaseSenstiveSelected()); //$NON-NLS-1$
        settings.put("isAsyncMessageReturnSelected", isAsyncMessageReturnSelected()); //$NON-NLS-1$
        settings.put("isAsyncMessageSelected", isAsyncMessageSelected()); //$NON-NLS-1$
        settings.put("isLifeLineSelected", isLifeLineSelected()); //$NON-NLS-1$
        settings.put("isStopSelected", isStopSelected()); //$NON-NLS-1$
        settings.put("isSyncMessageReturnSelected", isSyncMessageReturnSelected()); //$NON-NLS-1$
        settings.put("isSyncMessageSelected", isSyncMessageSelected()); //$NON-NLS-1$
    }

    /**
     * Loads the criteria with values of the dialog settings.
     *
     * @param settings The dialog settings
     */
    public void load(DialogSettings settings) {
        setExpression(settings.get("expression")); //$NON-NLS-1$
        setCaseSenstiveSelected(settings.getBoolean("isCaseSenstiveSelected")); //$NON-NLS-1$
        setAsyncMessageReturnSelected(settings.getBoolean("isAsyncMessageReturnSelected")); //$NON-NLS-1$
        setAsyncMessageSelected(settings.getBoolean("isAsyncMessageSelected")); //$NON-NLS-1$
        setLifeLineSelected(settings.getBoolean("isLifeLineSelected")); //$NON-NLS-1$
        setStopSelected(settings.getBoolean("isStopSelected")); //$NON-NLS-1$
        setSyncMessageReturnSelected(settings.getBoolean("isSyncMessageReturnSelected")); //$NON-NLS-1$
        setSyncMessageSelected(settings.getBoolean("isSyncMessageSelected")); //$NON-NLS-1$
    }

    /**
     * Gets the summary of supported graph nodes.
     *
     * @param provider A filter provider
     * @param loaderClassName A class loader
     * @return graph node summary
     */
    public String getGraphNodeSummary(ISDFilterProvider provider, String loaderClassName) {
        ArrayList<String> list = new ArrayList<String>();

        if (provider != null) {
            if (isLifeLineSelected()) {
                list.add(provider.getNodeName(ISDGraphNodeSupporter.LIFELINE, loaderClassName));
            }
            if (isSyncMessageSelected()) {
                list.add(provider.getNodeName(ISDGraphNodeSupporter.SYNCMESSAGE, loaderClassName));
            }
            if (isSyncMessageReturnSelected()) {
                list.add(provider.getNodeName(ISDGraphNodeSupporter.SYNCMESSAGERETURN, loaderClassName));
            }
            if (isAsyncMessageSelected()) {
                list.add(provider.getNodeName(ISDGraphNodeSupporter.ASYNCMESSAGE, loaderClassName));
            }
            if (isAsyncMessageReturnSelected()) {
                list.add(provider.getNodeName(ISDGraphNodeSupporter.ASYNCMESSAGERETURN, loaderClassName));
            }
            if (isStopSelected()) {
                list.add(provider.getNodeName(ISDGraphNodeSupporter.STOP, loaderClassName));
            }
        } else {
            if (isLifeLineSelected()) {
                list.add(SDMessages._28);
            }
            if (isSyncMessageSelected()) {
                list.add(SDMessages._30);
            }
            if (isSyncMessageReturnSelected()) {
                list.add(SDMessages._31);
            }
            if (isAsyncMessageSelected()) {
                list.add(SDMessages._32);
            }
            if (isAsyncMessageReturnSelected()) {
                list.add(SDMessages._33);
            }
            if (isStopSelected()) {
                list.add(SDMessages._29);
            }
        }
        StringBuffer ret = new StringBuffer();
        String prefix = "["; //$NON-NLS-1$
        for (Iterator<String> i = list.iterator(); i.hasNext();) {
            String s = i.next();
            ret.append(prefix);
            ret.append(s);
            prefix = " " + SDMessages._34 + " "; //$NON-NLS-1$ //$NON-NLS-2$
        }
        ret.append("]"); //$NON-NLS-1$
        return ret.toString();
    }

    /**
     * Matches given string using compiled pattern based on user expression.
     *
     * @param stringToMatch  A string to match
     * @return true if string matches expression
     */
    public boolean matches(String stringToMatch) {
        if (pattern == null) {
            return false;
        }
        return pattern.matcher(stringToMatch).matches();
    }

    /**
     * Updates the regular expression pattern based on the expression.
     */
    private void updatePattern() {
        if (fExpression != null) {
            try {
                if (fCaseSenstiveSelected) {
                    pattern = Pattern.compile(fExpression);
                }
                else {
                    pattern = Pattern.compile(fExpression, Pattern.CASE_INSENSITIVE);
                }
            } catch (PatternSyntaxException e) {
                pattern = null;
            }
        }
        else {
            pattern = null;
        }
    }

}
