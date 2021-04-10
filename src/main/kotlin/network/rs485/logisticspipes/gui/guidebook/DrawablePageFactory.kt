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

import network.rs485.logisticspipes.gui.guidebook.Drawable.Companion.createParent
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.guidebook.PageInfoProvider
import network.rs485.logisticspipes.guidebook.toLocation
import network.rs485.markdown.*
import kotlin.math.min

private typealias DrawableWordMap<T> = (List<DrawableWord>) -> T
private typealias DrawableMenuEntryFactory<T> = (linkedPage: String, pageName: String, icon: String) -> T

object DrawablePageFactory {
    /**
     * Calculates the scale from level 1 (2.0), reducing 0.2 for each level with a minimum of 1.0.
     */
    private fun getScaleFromLevel(headerLevel: Int): Float = min(1.0f, 2.0f - ((headerLevel - 1) / 5.0f))

    fun createDrawablePage(page: PageInfoProvider): DrawablePage =
            createDrawableParagraphs(page).let { it.createParent { DrawablePage(it) } }

    private fun getLinkGroup(linkGroups: MutableMap<Link, LinkGroup>, state: InlineDrawableState) = state.link?.let { link ->
        linkGroups.getOrPut(link) { LinkGroup(link) }
    }

    private fun LinkGroup?.createChild(constructor: (linkGroup: LinkGroup?) -> DrawableWord): DrawableWord =
            constructor(this).also { this?.addChild(it) }

    private fun <T : DrawableParagraph> createDrawableParagraph(paragraphConstructor: DrawableWordMap<T>, elements: List<InlineElement>, scale: Float) =
            defaultDrawableState.copy().let { state ->
                val linkGroups = mutableMapOf<Link, LinkGroup>()
                elements.mapNotNull { element ->
                    element.changeDrawableState(state)
                    when (element) {
                        is Word -> getLinkGroup(linkGroups, state).createChild { DrawableWord(element.str, scale, state, it) }
                        is Space -> getLinkGroup(linkGroups, state).createChild { DrawableSpace(scale, state, it) }
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
                            scale = 1.0f,
                    )
                    is HeaderParagraph -> createDrawableParagraph(
                            paragraphConstructor = ::DrawableHeaderParagraph,
                            elements = paragraph.elements,
                            scale = getScaleFromLevel(paragraph.headerLevel),
                    )
                    is HorizontalLineParagraph -> DrawableHorizontalLine(2)
                    is MenuParagraph -> {
                        if (page.metadata.menu.containsKey(paragraph.link)) {
                            createDrawableParagraph(
                                    paragraphConstructor = { drawableMenuTitle ->
                                        val factory = when (paragraph.type) {
                                            MenuParagraphType.LIST -> ::DrawableMenuListEntry
                                            MenuParagraphType.TILE -> ::DrawableMenuTile
                                        }
                                        createDrawableMenuParagraph(page, page.metadata.menu[paragraph.link]!!, drawableMenuTitle, factory)
                                    },
                                    elements = MarkdownParser.splitAndFormatWords(paragraph.description),
                                    scale = getScaleFromLevel(3),
                            )
                        } else {
                            createDrawableParagraph(
                                    paragraphConstructor = ::DrawableRegularParagraph,
                                    elements = MarkdownParser.splitSpacesAndWords("Menu `${paragraph.link}` not found"),
                                    scale = 1.0f,
                            )
                        }
                    }
                    is ImageParagraph -> createDrawableParagraph(
                            paragraphConstructor = { drawableAlternativeText ->
                                val imageResource = page.resolveResource(paragraph.imagePath)
                                DrawableImageParagraph(drawableAlternativeText, DrawableImage(imageResource)).also { drawableImageParagraph ->
                                    drawableImageParagraph.image.parent = drawableImageParagraph
                                }
                            },
                            elements = MarkdownParser.splitAndFormatWords(paragraph.alternative),
                            scale = 1.0f,
                    )
                }
            }

    private fun resolvePaths(page: PageInfoProvider, groupEntries: List<String>) =
            groupEntries.map { loc -> page.resolveLocation(loc).toLocation(true) }

    private fun <T> createDrawableMenuParagraph(
            page: PageInfoProvider,
            menuStructure: Map<String, List<String>>,
            drawableMenuTitle: List<DrawableWord>,
            drawableMenuEntryConstructor: DrawableMenuEntryFactory<T>,
    ) where T : Drawable, T : MouseInteractable = menuStructure.map { (groupTitle: String, groupEntries: List<String>) ->
        createDrawableParagraph(
                paragraphConstructor = { drawableGroupTitle ->
                    createDrawableMenu(resolvePaths(page, groupEntries), drawableGroupTitle, drawableMenuEntryConstructor)
                },
                elements = MarkdownParser.splitAndFormatWords(groupTitle),
                scale = getScaleFromLevel(6),
        )
    }.let { drawableMenuGroups ->
        drawableMenuGroups.createParent { DrawableMenuParagraph(drawableMenuTitle, drawableMenuGroups) }
    }

    private fun <T> createDrawableMenu(
            menuGroupEntries: List<String>,
            drawableGroupTitle: List<DrawableWord>,
            drawableMenuEntryConstructor: DrawableMenuEntryFactory<T>,
    ) where T : Drawable, T : MouseInteractable = menuGroupEntries
            .map { path ->
                BookContents.get(path).metadata.let { metadata ->
                    drawableMenuEntryConstructor(path, metadata.title, metadata.icon)
                }
            }.let { drawableMenuTiles ->
                drawableMenuTiles.createParent { DrawableMenuGroup(drawableGroupTitle, drawableMenuTiles) }
            }
}
