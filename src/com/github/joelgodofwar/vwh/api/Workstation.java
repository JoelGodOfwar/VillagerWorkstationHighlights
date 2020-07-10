package com.github.joelgodofwar.vwh.api;

import org.bukkit.Material;

public enum Workstation {
	BARREL(Material.BARREL),
	BLAST_FURNACE(Material.BLAST_FURNACE),
	BREWING_STAND(Material.BREWING_STAND),
	CARTOGRAPHY_TABLE(Material.CARTOGRAPHY_TABLE),
	CAULDRON(Material.CAULDRON),
	COMPOSTER(Material.COMPOSTER),
	FLETCHING_TABLE(Material.FLETCHING_TABLE),
	GRINDSTONE(Material.GRINDSTONE),
	LECTERN(Material.LECTERN),
	LOOM(Material.LOOM),
	SMITHING_TABLE(Material.SMITHING_TABLE),
	SMOKER(Material.SMOKER),
	STONECUTTER(Material.STONECUTTER),
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
