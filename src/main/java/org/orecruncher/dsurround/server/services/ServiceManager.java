/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.server.services;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.network.Network;
import org.orecruncher.dsurround.network.PacketServerData;
import org.orecruncher.dsurround.registry.RegistryManager;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import teksturepako.dsurround.extended.SoundOverridesService;

public final class ServiceManager extends Service {

	private static final ServiceManager INSTANCE = new ServiceManager();

	private final List<Service> services = new ArrayList<>();

	private ServiceManager() {
		super("ServiceManager");
	}

	private void addService(final Service service) {
		this.services.add(service);
	}

	private void clearServices() {
		this.services.clear();
	}

	private void init0() {
		for (final Service s : this.services) {
			s.init();
			MinecraftForge.EVENT_BUS.register(s);
		}
	}

	private void fini0() {
		for (final Service s : this.services) {
			s.fini();
			MinecraftForge.EVENT_BUS.unregister(s);
		}
	}

	public static void initialize() {
		INSTANCE.addService(INSTANCE);
		INSTANCE.addService(new AtmosphereService());
		INSTANCE.addService(new SpeechBubbleService());
		INSTANCE.addService(new EnvironmentService());
		///EXTENDED
		INSTANCE.addService(new SoundOverridesService());
		///EXTENDED_END
		INSTANCE.init0();
	}

	public static void deinitialize() {
		INSTANCE.fini0();
		INSTANCE.clearServices();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onWorldLoad(final WorldEvent.Load e) {
		// Tickle the Dimension Initialize so it has the
		// latest info.
		RegistryManager.DIMENSION.loading(e.getWorld());
	}

	private long tpsCount = 0;

	private static long mean(@Nonnull final long[] values) {
		long sum = 0L;
		for (final long v : values)
			sum += v;
		return sum / values.length;
	}

	/**
	 * Collect tick performance data for the loaded dimensions and broadcast to
	 * attached players.
	 *
	 * @param event Event that was triggered
	 */
    @SubscribeEvent
	public void tickEvent(@Nonnull final TickEvent.ServerTickEvent event) {
		if (!ModOptions.logging.reportServerStats || event.phase != Phase.END)
			return;

		// Spam once a second
		if ((++this.tpsCount % 20) != 0)
			return;

		final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

		final Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
		for (final Integer dim : DimensionManager.getIDs()) {
			map.put(dim.intValue(), mean(server.worldTickTimes.get(dim)) / 1000000D);
		}

		final double meanTickTime = mean(server.tickTimeArray) / 1000000D;
		final int total = (int) (Runtime.getRuntime().totalMemory() / 1024L / 1024L);
		final int max = (int) (Runtime.getRuntime().maxMemory() / 1024L / 1024L);
		final int free = (int) (Runtime.getRuntime().freeMemory() / 1024L / 1024L);

		final PacketServerData packet = new PacketServerData(map, meanTickTime, free, total, max);
		Network.sendToAll(packet);
	}
}
