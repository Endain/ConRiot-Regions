package net.conriot.sona.regions;

import net.conriot.sona.mysql.IOCallback;
import net.conriot.sona.mysql.MySQL;
import net.conriot.sona.mysql.Query;
import net.conriot.sona.mysql.Result;

class Chunk implements IOCallback {
	private int x, y, z;
	private boolean loaded;
	private Region[][][] regions;
	
	public Chunk(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.loaded = false;
		this.regions = new Region[16][16][16];
	}
	
	public void load() {
		// Create a query to region data for this chunk
		//Query q = MySQL.makeQuery();
		//q.setQuery("SELECT x,y,z, FROM regions WHERE name=?");
		//q.add(this.player.getName());
		
		// Execute query to asynchronously load permissions
		//MySQL.execute(this, "load", q);
	}
	
	public void unload() {
		this.regions = null;
		this.loaded = false;
	}
	
	@Override
	public void complete(boolean success, Object tag, Result result) {
		// TODO Auto-generated method stub
		
	}
}
