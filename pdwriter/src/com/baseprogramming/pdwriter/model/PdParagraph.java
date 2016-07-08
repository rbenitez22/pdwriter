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
import java.awt.Color;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


/**
 *
 * @author Roberto C. Benitez
 */
public class PdParagraph
{
    private final PageMetadata page;
    private Borders border;
    private PdUnit beforeTextIndent;
    private PdUnit afterTextIndent;
    private PdUnit firstLineIndent;
    
    private PdUnit aboveSpacing;
    private PdUnit belowSpacing;
    
    private float lineSpacing=1;
    private PdUnit blockWidth;
    private PDType1Font font = PDType1Font.TIMES_ROMAN;
    private float fontSize=12;
    private Color fontColor=Color.BLACK;

    public PdParagraph(PageMetadata page)
    {
        this.page = page;
        border = new Borders(0f);
        initIdentsAndSpacing();
    }
    
    private void initIdentsAndSpacing()
    {
        beforeTextIndent = new PdInch(0);
        afterTextIndent= new PdInch(0);
        firstLineIndent =  new PdInch(0);
        aboveSpacing= new PdInch(0);
        belowSpacing= new PdInch(0);
    }

    public Borders getBorder()
    {
        return border;
    }

    public void setBorder(Borders border)
    {
        this.border = border;
    }

    public PDType1Font getFont()
    {
        return font;
    }

    public void setFont(PDType1Font font)
    {
        this.font = font;
    }

    public float getFontSize()
    {
        return fontSize;
    }

    public void setFontSize(float fontSize)
    {
        this.fontSize = fontSize;
    }

    public Color getFontColor()
    {
        return fontColor;
    }

    public void setFontColor(Color fontColor)
    {
        this.fontColor = fontColor;
    }

    public PdUnit getBeforeTextIndent()
    {
        return beforeTextIndent;
    }

    public void setBeforeTextIndent(PdUnit units)
    {
        this.beforeTextIndent = units;
    }

    public PdUnit getAfterTextIndent()
    {
        return afterTextIndent;
    }

    public void setAfterTextIndent(PdUnit units)
    {
        this.afterTextIndent = units;
    }

    public PdUnit getFirstLineIndent()
    {
        return firstLineIndent;
    }

    public void setFirstLineIndent(PdUnit units)
    {
        this.firstLineIndent = units;
    }

    public PdUnit getAboveSpacing()
    {
        return aboveSpacing;
    }

    public void setAboveSpacing(PdUnit units)
    {
        this.aboveSpacing = units;
    }

    public PdUnit getBelowSpacing()
    {
        return belowSpacing;
    }

    public void setBelowSpacing(PdUnit units)
    {
        this.belowSpacing = units;
    }

    public float getLineSpacing()
    {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing)
    {
        this.lineSpacing = lineSpacing;
    }

    public PageMetadata getPage()
    {
        return page;
    }
    
    public float getLeftX()
    {
        return getLeftX(false);
    }
    
    public float getLeftX(boolean firstLine)
    {
        float pos = page.getLowerLeftX() + beforeTextIndent.getPoints();
        
        if(firstLine)
        {
            pos+=firstLineIndent.getPoints();
        }
        
        return pos;
    }
    
    public float getRightX()
    {
        float pos=page.getUpperRightX() - afterTextIndent.getPoints();
        return pos;
    }
    
    public float getWidth()
    {
        return getWidth(false);
    }
    
    public float getWidth(boolean firstLine)
    {
        return getRightX() - getLeftX(firstLine);
    }
    
    public float getUpperY(float offset)
    {
        float pos = offset - aboveSpacing.getPoints() ;
        if(offset < 1)
        {
            pos=page.getUpperRightY();
        }
        
        return pos;
    }
    
    public float getTextHeight(int lineCount)
    {
        return getLineHeight() * lineCount;
    }
   
    public float getNextY(float current)
    {
        return current-=getLineHeight();
    } 

    public float getLineHeight()
    {
        return fontSize * lineSpacing;
    }
    
    public int getWrapPosition(String string,int start,boolean firstLine) throws IOException
    {
        float lineWidth=getWidth(firstLine);
        int pos=getWrapPosition(start, lineWidth, string);
        
        return pos;
    }

    public int getWrapPosition(int start, float lineWidth, String string) throws IOException
    {
        return getWrapPositionFromOffset(start, lineWidth, 0, string);
    }
    
    public int getWrapPositionFromOffset(int start, float xPositionOffet,String string) throws IOException
    {
        float lineWidth=getWidth(false);
        return getWrapPositionFromOffset(start, lineWidth, xPositionOffet, string);
    }
    
    public int getWrapPositionFromOffset(int start,float lineWidth, float xPositionOffet,String string) throws IOException
    {
        int pos=start;
        float stringWidth=0;
        float widthBuffer=fontSize / 4;
        float availableWidth=lineWidth-xPositionOffet;
        while(stringWidth<(availableWidth-widthBuffer) && pos < string.length())
        {
            pos++;
            stringWidth=getStringWidth(string.substring(start, pos));
        }
        if(pos > start && stringWidth >=(availableWidth-widthBuffer))
        {
            //get position of previous space (start of last word)
            int tmp=string.lastIndexOf(' ', pos); 
            if(tmp < pos && tmp> start)
            {
                pos=tmp;
            }
        }
        
        if(pos>string.length()){return string.length();}
        
        return pos;
    }
    
    public float getStringWidth(String string) throws IOException
    {
        return (font.getStringWidth(string) / 1000 * fontSize);
    }
    
    public void copyTo(PdParagraph target)
    {
        target.setAboveSpacing(getAboveSpacing());
        target.setBelowSpacing(getBelowSpacing());
        target.setBeforeTextIndent(getBeforeTextIndent());
        target.setAfterTextIndent(getAfterTextIndent());
        target.setFont(getFont());
        target.setFontSize(getFontSize());
        target.setFontColor(getFontColor());
        target.setLineSpacing(getLineSpacing());
        target.setBorder(getBorder());
        
        
    }
}
