package teksturepako.dsurround.extended;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.config.models.ModConfiguration;
import teksturepako.dsurround.extended.config.models.SoundOverridesConfig;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Sound overrides are invoked last because they depend on other registries.
 */
public class SoundOverridesRegistry extends Registry {



    public SoundOverridesRegistry() {
        super("Sound Overrides Registry");
    }

    @Override
    protected void preInit() {
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
        cfg.soundOverrides.forEach(this::registerSoundOverride);
    }

    public void registerSoundOverride(SoundOverridesConfig soundOverridesConfig) {

    }

    @Override
    protected void postInit() {

    }
}
