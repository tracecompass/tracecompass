/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Button;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;

/**
 * Basic representation of a check box option for dialog.
 *
 * @author Jonathan Rajotte-Julien
 */
class LamiAxisCheckBoxOption {

    private final String fName;
    private final boolean fDefaultValue;
    private @Nullable Button fButton;
    private boolean fValue;
    private final Predicate<LamiTableEntryAspect> fAppliesToAspect;

    /**
     * Constructor
     *
     * @param name
     *          The name of the check box. The actual string shown to user.
     * @param defaultValue
     *          The default value of the check box.
     * @param validationPredicate
     *          The predicate to check if an option can be applied to an aspect
     */
    public LamiAxisCheckBoxOption(String name, boolean defaultValue, Predicate<LamiTableEntryAspect> validationPredicate) {
        fName = name;
        this.fDefaultValue = defaultValue;
        this.fValue = defaultValue;
        fButton = null;
        fAppliesToAspect = validationPredicate;
    }

    public String getName() {
        return fName;
    }

    public boolean getDefaultValue() {
        return fDefaultValue;
    }

    public void setButton(Button button) {
        fButton = button;
    }

    public boolean getValue() {
        return fValue;
    }

    public void updateValue() {
        if (fButton != null) {
            fValue = fButton.getSelection();
        }
    }

    public void setButtonEnabled(boolean enabled) {
        @Nullable Button button = fButton;
        if (button != null) {
            /* Only change state when necessary */
            if (button.getEnabled() != enabled) {
                button.setEnabled(enabled);
                button.setSelection(fDefaultValue);
            }
        }
    }

    public boolean getButtonEnabled() {
        if (fButton != null) {
            return fButton.getEnabled();
        }
        return false;
    }

    public Predicate<LamiTableEntryAspect> getPredicate() {
        return fAppliesToAspect;
    }
}