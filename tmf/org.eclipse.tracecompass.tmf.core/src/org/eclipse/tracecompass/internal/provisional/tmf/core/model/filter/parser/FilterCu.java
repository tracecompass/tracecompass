/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.filter.parser.FilterParserLexer;
import org.eclipse.tracecompass.tmf.filter.parser.FilterParserParser;
import org.eclipse.tracecompass.tmf.filter.parser.FilterParserParser.parse_return;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

/**
 * Compilation unit for a time event filter
 *
 * @author Jean-Christian Kouame
 *
 */
public class FilterCu implements IFilterCu {

    List<FilterExpressionCu> fExpressions;

    /**
     * Constructor
     *
     * @param expressions
     *            The list of time event filter expression
     */
    public FilterCu(List<FilterExpressionCu> expressions) {
        fExpressions = expressions;
    }

    /**
     * Get the filter expression units of this filter unit
     *
     * @return The filter expression units
     */
    protected List<FilterExpressionCu> getExpressions() {
        return fExpressions;
    }

    /**
     * Compile an item filter compilation unit
     *
     * @param regex
     *            The filter regex
     * @return The filter compilation unit
     */
    public static @Nullable FilterCu compile(@Nullable String regex) {
        if (regex == null || regex.isEmpty()) {
            return null;
        }
        try {
            ANTLRStringStream stream = new ANTLRStringStream(regex);
            FilterParserLexer lexer = new FilterParserLexer(stream);
            boolean[] invalid = new boolean[1];
            String[] exception = new String[1];
            lexer.setErrorListener(e -> {
                invalid[0] |= e instanceof RecognitionException;
                exception[0] = e.getMessage();
            });

            CommonTokenStream tokens = new CommonTokenStream();
            tokens.setTokenSource(lexer);

            FilterParserParser parser = new FilterParserParser(tokens);
            parser.setErrorListener(e -> {
                invalid[0] |= e instanceof RecognitionException;
                exception[0] = e.getMessage();
            });

            parse_return parse = parser.parse();

            if (invalid[0]) {
                // Do nothing
                return null;
            }

            CommonTree tree = parse.getTree();
            List<FilterExpressionCu> expressions = new ArrayList<>();

            List<CommonTree> children = new ArrayList<>();
            if (!tree.isNil()) {
                children.add(tree);
            } else {
                children.addAll(tree.getChildren());
            }

            for (CommonTree child : children) {
                FilterExpressionCu compile = FilterExpressionCu.compile(child);
                if (compile == null) {
                    return null;
                }
                expressions.add(compile);
            }
            return new FilterCu(expressions);
        } catch (RecognitionException e) {
            // Do nothing
        }
        return null;
    }

    /**
     * Generate a filter item runtime object
     *
     * @return a filter item runtime object
     */
    public Predicate<Multimap<String, Object>> generate() {
        ImmutableList.Builder<FilterExpression> builder = ImmutableList.builder();
        for (FilterExpressionCu filterExpression : fExpressions) {
            builder.add(filterExpression.generate());
        }
        return new Filter(builder.build());
    }

    @Override
    public ITmfFilterTreeNode getEventFilter(ITmfTrace trace) {
        ITmfFilterTreeNode rootNode = new TmfFilterRootNode();
        for (FilterExpressionCu expression : fExpressions) {
            ITmfFilterTreeNode node = expression.getEventFilter(trace);
            rootNode.addChild(node);
        }
        return rootNode;
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
