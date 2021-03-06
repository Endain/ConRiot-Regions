package net.conriot.sona.regions;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import net.conriot.sona.mysql.IOCallback;
import net.conriot.sona.mysql.MySQL;
import net.conriot.sona.mysql.Query;
import net.conriot.sona.mysql.Result;

class Chunk implements IOCallback {
	@Getter private int x, y, z;
	@Getter private boolean loaded;
	private Region[][][] regions; // x,z,y ordering
	
	public Chunk(int x, int z, int y) {
		this.x = x;
		this.z = z;
		this.y = y;
		this.loaded = false;
		this.regions = new Region[16][16][16]; // x,z,y
		
		// Load the data for this chunk immediately
		load();
	}
	
	public void addPvpPerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			this.regions[innerX][innerZ][innerY] = new Region();
		
		if(!regions[innerX][innerZ][innerY].hasPvpPerm(perm)) {
			this.regions[innerX][innerZ][innerY].addPvpPerm(perm);
			add(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "pvp", perm);
		}
	}
	
	public void addMovePerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			this.regions[innerX][innerZ][innerY] = new Region();
		
		if(!regions[innerX][innerZ][innerY].hasMovePerm(perm)) {
			this.regions[innerX][innerZ][innerY].addMovePerm(perm);
			add(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "move", perm);
		}
	}
	
	public void addChatPerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			this.regions[innerX][innerZ][innerY] = new Region();
		
		if(!regions[innerX][innerZ][innerY].hasChatPerm(perm)) {
			this.regions[innerX][innerZ][innerY].addChatPerm(perm);
			add(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "chat", perm);
		}
	}
	
	public void addBuildPerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			this.regions[innerX][innerZ][innerY] = new Region();
		
		if(!regions[innerX][innerZ][innerY].hasBuildPerm(perm)) {
			this.regions[innerX][innerZ][innerY].addBuildPerm(perm);
			add(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "build", perm);
		}
	}
	
	public void addDestroyPerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			this.regions[innerX][innerZ][innerY] = new Region();
		
		if(!regions[innerX][innerZ][innerY].hasDestroyPerm(perm)) {
			this.regions[innerX][innerZ][innerY].addDestroyPerm(perm);
			add(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "destroy", perm);
		}
	}
	
	public void addUsePerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			this.regions[innerX][innerZ][innerY] = new Region();
		
		if(!regions[innerX][innerZ][innerY].hasUsePerm(perm)) {
			this.regions[innerX][innerZ][innerY].addUsePerm(perm);
			add(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "use", perm);
		}
	}
	
	public void addCommandPerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			this.regions[innerX][innerZ][innerY] = new Region();
		
		if(!regions[innerX][innerZ][innerY].hasCommandPerm(perm)) {
			this.regions[innerX][innerZ][innerY].addCommandPerm(perm);
			add(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "command", perm);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void addWhitelistItem(Location loc, MaterialData mat) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			this.regions[innerX][innerZ][innerY] = new Region();
		
		this.regions[innerX][innerZ][innerY].addWhitelistItem(mat);
		add(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "whitelist", mat.getItemTypeId() + ":" + mat.getData());
	}
	
	public void removePvpPerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			return;
		
		this.regions[innerX][innerZ][innerY].removePvpPerm(perm);
		remove(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "command", perm);
	}
	
	public void removeMovePerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			return;
		
		this.regions[innerX][innerZ][innerY].removeMovePerm(perm);
		remove(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "move", perm);
	}
	
	public void removeChatPerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			return;
		
		this.regions[innerX][innerZ][innerY].removeChatPerm(perm);
		remove(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "chat", perm);
	}
	
	public void removeBuildPerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			return;
		
		this.regions[innerX][innerZ][innerY].removeBuildPerm(perm);
		remove(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "build", perm);
	}
	
	public void removeDestroyPerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			return;
		
		this.regions[innerX][innerZ][innerY].removeDestroyPerm(perm);
		remove(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "destroy", perm);
	}
	
	public void removeUsePerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			return;
		
		this.regions[innerX][innerZ][innerY].removeUsePerm(perm);
		remove(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "use", perm);
	}
	
	public void removeCommandPerm(Location loc, String perm) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			return;
		
		this.regions[innerX][innerZ][innerY].removeCommandPerm(perm);
		remove(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "command", perm);
	}
	
	@SuppressWarnings("deprecation")
	public void removeWhitelistItem(Location loc, MaterialData mat) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] == null)
			return;
		
		this.regions[innerX][innerZ][innerY].removeWhitelistItem(mat);
		add(loc.getBlockX() / 4, loc.getBlockY() / 4, loc.getBlockZ() / 4, "whitelist", mat.getItemTypeId() + ":" + mat.getData());
	}
	
	public boolean canPvp(Location loc, Player p) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		if(this.regions[innerX][innerZ][innerY] != null)
			return this.regions[innerX][innerZ][innerY].canPvp(p);
		return false;
	}
	
	public boolean canMove(Location loc, Player p) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		
		if(this.regions[innerX][innerZ][innerY] != null)
			return this.regions[innerX][innerZ][innerY].canMove(p);
		return true;
	}
	
	public boolean canChat(Location loc, Player p) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		if(this.regions[innerX][innerZ][innerY] != null)
			return this.regions[innerX][innerZ][innerY].canChat(p);
		return true;
	}
	
	public boolean canBuild(Location loc, Player p) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		if(this.regions[innerX][innerZ][innerY] != null)
			return this.regions[innerX][innerZ][innerY].canBuild(p);
		return false;
	}
	
	public boolean canDestroy(Location loc, Player p, MaterialData m) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		if(this.regions[innerX][innerZ][innerY] != null)
			return this.regions[innerX][innerZ][innerY].canDestroy(p, m);
		return false;
	}
	
	public boolean canUse(Location loc, Player p) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		if(this.regions[innerX][innerZ][innerY] != null)
			return this.regions[innerX][innerZ][innerY].canUse(p);
		return false;
	}
	
	public boolean canCommand(Location loc, Player p) {
		int innerX = (loc.getBlockX() - (this.x * 16 * 4)) / 4;
		int innerY = (loc.getBlockY() - (this.y * 16 * 4)) / 4;
		int innerZ = (loc.getBlockZ() - (this.z * 16 * 4)) / 4;
		if(this.regions[innerX][innerZ][innerY] != null)
			return this.regions[innerX][innerZ][innerY].canCommand(p);
		return true;
	}
	
	public void add(int x, int y, int z, String type, String entry) {
		// Create a query to add an entry for a region
		Query q = MySQL.makeQuery();
		q.setQuery("INSERT INTO regions (x,y,z,type,permission) VALUES (?,?,?,?,?)");
		q.add(x);
		q.add(y);
		q.add(z);
		q.add(type);
		q.add(entry);
		
		// Execute query asynchronously
		MySQL.execute(this, "add:Added " + type + " permission '" + entry + "'" + " at region (" + x + "," + y + "," + z + ")", q);
	}
	
	public void remove(int x, int y, int z, String type, String entry) {
		// Create a query to remove an entry for a region
		Query q = MySQL.makeQuery();
		q.setQuery("DELETE FROM regions WHERE x=? AND y=? AND z=? AND type=? AND permission=?");
		q.add(x);
		q.add(y);
		q.add(z);
		q.add(type);
		q.add(entry);
		
		// Execute query asynchronously
		MySQL.execute(this, "remove:Removed " + type + " permission '" + entry + "'" + " from region (" + x + "," + y + "," + z + ")", q);
	}
	
	private void load() {
		// Create a query to region data for this chunk
		Query q = MySQL.makeQuery();
		q.setQuery("SELECT x,y,z,type,permission FROM regions WHERE x BETWEEN ? AND ? AND y BETWEEN ? AND ? AND z BETWEEN ? AND ?");
		q.add(16 * this.x); // xmin and xmax
		q.add(16 * this.x + 15);
		q.add(16 * this.y); // ymin and ymax
		q.add(16 * this.y + 15);
		q.add(16 * this.z); // zmin and zmax
		q.add(16 * this.z + 15);
		
		// Execute query to asynchronously load chunk data
		MySQL.execute(this, "load", q);
	}
	
	public void unload() {
		// Free up memory and flag chunk as unloaded
		this.regions = null;
		this.loaded = false;
	}
	
	@Override
	public void complete(boolean success, Object tag, Result result) {
		if(tag instanceof String && ((String)tag).equals("load")) {
			while(result.next()) {
				// Bias the x/y/z to refer to Regions in this chunk
				int x = (int)result.get(0) - 16 * this.x;
				int y = (int)result.get(1) - 16 * this.y;
				int z = (int)result.get(2) - 16 * this.z;
				
				// Create a region object if it does not exist
				if(this.regions[x][z][y] == null)
					this.regions[x][z][y] = new Region();
				
				// Add the permission node depending on the type
				if(((String)result.get(3)).equals("pvp"))
					this.regions[x][z][y].addPvpPerm((String)result.get(4));
				else if(((String)result.get(3)).equals("move"))
					this.regions[x][z][y].addMovePerm((String)result.get(4));
				else if(((String)result.get(3)).equals("chat"))
					this.regions[x][z][y].addChatPerm((String)result.get(4));
				else if(((String)result.get(3)).equals("build"))
					this.regions[x][z][y].addBuildPerm((String)result.get(4));
				else if(((String)result.get(3)).equals("destroy"))
					this.regions[x][z][y].addDestroyPerm((String)result.get(4));
				else if(((String)result.get(3)).equals("use"))
					this.regions[x][z][y].addUsePerm((String)result.get(4));
				else if(((String)result.get(3)).equals("command"))
					this.regions[x][z][y].addCommandPerm((String)result.get(4));
				else if(((String)result.get(3)).equals("whitelist")) {
					String[] split = ((String)result.get(4)).split(":");
					@SuppressWarnings("deprecation")
					MaterialData mat = new MaterialData(Integer.parseInt(split[0]), Byte.parseByte(split[1]));
					this.regions[x][z][y].addWhitelistItem(mat);
				} else {
					Bukkit.getLogger().warning("Could not parse row result for Region at " + (x + this.x * 16) + "," + (z + this.z * 16) + "," + (y + this.y * 16) + " (x,z,y)");
					Bukkit.getLogger().warning("Unknown type for row: \"" + (String)result.get(3) + "\"");
				}
			}
			
			// Flag this chunk as loaded
			this.loaded = true;
		} else {
			String[] split = ((String)tag).split(":");
			if(split[0].equals("add")) {
				// Log a successful add
				Bukkit.getLogger().info(split[1]);
			} else if(split[0].equals("remove")) {
				// Log a successful removal
				Bukkit.getLogger().info(split[1]);
			}
		}
	}
}
