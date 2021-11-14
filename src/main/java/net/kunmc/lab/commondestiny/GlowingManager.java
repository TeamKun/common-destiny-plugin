package net.kunmc.lab.commondestiny;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class GlowingManager {
    public GlowingManager(PairingManager pairingManager) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(CommonDestinyPlugin.getInstance(), PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player receiver = event.getPlayer();
                Entity entity = packet.getEntityModifier(event).read(0);
                if (!(entity instanceof Player) || !pairingManager.isPair(receiver, (Player)entity)) {
                    return;
                }
                Player glowing = (Player)entity;
                List<WrappedWatchableObject> watchableObjects = event.getPacket().getWatchableCollectionModifier().read(0);
                for (int i = 0; i < watchableObjects.size(); i++) {
                    WrappedWatchableObject metadata = watchableObjects.get(i);
                    if (metadata.getIndex() != 0) {
                        continue;
                    }
                    byte value = (byte)metadata.getValue();
                    if ((value & 0x40) == 0) {
                        WrappedDataWatcher watcher = new WrappedDataWatcher();
                        watcher.setEntity(glowing);
                        watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte)(value | 0x40));
                        WrappedWatchableObject watchableObject = watcher.getWatchableObjects().get(0);
                        watchableObjects.remove(i);
                        watchableObjects.add(watchableObject);
                        packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
                        packet.getEntityModifier(event).write(0, glowing);
                        packet.getWatchableCollectionModifier().write(0, watchableObjects);
                        event.setPacket(packet);
                        break;
                    }
                }
            }
        });
    }

    public void setGlowing(Player receiver, Player glowing, boolean enabled) {
        try {
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setEntity(glowing);
            watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte)(enabled ? 0x40 : 0x00));
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, glowing.getEntityId());
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
            ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, packet);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
