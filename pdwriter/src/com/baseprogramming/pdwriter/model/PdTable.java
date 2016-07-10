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

import com.baseprogramming.pdwriter.units.PdPoints;
import com.baseprogramming.pdwriter.units.PdUnit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 *
 * @author Roberto C. Benitez
 */
public class PdTable extends PdParagraph
{

    private final PdTableHeader header;

    private PdUnit cellSpacing;
    private PdUnit cellPadding;
    private float rowBorder = 0;
    private float columnBorder = 0;

    private float startYPosition;

    public PdTable(PageMetadata page, List<PdColumn> columns)
    {
        super(page);
        header = new PdTableHeader(columns, getFont(), getFontSize());
        cellPadding = new PdPoints(0);
        cellSpacing = new PdPoints(0);
    }

    public PdTable(PageMetadata page, String... names)
    {
        super(page);
        header = createTableHeader(names);
        cellPadding = new PdPoints(0);
        cellSpacing = new PdPoints(0);

    }

    public PdTable(PageMetadata page)
    {
        this(page, new LinkedList<>());
    }

    public PdTableHeader getHeader()
    {
        return header;
    }
    
    public void calculateMissingColumnWidths()
    {
        List<PdColumn> columns=header.getColumnsWithUnsetWidth();        
        float availableWidth=getWidth() - getContentWidth();
        PdUnit columnWidth=new PdPoints(availableWidth / columns.size());
        columns.stream().forEach(e->e.setWidth(columnWidth));
    }
    
    public float getContentWidth()
    {
        float padding=getCellPadding().getPoints();
        float spacing=getCellSpacing().getPoints();
        
        float sum=0;
        sum = header.getColumns().stream()
                .map((column) -> column.getWidth().getPoints() + ((padding + spacing)*2) + columnBorder)
                .reduce(sum, (accumulator, _item) -> accumulator + _item);
        
        return sum;
    }

    @Override
    public float getUpperY(float offset)
    {
        float aboveSpace=getAboveSpacing().getPoints();
        float pos = offset - aboveSpace;
        if(offset < 1)
        {
            pos=getPage().getUpperRightY();
        }
        
        return pos;
    }
    
    @Override
    public float getRightX()
    {
        float padding=cellPadding.getPoints();
        float spacing=cellSpacing.getPoints();
        return super.getRightX() + padding + spacing; 
    }

    @Override
    public final float getFontSize()
    {
        return super.getFontSize();
    }

    @Override
    public final PDType1Font getFont()
    {
        return super.getFont();
    }

    public float getStartYPosition()
    {
        return startYPosition;
    }

    public void setStartYPosition(float startYPosition)
    {
        this.startYPosition = startYPosition;
    }
    
    public float getNextBorderPosition(float currentPosition)
    {
        float padding=getCellPadding().getPoints();
        float spacing=getCellSpacing().getPoints();
        
        return currentPosition - padding - spacing;
    }
    
    public float getSpacingAndPaddingGap()
    {
        return cellSpacing.getPoints() + cellPadding.getPoints();
    }
    
    public float getTextBaseToTopBorderDistance()
    {
        return getLineHeight() + getSpacingAndPaddingGap();
    }
    
    public float getRowHeight()
    {
        return getLineHeight() + (2*getSpacingAndPaddingGap());
    }

    public PdUnit getCellSpacing()
    {
        return cellSpacing;
    }

    public void setCellSpacing(PdUnit cellSpacing)
    {
        this.cellSpacing = cellSpacing;
    }

    public PdUnit getCellPadding()
    {
        return cellPadding;
    }

    public void setCellPadding(PdUnit cellPadding)
    {
        this.cellPadding = cellPadding;
    }

    public float getRowBorder()
    {
        return rowBorder;
    }

    public void setRowBorder(float rowBorder)
    {
        this.rowBorder = rowBorder;
    }

    public float getColumnBorder()
    {
        return columnBorder;
    }

    public float getNextRowYPosition(float offset)
    {
        float padding = cellPadding.getPoints();
        float spacing = cellSpacing.getPoints();
        float pos = getNextY(offset) - rowBorder - ((spacing + padding)*2);

        return pos;
    }

    public void setColumnBorder(float columnBorder)
    {
        this.columnBorder = columnBorder;
    }

    public float getColumnXPosition(float offset)
    {
        float padding = getCellPadding().getPoints();
        float spacing = getCellSpacing().getPoints();
        return offset + columnBorder + padding + spacing;
    }
    
    public float getFirstColumnXPosition()
    {
        return getLeftX() + getColumnXPosition(0);
    }

    public final PdTableHeader createTableHeader(String... names)
    {
        List<PdColumn> columns = createColumns(names);
        return new PdTableHeader(columns, getFont(), getFontSize());
    }

    public List<PdColumn> createColumns(String... names)
    {
        return Stream.of(names).map(stringToPdColumnMapper())
                .collect(Collectors.toList());
    }

    private Function<String, PdColumn> stringToPdColumnMapper()
    {
        return e
                -> 
                {
                    try
                    {
                        float width = getStringWidth(e);
                        PdUnit units = new PdPoints(width);
                        return new PdColumn(e, units);
                    }
                    catch (Exception ex)
                    {
                        Throwable t = (ex.getCause() == null) ? ex : ex.getCause();
                        throw new RuntimeException(t.getMessage(), t);
                    }
        };
    }

    public void calculateColumnWidths(List<Map<String, Object>> data, int rowsToSample) throws IOException
    {
        float spacing=2*getSpacingAndPaddingGap();
        for (PdColumn column : header.getColumns())
        {
            
            float width = spacing+calculateColumnWidth(column, rowsToSample, data);
            float labelWidth=spacing+header.getFont().getStringWidth(column.getLabel())/1000;
            
            if(labelWidth > width){width=labelWidth;}
            
            column.setWidth(new PdPoints(width));
        }
    }

    public List<PdColumn> createColumns(List<Map<String, Object>> data, int rowsToSample, String... names) throws IOException
    {
        List<PdColumn> list = new ArrayList<>();
        for (String name : names)
        {
            PdColumn column = new PdColumn(name, new PdPoints(0));
            float width = calculateColumnWidth(column, rowsToSample, data);
            column.setWidth(new PdPoints(width));
            list.add(column);
        }

        return list;
    }

    private float calculateColumnWidth(PdColumn column, int columWidthRowsToSample, List<Map<String, Object>> data) throws IOException
    {
        float sum = 0;
        int max = (columWidthRowsToSample > data.size()) ? data.size() : columWidthRowsToSample;
        for (int j = 0; j < max; j++)
        {
            Map<String, Object> row = data.get(j);
            Object value = row.get(column.getName());
            if (value != null)
            {
                sum += getStringWidth(value.toString());
            }
        }

        float avg = (sum / (max));
        float labelWidth = getStringWidth(column.getLabel());

        if (labelWidth > avg)
        {
            return labelWidth;
        }
        return avg;
    }

}
