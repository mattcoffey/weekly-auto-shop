package net.mattcoffey;

import java.io.IOException;

import net.mattcoffey.shopper.WeeklyShop;


/**
 * Entry point into application
 * 
 * @author mcoffey
 */
public class Main {
    
    /**
     * @param args are ignored
     * @throws IOException
     */
    public static void main(String...args) throws IOException {
        new WeeklyShop().doShop();
    }
}
