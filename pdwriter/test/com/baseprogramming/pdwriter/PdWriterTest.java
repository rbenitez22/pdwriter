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
import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
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
    
}
