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

package com.googlecode.cssxfire;

import com.googlecode.cssxfire.tree.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.css.CssBlock;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssRulesetList;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class IncomingChangesProcessor
{
    private final Project project;
    private final FirebugChangesBean changesBean;
    private final GlobalSearchScope searchScope;
    private final short searchContext = UsageSearchContext.ANY;

    private IncomingChangesProcessor(Project project, FirebugChangesBean changesBean)
    {
        this.project = project;
        this.changesBean = changesBean;
        this.searchScope = GlobalSearchScope.projectScope(project);
    }

    /**
     * Process files and CSS elements within the project according to the information reported by the Firebug extension.
     * The mission is to find all possible code that could be affected by the property change in Firebug CSS editor.
     * @param project the project
     * @param changesBean the changes picked up from the Firebug extension
     * @return all candidates matching the selector, media query, and filename contained in the bean
     */
    static Collection<CssDeclarationPath> getProjectCandidates(Project project, FirebugChangesBean changesBean)
    {
        return new IncomingChangesProcessor(project, changesBean).getCandidates();
    }

    private Collection<CssDeclarationPath> getCandidates()
    {
        final List<CssDeclarationPath> candidates = new ArrayList<CssDeclarationPath>();

        // find possible media query targets with its own processor
        Set<PsiElement> mediaCandidates = findCandidateMediaLists();

        // find possible file targets with its own search
        Set<PsiFile> fileCandidates = findCandidateFiles();

        // search for existing selectors
        CssSelectorSearchProcessor selectorProcessor = new CssSelectorSearchProcessor(changesBean.getSelector());
        PsiSearchHelper helper = PsiManager.getInstance(project).getSearchHelper();
        helper.processElementsWithWord(selectorProcessor, searchScope, selectorProcessor.getSearchWord(), searchContext, true);

        for (CssBlock block : selectorProcessor.getBlocks())
        {
            boolean hasDeclaration = false;
            CssDeclaration[] declarations = PsiTreeUtil.getChildrenOfType(block, CssDeclaration.class);
            if (declarations != null)
            {
                for (CssDeclaration declaration : declarations)
                {
                    if (changesBean.getProperty().equals(declaration.getPropertyName()))
                    {
                        hasDeclaration = true;

                        candidates.add(createPath(declaration, block));
                    }
                }
            }
            PsiFile file = block.getContainingFile().getOriginalFile();
            if (!hasDeclaration)
            {
                // non-existing - create new
                candidates.add(createNewPath(file, block));
            }

            // remove from collected files and media
            deleteCandidate(fileCandidates, file);
            deleteCandidate(mediaCandidates, CssUtil.getMediumList(block));
        }

        // add candidates from remaining media candidates
        for (PsiElement mediaCandidate : mediaCandidates)
        {
            // remove from collected files
            deleteCandidate(fileCandidates, mediaCandidate.getContainingFile().getOriginalFile());

            candidates.add(createNewPath(mediaCandidate.getContainingFile().getOriginalFile(), mediaCandidate));
        }

        // add candidate paths for remaining files
        for (PsiFile fileCandidate : fileCandidates)
        {
            CssRulesetList rulesetList = CssUtils.findFirstCssRulesetList(fileCandidate);
            if (rulesetList != null)
            {
                candidates.add(createNewPath(fileCandidate, rulesetList));
            }
        }

        return candidates;
    }

    /**
     * Assembles a path for a given CSS declaration and block.
     * @param declaration the declaration
     * @param block the block
     * @return a path for an existing CSS declaration
     */
    private CssDeclarationPath createPath(CssDeclaration declaration, CssBlock block)
    {
        CssDeclarationNode declarationNode = new CssDeclarationNode(declaration, changesBean.getValue(), changesBean.isDeleted());
        CssSelectorNode selectorNode = new CssSelectorNode(changesBean.getSelector(), block);
        CssFileNode fileNode = new CssFileNode(declaration.getContainingFile().getOriginalFile());

        return new CssDeclarationPath(fileNode, selectorNode, declarationNode);
    }

    /**
     * Assembles a declaration path for a file and destination (anchor) psi element
     * @param file the file
     * @param destinationElement the anchor
     * @return a path for a non-existing CSS declaration
     */
    private CssDeclarationPath createNewPath(PsiFile file, PsiElement destinationElement)
    {
        CssDeclaration declaration = CssUtils.createDeclaration(project, changesBean.getSelector(), changesBean.getProperty(), changesBean.getValue());
        CssDeclarationNode declarationNode = CssNewDeclarationNode.forDestination(declaration, destinationElement, changesBean.isDeleted());
        CssSelectorNode selectorNode = new CssSelectorNode(changesBean.getSelector(), destinationElement);
        CssFileNode fileNode = new CssFileNode(file);

        return new CssDeclarationPath(fileNode, selectorNode, declarationNode);
    }

    /**
     * Searches the project for all medium lists with same query text as reported by the bean
     * @return all matching media query elements
     */
    @NotNull
    private Set<PsiElement> findCandidateMediaLists()
    {
        final Set<PsiElement> elements = new HashSet<PsiElement>();
        if (changesBean.getMedia().length() > 0)
        {
            CssMediaSearchProcessor mediaProcessor = new CssMediaSearchProcessor(changesBean.getMedia());
            PsiSearchHelper helper = PsiManager.getInstance(project).getSearchHelper();
            helper.processElementsWithWord(mediaProcessor, searchScope, mediaProcessor.getSearchWord(), searchContext, true);
            elements.addAll(mediaProcessor.getMediaLists());
        }
        return elements;
    }

    /**
     * Searches the project for files with same name as reported by the bean
     * @return all files matching the filename
     */
    @NotNull
    private Set<PsiFile> findCandidateFiles()
    {
        final Set<PsiFile> files = new HashSet<PsiFile>();
        if (changesBean.getFilename().length() > 0)
        {
            files.addAll(Arrays.asList(FilenameIndex.getFilesByName(project, changesBean.getFilename(), searchScope)));
        }
        return files;
    }

    /**
     * Deletes <i>object</i> from the <i>collection</i>, if <i>object</i> is not <tt>null</tt> and contained by <i>collection</i>
     * @param collection the collection
     * @param object the object to remove
     * @param <T> the type of collection
     */
    private <T> void deleteCandidate(@NotNull Collection<T> collection, @Nullable T object)
    {
        if (object != null)
        {
            collection.remove(object);
        }
    }

}
