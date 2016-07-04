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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
    private final List<String> columnNames = new ArrayList<>();
    
    private String tableCaption="";
    private List<String> rowBuffer = new ArrayList<>();
    private final Deque<TagScanner> scanners =new ArrayDeque<>();

    public HtmlTableScanner()
    {
    }
    
    @Override
    public void head(Node node,int depth)
    {
        String name=node.nodeName();
        
        
        if("caption".equals(name))
        {
            TagScanner scanner= new CaptionScanner(node, 0);
            scanners.add(scanner);
        }
        else if("thead".equals(name))
        {
            THeadScanner scanner= new THeadScanner(node, 0);
            scanners.add(scanner);
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
                handleTextNode((TextNode)node);
            }
        }

        @Override
        public void handleTextNode(TextNode node)
        {
            String string=node.text();
            caption.append(string);
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
                columnNames.add(scanner.getTextContent());
            }

        }

        @Override public void tail(Node node, int depth)
        {
            if("thead".equals(node.nodeName()))
            {
                scannedHeader=true;
                System.out.printf("Column Headers\n");
                columnNames.forEach(e->System.out.printf("\t%s\t",e));
                System.out.println("");
                scanners.poll();
            }
        }

        @Override
        public void handleTextNode(TextNode node)
        {
            
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
                handleTextNode((TextNode)node);
            }
        }

        @Override public void handleTextNode(TextNode node)
        {
            String string=node.text();
            content.append(string);
        }

        @Override public void tail(Node node, int depth)
        {
            
        }
    }
    
    private class TrScanner extends TagScanner
    {
        private StringBuffer data= new StringBuffer();
        
        public TrScanner(Node node, int depth)
        {
            super(node, depth);
        }

        @Override public void handleTextNode(TextNode node)
        {
           
        }

        @Override public void head(Node node, int depth)
        {
             String name = node.nodeName();
            if ("td".equals(name) || "tr".equals(name))
            {
                data = new StringBuffer();
            }
            else if ("#text".equals(name))
            {
                String string = ((TextNode) node).text();
                if (!string.trim().isEmpty())
                {
                    data.append(string);
                }
            }
        }

        @Override public void tail(Node node, int depth)
        {
             String name=node.nodeName();
             if("td".equals(name))
             {
                 rowBuffer.add(data.toString());
             }
             else if("tr".equals(name))
             {
                 System.out.printf("Row data\n");
                 rowBuffer.forEach(e->System.out.printf("\t%s",e));
                 System.out.println("");
                 rowBuffer.clear();
             }
        }

    }
    
    
}
