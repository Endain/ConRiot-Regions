package net.conriot.sona.regions;

import java.lang.reflect.Field;

import net.minecraft.server.v1_6_R3.Packet63WorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

class Commands implements CommandExecutor {
	private Map map;
	
	public Commands(Map map) {
		this.map = map;
	}
	
	private void add(Player sender, String[] args) {
		if(args.length > 2) {
			if(this.map.addPermission(sender.getLocation(), args[1].toLowerCase(), args[2])) {
				sender.sendMessage(ChatColor.GREEN + "Added permisision '" + args[2] + "' to group '" + args[1] + "'!");
				highlightChunk(sender);
			} else {
				sender.sendMessage(ChatColor.RED + "Could not add permission!");
			}
		}
	}
	
	private void remove(Player sender, String[] args) {
		if(args.length > 2) {
			if(this.map.removePermission(sender.getLocation(), args[1].toLowerCase(), args[2])) {
				sender.sendMessage(ChatColor.GREEN + "Removed permisision '" + args[2] + "' from group '" + args[1] + "'!");
				highlightChunk(sender);
			} else {
				sender.sendMessage(ChatColor.RED + "Could not remove permission!");
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void whitelist(Player sender, String[] args) {
		if(args.length > 2) {
			String[] split = args[2].split(":");
			if(split.length == 2) {
				int id, data;
				
				try {
					id= Integer.parseInt(split[0]);
					data = Integer.parseInt(split[1]);
					MaterialData mat = new MaterialData(id, (byte) data);
					
					switch(args[1].toLowerCase()) {
					case "add":
						if(this.map.addWhitelist(sender.getLocation(), mat)) {
							sender.sendMessage(ChatColor.GREEN + "Added material '" + id + ":" + data + "' to whitelist!");
							highlightChunk(sender);
						} else {
							sender.sendMessage(ChatColor.RED + "Could not add to whitelist!");
						}
						break;
					case "remove":
						if(this.map.removeWhitelist(sender.getLocation(), mat)) {
							sender.sendMessage(ChatColor.GREEN + "Removed material '" + id + ":" + data + "' from whitelist!");
							highlightChunk(sender);
						} else {
							sender.sendMessage(ChatColor.RED + "Could not remove from whitelist!");
						}
						break;
					default:
						sender.sendMessage(ChatColor.RED + "Must specify either 'add' or 'remove'!");
					}
				} catch(Exception e) {
					// Notify of incorrect material
					sender.sendMessage(ChatColor.RED + "Invalid material! (Must be of format id:data)");
				}
			}
		}
	}
	
	private void list(Player sender, String[] args) {
		// TODO
	}
	
	private void highlightChunk(Player sender) {
		// Generate location at x/y/z rounded to nearest region corner
		Location base = sender.getLocation().clone();
		base.setX((sender.getLocation().getBlockX() / 4) * 4);
		base.setY((sender.getLocation().getBlockY() / 4) * 4);
		base.setZ((sender.getLocation().getBlockZ() / 4) * 4);
		
		for(float x = 0; x < 4; x += .5) {
			for(float y = 0; y < 4; y += .5) {
				for(float z = 0; z < 4; z += .5) {
					playBlockbreakEffect(base.clone().add(x, y, z), 0, 1);
				}
			}
		}
	}
	
	private void playBlockbreakEffect(Location loc, float speed, int count) {
		Packet63WorldParticles effect = null;
		try {
			effect = createCloudEffect(loc, speed, count);
			if(effect != null) {
				Player[] players = Bukkit.getOnlinePlayers();
				for(Player p: players) {
					if(p.getLocation().getWorld() == loc.getWorld()) {
						if(p.getLocation().distanceSquared(loc) < 256) {
							CraftPlayer cp = (CraftPlayer)p;
							cp.getHandle().playerConnection.sendPacket(effect);
						}
					}
				}
			}
		} catch(Exception e) {
			// Don't log for now
		}
	}
	
	private Packet63WorldParticles createCloudEffect(Location loc, float speed, int count) throws Exception {
		Packet63WorldParticles packet = new Packet63WorldParticles();
		//setValue(packet, "a", "tilecrack_" + id + "_" + data);
		setValue(packet, "a", "happyVillager");
		setValue(packet, "b", (float) loc.getX());
		setValue(packet, "c", (float) loc.getY());
		setValue(packet, "d", (float) loc.getZ());
		setValue(packet, "e", 0);
		setValue(packet, "f", 0);
		setValue(packet, "g", 0);
		setValue(packet, "h", speed);
		setValue(packet, "i", count);
		return packet;
    }
	
	private static void setValue(Object instance, String fieldName, Object value) throws Exception {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(instance, value);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length > 0 && sender.isOp() && sender instanceof Player) {
			String subcommand = args[0].toLowerCase();
			switch(subcommand) {
			case "add":
				add((Player)sender, args);
				break;
			case "remove":
				remove((Player)sender, args);
				break;
			case "list":
				list((Player)sender, args);
				break;
			case "whitelist":
				whitelist((Player)sender, args);
				break;
			}
		}
		// Always return true to prevent default Bukkit messages
		return true;
	}
}
