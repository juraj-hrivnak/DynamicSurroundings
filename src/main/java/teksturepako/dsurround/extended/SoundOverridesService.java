package teksturepako.dsurround.extended;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.fx.BlockEffect;
import org.orecruncher.dsurround.client.sound.ISoundInstance;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.client.sound.SoundState;
import org.orecruncher.dsurround.registry.blockstate.BlockStateData;
import org.orecruncher.dsurround.registry.blockstate.BlockStateUtil;
import org.orecruncher.dsurround.server.services.Service;
import org.orecruncher.lib.chunk.ClientChunkCache;
import org.orecruncher.lib.chunk.IBlockAccessEx;
import org.orecruncher.lib.random.XorShiftRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public final class SoundOverridesService extends Service {

    private final Random RANDOM = XorShiftRandom.current();

    public SoundOverridesService() {
        super("SoundOverridesService");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void blockBreak(BlockEvent.BreakEvent e) {
        IBlockState state = e.getState();
        BlockPos pos = e.getPos();

        final IBlockAccessEx provider = ClientChunkCache.instance();
        final BlockStateData blockStateData = BlockStateUtil.getStateData(state);

        // -- BLOCK EFFECT --

        final BlockEffect[] blockEffects = blockStateData.getEffects();

        if (blockEffects != BlockStateData.NO_EFFECTS) {
            for (final BlockEffect blockEffect : blockEffects) {
                if (blockEffect.canTrigger(provider, state, pos, RANDOM)) {
                    blockEffect.doEffect(provider, state, pos, RANDOM);
                }
            }
        }

        // -- SOUND EFFECT --

//        state.getBlock().getSoundType();

        final SoundEffect soundEffect = blockStateData.getSoundToPlay(RANDOM);

        if (soundEffect != null) {
            soundEffect.doEffect(provider, state, pos, RANDOM);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void soundEvent(PlaySoundEvent e) {
        ISound sound = e.getSound();

        ResourceLocation resourceLocation = sound.getSoundLocation();

        if (resourceLocation.equals(new ResourceLocation("minecraft:block.grass.break"))) {
            e.setResultSound(new ISoundInstance() {
                @Override
                public SoundState getState() {
                    return null;
                }

                @Override
                public void setState(@Nonnull SoundState state) {

                }

                @Override
                public void setQueue(boolean f) {

                }

                @Override
                public boolean getQueue() {
                    return false;
                }

                @Override
                public ResourceLocation getSoundLocation() {
                    return null;
                }

                @Nullable
                @Override
                public SoundEventAccessor createAccessor(SoundHandler handler) {
                    return null;
                }

                @Override
                public Sound getSound() {
                    return null;
                }

                @Override
                public SoundCategory getCategory() {
                    return null;
                }

                @Override
                public boolean canRepeat() {
                    return false;
                }

                @Override
                public int getRepeatDelay() {
                    return 0;
                }

                @Override
                public float getVolume() {
                    return 0;
                }

                @Override
                public float getPitch() {
                    return 0;
                }

                @Override
                public float getXPosF() {
                    return 0;
                }

                @Override
                public float getYPosF() {
                    return 0;
                }

                @Override
                public float getZPosF() {
                    return 0;
                }

                @Override
                public AttenuationType getAttenuationType() {
                    return null;
                }
            });
        }

        ModBase.log().warn("Sound played: [%s]", resourceLocation);
    }
}
