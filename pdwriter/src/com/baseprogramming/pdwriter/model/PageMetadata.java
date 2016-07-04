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

import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 *
 * @author Roberto C. Benitez
 */
public class PageMetadata
{
    private final PDRectangle pageFormat;
    private final Margin margin;

    public PageMetadata(PDRectangle pageFormat, Margin margin)
    {
        this.pageFormat = pageFormat;
        this.margin = margin;
    }

    public PageMetadata(Margin margin)
    {
        this.margin = margin;
        this.pageFormat=PDRectangle.LETTER;
    }

    public PDRectangle getPageFormat()
    {
        return pageFormat;
    }

    public Margin getMargin()
    {
        return margin;
    }
    
    public float getWidth()
    {
        return pageFormat.getWidth() - (margin.getLeft().getPoints() + margin.getRight().getPoints());
    }
    
    public float getHeight()
    {
        return pageFormat.getHeight() - (margin.getTop().getPoints() + margin.getBottom().getPoints());
    }
    
    public float getLowerLeftX()
    {
        return pageFormat.getLowerLeftX() + margin.getLeft().getPoints();
    }
    
    public float getLowerLeftY()
    {
        return pageFormat.getLowerLeftY()  + margin.getBottom().getPoints();
    }
    
    public float getUpperRightX()
    {
        return pageFormat.getUpperRightX() - margin.getRight().getPoints();
    }
    
    public float getUpperRightY()
    {
        return pageFormat.getUpperRightY() - margin.getTop().getPoints();
    }
}
