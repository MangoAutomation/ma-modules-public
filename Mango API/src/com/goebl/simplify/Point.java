package com.goebl.simplify;

/**
 * Access to X and Y coordinates (2D-Point).
 *
 * @author hgoebl
 * @since 06.07.13
 */
public interface Point extends Comparable<Point> {
    
    /**
     * Return the timestamp
     * @return
     */
    double getX();
    
    /**
     * Return the value
     * @return
     */
    double getY();
    
    /**
     * Is the value Process-able i.e. a real double?
     * @return
     */
    boolean isProcessable();
}
