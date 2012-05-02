/**********************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 * 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
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
    protected Criteria criteria;
    /**
     * Flag whether this criteria is active or not
     */
    protected boolean active;
    /**
     * Flag whether this criteria is for positive filter or not
     */
    protected boolean positive;
    /**
     * The loader class name.
     */
    protected String loaderClassName;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Standard constructor
     * 
     * @param criteria_ A criteria reference
     * @param active_ <code>true</code> if filter criteria is active else <code>false</code> 
     * @param positive_  <code>true</code> for positive filter else <code>false</code>
     */
    public FilterCriteria(Criteria criteria_, boolean active_, boolean positive_) {
        setCriteria(criteria_);
        setActive(active_);
        setPositive(positive_);
    }

    /**
     * Constructor
     * 
     * @param criteria_ A criteria reference
     * @param active_ <code>true</code> if filter criteria is active else <code>false</code> 
     * @param positive_  <code>true</code> for positive filter else <code>false</code>
     * @param loaderClassName_ A loader class name
     */
    public FilterCriteria(Criteria criteria_, boolean active_, boolean positive_, String loaderClassName_) {
        setCriteria(criteria_);
        setActive(active_);
        setPositive(positive_);
        setLoaderClassName(loaderClassName_);
    }

    /**
     * Copy Constructor
     * @param other FilterCriteria
     */
    public FilterCriteria (FilterCriteria other) {
        criteria = new Criteria(other.criteria);
        this.active = other.active;
        this.positive = other.positive;
        this.loaderClassName = other.loaderClassName;
    }

    /**
     * Default constructor
     */
    protected FilterCriteria() {
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(":"); //$NON-NLS-1$
        if (criteria != null) {
            sb.append(" expression=");sb.append(criteria.getExpression()); //$NON-NLS-1$
            sb.append(" active=");sb.append(active); //$NON-NLS-1$
            sb.append(" positive=");sb.append(positive); //$NON-NLS-1$
        } else {
            sb.append("empty criteria"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    /**
     * Sets a criteria reference.
     * @param criteria_ A criteria reference
     */
    public void setCriteria(Criteria criteria_) {
        criteria = criteria_;
    }

    /**
     * Returns the criteria reference.
     * 
     * @return the criteria reference
     */
    public Criteria getCriteria() {
        return criteria;
    }

    /**
     * Sets the active flag.
     * 
     * @param active_ A active value.
     */
    public void setActive(boolean active_) {
        active = active_;
    }

    /**
     * Returns whether filter criteria is active or not.
     * 
     * @return whether filter criteria is active or not.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets filter is for positive filtering or not.
     * 
     * @param positive_ The value to set.
     */
    public void setPositive(boolean positive_) {
        positive = positive_;
    }

    /**
     * Returns whether the filter si for positive filtering or not.
     * 
     * @return Returns the positive.
     */
    public boolean isPositive() {
        return positive;
    }

    /**
     * Sets the loader class name for this filter.
     * 
     * @param loaderClassName_ The loader class name to set
     */
    public void setLoaderClassName(String loaderClassName_) {
        loaderClassName = loaderClassName_;
    }

    /**
     * Returns the class loader name.
     * 
     * @return the class loader name.
     */
    public String getLoaderClassName() {
        return loaderClassName;
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
                    FilterCriteria fc = (FilterCriteria) i.next();
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
        if (criteria != null) {
            criteria.save(settings);
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
        String loaderClassName_ = settings.get(LOADERCLASSNAME);
        setLoaderClassName(loaderClassName_ != null && loaderClassName_.length() > 0 ? loaderClassName_ : null);
        if (criteria != null) {
            criteria.load(settings);
        }
    }
}