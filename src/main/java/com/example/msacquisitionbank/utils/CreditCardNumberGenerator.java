package com.example.msacquisitionbank.utils;

import java.util.Random;

/**
 * Peruvian credit card number
 * Author Orlando Kuan Becerra
 */
public class CreditCardNumberGenerator {
    private Random random = new Random(System.currentTimeMillis());

    /**
     * @param bin
     *            The bank identification number, a set digits at the start of the credit card
     *            number, used to identify the bank that is issuing the credit card.
     * @param length
     *            The total length (i.e. including the BIN) of the credit card number.
     * @return
     *            A randomly generated, valid, credit card number.
     */
    public String generate(String bin, int length) {
        int randomNumberLength = length - (bin.length() + 1);

        StringBuilder builder = new StringBuilder(bin);
        for (int i = 0; i < randomNumberLength; i++) {
            int digit = this.random.nextInt(10);
            if(i % 4 == 0) builder.append("-");
            builder.append(digit);
        }
        return builder.toString();
    }

}