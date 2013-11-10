package net.conriot.sona.regions;

import org.bukkit.plugin.java.JavaPlugin;

public class Regions extends JavaPlugin {
	private Map map;
	
	@Override
	public void onEnable() {
		// Instantiate the only map
		this.map = new Map(this);
		// Register commands for this map
		getCommand("rg").setExecutor(new Commands(this.map));
	}
	
	@Override
	public void onDisable() {
		// Nothing to do here
	}
}
