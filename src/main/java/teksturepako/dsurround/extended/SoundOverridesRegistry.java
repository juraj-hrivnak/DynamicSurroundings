package teksturepako.dsurround.extended;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.blockstate.BlockStateData;
import org.orecruncher.dsurround.registry.blockstate.BlockStateMatcher;
import org.orecruncher.dsurround.registry.blockstate.BlockStateProfile;
import org.orecruncher.dsurround.registry.blockstate.BlockStateUtil;
import org.orecruncher.dsurround.registry.config.models.ModConfiguration;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * EXTENDED
 * <p>
 * Sound overrides are invoked last because they depend on other registries.
 */
public class SoundOverridesRegistry extends Registry {

    private Map<BlockStateMatcher, BlockStateProfile> registry;

    public SoundOverridesRegistry() {
        super("Sound Overrides Registry");
    }

    @Override
    protected void preInit() {
        this.registry = new Object2ObjectOpenHashMap<>();

        // Wipe out any cached data
        getBlockStates().forEach(state -> BlockStateUtil.setStateData(state, null));
        BlockStateUtil.setStateData(Blocks.AIR.getDefaultState(), BlockStateData.DEFAULT);
    }

    private Stream<IBlockState> getBlockStates() {
        //@formatter:off
        return StreamSupport.stream(ForgeRegistries.BLOCKS.spliterator(), false)
                .map(block -> block.getBlockState().getValidStates())
                .flatMap(Collection::stream);
        //@formatter:on
    }

    @Override
    protected void init(@Nonnull ModConfiguration cfg) {
    }

}
