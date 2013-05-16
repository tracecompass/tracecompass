/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.DialogSettings;

/**
 * A filter criteria is a criteria that can be activated or not, positive or not.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class FilterCriteria {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The filter state value for 'active'.
     */
    protected static final String ACTIVE = "active"; //$NON-NLS-1$
    /**
     * The property value for positive filter.
     */
    protected static final String POSITIVE = "positive"; //$NON-NLS-1$
    /**
     * The filter loader class name property.
     */
    protected static final String LOADERCLASSNAME = "loaderClassName"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The criteria reference.
     */
    private Criteria fCriteria;
    /**
     * Flag whether this criteria is active or not
     */
    private boolean fIsActive;
    /**
     * Flag whether this criteria is for positive filter or not
     */
    private boolean fIsPositive;
    /**
     * The loader class name.
     */
    private String fLoaderClassName;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Standard constructor
     *
     * @param criteria A criteria reference
     * @param isActive <code>true</code> if filter criteria is active else <code>false</code>
     * @param isPositive  <code>true</code> for positive filter else <code>false</code>
     */
    public FilterCriteria(Criteria criteria, boolean isActive, boolean isPositive) {
        this(criteria, isActive, isPositive, null);
    }

    /**
     * Constructor
     *
     * @param criteria A criteria reference
     * @param isActive <code>true</code> if filter criteria is active else <code>false</code>
     * @param isPositive  <code>true</code> for positive filter else <code>false</code>
     * @param loaderClassName A loader class name
     */
    public FilterCriteria(Criteria criteria, boolean isActive, boolean isPositive, String loaderClassName) {
        fCriteria = criteria;
        fIsActive = isActive;
        fIsPositive = isPositive;
        fLoaderClassName = loaderClassName;
    }

    /**
     * Copy Constructor
     * @param other FilterCriteria
     */
    public FilterCriteria (FilterCriteria other) {
        fCriteria = new Criteria(other.fCriteria);
        fIsActive = other.fIsActive;
        fIsPositive = other.fIsPositive;
        fLoaderClassName = other.fLoaderClassName;
    }

    /**
     * Default constructor
     */
    protected FilterCriteria() {
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(':');
        if (fCriteria != null) {
            sb.append(" expression=");sb.append(fCriteria.getExpression()); //$NON-NLS-1$
            sb.append(" active=");sb.append(fIsActive); //$NON-NLS-1$
            sb.append(" positive=");sb.append(fIsPositive); //$NON-NLS-1$
        } else {
            sb.append("empty criteria"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    /**
     * Sets a criteria reference.
     * @param criteria A criteria reference
     */
    public void setCriteria(Criteria criteria) {
        fCriteria = criteria;
    }

    /**
     * Returns the criteria reference.
     *
     * @return the criteria reference
     */
    public Criteria getCriteria() {
        return fCriteria;
    }

    /**
     * Sets the active flag.
     *
     * @param isActive A active value.
     */
    public void setActive(boolean isActive) {
        fIsActive = isActive;
    }

    /**
     * Returns whether filter criteria is active or not.
     *
     * @return whether filter criteria is active or not.
     */
    public boolean isActive() {
        return fIsActive;
    }

    /**
     * Sets filter is for positive filtering or not.
     *
     * @param isPositive The value to set.
     */
    public void setPositive(boolean isPositive) {
        fIsPositive = isPositive;
    }

    /**
     * Returns whether the filter si for positive filtering or not.
     *
     * @return Returns the positive.
     */
    public boolean isPositive() {
        return fIsPositive;
    }

    /**
     * Sets the loader class name for this filter.
     *
     * @param loaderClassName The loader class name to set
     */
    public void setLoaderClassName(String loaderClassName) {
        fLoaderClassName = loaderClassName;
    }

    /**
     * Returns the class loader name.
     *
     * @return the class loader name.
     */
    public String getLoaderClassName() {
        return fLoaderClassName;
    }

    /**
     * Finds a filter criteria within a  list of criteria.
     *
     * @param what The filter to find
     * @param list A list of filter criteria
     * @return The found filter criteria or null
     */
    public static FilterCriteria find(FilterCriteria what, List<FilterCriteria> list) {
        if (what != null && list != null) {
            try {
                for (Iterator<FilterCriteria> i = list.iterator(); i.hasNext();) {
                    FilterCriteria fc = i.next();
                    if (what.compareTo(fc)) {
                        return fc;
                    }
                }
            } catch (Exception e) {
                // Silence
            }
        }
        return null;
    }

    /**
     * Compares this filter criteria with a given criteria.
     *
     * @param to The filter criteria to compare.
     * @return usual comparison result (< 0, 0, > 0)
     */
    public boolean compareTo(FilterCriteria to) {
        if (isPositive() == to.isPositive() && getCriteria().compareTo(to.getCriteria())) {
            if (getLoaderClassName() == null && to.getLoaderClassName() == null) {
                return true;
            }
            if ((getLoaderClassName() != null && to.getLoaderClassName() != null) && getLoaderClassName().equals(to.getLoaderClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves current criteria attributes in the dialog settings.
     *
     * @param settings The dialog settings
     */
    public void save(DialogSettings settings) {
        settings.put(ACTIVE, isActive());
        settings.put(POSITIVE, isPositive());
        if (getLoaderClassName() != null) {
            settings.put(LOADERCLASSNAME, getLoaderClassName());
        } else {
            settings.put(LOADERCLASSNAME, ""); //$NON-NLS-1$
        }
        if (fCriteria != null) {
            fCriteria.save(settings);
        }
    }

    /**
     * Loads the criteria with values of the dialog settings.
     *
     * @param settings The dialog settings
     */
    public void load(DialogSettings settings) {
        setActive(settings.getBoolean(ACTIVE));
        setPositive(settings.getBoolean(POSITIVE));
        String loaderClassName = settings.get(LOADERCLASSNAME);
        setLoaderClassName(loaderClassName != null && loaderClassName.length() > 0 ? loaderClassName : null);
        if (fCriteria != null) {
            fCriteria.load(settings);
        }
    }
}