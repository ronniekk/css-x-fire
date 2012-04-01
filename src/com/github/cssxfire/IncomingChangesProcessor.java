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

package com.github.cssxfire;

import com.github.cssxfire.tree.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class IncomingChangesProcessor {
    private static final Logger LOG = Logger.getInstance(IncomingChangesProcessor.class.getName());

    private final Project project;
    private final FirebugChangesBean changesBean;

    private IncomingChangesProcessor(Project project, FirebugChangesBean changesBean) {
        this.project = project;
        this.changesBean = changesBean;
    }

    /**
     * Process files and CSS elements within the project according to the information reported by the Firebug extension.
     * The mission is to find all possible code that could be affected by the property change in Firebug CSS editor.
     *
     * @param project     the project
     * @param changesBean the changes picked up from the Firebug extension
     * @return all candidates matching the selector, media query, and filename contained in the bean
     */
    static Collection<CssDeclarationPath> getProjectCandidates(Project project, FirebugChangesBean changesBean) {
        return new IncomingChangesProcessor(project, changesBean).getCandidates();
    }

    private Collection<CssDeclarationPath> getCandidates() {
        final List<CssDeclarationPath> candidates = new ArrayList<CssDeclarationPath>();

        // find possible media query targets with its own processor
        Set<CssMediumList> mediaCandidates = findCandidateMediaLists();

        // find possible file targets with its own search
        Set<PsiFile> fileCandidates = findCandidateFiles();

        // search for existing selectors
        CssSelectorSearchProcessor selectorProcessor = SearchProcessorCache.getInstance(project).getSelectorSearchProcessor(changesBean.getSelector());
        CssBlock[] cssBlocks = selectorProcessor.getBlocks();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Searched CSS selectors for '" + selectorProcessor.getSearchWord()
                    + "' ('" + selectorProcessor.getSelector() + "'), got " + cssBlocks.length + " results");
        }

        for (CssBlock block : cssBlocks) {
            final Ref<CssDeclaration> destination = new Ref<CssDeclaration>();
            CssUtils.processCssDeclarations(block, new PsiElementProcessor<CssDeclaration>() {
                public boolean execute(@NotNull CssDeclaration declaration) {
                    if (changesBean.getProperty().equals(declaration.getPropertyName())) {
                        destination.set(declaration);
                        return false;
                    }
                    return true;
                }
            });
            CssDeclaration existingDeclaration = destination.get();
            PsiFile file = block.getContainingFile().getOriginalFile();
            if (existingDeclaration != null) {
                // found existing declaration, possibly by resolving mixin
                candidates.add(createPath(existingDeclaration, block));
            } else {
                // non-existing - create new
                candidates.add(createNewPath(file, block));
            }

            // remove from collected files and media
            deleteCandidate(fileCandidates, file);
            deleteCandidate(mediaCandidates, CssUtils.findMediumList(block));
        }

        // add candidates from remaining media candidates
        for (CssMediumList mediaCandidate : mediaCandidates) {
            // remove from collected files
            deleteCandidate(fileCandidates, mediaCandidate.getContainingFile().getOriginalFile());

            candidates.add(createNewPath(mediaCandidate.getContainingFile().getOriginalFile(), mediaCandidate));
        }

        // add candidate paths for remaining files
        for (PsiFile fileCandidate : fileCandidates) {
            CssRulesetList rulesetList = CssUtils.findFirstCssRulesetList(fileCandidate);
            if (rulesetList != null) {
                candidates.add(createNewPath(fileCandidate, rulesetList));
            }
        }

        return candidates;
    }

    /**
     * Assembles a path for a given CSS declaration and block.
     *
     * @param declaration the declaration
     * @param block       the block
     * @return a path for an existing CSS declaration
     */
    private CssDeclarationPath createPath(CssDeclaration declaration, CssBlock block) {
        CssDeclarationNode declarationNode = new CssDeclarationNode(declaration, changesBean.getValue(), changesBean.isDeleted(), changesBean.isImportant());
        CssSelectorNode selectorNode = new CssSelectorNode(changesBean.getSelector(), block);
        PsiFile file = declaration.getContainingFile().getOriginalFile();
        CssFileNode fileNode = new CssFileNode(file);
        CssDirectoryNode directoryNode = new CssDirectoryNode(file.getParent());

        return new CssDeclarationPath(directoryNode, fileNode, selectorNode, declarationNode);
    }

    /**
     * Assembles a declaration path for a file and destination (anchor) psi element
     *
     * @param file               the file
     * @param destinationElement the anchor
     * @return a path for a non-existing CSS declaration
     */
    private CssDeclarationPath createNewPath(PsiFile file, CssElement destinationElement) {
        CssDeclaration declaration = CssUtils.createDeclaration(project, changesBean.getSelector(), changesBean.getProperty(), changesBean.getValue(), changesBean.isImportant());
        CssDeclarationNode declarationNode = CssNewDeclarationNode.forDestination(declaration, destinationElement, changesBean.isDeleted());
        CssSelectorNode selectorNode = new CssSelectorNode(changesBean.getSelector(), destinationElement);
        CssFileNode fileNode = new CssFileNode(file);
        CssDirectoryNode directoryNode = new CssDirectoryNode(file.getParent());

        return new CssDeclarationPath(directoryNode, fileNode, selectorNode, declarationNode);
    }

    /**
     * Searches the project for all medium lists with same query text as reported by the bean
     *
     * @return all matching media query elements
     */
    @NotNull
    private Set<CssMediumList> findCandidateMediaLists() {
        final Set<CssMediumList> elements = new HashSet<CssMediumList>();
        if (changesBean.getMedia().length() > 0) {
            CssMediaSearchProcessor mediaProcessor = SearchProcessorCache.getInstance(project).getMediaSearchProcessor(changesBean.getMedia());
            Set<CssMediumList> mediaLists = mediaProcessor.getMediaLists();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Searched CSS media for " + mediaProcessor.getSearchWord() + ", got " + mediaLists.size() + " results");
            }

            elements.addAll(mediaLists);
        }
        return elements;
    }

    /**
     * Searches the project for files with same name as reported by the bean
     *
     * @return all files matching the filename
     */
    @NotNull
    private Set<PsiFile> findCandidateFiles() {
        final Set<PsiFile> files = new HashSet<PsiFile>();
        if (changesBean.getFilename().length() > 0) {
            files.addAll(Arrays.asList(FilenameIndex.getFilesByName(project, changesBean.getFilename(), GlobalSearchScope.projectScope(project))));
        }
        return files;
    }

    /**
     * Deletes <i>object</i> from the <i>collection</i>, if <i>object</i> is not <tt>null</tt> and contained by <i>collection</i>
     *
     * @param collection the collection
     * @param object     the object to remove
     * @param <T>        the type of collection
     */
    private <T> void deleteCandidate(@NotNull Collection<T> collection, @Nullable T object) {
        if (object != null) {
            collection.remove(object);
        }
    }

}
