import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Tests {

    /* Arithmetic tests */

    @Test
    public void basicAdd() {
        Expression expression = new Expression("3 + 2");
        assert(expression.getValue().intValue() == 5);
    }

    @Test
    public void basicSubtract() {
        Expression expression = new Expression("3 - 2");
        assert(expression.getValue().intValue() == 1);
    }

    @Test
    public void basicMultiply() {
        Expression expression = new Expression("3 * 2");
        assert(expression.getValue().intValue() == 6);
    }

    @Test
    public void basicDivide() {
        Expression expression = new Expression("6 / 2");
        assert(expression.getValue().intValue() == 3);
    }

    @Test
    public void basicPower() {
        Expression expression = new Expression("6 ^ 2.5");
        assert(expression.getValue().intValue() == 88);
    }

    @Test
    public void basicAddFirstNegative() {
        Expression expression = new Expression("-3 + 2");
        assert(expression.getValue().intValue() == -1);
    }

    @Test
    public void basicSubtractFirstNegative() {
        Expression expression = new Expression("-3 - 2");
        assert(expression.getValue().intValue() == -5);
    }

    @Test
    public void basicMultiplyFirstNegative() {
        Expression expression = new Expression("-3 * 2");
        assert(expression.getValue().intValue() == -6);
    }

    @Test
    public void basicDivideFirstNegative() {
        Expression expression = new Expression("-6 / 2");
        assert(expression.getValue().intValue() == -3);
    }

    @Test
    public void basicPowerFirstNegative() {
        Expression expression = new Expression("(-6) ^ 3");
        assert(expression.getValue().intValue() == -216);
    }

    @Test
    public void basicAddSecondNegative() {
        Expression expression = new Expression("3 + -2");
        assert(expression.getValue().intValue() == 1);
    }

    @Test
    public void basicSubtractSecondNegative() {
        Expression expression = new Expression("3 - -2");
        assert(expression.getValue().intValue() == 5);
    }

    @Test
    public void basicMultiplySecondNegative() {
        Expression expression = new Expression("3 * -2");
        assert(expression.getValue().intValue() == -6);
    }

    @Test
    public void basicDivideSecondNegative() {
        Expression expression = new Expression("6 / -2");
        assert(expression.getValue().intValue() == -3);
    }

    @Test
    public void basicPowerSecondNegative() {
        Expression expression = new Expression("6 ^ - 2.5");
        assert(Math.abs(expression.getValue().doubleValue() - 0.01134) < 0.0001);
    }

    @Test
    public void basicAddBothNegative() {
        Expression expression = new Expression("-3 + -2");
        assert(expression.getValue().intValue() == -5);
    }

    @Test
    public void basicSubtractBothNegative() {
        Expression expression = new Expression("-3 - -2");
        assert(expression.getValue().intValue() == -1);
    }

    @Test
    public void basicMultiplyBothNegative() {
        Expression expression = new Expression("-3 * -2");
        assert(expression.getValue().intValue() == 6);
    }

    @Test
    public void basicDivideBothNegative() {
        Expression expression = new Expression("-6 / -2");
        assert(expression.getValue().intValue() == 3);
    }

    @Test
    public void basicPowerBothNegative() {
        Expression expression = new Expression("(-3) ^ (-2)");
        assert(Math.abs(expression.getValue().doubleValue() - 0.1111111111) < 0.0001);
    }

    @Test
    public void orderOfOperations1() {
        assertEquals(new Expression("7 - 24 / 8 * 4 + 6").getValue(), new BigDecimal("1"));
    }

    @Test
    public void orderOfOperations2() {
        assertEquals(new Expression("-3 * 2^2 - 16 / 4").getValue(), new BigDecimal("-16"));
    }

    /* Construction tests */

    @Test
    public void inconstructibleStartsWithOperation() {
        try {
            Expression expression = new Expression("* 4 / 3");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleEndsWithOperation() {
        try {
            Expression expression = new Expression("4 * 3 / ");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleEndsWithMinus() {
        try {
            Expression expression = new Expression("4 * 3 - ");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleTwoOperationsInaRow() {
        try {
            Expression expression = new Expression("4 * / 3");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    /** Nested expression tests **/
    /* Nested expression arithmetic */

    @Test
    public void nestedOrderOfOperations1() {
        assertEquals(new Expression("((1-(2-(3-4)*3/2)/3)+5)").getValue().intValue(), 4);
        assert(new Expression("((1-(2-(3-4)*3/2)/3)+5)").getValue().subtract(new BigDecimal("4.833333")).abs().compareTo(new BigDecimal("0.01")) < 0);
    }

    @Test
    public void nestedOrderOfOperations2() {
        assertEquals(new Expression("-2 * (1*4-2/2)+(6+2-3)").getValue(), new BigDecimal("-1"));
    }

    @Test
    public void parenthesesNextToEachOtherAreMultiplied1() {
        assertEquals(new Expression("(3-1)(1 * 3)").getValue(), new BigDecimal("6"));
    }

    @Test
    public void parenthesesNextToEachOtherAreMultiplied2() {
        assertEquals(new Expression("(3-1)(1 * 3)(-2 * (1*4-2/2)+(6+2-3))").getValue(), new BigDecimal("-6"));
    }

    @Test
    public void coefficientsInFrontOfParenthesisAreMultipliedCorrectly() {
        assertEquals(new Expression("4(3^2)").getValue(), new BigDecimal("36"));
    }

    @Test
    public void negativeCoefficientsInFrontOfParenthesisAreMultipliedCorrectly() {
        assertEquals(new Expression("-4(3^2)").getValue(), new BigDecimal("-36"));
    }

    @Test
    public void operandsNextToEachOtherAreMultiplied() {
        assertEquals(new Expression("(3^2)4").getValue(), new BigDecimal("36"));
    }

    @Test
    public void lotsOfAdjacentsBeingMultiplied1() {
        assertEquals(new Expression("16(16-4)3").getValue(), new BigDecimal("576"));
    }

    @Test
    public void lotsOfAdjacentsBeingMultiplied2() {
        assertEquals(new Expression("(1/4)16(16-4)3").getValue(), new BigDecimal("144"));
    }

    @Test
    public void lotsOfAdjacentsBeingMultiplied3() {
        assertEquals(new Expression("(1/4)(-16)(16-4)3").getValue(), new BigDecimal("-144"));
    }

    /* Nested expression construction tests */

    @Test
    public void inconstructibleTooManyOpenParenthesis() {
        try {
            Expression expression = new Expression("(2-1");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleTooManyOpenParenthesis2() {
        try {
            Expression expression = new Expression("(2-(1*4)");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleTooManyOpenParenthesis3() {
        try {
            Expression expression = new Expression("2-(1*4");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleTooManyClosedParenthesis1() {
        try {
            Expression expression = new Expression("2-1*4)");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleTooManyClosedParenthesis2() {
        try {
            Expression expression = new Expression("(2-1*4))");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleTooManyClosedParenthesis3() {
        try {
            Expression expression = new Expression("2-1)*4");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleParensEndsWithOperand() {
        try {
            Expression expression = new Expression("(2- )4");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleParensEndsWithOperand2() {
        try {
            Expression expression = new Expression("4 * (2/ )3 + 1");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleParensStartWithOperand1() {
        try {
            Expression expression = new Expression("1 + ( * 3 )");
        } catch (Exception e) {
            return;
        }
        fail();
    }

    @Test
    public void inconstructibleParensStartWithOperand2() {
        try {
            Expression expression = new Expression("16( * 3 )4");
        } catch (Exception e) {
            return;
        }
        fail();
    }

}
