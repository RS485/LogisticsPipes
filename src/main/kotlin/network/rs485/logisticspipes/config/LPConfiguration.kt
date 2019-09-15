/*
 * Copyright (c) 2019  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2019  RS485
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

package network.rs485.logisticspipes.config

import therealfarfetchd.commoncfg.api.CommonCfgApi
import therealfarfetchd.commoncfg.api.cmds.CommandInitializer
import therealfarfetchd.commoncfg.api.cmds.provide

object LPConfiguration {

    var pipeDetectionLength = 50
    var pipeDetectionCount = 100
    var pipeDetectionFrequency = 20 * 30
    var itemCountInvertWheel = false
    var itemListInvertWheel = false
    var maxUnroutedConnections = 32

    var hudRenderDistance = 15

    var pipeDurability = 0.25f // TODO

    var enableParticleFx = true

    var displayRequestPopup = true

    var threads = 4
    var threadPriority = Thread.NORM_PRIORITY

    var powerUsageMultiplier = 1.0
    var logisticsCraftingTablePowerUsage = 250

    var checkForUpdates = true

    var opaquePipes = false

}

class ClientConfigurationInit : CommandInitializer {

    override fun onInitialize(api: CommonCfgApi.Mutable) {
        val file = "logisticspipes"

        with(api.cvarRegistry) {
            provide("lp_item_count_invwheel", LPConfiguration::itemCountInvertWheel, file)
            provide("lp_item_list_invwheel", LPConfiguration::itemListInvertWheel, file)
            provide("lp_request_popup", LPConfiguration::displayRequestPopup, file)
            provide("lp_hud_range", LPConfiguration::hudRenderDistance, file)
            provide("lp_particles", LPConfiguration::enableParticleFx, file)
            provide("lp_opaque_pipes", LPConfiguration::opaquePipes, file)
        }
    }

}

class CommonConfigurationInit : CommandInitializer {

    override fun onInitialize(api: CommonCfgApi.Mutable) {
        val file = "logisticspipes"

        with(api.cvarRegistry) {
            provide("lp_scan_length", LPConfiguration::pipeDetectionLength, file)
            provide("lp_scan_count", LPConfiguration::pipeDetectionCount, file)
            provide("lp_scan_freq", LPConfiguration::pipeDetectionFrequency, file)
            provide("lp_scan_max_unrouted", LPConfiguration::maxUnroutedConnections, file)
            provide("lp_threads", LPConfiguration::threads, file)
            provide("lp_threads_prio", LPConfiguration::threadPriority, file)
            provide("lp_pipe_durability", LPConfiguration::pipeDurability, file)
            provide("lp_power_usage_mul", LPConfiguration::powerUsageMultiplier, file)
            provide("lp_craft_power_usage", LPConfiguration::logisticsCraftingTablePowerUsage, file)
            provide("lp_update_check", LPConfiguration::checkForUpdates, file)
        }
    }

}