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
package com.baseprogramming.pdwriter;


import com.baseprogramming.pdwriter.model.Margin;
import com.baseprogramming.pdwriter.model.PageMetadata;
import com.baseprogramming.pdwriter.model.PdColumn;
import com.baseprogramming.pdwriter.model.PdList;
import com.baseprogramming.pdwriter.model.PdParagraph;
import com.baseprogramming.pdwriter.model.PdTable;
import com.baseprogramming.pdwriter.model.ValueProvider;
import com.baseprogramming.pdwriter.units.PdPoints;
import com.baseprogramming.pdwriter.units.PdUnit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 *
 * @author Roberto C. Benitez
 */
public class PdWriter
{
    private final PageMetadata meta;
    private final PDDocument document;
    private PDPage currentPage;
  
    private float yPosition;

    public PdWriter(PDDocument document, Margin margin)
    {
        this(new PageMetadata(PDRectangle.LETTER, margin), document);
    }

    public PdWriter(PageMetadata meta, PDDocument document)
    {
        this.meta = meta;
        this.document = document;
        yPosition = meta.getUpperRightY();
    }
    
    public PdTable createTable()
    {
        return new PdTable(meta);
    }
    
    public PdTable createTable(String ... columnNames)
    {
       return new PdTable(meta, columnNames);
    }
    
    public PdTable createTable(List<PdColumn> columns)
    {
        return new PdTable(meta, columns);
    }
    
    public PdParagraph createParagraph()
    {
        return new PdParagraph(meta);
    }
    
    public PdList createBulletPdList()
    {
        return PdList.bulletList(meta);
    }
    
    public PdList createNumberedPdList()
    {
        return PdList.numeredList(meta);
    }
        

    public PageMetadata getMeta()
    {
        return meta;
    }

    public PDPage getCurrentPage()
    {
        return currentPage;
    }

    public void setCurrentPage(PDPage currentPage)
    {
        this.currentPage = currentPage;
    }

    public float getLastYPosition()
    {
        return yPosition;
    }
    
    public void setLastYPosition(float position)
    {
        yPosition=position;
    }
    
    public void increaseYPosition(PdUnit amount)
    {
        increaseYPosition(amount.getPoints());
    }
    
    public void increaseYPosition(float amount)
    {
        yPosition+=amount;
    }
    
    public void decreaseYPosition(PdUnit amount)
    {
        decreaseYPosition(amount.getPoints());
    }
    
    public void decreaseYPosition(float amount)
    {
        yPosition -=amount;
    }

    public PDDocument getDocument()
    {
        return document;
    }
    
     public void writeHtml(String html) throws IOException
     {
         HtmlPdWriter tmp= new HtmlPdWriter(this);
         tmp.write(html);
     }
     
     public void writeHtml(File htmlSourceFile,String rootElement) throws IOException
    {
         HtmlPdWriter tmp= new HtmlPdWriter(this);
         tmp.write(htmlSourceFile, rootElement);
    }
     
     public void writeHtml(File htmlSourceFile) throws IOException
     {
         HtmlPdWriter tmp= new HtmlPdWriter(this);
         tmp.write(htmlSourceFile);
     }
    
    public void write(PdTable table, List<Map<String,Object>> data) throws IOException
    {
        PdTableWriter writer= new PdTableWriter(this,table);
        writer.write(data);
    }

    public boolean causesPageOverflow(float height)
    {
        return ((yPosition - height) < meta.getLowerLeftY());
    }
    
    public List<String> wrapText(PdParagraph paragraph,String string,float width) 
            throws IOException
    {
        if(string==null || string.isEmpty()){return Collections.EMPTY_LIST;}
        List<String> wrapped=new ArrayList<>();
        
        int lastPos=0;
        while(true)
        {
            int start=lastPos;
            int end=paragraph.getWrapPosition(start, width, string);
            String line=string.substring(start, end);
            wrapped.add(line);
            lastPos=end;
            if(lastPos>=string.length()){break;}
        }
        
        return wrapped;
    }

    public void drawVerticalLine(float lineWidth, float x, float y1, float y2) throws IOException
    {
        try(PDPageContentStream stream = createStream())
        {
            drawVerticalLine(stream, lineWidth, x, y1, y2);
            stream.stroke();
        }
    }
    
    public void drawVerticalLine(PDPageContentStream stream, float lineWidth, float x, float y1, float y2) throws IOException
    {
        stream.setLineWidth(lineWidth);
        stream.moveTo(x, y1);
        stream.lineTo(x, y2);
    }

    public void drawHorizontalLine(float lineWidth, float x1, float y, float x2) throws IOException
    {
        try(PDPageContentStream stream = createStream())
        {
            drawHorizontalLine(stream,lineWidth, x1, y, x2);
            stream.stroke();
        }
    }
    
    public void drawHorizontalLine(PDPageContentStream stream, float lineWidth, float x1, float y, float x2) throws IOException
    {
        stream.setLineWidth(lineWidth);
        stream.moveTo(x1, y);
        stream.lineTo(x2, y);
    }

    public void write(PdList list,String ...items) throws IOException
    {
        write(list,Arrays.asList(items));
    }
    
    public void write(PdList list, List<String> content) throws IOException
    {
        ValueProvider prov=list.getItemLabel();
        PdParagraph itemStyle=list.createItemStyle();
        
        yPosition=list.getUpperY(yPosition);
        
        for(String body : content)
        {
            String label=prov.getValue().toString() + list.getLabelBodyDelimiter();
            
            float indent=list.getStringWidth(label);
            itemStyle.setBeforeTextIndent(new PdPoints(indent));
            itemStyle.setFirstLineIndent(new PdPoints(-(indent)));
            write(itemStyle,label + body);
        }
        
        yPosition-=list.getBelowSpacing().getPoints();
    }
    
    public void write(String content) throws IOException
    {
        PdParagraph par = new PdParagraph(meta);
        write(par, content);
    }
    
     /**
     * Write the content
     * @param paragraph
     * @param content content to write
     * @throws java.io.IOException
     */

    public void write(PdParagraph paragraph,String content) throws IOException
    {
        if(isAtEndOfPage())
        {
            createNewPage();
        }
        
        PDPageContentStream stream=createStream();
        try
        {
            stream.setFont(paragraph.getFont(), paragraph.getFontSize());
            stream.setNonStrokingColor(paragraph.getFontColor());
            yPosition=paragraph.getUpperY(yPosition);
            int lastPos=0;
            boolean firstLine=true;
            while(true)
            {
                int start=lastPos;
                boolean foundLineFeed=true;
                int end=content.indexOf("\n", start);
                if(end<0)
                {
                    end= paragraph.getWrapPosition(content, start,firstLine);
                    foundLineFeed=false;
                }
                
                if(end > content.length()){end=content.length();}

                String string=content.substring(start, end);
                float xPosition=paragraph.getLeftX(firstLine);
                writeText(stream, xPosition, yPosition, string);
                firstLine=false;
                lastPos=end;
                if(foundLineFeed){lastPos++;}
                yPosition=paragraph.getNextY(yPosition);
                
                if(end>=content.length()){break;}
                
                if(isAtEndOfPage())
                {
                    stream = createNewPageAndContentStream(stream, paragraph);
                }
            }
        }
        finally
        {
            yPosition -= (paragraph.getLineSpacing() + paragraph.getBelowSpacing().getPoints());
            if(stream!=null)
            {
                stream.close();
            }
        }
    }

    public boolean isAtEndOfPage()
    {
        return yPosition <=meta.getLowerLeftY();
    }

    protected PDPageContentStream createNewPageAndContentStream(PDPageContentStream stream, PdParagraph paragraph) throws IOException
    {
        createNewPage();
        stream.close();
        stream=createStream(paragraph);
        
        return stream;
    }

    protected void createNewPage()
    {
        currentPage = new PDPage(meta.getPageFormat());
        document.addPage(currentPage);
        yPosition=meta.getUpperRightY();
    }
    
    public PDPageContentStream createStream(PdParagraph style) throws IOException
    {
        PDPageContentStream stream=createStream();
        stream.setFont(style.getFont(), style.getFontSize());
        stream.setNonStrokingColor(style.getFontColor());
        
        return stream;
    }
    
    public PDPageContentStream createStream() throws IOException
    {
        if(currentPage==null)
        {
            createNewPage();
        }
        
        return new PDPageContentStream(document, currentPage,PDPageContentStream.AppendMode.APPEND,true);

    }
                    
    protected void writeText(PDPageContentStream stream, float xPosition, float yPosition, String string) throws IOException
    {
        stream.beginText();
        stream.newLineAtOffset(xPosition, yPosition);
        stream.showText(string);
        stream.endText();
    }

    /**
     * Draw an image on the current page.
     * @param imageFile File containing image
     * @param style  paragraph style to use for any applicable spacing
     */
    public void drawImage(File imageFile,PdParagraph style)
    {
        drawImage(imageFile, style, 0,0);
    }
    /**
     * Draw an image on the current page.
     * @param imageFile File containing image.
     * @param style paragraph style to use
     * @param width image width. Uses image actual width if argument is less than or equal to 0
     * @param height image height. Uses image actual height if argument is less than or equal to 0
     * @throws RuntimeException 
     */
    public void drawImage(File imageFile,PdParagraph style,float width, float height) 
    {
        try
        {
            PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
            
            float actualWidth = (width<=0)?imageObject.getWidth():width;
            float actualHeight = (height <= 0)? imageObject.getHeight():height;
            yPosition -= actualHeight;
            if (yPosition <= meta.getLowerLeftY())
            {
                createNewPage();
                yPosition -= actualHeight;
                if (yPosition < meta.getLowerLeftY())
                {
                    yPosition = meta.getLowerLeftY();
                }
            }
            try (final PDPageContentStream stream = createStream())
            {
                stream.drawImage(imageObject, style.getLeftX(), yPosition, actualWidth, actualHeight);
                yPosition -= style.getLineHeight();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
