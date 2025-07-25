package org.orecruncher.dsurround.server.services;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.fx.BlockEffect;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.blockstate.BlockStateData;
import org.orecruncher.dsurround.registry.blockstate.BlockStateUtil;
import org.orecruncher.lib.chunk.ClientChunkCache;
import org.orecruncher.lib.chunk.IBlockAccessEx;
import org.orecruncher.lib.random.XorShiftRandom;

import java.util.Random;

public final class BlockEventService extends Service {

    private final Random RANDOM = XorShiftRandom.current();

    BlockEventService() {
        super("BlockEventService");
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

        ModBase.log().warn("AWDADAWDAWDADWADADW state [%s]", state.toString());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void soundEvent(PlaySoundEvent e) {
        ISound sound = e.getSound();

        sound.getSoundLocation();


    }
}
