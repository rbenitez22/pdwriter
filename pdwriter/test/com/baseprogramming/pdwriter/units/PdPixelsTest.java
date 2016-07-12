/*
 * Copyright 2016 Roberto C. Benitez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baseprogramming.pdwriter.units;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Roberto C. Benitez
 */
public class PdPixelsTest
{
    
    public PdPixelsTest()
    {
    }

    @Test
    public void testSomeMethod()
    {
        float pixels=96;
        float dpi=150;
        PdUnit units= new PdPixels(pixels,dpi);
        
        System.out.printf("DPI: %s, pixels: %s, Points: %s\n",dpi,pixels,units.getPoints());
    }
    
}
