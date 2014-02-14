package org.quuux.plasma;

import java.util.Random;

class RandomGenerator {
    private static final Random random = new Random(System.currentTimeMillis());
   
    public static double randomRange(double min, double max) {
        return min + ((max-min) * random.nextDouble());
    }

    public static double randomPercentile() {
        return randomRange(0, 1);
    }

    public static int randomInt(int min, int max) {
        return min + random.nextInt(max-min);
    }
}
