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

import com.baseprogramming.dev.gen.DataFactory;
import com.baseprogramming.pdwriter.model.Borders;
import com.baseprogramming.pdwriter.model.Margin;
import com.baseprogramming.pdwriter.model.PageMetadata;
import com.baseprogramming.pdwriter.model.PdColumn;
import com.baseprogramming.pdwriter.model.PdList;
import com.baseprogramming.pdwriter.model.PdParagraph;
import com.baseprogramming.pdwriter.model.PdTable;
import com.baseprogramming.pdwriter.model.PdTableHeader;
import com.baseprogramming.pdwriter.units.PdInch;
import com.baseprogramming.pdwriter.units.PdPoints;
import com.baseprogramming.pdwriter.units.PdUnit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
     
     private void printHtmlTableWidthRandomData(int rows)
     {
          List<Map<String,Object>> data=getDataTable(50);
          
          StringBuilder html= new StringBuilder();
          
          html.append("<table border=\"1\" cellpadding=\"1\" cellspacing=\"0\">\n");
          html.append("\t<caption>Sample Table</caption>\n");
          html.append("\t\t<thead>\n");
          
          for(String name : data.get(0).keySet())
          {
              String string=String.format("\t\t\t<th>%s</th>\n",name);
              html.append(string);
          }
          
          html.append("\t\t</thead>\n");
          
          html.append("\t\t<tbody>\n");
          String rowTemplate="\t\t\t<td>%s</td>\n";
          
          for(Map<String,Object> row : data)
          {
              html.append("\t\t<tr>\n");
              row.entrySet().stream().map(e->String.format(rowTemplate,e.getValue())).forEach(e->html.append(e));
              html.append("\t\t</tr>\n");
          }
          
          html.append("\t</tbody>\n");
          
          
          html.append("</table>\n");
          System.out.println(html.toString());
          
          
     }
     
     @Test public void testGenerateHtmlTable()
     {
         printHtmlTableWidthRandomData(50);
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
        File input = new File("c:/tmp/html-input.html");
        
        File output= new File("c:/tmp/html-input.pdf");
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
    
    @Test public void testBasicDemo()
    {
        try(PDDocument pdDoc = new PDDocument())
        {
            Margin margin= new Margin(0.75f, 0.2f, 0.5f, 0.25f);
            PdWriter writer= new PdWriter(pdDoc, margin);
            
            PdParagraph heading=writer.createParagraph();
            heading.setFont(PDType1Font.TIMES_BOLD);
            heading.setFontSize(24);
            heading.setAboveSpacing(new PdInch(0.75f));
            heading.setBelowSpacing(new PdInch(0.75f));
            
            PdParagraph body = writer.createParagraph();
            body.setFirstLineIndent(new PdInch(0.3f));
            body.setBelowSpacing(new PdInch(0.17f));
            
            PdParagraph code=writer.createParagraph();
            code.setFont(PDType1Font.COURIER);
            code.setBeforeTextIndent(new PdInch(0.5f));
            code.setAboveSpacing(new PdInch(0.3f));
            code.setBelowSpacing(new PdInch(0.3f));
            
            writer.write(heading, "PdWriter Class");
            writer.write(body,"The PdWriter class (com.baseprogramming.pdwriter.PdWriter) is a class that demonstrates how to use the Apache project PDFBox.  More so, it demonstrates how PDFBox can be extended to provide a more user-friendly interface to write content to PDF--without having to worry about breaking up a large chunk of text such that it fits in a page.");
            
            writer.write(body,"This class attempts to emulate a basic word processor approach, where text is written in paragraphs(PdParagraph class), and each paragraph has settings such as font and spacing.");
            writer.write(body,"The PdWriter class has two constructors");
            writer.write(code,"public PdWriter(PDDocument document, Margin margin)");
            writer.write(body,"And");
            writer.write(code," public PdWriter(PageMetadata meta, PDDocument document)");
            
            writer.write(body, "The Margin class stores margin information (Top, Left, Bottom, and Right).  The margins are stored as a PdUnit. The concept of a PdUnit (as with all other code in this project) is an experimental concept; its goal is to provide a client with a wide range of options for units of measures.  Currently, the available units of measure are: PdInch, PdMillimeters,PdPica, PdPixels, and Points.  All units of measure convert the given value to points--the standard unit of measure in graphic systems;  the PdPoints class merely echos the value given.");
            
            writer.write(body,"The PageMetadata class has basic page information(Margin and PDRectangle), and has methods to compute page boundaries.  The default PDRectangle is PDRectangle.LETTER");
            
            writer.write(body,"To get started with the PdWriter class, create create an instance:");
            writer.write(code,"Margin margin= new Margin(0.75f, 0.2f, 0.5f, 0.25f);\n" +
                                "PdWriter writer= new PdWriter(pdDoc, margin);");
            
            writer.write(body,"Then create one (or more) PdParagraph objects:");
            writer.write(code,"PdParagraph heading=writer.createParagraph();\n" +
                                "heading.setFont(PDType1Font.TIMES_BOLD);\n" +
                                "heading.setFontSize(24);\n" +
                                "heading.setAboveSpacing(new PdInch(0.75f));\n" +
                                "heading.setBelowSpacing(new PdInch(0.75f));\n" +
                                "\n" +
                                "PdParagraph body = writer.createParagraph();\n" +
                                "body.setFirstLineIndent(new PdInch(0.3f));\n" +
                                "body.setBelowSpacing(new PdInch(0.17f));\n" +
                                "\n" +
                                "PdParagraph code=writer.createParagraph();\n" +
                                "code.setFont(PDType1Font.COURIER);\n" +
                                "code.setBeforeTextIndent(new PdInch(0.5f));\n" +
                                "code.setAboveSpacing(new PdInch(0.3f));\n" +
                                "code.setBelowSpacing(new PdInch(0.3f));");
            
            writer.write(body,"Write text (paragraphs, by calling the method PdWriter.write(PdParagraph,String):");
            writer.write(code,"writer.write(body,\"Write text (paragraphs, by calling the method PdWriter.write(PdParagraph,String):\");");
            writer.write(body,"Note that where is a write(String) method, that creates its own PdParagraph instance with the default values.");  
            
            PdList list= PdList.numeredList(writer.getMeta());
            
            writer.write(body,"It is also possible to write a numbered or bullet list--use the class PdList, which extends PdParagraph.  Use the factory method numberedList(PageMetadata) to create a numbered list paragraph style:");
            writer.write(code,"PdList list= PdList.numeredList(writer.getMeta());");
            
            writer.write(body,"Or");
            
            writer.write(code,"PdList list= PdList.bulletList(writer.getMeta());");
            
            writer.write(body,"to create a bullet list.  The line of code:");
            
            writer.write(code,"writer.write(list, \"Java\",\"C++\",\"Python\");");
            
            writer.write(body,"generates the following numbered list:");
            writer.write(list, "Java","C++","Python");
            
            writer.write(body,"A List of String can also be passed");
            
            list=PdList.bulletList(writer.getMeta());
            writer.write(body, "To create a bullet list, simple use the other factory method:");
            writer.write(code,"list=PdList.bulletList(writer.getMeta());");
            writer.write(body,"and call the PdWriter write(PdList,...) method as with the numbered list:");
            writer.write(list, "Java","C++","Python");
            
            
            pdDoc.save(new File("c:/tmp/PdWriter-Demo.pdf"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Test public void testTextFileToPdf()
    {
        String fileName="sample-text-file";
        String path = "c:/tmp/" + fileName + ".txt";
        File output= new File("c:/tmp/" + fileName +".pdf");
        
        Margin margin= new Margin(0.75f, 0.2f, 0.5f, 0.25f);
        try(PDDocument pdDoc = new PDDocument())
        {
            int parSize=30;
            int parCount=100;
            
            generateTextFile(new File(path), 30, 100);
            PdWriter writer= new PdWriter(pdDoc, margin);
            
            String string=String.format("This is a PDF file created from a randomly generated text file.  The text file has %s paragraph(s), each with %s randomly genrated words.  This example demonstrates how text is wrapped when the margin is reached (you will not from the text file that each paragraph appears as a single line of text), as well a new page started when the end of the page is reached. ", parCount,parSize);
            
            PdParagraph intro= writer.createParagraph();
            intro.setFont(PDType1Font.COURIER_OBLIQUE);
            intro.setAboveSpacing(new PdInch(0.17f));
            intro.setBelowSpacing(new PdInch(0.5f));
            
            writer.write(intro, string);
            
            PdParagraph par=writer.createParagraph();
            
            for(String line : Files.readAllLines(Paths.get(path)))
            {
                writer.write(par, line);
            }

            pdDoc.save(output);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void generateTextFile(File output, int paragraphSize, int paragraphCount) throws IOException
    {
        try(BufferedWriter writer= new BufferedWriter(new FileWriter(output)))
        {
            for(int i=0;i<paragraphCount;i++)
            {
                String line=DataFactory.genWords(paragraphSize);
                writer.write(line);
                writer.newLine();
                writer.newLine();
            }
        }
        
    }
}
