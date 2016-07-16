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

import com.baseprogramming.pdwriter.html.HtmlStyle;
import com.baseprogramming.pdwriter.html.HtmlTableScanner;
import com.baseprogramming.pdwriter.model.PdList;
import com.baseprogramming.pdwriter.model.PdParagraph;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

/**
 *
 * @author Roberto C. Benitez
 */
public class HtmlPdWriter
{
    private final AtomicLong nodeIdSequence =  new AtomicLong();
    private final PdWriter writer;
    private float dpi=96;
    private final Map<String,Map<String,CSSValue>> elementSelectors = new HashMap<>();
    private final Map<String,Map<String,CSSValue>> idSelectors = new HashMap<>();
    private final Map<String,Map<String,CSSValue>> classSelectors = new HashMap<>();
    private final Map<String,Map<String,CSSValue>> nodeStyleMaps= new HashMap<>();
    private final Map<String,PdParagraph> nodeParagraphMaps = new HashMap<>();
    
    private float xPosition;
    
    

    public HtmlPdWriter(PdWriter writer)
    {
        this.writer = writer;
        loadDefaultCss();
    }
    
    private long getNextId()
    {
        return nodeIdSequence.incrementAndGet();
    }
    
    private void loadDefaultCss()
    {
        try
        {
            Map<String, Map<String, CSSValue>> map=Utils.getDefaultHtmlCssMap();
            loadCssSelectorsIntoMaps(map);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void loadCssSelectorsIntoMaps(Map<String, Map<String, CSSValue>> map)
    {
        map.entrySet().forEach(e->
        {
            String selector=e.getKey();
            if(selector.startsWith("."))
            {
                classSelectors.put(selector.substring(1), e.getValue());
            }
            else if(selector.startsWith("#"))
            {
                idSelectors.put(selector.substring(1),e.getValue());
            }
            else
            {
                elementSelectors.put(selector, e.getValue());
            }
        });
    }

    public PdWriter getWriter()
    {
        return writer;
    }

    public float getDpi()
    {
        return dpi;
    }

    public void setDpi(float dpi)
    {
        this.dpi = dpi;
    }
    
    public void write(String html) throws IOException
    {
        Document document=Jsoup.parse(html);
        document.traverse(new NodeTextWriter(this));
    }
    
    public void write(File htmlSourceFile,String rootElement) throws IOException
    {
        Document document=Jsoup.parse(htmlSourceFile,"UTF-8");
        
        Element root=document.getElementsByTag(rootElement).first();
        root.traverse(new NodeTextWriter(this));
    }
    
    public void write(File htmlSourceFile) throws IOException
    {
        Document document=Jsoup.parse(htmlSourceFile,"UTF-8");
        document.traverse(new NodeTextWriter(this));
    }
  
    public PdParagraph createNodeStyle(Node node)
    {
        String id=node.attr("id");
        Map<String,CSSValue> map= buildNodeStyleMap(node);
        
        PdParagraph par;
        if(map.isEmpty())
        {
          par=getParentNodeParagraph(node);
          if(par==null)
          {
              par= new PdParagraph(writer.getMeta());
              nodeParagraphMaps.put(id, par);
          }
        }
        else
        {
            par=new HtmlStyle(writer.getMeta(), map,dpi);
            nodeStyleMaps.put(id, map);
            nodeParagraphMaps.put(id, par);
        }
          
        return par;  
    }

    public  Map<String, CSSValue> buildNodeStyleMap(Node node)
    {
        Map<String, CSSValue> styleMap = new HashMap<>();
        addParentStyle(node, styleMap);
        addElementSelectorStyles(node,styleMap);
        addIdSelectorStyles(node,styleMap);
        addClassSelectorStyles(node,styleMap);
        addStyleAttributeCssStyle(node, styleMap);
        
        return styleMap;
    }
    
    private void addParentStyle(Node node,Map<String,CSSValue> styleMap)
    {
        if(node.parent()==null){return;}
        String parentId=node.parent().attr("id");
        Map<String,CSSValue> parentStyle=nodeStyleMaps.get(parentId);
        if(parentStyle==null || parentStyle.isEmpty()){return;}
        
        styleMap.putAll(parentStyle);
    }
    
    public PdParagraph getParentNodeParagraph(Node node)
    {
        if(node.parent()==null){return null;}
        String parentId=node.parent().attr("id");
        
        PdParagraph par=nodeParagraphMaps.get(parentId);
        if(par==null){return null;}
        return par;
    }

    private void addStyleAttributeCssStyle(Node node, Map<String, CSSValue> styleMap)
    {
        Optional<CSSStyleDeclaration> style=getStyleCssDeclaration(node);
        if(style.isPresent())
        {
            styleMap.putAll(createCssDeclarationMap(style.get()));
        }
    }
    
    private void addElementSelectorStyles(Node node,Map<String,CSSValue> map)
    {
        Map<String,CSSValue> selectorMap=elementSelectors.get(node.nodeName());
        if(selectorMap!=null)
        {
            map.putAll(selectorMap);
        }
    }
 
    private void addIdSelectorStyles(Node node,Map<String,CSSValue> map)
    {
        String id=node.attr("id");
        Map<String,CSSValue> selectorMap = idSelectors.get(id);
        if(selectorMap!=null)
        {
            map.putAll(selectorMap);
        }
    }
    
    private void addClassSelectorStyles(Node Node,Map<String,CSSValue> map)
    {
        String id=Node.attr("class");
        Map<String,CSSValue> selectorMap = classSelectors.get(id);
        if(selectorMap!=null)
        {
            map.putAll(selectorMap);
        }
    }
    
    private Optional<CSSStyleDeclaration> getStyleCssDeclaration(Node node)
    {
        return getCss(node, "style");
    }
    
    private Optional<CSSStyleDeclaration> getCss(Node node,String attribute)
    {
        String style=node.attr(attribute);
        if(style==null || style.isEmpty())
        {
            return Optional.empty();
        }
        
        return getCssDeclaration(style);
    }
    
    private Optional<CSSStyleDeclaration> getCssDeclaration(String cssString)
    {
        try
        {
            InputSource source= new InputSource(new StringReader(cssString));
            CSSOMParser parser= new CSSOMParser(new SACParserCSS3());

            CSSStyleDeclaration decl = parser.parseStyleDeclaration(source);

            return Optional.of(decl);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private Map<String,CSSValue> createCssDeclarationMap(CSSStyleDeclaration declaration)
    {
        Map<String,CSSValue> map= new HashMap<>();
        for(int i=0;i<declaration.getLength();i++)
        {
            String key=declaration.item(i);
            CSSValue value=declaration.getPropertyCSSValue(key);
            map.put(key, value);
        }
        
        return map;
    }
    
    public String getBaseUri(Node node)
    {
        if(node==null){return "";}
        File file=new File(node.baseUri());
        if(file.isFile())
        {
            return file.getParent();
        }
        else
        {
            return file.getAbsolutePath();
        }
    }
        
    public static Predicate<Element> formatElementFilter()
    {
         return e->
         {
             String name=e.nodeName();
             return !("i".equalsIgnoreCase(name) || "b".equalsIgnoreCase(name) || "us".equalsIgnoreCase(name));
        };
    }
    
    private class NodeTextWriter implements NodeVisitor
    {
        private boolean writingTable;
        private HtmlTableScanner tableScanner;
        private final HtmlPdWriter parent;
        private HtmlListScanner listScanner;
        private boolean writingList=false;

        public NodeTextWriter(HtmlPdWriter parent)
        {
            this.parent = parent;
        }

        @Override public void head(Node node, int depth)
        {
            String name = node.nodeName();
            
            setNodeIdIfMissing(node);
     
            if("link".equals(name))
            {
                handleLink(node);
                return;
            }
            if(writingTable)
            {
                tableScanner.head(node, depth);
            }
            else if(writingList)
            {
                listScanner.head(node, depth);
            }
            if ("#text".equals(name))
            {
                return;
            }
            if(node instanceof Element==false){return;} //do something else?
                                                        //this should not happen
            
            PdParagraph style = createNodeStyle(node);

            Element tag=(Element)node;
         
            if("img".equals(name))
            {
                drawImage(node, style);
                
            }
            else if("table".equals(name))
            {
                writingTable=true;
                tableScanner= new HtmlTableScanner(parent);
                tableScanner.loadTableStyles(node);
            }
            else if("ol".equals(name))
            {
                PdList pdList=PdList.numeredList(writer.getMeta());
                listScanner = new HtmlListScanner(parent, pdList);
                style.copyTo(pdList);
                writingList=true;
            }
            else if("ul".equals(name))
            {
                PdList pdList=PdList.bulletList(writer.getMeta());
                listScanner = new HtmlListScanner(parent, pdList);
                style.copyTo(pdList);
                writingList=true;
            }
            else if (tag.isBlock() && writingList==false)
            {
                float yPos = style.getUpperY(writer.getLastYPosition());
                writer.setLastYPosition(yPos);
                xPosition = style.getLeftX();
            }
            
            
        }
      
        private void setNodeIdIfMissing(Node node)
        {
            String id = node.attr("id");
            if (id == null || id.trim().isEmpty())
            {
                id = "node-" + getNextId();
                node.attr("id", id);
            }
        }
        
        private void handleLink(Node node)
        {
        if("link".equals(node.nodeName()) == false){return;}
        String type=node.attr("type");
        if("text/css".equals(type)==false){return;}
        
        
        String uri=getBaseUri(node);
        String href=node.attr("href");
        String cssFilePath=Paths.get(uri, href).toString();
        try
        {
            Map<String, Map<String, CSSValue>> css = Utils.getHtmlCssMap(new File(cssFilePath));
            loadCssSelectorsIntoMaps(css);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
         
        
    }

        private void drawImage(Node node, PdParagraph style) throws RuntimeException
        {
            File baseUri = new File(node.baseUri());
            
            if(!baseUri.isDirectory())
            {
                baseUri=baseUri.getParentFile();
            }
            String src=node.attr("src");
            
            float width = 0;
            if (node.attributes().hasKey("width"))
            {
                width = Utils.parseDimension(node.attr("width"), style.getFontSize(), dpi).getPoints();
            }
            float height = 0;
            if (node.attributes().hasKey("height"))
            {
                height = Utils.parseDimension(node.attr("height"), style.getFontSize(), dpi).getPoints();
            }
            
            File imageFile=Paths.get(baseUri.getAbsolutePath(), src).toFile();
            writer.drawImage(imageFile, style, width,height );
        }


        @Override public void tail(Node node, int depth)
        {
            String name = node.nodeName();
            String id = node.attr("id");
            if ("#text".equals(name) && !(writingTable || writingList))
            {
                writeText((TextNode) node);
                return;
            }
            
            if(writingTable)
            {
                tableScanner.tail(node, depth);
            }
            else if(writingList)
            {
                listScanner.tail(node, depth);
            }
            
            if ("table".equals(name))
            {
                writingTable = false;
                
            }
            else if ("ol".equals(name) || "ul".equals(name))
            {
                writingList=false;
            }
            
            if(!("head".equals(name) || "#text".equals(name) ||  writingList))
            {
                handleEndOfBlock(node, name);
                nodeParagraphMaps.remove(id);
                nodeStyleMaps.remove(id);
            }
 
        }
    

        private void handleEndOfBlock(Node node, String name) throws IllegalStateException
        {
            if(node instanceof Element == false)
            {
                String string=String.format("Node argument is of type '%s'; expected '%s'",node.getClass(),Element.class);
                throw new IllegalStateException(string);
            }
            
            
            Element tag=(Element)node;
            PdParagraph par = getNodeStyle(node, name);
            if (writingTable==false && tag.isBlock())
            {
                float yPos = writer.getLastYPosition() - (par.getLineHeight() + par.getBelowSpacing().getPoints());
                writer.setLastYPosition(yPos);
            }
            else
            {
                try
                {
                    xPosition += par.getStringWidth(" ");
                }
                catch (Exception e)
                {
                }
            }
        }
    }
    protected PdParagraph getNodeStyle(Node node, String name) throws IllegalStateException
    {
        String id = node.attr("id");
        PdParagraph par = nodeParagraphMaps.get(id);
        if (par == null)
        {
            String parentId = node.parent().attr("id");
            par = nodeParagraphMaps.get(parentId);
        }
        if (par == null)
        {
            String string = String.format("Found tail of block-level node '%s(Id=%s)', but no paragraph style has been set.", name, id);
            throw new IllegalStateException(string);
        }
        return par;
    }

    protected void writeText(TextNode node)
    {
        String text = node.text();
        if (text != null)
        {
            text = text.trim();
        }

        if (text == null || text.isEmpty())
        {
            return;
        }

        PdParagraph style = getParentNodeParagraph(node);
        if (style == null)
        {
            style = new PdParagraph(writer.getMeta());
        }
        try
        {
            writeText(style, text);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void writeText(PdParagraph style, String content) throws IOException
    {
        if (writer.isAtEndOfPage())
        {
            writer.createNewPage();
            xPosition = style.getLeftX();
        }

        PDPageContentStream stream = writer.createStream();
        try
        {
            stream.setFont(style.getFont(), style.getFontSize());
            stream.setNonStrokingColor(style.getFontColor());
            float yPosition = writer.getLastYPosition();
            int lastPos = 0;
            boolean firstLine = true;

            while (true)
            {
                int start = lastPos;
                int end = content.indexOf("\n", start);
                if (end < 0)
                {
                    end = style.getWrapPositionFromOffset(start, xPosition, content);
                }

                String string = content.substring(start, end);
                writer.writeText(stream, xPosition, yPosition, string);

                firstLine = false;
                lastPos = end;
                updateXPosition(style, string);
                if (end >= content.length())
                {
                    break;
                }

                yPosition = style.getNextY(yPosition);
                xPosition = style.getLeftX();
                writer.setLastYPosition(yPosition);
                if (writer.isAtEndOfPage())
                {
                    stream = writer.createNewPageAndContentStream(stream, style);
                    yPosition = writer.getLastYPosition();
                    xPosition = style.getLeftX();
                }
            }
            writer.setLastYPosition(yPosition);
        }
        finally
        {
            if (stream != null)
            {
                stream.close();
            }
        }
    }

    protected void updateXPosition(PdParagraph style, String string) throws IOException
    {
        float stringWidth = style.getStringWidth(string);
        xPosition += stringWidth + style.getStringWidth(" ");
        if (xPosition >= style.getRightX())
        {
            xPosition = style.getLeftX();
        }
    }
    
    protected void setXPosition(float position)
    {
        xPosition=position;
    }
}
