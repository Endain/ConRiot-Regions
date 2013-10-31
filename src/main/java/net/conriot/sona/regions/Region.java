package net.conriot.sona.regions;

import java.util.HashSet;

import net.conriot.sona.permissions.Permissions;

import org.bukkit.Material;
import org.bukkit.entity.Player;

class Region {
	private HashSet<String> pvp;
	private HashSet<String> move;
	private HashSet<String> chat;
	private HashSet<String> build;
	private HashSet<String> destroy;
	private HashSet<String> use;
	private HashSet<String> command;
	private HashSet<Material> whitelist;
	
	public Region() {
		// Initialize all sets
		this.pvp = new HashSet<String>();
		this.move = new HashSet<String>();
		this.chat = new HashSet<String>();
		this.build = new HashSet<String>();
		this.destroy = new HashSet<String>();
		this.use = new HashSet<String>();
		this.command = new HashSet<String>();
		this.whitelist = new HashSet<Material>();
	}
	
	public void addPvpPerm(String perm) {
		this.pvp.add(perm);
	}
	
	public void addMovePerm(String perm) {
		this.move.add(perm);
	}
	
	public void addChatPerm(String perm) {
		this.chat.add(perm);
	}
	
	public void addBuildPerm(String perm) {
		this.build.add(perm);
	}
	
	public void addDestroyPerm(String perm) {
		this.destroy.add(perm);
	}
	
	public void addUsePerm(String perm) {
		this.use.add(perm);
	}
	
	public void addCommandPerm(String perm) {
		this.command.add(perm);
	}
	
	public void addWhitelistItem(Material mat) {
		this.whitelist.add(mat);
	}
	
	public void removePvpPerm(String perm) {
		this.pvp.remove(perm);
	}
	
	public void removeMovePerm(String perm) {
		this.move.remove(perm);
	}
	
	public void removeChatPerm(String perm) {
		this.chat.remove(perm);
	}
	
	public void removeBuildPerm(String perm) {
		this.build.remove(perm);
	}
	
	public void removeDestroyPerm(String perm) {
		this.destroy.remove(perm);
	}
	
	public void removeUsePerm(String perm) {
		this.use.remove(perm);
	}
	
	public void removeCommandPerm(String perm) {
		this.command.remove(perm);
	}
	
	public void removeWhitelistItem(Material mat) {
		this.whitelist.remove(mat);
	}
	
	public boolean canPvp(Player p) {
		return Permissions.hasAnyPerms(p, this.pvp);
	}
	
	public boolean canMove(Player p) {
		return Permissions.hasAnyPerms(p, this.pvp);
	}
	
	public boolean canChat(Player p) {
		return Permissions.hasAnyPerms(p, this.pvp);
	}
	
	public boolean canBuild(Player p) {
		return Permissions.hasAnyPerms(p, this.pvp);
	}
	
	public boolean CanDestroy(Player p, Material m) {
		if(this.whitelist.contains(m))
			return true;
		return Permissions.hasAnyPerms(p, this.pvp);
	}
	
	public boolean canUse(Player p) {
		return Permissions.hasAnyPerms(p, this.pvp);
	}
	
	public boolean canCommand(Player p) {
		return Permissions.hasAnyPerms(p, this.pvp);
	}
}
