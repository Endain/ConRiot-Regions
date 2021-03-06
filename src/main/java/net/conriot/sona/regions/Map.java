package net.conriot.sona.regions;

import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

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
			// Ignore if damager is OP, they can always hit people
			if(((Player)event.getDamager()).isOp())
				return;
			
			// Ignore if out of the world
			if(isOutsideWorld(event.getEntity().getLocation()))
				return;
			
			// Get damaged player's coords
			int x = event.getEntity().getLocation().getBlockX() / (16 * 4);
			int y = event.getEntity().getLocation().getBlockY() / (16 * 4);
			int z = event.getEntity().getLocation().getBlockZ() / (16 * 4);
			// Get attacking player's coords
			int x2 = event.getEntity().getLocation().getBlockX() / (16 * 4);
			int y2 = event.getEntity().getLocation().getBlockY() / (16 * 4);
			int z2 = event.getEntity().getLocation().getBlockZ() / (16 * 4);
			
			// Behavior if chunk is loaded
			if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
				// Check if player in pvp zone
				if(!this.map[x][z][y].canPvp(event.getEntity().getLocation(), (Player)event.getEntity())) {
					// Check if attacker in pvp zone
					if(!this.map[x2][z2][y2].canPvp(event.getDamager().getLocation(), (Player)event.getDamager())) {
						event.setCancelled(true);
					}
				}
			} else { // Default behavior if chunk not loaded
				event.setCancelled(true);
			}
		}
	}
	
	// CAN-MOVE EVENTS
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		// Ignore if player is OP
		if(event.getPlayer().isOp())
			return;
		
		// Ignore if out of the world
		if(isOutsideWorld(event.getTo()))
			return;
				
		int x = event.getTo().getBlockX() / (16 * 4);
		int y = event.getTo().getBlockY() / (16 * 4);
		int z = event.getTo().getBlockZ() / (16 * 4);
		
		// Behavior if chunk is loaded
		if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
			if(!this.map[x][z][y].canMove(event.getTo(), event.getPlayer())) {
				Location l = event.getFrom().clone();
				l.setPitch(event.getTo().getPitch());
				l.setYaw(event.getTo().getYaw());
				Vector v = event.getTo().toVector().subtract(event.getFrom().toVector()).multiply(-2.5).setY(0.175);

				event.setTo(l);
				event.getPlayer().setVelocity(v);
				event.getPlayer().playEffect(event.getTo(), Effect.MOBSPAWNER_FLAMES, 0);
				event.getPlayer().playSound(event.getTo(), Sound.FIREWORK_BLAST, 0.5f, 1.75f);
			}
		} else { // Default behavior if chunk not loaded
			event.setCancelled(true);
		}
	}
	
	// CAN-CHAT EVENTS
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		// Ignore if player is OP
		if(event.getPlayer().isOp())
			return;
		
		// Ignore if out of the world
		if(isOutsideWorld(event.getPlayer().getLocation()))
			return;
		
		int x = event.getPlayer().getLocation().getBlockX() / (16 * 4);
		int y = event.getPlayer().getLocation().getBlockY() / (16 * 4);
		int z = event.getPlayer().getLocation().getBlockZ() / (16 * 4);
		
		// Behavior if chunk is loaded
		if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
			if(!this.map[x][z][y].canChat(event.getPlayer().getLocation(), event.getPlayer())) {
				event.setCancelled(true);
			}
		} else { // Default behavior if chunk not loaded
			event.setCancelled(true);
		}
	}
	
	// CAN-BUILD EVENTS
	@EventHandler
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		// Ignore if player is OP
		if(event.getPlayer().isOp())
			return;
		
		// Ignore if out of the world
		if(isOutsideWorld(event.getBlock().getLocation()))
			return;
		
		int x = event.getBlock().getLocation().getBlockX() / (16 * 4);
		int y = event.getBlock().getLocation().getBlockY() / (16 * 4);
		int z = event.getBlock().getLocation().getBlockZ() / (16 * 4);
		
		// Behavior if chunk is loaded
		if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
			if(!this.map[x][z][y].canBuild(event.getBlock().getLocation(), event.getPlayer())) {
				event.setCancelled(true);
				event.getPlayer().damage(0.1);
			}
		} else { // Default behavior if chunk not loaded
			event.setCancelled(true);
		}
	}
	
	// CAN-DESTROY EVENTS
	@EventHandler
	public void onPlayerDestroy(BlockBreakEvent event) {
		// Ignore if player is OP
		if(event.getPlayer().isOp())
			return;
		
		// Ignore if out of the world
		if(isOutsideWorld(event.getBlock().getLocation()))
			return;
		
		int x = event.getBlock().getLocation().getBlockX() / (16 * 4);
		int y = event.getBlock().getLocation().getBlockY() / (16 * 4);
		int z = event.getBlock().getLocation().getBlockZ() / (16 * 4);
		
		// Behavior if chunk is loaded
		if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
			if(!this.map[x][z][y].canDestroy(event.getBlock().getLocation(), event.getPlayer(), event.getBlock().getState().getData())) {
				event.setCancelled(true);
				event.getPlayer().damage(0.1);
			}
		} else { // Default behavior if chunk not loaded
			event.setCancelled(true);
		}
	}
	
	// CAN-USE EVENTS
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		// Ignore if player is OP
		if(event.getPlayer().isOp())
			return;
		
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
			// Ignore if out of the world or not the correct type of block
			if(isOutsideWorld(event.getClickedBlock().getLocation()) || !isInteractableBlock(event.getClickedBlock()))
				return;
			
			int x = event.getClickedBlock().getLocation().getBlockX() / (16 * 4);
			int y = event.getClickedBlock().getLocation().getBlockY() / (16 * 4);
			int z = event.getClickedBlock().getLocation().getBlockZ() / (16 * 4);
			
			// Behavior if chunk is loaded
			if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
				if(!this.map[x][z][y].canUse(event.getClickedBlock().getLocation(), event.getPlayer())) {
					event.setCancelled(true);
					event.getPlayer().damage(0.1);
				}
			} else { // Default behavior if chunk not loaded
				event.setCancelled(true);
			}
		} else if(event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null) {
			// Ignore if out of the world or not the correct type of block
			if(isOutsideWorld(event.getClickedBlock().getLocation()) || !isInteractableBlock(event.getClickedBlock()))
				return;
			
			int x = event.getClickedBlock().getLocation().getBlockX() / (16 * 4);
			int y = event.getClickedBlock().getLocation().getBlockY() / (16 * 4);
			int z = event.getClickedBlock().getLocation().getBlockZ() / (16 * 4);
			
			// Behavior if chunk is loaded
			if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
				if(!this.map[x][z][y].canUse(event.getClickedBlock().getLocation(), event.getPlayer())) {
					event.setCancelled(true);
					event.getPlayer().damage(0.1);
				}
			} else { // Default behavior if chunk not loaded
				event.setCancelled(true);
			}
		}
	}
	
	private boolean isInteractableBlock(Block clicked) {
		Material m = clicked.getType();
		switch(m) {
		case STONE_PLATE:
		case WOOD_PLATE:
		case TRIPWIRE:
		case LEVER:
		case STONE_BUTTON:
		case WOOD_BUTTON:
			return true;
		default:
			return false;
		}
	}
	
	// CAN-COMMAND EVENTS
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		// Ignore if player is OP
		if(event.getPlayer().isOp())
			return;
		
		// Ignore if out of the world
		if(isOutsideWorld(event.getPlayer().getLocation()))
			return;
		
		int x = event.getPlayer().getLocation().getBlockX() / (16 * 4);
		int y = event.getPlayer().getLocation().getBlockY() / (16 * 4);
		int z = event.getPlayer().getLocation().getBlockZ() / (16 * 4);
		
		// Behavior if chunk is loaded
		if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
			if(!this.map[x][z][y].canCommand(event.getPlayer().getLocation(), event.getPlayer())) {
				event.setCancelled(true);
			}
		} else { // Default behavior if chunk not loaded
			event.setCancelled(true);
		}
	}
	
	public boolean addPermission(Location loc, String type, String perm) {
		// Ignore if out of the world
		if(isOutsideWorld(loc))
			return false;
		
		int x = loc.getBlockX() / (16 * 4);
		int y = loc.getBlockY() / (16 * 4);
		int z = loc.getBlockZ() / (16 * 4);
		
		// verify the region we are trying to modify is laoded
		if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
			switch(type) {
			case "pvp":
				this.map[x][z][y].addPvpPerm(loc, perm);
				break;
			case "move":
				this.map[x][z][y].addMovePerm(loc, perm);
				break;
			case "chat":
				this.map[x][z][y].addChatPerm(loc, perm);
				break;
			case "build":
				this.map[x][z][y].addBuildPerm(loc, perm);
				break;
			case "destroy":
				this.map[x][z][y].addDestroyPerm(loc, perm);
				break;
			case "use":
				this.map[x][z][y].addUsePerm(loc, perm);
				break;
			case "command":
				this.map[x][z][y].addCommandPerm(loc, perm);
				break;
			default:
				return false;
			}
			// Return success
			return true;
		}
		
		// Cannot add yet, return false
		return false;
	}
	
	public boolean removePermission(Location loc, String type, String perm) {
		// Ignore if out of the world
		if(isOutsideWorld(loc))
			return false;
		
		int x = loc.getBlockX() / (16 * 4);
		int y = loc.getBlockY() / (16 * 4);
		int z = loc.getBlockZ() / (16 * 4);
		
		// verify the region we are trying to modify is laoded
		if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
			switch(type) {
			case "pvp":
				this.map[x][z][y].removePvpPerm(loc, perm);
				break;
			case "move":
				this.map[x][z][y].removeMovePerm(loc, perm);
				break;
			case "chat":
				this.map[x][z][y].removeChatPerm(loc, perm);
				break;
			case "build":
				this.map[x][z][y].removeBuildPerm(loc, perm);
				break;
			case "destroy":
				this.map[x][z][y].removeDestroyPerm(loc, perm);
				break;
			case "use":
				this.map[x][z][y].removeUsePerm(loc, perm);
				break;
			case "command":
				this.map[x][z][y].removeCommandPerm(loc, perm);
				break;
			default:
				return false;
			}
			// Return success
			return true;
		}
		
		// Cannot remove yet, return false
		return false;
	}
	
	public boolean addWhitelist(Location loc, MaterialData mat) {
		// Ignore if out of the world
		if(isOutsideWorld(loc))
			return false;
		
		int x = loc.getBlockX() / (16 * 4);
		int y = loc.getBlockY() / (16 * 4);
		int z = loc.getBlockZ() / (16 * 4);
		
		// verify the region we are trying to modify is laoded
		if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
			this.map[x][z][y].addWhitelistItem(loc, mat);
			// Return success
			return true;
		}
		
		// Cannot remove yet, return false
		return false;
	}
	
	public boolean removeWhitelist(Location loc, MaterialData mat) {
		// Ignore if out of the world
		if(isOutsideWorld(loc))
			return false;
		
		int x = loc.getBlockX() / (16 * 4);
		int y = loc.getBlockY() / (16 * 4);
		int z = loc.getBlockZ() / (16 * 4);
		
		// verify the region we are trying to modify is laoded
		if(this.map[x][z][y] != null && this.map[x][z][y].isLoaded()) {
			this.map[x][z][y].removeWhitelistItem(loc, mat);
			// Return success
			return true;
		}
		
		// Cannot remove yet, return false
		return false;
	}
	
	private boolean isOutsideWorld(Location loc) {
		if(loc.getBlockX() < 0 || loc.getBlockX() >= 2048)
			return true;
		if(loc.getBlockZ() < 0 || loc.getBlockZ() >= 2048)
			return true;
		if(loc.getBlockY() < 0 || loc.getBlockY() >= 256)
			return true;
		return false;
	}
	
	private void bufferChunksForPlayer(Player p) {
		Bukkit.getLogger().info("Buffering chunks around " + p.getName() + " immediately!");
		
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
			// Log the number of chunks currently loaded
			Bukkit.getLogger().info("Currently loaded CRM-Chunks: " + loaded.size());
			// Schedule the next cache validation
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new AsynchronousValidation(), 600);
		}
		
	}
}
