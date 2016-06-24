package george.test.exchange.core.processing;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import org.junit.Test;

public class RatesTest {

    @Test
    public void toStringBD() throws Exception {
        String format = "%#f";
        Locale locale = new Locale("cs");
        for (BigDecimal v : new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("1.00"), new BigDecimal("1.12"), new BigDecimal("1.123"), new BigDecimal("12345.123")}) {
            NumberFormat instance = NumberFormat.getInstance(locale);
            instance.setGroupingUsed(false);
            System.out.println(instance.format(v));
            System.out.println(String.format(locale, format, v));
        }
    }
    
    @Test
    public void testSome() {
        BigDecimal rate = new BigDecimal("10.1234");
        
//        int precisionA = 2;
        int precisionB = 2;
        
        
        BigDecimal firstGetsB = new BigDecimal("10"); 
        
        
        BigDecimal firstPayAToTrust = firstGetsB.multiply(rate).setScale(precisionB, BigDecimal.ROUND_UP);       
        BigDecimal secondPayBToTrust = firstGetsB;
        BigDecimal secondGetsA = firstPayAToTrust;
        
        System.out.println("Rate: " + rate);
        System.out.println("First gets as "
                + "requested B: " + firstGetsB.toPlainString());
        System.out.println("First pays A to trust: " + firstPayAToTrust.toPlainString());
        System.out.println("Second pays B to trust: " + secondPayBToTrust.toPlainString());
        System.out.println("Second gets A: " + secondGetsA.toPlainString());
        System.out.println("Trust balance A: " + (firstPayAToTrust.subtract(secondGetsA)).toPlainString());
        System.out.println("Trust balance B: " + (secondPayBToTrust.subtract(firstGetsB)).toPlainString());
    }
}
