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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 *
 * @author Roberto C. Benitez
 */
public class PdTableHeader
{
    private List<PdColumn> columns;
    private PDType1Font font;
    private float fontSize;

    public PdTableHeader(List<PdColumn> column, PDType1Font font, float fontSize)
    {
        this.columns = column;
        this.font = font;
        this.fontSize = fontSize;
    }
    
    public PdColumn getColumn(String name)
    {
        Optional<PdColumn> col=columns.stream().filter(e->e.getName().equals(name))
                .findFirst();
        if(col.isPresent())
        {
            return col.get();
        }
        throw new IllegalArgumentException("No column found for '" + name  +  ";");
                
    }

    public void setFont(PDType1Font font)
    {
        this.font = font;
    }

    public void setFontSize(float fontSize)
    {
        this.fontSize = fontSize;
    }

    public void setColumns(List<PdColumn> columns)
    {
        this.columns = columns;
    }

    public List<PdColumn> getColumns()
    {
        return columns;
    }
    
    public int getColumnCount()
    {
        if(columns==null){return 0;}
        return columns.size();
    }

    public PDType1Font getFont()
    {
        return font;
    }

    public float getFontSize()
    {
        return fontSize;
    }
    
    public List<PdColumn> getColumnsWithUnsetWidth()
    {
        return columns.stream()
                .filter(e->e.getWidth()==null || e.getWidth().getPoints() <= 0)
                .collect(Collectors.toList());
    }
}
