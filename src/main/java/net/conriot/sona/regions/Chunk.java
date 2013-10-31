package net.conriot.sona.regions;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import net.conriot.sona.mysql.IOCallback;
import net.conriot.sona.mysql.MySQL;
import net.conriot.sona.mysql.Query;
import net.conriot.sona.mysql.Result;

class Chunk implements IOCallback {
	private int x, y, z;
	@Getter private boolean loaded;
	private Region[][][] regions; // x,z,y ordering
	
	public Chunk(int x, int z, int y) {
		this.x = x;
		this.y = z;
		this.z = y;
		this.loaded = false;
		this.regions = new Region[16][16][16]; // x,z,y
	}
	
	public void addPvpPerm(String perm) {
	}
	
	public void addMovePerm(String perm) {
	}
	
	public void addChatPerm(String perm) {
	}
	
	public void addBuildPerm(String perm) {
	}
	
	public void addDestroyPerm(String perm) {
	}
	
	public void addUsePerm(String perm) {
	}
	
	public void addCommandPerm(String perm) {
	}
	
	public void addWhitelistItem(MaterialData mat) {
	}
	
	public void removePvpPerm(String perm) {
	}
	
	public void removeMovePerm(String perm) {
	}
	
	public void removeChatPerm(String perm) {
	}
	
	public void removeBuildPerm(String perm) {
	}
	
	public void removeDestroyPerm(String perm) {
	}
	
	public void removeUsePerm(String perm) {
	}
	
	public void removeCommandPerm(String perm) {
	}
	
	public void removeWhitelistItem(Material mat) {
	}
	
	public boolean canPvp(Player p) {
	}
	
	public boolean canMove(Player p) {
	}
	
	public boolean canChat(Player p) {
	}
	
	public boolean canBuild(Player p) {
	}
	
	public boolean CanDestroy(Player p, Material m) {
	}
	
	public boolean canUse(Player p) {
	}
	
	public boolean canCommand(Player p) {
	}
	
	public void add(int x, int y, int z, String type, String entry) {
		// Create a query to add an entry for a region
		Query q = MySQL.makeQuery();
		q.setQuery("INSERT INTO regions (x,y,z,type,permission) VALUES ?,?,?,?,?");
		//q.add();
		
		// Execute query asynchronously
		MySQL.execute(this, null, q);
	}
	
	public void remove(int x, int y, int z, String type, String entry) {
		
	}
	
	public void load() {
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
				else if(((String)result.get(3)).equals("move")) {
					String[] split = ((String)result.get(4)).split(":");
					@SuppressWarnings("deprecation")
					MaterialData mat = new MaterialData(Integer.parseInt(split[0]), Byte.parseByte(split[1]));
					this.regions[x][z][y].addWhitelistItem(mat);
				} else {
					Bukkit.getLogger().warning("Could not parse row result for Region at " + x + this.x * 16 + "," + z + this.z * 16 + "," + y + this.y * 16 + " (x,z,y)");
					Bukkit.getLogger().warning("Unknown type for row: \"" + (String)result.get(3) + "\"");
				}
			}
			
			// Flag this chunk as loaded
			this.loaded = true;
		}
	}
}
