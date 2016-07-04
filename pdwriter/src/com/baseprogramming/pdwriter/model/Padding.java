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
package com.baseprogramming.pdwriter.model;

import com.baseprogramming.pdwriter.units.PdInch;
import com.baseprogramming.pdwriter.units.PdUnit;

/**
 *
 * @author Roberto C. Benitez
 */
public class Padding
{
    private final PdUnit top;
    private final PdUnit right;
    private final PdUnit bottom;
    private final PdUnit left;

    public Padding(PdUnit margins)
    {
        top=margins;
        right=margins;
        bottom=margins;
        left=margins;
    }
    
     public Padding(float inInches)
    {
        top=new PdInch(inInches);
        right=new PdInch(inInches);
        bottom=new PdInch(inInches);
        left=new PdInch(inInches);
    }

    public Padding(PdUnit top, PdUnit right, PdUnit bottom, PdUnit left)
    {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }
    
    public Padding(float topInInches, float rightInInches, float bottomInInches, float leftInInches)
    {
        top= new PdInch(topInInches);
        right= new PdInch(rightInInches);
        bottom= new PdInch(bottomInInches);
        left= new PdInch(leftInInches);
    }
    
    public PdUnit getTop()
    {
        return top;
    }

    public PdUnit getRight()
    {
        return right;
    }

    public PdUnit getBottom()
    {
        return bottom;
    }

    public PdUnit getLeft()
    {
        return left;
    }
}
