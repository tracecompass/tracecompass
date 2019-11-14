/*******************************************************************************
 * Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAspectNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.filter.parser.FilterParserParser;

/**
 * Compilation unit for a simple filter expression
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterSimpleExpressionCu implements IFilterCu {

    private static final Format DECIMAL_FORMAT = new DecimalUnitFormat();

    private final String fField;
    private final String fOperator;
    private final @Nullable String fValue;

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
    public FilterSimpleExpressionCu(String field, String op, @Nullable String value) {
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
    protected String getOperator() {
        return fOperator;
    }

    /**
     * Get the filter value parameter
     *
     * @return The value parameter
     */
    protected @Nullable String getValue() {
        return fValue;
    }

    /**
     * Compile a simple filter expression compilation unit from a tree
     *
     * @param tree
     *            The input tree
     * @return The simple filter expression compilation unit
     */
    public static @Nullable FilterSimpleExpressionCu compile(CommonTree tree) {
        if (tree.getToken() == null) {
            return null;
        }

        int childCount = tree.getChildCount();
        switch (tree.getToken().getType()) {
        case FilterParserParser.CONSTANT:
        case FilterParserParser.PAR_CONSTANT:
            StringBuilder paragraph = new StringBuilder();
            extractParagraph(tree, paragraph, 0, childCount);
            return new FilterSimpleExpressionCu(IFilterStrings.WILDCARD, IFilterStrings.MATCHES, paragraph.toString().trim());
        case FilterParserParser.OPERATION:
            String left = Objects.requireNonNull(tree.getChild(0).getText());
            String op = Objects.requireNonNull(tree.getChild(1).getText());
            String right = tree.getChild(2).getText();
            return new FilterSimpleExpressionCu(left, op, right);
        case FilterParserParser.OPERATION1:
            String left1 = Objects.requireNonNull(tree.getChild(0).getText());
            String op1 = Objects.requireNonNull(tree.getChild(1).getText());
            String right1 = null;
            return new FilterSimpleExpressionCu(left1, op1, right1);
        case FilterParserParser.OPERATION2:
        case FilterParserParser.OPERATION4:
        case FilterParserParser.OPERATION5:
            StringBuilder builder = new StringBuilder();
            int index = extractParagraph(tree, builder, 0, childCount);
            String left2 = builder.toString().trim();
            String op2 = Objects.requireNonNull(tree.getChild(index++).getText());
            builder = new StringBuilder();
            extractParagraph(tree, builder, index, childCount);
            String right2 = builder.toString().trim();
            return new FilterSimpleExpressionCu(left2, op2, right2);
        case FilterParserParser.OPERATION3:
            StringBuilder builder1 = new StringBuilder();
            int index1 = extractParagraph(tree, builder1, 0, childCount);
            String left3 = builder1.toString().trim();
            String op3 = Objects.requireNonNull(tree.getChild(index1).getText());
            String right3 = null;
            return new FilterSimpleExpressionCu(left3, op3, right3);
        case FilterParserParser.ROOT2:
            if (childCount == 0 || (childCount == 2 && tree.getChild(1).getType() != FilterParserParser.CONSTANT)) {
                return null;
            }

            boolean negate = tree.getChild(0).getText().equals(IFilterStrings.NOT);
            CommonTree expression = Objects.requireNonNull((CommonTree) tree.getChild(childCount - 1));
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
        return new FilterSimpleExpression(fField, getConditionOperator(fOperator), fValue);
    }

    /**
     * Get the condition predicate for the operator
     *
     * @param equationType
     *            The operator to convert to predicate
     * @return The condition predicate
     */
    protected static ConditionOperator getConditionOperator(String equationType) {
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
     * Condition operators used to compare 2 values together. The first value
     * should be the internal value and the second value the value entered by
     * the user or filter.
     */
    protected enum ConditionOperator implements BiPredicate<Object, Object> {
        /** equal */
        EQ((i, j) -> equals(i, j)),
        /** not equal */
        NE((i, j) -> !equals(i, j)),
        /** Matches */
        MATCHES(matchFunc()),
        /** Contains */
        CONTAINS((i, j) -> String.valueOf(i).contains(String.valueOf(j))),
        /** Less than */
        LT((i, j) -> numericalCompare(i, j) < 0),
        /** Greater than */
        GT((i, j) -> numericalCompare(i, j) > 0),
        /** Field is present */
        PRESENT((i, j) -> true);

        private final BiFunction<Object, Object, Boolean> fCmpFunction;

        ConditionOperator(BiFunction<Object, Object, Boolean> cmpFunction) {
            fCmpFunction = cmpFunction;
        }

        private static BiFunction<Object, Object, Boolean> matchFunc() {
            return (i, j) -> {
                Pattern filterPattern = null;
                Object value = j;
                if (j instanceof Pair) {
                    Pair<?, ?> pair = (Pair<?, ?>) j;
                    if (pair.getFirst() instanceof Pattern) {
                        filterPattern = (Pattern) pair.getFirst();
                    }
                    value = pair.getSecond();
                }
                /*
                 * 'value' is the value entered by the user, if it has been
                 * converted to a Number, the equality check is better suited as
                 * the format (hex, decimal) is considered
                 */
                if (value instanceof Number && equals(i, value)) {
                    return true;
                }
                if (filterPattern != null) {
                    return filterPattern.matcher(String.valueOf(i)).find();
                }
                return false;
            };
        }

        private static boolean equals(Object i, Object j) {
            // Are objects equal
            if (Objects.equals(i, j)) {
                return true;
            }
            // Are their String representation equals
            if (Objects.equals(String.valueOf(i), String.valueOf(j))) {
                return true;
            }
            // Try to convert them to number and see if they are the same
            Number number1 = toNumber(i);
            if (number1 == null) {
                return false;
            }
            Number number2 = toNumber(j);
            if (number2 == null) {
                return false;
            }
            if (number1 instanceof Double || number2 instanceof Double
                    || number1 instanceof Float || number2 instanceof Float) {
                return number1.doubleValue() == number2.doubleValue();
            }
            return number1.longValue() == number2.longValue();
        }

        private static int numericalCompare(Object i, Object j) {
            Number number1 = toNumber(i);
            Number number2 = toNumber(j);
            if (number2 == null || number1 == null) {
                // Compare their string representation
                return String.valueOf(i).compareTo(String.valueOf(j));
            } else if (number1 instanceof Double || number2 instanceof Double
                    || number1 instanceof Float || number2 instanceof Float) {
                return Double.compare(number1.doubleValue(), number2.doubleValue());
            }
            return Long.compare(number1.longValue(), number2.longValue());
        }

        private static @Nullable Number toNumber(Object value) {
            if (value instanceof Number) {
                return (Number) value;
            }
            String val = String.valueOf(value);
            try {
                return Long.decode(val);
            } catch (NumberFormatException e) {
            }

            // Try to use some formatters to parse the value
            ParsePosition pos = new ParsePosition(0);
            Number parsed = NumberFormat.getInstance().parse(val, pos);
            // The full string should have been parsed, not just the first
            // numerical characters
            if (pos.getErrorIndex() < 0 && pos.getIndex() == val.length()) {
                return parsed;
            }

            // Try the decimal with unit formatter
            pos = new ParsePosition(0);
            Object parsedObj = DECIMAL_FORMAT.parseObject(val, pos);
            // The full string should have been parsed, not just the first
            // numerical characters
            if (pos.getErrorIndex() < 0 && pos.getIndex() == val.length() && parsedObj instanceof Number) {
                return (Number) parsedObj;
            }

            // Try the duration formatter
            pos = new ParsePosition(0);
            parsedObj = SubSecondTimeWithUnitFormat.getInstance().parseObject(val, pos);
            // The full string should have been parsed, not just the first
            // numerical characters
            if (pos.getErrorIndex() < 0 && pos.getIndex() == val.length() && parsedObj instanceof Number) {
                return (Number) parsedObj;
            }

            // Try the timestamp formatter
            try {
                return TmfTimestampFormat.getDefaulTimeFormat().parseValue(val);
            } catch (ParseException e) {
                // Nothing to do
            }

            return null;
        }

        @Override
        public boolean test(Object arg0, Object arg1) {
            return Objects.requireNonNull(fCmpFunction.apply(arg0, arg1));
        }

        /**
         * Convert a human-readable string value to its machine representation.
         * For example, duration strings such as "200ms" can be converted to the
         * long value of 200000000. For the MATCHES operator, return a pair that
         * contains the compiled pattern and the parsed value.
         *
         * @param operator
         *            The operator for which the value is prepared
         * @param value
         *            The human-readable string value entered by the user
         * @return The parsed value as a number if available, or the string
         *         itself if no formatter succeeded, or a pair of the compiled
         *         pattern and the parsed value.
         */
        public static @Nullable Object prepareValue(ConditionOperator operator, @Nullable String value) {
            if (value == null) {
                return null;
            }
            Object parsedValue = value;
            // Try to convert to a number
            Number number = toNumber(value);
            if (number != null) {
                parsedValue = number;
            }
            Pattern pattern = null;
            if (operator == ConditionOperator.MATCHES) {
                // Try to compile to a pattern
                try {
                    pattern = Pattern.compile(String.valueOf(value));
                } catch (PatternSyntaxException e) {
                    // Ignore
                }
                if (pattern != null) {
                    return new Pair<>(pattern, parsedValue);
                }
            }
            return parsedValue;
        }
    }

    @Override
    public ITmfFilterTreeNode getEventFilter(ITmfTrace trace) {
        if (fField.equals(IFilterStrings.WILDCARD)) {
            // Make a OR on all aspects
            TmfFilterOrNode orNode = new TmfFilterOrNode(null);
            for (ITmfEventAspect<?> aspect : trace.getEventAspects()) {
                TmfFilterMatchesNode node = new TmfFilterMatchesNode(null);
                node.setEventAspect(aspect);
                node.setRegex(fValue);
                orNode.addChild(node);
            }
            orNode.setNot(getNot());
            return orNode;
        }
        // Find an event aspect corresponding to the field
        ITmfEventAspect<?> filterAspect = null;
        for (ITmfEventAspect<?> aspect : trace.getEventAspects()) {
            if (aspect.getName().equals(fField)) {
                filterAspect = aspect;
                break;
            }
        }
        if (filterAspect == null) {
            // Fallback to a field aspect
            filterAspect = TmfBaseAspects.getContentsAspect().forField(fField);
        }
        TmfFilterAspectNode conditionNode = createConditionNode();
        conditionNode.setEventAspect(filterAspect);

        return conditionNode;
    }

    private TmfFilterAspectNode createConditionNode() {
        switch (fOperator) {
        case IFilterStrings.EQUAL:
            TmfFilterEqualsNode equalsNode = new TmfFilterEqualsNode(null);
            equalsNode.setValue(fValue);
            equalsNode.setNot(getNot());
            return equalsNode;
        case IFilterStrings.NOT_EQUAL:
            TmfFilterEqualsNode notEqualsNode = new TmfFilterEqualsNode(null);
            notEqualsNode.setValue(fValue);
            notEqualsNode.setNot(!getNot());
            return notEqualsNode;
        case IFilterStrings.MATCHES:
            TmfFilterMatchesNode matchesNode = new TmfFilterMatchesNode(null);
            matchesNode.setRegex(fValue);
            matchesNode.setNot(getNot());
            return matchesNode;
        case IFilterStrings.CONTAINS:
            TmfFilterContainsNode containsNode = new TmfFilterContainsNode(null);
            containsNode.setValue(fValue);
            containsNode.setNot(getNot());
            return containsNode;
        case IFilterStrings.PRESENT:
            TmfFilterMatchesNode presentNode = new TmfFilterMatchesNode(null);
            presentNode.setRegex(".*"); //$NON-NLS-1$
            presentNode.setNot(getNot());
            return presentNode;
        case IFilterStrings.GT:
            TmfFilterCompareNode gtNode = new TmfFilterCompareNode(null);
            gtNode.setResult(1);
            gtNode.setValue(fValue);
            gtNode.setNot(getNot());
            return gtNode;
        case IFilterStrings.LT:
            TmfFilterCompareNode ltNode = new TmfFilterCompareNode(null);
            ltNode.setResult(-1);
            ltNode.setValue(fValue);
            ltNode.setNot(getNot());
            return ltNode;
        default:
            throw new IllegalArgumentException("FilterSimpleExpression: invalid comparison operator."); //$NON-NLS-1$
        }
    }

    /**
     * Get whether this Cu expression is a negation
     *
     * @return <code>true</code> if the expression is a negation
     */
    protected boolean getNot() {
        return false;
    }

}
