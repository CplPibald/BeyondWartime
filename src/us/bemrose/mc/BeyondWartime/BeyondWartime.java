package us.bemrose.mc.BeyondWartime;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.ChatColor;
import org.bukkit.entity.Projectile;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

import us.bemrose.mc.BeyondWartime.War.Team;

public class BeyondWartime
    extends org.bukkit.plugin.java.JavaPlugin
    implements org.bukkit.event.Listener, org.bukkit.command.CommandExecutor {

    public War war = null;
    java.util.List<Warzone> warzones = new java.util.LinkedList<Warzone>();
    java.util.Map<Player, ClickContext> pendingClicks = new java.util.HashMap<Player, ClickContext>();
    org.bukkit.World warWorld = null; 

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("war").setExecutor(this);

        loadConfiguration();
        
        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            public void run() {
                // Maintenance timer - runs every 5 mins
                if(war != null){
                    war.executeMaintenanceTick();
                }
            }
        },1200L, 3600L);
        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            public void run() {
                // War timer - runs every second
                if(war != null) {
                    if(war.WarHasEnded()) {
                        war = null;
                    }
                    else {
                        war.executeWarTick();
                    }
                }
            }
        },200L, 20L);

    }

    void loadConfiguration() {
        org.bukkit.configuration.ConfigurationSection config = getConfig();
        
        // Init and write defaults for variables
        boolean writeNewDefaults = false;
        if (!config.isSet("world")) {
            config.set("world", "survival");
            writeNewDefaults = true;
        }
        if (!config.isSet("number_of_teams")) {
            config.set("number_of_teams", 3);
            writeNewDefaults = true;
        }
        if (!config.isSet("capture_radius")) {
            config.set("capture_radius", 3);
            writeNewDefaults = true;
        }
        if (!config.isSet("rewards.conquer_zone")) {
            config.set("rewards.conquer_zone", 100);
            writeNewDefaults = true;
        }
        if (writeNewDefaults) { saveConfig(); }
        
        warWorld = Bukkit.getWorld(getConfig().getString("world"));
        // TODO: Throw a tantrum if this world isn't loaded.

        // Read zone list
        if (config.isConfigurationSection("zones")) {
            org.bukkit.configuration.ConfigurationSection zones = config.getConfigurationSection("zones");
            java.util.Set<String> zoneNames = zones.getKeys(false);
            for (String name : zoneNames) {
                String locationString = zones.getString(name);
                String[] parts = locationString.split(",");
                Location location = new Location(Bukkit.getServer().getWorld(parts[3]), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

                Warzone zone = new Warzone(name, location);
                warzones.add(zone);
            }
        }
    }
    
    void saveZoneList() {

        org.bukkit.configuration.ConfigurationSection zoneSection = getConfig().createSection("zones");
        
        for (Warzone zone : warzones) {
            Location l = zone.location;
            String locationString = ("" + l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ()+","+l.getWorld().getName());
            zoneSection.set(zone.name, locationString);
        }
        saveConfig();
    }
    
    // ------------------------------------
    // handlers ------------------


    @EventHandler
    public void onNameTag(PlayerReceiveNameTagEvent event) {
    	if(event.getNamedPlayer().getWorld().getName().equalsIgnoreCase("war") && war.isFighting(event.getNamedPlayer())){
    		if(war.getPlayerTeam(event.getPlayer()).equals(war.getPlayerTeam(event.getNamedPlayer()))){
    			event.setTag(war.getPlayerTeam(event.getNamedPlayer()).getColor() + event.getNamedPlayer().getName());
    		}else{
    			event.setTag(war.getPlayerTeam(event.getNamedPlayer()).getColor() + war.getPlayerTeam(event.getNamedPlayer()).getName());
    		}
    	}
    }
    @EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event){
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//
    	// PvP rules
    	// 
    	// PVP is blocked in the war world
    	//
        // During war, PVP is turned on between members of different teams.  Friendly fire is off, and PVP is off if you're not participating.
        //
        // Ops can hurt people everywhere.
        //
        if(event.getEntity().getWorld() != warWorld) {
            // alternate world
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            // Not a player
            return;
        }

        Player hurt = (Player)event.getEntity();

        if(!(event.getDamager() instanceof Player || (event.getDamager() instanceof Projectile && ((Projectile)event.getDamager()).getShooter() instanceof Player))) {
            // Attacker is not a player
            return;
        }

        Player attacker;
        if(event.getDamager() instanceof Player) attacker = (Player)event.getDamager();
        else attacker = (Player)((Projectile)event.getDamager()).getShooter();

        if(attacker.isOp()){
            // Ops can always hurt you.
            return;
        }

        if (attacker.getInventory().getHelmet() != null && attacker.getInventory().getHelmet().getType() == org.bukkit.Material.PUMPKIN 
            && hurt.getInventory().getHelmet() != null && hurt.getInventory().getHelmet().getType() == org.bukkit.Material.PUMPKIN) {
            // Pumpkin on the head means willing pvp
            return;
        }

        if (war != null) {
            if (war.fightingOnDifferentTeams(attacker, hurt)) {
                // Both players in war, on different teams
                return;
            }
        }
        // Couldn't find an excuse to allow it.  Guess we should cancel the pvp.
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (war != null) {
        	war.unregisterPlayer(player);
        }
        if (pendingClicks.containsKey(player)) {
        	pendingClicks.remove(player);
        }            
    }
    @EventHandler
    public void onPlayerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
    	if((event.getFrom().getWorld() == warWorld) && (event.getTo().getWorld() != warWorld)){
    		Player player = event.getPlayer();
    		if (war != null) {
    			war.unregisterPlayer(player);
    		}
    		if (pendingClicks.containsKey(player)) {
    			pendingClicks.remove(player);
    		}            
    	}
    }

    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event){
    
        Player player = event.getPlayer();
        // If player just entered a command, complete it here
        if (pendingClicks.containsKey(player)) {
        
            ClickContext context = pendingClicks.get(player);
            pendingClicks.remove(player);
            event.setCancelled(true);

            if ((new java.util.Date()).before(context.expiry)) {
                if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
                    org.bukkit.block.Block block = event.getClickedBlock();
                    if (block.getLocation().getWorld() == warWorld) {
                        Warzone zone = new Warzone(context.name, block.getLocation());
                        warzones.add(zone);
                        saveZoneList();
                        player.sendMessage("Warzone " + context.name + " created.");
                        return;
                    }
                    else {
                        player.sendMessage("Wrong world!  Need to be in " + warWorld.getName());
                    }
                }
            }
            player.sendMessage("Zone creation canceled.");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (war != null && war.isFighting(player)) {
            Location loc = event.getRespawnLocation();
            if (loc.getWorld() != warWorld) {
                event.setRespawnLocation(warWorld.getSpawnLocation());
            }
            war.allocatePlayerStartingKit(event.getPlayer());
        }
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String string, String[] arg) {
    
        if(cmd.getName().equalsIgnoreCase("war")) {

            if (arg.length == 0) {
                sender.sendMessage("/war join  -- Join current war");
                sender.sendMessage("/war stats -- See current war scoreboard");
                sender.sendMessage("/war teams -- See the current teams");
                if (sender.isOp()) {
                    sender.sendMessage("Op only:");
                    sender.sendMessage("/war start [duration-in-mins]");
                    sender.sendMessage("/war createzone <name>");
                    sender.sendMessage("/war removezone <name>");
                    sender.sendMessage("/war reload");
                }
                return true;
            }

            if (arg[0].equalsIgnoreCase("stats")) {
                if (war == null) { sender.sendMessage("Server is not at war."); return true; }
                war.postWarScoreboard(sender);
                return true;
            }

            if (arg[0].equalsIgnoreCase("join")) {
                if (war == null) { sender.sendMessage("Server is not at war.  Beg an admin to start one."); return true; }
                if (sender instanceof Player) {
                    sender.sendMessage("Put on your best gear, and load up on ender pearls.  You're signed up!");
                    if (war.registerPlayer((Player)sender)) {
                        Bukkit.getServer().broadcastMessage(ChatColor.GREEN.toString() + sender.getName() + ChatColor.WHITE + " straps on a pack and prepares for WAR!");
                    }
                }
                else {
                    sender.sendMessage("Console can't join wars! You don't have enough gear.");
                }
                return true;
            }

            if (arg[0].equalsIgnoreCase("teams")) {
                if (war == null) { sender.sendMessage("Server is not at war."); return true; }
                war.postWarTeams(sender);
                return true;
            }
                
            // Everything below here is op-only
            if (!sender.isOp()) { return false; }

            if (arg[0].equalsIgnoreCase("debug")) {
                if (war == null) { sender.sendMessage("Server is not at war."); return true; }
                sender.sendMessage("Unassigned players = " + war.unassignedPlayers);
                String message = "Logged players: ";
                for (java.util.Map.Entry entry : war.loggedPlayers.entrySet()) {
                    War.Team t = (War.Team)entry.getValue();
                    message += (String)entry.getKey() + "(" + (t != null ? t.getName() : null) + ")";
                }
                sender.sendMessage(message);
                return true;
            }
           
            if (arg[0].equalsIgnoreCase("start")) {
                if (warWorld == null) { sender.sendMessage("Config error!  'world' is not set to a loaded world!");  return true; }

                int nextWarDuration = (arg.length >= 2) ? Integer.parseInt(arg[1]) : 60;
                int nextWarStartMinutes = (int)java.lang.Math.ceil((double)nextWarDuration / 10.0);
                war = new War(nextWarStartMinutes, nextWarDuration, warzones, getConfig());
                sender.sendMessage("War will start in " + nextWarStartMinutes + " minutes, and last " + nextWarDuration + " minutes.");
                return true;
            }
            
            else if (arg[0].equalsIgnoreCase("createzone")) {
                if (warWorld == null) { sender.sendMessage("Config error!  'world' is not set to a loaded world!");  return true; }
                if (!(sender instanceof Player)) { sender.sendMessage("Console can't create a zone. How would you punch a block?!"); return true; }
                
                if (arg.length < 2) {
                    sender.sendMessage("/war createzone <name>");
                    return true;
                }
                ClickContext context = new ClickContext();
                context.name = arg[1];
                
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.add(java.util.Calendar.SECOND, 30);
                context.expiry = c.getTime();
                pendingClicks.put((Player)sender, context);

                sender.sendMessage("Left-click a block to create the zone '" + context.name + "'");
                return true;
            }

            if (arg[0].equalsIgnoreCase("removezone")) {
                if (warWorld == null) { sender.sendMessage("Config error!  'world' is not set to a loaded world!");  return true; }
                if (arg.length < 2) {
                    sender.sendMessage("/war removezone <name>");
                }
                else if (warzones.contains(arg[1])) {
                    warzones.remove(arg[1]);
                    getConfig().set("zones." + arg[1], null);
                    saveConfig();
                    sender.sendMessage("Zone '" + arg[1] + "' removed.");
                }
                else {
                    sender.sendMessage("Zone not found");
                }
                return true;
            }
            
            if (arg[0].equalsIgnoreCase("reload")) {
                loadConfiguration();
                return true;
            }
        }
        return false;
    }

    public class Warzone {
        String name;
        Location location;
        Warzone(String n, Location l) { name = n; location = l;}
    }

    class ClickContext {
        String name;
        
        // Commands expire if a block is not punched in 30s
        public java.util.Date expiry;
    }
}
