package me.Jonathon594.Mythria.Packet;

import io.netty.buffer.ByteBuf;
import me.Jonathon594.Mythria.Capability.Profile.Profile;
import me.Jonathon594.Mythria.Client.Manager.ClientManager;
import me.Jonathon594.Mythria.Enum.Consumable;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.EnumMap;
import java.util.function.Supplier;

public class SPacketUpdateConsumables {
    private final EnumMap<Consumable, Double> values;

    public SPacketUpdateConsumables(Profile profile) {
        values = profile.getConsumables();
    }

    public SPacketUpdateConsumables(PacketBuffer packetBuffer) {
        values = new EnumMap<>(Consumable.class);
        for (Consumable consumable : Consumable.values()) {
            values.put(consumable, (double) packetBuffer.readFloat()); //converted to float for performance as precision isn't necessary on client
        }
    }

    public void encode(ByteBuf buf) {
        for (Double value : values.values()) {
            buf.writeFloat(value.floatValue());
        }
    }

    public EnumMap<Consumable, Double> getValues() {
        return values;
    }

    public static void handle(SPacketUpdateConsumables msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> ClientManager.onUpdateConsumablesPacket(msg));
        contextSupplier.get().setPacketHandled(true);
    }
}
