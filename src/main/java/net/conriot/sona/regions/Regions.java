package net.conriot.sona.regions;

import org.bukkit.plugin.java.JavaPlugin;

public class Regions extends JavaPlugin {
	@SuppressWarnings("unused")
	private Map map;
	
	@Override
	public void onEnable() {
		// Instantiate the only map
		this.map = new Map(this);
	}
	
	@Override
	public void onDisable() {
		// Nothing to do here
	}
}
