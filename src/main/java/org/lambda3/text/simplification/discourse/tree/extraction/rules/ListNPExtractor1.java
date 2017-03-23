/*
 * ==========================License-Start=============================
 * DiscourseSimplification : ExtractionRule
 *
 * Copyright © 2017 Lambda³
 *
 * GNU General Public License 3
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 * ==========================License-End==============================
 */

package org.lambda3.text.simplification.discourse.tree.extraction.rules;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import org.lambda3.text.simplification.discourse.tree.extraction.Extraction;
import org.lambda3.text.simplification.discourse.tree.extraction.ExtractionRule;
import org.lambda3.text.simplification.discourse.tree.extraction.model.CoordinationExtraction;
import org.lambda3.text.simplification.discourse.tree.extraction.utils.ListNPSplitter;
import org.lambda3.text.simplification.discourse.tree.model.Leaf;
import org.lambda3.text.simplification.discourse.utils.parseTree.ParseTreeExtractionUtils;
import org.lambda3.text.simplification.discourse.utils.words.WordsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class ListNPExtractor1 extends ExtractionRule {

    @Override
    public Optional<Extraction> extract(Tree parseTree) {

        TregexPattern p = TregexPattern.compile("ROOT <<: (S < (NP=np $.. VP))");
        TregexMatcher matcher = p.matcher(parseTree);

        while (matcher.findAt(parseTree)) {

            List<Word> precedingWords = ParseTreeExtractionUtils.getPrecedingWords(parseTree, matcher.getNode("np"), false);
            List<Word> followingWords = ParseTreeExtractionUtils.getFollowingWords(parseTree, matcher.getNode("np"), false);

            Optional<ListNPSplitter.Result> r = ListNPSplitter.splitInitList(matcher.getNode("np"));
            if (r.isPresent()) {

                // the left constituent
                List<Word> leftConstituentWords = new ArrayList<>();
                leftConstituentWords.addAll(precedingWords);
                leftConstituentWords.addAll(r.get().getElementsWords().get(0));
                leftConstituentWords.addAll(followingWords);

                Leaf leftConstituent = new Leaf(getClass().getSimpleName(), WordsUtils.wordsToProperSentenceString(leftConstituentWords));
                leftConstituent.dontAllowSplit();

                // the right constituent
                List<Word> rightConstituentWords = new ArrayList<>();
                rightConstituentWords.addAll(precedingWords);
                rightConstituentWords.addAll(r.get().getElementsWords().get(1));
                rightConstituentWords.addAll(followingWords);

                Leaf rightConstituent = new Leaf(getClass().getSimpleName(), WordsUtils.wordsToProperSentenceString(rightConstituentWords));

                Extraction res = new CoordinationExtraction(
                        getClass().getSimpleName(),
                        r.get().getRelation(),
                        null,
                        leftConstituent,
                        rightConstituent
                );

                return Optional.of(res);
            }

            r = ListNPSplitter.splitList(matcher.getNode("np"));
            if (r.isPresent()) {

                // constituents
                List<Leaf> constituents = new ArrayList<>();
                for (List<Word> element : r.get().getElementsWords()) {
                    List<Word> words = new ArrayList<Word>();
                    words.addAll(precedingWords);
                    words.addAll(element);
                    words.addAll(followingWords);

                    Leaf constituent = new Leaf(getClass().getSimpleName(), WordsUtils.wordsToProperSentenceString(words));
                    constituent.dontAllowSplit();
                    constituents.add(constituent);
                }

                Extraction res = new CoordinationExtraction(
                        getClass().getSimpleName(),
                        r.get().getRelation(),
                        constituents
                );

                return Optional.of(res);
            }
        }

        return Optional.empty();
    }
}
