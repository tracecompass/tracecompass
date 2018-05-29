/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.filter.parser.FilterParserParser;

/**
 * Compilation unit for a simple filter expression
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterSimpleExpressionCu {

    private String fField;
    private BiPredicate<String, String> fOperator;
    private String fValue;

    /**
     * Constructor
     *
     * @param field
     *            The field to look for
     * @param op
     *            The operator to use for the test
     * @param value
     *            The value to to test
     */
    public FilterSimpleExpressionCu(String field, BiPredicate<String, String> op, String value) {
        fField = field;
        fOperator = op;
        fValue = value;
    }

    /**
     * Get the filter field parameter
     *
     * @return the field parameter
     */
    protected String getField() {
        return fField;
    }

    /**
     * Get the filter operator
     *
     * @return The filter operator
     */
    protected BiPredicate<String, String> getOperator() {
        return fOperator;
    }

    /**
     * Get the filter value parameter
     *
     * @return The value parameter
     */
    protected String getValue() {
        return fValue;
    }

    /**
     * Compile a simple filter expression compilation unit from a tree
     *
     * @param tree
     *            The input tree
     * @return The simple filter expression compilation unit
     */
    public static FilterSimpleExpressionCu compile(CommonTree tree) {
        if (tree.getToken() == null) {
            return null;
        }

        int childCount = tree.getChildCount();
        switch (tree.getToken().getType()) {
        case FilterParserParser.CONSTANT:
        case FilterParserParser.PAR_CONSTANT:
            StringBuilder paragraph = new StringBuilder();
            extractParagraph(tree, paragraph, 0, childCount);
            return new FilterSimpleExpressionCu(IFilterStrings.WILDCARD, ConditionOperator.MATCHES, paragraph.toString().trim());
        case FilterParserParser.OPERATION:
            String left = tree.getChild(0).getText();
            BiPredicate<String, String> op = getConditionOperator(tree.getChild(1).getText());
            String right = tree.getChild(2).getText();
            return new FilterSimpleExpressionCu(left, op, right);
        case FilterParserParser.OPERATION1:
            String left1 = tree.getChild(0).getText();
            BiPredicate<String, String> op1 = getConditionOperator(tree.getChild(1).getText());
            String right1 = null;
            return new FilterSimpleExpressionCu(left1, op1, right1);
        case FilterParserParser.OPERATION2:
        case FilterParserParser.OPERATION4:
        case FilterParserParser.OPERATION5:
            StringBuilder builder = new StringBuilder();
            int index = extractParagraph(tree, builder, 0, childCount);
            String left2 = builder.toString().trim();
            BiPredicate<String, String> op2 = getConditionOperator(tree.getChild(index++).getText());
            builder = new StringBuilder();
            extractParagraph(tree, builder, index, childCount);
            String right2 = builder.toString().trim();
            return new FilterSimpleExpressionCu(left2, op2, right2);
        case FilterParserParser.OPERATION3:
            StringBuilder builder1 = new StringBuilder();
            int index1 = extractParagraph(tree, builder1, 0, childCount);
            String left3 = builder1.toString().trim();
            BiPredicate<String, String> op3 = getConditionOperator(tree.getChild(index1).getText());
            String right3 = null;
            return new FilterSimpleExpressionCu(left3, op3, right3);
        case FilterParserParser.ROOT2:
            if (childCount == 0 || (childCount == 2 && tree.getChild(1).getType() != FilterParserParser.CONSTANT)) {
                return null;
            }

            boolean negate = tree.getChild(0).getText().equals(IFilterStrings.NOT);
            CommonTree expression = (CommonTree) tree.getChild(childCount - 1);
            FilterSimpleExpressionCu compiled = negate ? FilterSimpleExpressionNotCu.compile(expression) : FilterSimpleExpressionCu.compile(expression);
            return compiled;
        default:
            break;
        }
        return null;
    }

    private static int extractParagraph(CommonTree tree, StringBuilder builder, int index, int count) {
        String separator = " "; //$NON-NLS-1$
        int i;
        boolean stop = false;
        for (i = index; i < count && !stop; i++) {
            Tree child = tree.getChild(i);
            if (child.getType() != FilterParserParser.TEXT) {
                stop = true;
                continue;
            }
            builder.append(child.getText());
            builder.append(separator);
        }
        return --i;
    }

    /**
     * Generates a filter simple expression runtime object
     *
     * @return The filter simple expression
     */
    public FilterSimpleExpression generate() {
        return new FilterSimpleExpression(fField, fOperator, fValue);
    }

    private static BiPredicate<String, String> getConditionOperator(String equationType) {
        switch (equationType) {
        case IFilterStrings.EQUAL:
            return ConditionOperator.EQ;
        case IFilterStrings.NOT_EQUAL:
            return ConditionOperator.NE;
        case IFilterStrings.MATCHES:
            return ConditionOperator.MATCHES;
        case IFilterStrings.CONTAINS:
            return ConditionOperator.CONTAINS;
        case IFilterStrings.PRESENT:
            return ConditionOperator.PRESENT;
        case IFilterStrings.GT:
            return ConditionOperator.GT;
        case IFilterStrings.LT:
            return ConditionOperator.LT;
        default:
            throw new IllegalArgumentException("FilterSimpleExpression: invalid comparison operator."); //$NON-NLS-1$
        }
    }

    /**
     * Condition operators used to compare 2 values together
     */
    protected enum ConditionOperator implements BiPredicate<String, String> {
        /** equal */
        EQ((i, j) -> i.equals(j)),
        /** not equal */
        NE((i, j) -> !i.equals(j)),
        /** Matches */
        MATCHES(matchFunc()),
        /** Contains*/
        CONTAINS((i, j) -> i.contains(j)),
        /** Less than */
        LT((i, j) -> lessThanComparison(i, j)),
        /** Greater than */
        GT((i, j) -> greaterThanComparison(i, j)),
        /** Field is present*/
        PRESENT((i, j) -> true);

        private final BiFunction<String, String, Boolean> fCmpFunction;

        ConditionOperator(BiFunction<String, String, Boolean> cmpFunction) {
            fCmpFunction = cmpFunction;
        }

        private static BiFunction<String, String, Boolean> matchFunc() {
            return (i, j) -> {
                try {
                    Pattern filterPattern = Pattern.compile(j);
                    return filterPattern.matcher(i).find();
                } catch (PatternSyntaxException e) {
                    Activator.logWarning("The regex syntax is invalid"); //$NON-NLS-1$
                    return false;
                }
            };
        }

        private static boolean greaterThanComparison(String i, String j) {
            try {
                long long1 = Long.parseLong(i);
                long long2 = Long.parseLong(j);
                return long1 > long2;
            } catch (NumberFormatException e) {
                Activator.logWarning("The search criteria is not a number"); //$NON-NLS-1$
                return false;
            }
        }

        private static boolean lessThanComparison(String i, String j) {
            try {
                long long1 = Long.parseLong(i);
                long long2 = Long.parseLong(j);
                return long1 < long2;
            } catch (NumberFormatException e) {
                Activator.logWarning("The search criteria is not a number"); //$NON-NLS-1$
                return false;
            }
        }

        @Override
        public boolean test(String arg0, String arg1) {
            return Objects.requireNonNull(fCmpFunction.apply(arg0, arg1));
        }
    }
}
