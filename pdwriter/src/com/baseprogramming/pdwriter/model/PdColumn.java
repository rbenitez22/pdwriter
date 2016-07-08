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

import com.baseprogramming.pdwriter.units.PdUnit;

/**
 *
 * @author Roberto C. Benitez
 */
public class PdColumn
{
    private final String name;
    private final String label;
    private PdUnit width;
    private PdParagraph style;

    public PdColumn(String label, PdUnit width)
    {
        this.name=label;
        this.label = label;
        this.width = width;
    }

    public PdColumn(String name, String label, PdUnit width)
    {
        this.name = name;
        this.label = label;
        this.width = width;
    }

    public String getName()
    {
        return name;
    }

    public String getLabel()
    {
        return label;
    }

    public void setWidth(PdUnit width)
    {
        this.width = width;
    }

    public PdUnit getWidth()
    {
        return width;
    }

    public PdParagraph getStyle()
    {
        return style;
    }

    public void setStyle(PdParagraph style)
    {
        this.style = style;
    }
    
}
