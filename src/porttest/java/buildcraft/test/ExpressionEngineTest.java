/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import buildcraft.lib.expression.ExpressionEngine;

class ExpressionEngineTest {

    private static final double EPS = 1.0e-9;

    @Test
    void arithmeticRespectsPrecedence() {
        assertEquals(14.0, ExpressionEngine.evaluate("2 + 3 * 4"), EPS);
        assertEquals(20.0, ExpressionEngine.evaluate("(2 + 3) * 4"), EPS);
        assertEquals(2.0, ExpressionEngine.evaluate("10 % 4"), EPS);
        assertEquals(-6.0, ExpressionEngine.evaluate("2 * -3"), EPS);
    }

    @Test
    void booleanLogicAndComparisons() {
        assertEquals(1.0, ExpressionEngine.evaluate("3 > 2"), EPS);
        assertEquals(0.0, ExpressionEngine.evaluate("3 < 2"), EPS);
        assertEquals(1.0, ExpressionEngine.evaluate("1 && 1"), EPS);
        assertEquals(0.0, ExpressionEngine.evaluate("1 && 0"), EPS);
        assertEquals(1.0, ExpressionEngine.evaluate("0 || 1"), EPS);
        assertEquals(1.0, ExpressionEngine.evaluate("!0"), EPS);
        assertEquals(1.0, ExpressionEngine.evaluate("(2 == 2) && (3 != 4)"), EPS);
    }

    @Test
    void functionsAndConstants() {
        assertEquals(5.0, ExpressionEngine.evaluate("max(2, 5)"), EPS);
        assertEquals(2.0, ExpressionEngine.evaluate("min(2, 5)"), EPS);
        assertEquals(3.0, ExpressionEngine.evaluate("abs(-3)"), EPS);
        assertEquals(8.0, ExpressionEngine.evaluate("pow(2, 3)"), EPS);
        assertEquals(4.0, ExpressionEngine.evaluate("floor(4.9)"), EPS);
        assertEquals(0.0, ExpressionEngine.evaluate("sin(0)"), EPS);
        assertEquals(Math.PI, ExpressionEngine.evaluate("pi"), EPS);
    }

    @Test
    void variablesAreResolvedFromContext() {
        ExpressionEngine.Node node = ExpressionEngine.compile("heat * 2 + offset");
        assertEquals(25.0, node.eval(Map.of("heat", 10.0, "offset", 5.0)), EPS);
        // Missing variables default to zero.
        assertEquals(20.0, node.eval(Map.of("heat", 10.0)), EPS);
    }
}
