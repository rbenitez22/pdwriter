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



/**
 *
 * @author Roberto C. Benitez
 */
public class Borders
{
    private final float top;
    private final float right;
    private final float bottom;
    private final float left;

    public Borders(float width)
    {
        top=width;
        right=width;
        bottom=width;
        left=width;        
    }

    public Borders(float top, float right, float bottom, float left)
    {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public float getTop()
    {
        return top;
    }

    public float getRight()
    {
        return right;
    }

    public float getBottom()
    {
        return bottom;
    }

    public float getLeft()
    {
        return left;
    }
    
    public boolean hasBorders()
    {
        return (top > 0 || right > 0 || bottom > 0 || left > 0);
    }
}
