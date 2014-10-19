/*
 * Copyright (c) 2014 PEER 1 Network Enterprises, Inc. 
 * All rights reserved. 
 * No part of this program may be reproduced, translated or transmitted, 
 * in any form or by any means, electronic, mechanical, photocopying, 
 * recording or otherwise, or stored in any retrieval system of any nature, 
 * without written permission of the copyright holder.
 */

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
