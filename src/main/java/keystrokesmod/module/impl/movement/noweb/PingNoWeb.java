package keystrokesmod.module.impl.movement.noweb;

import keystrokesmod.event.world.BlockWebEvent;
import keystrokesmod.event.network.ReceivePacketEvent;
import keystrokesmod.event.network.SendPacketEvent;
import keystrokesmod.module.impl.movement.NoWeb;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PingNoWeb extends SubMode<NoWeb> {
    private final Set<BlockPos> ignoredBlock = new HashSet<>();
    private final Queue<C0FPacketConfirmTransaction> delayedPackets = new ConcurrentLinkedQueue<>();
    private boolean delay = false;

    public PingNoWeb(String name, @NotNull NoWeb parent) {
        super(name, parent);
    }

    @EventListener
    public void onBlockWeb(@NotNull BlockWebEvent event) {
        if (ignoredBlock.contains(event.getBlockPos())) {
            event.cancel();
        }
    }

    @EventListener
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (!Utils.nullCheck() || mc.thePlayer.ticksExisted < 20) {
            onDisable();
            return;
        }

        if (event.getPacket() instanceof S23PacketBlockChange) {
            S23PacketBlockChange packet = (S23PacketBlockChange) event.getPacket();
            if (packet.getBlockState().getBlock() == Blocks.web) {
                delay = true;
                ignoredBlock.add(packet.getBlockPosition());
            }
        } else if (event.getPacket() instanceof S22PacketMultiBlockChange) {
            S22PacketMultiBlockChange packet = (S22PacketMultiBlockChange) event.getPacket();
            for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : packet.getChangedBlocks()) {
                if (changedBlock.getBlockState().getBlock() == Blocks.web) {
                    delay = true;
                    ignoredBlock.add(changedBlock.getPos());
                }
            }
        }
    }

    @EventListener
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.getPacket() instanceof C0FPacketConfirmTransaction) {
            if (delay) {
                event.cancel();
                delayedPackets.add(((C0FPacketConfirmTransaction) event.getPacket()));
            }
        }
    }

    @Override
    public void onDisable() {
        if (delay) {
            for (C0FPacketConfirmTransaction p : delayedPackets) {
                PacketUtils.sendPacket(p);
            }
        }
        delayedPackets.clear();
        ignoredBlock.clear();
        delay = false;
    }
}
