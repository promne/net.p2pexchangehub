package net.p2pexchangehub.core.api._domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class CurrencyAmountTest {

    @Test    
    public void testHashAndEqualsTheSame() {
        CurrencyAmount a1 = new CurrencyAmount("currencyCode", new BigDecimal("18.3")); 
        CurrencyAmount a2 = new CurrencyAmount("currencyCode", new BigDecimal("18.300")); 

        assertEquals(a1.hashCode(), a1.hashCode());
        assertEquals(a1.hashCode(), a2.hashCode());
        assertEquals(a2.hashCode(), a2.hashCode());
        
        assertEquals(a1, a1);
        assertEquals(a1, a2);
        assertEquals(a2, a2);
        
        CurrencyAmount s1 = new CurrencyAmount("currencyCode2", new BigDecimal("18.3")); 
        CurrencyAmount s2 = new CurrencyAmount("currencyCode2", new BigDecimal("18.300"));
        
        assertEquals(s1.hashCode(), s1.hashCode());
        assertEquals(s1.hashCode(), s2.hashCode());
        assertEquals(s2.hashCode(), s2.hashCode());
        
        assertEquals(s1, s1);
        assertEquals(s1, s2);
        assertEquals(s2, s2);
    }
    
    @Test    
    public void testHashAndEqualsDifferent() {
        CurrencyAmount a1 = new CurrencyAmount("currencyCode", new BigDecimal("18.3"));         
        CurrencyAmount a2 = new CurrencyAmount("currencyCode", new BigDecimal("18.4")); 
        CurrencyAmount a3 = new CurrencyAmount("currencyCode2", new BigDecimal("18.3")); 
        
        assertNotEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1.hashCode(), a3.hashCode());
        assertNotEquals(a2.hashCode(), a3.hashCode());
        
        assertNotEquals(a1, a2);
        assertNotEquals(a1, a3);
        assertNotEquals(a2, a3);        
    }

    @Test    
    public void testHashSameAndEqualsDifferent() {
        CurrencyAmount a1 = new CurrencyAmount("currencyCode", new BigDecimal("18.0000000000000001111111111"));         
        CurrencyAmount a2 = new CurrencyAmount("currencyCode", new BigDecimal("18.0000000000000009999999999")); 

        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1, a2);
    }
   
    
    public static void main(String[] args) {
        String text = "2WR2AOJBa";
        
        Set<String> m = new HashSet<>();
        final int codeLength = 8;
        for (int i=0; i<=text.length()-codeLength; i++) {
            String s = text.substring(i, i+codeLength).replaceAll("\\s", "").trim().toUpperCase();
            if (s.length()==codeLength) {
                System.out.println(s);
//                m.add(s);                
            }
        }

        m.forEach(System.out::println);
    }
}
