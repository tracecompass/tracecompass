/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Criteria.java,v 1.2 2006/09/20 20:56:27 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDGraphNodeSupporter;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;

/**
 * This class describe the Find or Filter criteria selected by the user in the Find or Filter dialog box
 * 
 * @author sveyrier
 */
public class Criteria {

    protected boolean lifeLineSelected = false;
    protected boolean syncMessageSelected = false;
    protected boolean syncMessageReturnSelected = false;
    protected boolean asyncMessageSelected = false;
    protected boolean asyncMessageReturnSelected = false;
    private   boolean caseSenstiveSelected = false;
    protected boolean stopSelected = false;
    private   String  expression = null;
    private   Pattern pattern = null;

    /**
     * Default constructor
     */
    public Criteria () {
    }

    /**
     * Copy constructor
     * @param other Criteria to create new criteria
     */
    public Criteria (Criteria other) {
         this.lifeLineSelected = other.lifeLineSelected;
         this.syncMessageSelected = other.syncMessageSelected;
         this.syncMessageReturnSelected = other.syncMessageReturnSelected;
         this.asyncMessageSelected = other.asyncMessageSelected;
         this.asyncMessageReturnSelected = other.asyncMessageReturnSelected;
         this.caseSenstiveSelected = other.caseSenstiveSelected;
         this.stopSelected = other.stopSelected;
         setExpression(other.expression);
    }
    
    /**
     * Returns true if the AsyncMessageReturn is selected, false otherwise
     * 
     * @return true if the AsyncMessageReturn is selected, false otherwise
     */
    public boolean isAsyncMessageReturnSelected() {
        return asyncMessageReturnSelected;
    }

    /**
     * Returns true if the AsyncMessage is selected, false otherwise
     * 
     * @return true if the AsyncMessage is selected, false otherwise
     */
    public boolean isAsyncMessageSelected() {
        return asyncMessageSelected;
    }

    /**
     * Returns the text enter by the user
     * 
     * @return the text
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @return pattern
     */
    public Pattern getPattern() {
        return pattern;
    }
    
    /**
     * @param pattern
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Returns true if the LifeLine is selected, false otherwise
     * 
     * @return true if the LifeLine is selected, false otherwise
     */
    public boolean isLifeLineSelected() {
        return lifeLineSelected;
    }

    /**
     * Returns true if the Stop is selected, false otherwise
     * 
     * @return true if the Stop is selected, false otherwise
     */
    public boolean isStopSelected() {
        return stopSelected;
    }

    /**
     * Returns true if the SyncMessageReturn is selected, false otherwise
     * 
     * @return true if the SyncMessageReturn is selected, false otherwise
     */
    public boolean isSyncMessageReturnSelected() {
        return syncMessageReturnSelected;
    }

    /**
     * Returns true if the SyncMessage is selected, false otherwise
     * 
     * @return true if the SyncMessage is selected, false otherwise
     */
    public boolean isSyncMessageSelected() {
        return syncMessageSelected;
    }

    /**
     * Set AsyncMessageReturn selection state
     * 
     * @param b true if selected, false otherwise
     */
    public void setAsyncMessageReturnSelected(boolean b) {
        asyncMessageReturnSelected = b;
    }

    /**
     * Set AsyncMessage selection state
     * 
     * @param b true if selected, false otherwise
     */
    public void setAsyncMessageSelected(boolean b) {
        asyncMessageSelected = b;
    }

    /**
     * Set the text enter by the user
     * 
     * @param string the text
     */
    public void setExpression(String string) {
        expression = string;
        if (expression != null) {

            try {
                if (caseSenstiveSelected) {
                    pattern = Pattern.compile(string);
                }
                else {
                    pattern = Pattern.compile(string, Pattern.CASE_INSENSITIVE);   
                }

            } catch (PatternSyntaxException e) {
                pattern = null;
            }
        }
        else {
            pattern = null;
        }
    }

    /**
     * Set Stop selection state
     * 
     * @param b true if selected, false otherwise
     */
    public void setLifeLineSelected(boolean b) {
        lifeLineSelected = b;
    }

    /**
     * Set Stop selection state
     * 
     * @param b true if selected, false otherwise
     */
    public void setStopSelected(boolean b) {
        stopSelected = b;
    }

    /**
     * Set SyncMessageReturn selection state
     * 
     * @param b true if selected, false otherwise
     */
    public void setSyncMessageReturnSelected(boolean b) {
        syncMessageReturnSelected = b;
    }

    /**
     * Set SyncMessage selection state
     * 
     * @param b true if selected, false otherwise
     */
    public void setSyncMessageSelected(boolean b) {
        syncMessageSelected = b;
    }

    /**
     * Returns true if the case sensitive is selected, false otherwise
     * 
     * @return true if the case sensitive is selected, false otherwise
     */
    public boolean isCaseSenstiveSelected() {
        return caseSenstiveSelected;
    }

    /**
     * Set case sensitive selection state
     * 
     * @param b true if selected, false otherwise
     */
    public void setCaseSenstiveSelected(boolean b) {
        caseSenstiveSelected = b;
        // Make sure that pattern is set
        setExpression(expression);
    }

    /**
     * @param to
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
     * @param settings
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
     * @param settings
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

        String ret = "", prefix = "["; //$NON-NLS-1$ //$NON-NLS-2$
        for (Iterator<String> i = list.iterator(); i.hasNext();) {
            String s = (String) i.next();
            ret += prefix + s;
            prefix = " " + SDMessages._34 + " "; //$NON-NLS-1$ //$NON-NLS-2$
        }
        ret += "]"; //$NON-NLS-1$
        return ret;
    }

    /**
     * @param stringToMatch
     * @return true if string matches expression
     */
    public boolean matches(String stringToMatch) {
        if (pattern == null) {
            return false;
        }
        return pattern.matcher(stringToMatch).matches();
    }
}
