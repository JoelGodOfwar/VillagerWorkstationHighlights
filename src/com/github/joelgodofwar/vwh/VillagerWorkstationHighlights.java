package com.github.joelgodofwar.vwh;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.github.joelgodofwar.vwh.api.Metrics;
import com.github.joelgodofwar.vwh.api.UpdateChecker;
import com.github.joelgodofwar.vwh.api.Workstation;

@SuppressWarnings("unused")
public class VillagerWorkstationHighlights extends JavaPlugin implements Listener{
	public final static Logger logger = Logger.getLogger("Minecraft");
	public static boolean UpdateCheck;
	public static boolean debug;
	private boolean UpdateAvailable = false;
	
	@Override // TODO:
	public void onEnable() {
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info("**************************************");
		logger.info(pdfFile.getName() + " v" + pdfFile.getVersion() + " Loading...");
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			logDebug("Jar file contains -DEV, debug set to true");
			//log("jarfile contains dev, debug set to true.");
		}
		/**  Check for config */
		try{
			if(!getDataFolder().exists()){
				log("Data Folder doesn't exist");
				log("Creating Data Folder");
				getDataFolder().mkdirs();
				log("Data Folder Created at " + getDataFolder());
			}
			File  file = new File(getDataFolder(), "config.yml");
			log("" + file);
			if(!file.exists()){
				log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		/** end config check */
		/** Update Checker */
		if(UpdateCheck){
			try {
				Bukkit.getConsoleSender().sendMessage("Checking for updates...");
				UpdateChecker updater = new UpdateChecker(this, 81498);
				if(updater.checkForUpdates()) {
					UpdateAvailable = true;
					Bukkit.getConsoleSender().sendMessage(this.getName() + " NEW VERSION AVAILABLE!");
					Bukkit.getConsoleSender().sendMessage(UpdateChecker.getResourceUrl());
				}else{
					UpdateAvailable = false;
				}
			}catch(Exception e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not process update check");
			}
		}
		/** end update checker */
		
		Bukkit.getPluginManager().registerEvents(this, this);
		consoleInfo("ENABLED");
		log("MC v" + Bukkit.getVersion() + " debug=" + debug + " in " + this.getDataFolder() + "/config.yml");
		
		if(getConfig().getBoolean("debug")==true&&!(jarfile.toString().contains("-DEV"))){
			logDebug("Config.yml dump");
			logDebug("auto_update_check=" + getConfig().getBoolean("auto_update_check"));
			logDebug("debug=" + getConfig().getBoolean("debug"));
		}
		
		try {
			//PluginBase plugin = this;
			Metrics metrics  = new Metrics(this);
			// New chart here
			// myPlugins()
			metrics.addCustomChart(new Metrics.AdvancedPie("my_other_plugins", new Callable<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> call() throws Exception {
					Map<String, Integer> valueMap = new HashMap<>();
					//int varTotal = myPlugins();
					if(getServer().getPluginManager().getPlugin("DragonDropElytra") != null){valueMap.put("DragonDropElytra", 1);}
					if(getServer().getPluginManager().getPlugin("NoEndermanGrief") != null){valueMap.put("NoEndermanGrief", 1);}
					if(getServer().getPluginManager().getPlugin("PortalHelper") != null){valueMap.put("PortalHelper", 1);}
					if(getServer().getPluginManager().getPlugin("ShulkerRespawner") != null){valueMap.put("ShulkerRespawner", 1);}
					if(getServer().getPluginManager().getPlugin("MoreMobHeads") != null){valueMap.put("MoreMobHeads", 1);}
					if(getServer().getPluginManager().getPlugin("SilenceMobs") != null){valueMap.put("SilenceMobs", 1);}
					return valueMap;
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("auto_update_check", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("auto_update_check").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_debug", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("debug").toUpperCase();
				}
			}));
			/**metrics.addCustomChart(new Metrics.SimplePie("var_lang", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("lang").toUpperCase();
				}
			}));*/
		}catch (Exception e){
			// Failed to submit the stats
		}
	}
	
	@Override // TODO:
	public void onDisable() {
		consoleInfo("DISABLED");
	}
	
	public void consoleInfo(String state) {
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info("**************************************");
		logger.info(pdfFile.getName() + " v" + pdfFile.getVersion() + " is " + state);
		logger.info("**************************************");
	}
	public  void log(String dalog){
		logger.info("" + this.getName() + " " + this.getDescription().getVersion() + " " + dalog);
	}
	public  void logDebug(String dalog){
		log("[DEBUG] " + dalog);
	}
	public void logWarn(String dalog){
		log("[WARNING] " + dalog);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("VWH")||cmd.getName().equalsIgnoreCase("VillagerWorkstationHighlights")){
			if(!(sender instanceof Player)){
				return false;
			}else{
				if (args.length == 0){
					Player player = (Player) sender;
					if(player.hasPermission("vwhighlights.command")){
						Entity entity = getNearestEntityInSight(player, 10);
						if(debug){logDebug("entity=" + entity.toString());}
						
						if(entity instanceof Villager){
							Villager villager = (Villager) entity;
							if(debug){logDebug("villager=" + villager.toString());}
							Location workstation = villager.getMemory(MemoryKey.JOB_SITE);
							if(debug){logDebug("workstation=" + workstation.toString());}
							AreaEffectCloud cloud = (AreaEffectCloud) villager.getLocation().getWorld().spawnEntity(workstation.add(.5, 1, .5), EntityType.AREA_EFFECT_CLOUD);
							cloud.setParticle(Particle.HEART, null);
							cloud.setDuration(200);
							cloud.setReapplicationDelay(10);
							cloud.setRadius(0.5f);
							cloud.setRadiusPerTick(0f);
							cloud.setRadiusOnUse(0f);
							
							//villager.getWorld().spawnParticle(Particle.HEART, workstation.add(0, .5, 0), 2000);
							//villager.getWorld().spawnParticle(Particle.HEART, workstation.add(.5, 0, 0), 2000);
							//villager.getWorld().spawnParticle(Particle.HEART, workstation.add(0, 0, .5), 2000);
							//villager.getWorld().spawnParticle(Particle.HEART, workstation.add(0, .5, 0), 2000);
							//villager.getWorld().spawnParticle(Particle.HEART, workstation.add(.5, 0, 0), 2000);
							if(debug){logDebug("completed");}
							//villager.getWorld().sp
							//BlockFace a = player.getLineOfSight(arg0, arg1);
							return true;
						}
					}
				}
				/**if(args[0].equalsIgnoreCase("set")){
					// /vwh set x y z
					//       0  1 2 3
					//   1   2  3 4 5
					if(debug){logDebug("args.length=" + args.length);
					if(!(args.length >= 4)){
						sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " Arguments needed \n/vwh set x y z");
						return false;
					}else{
						Player player = (Player) sender;
						Entity entity = getNearestEntityInSight(player, 10);
						if(debug){logDebug("entity=" + entity.toString());
						if(entity instanceof Villager){
							Villager villager = (Villager) entity;
							if(debug){logDebug("villager=" + villager.toString());
							Location workstation = new Location(villager.getWorld(), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
							villager.setMemory(MemoryKey.POTENTIAL_JOB_SITE, workstation);//.getMemory(MemoryKey.JOB_SITE);
							if(debug){logDebug("workstation=" + workstation.toString());
						}
					}
					
				}*/
			}
			
		}
		return false;
	}
	
    public static Entity getNearestEntityInSight(Player player, int range) {
        ArrayList<Entity> entities = (ArrayList<Entity>) player.getNearbyEntities(range, range, range);
        ArrayList<Block> sightBlock = (ArrayList<Block>) player.getLineOfSight((Set<Material>) null, range);
        ArrayList<Location> sight = new ArrayList<Location>();
        for (int i = 0;i<sightBlock.size();i++)
            sight.add(sightBlock.get(i).getLocation());
        for (int i = 0;i<sight.size();i++) {
            for (int k = 0;k<entities.size();k++) {
                if (Math.abs(entities.get(k).getLocation().getX()-sight.get(i).getX())<1.3) {
                    if (Math.abs(entities.get(k).getLocation().getY()-sight.get(i).getY())<1.5) {
                        if (Math.abs(entities.get(k).getLocation().getZ()-sight.get(i).getZ())<1.3) {
                            return entities.get(k);
                        }
                    }
                }
            }
        }
        return null; //Return null/nothing if no entity was found
    }
	
	private List<Entity> getEntitys(Player player){
	    List<Entity> entitys = new ArrayList<Entity>();
	    for(Entity e : player.getNearbyEntities(10, 10, 10)){
	        if(e instanceof LivingEntity){
	            if(getLookingAt(player, (LivingEntity) e)){
	                entitys.add(e);
	            	if(debug){logDebug("added");}
	            }
	        }
	    }

	    return entitys;
	}
	private boolean getLookingAt(Player player, LivingEntity livingEntity){
	    Location eye = player.getEyeLocation();
	    Vector toEntity = livingEntity.getEyeLocation().toVector().subtract(eye.toVector());
	    double dot = toEntity.normalize().dot(eye.getDirection());

	    return dot > 0.99D;
	}
	
	@EventHandler
	public void playerInteract(PlayerInteractEvent event){
	  //this will be called automatically by bukkit whenever a player interacts
	  if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
	    //the player right-clicked a block
	    Material material = event.getClickedBlock().getType(); //get the block type clicked
	    if(material.equals(Material.STONE)){
	      //the block clicked was stone.
	    }
	  }
	}
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEntityEvent event){	
		Player player = event.getPlayer();
		ItemStack main = player.getInventory().getItemInMainHand();
		if(debug){logDebug("main.getType()=" + main.getType());}
		ItemStack off = player.getInventory().getItemInOffHand();
		if(debug){logDebug("off.getType()=" + off.getType());}
		if(Workstation.isWorkstation(main.getType())||Workstation.isWorkstation(off.getType())){
			if(debug){logDebug("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");}
			Entity clicked = event.getRightClicked();
			
			if(event.getRightClicked() instanceof Villager&&player.isSneaking()){
				//player.isSneaking()
				if(player.hasPermission("vwhighlights.click")){
					event.setCancelled(true);
					if(debug){logDebug("isVillager");}
					Villager villager = (Villager) event.getRightClicked();
					Location workstation = villager.getMemory(MemoryKey.JOB_SITE);
					if(debug){logDebug("workstation=" + workstation.toString());}
					if(workstation != null){
						if(debug){logDebug("workstation != null");}
						if(!(workstation.getWorld().getNearbyEntities(workstation, .5, 1, .5) instanceof AreaEffectCloud)){
							AreaEffectCloud cloud = (AreaEffectCloud) villager.getLocation().getWorld().spawnEntity(workstation.add(.5, 1, .5), EntityType.AREA_EFFECT_CLOUD);
							cloud.setParticle(Particle.HEART, null);
							cloud.setDuration(200);
							cloud.setReapplicationDelay(10);
							cloud.setRadius(0.5f);
							cloud.setRadiusPerTick(0f);
							cloud.setRadiusOnUse(0f);
							if(debug){logDebug("AreaEffectCloud set");}
							}
					}else{
						if(debug){logDebug("workstation = null");}
					}
				}
			}else{
				if(debug){logDebug("!isVillager");}
			}
			if(debug){logDebug("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");}
			//event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
		{
		Player p = event.getPlayer();
		//if(p.isOp() && UpdateCheck||p.hasPermission("sps.showUpdateAvailable")){	
		/** Notify Ops */
		if(UpdateAvailable&&(p.isOp()||p.hasPermission("sps.showUpdateAvailable"))){
			p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + "  NEW VERSION AVAILABLE!" + 
					" \n" + ChatColor.GREEN + UpdateChecker.getResourceUrl() + ChatColor.RESET);
		}

		if(p.getDisplayName().equals("JoelYahwehOfWar")||p.getDisplayName().equals("JoelGodOfWar")){
			p.sendMessage(this.getName() + " " + this.getDescription().getVersion() + " Hello father!");
			//p.sendMessage("seed=" + p.getWorld().getSeed());
		}
	}
}
