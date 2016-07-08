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

import com.baseprogramming.pdwriter.HtmlPdWriter;
import com.baseprogramming.pdwriter.PdTableWriter;
import com.baseprogramming.pdwriter.PdWriter;
import com.baseprogramming.pdwriter.model.Borders;
import com.baseprogramming.pdwriter.model.PdColumn;
import com.baseprogramming.pdwriter.model.PdParagraph;
import com.baseprogramming.pdwriter.model.PdTable;
import com.baseprogramming.pdwriter.model.PdTableHeader;
import com.baseprogramming.pdwriter.units.PdPoints;
import com.baseprogramming.pdwriter.units.PdUnit;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

/**
 *
 * @author Roberto C. Benitez
 */
public class HtmlTableScanner implements NodeVisitor
{
    private boolean scannedHeader=false;
    private boolean firstTrIsHeader=false;
    private boolean scanningBody=false;
    private final List<String> columnNames = new LinkedList<>();
    
    private String tableCaption="";
    private List<String> rowBuffer = new ArrayList<>();
    private final Deque<TagScanner> scanners =new ArrayDeque<>();
    private final HtmlPdWriter htmlWriter;
    private PdTable tableModel;
    
    private String tableNodeId;
    private PdTableWriter tableWriter;

    public HtmlTableScanner(HtmlPdWriter htmlWriter)
    {
        this.htmlWriter=htmlWriter;
        tableModel = new PdTable(htmlWriter.getWriter().getMeta());
        tableWriter = new PdTableWriter(htmlWriter.getWriter(),tableModel);
    }
      
    @Override
    public void head(Node node,int depth)
    {
        String name=node.nodeName();
        
        if("table".equals(name))
        {
            loadTableStyles(node);
            tableNodeId=node.attr("id");
        }
        else if("caption".equals(name))
        {
            TagScanner scanner= new CaptionScanner(node, 0);
            scanners.add(scanner);
        }
        else if("thead".equals(name))
        {
            THeadScanner scanner= new THeadScanner(node, 0);
            scanners.add(scanner);
            columnNames.clear();
        }
        else if("tbody".equals(name) )
        {
            if(scannedHeader==false)
            {
                firstTrIsHeader=true;
            }
            scanningBody=true;
            TagScanner scanner= new TrScanner(node, depth);
            scanners.add(scanner);
        }
        else
        {
            TagScanner scanner=scanners.peekLast();
            if(scanner!=null)
            {
                scanner.head(node, depth);
            }
        }
         
    }

    @Override
    public void tail(Node node, int depth)
    {
        String name=node.nodeName();
        TagScanner scanner=scanners.peekLast();
        if(scanner!=null)
        {
            scanner.tail(node, depth);
        }
        if("table".equals(name))
        {
            finishTable();
        }
            
    }

    public void finishTable() throws RuntimeException
    {
        PdWriter writer=htmlWriter.getWriter();
        float y=writer.getLastYPosition() + tableModel.getLineHeight() + tableModel.getRowBorder();
        writer.setLastYPosition(y);
        try
        {
            if(tableWriter.isDrewRowBorder())
            {
                writer.increaseYPosition(tableModel.getSpacingAndPaddingGap());
            }
            tableWriter.drawBordersIfPresent(tableWriter.isDrewRowBorder());
            tableWriter.drawColumnBorders();
            tableWriter.close();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public void loadTableStyles(Node node)
    {
        PdParagraph style=htmlWriter.createNodeStyle(node);
        if(node.hasAttr("border"))
        {
            float border=Float.parseFloat(node.attr("border"));
            Borders borders  = new Borders(border);
            tableModel.setBorder(borders);
            tableModel.setRowBorder(border);
            tableModel.setColumnBorder(border);
        }
        
        if(node.hasAttr("cellpadding"))
        {
            float padding=Float.parseFloat(node.attr("cellpadding"));
            tableModel.setCellPadding(new PdPoints(padding));
        }
        
        if(node.hasAttr("cellspacing"))
        {
            float spacing=Float.parseFloat(node.attr("cellspacing"));
            tableModel.setCellSpacing(new PdPoints(spacing));
        }
    }
    
    public List<String> getColumnNames()
    {
        return columnNames;
    }

    public List<String> getRowBuffer()
    {
        return rowBuffer;
    }
    
    private class CaptionScanner extends TagScanner
    {

        public CaptionScanner(Node node, int depth)
        {
            super(node, depth);
        }
         
        private final StringBuilder caption = new StringBuilder();
        
        @Override public void head(Node node, int depth)
        {
            String name=node.nodeName();
            if("#text".equals(name))
            {
                String string = ((TextNode)node).text();
                caption.append(string);
            }
        }

        @Override public void tail(Node node, int depth)
        {
            if("caption".equals(node.nodeName()))
            {
                tableCaption=caption.toString();
                scanners.poll();
            }
        }
    }
    
    private class THeadScanner extends TagScanner
    {
        private Map<String,PdParagraph> styleMap= new HashMap<>();

        public THeadScanner(Node node, int depth)
        {
            super(node, depth);
        }

        @Override public void head(Node node, int depth)
        {
            String name = node.nodeName();
            if ("th".equals(name) || "td".equals(name))
            {
                CellScanner scanner = new CellScanner(node, depth, true);
                PdTableHeader header= tableModel.getHeader();
                String id="column-"+header.getColumnCount();
                String label=scanner.getTextContent();
                
                PdParagraph style=htmlWriter.createNodeStyle(node);
                styleMap.put(id, style);
                PdUnit width = getBlockWidth(style);
                PdColumn column= new PdColumn(id,label, width);
                header.getColumns().add(column);
            }

        }

        private PdUnit getBlockWidth(PdParagraph style)
        {
            PdUnit width;
            if(style instanceof HtmlStyle==false)
            {
                width=new PdPoints(0);
            }
            else
            {
                HtmlStyle tmp=(HtmlStyle)style;
                width=(tmp.getBlockWidth()==null)?new PdPoints(0):tmp.getBlockWidth();
            }
            return width;
        }

        @Override public void tail(Node node, int depth)
        {
            if("thead".equals(node.nodeName()))
            {
                scannedHeader=true;
                scanners.poll();
                
                writeCaptionIfPresent();
                writeTableHeader();
            }
        }
        

        private void writeTableHeader() throws RuntimeException
        {
            try
            {
                float y=tableWriter.initYPosition();
                tableModel.calculateMissingColumnWidths();
                tableWriter.writeColumnHeaders();
                
            }
            catch(Exception e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        private void writeCaptionIfPresent() 
        {
            try
            {
                if(!(tableCaption==null || tableCaption.trim().isEmpty()))
                {
                    PdWriter writer=htmlWriter.getWriter();
                    writer.write(tableModel, tableCaption);
                    float y=writer.getLastYPosition() + tableModel.getLineHeight();
                    writer.setLastYPosition(y);
                }
            }
            catch(Exception e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
    
    private class CellScanner extends TagScanner
    {
        private final StringBuilder content = new StringBuilder();

        public CellScanner(Node node, int depth)
        {
            this(node, depth, false);
        }
        public CellScanner(Node node, int depth,boolean autoScan)
        {
            super(node, depth);
            if(autoScan && node!=null)
            {
                doScan();
            }
        }
        
        private void doScan()
        {
            if(getNode()!=null)
            {
                getNode().traverse(this);
            }
        }
        
        public String getTextContent()
        {
            return content.toString();
        }

        @Override public final void head(Node node, int depth)
        {
            String name=node.nodeName();
            if("#text".equals(name))
            {
                 String string = ((TextNode) node).text();
                content.append(string);
            }
        }

        @Override public void tail(Node node, int depth)
        {
            
        }
    }
    
    private class TrScanner extends TagScanner
    {
        private StringBuffer cellData= new StringBuffer();
        private final Map<String,Object> rowData= new TreeMap<>();
        
        public TrScanner(Node node, int depth)
        {
            super(node, depth);
        }

        @Override public void head(Node node, int depth)
        {
            String name = node.nodeName();
            if("tr".equals(name))
            {
                rowData.clear();
                cellData = new StringBuffer();
            }
            else if ("td".equals(name))
            {
                cellData = new StringBuffer();
            }
            else if ("#text".equals(name))
            {
                String string = ((TextNode) node).text();
                if (!string.trim().isEmpty())
                {
                    cellData.append(string);
                }
            }
        }

        @Override public void tail(Node node, int depth)
        {
             String name=node.nodeName();
             if("td".equals(name))
             {
                 String id="column-"+rowData.size();
                 rowData.put(id, cellData.toString());
             }
             else if("tr".equals(name))
             {
                try 
                {
                    tableWriter.writeRow(rowData);
                }
                catch(IOException | RuntimeException e)
                {
                    throw  new RuntimeException(e.getMessage(), e);
                }   
             }
        }
    }
    
    
}
