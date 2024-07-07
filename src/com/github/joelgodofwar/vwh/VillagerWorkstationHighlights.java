package com.github.joelgodofwar.vwh;

import java.io.File;
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
import java.util.concurrent.TimeUnit;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.github.joelgodofwar.vwh.common.PluginLibrary;
import com.github.joelgodofwar.vwh.common.PluginLogger;
import com.github.joelgodofwar.vwh.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.vwh.common.error.Report;
import com.github.joelgodofwar.vwh.i18n.Translator;
import com.github.joelgodofwar.vwh.util.Metrics;
import com.github.joelgodofwar.vwh.util.Utils;
import com.github.joelgodofwar.vwh.util.Version;
import com.github.joelgodofwar.vwh.util.VersionChecker;
import com.github.joelgodofwar.vwh.util.Workstation;
import com.github.joelgodofwar.vwh.util.YmlConfiguration;

@SuppressWarnings("unused")
public class VillagerWorkstationHighlights extends JavaPlugin implements Listener{
	/** Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SV), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW) */
	// public final static Logger logger = Logger.getLogger("Minecraft");
	static String THIS_NAME;
	static String THIS_VERSION;
	/** update checker variables */
	public int projectID = 81498; // https://spigotmc.org/resources/71236
	public String githubURL = "https://github.com/JoelGodOfwar/VillagerWorkstationHighlights/raw/master/versioncheck/1.14/versions.xml";
	boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
	public String DownloadLink = "https://www.spigotmc.org/resources/villager-workstation-highlights.81498";
	/** end update checker variables */
	public static boolean debug;
	public static String daLang;
	FileConfiguration lang;
	YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
	String pluginName = THIS_NAME;
	Translator lang2;
	boolean handSuccess = false;
	public boolean colorful_console;
	public String jarfilename = this.getFile().getAbsoluteFile().toString();
	public static DetailedErrorReporter reporter;
	public PluginLogger LOGGER;

	@Override
	public void onLoad() {
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		lang2 = new Translator(daLang, getDataFolder().toString());
		THIS_NAME = this.getDescription().getName();
		THIS_VERSION = this.getDescription().getVersion();
		if(!getConfig().getBoolean("console.longpluginname", true)) {
			pluginName = "VWH";
		}else {
			pluginName = THIS_NAME;
		}
	}

	@Override // TODO:
	public void onEnable() {
		long startTime = System.currentTimeMillis();
		LOGGER = new PluginLogger(this);
		reporter = new DetailedErrorReporter(this);
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		lang2 = new Translator(daLang, getDataFolder().toString());
		THIS_NAME = this.getDescription().getName();
		THIS_VERSION = this.getDescription().getVersion();
		if(!getConfig().getBoolean("console.longpluginname", true)) {
			pluginName = "VWH";
		}else {
			pluginName = THIS_NAME;
		}

		LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.GREEN + "v" + THIS_VERSION + ChatColor.RESET + " Loading...");
		LOGGER.log("Jar Filename: " + this.getFile().getName());//.getAbsoluteFile());
		LOGGER.log("Server Version: " + getServer().getVersion().toString());

		LOGGER.log("DEV Version Check...");
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			LOGGER.debug(ChatColor.RED + "Jar file contains -DEV, debug set to true" + ChatColor.RESET);
			//log("jarfile contains dev, debug set to true.");
		}
		LOGGER.log(": Loading Config File...");
		/**  Check for config */
		try{
			if(!getDataFolder().exists()){
				LOGGER.log("Data Folder doesn't exist");
				LOGGER.log("Creating Data Folder");
				getDataFolder().mkdirs();
				LOGGER.log("Data Folder Created at " + getDataFolder());
			}
			File  file = new File(getDataFolder(), "config.yml");
			LOGGER.log("" + file);
			if(!file.exists()){
				LOGGER.log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_CHECK_CONFIG).error(exception));
		}
		/** end config check */

		/**  Check config version */
		try {
			oldconfig.load(new File(getDataFolder(), "config.yml"));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
		}
		String checkconfigversion = oldconfig.getString("version", "1.0.0");
		if(checkconfigversion != null){
			if(!checkconfigversion.equalsIgnoreCase("1.0.4")){
				try {
					copyFile_Java7(getDataFolder() + "" + File.separatorChar + "config.yml", getDataFolder() + "" + File.separatorChar + "old_config.yml");
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_COPY_FILE).error(exception));
				}
				saveResource("config.yml", true);
				try {
					config.load(new File(getDataFolder(), "config.yml"));
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
				}
				try {
					oldconfig.load(new File(getDataFolder(), "old_config.yml"));
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
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
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_CONFIG).error(exception));
				}
				LOGGER.log("config.yml Updated! old config saved as old_config.yml");
			}
		}

		/** Update Checker */
		if(UpdateCheck){
			try {
				Bukkit.getConsoleSender().sendMessage("Checking for updates...");
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					/** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();

					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("vwh.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
					LOGGER.log("* " + get("vwh.version.old_vers") + ChatColor.RED + UColdVers );
					LOGGER.log("* " + get("vwh.version.new_vers") + ChatColor.GREEN + UCnewVers );
					LOGGER.log("*");
					LOGGER.log("* " + get("vwh.version.please_update") );
					LOGGER.log("*");
					LOGGER.log("* " + get("vwh.version.download") + ": " + DownloadLink + "/history");
					LOGGER.log("* " + get("vwh.version.donate") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
				}else{
					/** Up to date */
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("vwh.version.curvers"));
					LOGGER.log("* " + get("vwh.version.donate") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					UpdateAvailable = false;
				}
			}catch(Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
			}
		}else {
			/** auto_update_check is false so nag. */
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			LOGGER.log("* " + get("vwh.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}
		/** end update checker */

		Bukkit.getPluginManager().registerEvents(this, this);
		consoleInfo("ENABLED - Loading took " + LoadTime(startTime));

		if((getConfig().getBoolean("debug")==true)&&!(jarfile.toString().contains("-DEV"))){
			LOGGER.debug("Config.yml dump");
			LOGGER.debug("auto_update_check=" + getConfig().getBoolean("auto_update_check"));
			LOGGER.debug("debug=" + getConfig().getBoolean("debug"));
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

		}catch (Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_METRICS_LOAD_ERROR).error(exception));
		}
	}

	@Override // TODO:
	public void onDisable() {
		lang2.clearVars();
		lang2 = null;
		consoleInfo("DISABLED");
	}

	public void consoleInfo(String state) {
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.YELLOW + " v" + THIS_VERSION + ChatColor.RESET + " is " + state  + ChatColor.RESET);
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
	}

	@Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("VWH")||cmd.getName().equalsIgnoreCase("VillagerWorkstationHighlights")){
			if((args.length == 0)&&(sender instanceof Player)){
				Player player = (Player) sender;
				if(player.hasPermission("vwhighlights.command")){
					Entity entity = getNearestEntityInSight(player, 10);
					if(entity instanceof Villager){
						LOGGER.debug("CMD - entity=" + entity.toString());
						Villager villager = (Villager) entity;
						LOGGER.debug("CMD - villager=" + villager.toString());
						Location workstation = villager.getMemory(MemoryKey.JOB_SITE);
						if(workstation != null){
							Location workstation2 = workstation;
							int x = (int) workstation2.getX();
							int y = (int) workstation2.getY();
							int z = (int) workstation2.getZ();
							String daJob = workstation2.getBlock().getType().toString();
							LOGGER.debug("CMD - workstation=" + workstation.toString());
							LOGGER.debug("CMD - workstation2=" + workstation2.toString());
							//workstation2 = workstation2.subtract(0, 1, 0);
							LOGGER.debug("CMD - workstation2=" + workstation2.getBlock().getType().toString());
							if(!(workstation.getWorld().getNearbyEntities(workstation, .5, 1, .5) instanceof AreaEffectCloud)){
								AreaEffectCloud cloud = (AreaEffectCloud) villager.getLocation().getWorld().spawnEntity(workstation.add(.5, 1, .5), EntityType.AREA_EFFECT_CLOUD);
								cloud.setParticle(Particle.valueOf(getConfig().getString("particle.name", "HEART").toUpperCase()), null);
								cloud.setDuration(getConfig().getInt("particle.duration", 200));
								cloud.setReapplicationDelay(10);
								cloud.setRadius(0.5f);
								cloud.setRadiusPerTick(0f);
								cloud.setRadiusOnUse(0f);
								LOGGER.debug("AreaEffectCloud set");
								//Material blockMaterial = workstation2.getBlock().getType();
								//Location blockLocation = new Location(workstation.getWorld(), workstation.getX(), workstation.getY(), workstation.getZ());
								String msg = get("vwh.message.workstation", "Workstation is  <daJob> at <X> , <Y>, <Z>");
								msg = msg.replace("<daJob>", daJob).replace("<X>", "" + x).replace("<Y>", "" + y).replace("<Z>", "" + z);
								msg = ChatColor.translateAlternateColorCodes('&', msg);
								sender.sendMessage("" + msg);

							}
						}else{
							String msg = get("vwh.message.unemployed", "That villager is unemployed.");
							msg = ChatColor.translateAlternateColorCodes('&', msg);
							player.sendMessage("" + msg);
							LOGGER.debug("CMD - workstation = null");
							return false;
						}
						return true;
					}else{
						player.sendMessage("" + get("vwh.message.not_found") );
					}
				}
			}else if(args.length != 0){
				if(args[0].equalsIgnoreCase("reload")){
					if(sender.hasPermission("vwhighlights.reload")||!(sender instanceof Player)){
						//ConfigAPI.Reloadconfig(this, p);
						this.reloadConfig();
						VillagerWorkstationHighlights plugin = this;
						// getServer().getPluginManager().disablePlugin(plugin);
						// getServer().getPluginManager().enablePlugin(plugin);
						reloadConfig();
						try {
							config.load(new File(getDataFolder(), "config.yml"));
						} catch (Exception exception) {
							LOGGER.warn("Could not load config.yml");
							//e1.printStackTrace();
						}

						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("vwh.command.reloaded", "Has been reloaded!") );
						return true;
					}else if(!sender.hasPermission("vwhighlights.reload")){
						String msg = get("vwh.message.no_perm", "You do not have permission (<perm>)");
						msg = msg.replace("<perm>", "vwhighlights.reload");
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + msg);
						return false;
					}
				}
				if(args[0].equalsIgnoreCase("toggledebug")||args[0].equalsIgnoreCase("td")){
					String perm = "vwhighlights.toggledebug";
					if(sender.isOp()||sender.hasPermission(perm)||!(sender instanceof Player)){
						debug = !debug;
						String msg = get("vwh.command.debug_set", "DEBUG has been set to <debug_var>");
						msg = msg.replace("<debug_var>", "" + ChatColor.YELLOW  + debug);
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + msg);
						return true;
					}else if(!sender.hasPermission(perm)){
						String msg = get("vwh.message.no_perm", "You do not have permission (<perm>)");
						msg = msg.replace("<perm>", perm);
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + msg );
						return false;
					}
				}
			}else{
				sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("vwh.message.no_console", "Console can not use this command.") );
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
		for (int i = 0;i<sightBlock.size();i++) {
			sight.add(sightBlock.get(i).getLocation());
		}
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
					LOGGER.debug("added " + e.toString());
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
		if(handSuccess) {
			handSuccess = !handSuccess;
			return;
		}
		LOGGER.debug("PIE - vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
		Action action = event.getAction();
		LOGGER.debug("PIE - Action=" + action.name());
		if(action != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		Player player = event.getPlayer();
		boolean show_particles = true;
		ItemStack main;
		ItemStack off;
		ItemStack hand;
		ItemStack material;
		boolean reqItem = false;
		Block clicked = event.getClickedBlock();
		if((clicked == null) || (clicked.getType() == Material.REDSTONE_ORE)) {
			return;
		}


		LOGGER.debug("PIE - rightClicked=" + event.getClickedBlock().getType());
		LOGGER.debug("PIE - sneaking=" + player.isSneaking());
		LOGGER.debug("PIE - permission=" + player.hasPermission("vwhighlights.click"));
		try {
			if( event.getHand().equals(EquipmentSlot.HAND) || event.getHand().equals(EquipmentSlot.OFF_HAND) ) {

				main = player.getInventory().getItemInMainHand();
				LOGGER.debug("PIEE - main.getType()=" + main.getType());
				off = player.getInventory().getItemInOffHand();
				LOGGER.debug("PIEE - off.getType()=" + off.getType());

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
						player.sendMessage("" + get("vwh.message.villager_found").replace("<X>", "" + loc.getBlockX()).replace("<Y>", "" + loc.getBlockY()).replace("<Z>", "" + loc.getBlockZ()) );
						player.setCompassTarget(loc);
						event.setCancelled(true);
					}else {
						// failed to find villager within 50 blocks
						player.sendMessage("" + get("vwh.message.no_villager_50") );
						Location loc = player.getWorld().getSpawnLocation();
						player.setCompassTarget(loc);
					}

					handSuccess = true;
				}else {
					handSuccess = false;
				}
			}
		}catch(Exception exception){
			LOGGER.log("Exception caught, turn on debug to see it.");
			if(debug){exception.printStackTrace();}
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
			LOGGER.log("blockX=" + point1.getBlockX() + " blockZ=" + point1.getBlockZ() + " x=" + point1.getX() + " z=" + point1.getZ());
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
		LOGGER.debug("PIEE - vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
		LOGGER.debug("PIEE - rightClicked instance of villager=" + (event.getRightClicked() instanceof Villager));
		LOGGER.debug("PIEE - sneaking=" + player.isSneaking());
		LOGGER.debug("PIEE - permission=" + player.hasPermission("vwhighlights.click"));
		if((event.getRightClicked() instanceof Villager)&&player.isSneaking()&&player.hasPermission("vwhighlights.click")){
			Entity clicked = event.getRightClicked();
			event.setCancelled(true);
			LOGGER.debug("PIEE - isVillager");
			Villager villager = (Villager) event.getRightClicked();
			Location workstation = villager.getMemory(MemoryKey.JOB_SITE);
			if(workstation != null){
				Location workstation2 = workstation;
				int x = (int) workstation2.getX();
				int y = (int) workstation2.getY();
				int z = (int) workstation2.getZ();
				String daJob = workstation2.getBlock().getType().toString();
				LOGGER.debug("PIEE - workstation=" + workstation.toString());
				LOGGER.debug("CMD - workstation2=" + workstation2.toString());
				if(getConfig().getBoolean("shift_click.require_item", true)){
					main = player.getInventory().getItemInMainHand();
					LOGGER.debug("PIEE - main.getType()=" + main.getType());
					off = player.getInventory().getItemInOffHand();
					LOGGER.debug("PIEE - off.getType()=" + off.getType());
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
									LOGGER.debug("PIEE - AreaEffectCloud set");
									//Location blockLocation = new Location(workstation.getWorld(), workstation.getX(), workstation.getY(), workstation.getZ());
								}
							}
							//Material blockMaterial = workstation2.getBlock().getBlockData().getMaterial();
							String msg = get("vwh.message.workstation", "Workstation is  <daJob> at <X> , <Y>, <Z>");
							msg = msg.replace("<daJob>", daJob).replace("<X>", "" + x).replace("<Y>", "" + y).replace("<Z>", "" + z);
							msg = ChatColor.translateAlternateColorCodes('&', msg);
							player.sendMessage("" + msg);
						}else{
							show_particles = false;
						}
					}else{
						LOGGER.debug("config material=" + getConfig().getString("shift_click.required_material", "crafting_table"));
						LOGGER.debug("getMaterial=" + Material.getMaterial(getConfig().getString("shift_click.required_material", "crafting_table").toUpperCase()).toString());
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
									LOGGER.debug("PIEE - AreaEffectCloud set");
									//Location blockLocation = new Location(workstation.getWorld(), workstation.getX(), workstation.getY(), workstation.getZ());
								}
							}
							Material blockMaterial = workstation2.getBlock().getBlockData().getMaterial();
							String msg = get("vwh.message.workstation", "Workstation is  <daJob> at <X> , <Y>, <Z>");
							msg = msg.replace("<daJob>", daJob).replace("<X>", "" + x).replace("<Y>", "" + y).replace("<Z>", "" + z);
							msg = ChatColor.translateAlternateColorCodes('&', msg);
							player.sendMessage("" + msg);
						}else{
							LOGGER.debug("PIEE - materials not matching");
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
							LOGGER.debug("PIEE - AreaEffectCloud set");
							//Location blockLocation = new Location(workstation.getWorld(), workstation.getX(), workstation.getY(), workstation.getZ());
						}
					}
					Material blockMaterial = workstation2.getBlock().getBlockData().getMaterial();
					String msg = get("vwh.message.workstation", "Workstation is  <daJob> at <X> , <Y>, <Z>");
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
				String msg = get("vwh.message.unemployed", "That villager is unemployed.");
				msg = ChatColor.translateAlternateColorCodes('&', msg);
				player.sendMessage("" + msg);
				LOGGER.debug("PIEE - workstation = null");
			}
			//}
		} else {
			LOGGER.debug("PIEE - !isVillager");
			LOGGER.debug("PIEE - ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			//event.setCancelled(false);
			//}
		}
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		//if(p.isOp() && UpdateCheck||p.hasPermission("sps.showUpdateAvailable")){
		/** Notify Ops */
		if(UpdateAvailable&&(player.isOp()||player.hasPermission("vwhighlights.showUpdateAvailable"))){
			String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>\"}}]";
			links = links.replace("<DownloadLink>", DownloadLink).replace("<Download>", get("vwh.version.download"))
					.replace("<Donate>", get("vwh.version.donate")).replace("<please_update>", get("vwh.version.please_update"))
					.replace("<Donate_msg>", get("vwh.version.donate.message")).replace("<Notes>", get("vwh.version.notes"))
					.replace("<Notes_msg>", get("vwh.version.notes.message"));
			String versions = "" + ChatColor.GRAY + get("vwh.newvers.new_vers") + ": " + ChatColor.GREEN + "{nVers} | " + get("vwh.newvers.old_vers") + ": " + ChatColor.RED + "{oVers}";
			player.sendMessage("" + ChatColor.GRAY + get("vwh.newvers.message").toString().replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.GRAY) );
			Utils.sendJson(player, links);
			player.sendMessage(versions.replace("{nVers}", UCnewVers).replace("{oVers}", UColdVers));
			//p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + "  " + get("vwh.new_vers", "NEW VERSION AVAILABLE!") + " " + " \n" + ChatColor.GREEN + UpdateChecker.getResourceUrl() + ChatColor.RESET);
		}

		if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
			player.sendMessage(THIS_NAME + " " + THIS_VERSION + " Hello father!");
		}
	}

	public static void copyFile_Java7(String origin, String destination) throws Exception {
		try {
			Path FROM = Paths.get(origin);
			Path TO = Paths.get(destination);
			//overwrite the destination file if it exists, and copy
			// the file attributes, including the rwx permissions
			CopyOption[] options = new CopyOption[]{
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.COPY_ATTRIBUTES
			};
			Files.copy(FROM, TO, options);
		} catch (Exception exception) {

		}
	}

	public String LoadTime(long startTime) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
		long milliseconds = elapsedTime % 1000;

		if (minutes > 0) {
			return String.format("%d min %d s %d ms.", minutes, seconds, milliseconds);
		} else if (seconds > 0) {
			return String.format("%d s %d ms.", seconds, milliseconds);
		} else {
			return String.format("%d ms.", elapsedTime);
		}
	}

	@SuppressWarnings("static-access")
	public String get(String key, String... defaultValue) {
		return lang2.get(key, defaultValue);
	}

	/**public boolean isPluginRequired(String pluginName) {
		String[] requiredPlugins = {"SinglePlayerSleep", "MoreMobHeads", "NoEndermanGrief", "ShulkerRespawner", "DragonDropElytra", "RotationalWrench", "SilenceMobs", "VillagerWorkstationHighlights"};
		for (String requiredPlugin : requiredPlugins) {
			if ((getServer().getPluginManager().getPlugin(requiredPlugin) != null) && getServer().getPluginManager().isPluginEnabled(requiredPlugin)) {
				if (requiredPlugin.equals(pluginName)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return true;
	}//*/

	// Used to check Minecraft version
	private Version verifyMinecraftVersion() {
		Version minimum = new Version(PluginLibrary.MINIMUM_MINECRAFT_VERSION);
		Version maximum = new Version(PluginLibrary.MAXIMUM_MINECRAFT_VERSION);
		try {
			Version current = new Version(this.getServer());

			// We'll just warn the user for now
			if (current.compareTo(minimum) < 0) {
				LOGGER.warn("Version " + current + " is lower than the minimum " + minimum);
			}
			if (current.compareTo(maximum) > 0) {
				LOGGER.warn("Version " + current + " has not yet been tested! Proceed with caution.");
			}

			return current;
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_PARSE_MINECRAFT_VERSION).error(exception).messageParam(maximum));
			// Unknown version - just assume it is the latest
			return maximum;
		}
	}

	public String getjarfilename() {
		return jarfilename;
	}

	public boolean getDebug() {
		return debug;
	}

	public static VillagerWorkstationHighlights getInstance() {
		return getPlugin(VillagerWorkstationHighlights.class);
	}

}
