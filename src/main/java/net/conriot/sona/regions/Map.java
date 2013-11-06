package net.conriot.sona.regions;

import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

class Map implements Listener {
	private Plugin plugin;
	private Chunk[][][] map;
	private LinkedList<Chunk> loaded;
	
	public Map(Plugin plugin) {
		// Keep a reference to the plugin
		this.plugin = plugin;
		// Create an empty map, load chunks on demand
		this.map = new Chunk[32][32][4];
		// Create a list to store the currently loaded chunks
		this.loaded = new LinkedList<Chunk>();
		
		// Register all events
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		
		// Kick off buffering and cache validation routines
		Bukkit.getScheduler().runTaskLater(plugin, new SynchronousBuffer(), 200);
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new AsynchronousValidation(), 600);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		// Force a buffer around the player when they join
		bufferChunksForPlayer(event.getPlayer());
	}
	
	// CAN-PVP EVENTS
	@EventHandler
	public void onPlayerTakePlayerDamage(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			int x = event.getEntity().getLocation().getBlockX() / (16 * 4);
			int y = event.getEntity().getLocation().getBlockY() / (16 * 4);
			int z = event.getEntity().getLocation().getBlockZ() / (16 * 4);
			
			// Default behavior if chunk is loaded
			if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
				event.setCancelled(!(this.map[x][z][y].canPvp(event.getEntity().getLocation(), (Player)event.getEntity())));
			} else { // Default behavior if chunk not loaded
				event.setCancelled(true);
			}
		}
	}
	
	// CAN-MOVE EVENTS
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		int x = event.getTo().getBlockX() / (16 * 4);
		int y = event.getTo().getBlockY() / (16 * 4);
		int z = event.getTo().getBlockZ() / (16 * 4);
		
		// Default behavior if chunk is loaded
		if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
			if(!(this.map[x][z][y].canMove(event.getTo(), event.getPlayer()))) {
				event.setTo(event.getFrom());
				event.getPlayer().setVelocity(event.getPlayer().getVelocity().multiply(-3.0).setY(1.0));
				event.getPlayer().playEffect(event.getTo(), Effect.SMOKE, 0);
				event.getPlayer().playSound(event.getTo(), Sound.FIREWORK_BLAST, 0.5f, 2.0f);
			}
		} else { // Default behavior if chunk not loaded
			event.setCancelled(true);
		}
	}
	
	private void bufferChunksForPlayer(Player p) {
		// Extract the "Chunk" location of a player
		Location loc = p.getLocation();
		int x = loc.getBlockX() / (16 * 4);
		int y = loc.getBlockY() / (16 * 4);
		int z = loc.getBlockZ() / (16 * 4);
		
		// Attempt to load chunk player is in
		if(this.map[x][z][y] == null) {
			this.map[x][z][y] = new Chunk(x, z, y);
			this.loaded.add(map[x][z][y]);
		}
		
		// Attempt to load adjacent chunks
		if(x - 1 >= 0 && this.map[x - 1][z][y] == null) {
			this.map[x - 1][z][y] = new Chunk(x - 1, z, y);
			this.loaded.add(map[x - 1][z][y]);
		}
		if(x + 1 < 32 && this.map[x + 1][z][y] == null) {
			this.map[x + 1][z][y] = new Chunk(x + 1, z, y);
			this.loaded.add(map[x + 1][z][y]);
		}
		
		if(z - 1 >= 0 && this.map[x][z - 1][y] == null) {
			this.map[x][z - 1][y] = new Chunk(x, z - 1, y);
			this.loaded.add(map[x][z - 1][y]);
		}
		if(z + 1 < 32 && this.map[x][z + 1][y] == null) {
			this.map[x][z + 1][y] = new Chunk(x, z + 1, y);
			this.loaded.add(map[x][z + 1][y]);
		}
		
		if(y - 1 >= 0 && this.map[x][z][y - 1] == null) {
			this.map[x][z][y - 1] = new Chunk(x, z, y - 1);
			this.loaded.add(map[x][z][y - 1]);
		}
		if(y + 1 < 4 && this.map[x][z][y + 1] == null) {
			this.map[x][z][y + 1] = new Chunk(x, z, y + 1);
			this.loaded.add(map[x][z][y + 1]);
		}
	}
	
	private void bufferChunks() {
		Player[] players = Bukkit.getServer().getOnlinePlayers();
		for(int i = 0; i < players.length; i++) {
			// Extract the "Chunk" location of a player
			Location loc = players[i].getLocation();
			int x = loc.getBlockX() / (16 * 4);
			int y = loc.getBlockY() / (16 * 4);
			int z = loc.getBlockZ() / (16 * 4);
			
			// Attempt to load chunk player is in
			if(this.map[x][z][y] == null) {
				this.map[x][z][y] = new Chunk(x, z, y);
				this.loaded.add(map[x][z][y]);
			}
			
			// Attempt to load adjacent chunks
			if(x - 1 >= 0 && this.map[x - 1][z][y] == null) {
				this.map[x - 1][z][y] = new Chunk(x - 1, z, y);
				this.loaded.add(map[x - 1][z][y]);
			}
			if(x + 1 < 32 && this.map[x + 1][z][y] == null) {
				this.map[x + 1][z][y] = new Chunk(x + 1, z, y);
				this.loaded.add(map[x + 1][z][y]);
			}
			
			if(z - 1 >= 0 && this.map[x][z - 1][y] == null) {
				this.map[x][z - 1][y] = new Chunk(x, z - 1, y);
				this.loaded.add(map[x][z - 1][y]);
			}
			if(z + 1 < 32 && this.map[x][z + 1][y] == null) {
				this.map[x][z + 1][y] = new Chunk(x, z + 1, y);
				this.loaded.add(map[x][z + 1][y]);
			}
			
			if(y - 1 >= 0 && this.map[x][z][y - 1] == null) {
				this.map[x][z][y - 1] = new Chunk(x, z, y - 1);
				this.loaded.add(map[x][z][y - 1]);
			}
			if(y + 1 < 4 && this.map[x][z][y + 1] == null) {
				this.map[x][z][y + 1] = new Chunk(x, z, y + 1);
				this.loaded.add(map[x][z][y + 1]);
			}
		}
	}
	
	private void validateChunks() {
		// Create a list of all player locations
		Player[] players = Bukkit.getServer().getOnlinePlayers();
		int[] x = new int[players.length];
		int[] y = new int[players.length];
		int[] z = new int[players.length];
		for(int i = 0; i < players.length; i++) {
			// Extract the "Chunk" location of a player
			Location loc = players[i].getLocation();
			x[i] = loc.getBlockX() / (16 * 4);
			y[i] = loc.getBlockY() / (16 * 4);
			z[i] = loc.getBlockZ() / (16 * 4);
		}
		
		// Iterate over all loaded chunks, check if it meets conditions to be unloaded
		Iterator<Chunk> iter = this.loaded.iterator();
		while(iter.hasNext()) {
			Chunk c = iter.next();
			// Assume it will not be kept
			boolean keep = false;
			
			// See if it should be kept
			for(int i = 0; i < x.length; i++) {
				// Check if along x-axis
				if(c.getZ() == z[i] && c.getY() == y[i]) {
					if(c.getX() >= x[i] - 1 && c.getX() <= x[i] + 1) {
						keep = true;
						break;
					}
				}
				// Check if along z-axis
				if(c.getX() == x[i] && c.getY() == y[i]) {
					if(c.getZ() >= z[i] - 1 && c.getZ() <= z[i] + 1) {
						keep = true;
						break;
					}
				}
				// Check if along y-axis
				if(c.getX() == x[i] && c.getZ() == z[i]) {
					if(c.getY() >= y[i] - 1 && c.getY() <= y[i] + 1) {
						keep = true;
						break;
					}
				}
			}
			
			// Queue for synchronous removal if needed
			if(!keep) {
				Bukkit.getScheduler().runTaskLater(this.plugin, new SynchronousClearChunk(c.getX(), c.getY(), c.getZ()), 0);
				iter.remove();
			}
		}
	}
	
	private class SynchronousClearChunk implements Runnable {
		private int x, y, z;
		
		public SynchronousClearChunk(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		@Override
		public void run() {
			// Set the chunk to null to free it from memory
			map[x][y][z] = null;
		}
		
	}
	
	private class SynchronousBuffer implements Runnable {
		@Override
		public void run() {
			// Perform the chunk buffering routine
			bufferChunks();
			// Schedule the next chunk buffering
			Bukkit.getScheduler().runTaskLater(plugin, new SynchronousBuffer(), 200);
		}
		
	}
	
	private class AsynchronousValidation implements Runnable {
		@Override
		public void run() {
			// Perform the cache validation routine
			validateChunks();
			// Schedule the next cache validation
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new AsynchronousValidation(), 600);
		}
		
	}
}
