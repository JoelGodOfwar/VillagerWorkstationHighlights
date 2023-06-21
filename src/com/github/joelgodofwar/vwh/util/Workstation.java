package com.github.joelgodofwar.vwh.util;

import org.bukkit.Material;

public enum Workstation {
	BARREL(Material.BARREL),						// Fisherman
	BLAST_FURNACE(Material.BLAST_FURNACE),			// Armorer
	BREWING_STAND(Material.BREWING_STAND),			// Cleric
	CARTOGRAPHY_TABLE(Material.CARTOGRAPHY_TABLE),	// Cartographer
	CAULDRON(Material.CAULDRON),					// Leatherworker
	COMPOSTER(Material.COMPOSTER),					// Farmer
	FLETCHING_TABLE(Material.FLETCHING_TABLE),		// Fletcher
	GRINDSTONE(Material.GRINDSTONE),				// Weaponsmith
	LECTERN(Material.LECTERN),						// Librarian
	LOOM(Material.LOOM),							// Shepherd
	SMITHING_TABLE(Material.SMITHING_TABLE),		// Toolsmith
	SMOKER(Material.SMOKER),						// Butcher
	STONECUTTER(Material.STONECUTTER),				// Mason
	;
	
	Material material;
	
	Workstation(Material material){
		this.material = material;
	}
	
	/**
	 * @return the name
	 */
	public Material getMaterial() {
		return material;
	}
	
	public final static boolean isWorkstation(Material material){
		for(Workstation verbosity : Workstation.values()){
			if(verbosity.getMaterial().equals(material) )
	             return true ;
		}
		return false;
	}
}
