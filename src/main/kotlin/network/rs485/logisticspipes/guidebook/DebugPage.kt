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

package network.rs485.logisticspipes.guidebook

import logisticspipes.utils.MinecraftColor
import network.rs485.markdown.*
import java.util.*
import kotlin.random.Random

object DebugPage : PageInfoProvider {
    private fun randomColor(): Int = MinecraftColor.values()[Random.nextInt(MinecraftColor.values().size)].colorCode

    const val FILE = "/debug/debug_page.md"

    override val bookmarkable: Boolean = false
    override val fileLocation = FILE
    override val language = "en_us"
    override val metadata = YamlPageMetadata(
        title = "Debug Page",
        icon = "logisticspipes:itemcard",
        menu = mapOf(
            "listed" to mapOf(
                "Guides:" to listOf(
                    "/guides/quickstart_guide.md",
                    "/guides/start_guide.md",
                    "/guides/intermediate_guide.md",
                    "/guides/advanced_guide.md",
                    "/guides/in_depth.md",
                    "/guides/not_found.md",
                )
            )
        )
    )
    override val paragraphs: List<Paragraph> = listOf(
        MenuParagraph(
            description = "A listed menu.",
            link = "listed",
            type = MenuParagraphType.LIST,
        ),
        MenuParagraph(
            description = "A menu.",
            link = "listed",
            type = MenuParagraphType.TILE,
        ),
        ImageParagraph(
            alternative = "This image failed loading",
            imagePath = "/guides/test_image.png",
        ),
        HeaderParagraph(
            listOf(
                listOf(TextFormatting(EnumSet.of(TextFormat.Italic, TextFormat.Shadow))),
                MarkdownParser.splitSpacesAndWords(
                    "Nulla faucibus cursus bibendum."
                ).map {
                    if (it is Word) listOf(ColorFormatting(randomColor()), it) else listOf(it)
                }.flatten(),
            ).flatten(), headerLevel = 4
        ),
        RegularParagraph(
            listOf(
                listOf(TextFormatting(EnumSet.of(TextFormat.Bold, TextFormat.Shadow))),
                listOf(LinkFormatting(PageLink("/cantfindme.md")), Word("Hello"), Space, Word("World"), LinkFormatting(null), Break),
                listOf(ColorFormatting(randomColor())),
                MarkdownParser.splitSpacesAndWords(
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris vel sapien nisl."
                ),
            ).flatten()
        ),
        RegularParagraph(
            listOf(
                listOf(ColorFormatting(randomColor())),
                listOf(TextFormatting(EnumSet.of(TextFormat.Bold, TextFormat.Italic))),
                MarkdownParser.splitWhitespaceCharactersAndWords(
                    "Phasellus ut ipsum quis metus rutrum tempus eget in lacus. Nam at sollicitudin massa. Curabitur fringilla nisl ut quam lacinia, vel laoreet leo placerat. Aliquam erat volutpat. Nulla faucibus cursus bibendum.  \n" +
                            "Etiam porttitor sed nulla vitae vehicula. Mauris nec dolor ipsum. In eget leo malesuada, faucibus turpis a, convallis neque."
                ),
            ).flatten()
        ),
        HeaderParagraph(
            listOf(
                listOf(TextFormatting(EnumSet.of(TextFormat.Strikethrough))),
                MarkdownParser.splitSpacesAndWords(
                    "Nulla faucibus cursus bibendum."
                ).map {
                    if (it is Word) listOf(ColorFormatting(randomColor()), it) else listOf(it)
                }.flatten(),
            ).flatten(), headerLevel = 4
        ),
        RegularParagraph(
            listOf(
                listOf(TextFormatting(EnumSet.of(TextFormat.Underline, TextFormat.Shadow))),
                listOf(ColorFormatting(randomColor())),
                MarkdownParser.splitSpacesAndWords(
                    "Cras sit amet nisi velit. Etiam vitae elit quis ipsum rhoncus facilisis et ac ante."
                ),
            ).flatten(),
        ),
        RegularParagraph(
            listOf(
                listOf(TextFormatting(EnumSet.of(TextFormat.Underline, TextFormat.Italic))),
                listOf(ColorFormatting(randomColor())),
                MarkdownParser.splitWhitespaceCharactersAndWords(
                    "Phasellus ut ipsum quis metus rutrum tempus eget in lacus. Nam at sollicitudin massa.\n" +
                            "Curabitur fringilla nisl ut quam lacinia, vel laoreet leo placerat. Aliquam erat volutpat. Nulla faucibus cursus bibendum.\n" +
                            "Etiam porttitor sed nulla vitae vehicula. Mauris nec dolor ipsum. In eget leo malesuada, faucibus turpis a, convallis neque."
                ),
            ).flatten(),
        ),
    )
}
