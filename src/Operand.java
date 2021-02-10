//import java.math.BigDecimal;
//
//public class Operand extends ExpressionElement{
//    private final BigDecimal value;
//
//    public Operand(String value)
//    {
//        try
//        {
//            this.value = new BigDecimal(value);
//        }
//        catch (NumberFormatException nfe)
//        {
//            throw new NumberFormatException("'" + value + "' cannot be intepreted as a number.");
//        }
//    }
//
//    public BigDecimal getValue()
//    {
//        return new BigDecimal(value.toString());
//    }
//
//    public String toString()
//    {
//        return value.toString();
//    }
//}
