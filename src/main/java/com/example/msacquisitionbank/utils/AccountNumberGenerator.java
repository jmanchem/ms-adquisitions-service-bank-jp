package com.example.msacquisitionbank.utils;

import java.util.Random;

/**
 * Peruvian generator account number
 * Author Orlando Kuan Becerra
 */
public class AccountNumberGenerator {
    private Random random = new Random(System.currentTimeMillis());

    /**
     *
     * @param length The total length of the account number.
     * @return  A randomly generated, account number.
     */
    public String generate(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int n = this.random.nextInt(10);
            if (i == 3){
                builder.append("-");
            }else if (i == 10){
                builder.append("-");
            }else if (i == 12){
                builder.append("-");
            }else {
                builder.append(Integer.toString(n));
            }
        }
        return builder.toString();
    }
}
