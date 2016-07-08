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
import com.baseprogramming.pdwriter.model.PdColumn;
import com.baseprogramming.pdwriter.model.PdTable;
import com.baseprogramming.pdwriter.model.PdTableHeader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

/**
 *
 * @author Roberto C. Benitez
 */
public class PdTableWriter implements AutoCloseable
{
    private final PdWriter writer;
    private PDPageContentStream stream;
    private final PdTable table;
    boolean drewRowBorder=false;

    public PdTableWriter(PdWriter writer, PdTable table)
    {
        this.writer = writer;
        this.table = table;
    }
    
    private void createStreamIfNull() throws IOException
    {
        if(stream==null)
        {
            stream=writer.createStream(table);
        }
    }

    public boolean isDrewRowBorder()
    {
        return drewRowBorder;
    }
    
    public void writeColumnHeaders() throws IOException
    {
        PdTableHeader header=table.getHeader();
        createStreamIfNull();
        float xPosition = table.getFirstColumnXPosition();
        float topY = table.getStartYPosition();
        float y=topY - (table.getSpacingAndPaddingGap() + table.getLineHeight());
        for (PdColumn column : header.getColumns())
        {
            String label = column.getLabel();
            float width = column.getWidth().getPoints();
            writer.writeText(stream, xPosition,y, label);
            xPosition = table.getColumnXPosition(xPosition + width);
        }
        
        if (table.getRowBorder() > 0)
        {
            float borderYPosition = table.getNextBorderPosition(y);
            drawRowBorder( borderYPosition);
            y = borderYPosition - (table.getTextBaseToTopBorderDistance());
        }
        else
        {
            y -= (table.getSpacingAndPaddingGap() +table.getLineHeight());
        }
        writer.setLastYPosition(y);
    }
    
    public void write(List<Map<String,Object>> data) throws IOException
    {
        initYPosition();
        writeColumnHeaders();
        try
        {   
           drewRowBorder=false;
            for (int i = 0; i < data.size(); i++)
            {
                Map<String, Object> rowData = data.get(i);
                writeRow(rowData);
            }
            
            if(drewRowBorder)
            {
                writer.increaseYPosition(table.getTextBaseToTopBorderDistance() + table.getRowBorder());
            }
            
            drawBordersIfPresent(drewRowBorder);   
            drawColumnBorders();
        }
        finally
        {
            if(stream!=null)
            {
                stream.close();
            }
            writer.decreaseYPosition(table.getBelowSpacing());
        }
    }

    public void writeRow(Map<String, Object> rowData) throws IOException
    {
        drewRowBorder=false;

        Map<String, List<String>> wrappedRow = new TreeMap<>();
        int maxRows = wrapRowColumnData(rowData, wrappedRow);
        float textHeight = table.getTextHeight(maxRows);
        if (writer.causesPageOverflow(textHeight))
        {
            writer.increaseYPosition(table.getTextBaseToTopBorderDistance());
            handlePageOverflow(true);
            drewRowBorder=false;
        }
        
        float maxRowPosition = writeWrappedRow(wrappedRow);
        float yPosition=table.getNextRowYPosition(maxRowPosition);
        writer.setLastYPosition(yPosition);
        if (writer.isAtEndOfPage())
        {
            writer.increaseYPosition(table.getTextBaseToTopBorderDistance());
            handlePageOverflow(false);
            drewRowBorder=false;
        }
        else if(table.getRowBorder()> 0)
        {
            float borderYPosition = table.getNextBorderPosition(maxRowPosition);
            drawRowBorder( borderYPosition);
            drewRowBorder=true;
        }
    }

    public float initYPosition()
    {
        float yPosition = writer.getLastYPosition() - table.getAboveSpacing().getPoints();
        table.setStartYPosition(yPosition);
        writer.setLastYPosition(yPosition);
        return yPosition;
    }
    
    public float writeWrappedRow(Map<String, List<String>> wrappedRow) throws IOException
    {   
        createStreamIfNull();
        PdTableHeader header=table.getHeader();
        
        float xPosition=table.getFirstColumnXPosition();
        float maxRowPosition=writer.getLastYPosition();
        for(int j=0;j<header.getColumnCount();j++)
        {
            PdColumn column=header.getColumns().get(j);
            float width=column.getWidth().getPoints();
            List<String> content=wrappedRow.get(column.getName());
            if(!content.isEmpty())
            {
                float rowYPosition = writeCellContent(content, xPosition);
                if(rowYPosition < maxRowPosition){maxRowPosition=rowYPosition;}
            }
            xPosition=table.getColumnXPosition(xPosition + width);
        }
        return maxRowPosition + table.getLineHeight();
    }
    
    protected float writeCellContent(List<String> cellContent, float xPosition) throws IOException
    {   
        createStreamIfNull();
        float rowYPosition=writer.getLastYPosition();
        for(String line : cellContent)
        {
            writer.writeText(stream, xPosition, rowYPosition,line);
            rowYPosition=table.getNextY(rowYPosition);
        }
        
        return rowYPosition;
    }
    
    public void drawRowBorder(float yRowPosition) throws IOException
    {
        createStreamIfNull();
        
        float x=table.getLeftX();
        float x2=table.getRightX();
        writer.drawHorizontalLine(stream, table.getRowBorder(), x, yRowPosition,x2);
        stream.closeAndStroke();
    }
    
    public void drawColumnBorders() throws IOException
    {
        if(table.getColumnBorder() <=0){return;}
        createStreamIfNull();
        float x = table.getLeftX();
        float y1 = table.getStartYPosition();
        float y2 = writer.getLastYPosition() - table.getCellPadding().getPoints() + table.getCellPadding().getPoints();
        for (PdColumn column : table.getHeader().getColumns())
        {
            writer.drawVerticalLine(stream, table.getColumnBorder(), x, y1, y2);
            float width = column.getWidth().getPoints();
            x = table.getColumnXPosition(x + width);
        }
        stream.closeAndStroke();
    }
    
    public void drawBordersIfPresent(boolean supressBottomBorder) throws IOException
    {
        Borders border=table.getBorder();
        if (!border.hasBorders()){return;}

        float x = table.getLeftX();
        float x2 = table.getRightX();
        float y = table.getStartYPosition();
        float y2 = writer.getLastYPosition();

        if (border.getTop() > 0)
        {
            writer.drawHorizontalLine(stream, border.getTop(), x, y, x2);
        }

        if (border.getRight() > 0)
        {
            writer.drawVerticalLine(stream, border.getRight(), x2, y, y2);
        }

        if (border.getBottom() > 0 && !supressBottomBorder)
        {
            writer.drawHorizontalLine(stream, border.getBottom(), x, y2, x2);
        }

        if (border.getLeft() > 0)
        {
            writer.drawVerticalLine(stream, border.getLeft(), x, y, y2);
        }

        stream.stroke();
    }
    
    public int wrapRowColumnData(Map<String, Object> rowData, Map<String, List<String>> wrappedRow) throws IOException
    {
        int maxRowCount=1;
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
                wrapped=writer.wrapText(table,value.toString(), width);
            }
            wrappedRow.put(column.getName(), wrapped);
            if(wrapped.size() > maxRowCount)
            {
                maxRowCount=wrapped.size();
            }
        }
        
        return maxRowCount;
    }
    
    public PDPageContentStream handlePageOverflow(boolean supressBottomBorder) throws IOException
    {
        createStreamIfNull();
        drawBordersIfPresent(supressBottomBorder);
        
        if(table.getColumnBorder() > 0)
        {
            drawColumnBorders();
        }
        
        stream = writer.createNewPageAndContentStream(stream, table);
        float tableTopY=table.getUpperY(writer.getLastYPosition()) + table.getLineHeight() + table.getSpacingAndPaddingGap();
        table.setStartYPosition(tableTopY);
        return stream;
    }

    @Override
    public void close() throws Exception
    {
        if(stream!=null)
        {
            stream.close();
        }
    }
 
}
