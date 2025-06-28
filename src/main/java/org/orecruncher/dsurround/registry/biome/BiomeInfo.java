/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.dsurround.registry.biome;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.capabilities.season.TemperatureRating;
import org.orecruncher.dsurround.client.handlers.BiomeSoundEffectsHandler;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.config.models.BiomeConfig;
import org.orecruncher.dsurround.registry.config.models.SoundConfig;
import org.orecruncher.dsurround.registry.config.models.SoundType;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.MyUtils;
import org.orecruncher.lib.WeightTable;
import org.orecruncher.lib.collections.ObjectArray;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BiomeInfo implements Comparable<BiomeInfo> {

	private final static float DEFAULT_FOG_DENSITY = 0.4F;
	private final static Color DEFAULT_FOG_COLOR = new Color(64, 96, 64).asImmutable();
	private final static Color DEFAULT_DUST_COLOR = new Color(255, 234, 151).asImmutable();

	public final static int DEFAULT_SPOT_CHANCE = 1000 / BiomeSoundEffectsHandler.SCAN_INTERVAL;
	public final static SoundEffect[] NO_SOUNDS = {};

	protected final IBiome biome;

	protected boolean hasPrecipitation;
	protected boolean hasDust;
	protected boolean hasAurora;
	protected boolean hasFog;

	private Color dustColor = DEFAULT_DUST_COLOR;
	private Color fogColor = DEFAULT_FOG_COLOR;
	private float fogDensity = DEFAULT_FOG_DENSITY;

	protected SoundEffect[] sounds = NO_SOUNDS;
	protected SoundEffect[] spotSounds = NO_SOUNDS;
	protected int spotSoundChance = DEFAULT_SPOT_CHANCE;

	protected final List<String> comments = Lists.newArrayList();

	protected final boolean isRiver;
	protected final boolean isOcean;
	protected final boolean isDeepOcean;
	
	protected final String traits;

	public BiomeInfo(@Nonnull final IBiome biome) {
		this.biome = biome;

		if (!isFake()) {
			this.hasPrecipitation = canRain() || getEnableSnow();
		}

		// If it is a BOP biome initialize from the BoP Biome
		// instance. May be overwritten by DS config.
		if (!biome.isFake()) {
			final Biome b = biome.getBiome();
			if (BiomeUtil.isBoPBiome(b)) {
				final int color = BiomeUtil.getBoPBiomeFogColor(b);
				if (color > 0) {
					this.hasFog = true;
					this.fogColor = new Color(color);
					this.fogDensity = BiomeUtil.getBoPBiomeFogDensity(b);
				}
			}
		}

		this.isRiver = this.biome.getTypes().contains(Type.RIVER);
		this.isOcean = this.biome.getTypes().contains(Type.OCEAN);
		this.isDeepOcean = this.isOcean && getBiomeName().matches("(?i).*deep.*ocean.*|.*abyss.*");
	
		this.traits = getBiomeTypes().stream().map(BiomeDictionary.Type::getName).collect(Collectors.joining(" "));
	}

	public boolean isRiver() {
		return this.isRiver;
	}

	public boolean isOcean() {
		return this.isOcean;
	}

	public boolean isDeepOcean() {
		return this.isDeepOcean;
	}

	public ResourceLocation getKey() {
		return this.biome.getKey();
	}

	public int getBiomeId() {
		return this.biome.getId();
	}

	public Biome getBiome() {
		return this.biome.getBiome();
	}

	public Set<Type> getBiomeTypes() {
		return this.biome.getTypes();
	}
	
	public String getBiomeTraits() {
		return this.traits;
	}

	void addComment(@Nonnull final String comment) {
		if (!StringUtils.isEmpty(comment))
			this.comments.add(comment);
	}

	public List<String> getComments() {
		return this.comments;
	}

	public String getBiomeName() {
		return this.biome.getName();
	}

	public boolean hasWeatherEffect() {
		return getHasPrecipitation() || getHasDust();
	}

	public boolean getHasPrecipitation() {
		return this.hasPrecipitation;
	}

	public boolean canRain() {
		return this.biome.canRain();
	}

	public boolean getEnableSnow() {
		return this.biome.getEnableSnow();
	}

	void setHasPrecipitation(final boolean flag) {
		this.hasPrecipitation = flag;
	}

	public boolean getHasDust() {
		return this.hasDust;
	}

	void setHasDust(final boolean flag) {
		this.hasDust = flag;
	}

	public boolean getHasAurora() {
		return this.hasAurora;
	}

	void setHasAurora(final boolean flag) {
		this.hasAurora = flag;
	}

	public boolean getHasFog() {
		return this.hasFog;
	}

	void setHasFog(final boolean flag) {
		this.hasFog = flag;
	}

	public Color getDustColor() {
		return this.dustColor;
	}

	void setDustColor(final Color color) {
		this.dustColor = color;
	}

	public Color getFogColor() {
		return this.fogColor;
	}

	void setFogColor(@Nonnull final Color color) {
		this.fogColor = color;
	}

	public float getFogDensity() {
		return this.fogDensity;
	}

	void setFogDensity(final float density) {
		this.fogDensity = density;
	}

	void setSpotSoundChance(final int chance) {
		this.spotSoundChance = chance;
	}

	void addSound(final SoundEffect sound) {
		this.sounds = MyUtils.append(this.sounds, sound);
	}

	void addSpotSound(final SoundEffect sound) {
		this.spotSounds = MyUtils.append(this.spotSounds, sound);
	}

	public boolean isFake() {
		return this.biome instanceof FakeBiome;
	}

	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return this.biome.getFloatTemperature(pos);
	}

	public float getTemperature() {
		return this.biome.getTemperature();
	}

	public TempCategory getTempCategory() {
		return this.biome.getTempCategory();
	}

	public TemperatureRating getTemperatureRating() {
		return TemperatureRating.fromTemp(getTemperature());
	}

	public boolean isHighHumidity() {
		return this.biome.isHighHumidity();
	}

	public float getRainfall() {
		return this.biome.getRainfall();
	}

	@Nonnull
	public Collection<SoundEffect> findSoundMatches() {
		return findSoundMatches(new ObjectArray<>(8));
	}

	@Nonnull
	public Collection<SoundEffect> findSoundMatches(@Nonnull final Collection<SoundEffect> results) {
		for (final SoundEffect sound : this.sounds) {
			if (sound.matches())
				results.add(sound);
		}
		return results;
	}

	@Nullable
	public SoundEffect getSpotSound(@Nonnull final Random random) {
		return this.spotSounds != NO_SOUNDS && random.nextInt(this.spotSoundChance) == 0
				? new WeightTable<>(this.spotSounds).next()
				: null;
	}

	void resetSounds() {
		this.sounds = NO_SOUNDS;
		this.spotSounds = NO_SOUNDS;
		this.spotSoundChance = DEFAULT_SPOT_CHANCE;
	}

	public boolean isBiomeType(@Nonnull final BiomeDictionary.Type type) {
		return getBiomeTypes().contains(type);
	}

	public boolean areBiomesSameClass(@Nonnull final Biome biome) {
		return BiomeUtil.areBiomesSimilar(this.biome.getBiome(), biome);
	}

	public void update(@Nonnull final BiomeConfig entry) {
		addComment(entry.comment);
		if (entry.hasPrecipitation != null)
			setHasPrecipitation(entry.hasPrecipitation);
		if (entry.hasAurora != null)
			setHasAurora(entry.hasAurora);
		if (entry.hasDust != null)
			setHasDust(entry.hasDust);
		if (entry.hasFog != null)
			setHasFog(entry.hasFog);
		if (entry.fogDensity != null)
			setFogDensity(entry.fogDensity);
		if (entry.fogColor != null) {
			final int[] rgb = MyUtils.splitToInts(entry.fogColor, ',');
			if (rgb.length == 3)
				setFogColor(new Color(rgb[0], rgb[1], rgb[2]));
		}
		if (entry.dustColor != null) {
			final int[] rgb = MyUtils.splitToInts(entry.dustColor, ',');
			if (rgb.length == 3)
				setDustColor(new Color(rgb[0], rgb[1], rgb[2]));
		}
		if (entry.soundReset != null && entry.soundReset) {
			addComment("> Sound Reset");
			resetSounds();
		}

		if (entry.spotSoundChance != null)
			setSpotSoundChance(entry.spotSoundChance);

		for (final SoundConfig soundConfig : entry.sounds) {
			if (RegistryManager.SOUND.isSoundBlocked(new ResourceLocation(soundConfig.sound)))
				continue;
			final SoundEffect.Builder b = new SoundEffect.Builder(soundConfig);
			final SoundEffect s = b.build();
			if (s.getSoundType() == SoundType.SPOT)
				addSpotSound(s);
			else
				addSound(s);
		}

	}

	@Override
	@Nonnull
	public String toString() {
		final ResourceLocation rl = this.biome.getKey();
		final String registryName = rl == null ? (isFake() ? "FAKE" : "UNKNOWN") : rl.toString();

		final StringBuilder builder = new StringBuilder();
		builder.append("Biome [").append(getBiomeName()).append('/').append(registryName).append("] (")
				.append(getBiomeId()).append("):");
		if (!isFake()) {
			builder.append("\n+ ").append('<');
			builder.append(getBiomeTraits());
			builder.append('>').append('\n');
			builder.append("+ temp: ").append(getTemperature()).append(" (").append(getTemperatureRating().getValue())
					.append(")");
			builder.append(" rain: ").append(getRainfall());
		}

		if (this.hasPrecipitation)
			builder.append(" PRECIPITATION");
		if (this.hasDust)
			builder.append(" DUST");
		if (this.hasAurora)
			builder.append(" AURORA");
		if (this.hasFog)
			builder.append(" FOG");
		if (this.hasDust && this.dustColor != null)
			builder.append(" dustColor:").append(this.dustColor.toString());
		if (this.hasFog && this.fogColor != null) {
			builder.append(" fogColor:").append(this.fogColor.toString());
			builder.append(" fogDensity:").append(this.fogDensity);
		}

		if (this.sounds.length > 0) {
			builder.append("\n+ sounds [\n");
			builder.append(
					Arrays.stream(this.sounds).map(c -> "+   " + c.toString()).collect(Collectors.joining("\n")));
			builder.append("\n+ ]");
		}

		if (this.spotSounds.length > 0) {
			builder.append("\n+ spot sound chance:").append(this.spotSoundChance);
			builder.append("\n+ spot sounds [\n");
			builder.append(
					Arrays.stream(this.spotSounds).map(c -> "+   " + c.toString()).collect(Collectors.joining("\n")));
			builder.append("\n+ ]");
		}

		if (this.comments.size() > 0) {
			builder.append("\n+ comments:\n");
			builder.append(this.comments.stream().map(c -> "+   " + c).collect(Collectors.joining("\n")));
			builder.append('\n');
		}

		return builder.toString();
	}

	@Override
	public int compareTo(@Nonnull final BiomeInfo o) {
		return getBiomeName().compareTo(o.getBiomeName());
	}
}
