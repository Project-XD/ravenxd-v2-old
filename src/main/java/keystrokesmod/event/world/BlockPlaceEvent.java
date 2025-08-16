package keystrokesmod.event.world;

import keystrokesmod.eventbus.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class BlockPlaceEvent extends Event {
    private final EntityPlayer player;
    private final BlockPos pos;
    private final IBlockState state;
}
