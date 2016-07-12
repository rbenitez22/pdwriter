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

/**
 *
 * @author Roberto C. Benitez
 */
public class PdPixels implements PdUnit
{
    private final float pixels;
    private final float points;
    
    public PdPixels(float pixels, float dpi)
    {
        if(dpi <=0)
        {
            throw new IllegalArgumentException("Invalid DPI argument");
        }
        
        this.pixels=pixels;
        points=pixels * PdUnit.POINTS_PER_INCH / dpi;
    }

    public float getPixels()
    {
        return pixels;
    }

    @Override
    public float getPoints()
    {
        return points;
    }
    
    @Override public String toString()
    {
        return Float.toString(points);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 11 * hash + Float.floatToIntBits(this.points);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final PdPixels other = (PdPixels) obj;
        if (Float.floatToIntBits(this.points) != Float.floatToIntBits(other.points))
        {
            return false;
        }
        return true;
    }
    
    @Override public int compareTo(PdUnit other)
    {
        if (other == null)
        {
            throw new NullPointerException("PDUnit argument is null");
        }

        return Float.compare(points, other.getPoints());
    }

}
