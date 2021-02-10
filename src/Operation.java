public class Operation implements Comparable<Operation>{

    private final OperationType type;
    private final int position; // This is how far to the right this operation is.

    public Operation(char operationCharacter, int operationPosition)
    {
        position = operationPosition;
        switch (operationCharacter) {
            case '*':
                type = OperationType.MULTIPLY;
                break;
            case '/':
                type = OperationType.DIVIDE;
                break;
            case '+':
                type = OperationType.ADD;
                break;
            case '-':
                type = OperationType.SUBTRACT;
                break;
            case '^':
                type = OperationType.POWER;
                break;
            default:
                throw new IllegalArgumentException("Cannot create operation out of character '" + operationCharacter + "'.");
        }
    }

    public OperationType getType()
    {
        return type;
    }

    public String toString()
    {
        String returningString;
        switch (type) {
            case MULTIPLY:
                returningString = "*";
                break;
            case ADD:
                returningString = "+";
                break;
            case SUBTRACT:
                returningString = "-";
                break;
            case DIVIDE:
                returningString = "/";
                break;
            case POWER:
                returningString = "^";
                break;
            default:
                throw new RuntimeException("Somehow an operation was created with an illegal type: " + type);
        }
        return returningString;
    }

    @Override
    public int compareTo(Operation otherOperation) {
        if (getType() == OperationType.POWER)
        {
            if (otherOperation.getType() == OperationType.POWER)
            {
                if (position < otherOperation.position)
                {
                    return 1;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                return 1;
            }
        }
        else if (getType() == OperationType.DIVIDE || getType() == OperationType.MULTIPLY)
        {
            if (otherOperation.getType() == OperationType.POWER)
            {
                return -1;
            }
            else if (otherOperation.getType() == OperationType.DIVIDE || otherOperation.getType() == OperationType.MULTIPLY)
            {
                if (position < otherOperation.position)
                {
                    return 1;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                return 1;
            }
        }
        else
        {
            if (otherOperation.getType() == OperationType.ADD || otherOperation.getType() == OperationType.SUBTRACT)
            {
                if (position < otherOperation.position)
                {
                    return 1;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                return -1;
            }
        }
    }
}

enum OperationType {
    MULTIPLY, DIVIDE, SUBTRACT, ADD, POWER
}