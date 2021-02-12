import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Expression {

    private LinkedList<Expression> operands;
    private LinkedList<Operation> operations;
    private BigDecimal value;
    private int numNestedExpressions;
    private StringBuilder operandBuilder;
    private int currentCharacterPosition;
    private String pureExpressionString;

    public Expression(String expressionString) {

        operands = new LinkedList<>();
        operations = new LinkedList<>();
        numNestedExpressions = 0;

        pureExpressionString = expressionString.replaceAll(" ", "");
        operandBuilder = new StringBuilder();

        currentCharacterPosition = 0;
        for (char character : pureExpressionString.toCharArray())
        {
            if (character == '(')
            {
                if (numNestedExpressions > 0)
                {
                    operandBuilder.append('(');
                }
                numNestedExpressions++;
            }
            else if (character == ')')
            {
                numNestedExpressions--;
                if (numNestedExpressions > 0)
                {
                    operandBuilder.append(')');
                }
            }
            else if (isOperandCharacter(character)) {
                operandBuilder.append(character);
                if (! nextCharacterIsOperandCharacter(currentCharacterPosition, pureExpressionString) || nextCharacterWillFinishNestedExpression(currentCharacterPosition, pureExpressionString))
                {
                    // This is the last character of the operand.
                    addOperand();
                }
            }
            else if (isRawOperationCharacter(character))
            {
                operations.add(new Operation(character, operations.size()));
            }
            else
            {
                throw new IllegalArgumentException("Invalid character in expression: '" + character + "'.");
            }
            currentCharacterPosition++;
        }

        if (numNestedExpressions != 0)
        {
            throw new ExpressionParseException("Parenthesis mismatch: too many " + (numNestedExpressions < 0 ? "closing" : "opening") + " parenthesis.");
        }

        evaluate();
    }

    public Expression(BigDecimal value)
    {
        this.value = new BigDecimal(value.toString(), new MathContext(100));
    }

    private boolean nextCharacterWillFinishNestedExpression(int currentCharacterPosition, String pureExpressionString)
    {
        if (currentCharacterPosition == pureExpressionString.length() - 1) {
            return false;
        }
        return pureExpressionString.charAt(currentCharacterPosition + 1) == ')' && numNestedExpressions == 1;
    }

    private boolean nextCharacterIsOperandCharacter(int currentCharacterPosition, String pureExpressionString)
    {
        if (currentCharacterPosition == pureExpressionString.length() - 1) {
            return false;
        }
        return isOperandCharacter(pureExpressionString.charAt(currentCharacterPosition + 1));
    }

    private boolean isRawOperationCharacter(char character)
    {
        return character == '*' || character == '^' || character == '-' || character == '/' || character == '+';
    }

    private boolean isOperandCharacter(char character)
    {
        return '0' <= character && character <= '9' || character == '.' || character == '-' && justFinishedParsingOperation() || numNestedExpressions > 0;
    }

    private boolean justFinishedParsingOperation()
    {
        return operations.size() == operands.size() && operandBuilder.length() == 0 || pureExpressionString.charAt(0) == '-' && currentCharacterPosition == 0;
    }

    private void addOperand() {
        try {
            String operandString = operandBuilder.toString();
            if (stringContainsRawOperationCharacter(operandString) && ! stringIsNegativeNumber(operandString)) {
                operands.add(new Expression(operandString));
            } else {
                operands.add(new Expression(new BigDecimal(operandString, new MathContext(100))));
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Illegal operand: '" + operandBuilder.toString() + "'.");
        }
        operandBuilder.delete(0, operandBuilder.length());
    }

    private boolean stringIsNegativeNumber(String operandString) {
        if (operandString.charAt(0) == '-') {
            try {
                BigDecimal bigDecimal = new BigDecimal(operandString);
                return true;
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean stringContainsRawOperationCharacter(String operand)
    {
        for (char c : operand.toCharArray())
        {
            if (isRawOperationCharacter(c))
            {
                return true;
            }
        }
        return false;
    }

    private void evaluate() {
        PriorityQueue<Operation> operationsToEvaluate = new PriorityQueue<>(10, Collections.reverseOrder());
        for (Operation operation : operations)
        {
            operationsToEvaluate.offer(operation);
        }
        Operation currentOperation;
        Expression result;
        while (! operationsToEvaluate.isEmpty())
        {
            currentOperation = operationsToEvaluate.remove();
            int currentOperationIndex = operations.indexOf(currentOperation);
            Expression firstOperand = operands.remove(currentOperationIndex);
            Expression secondOperand = operands.remove(currentOperationIndex);
            result = new Expression(evaluateOperation(currentOperation, firstOperand, secondOperand));
            operations.remove(currentOperation);
            operands.add(currentOperationIndex, result);
        }
        value = operands.get(0).value;
    }

    private BigDecimal evaluateOperation(Operation currentOperation, Expression firstOperand, Expression secondOperand)
    {
        if (currentOperation.getType() == OperationType.ADD)
        {
            return firstOperand.getValue().add(secondOperand.getValue());
        }
        else if (currentOperation.getType() == OperationType.SUBTRACT)
        {
            return firstOperand.getValue().subtract(secondOperand.getValue());
        }
        else if (currentOperation.getType() == OperationType.MULTIPLY)
        {
            return firstOperand.getValue().multiply(secondOperand.getValue());
        }
        else if (currentOperation.getType() == OperationType.DIVIDE)
        {
            return firstOperand.getValue().divide(secondOperand.getValue(), 100, RoundingMode.HALF_UP);
        }
        else
        {
            return BigDecimalMath.pow(firstOperand.getValue(), secondOperand.getValue(), new MathContext(100));
        }
    }

    public BigDecimal getValue() {
        return new BigDecimal(value.toString()).stripTrailingZeros();
    }

    public String toString() {
        StringBuilder output = new StringBuilder("(");
        if (operations == null)
        {
            return value.toString();
        }
        for (int i = 0; i < operations.size() + operands.size(); i++)
        {
            if (i % 2 == 0) {
                output.append(operands.get(i / 2));
            }
            else {
                output.append(operations.get(i / 2));
            }
        }
        output.append(")");
        return output.toString();
    }
}
