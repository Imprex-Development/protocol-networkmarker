package dev.imprex.protocol;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketPostAdapter;

public class NetworkMarkerTest extends JavaPlugin {

	private Logger logger = Logger.getGlobal();

	private ProtocolManager protocolManager;
	private AsynchronousManager asynchronousManager;

	@Override
	public void onEnable() {
		logger = getLogger();

		this.protocolManager = ProtocolLibrary.getProtocolManager();
		this.protocolManager
				.addPacketListener(this.new SyncPacketListener());

		this.asynchronousManager = protocolManager.getAsynchronousManager();
		this.asynchronousManager
				.registerAsyncHandler(this.new AsyncPacketListener())
				.start(1);
	}

	private class SyncPacketListener extends PacketAdapter {

		public SyncPacketListener() {
			super(NetworkMarkerTest.this, PacketType.Play.Server.CHAT);
		}

		@Override
		public void onPacketSending(PacketEvent event) {
			logger.info("listener: sync");

			event.getNetworkMarker().addPostListener(new PacketPostAdapter(this.plugin) {
				
				public void onPostEvent(PacketEvent event) {
					logger.info("post-listener: sync");
				}
			});
		}
	}

	private class AsyncPacketListener extends PacketAdapter {

		public AsyncPacketListener() {
			super(NetworkMarkerTest.this, PacketType.Play.Server.CHAT);
		}

		@Override
		public void onPacketSending(PacketEvent event) {
			logger.info("listener: async");

			event.getAsyncMarker().incrementProcessingDelay();

			event.getNetworkMarker().addPostListener(new PacketPostAdapter(this.plugin) {
				
				public void onPostEvent(PacketEvent event) {
					logger.info("post-listener: async");
				}
			});

			Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
				asynchronousManager.signalPacketTransmission(event);
			}, 60);
		}
	}
}
