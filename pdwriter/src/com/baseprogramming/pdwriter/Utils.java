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

import com.baseprogramming.pdwriter.units.PdMillimeters;
import com.baseprogramming.pdwriter.units.PdPica;
import com.baseprogramming.pdwriter.units.PdPixels;
import com.baseprogramming.pdwriter.units.PdPoints;
import com.baseprogramming.pdwriter.units.PdUnit;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;

/**
 *
 * @author Roberto C. Benitez
 */
public final class Utils
{
    public final static String DEFAULT_CSS_FILE_NAME="default-css.css";

    public static PdUnit parseDimension(String input, float fontSize, float dpi) throws NumberFormatException, RuntimeException
    {
        int sz = input.length();
        String type = input.substring(sz - 2).toLowerCase();
        float value = Float.valueOf(input.substring(0, sz - 2));
        PdUnit unit;
        if (type.equals("px"))
        {
            unit = new PdPixels(value, dpi);
        }
        else if (type.equals("mm"))
        {
            unit = new PdMillimeters(value);
        }
        else if (type.equals("pt"))
        {
            unit = new PdPoints(value);
        }
        else if (type.equals("pc"))
        {
            unit = new PdPica(value);
        }
        else if (type.equals("em"))
        {
            float size = fontSize * value;
            unit = new PdPoints(size);
        }
        else
        {
            throw new RuntimeException("Unsuported Dimension unit: " + type);
        }
        return unit;
    }

    private Utils(){}
    
    public static Map<String, Map<String, CSSValue>> createCssRuleMap(CSSStyleSheet css)
    {
        CSSRuleList rules = css.getCssRules();
        Map<String, Map<String, CSSValue>> ruleMap = new HashMap<>();
        for (int i = 0; i < rules.getLength(); i++)
        {
            if (rules.item(i) instanceof CSSStyleRule == false)
            {
                continue;
            }
            CSSStyleRule styleRule = (CSSStyleRule) rules.item(i);
            String selector = styleRule.getSelectorText();
            Map<String, CSSValue> declMap = createStyleDeclarationMap(styleRule);
            ruleMap.put(selector, declMap);
        }
        return ruleMap;
    }

    public static Map<String, CSSValue> createStyleDeclarationMap(CSSStyleRule rule)
    {
        CSSStyleDeclaration decl = rule.getStyle();
        Map<String, CSSValue> ruleMap = new HashMap<>();
        for (int j = 0; j < decl.getLength(); j++)
        {
            String prop = decl.item(j);
            CSSValue value = decl.getPropertyCSSValue(prop);
            ruleMap.put(prop, value);
        }
        return ruleMap;
    }
    
    public static File getDefaultHtmlHeadingsCssFile() throws IOException
    {
        try
        {
            ClassLoader loader=Utils.class.getClassLoader();
            return new File(loader.getResource(DEFAULT_CSS_FILE_NAME).toURI());
        }
        catch(URISyntaxException e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    public static InputStream getDefaultHtmlCssInputStream() throws IOException
    {
        ClassLoader loader = Utils.class.getClassLoader();
        return loader.getResourceAsStream(DEFAULT_CSS_FILE_NAME);

    }
    
    public static CSSStyleSheet getDefaultHtmlCss()
            throws IOException
    {
        InputStream is = getDefaultHtmlCssInputStream();
        return parseStyleSheet(is);
    }

    public static CSSStyleSheet parseStyleSheet(InputStream is) throws IOException
    {
        InputSource source = new InputSource(new InputStreamReader(is));
        CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
        return parser.parseStyleSheet(source, null, null);
    }

    public static Map<String, Map<String, CSSValue>> getDefaultHtmlCssMap() throws IOException
    {
        CSSStyleSheet css = Utils.getDefaultHtmlCss();
        Map<String, Map<String, CSSValue>> map = Utils.createCssRuleMap(css);
        return map;
    }
    
    public static Map<String, Map<String, CSSValue>> getHtmlCssMap(InputStream cssSource) throws IOException
    {
        CSSStyleSheet css=parseStyleSheet(cssSource);
        Map<String, Map<String, CSSValue>> map = Utils.createCssRuleMap(css);
        return map;
    }
        
}
