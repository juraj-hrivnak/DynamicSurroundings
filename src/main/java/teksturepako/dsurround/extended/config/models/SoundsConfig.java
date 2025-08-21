package teksturepako.dsurround.extended.config.models;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import org.orecruncher.dsurround.registry.config.models.SoundConfig;

import java.util.List;

public class SoundsConfig {
    @SerializedName("step")
    public List<String> stepSounds = ImmutableList.of();
    @SerializedName("break")
    public List<SoundConfig> breakSounds = ImmutableList.of();
    @SerializedName("fall")
    public List<SoundConfig> fallSounds = ImmutableList.of();
    @SerializedName("hit")
    public List<SoundConfig> hitSounds = ImmutableList.of();
    @SerializedName("place")
    public List<SoundConfig> placeSounds = ImmutableList.of();
}
