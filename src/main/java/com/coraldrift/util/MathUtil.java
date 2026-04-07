package com.coraldrift.util;

import java.util.Random;

/**
 * Math utilities for game calculations.
 */
public final class MathUtil {
    
    private static final Random random = new Random();
    
    private MathUtil() {}
    
    /**
     * Linear interpolation between two values.
     */
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * clamp(t, 0, 1);
    }
    
    /**
     * Clamp a value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Map a value from one range to another.
     */
    public static double map(double value, double inMin, double inMax, double outMin, double outMax) {
        return outMin + (outMax - outMin) * ((value - inMin) / (inMax - inMin));
    }
    
    /**
     * Random double in range [min, max).
     */
    public static double randomRange(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }
    
    /**
     * Random integer in range [min, max].
     */
    public static int randomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
    
    /**
     * Random boolean with given probability of true.
     */
    public static boolean randomChance(double probability) {
        return random.nextDouble() < probability;
    }
    
    /**
     * Smooth step interpolation (ease in/out).
     */
    public static double smoothStep(double t) {
        t = clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }
    
    /**
     * Ease out cubic for snappy animations.
     */
    public static double easeOutCubic(double t) {
        return 1 - Math.pow(1 - t, 3);
    }
    
    /**
     * Ease in cubic.
     */
    public static double easeInCubic(double t) {
        return t * t * t;
    }
    
    /**
     * Ease out back (overshoot effect).
     */
    public static double easeOutBack(double t) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }
    
    /**
     * Elastic bounce effect.
     */
    public static double easeOutElastic(double t) {
        if (t == 0 || t == 1) return t;
        double p = 0.3;
        return Math.pow(2, -10 * t) * Math.sin((t - p / 4) * (2 * Math.PI) / p) + 1;
    }
    
    /**
     * Calculate distance between two points.
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Oscillate between 0 and 1 using sine wave.
     */
    public static double oscillate(double time, double frequency) {
        return (Math.sin(time * frequency * 2 * Math.PI) + 1) / 2;
    }
    
    /**
     * Get a random element from an array.
     */
    public static <T> T randomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }
}
