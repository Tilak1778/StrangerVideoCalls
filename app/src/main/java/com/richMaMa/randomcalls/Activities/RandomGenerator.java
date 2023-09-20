package com.richMaMa.randomcalls.Activities;

import java.util.Random;
public class RandomGenerator {
    private Random random;

    public RandomGenerator() {
        this.random = new Random();
    }

    public int generateRandomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Invalid range. Min must be less than or equal to Max.");
        }

        // Calculate the range of numbers (inclusive)
        int range = max - min + 1;

        // Generate a random number within the range and add min to it
        int randomInt = random.nextInt(range) + min;

        return randomInt;
    }
}
