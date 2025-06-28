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

import java.util.Set;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.registry.config.models.BiomeConfig;
import org.orecruncher.lib.expression.BooleanValue;
import org.orecruncher.lib.expression.Expression;
import org.orecruncher.lib.expression.Function;
import org.orecruncher.lib.expression.IVariant;
import org.orecruncher.lib.expression.NumberValue;
import org.orecruncher.lib.expression.StringValue;
import org.orecruncher.lib.expression.Variant;

import com.google.common.primitives.Booleans;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class BiomeMatcher {

	public abstract boolean match(@Nonnull final BiomeInfo info);

	public static BiomeMatcher getMatcher(@Nonnull final BiomeConfig cfg) {
		if (cfg.conditions == null)
			cfg.conditions = "";
		return new ConditionsImpl(cfg);
	}

	private static class ConditionsImpl extends BiomeMatcher {

		private class BiomeTypeVariable extends Variant {

			private final BiomeDictionary.Type type;

			public BiomeTypeVariable(@Nonnull final BiomeDictionary.Type t) {
				super("biome.is" + t.getName());
				this.type = t;
			}

			@Override
			public int compareTo(IVariant o) {
				return Booleans.compare(asBoolean(), o.asBoolean());
			}

			@Override
			public float asNumber() {
				return asBoolean() ? 1 : 0;
			}

			@Override
			public String asString() {
				return Boolean.toString(asBoolean());
			}

			@Override
			public boolean asBoolean() {
				return ConditionsImpl.this.current.isBiomeType(this.type);
			}

			@Nonnull
			@Override
			public Variant add(@Nonnull IVariant term) {
				return new BooleanValue(asBoolean() || term.asBoolean());
			}
		}

		protected BiomeInfo current;
		protected final Expression exp;

		public ConditionsImpl(@Nonnull final BiomeConfig config) {
			this.exp = new Expression(config.conditions);

			// Biome name!
			this.exp.addVariable(new Variant("biome.name") {

				@Override
				public int compareTo(@Nonnull IVariant o) {
					return asString().compareTo(o.asString());
				}

				@Override
				public float asNumber() {
					return 0;
				}

				@Override
				public String asString() {
					return ConditionsImpl.this.current.getBiomeName();
				}

				@Override
				public boolean asBoolean() {
					return false;
				}

				@Nonnull
				@Override
				public IVariant add(@Nonnull IVariant term) {
					return new StringValue(asString().concat(term.asString()));
				}

			});

			this.exp.addVariable(new Variant("biome.id") {

				@Override
				public int compareTo(@Nonnull IVariant o) {
					return asString().compareTo(o.asString());
				}

				@Override
				public float asNumber() {
					return 0;
				}

				@Override
				public String asString() {
					return ConditionsImpl.this.current.getKey().toString();
				}

				@Override
				public boolean asBoolean() {
					return false;
				}

				@Nonnull
				@Override
				public IVariant add(@Nonnull IVariant term) {
					return new StringValue(asString().concat(term.asString()));
				}

			});

			this.exp.addVariable(new Variant("biome.modid") {

				@Override
				public int compareTo(@Nonnull IVariant o) {
					return asString().compareTo(o.asString());
				}

				@Override
				public float asNumber() {
					return 0;
				}

				@Override
				public String asString() {
					return ConditionsImpl.this.current.getKey().getNamespace();
				}

				@Override
				public boolean asBoolean() {
					return false;
				}

				@Nonnull
				@Override
				public IVariant add(@Nonnull IVariant term) {
					return new StringValue(asString().concat(term.asString()));
				}

			});

			this.exp.addVariable(new Variant("biome.rainfall") {
				@Override
				public int compareTo(@Nonnull IVariant o) {
					return Float.compare(asNumber(), o.asNumber());
				}

				@Override
				public float asNumber() {
					return ConditionsImpl.this.current.getRainfall();
				}

				@Override
				public String asString() {
					return Float.toString(asNumber());
				}

				@Override
				public boolean asBoolean() {
					return asNumber() != 0F;
				}

				@Nonnull
				@Override
				public IVariant add(@Nonnull IVariant term) {
					return new NumberValue(asNumber() + term.asNumber());
				}

			});

			// Fake biome
			this.exp.addVariable(new Variant("biome.isFake") {

				@Override
				public int compareTo(@Nonnull IVariant o) {
					return asString().compareTo(o.asString());
				}

				@Override
				public float asNumber() {
					return asBoolean() ? 1 : 0;
				}

				@Override
				public String asString() {
					return Boolean.toString(asBoolean());
				}

				@Override
				public boolean asBoolean() {
					return ConditionsImpl.this.current.isFake();
				}

				@Nonnull
				@Override
				public IVariant add(@Nonnull IVariant term) {
					return new BooleanValue(asBoolean() || term.asBoolean());
				}

			});

			// Scan the BiomeDictionary adding the the types
			final Set<BiomeDictionary.Type> stuff = BiomeUtil.getBiomeTypes();
			for (final BiomeDictionary.Type t : stuff)
				this.exp.addVariable(new BiomeTypeVariable(t));

			// Add the biomes in the biome list
			for (final ResourceLocation b : Biome.REGISTRY.getKeys())
				if ("minecraft".equals(b.getNamespace()))
					this.exp.addVariable(new StringValue("biomeType." + b.getPath(), b.toString()));

			// Add a function to do some biome comparisons
			this.exp.addFunction(new Function("biome.isLike", 1) {
				@Override
				public IVariant eval(final IVariant... params) {
					final String biomeName = params[0].asString();
					final Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(biomeName));
					return biome != null && ConditionsImpl.this.current.areBiomesSameClass(biome) ? Expression.TRUE
							: Expression.FALSE;
				}
			});

			// Compile it
			this.exp.getRPN();

		}

		@Override
		public boolean match(@Nonnull final BiomeInfo info) {
			this.current = info;
			return this.exp.eval().asBoolean();
		}

	}
}
