package org.ff4j.feature.togglestrategy.expression;

/*-
 * #%L
 * ff4j-core
 * %%
 * Copyright (C) 2013 - 2018 FF4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Syntax Parser for expression as ( (sampleA|sampleB) & (C|D|!B) & !(A|D) ) | ( (A&B&C)|(C&D)|(A|B&D) )
 * 
 * to be converted as :
 * 
 * <pre>
 *                           +------------ OR ---------------+
 *                          /                                 \
 *             +--------- AND ------+            +------ OR ----+   
 *            /            |         \          /        |       \
 *           OR           OR         NOT        AND      AND       OR
 *         /    \        / | \        |        / | \     / \      / \
 *   sampleA sampleB    C  D  NOT    OR        A  B  C   C   D    A   AND
 *                             |    /   \                             / \
 *                             B   A     D                           B   D
 * </pre>
 */
public final class ExpressionParser {

    /** character parsed. */
    private static final char OPEN_BRACKET = '(';

    /** character parsed. */
    private static final char CLOSE_BRACKET = ')';

    /** character parsed. */
    private static final char OR = ExpressionOperator.OR.getChar();

    /** character parsed. */
    private static final char AND = ExpressionOperator.AND.getChar();

    /** character parsed. */
    private static final char NOT = ExpressionOperator.NOT.getChar();

    /**
     * Hide constructor for utility class.
     */
    private ExpressionParser() {}

    /**
     * Utility class to store expression and offset of it to avoid multimap
     */
    private static class ExpressionBracket {

        /** store expression. */
        private final String expr;

        /** offset of end of this expression in parent expression */
        private final int offsetEnd;

        /**
         * Convenient constructor for initialization.
         * 
         * @param e
         *            target expression
         * @param o
         *            target offset
         */
        public ExpressionBracket(String e, int o) {
            this.expr = e;
            this.offsetEnd = o;
        }

        /**
         * Getter accessor for attribute 'expr'.
         * 
         * @return current value of 'expr'
         */
        public String getExpr() {
            return expr;
        }

        /**
         * Getter accessor for attribute 'offsetEnd'.
         * 
         * @return current value of 'offsetEnd'
         */
        public int getOffsetEnd() {
            return offsetEnd;
        }
    }

    /**
     * Brackets have been detected, extract expression to be recursively parsed.
     * 
     * @param workingExpr
     *            current working expression
     * @return expression within brackets
     */
    private static ExpressionBracket extractExprWithinParenthesis(String workingExpr) {
        char[] chars = workingExpr.toCharArray();
        int offsetOpenBracket = workingExpr.indexOf(OPEN_BRACKET);
        int offsetEnd = offsetOpenBracket + 1;
        int bracketLevel = 0;
        boolean closingBracketFound = false;
        while (!closingBracketFound) {
            if (chars[offsetEnd] == OPEN_BRACKET) {
                bracketLevel++;
            }
            if (chars[offsetEnd] == CLOSE_BRACKET) {
                if (bracketLevel == 0) {
                    closingBracketFound = true;
                }
                bracketLevel--;
            }
            offsetEnd++;
        }

        // Expression Within parenthesis
        String withinBracketExpr = workingExpr.substring(offsetOpenBracket + 1, offsetEnd - 1);
        return new ExpressionBracket(withinBracketExpr, offsetEnd);
    }

    /**
     * Build Expression tree from string.
     * 
     * @param expressionInput
     *            expression.
     */
    public static ExpressionNode parseExpression(String expressionInput) {
        ExpressionNode returnedNode;
        String expression = expressionInput.replaceAll(" ", "");

        // Only use operator priorities to build.
        if (!expression.contains(String.valueOf(OPEN_BRACKET))) {
            return parseExpressionWithoutParenthesis(expression);
        }

        int idx = 0;
        String workingExpr = expression;
        Map<String, String> exprMap = new HashMap<>();
        Map<String, ExpressionNode> exprNodes = new HashMap<>();

        // Embedded Parenthesis
        while (workingExpr.contains(String.valueOf(OPEN_BRACKET))) {
            // Find expression in parentheses
            ExpressionBracket eb = extractExprWithinParenthesis(workingExpr);

            // Escaping operators
            exprMap.put("P" + idx, eb.getExpr().replaceAll("\\|", " OR ").replaceAll("&", " AND "));

            // Saving subNode
            exprNodes.put("P" + idx, parseExpression(eb.getExpr()));
            idx++;

            // Is there any expression with same level of parenthesis ?
            if ((eb.getOffsetEnd() + 1) <= workingExpr.length()) {
                workingExpr = workingExpr.substring(eb.getOffsetEnd() + 1);
            } else {
                // stop
                workingExpr = "";
            }

        }
        // LOG.debug("Embedded Parenthesis finished : INITIAL=" + initExpr + " WITH NODES=" + exprNodes);

        // Take Initial Expression and replace expression within first parenthesis level to constants
        String exp = workingExpr.replaceAll("\\|", " OR ").replaceAll("&", " AND ");
        for (Entry<String, String> entries : exprMap.entrySet()) {
            // Escaping parenthesis
            String val = entries.getValue().replaceAll("\\(", "\\\\\\(").replaceAll("\\)", "\\\\\\)");
            exp = exp.replaceAll("\\(" + val + "\\)", entries.getKey());
        }

        // Place real operator for recursive calls
        exp = exp.replaceAll(" AND ", "&").replaceAll(" OR ", "|").replace(" ", "");
        // LOG.debug("Embedded Parenthesis finished with : " + exp + " : " + exprMap.keySet());

        returnedNode = parseExpressionWithoutParenthesis(exp);

        // Process substitution
        ExpressionNode tmpNode = new ExpressionNode(returnedNode.getOperator());
        tmpNode.getSubNodes().addAll(returnedNode.getSubNodes());
        for (ExpressionNode exprNode : returnedNode.getSubNodes()) {
            processSubstitution(exprNode, tmpNode, exprNodes);
        }

        return tmpNode;
    }

    /**
     * Sub expression have been store and substitute by constant, remove constant and evaluate.
     * 
     * @param exprNode
     *            current node
     * @param tmpNode
     *            substituted node
     * @param exprNodes
     *            available nodes
     */
    private static void processSubstitution(ExpressionNode exprNode, ExpressionNode tmpNode, Map<String, ExpressionNode> exprNodes) {
        // Only sheets : (there are only NOT operators with level inner)
        if (exprNode.getValue() != null && !exprNode.getValue().isEmpty()) {
            if (exprNodes.containsKey(exprNode.getValue())) {
                ExpressionNode storedNode = exprNodes.get(exprNode.getValue());
                // LOG.info("Replacing '" + exprNode + "' by '" + storedNode + "'");
                tmpNode.getSubNodes().add(storedNode);
                tmpNode.getSubNodes().remove(exprNode);
            }
        } else {
            // Not with SUB LEVEL
            ExpressionNode subNodeNot = exprNode.getSubNodes().get(0);
            if (exprNodes.containsKey(subNodeNot.getValue())) {
                ExpressionNode notNode = new ExpressionNode(ExpressionOperator.NOT);
                notNode.getSubNodes().add(exprNodes.get(subNodeNot.getValue()));
                // LOG.info("Replacing '" + subNodeNot + "' by '" + exprNodes.get(subNodeNot.getValue()) + "'");
                tmpNode.getSubNodes().add(notNode);
                tmpNode.getSubNodes().remove(exprNode);
            }
        }
    }

    /**
     * If no bracket nor OR operator contains AND/NOT.
     * 
     * @param currentNode
     *            current OR Node
     * @param expr
     *            current expression
     */
    private static ExpressionNode parseExpressionAndNot(ExpressionNode currentNode, String expr) {
        String[] andOperatorArray = expr.replaceAll(" ", "").split("\\\\" + AND);
        if (andOperatorArray.length > 1) {
            // There is AND operation, loop over elements
            // LOG.debug("NoBracket [" + expr + "] : Operator AND");
            ExpressionNode subNodeAND = new ExpressionNode(ExpressionOperator.AND);
            for (String andOperator : andOperatorArray) {
                if (andOperator.startsWith("!")) {
                    ExpressionNode node = new ExpressionNode(ExpressionOperator.NOT);
                    node.getSubNodes().add(new ExpressionNode(andOperator.substring(1)));
                    subNodeAND.getSubNodes().add(node);
                } else {
                    // Handle sheet
                    // LOG.debug("Adding sheet [" + andOperator + "]");
                    subNodeAND.getSubNodes().add(new ExpressionNode(andOperator));
                }
            }
            if (currentNode != null) {
                currentNode.getSubNodes().add(subNodeAND);
                return currentNode;
            } else {
                return subNodeAND;
            }
        } else if (expr.startsWith("!")) {
            return parseOperatorNot(currentNode, expr);
        }
        return parseSheet(currentNode, expr);
    }

    /**
     * Convenient method to parse operator NOT expressions.
     * 
     * @param currentNode
     *            current node
     * @param expr
     *            current expression
     * @return parsed expression as node
     */
    private static ExpressionNode parseOperatorNot(ExpressionNode currentNode, String expr) {
        // LOG.debug("NoBracket NoOR [" + expr + "] : Operator NOT");
        ExpressionNode subNodeNot = new ExpressionNode(ExpressionOperator.NOT);
        subNodeNot.getSubNodes().add(new ExpressionNode(expr.substring(1)));
        if (currentNode != null) {
            currentNode.getSubNodes().add(subNodeNot);
        } else {
            return subNodeNot;
        }
        return currentNode;
    }

    /**
     * Convenient method to parse operator SHEETS expressions.
     * 
     * @param currentNode
     *            current node
     * @param expr
     *            current expression
     * @return parsed expression as node
     */
    private static ExpressionNode parseSheet(ExpressionNode currentNode, String expr) {
        // LOG.debug("Adding sheet [" + expr + "]");
        if (currentNode != null) {
            currentNode.getSubNodes().add(new ExpressionNode(expr));
        } else {
            return new ExpressionNode(expr);
        }
        return currentNode;
    }

    /**
     * The expression does not contain any parenthesis. We will split through operator Should not be without any opera
     * 
     * @param expr
     *            expression to be parsed
     */
    private static ExpressionNode parseExpressionWithoutParenthesis(String expr) {
        // Expression without operator => Sheet
        if (!expr.contains(String.valueOf(OR)) && !expr.contains(String.valueOf(AND)) && !expr.contains(String.valueOf(NOT))) {
            // LOG.info("Unique sheet : " + expr);
            return new ExpressionNode(expr);

        } else {
            // First priority to & then to |
            String[] orOperatorArray = expr.split("\\\\" + OR);
            // current
            ExpressionNode currentNode;
            // less priority operator is OR
            if (orOperatorArray.length > 1) {
                // LOG.debug("NoBracket [" + expr + "] : Operator OR");
                currentNode = new ExpressionNode(ExpressionOperator.OR);
                for (String orOperator : orOperatorArray) {
                    currentNode = parseExpressionAndNot(currentNode, orOperator);
                }
            } else {
                // Check next priority operator AND
                // LOG.debug("NoBracket [" + expr + "] : No Operator OR");
                currentNode = parseExpressionAndNot(null, expr);
            }
            return currentNode;
        }
    }

}
