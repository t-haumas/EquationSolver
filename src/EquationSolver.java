import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;

public class EquationSolver {
    public static void main(String[] args)
    {
        Expression myExpression = new Expression("(15) + 4");
        System.out.println(myExpression);
        System.out.println(myExpression.getValue());
        int decimalAccuracy = 25;
        System.out.println(solve("1 + (x + ) 2 = 0", decimalAccuracy));
        /*
         * Coefficients must be split away and multiplied - including negatives!
         */
        //TODO: add checking to see if it grows very fast (or very slow?)
        //TODO: check if it's taking a long time, and run again.
        //TODO: try making range offset / size random as well.
        /*
        Seems like sometimes converging takes a long time...
         */
    }

    public static BigDecimal solve(String equation, int numDecimalsAccurate) {
        String expressionString = equation.substring(0, equation.indexOf("="));
        char variableCharacter = '@';
        for (char character : expressionString.toCharArray()) {
            if (character >= 65 && character <= 90 || character >= 97 && character <= 122) {
                variableCharacter = character;
                break;
            }
        }
        System.out.println(new Expression(expressionString.replaceAll(String.valueOf(variableCharacter), "9")));
        if (variableCharacter == '@') {
            throw new IllegalArgumentException("No variable in the provided expression " + expressionString + ".");
        }
        BigDecimal target = new Expression(equation.substring(equation.indexOf("=") + 1)).getValue();
        //BigDecimal target = new BigDecimal(equation.substring(equation.indexOf("=") + 1).replaceAll(" ", ""));
        BigDecimal[] lowerBound = new BigDecimal[2];
        BigDecimal[] upperBound = new BigDecimal[2];
        findBounds(lowerBound, upperBound, expressionString, variableCharacter, target);
        if (lowerBound[0] == null || upperBound[0] == null)
        {
            System.err.println("Unable to find a solution to " + equation + ".");
            return null;
        }
        convergeBounds(lowerBound, upperBound, expressionString, variableCharacter, target, numDecimalsAccurate);
        System.out.println("An input of " + lowerBound[0].toPlainString() + " gets the output " + lowerBound[1].toPlainString() + ".");
        return getAgreedUponBigDecimal(upperBound[0], lowerBound[0]);
    }

    private static BigDecimal getAgreedUponBigDecimal(BigDecimal bd1, BigDecimal bd2) {
        int agreedUponDigits = bd1.subtract(bd2).scale() - bd1.subtract(bd2).precision();
        return bd1.round(new MathContext(agreedUponDigits + 1));
    }

    private static void convergeBounds(BigDecimal[] lowerBound, BigDecimal[] upperBound,
                                       String expressionString, char variableCharacter,
                                       BigDecimal target, int numDecimalsAccurate) {
        while (lowerBound[1].subtract(target).abs().compareTo(BigDecimal.valueOf(Math.pow(10,-numDecimalsAccurate))) > 0 || upperBound[1].subtract(target).abs().compareTo(BigDecimal.valueOf(Math.pow(10,-numDecimalsAccurate))) > 0) {
            BigDecimal newInput = (lowerBound[0].add(upperBound[0])).divide(BigDecimal.valueOf(2), 100, RoundingMode.HALF_UP);
            BigDecimal newOutput = new Expression(expressionString.replaceAll(String.valueOf(variableCharacter), newInput.toPlainString())).getValue();
            forceUpdateBoundsBasedOnThisTest(newInput, newOutput, lowerBound, upperBound, target);
        }
        lowerBound[0] = lowerBound[0].stripTrailingZeros();
        lowerBound[1] = lowerBound[1].stripTrailingZeros();
        upperBound[0] = upperBound[0].stripTrailingZeros();
        upperBound[1] = upperBound[1].stripTrailingZeros();

    }

    private static void forceUpdateBoundsBasedOnThisTest(BigDecimal input, BigDecimal output, BigDecimal[] lowerBound, BigDecimal[] upperBound, BigDecimal target) {
        int outputCompareToTarget = target.compareTo(output);
        if (outputCompareToTarget > 0) {
            lowerBound[0] = input;
            lowerBound[1] = output;
        } else if (outputCompareToTarget < 0) {
            upperBound[0] = input;
            upperBound[1] = output;
        } else {
            System.out.println("We just found the correct one by pure chance! " + output);
        }
    }

    public static void findBounds(BigDecimal[] lowerBound, BigDecimal[] upperBound, String expressionString, char variableCharacter, BigDecimal target) {
        Random randomGenerator = new Random();
        int totalAttemptsMade = 0;

        //ten thousand attempts 10000
        for (int numAttemptsPerRound = 100; numAttemptsPerRound <= 10000; numAttemptsPerRound *= 10) {
            System.out.println("new round");
            for (int range = 1; range < 1000000000; range *= 100) {
                System.out.println("new range: " + range);
                System.out.println(numAttemptsPerRound);
                for (int attempts = 1; attempts <= numAttemptsPerRound;) {
                    totalAttemptsMade++;
                    attempts += makeAttempt(range, randomGenerator, lowerBound, upperBound, expressionString, variableCharacter, target);
                    if (upperBound[0] != null && lowerBound[0] != null) {
                        System.out.println("bounds found in -" + range + " to " + range + " after " + totalAttemptsMade + " attempts.");
                        return;
                    }
                }
            }
        }
    }

    private static int makeAttempt(int range, Random randomGenerator, BigDecimal[] lowerBound, BigDecimal[] upperBound,
                                    String expressionString, char variableCharacter, BigDecimal target) {
        BigDecimal input = BigDecimal.valueOf(randomGenerator.nextInt(range * 2) - range + randomGenerator.nextDouble());
        try {
            BigDecimal output = new Expression(expressionString.replaceAll(String.valueOf(variableCharacter), input.toString())).getValue();
            updateBoundsBasedOnThisTest(input, output, lowerBound, upperBound, target);
            return 1;
        }
        catch (ArithmeticException ignored)
        { return 0;}
    }

    public static void updateBoundsBasedOnThisTest(BigDecimal input, BigDecimal output, BigDecimal[] lowerBound, BigDecimal[] upperBound, BigDecimal target) {
        int outputCompareToTarget = target.compareTo(output);
        if (outputCompareToTarget > 0) {
            // if target > output, thistest could be a lower bound.
            if (lowerBound[0] == null) {
                lowerBound[0] = input;
                lowerBound[1] = output;
            }
            else if (output.compareTo(lowerBound[1]) > 0) {
                // ThisTest is closer to target than current lower bound.
                lowerBound[0] = input;
                lowerBound[1] = output;
            }
        } else if (outputCompareToTarget < 0) {
            // if target < output, thistest could be an upper bound.
            if (upperBound[0] == null)
            {
                upperBound[0] = input;
                upperBound[1] = output;
            }
            else if (output.compareTo(upperBound[1]) < 0)
            {
                // ThisTest is closer to target than current lower bound.
                upperBound[0] = input;
                upperBound[1] = output;
            }
        } else {
            System.out.println("We just found the correct one by pure chance! " + output);
        }
    }
}
