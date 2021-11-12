package net.kunmc.lab.commondestiny;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class GlowingManager {
    public GlowingManager() {
        PairingManager pairingManager = CommonDestinyPlugin.getInstance().getPairingManager();
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(CommonDestinyPlugin.getInstance(), PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA) {
                    return;
                }
                PacketContainer packet = event.getPacket();
                Player receiver = event.getPlayer();
                Entity entity = packet.getEntityModifier(event).read(0);
                if (!(entity instanceof Player)) {
                    return;
                }
                Player glowing = (Player)entity;
                if (!pairingManager.isPair(receiver, glowing)) {
                    return;
                }
                List<WrappedWatchableObject> watchableObjectList = event.getPacket().getWatchableCollectionModifier().read(0);
                for (WrappedWatchableObject metadata : watchableObjectList) {
                    if (metadata.getIndex() == 0) {
                        byte value = (byte)metadata.getValue();
                        if ((value & 0x40) == 0) {
                            packet.getIntegers().write(0, glowing.getEntityId());
                            WrappedDataWatcher watcher = new WrappedDataWatcher();
                            watcher.setEntity(glowing);
                            watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte)(value | 0x40));
                            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
                            event.setPacket(packet);
                        }
                    }
                }
            }
        });
    }

    public void setGlowing(Player receiver, Player glowing, boolean enabled) {
        try {
            PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            container.getIntegers().write(0, glowing.getEntityId());
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setEntity(glowing);
            watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte)(enabled ? 0x40 : 0x00));
            container.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
            ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, container);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
