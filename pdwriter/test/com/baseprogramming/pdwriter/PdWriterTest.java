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

import com.baseprogramming.dev.gen.DataFactory;
import com.baseprogramming.pdwriter.model.Borders;
import com.baseprogramming.pdwriter.model.Margin;
import com.baseprogramming.pdwriter.model.PageMetadata;
import com.baseprogramming.pdwriter.model.PdColumn;
import com.baseprogramming.pdwriter.model.PdParagraph;
import com.baseprogramming.pdwriter.model.PdTable;
import com.baseprogramming.pdwriter.model.PdTableHeader;
import com.baseprogramming.pdwriter.units.PdInch;
import com.baseprogramming.pdwriter.units.PdPoints;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Roberto C. Benitez
 */
public class PdWriterTest
{
    
    public PdWriterTest()
    {
    }
    
    @BeforeClass public static final void init()
    {
        
    }
     
     private List<Map<String,Object>> getDataTable(int rows)
     {
         DataFactory.loadAllDefaultData();
         
         List<Map<String,Object>> table= new ArrayList<>();
         
         DateFormat format= new SimpleDateFormat("yyyy-MM-dd");
         Random rand= new Random();
         for(int i=0;i<rows;i++)
         {
             String first=DataFactory.getFirstNameAnyGender().getWord();
             String middle=DataFactory.getFirstNameAnyGender().getWord();
             String last=DataFactory.getLastName().getWord();
             Date date = DataFactory.genDate(1910, 2015);
             String dob=format.format(date);
             int count=rand.nextInt(10) +2;
             String memo=DataFactory.genWords(count);
             
             Map<String,Object> row= new HashMap<>();
             row.put("First Name", first);
             row.put("Middle Name", middle);
             row.put("Last Name", last);
             row.put("D.O.B", dob);
             row.put("Memo", memo);
             
             table.add(row);
         }
         
         return table;
     }
    
    @Test public void testWriteTable()
    {
        List<Map<String,Object>> data=getDataTable(50);
        
        final File file= new File("C:/tmp/test-PdWriter-PdTable.pdf");
        Margin margin= new Margin(0.75f, 0.2f, 0.5f, 0.5f);

        try(PDDocument doc = new PDDocument())
        {
            PdWriter writer =new PdWriter(doc, margin);
            PdTable table= writer.createTable("First Name","Middle Name","Last Name","D.O.B","Memo");
            Borders border= new Borders(3, 1, 2, 1);
            table.setCellPadding(new PdPoints(10));
            table.setRowBorder(1);
            table.setColumnBorder(1);
            table.setBorder(border);
            PdTableHeader header=table.getHeader();
            header.setFont(PDType1Font.TIMES_BOLD);
            table.calculateColumnWidths(data, 5);
            PdColumn memo=header.getColumn("Memo");
            memo.setWidth(new PdInch(2));
            for(PdColumn column : header.getColumns())
            {
                System.out.printf("%s->%s\n",column.getName(),column.getWidth());
            }
            writer.write(table,data);
            
            
            doc.save(file);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testHtmlWriter()
    {
        File input = new File("c:/tmp/html-input-table-with-thead.html");
        
        File output= new File("c:/tmp/html-input-table-with-thead.pdf");
        Margin margin= new Margin(0.75f, 0.2f, 0.5f, 0.5f);
        try(PDDocument pdDoc = new PDDocument())
        {
            PdWriter writer= new PdWriter(pdDoc, margin);
            writer.writeHtml(input, "body");

            pdDoc.save(output);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
    @Test public void testTextPosition()
    {
        File ouptut= new File("c:/tmp/position-tests.pdf");
        
        try(PDDocument doc= new PDDocument())
        {
            Margin margin= new Margin(0.75f, 0.2f, 0.5f, 0.5f);
            PdWriter writer= new PdWriter(doc, margin);
            PdParagraph par=writer.createParagraph();
            float y=writer.getLastYPosition();
            float fontHeight=par.getFont().getBoundingBox().getHeight()/1000;
            String string=String.format("Y Position: %s, Font Size: %s, Line Height: %s, Font BB Height: %s",y,par.getFontSize(),par.getLineHeight(),fontHeight);
            writer.write(par,string);
            
            PageMetadata meta=writer.getMeta();
            float x1=meta.getLowerLeftX();
            float x2=meta.getUpperRightX();
            float y1=y+par.getLineHeight();
            writer.drawHorizontalLine(1, x1, y1, x2);
            writer.drawHorizontalLine(1, x1, y, x2);
            
            doc.save(ouptut);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
