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
package com.baseprogramming.pdwriter.html;

import com.baseprogramming.pdwriter.Utils;
import com.baseprogramming.pdwriter.model.Borders;
import com.baseprogramming.pdwriter.model.PageMetadata;
import com.baseprogramming.pdwriter.model.PdParagraph;
import com.baseprogramming.pdwriter.units.PdPoints;
import com.baseprogramming.pdwriter.units.PdUnit;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.w3c.dom.css.CSSValue;


/**
 *
 * @author Roberto C. Benitez
 */
public class HtmlStyle extends PdParagraph
{
    private final float dpi;
    private static Map<String,PDType1Font> fontMap;
    private HtmlStyle parent;
    private PdUnit blockWidth;

    public HtmlStyle(PageMetadata page,Map<String,CSSValue> style,float dpi)
    {
        super(page);
        parent=null;
        this.dpi=dpi;
        if(fontMap==null)
        {
            createFontMap();
        }
        
        setupFromStyleMap(style);
    }
    
    public HtmlStyle(HtmlStyle parent,Map<String,CSSValue> style,float dpi)
    {
        this(parent.getPage(),style,dpi);
        this.parent=parent;
    }
    
    private void createFontMap()
    {
        fontMap= new HashMap <>();
        fontMap.put("TIMES NEW ROMAN", PDType1Font.TIMES_ROMAN);
        fontMap.put("TIMES NEW ROMAN_BOLD", PDType1Font.TIMES_BOLD);
        fontMap.put("TIMES NEW ROMAN_ITALIC", PDType1Font.TIMES_ITALIC);
        fontMap.put("TIMES NEW ROMAN_OBLIQUE", PDType1Font.TIMES_ITALIC);
        fontMap.put("TIMES NEW ROMAN_BOLD_ITALIC", PDType1Font.TIMES_BOLD_ITALIC);
        
        fontMap.put("COURIER", PDType1Font.COURIER);
        fontMap.put("COURIER_BOLD", PDType1Font.COURIER_BOLD);
        fontMap.put("COURIER_ITALIC", PDType1Font.COURIER_OBLIQUE);
        fontMap.put("COURIER_OBLIQUE", PDType1Font.COURIER_OBLIQUE);
        fontMap.put("COURIER_BOLD_ITALIC", PDType1Font.COURIER_BOLD_OBLIQUE);
        
        fontMap.put("HELVATICA", PDType1Font.TIMES_ROMAN);
        fontMap.put("HELVATICA_ITALIC", PDType1Font.HELVETICA_BOLD);
        fontMap.put("HELVATICA_OBLIQUE", PDType1Font.HELVETICA_BOLD);
        fontMap.put("HELVATICA_BOLD_ITALIC", PDType1Font.HELVETICA_OBLIQUE);
        fontMap.put("HELVATICA_BOLD_ITALIC", PDType1Font.HELVETICA_BOLD_OBLIQUE);
    }
    
    private void  setupFromStyleMap(Map<String,CSSValue> style)
    {
       setupFont(style); 
       setupBorders(style);
       setupPadding(style);
       blockWidth=parseDimension(style, "width", new PdPoints(0));
    }
    
    private void setupFont(Map<String,CSSValue> style)
    {
       String fontColor=getStyleValue(style,"color", "black");
       PdUnit fontSize=parseDimension(style, "font-size", new PdPoints(12));
       String fontStyle=getStyleValue(style,"font-style", "normal");
       String fontWeight=getStyleValue(style,"font-weight", "normal");
       String fontFamily=getStyleValue(style,"font-family", "Times New Roman");
       
       PDType1Font font=getPDType1Font(fontFamily, fontStyle, fontWeight);
       setFont(font);

       Color color=getColor(fontColor);
       setFontColor(color);
       
        setFontSize(fontSize.getPoints());
    }
    
    private String getStyleValue(Map<String,CSSValue> style,String name,String defaultValue)
    {
        CSSValue value=style.get(name);
        if(value==null)
        {
            return defaultValue;
        }

        return value.getCssText();
    }
    
    public Color getColor(String colorString)
    {
        if(colorString==null || colorString.isEmpty()){return Color.BLACK;}
        
        if(colorString.startsWith("#")){return HtmlColor.getColor(colorString);}
        
        
        if(colorString.startsWith("rgb("))
        {
            String string=colorString.substring(4, colorString.length()-1);
            String[] tokens=string.split(",");
            int red=Integer.valueOf(tokens[0].trim());
            int green=Integer.valueOf(tokens[1].trim());
            int blue=Integer.valueOf(tokens[2].trim());
            
            return new Color(red, green, blue);
        }
        
        try
        {
            return HtmlColor.valueOf(colorString.toUpperCase()).getColor();
        }
        catch(Exception e)
        {
            return Color.BLACK;
        }
    }
    
    private PDType1Font getPDType1Font(String htmlFamily,String style,String weight)
    {
        String key=htmlFamily;
        
         if(!(weight==null || "normal".equals(weight)))
        {
            key+="_"+weight;
        }
        
        if(!(style==null || "normal".equals(style)))
        {
            key+="_"+style;
        }
        
        return fontMap.getOrDefault(key.toUpperCase(), PDType1Font.TIMES_ROMAN);
       
    }

    private void setupBorders(Map<String,CSSValue> style)
    {
        PdUnit def= new PdPoints(0);
        PdUnit all=parseDimension(style, "border", def);
        
        float top=parseDimension(style, "border-top", all).getPoints();
        float right=parseDimension(style, "border-right", all).getPoints();
        float bottom=parseDimension(style, "border-bottom", all).getPoints();
        float left=parseDimension(style, "border-left", all).getPoints();
        
        Borders borders= new Borders(top, right, bottom, left);
        setBorder(borders);
    }
    
    private void setupPadding(Map<String,CSSValue> style)
    {
        PdUnit def= new PdPoints(0);
        PdUnit all=parseDimension(style, "padding", def);
        
        PdUnit top=parseDimension(style, "padding-top", all);
        PdUnit right=parseDimension(style, "padding-right", all);
        PdUnit bottom=parseDimension(style, "padding-bottom", all);
        PdUnit left=parseDimension(style, "padding-left", all);
        
        setAboveSpacing(top);
        setBelowSpacing(bottom);
        setBeforeTextIndent(left);
        setAfterTextIndent(right);
        
    }
    
    private PdUnit parseDimension(Map<String,CSSValue> style, String name,PdUnit defaultValue)
    {
        if(!style.containsKey(name)){return defaultValue;}
        String input=style.get(name).getCssText();
        if(input==null || input.isEmpty()){return defaultValue;}
        
        if(input.length()<3)
        {
            return new PdPoints(Float.valueOf(input));
        }
        
        
        PdUnit unit=Utils.parseDimension(input,getFontSize(),dpi);
        
        return unit;
        
    }

    public HtmlStyle getParent()
    {
        return parent;
    }

    public void setParent(HtmlStyle parent)
    {
        this.parent = parent;
    }

    @Override public float getWidth()
    {
        if(blockWidth==null || blockWidth.getPoints() <=0)
        {
            return super.getWidth();
        }
        
        return blockWidth.getPoints();
    }
    
    public PdUnit getBlockWidth()
    {
        return blockWidth;
    }

    public void setBlockWidth(PdUnit blockWidth)
    {
        this.blockWidth = blockWidth;
    }
    
}
