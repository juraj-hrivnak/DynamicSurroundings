package teksturepako.dsurround.extended.config.models;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SoundOverridesConfig {
    @SerializedName("blocks")
    public List<String> blocks = ImmutableList.of();
    @SerializedName("sounds")
    public SoundsConfig soundsConfig = null;
}
