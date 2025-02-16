/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.secret;

import kr.syeyoung.dungeonsguide.events.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.KeybindPressedListener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

public class FeatureBloodRush extends SimpleFeature implements KeybindPressedListener {
    public FeatureBloodRush() {
        super("Dungeon Secrets.Blood Rush", "Blood Rush Mode", "Auto pathfind to witherdoors. \nCan be toggled with key set in settings", "secret.bloodrush", false);
        this.parameters.put("key", new FeatureParameter<Integer>("key", "Key", "Press to toggle Blood Rush", Keyboard.KEY_NONE, "keybind"));
    }

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyBindPressedEvent) {
        if (keyBindPressedEvent.getKey() == this.<Integer>getParameter("key").getValue()) {
            setEnabled(!isEnabled());
            try {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fToggled Blood Rush to §e"+(FeatureRegistry.SECRET_BLOOD_RUSH.isEnabled() ? "on":"off")));
            } catch (Exception ignored) {}
        }
    }
}
