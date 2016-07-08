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


public class PdList extends PdParagraph
{
    private final ValueProvider itemLabel;
    private final String labelBodyDelimiter;
    
    private PdList(PageMetadata page,ValueProvider itemLabelProvider)
    {
        this(itemLabelProvider, ". ", page);
    }

    public PdList(ValueProvider labelProvider, String delimiter, PageMetadata page)
    {
        super(page);
        this.itemLabel = labelProvider;
        this.labelBodyDelimiter = delimiter;
        setFirstLineIndent(new PdInch(-0.10f));
        setAboveSpacing(new PdInch(0.3f));
    }

    @Override public final void setAboveSpacing(PdUnit units)
    {
        super.setAboveSpacing(units); 
    }

    @Override public final void setFirstLineIndent(PdUnit units)
    {
        super.setFirstLineIndent(units);
    }

    public ValueProvider getItemLabel()
    {
        return itemLabel;
    }

    public String getLabelBodyDelimiter()
    {
        return labelBodyDelimiter;
    }
    
    public String createFullItemString(String itemText)
    {
        return String.format("%s%s%s",itemLabel.getValue(),labelBodyDelimiter,itemText);
    }
    
    public PdParagraph createItemStyle()
    {
        PdParagraph par= new PdParagraph(getPage());
        par.setAboveSpacing(new PdInch(0));
        par.setBelowSpacing(new PdInch(0));
        par.setAfterTextIndent(getAfterTextIndent());
        par.setFont(getFont());
        par.setFontSize(getFontSize());
        par.setFontColor(getFontColor());
        
        return par;
    }

    public static PdList bulletList(PageMetadata page)
    {
        ValueProvider<Character> bullet= new StaticContentProvider<>('*');
        
        PdList list = new PdList(bullet, "", page);
        return list;
    }
    
    public static PdList numeredList(PageMetadata page)
    {
        ValueProvider<Integer> seq= new SequenceValueProvider();
        return new PdList(page, seq);
    }
    
}
