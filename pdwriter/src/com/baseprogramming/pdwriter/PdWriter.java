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


import com.baseprogramming.pdwriter.model.Borders;
import com.baseprogramming.pdwriter.model.Margin;
import com.baseprogramming.pdwriter.model.PageMetadata;
import com.baseprogramming.pdwriter.model.PdColumn;
import com.baseprogramming.pdwriter.model.PdList;
import com.baseprogramming.pdwriter.model.PdParagraph;
import com.baseprogramming.pdwriter.model.PdTable;
import com.baseprogramming.pdwriter.model.PdTableHeader;
import com.baseprogramming.pdwriter.model.ValueProvider;
import com.baseprogramming.pdwriter.units.PdPoints;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
        yPosition-=table.getAboveSpacing().getPoints();
        float topBorderPosition=yPosition+ + table.getHeader().getFontSize();
        table.setStartYPosition(topBorderPosition);
        writeColumnHeaders(table);
        PDPageContentStream stream= createStream();
        try
        {
            stream.setFont(table.getFont(), table.getFontSize());
            
            yPosition=table.getNextRowYPosition(yPosition);
           boolean drewRowBorder=false;
            for (int i = 0; i < data.size(); i++)
            {
                Map<String, Object> rowData = data.get(i);
                Map<String, List<String>> wrappedRow = new TreeMap<>();
                int maxRows = wrapRowColumnData(rowData, table, wrappedRow);
                float rowHeight = table.getTextHeight(maxRows);

                if (causesPageOverflow(rowHeight))
                {
                    yPosition+= table.getLineHeight() + table.getRowBorder();
                    stream = handlePageOverflow(table,true, stream);
                    drewRowBorder=false;
                }
                
                float maxRowPosition = writeRow(wrappedRow, table, stream);
                
                yPosition=table.getNextRowYPosition(maxRowPosition);
                if (isAtEndOfPage())
                {
                    stream = handlePageOverflow(table,false, stream);
                    drewRowBorder=false;
                }
                else if(table.getRowBorder()> 0)
                {
                     float borderYPosition = table.getNextBorderPosition(maxRowPosition);
                    drawRowBorder(table, borderYPosition, stream);
                    drewRowBorder=true;
                }
            }
            
            if(drewRowBorder)
            {
                yPosition+= table.getLineHeight() + table.getRowBorder();
            }
            
            drawBordersIfPresent(table,drewRowBorder, stream);   
            drawColumnBorders(table, stream);
        }
        finally
        {
            if(stream!=null)
            {
                stream.close();
            }
            yPosition-=table.getBelowSpacing().getPoints();
        }
    }

    private void drawColumnBorders(PdTable table, PDPageContentStream stream) throws IOException
    {
        if(table.getColumnBorder() <=0){return;}
        float x=table.getLeftX();
        float y1=table.getStartYPosition();
        float y2=yPosition - table.getCellPadding().getPoints() + table.getCellPadding().getPoints();
        for(PdColumn column : table.getHeader().getColumns())
        {
            drawVerticalLine(stream, table.getColumnBorder(), x, y1, y2);
            float width=column.getWidth().getPoints();
            x=table.getColumnXPosition(x+width);
        }
        stream.closeAndStroke();
    }

    protected PDPageContentStream handlePageOverflow(PdTable table,boolean supressBottomBorder, PDPageContentStream stream) throws IOException
    {
        drawBordersIfPresent(table,supressBottomBorder, stream);
        
        if(table.getColumnBorder() > 0)
        {
            drawColumnBorders(table, stream);
        }
        
        stream = createNewPageAndContentStream(stream, table);
        float tableTopY=table.getUpperY(yPosition);
        table.setStartYPosition(tableTopY);
        return stream;
    }

    private void drawRowBorder(PdTable table, float yRowPosition, PDPageContentStream stream) throws IOException
    {
        float x=table.getLeftX();
        float x2=table.getRightX();
        drawHorizontalLine(stream, table.getRowBorder(), x, yRowPosition,x2);
        stream.closeAndStroke();
    }

    private void drawBordersIfPresent(PdTable table,boolean supressBottomBorder, PDPageContentStream stream) throws IOException
    {
        Borders border=table.getBorder();
        if(border.hasBorders())
        {
            float x=table.getLeftX();
            float x2=table.getRightX();
            float y=table.getStartYPosition();
            float y2=yPosition;
            
            if(border.getTop() > 0)
            {
                drawHorizontalLine(stream, border.getTop(), x, y, x2);
            }
            
            if(border.getRight() > 0)
            {
                drawVerticalLine(stream, border.getRight(), x2, y, y2);
            }
            
            if(border.getBottom() > 0 && !supressBottomBorder)
            {
                drawHorizontalLine(stream, border.getBottom(), x, y2, x2);
            }
            
            if(border.getLeft() > 0)
            {
                drawVerticalLine(stream, border.getLeft(), x, y, y2);
            }
            
            stream.closeAndStroke();
        }
    }

    private void writeColumnHeaders(PdTable table) throws IOException
    {
        PdTableHeader header=table.getHeader();
        try(PDPageContentStream stream=createStream())
        {
            stream.setFont(header.getFont(), header.getFontSize());
            float xPosition=table.getColumnXPosition(0);
            float rowTopY=yPosition;
            for(PdColumn column : header.getColumns())
            {
                String label=column.getLabel();
                float width=column.getWidth().getPoints();
                writeText(stream, xPosition, yPosition, label);
                xPosition=table.getColumnXPosition(xPosition+width);
            }
            float borderYPosition= table.getNextBorderPosition(rowTopY);
            if(table.getRowBorder() > 0)
            {
                drawRowBorder(table, borderYPosition, stream);
            }
        }
    }

    private float writeRow(Map<String, List<String>> wrappedRow, PdTable table, PDPageContentStream stream) throws IOException
    {   
        PdTableHeader header=table.getHeader();
       
        float xPosition=table.getColumnXPosition(0);
        float maxRowPosition=yPosition;
        for(int j=0;j<header.getColumnCount();j++)
        {
            PdColumn column=header.getColumns().get(j);
            float width=column.getWidth().getPoints();
            List<String> content=wrappedRow.get(column.getName());
            if(!content.isEmpty())
            {
                float rowYPosition = writeCellContent(table,content, stream, xPosition);
                if(rowYPosition < maxRowPosition){maxRowPosition=rowYPosition;}
            }
            xPosition=table.getColumnXPosition(xPosition + width);
        }
        return maxRowPosition+table.getLineHeight();
    }

    private int wrapRowColumnData(Map<String, Object> rowData, PdTable table, Map<String, List<String>> wrappedRow) throws IOException
    {
        int maxRowCount=0;
        for(PdColumn column : table.getHeader().getColumns())
        {
            Object value=rowData.get(column.getName());
            List<String> wrapped;
            if(value==null)
            {
                wrapped=Collections.EMPTY_LIST;
            }
            else
            {
                float width=column.getWidth().getPoints();
                wrapped=wrapText(table,value.toString(), width);
            }
            wrappedRow.put(column.getName(), wrapped);
            if(wrapped.size() > maxRowCount)
            {
                maxRowCount=wrapped.size();
            }
        }
        
        return maxRowCount;
    }

    private float writeCellContent(PdTable table, List<String> cellContent,PDPageContentStream stream, float xPosition) throws IOException
    {   
        float rowYPosition=yPosition;
        for(String line : cellContent)
        {
            writeText(stream, xPosition, rowYPosition,line);
            rowYPosition=table.getNextY(rowYPosition);
        }
        
        return rowYPosition;
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

    public void drawVerticalLine(PDPageContentStream stream, float lineWidth, float x, float y1, float y2) throws IOException
    {
        stream.setLineWidth(lineWidth);
        stream.moveTo(x, y1);
        stream.lineTo(x, y2);
    }

    public void drawHorizontalLine(PDPageContentStream stream, float lineWidth, float x1, float y, float x2) throws IOException
    {
        stream.setLineWidth(lineWidth);
        stream.moveTo(x1, y);
        stream.lineTo(x2, y);
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
            yPosition=paragraph.getUpperY(yPosition);
            int lastPos=0;
            boolean firstLine=true;
            while(true)
            {
                int start=lastPos;
                
                int end=paragraph.getWrapPosition(content, start,firstLine);
                float xPosition=paragraph.getLeftX(firstLine);

                if(end > content.length()){end=content.length();}

                String string=content.substring(start, end);
                writeText(stream, xPosition, yPosition, string);
                firstLine=false;
                lastPos=end;
                
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

    protected boolean isAtEndOfPage()
    {
        return yPosition <=meta.getLowerLeftY();
    }

    protected PDPageContentStream createNewPageAndContentStream(PDPageContentStream stream, PdParagraph paragraph) throws IOException
    {
        createNewPage();
        stream.close();
        stream=createStream();
        stream.setFont(paragraph.getFont(), paragraph.getFontSize());
        stream.setNonStrokingColor(paragraph.getFontColor());
        return stream;
    }

    protected void createNewPage()
    {
        currentPage = new PDPage(meta.getPageFormat());
        document.addPage(currentPage);
        yPosition=meta.getUpperRightY();
    }
    
    protected PDPageContentStream createStream() throws IOException
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
     * @param imageFile File containing image.
     * @param style paragraph style to use
     * @param width image width. Uses image actual width if argument is less than or equal to 0
     * @param height image height. Uses image actual height if argument is less than or equal to 0
     * @throws RuntimeException 
     */
    public void drawImage(File imageFile,PdParagraph style,float width, float height) throws RuntimeException
    {
        try
        {
            PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
            
            float actualWidth = (width<=0)?imageObject.getWidth():width;
            float actualHeight = (height <= 0)? imageObject.getHeight():height;
            yPosition -= height;
            if (yPosition <= meta.getLowerLeftY())
            {
                createNewPage();
                yPosition -= height;
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
