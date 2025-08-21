package teksturepako.dsurround.extended;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.registry.blockstate.BlockStateUtil;
import org.orecruncher.dsurround.server.services.Service;
import org.orecruncher.lib.random.XorShiftRandom;

import java.util.Random;

public final class SoundOverridesService extends Service {

    private final Random RANDOM = XorShiftRandom.current();

    public SoundOverridesService() {
        super("SoundOverridesService");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void soundEvent(PlaySoundEvent e) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.isGamePaused() || mc.world == null || mc.player == null) return;

        EntityPlayerSP player = mc.player;
        World world = mc.world;

        ISound sound = e.getSound();
        BlockPos soundBlockPos = getBlockPosOfSound(sound);

        IBlockState blockState = world.getBlockState(soundBlockPos);
        BlockStateUtil.getStateData(blockState);
        @SuppressWarnings("deprecation") SoundType soundType = blockState.getBlock().getSoundType();

        ResourceLocation resourceLocation = sound.getSoundLocation();

        if (resourceLocation.equals(new ResourceLocation("minecraft:block.grass.break"))) {
            e.setResultSound(null);
        }

        ModBase.log().warn("Sound played: [%s]", resourceLocation);
    }

    private BlockPos getBlockPosOfSound(ISound sound) {
        final float soundX = sound.getXPosF();
        final float soundY = sound.getYPosF();
        final float soundZ = sound.getZPosF();

        return new BlockPos(MathHelper.floor(soundX), MathHelper.floor(soundY - 0.01F), MathHelper.floor(soundZ));
    }

}
