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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.Protocol1_21_9To1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter.EntityPacketRewriter1_21_11;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(value = EntityPacketRewriter1_21_11.class, remap = false)
public abstract class MixinEntityPacketRewriter1_21_11 extends EntityRewriter<ClientboundPacket1_21_9, Protocol1_21_9To1_21_11> {

    @Shadow
    @Final
    private static UUID CREATIVE_BLOCK_INTERACTION_RANGE;

    @Shadow
    @Final
    private static UUID CREATIVE_ENTITY_INTERACTION_RANGE;

    protected MixinEntityPacketRewriter1_21_11(Protocol1_21_9To1_21_11 protocol) {
        super(protocol);
    }

    @Shadow
    protected abstract void writeAttribute(PacketWrapper wrapper, String attributeId, double base, UUID modifierId, double amount);

    /**
     * @author RK_01
     * @reason Fix interaction range and step height differences
     */
    @Overwrite
    private void sendRangeAttributes(final UserConnection connection, final boolean creativeMode) {
        final PacketWrapper updateAttributes = PacketWrapper.create(ClientboundPackets1_21_11.UPDATE_ATTRIBUTES, connection);
        updateAttributes.write(Types.VAR_INT, this.tracker(connection).clientEntityId());
        if (connection.getProtocolInfo().serverProtocolVersion().olderThanOrEqualTo(ProtocolVersion.v1_7_6)) {
            updateAttributes.write(Types.VAR_INT, 3); // Number of attributes
            this.writeAttribute(updateAttributes, "generic.step_height", 0.5D, null, 0D);
        } else {
            updateAttributes.write(Types.VAR_INT, 2); // Number of attributes
        }
        if (connection.getProtocolInfo().serverProtocolVersion().olderThan(LegacyProtocolVersion.r1_0_0tor1_0_1)) {
            this.writeAttribute(updateAttributes, "player.block_interaction_range", 4D, creativeMode ? CREATIVE_BLOCK_INTERACTION_RANGE : null, 1D);
        } else {
            this.writeAttribute(updateAttributes, "player.block_interaction_range", 4.5D, creativeMode ? CREATIVE_BLOCK_INTERACTION_RANGE : null, 0.5D);
        }
        if (connection.getProtocolInfo().serverProtocolVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_6_4)) {
            this.writeAttribute(updateAttributes, "player.entity_interaction_range", 3D, creativeMode ? CREATIVE_ENTITY_INTERACTION_RANGE : null, 3D);
        } else if (connection.getProtocolInfo().serverProtocolVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            this.writeAttribute(updateAttributes, "player.entity_interaction_range", 3D, creativeMode ? CREATIVE_ENTITY_INTERACTION_RANGE : null, 1D);
        } else {
            this.writeAttribute(updateAttributes, "player.entity_interaction_range", 3D, creativeMode ? CREATIVE_ENTITY_INTERACTION_RANGE : null, 2D);
        }
        updateAttributes.scheduleSend(Protocol1_21_9To1_21_11.class);
    }

}
