package keystrokesmod.event.world;


import keystrokesmod.eventbus.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

@Getter
@AllArgsConstructor
public class BlockWebEvent extends CancellableEvent {
    private final BlockPos blockPos;
    private final IBlockState blockState;
}
