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

import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

/**
 *
 * @author Roberto C. Benitez
 */
public abstract class TagScanner implements NodeVisitor
{
    private final Node node;
    private final int depth;

    public TagScanner(Node node, int depth)
    {
        this.node = node;
        this.depth = depth;
    }

    public Node getNode()
    {
        return node;
    }

    public int getDepth()
    {
        return depth;
    }
}
