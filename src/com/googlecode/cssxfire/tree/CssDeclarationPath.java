/*
 * Copyright 2010 Ronnie Kolehmainen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.cssxfire.tree;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssDeclarationPath {
    private CssFileNode fileNode;
    private CssSelectorNode selectorNode;
    private CssDeclarationNode declarationNode;

    public CssDeclarationPath(CssFileNode fileNode, CssSelectorNode selectorNode, CssDeclarationNode declarationNode) {
        this.fileNode = fileNode;
        this.selectorNode = selectorNode;
        this.declarationNode = declarationNode;
    }

    public CssTreeNode[] getPathFromRoot() {
        return new CssTreeNode[]{fileNode, selectorNode, declarationNode};
    }

    public CssFileNode getFileNode() {
        return fileNode;
    }

    public CssSelectorNode getSelectorNode() {
        return selectorNode;
    }

    public CssDeclarationNode getDeclarationNode() {
        return declarationNode;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " ['" + fileNode.getVirtualFile()
                + "', '" + selectorNode.getSelector()
                + "', '" + declarationNode.getPropertyName() + "']";
    }
}
