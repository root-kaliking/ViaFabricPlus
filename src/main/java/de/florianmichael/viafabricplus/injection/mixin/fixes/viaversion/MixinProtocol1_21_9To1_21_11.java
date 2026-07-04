/*
 * This file is part of ViaFabricPlus - https://github.com/FlorianMichael/ViaFabricPlus
 * Copyright (C) 2021-2024 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and RK_01/RaphiMC
 * Copyright (C) 2023-2024 contributors
 *
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.florianmichael.viafabricplus.injection.mixin.fixes.viaversion;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.Protocol1_21_9To1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ServerboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ServerboundPackets1_21_11;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Protocol1_21_9To1_21_11.class, remap = false)
public abstract class MixinProtocol1_21_9To1_21_11 extends AbstractProtocol<ClientboundPacket1_21_9, ClientboundPacket1_21_11, ServerboundPacket1_21_9, ServerboundPacket1_21_11> {

    @Inject(method = "registerPackets", at = @At("RETURN"))
    private void removeCommandHandlers(CallbackInfo ci) {
        // Don't fake acknowledgements for chat messages.
        registerClientbound(ClientboundPackets1_21_9.PLAYER_CHAT, ClientboundPackets1_21_11.PLAYER_CHAT, wrapper -> {}, true);
        registerServerbound(ServerboundPackets1_21_11.CHAT, ServerboundPackets1_21_9.CHAT, wrapper -> {}, true);

        // Directly map types, no changes are needed.
        registerServerbound(ServerboundPackets1_21_11.CHAT_COMMAND_SIGNED, ServerboundPackets1_21_9.CHAT_COMMAND, wrapper -> {}, true);
        // If the client for whatever reason sends an unsigned command, map to signed by calling game code:
        registerServerbound(ServerboundPackets1_21_11.CHAT_COMMAND, ServerboundPackets1_21_9.CHAT_COMMAND, wrapper -> {
            final String command = wrapper.read(Types.STRING);
            wrapper.cancel();
            MinecraftClient.getInstance().getNetworkHandler().sendChatCommand(command); // TODO sync to correct thread?
        }, true);

        // Don't cancel any packets we receive.
        registerServerbound(ServerboundPackets1_21_11.CHAT_ACK, ServerboundPackets1_21_9.CHAT_ACK, wrapper -> {}, true);
        registerServerbound(ServerboundPackets1_21_11.CHAT_SESSION_UPDATE, ServerboundPackets1_21_9.CHAT_SESSION_UPDATE, wrapper -> {}, true);
    }

}
