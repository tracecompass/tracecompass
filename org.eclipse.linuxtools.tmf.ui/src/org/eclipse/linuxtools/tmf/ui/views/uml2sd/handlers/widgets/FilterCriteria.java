/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: FilterCriteria.java,v 1.3 2008/01/24 02:29:09 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.widgets;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.DialogSettings;

/**
 * A filter criteria is a criteria that can be activated or not, positive or not.
 */
public class FilterCriteria {

    protected static final String ACTIVE = "active"; //$NON-NLS-1$
    protected static final String POSITIVE = "positive"; //$NON-NLS-1$
    protected static final String LOADERCLASSNAME = "loaderClassName"; //$NON-NLS-1$

    protected Criteria criteria;
    protected boolean active;
    protected boolean positive;
    protected String loaderClassName;

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
     * @param criteria_
     * @param active_
     * @param positive_
     */
    public FilterCriteria(Criteria criteria_, boolean active_, boolean positive_) {
        setCriteria(criteria_);
        setActive(active_);
        setPositive(positive_);
    }

    /**
     * @param criteria_
     * @param active_
     * @param positive_
     * @param loaderClassName_
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

    protected FilterCriteria() {
    }

    /**
     * @param criteria_
     */
    public void setCriteria(Criteria criteria_) {
        criteria = criteria_;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    /**
     * @param active_
     */
    public void setActive(boolean active_) {
        active = active_;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * @param positive_ The positive to set.
     */
    public void setPositive(boolean positive_) {
        positive = positive_;
    }

    /**
     * @return Returns the positive.
     */
    public boolean isPositive() {
        return positive;
    }

    /**
     */
    public void setLoaderClassName(String loaderClassName_) {
        loaderClassName = loaderClassName_;
    }

    /**
     * @return Returns the class loader name.
     */
    public String getLoaderClassName() {
        return loaderClassName;
    }

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

    public void save(DialogSettings settings) {
        settings.put(ACTIVE, isActive());
        settings.put(POSITIVE, isPositive());
        if (getLoaderClassName() != null) {
            settings.put(LOADERCLASSNAME, getLoaderClassName());
        } else {
            settings.put(LOADERCLASSNAME, ""); //$NON-NLS-1$
        }
        if (criteria != null)
            criteria.save(settings);
    }

    /**
     * @param settings
     */
    public void load(DialogSettings settings) {
        setActive(settings.getBoolean(ACTIVE));
        setPositive(settings.getBoolean(POSITIVE));
        String loaderClassName_ = settings.get(LOADERCLASSNAME);
        setLoaderClassName(loaderClassName_ != null && loaderClassName_.length() > 0 ? loaderClassName_ : null);
        if (criteria != null)
            criteria.load(settings);
    }
}