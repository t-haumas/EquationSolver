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
    private boolean evaluated;
    private BigDecimal value;
    private int numNestedExpressions;

    public Expression(String expressionString) {
        evaluated = false;
        operands = new LinkedList<>();
        operations = new LinkedList<>();

        String myExpressionString = expressionString.replaceAll(" ", "");
        StringBuilder operandBuilder = new StringBuilder();
        numNestedExpressions = 0;
        int i = 0;
        for (char character : myExpressionString.toCharArray())
        {
            if (character >= 48 && character <= 57 || character == 46)
            {
                appendToExpression(operandBuilder, character, true);
            }
            else if (character == 42 || character == 43 || character == 47 || character == 94)
            {
                appendToExpression(operandBuilder, character, false);
            }
            else if (character == 45) {
                if (i == 0)
                {
                    appendToExpression(operandBuilder, character, true);
                }
                else {
                    char previousCharacter = myExpressionString.charAt(i - 1);
                    if (previousCharacter >= 48 && previousCharacter <= 57 || previousCharacter == 46 || previousCharacter == ')') {
                        appendToExpression(operandBuilder, character, false);
                    } else {
                        appendToExpression(operandBuilder, character, true);
                    }
                }
            }
            else if (character == 40)
            {
                numNestedExpressions++;
                if (numNestedExpressions > 1) {
                    appendToExpression(operandBuilder, character, true);
                }
            }
            else if (character == 41)
            {
                numNestedExpressions--;
                if (numNestedExpressions >= 1) {
                    appendToExpression(operandBuilder, character, true);
                }
            }
            else
            {
                throw new IllegalArgumentException("Invalid character in expression: '" + character + "'.");
            }
            i++;
        }
        addOperand(operandBuilder);
    }

    public Expression(BigDecimal value)
    {
        this.value = new BigDecimal(value.toString(), new MathContext(100));
        evaluated = true;
    }

    private void appendToExpression(StringBuilder operandBuilder, char character, boolean isOperandCharacter) {
        if (isOperandCharacter || numNestedExpressions > 0) {
            operandBuilder.append(character);
        } else {
            if (operandBuilder.length() < 1) {
                throw new IllegalArgumentException("Must have an operand before operation '" + character + "'.");
            }
            addOperand(operandBuilder);
            operandBuilder.delete(0, operandBuilder.length());
            operations.add(new Operation(character, operations.size()));
        }
    }

    private void addOperand(StringBuilder operandBuilder) {
        try {
            if (operandBuilder.toString().contains("+") || operandBuilder.toString().contains("*") || operandBuilder.toString().contains("/") || operandBuilder.toString().contains("^") || operandBuilder.toString().indexOf("-") > 0 || containsMultipleMinuses(operandBuilder.toString().toCharArray())) {
                operands.add(new Expression(operandBuilder.toString()));
            } else {
                operands.add(new Expression(new BigDecimal(operandBuilder.toString(), new MathContext(100))));
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Illegal operand: '" + operandBuilder.toString() + "'.");
        }
    }

    private boolean containsMultipleMinuses(char[] characterArray) {
        int numMinuses = 0;
        for (char character : characterArray)
        {
            if (character == '-')
            {
                numMinuses++;
                if (numMinuses > 1)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void evaluate() {
        if (! evaluated)
        {
            PriorityQueue<Operation> operationsToEvaluate = new PriorityQueue<Operation>(10, Collections.reverseOrder());
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
            evaluated = true;
        }
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
        if (! evaluated)
        {
            evaluate();
        }
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
