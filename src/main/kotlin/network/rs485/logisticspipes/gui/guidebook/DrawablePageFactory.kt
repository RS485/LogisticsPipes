/*
 * Copyright (c) 2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2021  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.gui.guidebook

import logisticspipes.LogisticsPipes
import network.rs485.logisticspipes.gui.guidebook.Drawable.Companion.createParent
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.guidebook.PageInfoProvider
import network.rs485.logisticspipes.guidebook.YamlPageMetadata
import network.rs485.markdown.*
import java.lang.Double.min

private typealias DrawableWordMap<T> = (List<DrawableWord>) -> T

object DrawablePageFactory {
    /**
     * Calculates the scale from level 1 (2.0), reducing 0.2 for each level with a minimum of 1.0.
     */
    private fun getScaleFromLevel(headerLevel: Int): Double = min(1.0, 2.0 - ((headerLevel - 1) / 5.0))

    fun createDrawablePage(page: PageInfoProvider): DrawablePage =
        createDrawableParagraphs(page).let { it.createParent { DrawablePage(it) } }

    private fun <T : DrawableParagraph> createDrawableParagraph(paragraphConstructor: DrawableWordMap<T>, elements: List<InlineElement>, scale: Double) =
        defaultDrawableState.copy().let { state ->
            elements.mapNotNull { element ->
                element.changeDrawableState(state)
                when (element) {
                    is LinkWord -> TODO()
                    is Word -> DrawableWord(element.str, scale, state)
                    is Space -> DrawableSpace(scale, state)
                    Break -> DrawableBreak
                    else -> null
                }
            }
        }.let { drawableWords ->
            drawableWords.createParent { paragraphConstructor(drawableWords) }
        }

    private fun createDrawableParagraphs(page: PageInfoProvider): List<DrawableParagraph> =
        page.paragraphs.map { paragraph ->
            when (paragraph) {
                is RegularParagraph -> createDrawableParagraph(
                    paragraphConstructor = ::DrawableRegularParagraph,
                    elements = paragraph.elements,
                    scale = 1.0
                )
                is HeaderParagraph -> createDrawableParagraph(
                    paragraphConstructor = ::DrawableHeaderParagraph,
                    elements = paragraph.elements,
                    scale = getScaleFromLevel(paragraph.headerLevel)
                )
                is HorizontalLineParagraph -> DrawableHorizontalLine(2)
                is MenuParagraph -> createDrawableParagraph(
                    paragraphConstructor = { drawableMenuTitle ->
                        createDrawableMenuParagraph(page.metadata, paragraph, drawableMenuTitle)
                    },
                    elements = MarkdownParser.splitSpacesAndWords(paragraph.description),
                    scale = getScaleFromLevel(3)
                )
                is ImageParagraph -> TODO()
            }
        }

    private fun createDrawableMenuParagraph(
        pageMetadata: YamlPageMetadata,
        paragraph: MenuParagraph,
        drawableMenuTitle: List<DrawableWord>
    ) = (pageMetadata.menu[paragraph.link] ?: error("Requested menu ${paragraph.link}, not found.")).map { (groupTitle: String, groupEntries: List<String>) ->
        createDrawableParagraph(
            paragraphConstructor = { drawableGroupTitle -> createDrawableMenuTileGroup(groupEntries, drawableGroupTitle) },
            elements = MarkdownParser.splitSpacesAndWords(groupTitle),
            scale = getScaleFromLevel(6)
        )
    }.let { drawableMenuGroups ->
        drawableMenuGroups.createParent { DrawableMenuParagraph(drawableMenuTitle, drawableMenuGroups) }
    }

    private fun createDrawableMenuTileGroup(menuGroupEntries: List<String>, drawableGroupTitle: List<DrawableWord>) =
        menuGroupEntries.map { path ->
            BookContents.get(path).metadata.let { metadata ->
                DrawableMenuTile(metadata.title, metadata.icon, onClick = {
                    LogisticsPipes.log.info("You tried to open $path! $it")
                })
            }
        }.let { drawableMenuTiles ->
            drawableMenuTiles.createParent { DrawableMenuTileGroup(drawableGroupTitle, drawableMenuTiles) }
        }
}
