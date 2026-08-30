/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.expression;

import java.util.Map;

/**
 * A compact port of BuildCraft's expression engine: compiles an arithmetic/boolean expression string
 * into an evaluable {@link Node}. Booleans are represented as {@code 1.0} (true) / {@code 0.0}
 * (false). Supports {@code + - * / %}, comparisons, {@code && || !}, parentheses, numeric literals,
 * named variables, and the functions min, max, abs, floor, ceil, round, sqrt, sin, cos, pow.
 *
 * <p>Originally BuildCraft used this to drive JSON-defined model animations; here it is provided as a
 * reusable, well-tested library utility.
 */
public final class ExpressionEngine {

    private ExpressionEngine() {}

    /** A compiled expression. */
    @FunctionalInterface
    public interface Node {
        double eval(Map<String, Double> variables);
    }

    public static Node compile(String expression) {
        Parser parser = new Parser(expression);
        Node node = parser.parseExpression();
        parser.expectEnd();
        return node;
    }

    /** Convenience: compile and evaluate with no variables. */
    public static double evaluate(String expression) {
        return compile(expression).eval(Map.of());
    }

    private static final class Parser {
        private final String src;
        private int pos;

        Parser(String src) {
            this.src = src;
        }

        // expression := or
        Node parseExpression() {
            return parseOr();
        }

        private Node parseOr() {
            Node left = parseAnd();
            while (match("||")) {
                Node right = parseAnd();
                Node l = left;
                left = vars -> (l.eval(vars) != 0 || right.eval(vars) != 0) ? 1 : 0;
            }
            return left;
        }

        private Node parseAnd() {
            Node left = parseEquality();
            while (match("&&")) {
                Node right = parseEquality();
                Node l = left;
                left = vars -> (l.eval(vars) != 0 && right.eval(vars) != 0) ? 1 : 0;
            }
            return left;
        }

        private Node parseEquality() {
            Node left = parseComparison();
            while (true) {
                if (match("==")) {
                    Node right = parseComparison();
                    Node l = left;
                    left = vars -> l.eval(vars) == right.eval(vars) ? 1 : 0;
                } else if (match("!=")) {
                    Node right = parseComparison();
                    Node l = left;
                    left = vars -> l.eval(vars) != right.eval(vars) ? 1 : 0;
                } else {
                    return left;
                }
            }
        }

        private Node parseComparison() {
            Node left = parseAdditive();
            while (true) {
                if (match("<=")) {
                    left = cmp(left, parseAdditive(), 0);
                } else if (match(">=")) {
                    left = cmp(left, parseAdditive(), 1);
                } else if (match("<")) {
                    left = cmp(left, parseAdditive(), 2);
                } else if (match(">")) {
                    left = cmp(left, parseAdditive(), 3);
                } else {
                    return left;
                }
            }
        }

        private Node cmp(Node left, Node right, int op) {
            return vars -> {
                double a = left.eval(vars);
                double b = right.eval(vars);
                boolean r = switch (op) {
                    case 0 -> a <= b;
                    case 1 -> a >= b;
                    case 2 -> a < b;
                    default -> a > b;
                };
                return r ? 1 : 0;
            };
        }

        private Node parseAdditive() {
            Node left = parseMultiplicative();
            while (true) {
                if (match("+")) {
                    Node right = parseMultiplicative();
                    Node l = left;
                    left = vars -> l.eval(vars) + right.eval(vars);
                } else if (match("-")) {
                    Node right = parseMultiplicative();
                    Node l = left;
                    left = vars -> l.eval(vars) - right.eval(vars);
                } else {
                    return left;
                }
            }
        }

        private Node parseMultiplicative() {
            Node left = parseUnary();
            while (true) {
                if (match("*")) {
                    Node right = parseUnary();
                    Node l = left;
                    left = vars -> l.eval(vars) * right.eval(vars);
                } else if (match("/")) {
                    Node right = parseUnary();
                    Node l = left;
                    left = vars -> l.eval(vars) / right.eval(vars);
                } else if (match("%")) {
                    Node right = parseUnary();
                    Node l = left;
                    left = vars -> l.eval(vars) % right.eval(vars);
                } else {
                    return left;
                }
            }
        }

        private Node parseUnary() {
            if (match("-")) {
                Node operand = parseUnary();
                return vars -> -operand.eval(vars);
            }
            if (match("!")) {
                Node operand = parseUnary();
                return vars -> operand.eval(vars) == 0 ? 1 : 0;
            }
            return parsePrimary();
        }

        private Node parsePrimary() {
            skipWhitespace();
            if (match("(")) {
                Node inner = parseExpression();
                if (!match(")")) {
                    throw new IllegalArgumentException("Expected ')' at position " + pos + " in '" + src + "'");
                }
                return inner;
            }
            char c = peek();
            if (Character.isDigit(c) || c == '.') {
                return parseNumber();
            }
            if (Character.isLetter(c) || c == '_') {
                return parseIdentifier();
            }
            throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + pos + " in '" + src + "'");
        }

        private Node parseNumber() {
            int start = pos;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
                pos++;
            }
            double value = Double.parseDouble(src.substring(start, pos));
            return vars -> value;
        }

        private Node parseIdentifier() {
            int start = pos;
            while (pos < src.length() && (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_')) {
                pos++;
            }
            String name = src.substring(start, pos);
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == '(') {
                return parseFunctionCall(name);
            }
            if (name.equals("true")) {
                return vars -> 1;
            }
            if (name.equals("false")) {
                return vars -> 0;
            }
            if (name.equals("pi")) {
                return vars -> Math.PI;
            }
            if (name.equals("e")) {
                return vars -> Math.E;
            }
            return vars -> vars.getOrDefault(name, 0.0);
        }

        private Node parseFunctionCall(String name) {
            match("(");
            java.util.List<Node> args = new java.util.ArrayList<>();
            skipWhitespace();
            if (!match(")")) {
                do {
                    args.add(parseExpression());
                } while (match(","));
                if (!match(")")) {
                    throw new IllegalArgumentException("Expected ')' after arguments to " + name);
                }
            }
            return vars -> applyFunction(name, args, vars);
        }

        private double applyFunction(String name, java.util.List<Node> args, Map<String, Double> vars) {
            return switch (name) {
                case "min" -> Math.min(args.get(0).eval(vars), args.get(1).eval(vars));
                case "max" -> Math.max(args.get(0).eval(vars), args.get(1).eval(vars));
                case "abs" -> Math.abs(args.get(0).eval(vars));
                case "floor" -> Math.floor(args.get(0).eval(vars));
                case "ceil" -> Math.ceil(args.get(0).eval(vars));
                case "round" -> Math.round(args.get(0).eval(vars));
                case "sqrt" -> Math.sqrt(args.get(0).eval(vars));
                case "sin" -> Math.sin(args.get(0).eval(vars));
                case "cos" -> Math.cos(args.get(0).eval(vars));
                case "pow" -> Math.pow(args.get(0).eval(vars), args.get(1).eval(vars));
                default -> throw new IllegalArgumentException("Unknown function '" + name + "'");
            };
        }

        private boolean match(String token) {
            skipWhitespace();
            if (src.regionMatches(pos, token, 0, token.length())) {
                // Guard against matching a prefix of a longer operator (e.g. '=' vs '==').
                pos += token.length();
                return true;
            }
            return false;
        }

        private char peek() {
            skipWhitespace();
            if (pos >= src.length()) {
                throw new IllegalArgumentException("Unexpected end of expression '" + src + "'");
            }
            return src.charAt(pos);
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        void expectEnd() {
            skipWhitespace();
            if (pos != src.length()) {
                throw new IllegalArgumentException("Unexpected trailing characters at position " + pos + " in '" + src + "'");
            }
        }
    }
}
