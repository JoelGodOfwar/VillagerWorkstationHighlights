package com.github.joelgodofwar.vwh;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.github.joelgodofwar.vwh.util.Ansi;
import com.github.joelgodofwar.vwh.util.Metrics;
import com.github.joelgodofwar.vwh.util.UpdateChecker;
import com.github.joelgodofwar.vwh.util.Workstation;
import com.github.joelgodofwar.vwh.util.YmlConfiguration;

@SuppressWarnings("unused")
public class VillagerWorkstationHighlights extends JavaPlugin implements Listener{
	public final static Logger logger = Logger.getLogger("Minecraft");
	/** update checker variables */
	public String updateurl = "https://github.com/JoelGodOfwar/VillagerWorkstationHighlights/raw/master/versioncheck/{vers}/version.txt";
	public String newVerMsg;// = Ansi.YELLOW + this.getName() + Ansi.MAGENTA + " v{oVer}" + Ansi.RESET + " " + lang.getString("vwh.enabled", "ENABLED") + " " + Ansi.GREEN + " v{nVer}" + Ansi.RESET;
	public int updateVersion = 81498; // https://spigotmc.org/resources/81498
	boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
	public String thisName = this.getName();
	public String thisVersion = this.getDescription().getVersion();
	/** end update checker variables */
	public static boolean debug;
	public static String daLang;
	File langFile;
    FileConfiguration lang;
    YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();

	
	
	@Override // TODO:
	public void onEnable() {
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info("**************************************");
		logger.info(pdfFile.getName() + " v" + pdfFile.getVersion() + " Loading...");
	log(": DEV Version Check...");
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			logDebug("Jar file contains -DEV, debug set to true");
			//log("jarfile contains dev, debug set to true.");
		}
	log(": Loading Config File...");
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
				log(": config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		/** end config check */
		
		/**  Check config version */
		try {
			oldconfig.load(new File(getDataFolder(), "config.yml"));
		} catch (IOException | InvalidConfigurationException e1) {
			logWarn("Could not load config.yml");
			e1.printStackTrace();
		}
		String checkconfigversion = oldconfig.getString("version", "1.0.0");
		if(checkconfigversion != null){
			if(!checkconfigversion.equalsIgnoreCase("1.0.4")){
				try {
					copyFile_Java7(getDataFolder() + "" + File.separatorChar + "config.yml", getDataFolder() + "" + File.separatorChar + "old_config.yml");
				} catch (IOException e) {
					e.printStackTrace();
				}
				saveResource("config.yml", true);
				
				try {
					config.load(new File(getDataFolder(), "config.yml"));
				} catch (IOException | InvalidConfigurationException e1) {
					logWarn("Could not load config.yml");
					e1.printStackTrace();
				}
				try {
					oldconfig.load(new File(getDataFolder(), "old_config.yml"));
				} catch (IOException | InvalidConfigurationException e1) {
					e1.printStackTrace();
				}
				config.set("auto_update_check", oldconfig.get("auto_update_check", true));
				config.set("debug", oldconfig.get("debug", false));
				config.set("lang", oldconfig.get("lang", "en_US"));
				config.set("particle.display", oldconfig.get("particle.display", true));
				config.set("particle.workstation.name", oldconfig.get("particle.name", "HEART"));
				config.set("particle.workstation.duration", oldconfig.get("particle.duration", 200));
				config.set("particle.villager_line.name", oldconfig.get("particle.villager_line.name", "VILLAGER_HAPPY"));
				config.set("particle.villager_line.count", oldconfig.get("particle.villager_line.count", 10));
				
				config.set("shift_click.require_item", oldconfig.get("shift_click.require_item", true));
				config.set("shift_click.require_workstation", oldconfig.get("shift_click.require_workstation", true));
				config.set("shift_click.required_material", oldconfig.get("shift_click.required_material", "crafting_table"));
				
				try {
					config.save(new File(getDataFolder(), "config.yml"));
				} catch (IOException e) {
					logWarn("Could not save old settings to config.yml");
					e.printStackTrace();
				}
				log(": config.yml Updated! old config saved as old_config.yml");
			}
		}
	log(": Loading Lang File...");
		/** Lang file check */
		if(debug){logDebug("datafolder=" + getDataFolder());}
		langFile = new File(getDataFolder() + "" + File.separatorChar + "lang" + File.separatorChar, daLang + ".yml");//\
		if(debug){logDebug("langFilePath=" + langFile.getPath());}
		if(!langFile.exists()){                                  // checks if the yaml does not exist
			langFile.getParentFile().mkdirs();                  // creates the /plugins/<pluginName>/ directory if not found
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			log("Updating lang files! copied en_US.yml, and nl_NL.yml to "
			+ getDataFolder() + "" + File.separatorChar + "lang");
			//ConfigAPI.copy(getResource("lang.yml"), langFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		String checklangversion = lang.getString("version", "1.0.0");
		if(checklangversion != null){
			if(!checklangversion.equalsIgnoreCase("1.0.1")){
				saveResource("lang" + File.separatorChar + "en_US.yml", true);
				saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
				log("Updating lang files! copied en_US.yml, and nl_NL.yml to "
						+ getDataFolder() + "" + File.separatorChar + "lang");
			}
		}else{
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			log("Updating lang files! copied en_US.yml, and nl_NL.yml to "
					+ getDataFolder() + "" + File.separatorChar + "lang");
		}
		/** Lang file check */
		newVerMsg = Ansi.YELLOW + this.getName() + Ansi.MAGENTA + " v{oVer}" + Ansi.RESET + " " + lang.getString("vwh.new_vers", "NEW VERSION AVAILABLE!") + " " + Ansi.GREEN + " v{nVer}" + Ansi.RESET;
		/** Update Checker */
		if(UpdateCheck){
			try {
						Bukkit.getConsoleSender().sendMessage("Checking for updates...");
						UpdateChecker updater = new UpdateChecker(this, updateVersion, updateurl);
				if(updater.checkForUpdates()) {
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();
					Bukkit.getConsoleSender().sendMessage(newVerMsg.replace("{oVer}", UColdVers).replace("{nVer}", UCnewVers));
					Bukkit.getConsoleSender().sendMessage(Ansi.GREEN + UpdateChecker.getResourceUrl() + Ansi.RESET);
				}else{
					UpdateAvailable = false;
				}
			}catch(Exception e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not process update check");
				e.printStackTrace();
			}
		}
		/** end update checker */
		
		Bukkit.getPluginManager().registerEvents(this, this);
		consoleInfo("" + lang.getString("vwh.enabled", "ENABLED"));
		log("MC v" + Bukkit.getVersion() + " debug=" + debug + " in " + this.getDataFolder() + "/config.yml");
		
		if(getConfig().getBoolean("debug")==true&&!(jarfile.toString().contains("-DEV"))){
			logDebug("Config.yml dump");
			logDebug("auto_update_check=" + getConfig().getBoolean("auto_update_check"));
			logDebug("debug=" + getConfig().getBoolean("debug"));
		}
		
		try {
			//PluginBase plugin = this;
			Metrics metrics  = new Metrics(this, 8197);
			// New chart here
			// myPlugins()
			metrics.addCustomChart(new Metrics.AdvancedPie("my_other_plugins", new Callable<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> call() throws Exception {
					Map<String, Integer> valueMap = new HashMap<>();
					
					if(getServer().getPluginManager().getPlugin("DragonDropElytra") != null){valueMap.put("DragonDropElytra", 1);}
		    		if(getServer().getPluginManager().getPlugin("NoEndermanGrief") != null){valueMap.put("NoEndermanGrief", 1);}
		    		if(getServer().getPluginManager().getPlugin("PortalHelper") != null){valueMap.put("PortalHelper", 1);}
		    		if(getServer().getPluginManager().getPlugin("ShulkerRespawner") != null){valueMap.put("ShulkerRespawner", 1);}
		    		if(getServer().getPluginManager().getPlugin("MoreMobHeads") != null){valueMap.put("MoreMobHeads", 1);}
		    		if(getServer().getPluginManager().getPlugin("SilenceMobs") != null){valueMap.put("SilenceMobs", 1);}
		    		if(getServer().getPluginManager().getPlugin("SinglePlayerSleep") != null){valueMap.put("SinglePlayerSleep", 1);}
					//if(getServer().getPluginManager().getPlugin("VillagerWorkstationHighlights") != null){valueMap.put("VillagerWorkstationHighlights", 1);}
					if(getServer().getPluginManager().getPlugin("RotationalWrench") != null){valueMap.put("RotationalWrench", 1);}
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
			metrics.addCustomChart(new Metrics.SimplePie("var_lang", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("lang").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("var_particle_display", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("particle.display").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("var_particle_name", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("particle.name").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("var_particle_duration", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("particle.duration").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("var_shift_click_require_item", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("shift_click.require_item").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("var_shift_click_require_workstation", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("shift_click.require_workstation").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("var_shift_click_required_material", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("shift_click.required_material").toUpperCase();
		        }
		    }));

		}catch (Exception e){
			// Failed to submit the stats
		}
	}
	
	@Override // TODO:
	public void onDisable() {
		consoleInfo("" + lang.getString("vwh.disabled", "DISABLED"));
	}
	
	public void consoleInfo(String state) {
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(Ansi.GREEN + "**************************************" + Ansi.RESET);
		logger.info(Ansi.YELLOW + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.RESET + " is " + state);
		logger.info(Ansi.GREEN + "**************************************" + Ansi.RESET);
	}
	
	public	void log(String dalog){// TODO: log
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(Ansi.YELLOW + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.RESET + " " + dalog );
	}
	public	void logDebug(String dalog){
		log(Ansi.RED + "[DEBUG] " + Ansi.RESET + dalog);
	}
	public void logWarn(String dalog){
		log(Ansi.RED + "[WARN] " + Ansi.RESET  + dalog);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("VWH")||cmd.getName().equalsIgnoreCase("VillagerWorkstationHighlights")){
			if(args.length == 0&&(sender instanceof Player)){
				Player player = (Player) sender;
				if(player.hasPermission("vwhighlights.command")){
					Entity entity = getNearestEntityInSight(player, 10);
					if(entity instanceof Villager){
						if(debug){logDebug("CMD - entity=" + entity.toString());}
						Villager villager = (Villager) entity;
						if(debug){logDebug("CMD - villager=" + villager.toString());}
						Location workstation = villager.getMemory(MemoryKey.JOB_SITE);
						if(workstation != null){
							Location workstation2 = workstation;
							int x = (int) workstation2.getX();
							int y = (int) workstation2.getY();
							int z = (int) workstation2.getZ();
							String daJob = workstation2.getBlock().getType().toString();
							if(debug){logDebug("CMD - workstation=" + workstation.toString());}
							if(debug){logDebug("CMD - workstation2=" + workstation2.toString());}
							//workstation2 = workstation2.subtract(0, 1, 0);
							if(debug){logDebug("CMD - workstation2=" + workstation2.getBlock().getType().toString());}
							/**workstation2 = workstation2.add(1, 0, 0);
							if(debug){logDebug("CMD - workstation2=" + workstation2.getBlock().getType().toString());}
							workstation2 = workstation2.add(0, 0, 1);
							if(debug){logDebug("CMD - workstation2=" + workstation2.getBlock().getType().toString());}
							workstation2 = workstation2.subtract(1, 0, 0);
							if(debug){logDebug("CMD - workstation2=" + workstation2.getBlock().getType().toString());}
							workstation2 = workstation2.subtract(1, 0, 0);
							if(debug){logDebug("CMD - workstation2=" + workstation2.getBlock().getType().toString());}
							workstation2 = workstation2.subtract(0, 0, 1);
							if(debug){logDebug("CMD - workstation2=" + workstation2.getBlock().getType().toString());}
							workstation2 = workstation2.subtract(0, 0, 1);
							if(debug){logDebug("CMD - workstation2=" + workstation2.getBlock().getType().toString());}
							workstation2 = workstation2.add(1, 0, 0);
							if(debug){logDebug("CMD - workstation2=" + workstation2.getBlock().getType().toString());}
							workstation2 = workstation2.add(1, 0, 0);
							if(debug){logDebug("CMD - workstation2=" + workstation2.getBlock().getType().toString());}*/
							if(!(workstation.getWorld().getNearbyEntities(workstation, .5, 1, .5) instanceof AreaEffectCloud)){
								AreaEffectCloud cloud = (AreaEffectCloud) villager.getLocation().getWorld().spawnEntity(workstation.add(.5, 1, .5), EntityType.AREA_EFFECT_CLOUD);
								cloud.setParticle(Particle.valueOf(getConfig().getString("particle.name", "HEART").toUpperCase()), null);
								cloud.setDuration(getConfig().getInt("particle.duration", 200));
								cloud.setReapplicationDelay(10);
								cloud.setRadius(0.5f);
								cloud.setRadiusPerTick(0f);
								cloud.setRadiusOnUse(0f);
								if(debug){logDebug("AreaEffectCloud set");}
								//Material blockMaterial = workstation2.getBlock().getType();
								//Location blockLocation = new Location(workstation.getWorld(), workstation.getX(), workstation.getY(), workstation.getZ());
								String msg = lang.getString("vwh.workstation", "Workstation is  <daJob> at <X> , <Y>, <Z>");
								msg = msg.replace("<daJob>", daJob).replace("<X>", "" + x).replace("<Y>", "" + y).replace("<Z>", "" + z);
								msg = ChatColor.translateAlternateColorCodes('&', msg);
								sender.sendMessage("" + msg);
								
							}
						}else{
							String msg = lang.getString("vwh.unemployed", "That villager is unemployed.");
							msg = ChatColor.translateAlternateColorCodes('&', msg);
							player.sendMessage("" + msg);
							if(debug){logDebug("CMD - workstation = null");}
							return false;
						}
						return true;
					}else{
						player.sendMessage("Villager not found.");
					}
				}
			}else if(args.length != 0){
				if(args[0].equalsIgnoreCase("reload")){
					if(sender.hasPermission("vwhighlights.reload")||!(sender instanceof Player)){
						//ConfigAPI.Reloadconfig(this, p);
						this.reloadConfig();
						VillagerWorkstationHighlights plugin = this;
						getServer().getPluginManager().disablePlugin(plugin);
						getServer().getPluginManager().enablePlugin(plugin);
						reloadConfig();
						try {
							config.load(new File(getDataFolder(), "config.yml"));
						} catch (IOException | InvalidConfigurationException e1) {
							logWarn("Could not load config.yml");
							e1.printStackTrace();
						}
						langFile = new File(getDataFolder() + "" + File.separatorChar + "lang" + File.separatorChar, daLang + ".yml");
						try {
							lang.load(langFile);
						} catch (IOException | InvalidConfigurationException e) {
							logWarn("Could not load " + daLang + ".yml");
							e.printStackTrace();
						}
						sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.getString("vwh.reloaded", "Has been reloaded!") );
						return true;
					}else if(!sender.hasPermission("vwhighlights.reload")){
						String msg = lang.getString("vwh.no_perm", "You do not have permission (<perm>)");
						msg = msg.replace("<perm>", "vwhighlights.reload");
						sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + msg);
						return false;
					}
				}
				if(args[0].equalsIgnoreCase("toggledebug")||args[0].equalsIgnoreCase("td")){
					String perm = "vwhighlights.toggledebug";
					if(sender.isOp()||sender.hasPermission(perm)||!(sender instanceof Player)){
						debug = !debug;
						String msg = lang.getString("vwh.debug_set", "DEBUG has been set to <debug_var>");
						msg = msg.replace("<debug_var>", "" + ChatColor.YELLOW  + debug);
						sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + msg);
						return true;
					}else if(!sender.hasPermission(perm)){
						String msg = lang.getString("vwh.no_perm", "You do not have permission (<perm>)");
						msg = msg.replace("<perm>", perm);
						sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + msg );
						return false;
					}
				}
			}else{
				sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.getString("vwh.no_console", "Console can not use this command.") );
				return false;
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
						villager.setMemory(MemoryKey.JOB_SITE, workstation);//.getMemory(MemoryKey.JOB_SITE);
						if(debug){logDebug("workstation=" + workstation.toString());
						return true;
						}
					}
				}
				
			}*/
		}
		
		return false;
	}
	
	@Override 
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) { // TODO: Tab Complete
		if (command.getName().equalsIgnoreCase("VWH")||command.getName().equalsIgnoreCase("VillagerWorkstationHighlights")) {
			List<String> autoCompletes = new ArrayList<>(); //create a new string list for tab completion
			if (args.length == 1) { // reload, toggledebug, playerheads, customtrader, headfix
				autoCompletes.add("reload");
				autoCompletes.add("toggledebug");
				return autoCompletes; // then return the list
			}
		}
		return null;
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
	            	if(debug){logDebug("added " + e.toString());}
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
	public void onPlayerInteractEvent(PlayerInteractEvent event) { // TODO: PIE
		Player player = event.getPlayer();
		boolean show_particles = true;
		ItemStack main;
		ItemStack off;
		ItemStack hand;
		ItemStack material;
		boolean reqItem = false;
		if(debug){logDebug("PIE - vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");}
		Block clicked = event.getClickedBlock();
		if(clicked == null) {
			return;
		}
			if(debug){logDebug("PIE - rightClicked=" + event.getClickedBlock().getType());}
			if(debug){logDebug("PIE - sneaking=" + player.isSneaking());}
			if(debug){logDebug("PIE - permission=" + player.hasPermission("vwhighlights.click"));}
			if( event.getHand().equals(EquipmentSlot.HAND) || event.getHand().equals(EquipmentSlot.OFF_HAND) ) {
				
				main = player.getInventory().getItemInMainHand();
				if(debug){logDebug("PIEE - main.getType()=" + main.getType());}
				off = player.getInventory().getItemInOffHand();
				if(debug){logDebug("PIEE - off.getType()=" + off.getType());}
				
				if(getConfig().getBoolean("shift_click.require_item", true)){
					if(getConfig().getBoolean("shift_click.require_workstation", true)){
						if(Workstation.isWorkstation(main.getType())||Workstation.isWorkstation(off.getType())){
							reqItem = true;
						}
					}else {
						material = new ItemStack(Material.getMaterial(getConfig().getString("shift_click.required_material", "crafting_table").toUpperCase()), 1);
						if(main.getType().equals(material.getType())||off.getType().equals(material.getType())){
							reqItem = true;
						}
					}
				}else {
					reqItem = true;
				}
				
				if( Workstation.isWorkstation(clicked.getType()) && player.isSneaking() && player.hasPermission("vwhighlights.click") 
						&& event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && reqItem ) {
					
					Villager villager = streamInRange(clicked.getLocation(), 50);
					if(villager != null) {
						// found villager
						Location loc = villager.getLocation();
						if(getConfig().getBoolean("particle.display", true)){
							drawLine(Particle.valueOf(getConfig().getString("particle.villager_line.name", "VILLAGER_HAPPY").toUpperCase()), clicked.getLocation(), loc, 0.25, false);
							villager.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1));
						}
						player.sendMessage("Villager found at x:" + loc.getBlockX() +" y:" + loc.getBlockY() + " z:" + loc.getBlockZ());
						player.setCompassTarget(loc);
						event.setCancelled(true);
					}else {
						// failed to find villager within 50 blocks
						player.sendMessage("No villager assigned to this workstation within 50 blocks.");
						Location loc = player.getWorld().getSpawnLocation();
						player.setCompassTarget(loc);
					}
					
					// If Villager JOB_SITE == Clicked then we have a match, so do something.
				}
			}
	}
	
	public void drawLine(
			Particle particle,	// Particle to display
	        Location point1, 	// Start
	        Location point2, 	// End
	        double space,   	// distance between particles.
	        boolean from		// from villager?
	        ) {
	 
	    World world = point1.getWorld();
	 
	    /*Throw an error if the points are in different worlds*/
	    Validate.isTrue(point2.getWorld().equals(world), "Lines cannot be in different worlds!");
	    
	    if(from) {
		    point1 = point1.add(0, 1.5, 0);
		    point2 = point2.add(point2.getBlockX() > 0 ? 0.5 : -0.5, 0.5, point2.getBlockZ() > 0 ? -0.5 : 0.5);
	    }
	    else if(!from){
	    	point2 = point2.add(0, 1.5, 0);
		    point1 = point1.add(point1.getBlockX() > 0 ? 0.5 : -0.5, 0.5, point1.getBlockZ() > 0 ? -0.5 : 0.5);
		    log("blockX=" + point1.getBlockX() + " blockZ=" + point1.getBlockZ() + " x=" + point1.getX() + " z=" + point1.getZ());
	    }
	    
	    /*Distance between the two particles*/
	    double distance = point1.distance(point2);
	 
	    /* The points as vectors */
	    Vector p1 = point1.toVector();
	    Vector p2 = point2.toVector();
	 
	    /* Subtract gives you a vector between the points, we multiply by the space*/
	    Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
	 
	    /*The distance covered*/
	    double covered = 0;
	 
	    /* We run this code while we haven't covered the distance, we increase the point by the space every time*/
	    for (; covered < distance; p1.add(vector)) {
	        /*Spawn the particle at the point*/
	        world.spawnParticle(particle, p1.getX(), p1.getY(), p1.getZ(), getConfig().getInt("particle.villager_line.count", 10));
	 // Particle.VILLAGER_HAPPY, 
	        /* We add the space covered */
	        covered += space;
	    }
	}
	
	public static Villager streamInRange( Location center, double radius) {
		Collection<Entity> entities = center.getWorld().getNearbyEntities(center, radius, radius, radius);
		for (Entity entity : entities) {
		    if(entity instanceof Villager) {
		    	if(center.equals(((Villager) entity).getMemory(MemoryKey.JOB_SITE))) {
		    		return (Villager) entity;
		    	}
		    }
		}
		  //return center.getWorld().getNearbyEntities(center, radius, radius, radius);
				  //.stream()
				  //.filter(e -> e instanceof Villager)
				  //.map(e -> ((Villager) e));
		return null;
		}
	
	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) { // TODO: PIEE
		Player player = event.getPlayer();
		boolean show_particles = true;
		ItemStack main;
		ItemStack off;
		ItemStack material;
		if(debug){logDebug("PIEE - vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");}
		if(debug){logDebug("PIEE - rightClicked instance of villager=" + (event.getRightClicked() instanceof Villager));}
		if(debug){logDebug("PIEE - sneaking=" + player.isSneaking());}
		if(debug){logDebug("PIEE - permission=" + player.hasPermission("vwhighlights.click"));}
		if(event.getRightClicked() instanceof Villager&&player.isSneaking()&&player.hasPermission("vwhighlights.click")){
			Entity clicked = event.getRightClicked();
			event.setCancelled(true);
			if(debug){logDebug("PIEE - isVillager");}
			Villager villager = (Villager) event.getRightClicked();
			Location workstation = villager.getMemory(MemoryKey.JOB_SITE);
			if(workstation != null){
				Location workstation2 = workstation;
				int x = (int) workstation2.getX();
				int y = (int) workstation2.getY();
				int z = (int) workstation2.getZ();
				String daJob = workstation2.getBlock().getType().toString();
				if(debug){logDebug("PIEE - workstation=" + workstation.toString());}
				if(debug){logDebug("CMD - workstation2=" + workstation2.toString());}
				if(getConfig().getBoolean("shift_click.require_item", true)){
					main = player.getInventory().getItemInMainHand();
					if(debug){logDebug("PIEE - main.getType()=" + main.getType());}
					off = player.getInventory().getItemInOffHand();
					if(debug){logDebug("PIEE - off.getType()=" + off.getType());}
					if(getConfig().getBoolean("shift_click.require_workstation", true)){
						if(Workstation.isWorkstation(main.getType())||Workstation.isWorkstation(off.getType())){
							if(getConfig().getBoolean("particle.display", true)){
								if(!(workstation.getWorld().getNearbyEntities(workstation, .5, 1, .5) instanceof AreaEffectCloud)){
									drawLine(Particle.valueOf(getConfig().getString("particle.villager_line.name", "VILLAGER_HAPPY").toUpperCase()), clicked.getLocation(), workstation, 0.25, true);
									AreaEffectCloud cloud = (AreaEffectCloud) villager.getLocation().getWorld().spawnEntity(workstation.add(.5, 1, .5), EntityType.AREA_EFFECT_CLOUD);
									cloud.setParticle(Particle.valueOf(getConfig().getString("particle.workstation.name", "HEART").toUpperCase()), null);
									cloud.setDuration(getConfig().getInt("particle.duration", 200));
									cloud.setReapplicationDelay(10);
									cloud.setRadius(0.5f);
									cloud.setRadiusPerTick(0f);
									cloud.setRadiusOnUse(0f);
									if(debug){logDebug("PIEE - AreaEffectCloud set");}
									//Location blockLocation = new Location(workstation.getWorld(), workstation.getX(), workstation.getY(), workstation.getZ());
								}
							}
							//Material blockMaterial = workstation2.getBlock().getBlockData().getMaterial();
							String msg = lang.getString("vwh.workstation", "Workstation is  <daJob> at <X> , <Y>, <Z>");
							msg = msg.replace("<daJob>", daJob).replace("<X>", "" + x).replace("<Y>", "" + y).replace("<Z>", "" + z);
							msg = ChatColor.translateAlternateColorCodes('&', msg);
							player.sendMessage("" + msg);
						}else{
							show_particles = false;
						}
					}else{
						if(debug){logDebug("config material=" + getConfig().getString("shift_click.required_material", "crafting_table"));}
						if(debug){logDebug("getMaterial=" + Material.getMaterial(getConfig().getString("shift_click.required_material", "crafting_table").toUpperCase()).toString());}
						material = new ItemStack(Material.getMaterial(getConfig().getString("shift_click.required_material", "crafting_table").toUpperCase()), 1);
						if(main.getType().equals(material.getType())||off.getType().equals(material.getType())){
							if(getConfig().getBoolean("particle.display", true)){
								if(!(workstation.getWorld().getNearbyEntities(workstation, .5, 1, .5) instanceof AreaEffectCloud)){
									drawLine(Particle.valueOf(getConfig().getString("particle.villager_line.name", "VILLAGER_HAPPY").toUpperCase()), clicked.getLocation(), workstation, 0.25, true);
									AreaEffectCloud cloud = (AreaEffectCloud) villager.getLocation().getWorld().spawnEntity(workstation.add(.5, 1, .5), EntityType.AREA_EFFECT_CLOUD);
									cloud.setParticle(Particle.valueOf(getConfig().getString("particle.name", "HEART").toUpperCase()), null);
									cloud.setDuration(getConfig().getInt("particle.duration", 200));
									cloud.setReapplicationDelay(10);
									cloud.setRadius(0.5f);
									cloud.setRadiusPerTick(0f);
									cloud.setRadiusOnUse(0f);
									if(debug){logDebug("PIEE - AreaEffectCloud set");}
									//Location blockLocation = new Location(workstation.getWorld(), workstation.getX(), workstation.getY(), workstation.getZ());
								}
							}
							Material blockMaterial = workstation2.getBlock().getBlockData().getMaterial();
							String msg = lang.getString("vwh.workstation", "Workstation is  <daJob> at <X> , <Y>, <Z>");
							msg = msg.replace("<daJob>", daJob).replace("<X>", "" + x).replace("<Y>", "" + y).replace("<Z>", "" + z);
							msg = ChatColor.translateAlternateColorCodes('&', msg);
							player.sendMessage("" + msg);
						}else{
							if(debug){logDebug("PIEE - materials not matching");}
							show_particles = false;
						}
					}
				}else{
					if(getConfig().getBoolean("particle.display", true)){
						if(!(workstation.getWorld().getNearbyEntities(workstation, .5, 1, .5) instanceof AreaEffectCloud)){
							drawLine(Particle.valueOf(getConfig().getString("particle.villager_line.name", "VILLAGER_HAPPY").toUpperCase()), clicked.getLocation(), workstation, 0.25, true);
							AreaEffectCloud cloud = (AreaEffectCloud) villager.getLocation().getWorld().spawnEntity(workstation.add(.5, 1, .5), EntityType.AREA_EFFECT_CLOUD);
							cloud.setParticle(Particle.valueOf(getConfig().getString("particle.name", "HEART").toUpperCase()), null);
							cloud.setDuration(getConfig().getInt("particle.duration", 200));
							cloud.setReapplicationDelay(10);
							cloud.setRadius(0.5f);
							cloud.setRadiusPerTick(0f);
							cloud.setRadiusOnUse(0f);
							if(debug){logDebug("PIEE - AreaEffectCloud set");}
							//Location blockLocation = new Location(workstation.getWorld(), workstation.getX(), workstation.getY(), workstation.getZ());
						}
					}
					Material blockMaterial = workstation2.getBlock().getBlockData().getMaterial();
					String msg = lang.getString("vwh.workstation", "Workstation is  <daJob> at <X> , <Y>, <Z>");
					msg = ChatColor.translateAlternateColorCodes('&', msg);
					msg = msg.replace("<daJob>", daJob).replace("<X>", "" + x).replace("<Y>", "" + y).replace("<Z>", "" + z);
					player.sendMessage("" + msg);
				}
			
		//if(Workstation.isWorkstation(main.getType())||Workstation.isWorkstation(off.getType())){
			
				//player.isSneaking()
				//if(player.hasPermission("vwhighlights.click")){
					
					
						/**
						if(getConfig().getBoolean("particle.display", true)){
							if(!(workstation.getWorld().getNearbyEntities(workstation, .5, 1, .5) instanceof AreaEffectCloud)){
								AreaEffectCloud cloud = (AreaEffectCloud) villager.getLocation().getWorld().spawnEntity(workstation.add(.5, 1, .5), EntityType.AREA_EFFECT_CLOUD);
								cloud.setParticle(Particle.valueOf(getConfig().getString("particle.name", "HEART").toUpperCase()), null);
								cloud.setDuration(getConfig().getInt("particle.duration", 200));
								cloud.setReapplicationDelay(10);
								cloud.setRadius(0.5f);
								cloud.setRadiusPerTick(0f);
								cloud.setRadiusOnUse(0f);
								if(debug){logDebug("PIEE - AreaEffectCloud set");}
								//Location blockLocation = new Location(workstation.getWorld(), workstation.getX(), workstation.getY(), workstation.getZ());
							}
						}
						Material blockMaterial = workstation2.getBlock().getBlockData().getMaterial();
						player.sendMessage("Workstation is " + blockMaterial.toString() + " at " + workstation2.getX() + ", " + workstation2.getY() + ", " + workstation2.getZ());
						*/
			}else{
				String msg = lang.getString("vwh.unemployed", "That villager is unemployed.");
				msg = ChatColor.translateAlternateColorCodes('&', msg);
				player.sendMessage("" + msg);
				if(debug){logDebug("PIEE - workstation = null");}
			}
				//}
		}else{
			if(debug){logDebug("PIEE - !isVillager");}
		}
		if(debug){logDebug("PIEE - ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");}
			//event.setCancelled(false);
		//}
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
		{
		Player p = event.getPlayer();
		//if(p.isOp() && UpdateCheck||p.hasPermission("sps.showUpdateAvailable")){	
		/** Notify Ops */
		if(UpdateAvailable&&(p.isOp()||p.hasPermission("vwhighlights.showUpdateAvailable"))){
			p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + "  " + lang.getString("vwh.new_vers", "NEW VERSION AVAILABLE!") + " " + 
					" \n" + ChatColor.GREEN + UpdateChecker.getResourceUrl() + ChatColor.RESET);
		}

		if(p.getDisplayName().equals("JoelYahwehOfWar")||p.getDisplayName().equals("JoelGodOfWar")){
			p.sendMessage(this.getName() + " " + this.getDescription().getVersion() + " Hello father!");
			//p.sendMessage("seed=" + p.getWorld().getSeed());
		}
	}
	
	public static void copyFile_Java7(String origin, String destination) throws IOException {
		Path FROM = Paths.get(origin);
		Path TO = Paths.get(destination);
		//overwrite the destination file if it exists, and copy
		// the file attributes, including the rwx permissions
		CopyOption[] options = new CopyOption[]{
			StandardCopyOption.REPLACE_EXISTING,
			StandardCopyOption.COPY_ATTRIBUTES
		}; 
		Files.copy(FROM, TO, options);
	}
}
