/*
 * Copyright 2016 Roberto C. Benitez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copyTo of the License at
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

import com.baseprogramming.pdwriter.model.PdList;
import com.baseprogramming.pdwriter.model.PdParagraph;
import java.io.IOException;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

/**
 *
 * @author Roberto C. Benitez
 */
public class HtmlListScanner implements NodeVisitor
{
    private final  HtmlPdWriter htmlWriter;
    private final PdList style;
    private PdParagraph itemStyle;

    public HtmlListScanner(HtmlPdWriter htmlWriter, PdList style)
    {
        this.htmlWriter = htmlWriter;
        this.style = style;
        itemStyle = style.createItemStyle();
    }
    
    @Override
    public void head(Node node, int depth)
    {
        String name=node.nodeName();
        if("#text".equals(name)){return;}
        itemStyle=htmlWriter.createNodeStyle(node);
        if(itemStyle==null){itemStyle=style.createItemStyle();}
        if("li".equals(name))
        {
            htmlWriter.setXPosition(style.getLeftX());
            String label=style.getItemLabel().getValue() + style.getLabelBodyDelimiter();
            try
            {
                htmlWriter.writeText(itemStyle, label);
            }
            catch(Exception e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void tail(Node node, int depth)
    {
        String name=node.nodeName();
        if("#text".equals(name))
        {
            writeText(node);
        }
        else if("li".equals(name))
        {
            PdWriter writer=htmlWriter.getWriter();
            float y=itemStyle.getNextY(writer.getLastYPosition());
            writer.setLastYPosition(y);
        }
    }

    private void writeText(Node node) throws RuntimeException
    {
        String body=((TextNode)node).text();
        if(body.trim().isEmpty() && "li".equals(node.parentNode().nodeName())==false){return;}
        
        try
        {
            htmlWriter.writeText(itemStyle, body);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
}
