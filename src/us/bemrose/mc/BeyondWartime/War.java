package us.bemrose.mc.BeyondWartime;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class War {

    // -------------------------------------
    // Classes/enums --------------------
    enum WarState {
        PENDING,
        RUNNING,
        ENDED,
        FUCKED
    };

    static class Team {

        String name;
        ChatColor color;

        Set<Player> players = new HashSet<Player>();

        Team(String n, ChatColor c) { name = n; color = c;}

        public String getName() { return name; }
        public ChatColor getColor() { return color; }

        public void addPlayer(Player p) { players.add(p); }
        public void removePlayer(Player p) { players.remove(p); }
        public boolean hasPlayer(Player p) { return players.contains(p); }
        
        /**
         * Gets the list of players belonging to a team.
         * @return Player[] - An array of Players.
         */
        public Player[] getPlayers() {
            Object[] javaSucks = players.toArray();
            Player[] returnMe = new Player[javaSucks.length];
            for (int i=0; i<returnMe.length; i++) {
                if (javaSucks[i] instanceof Player)
                {
                    returnMe[i] = (Player) javaSucks[i];
                } else {
                    returnMe[i] = null;
                }
            }
            return returnMe;
        }
        public int size() { return players.size(); }

    }

    static class WarNode {
        public String name;
        public Location location;
        public int captureCounter = 0;
        public Team owner = null;
        public Team captureTeamTemp;
        
        public List<Player> capturingPlayers = new LinkedList<Player>();

        public Map<Team, Integer> teamCounters = new HashMap<Team, Integer>();
        public boolean conquered = false;

        public WarNode(String n, Location l) {
            name = n;
            location = l;
        };
    }

    // -------------------------------------
    // Members --------------------
    WarState state = WarState.PENDING;
    LinkedList<Player> unassignedPlayers = new LinkedList<Player>();
    ArrayList<WarNode> nodes = new ArrayList<WarNode>();
    List<Team> teams = new ArrayList<Team>();
    Map<Player, WarClass> playerClasses = new HashMap<Player, WarClass>();
    boolean allNodesConquered = false;
    int ticksToConquer = 0;
    int captureRadiusSquared = 3*3;
    org.bukkit.World world;
    Date beginTime = null;
    Date endTime = null;
    Date cancelTimer = null;
    Map<String, Team> loggedPlayers = new HashMap<String, Team>();
    java.util.Random random = new java.util.Random();
    List<Player> playersWhoNeedStartingKit = new LinkedList<Player>();
    ConfigurationSection config;

    static final Team Contested = new Team("Contested",ChatColor.BLACK);

    static int cancelStreak = 0;
    
    // -------------------------------------
    // Constructor --------------------
    public War(int startDelayMinutes, int durationMinutes, List<BeyondWartime.Warzone> zones, ConfigurationSection conf) {

        config = conf;

        for (BeyondWartime.Warzone z : zones) {
            WarNode wn = new WarNode(z.name, z.location);
            nodes.add(wn);
        }
        if (nodes.size() == 0) {
            cancel("No warzones defined");
            return;
        }
        String worldName = config.getString("world");
        world = Bukkit.getWorld(worldName);
        if (world == null) {
            cancel("World '" + worldName + "' is not loaded.");
            return;
        }
        
        int captureRadius = config.getInt("capture_radius", 3);
        captureRadiusSquared = captureRadius * captureRadius;

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, startDelayMinutes);
        beginTime = c.getTime();
        c.add(Calendar.MINUTE, durationMinutes);
        endTime = c.getTime();

        ticksToConquer = durationMinutes * 30;
        
        WarClass.initClasses(config.getConfigurationSection("classes"));
        
        postWarPendingNotice();
    }

    // -------------------------------------
    // Helpers --------------------

    public Team getPlayerTeam(Player p) {
        for (Team t : teams) {
            if (t.hasPlayer(p)) { return t; }
        }
        return null;
    }
    
    public boolean isFighting(Player p) {
        return (getPlayerTeam(p) != null);
    }

    public boolean fightingOnDifferentTeams(Player p1, Player p2) {
        Team t1 = getPlayerTeam(p1);
        Team t2 = getPlayerTeam(p2);
        return ((t1 != null) && (t2 != null) && (t1 != t2));
    }
    
    int getWarPlayerCount()  {
        int count = unassignedPlayers.size();
        
        for (Team t : teams) {
            count += t.size();
        }
        return count;
    }
    
    // -------------------------------------
    // Methods --------------------
    public boolean registerPlayer(Player p) {
        if (unassignedPlayers.contains(p)) {
            return false;
        }
        if (getPlayerTeam(p) != null) {
            return false;
        }
        unassignedPlayers.add(p);

        if (p.getLocation().getWorld() != world) {
            p.teleport(world.getSpawnLocation());
        }

        if (cancelTimer != null) {
            if (getWarPlayerCount() >= config.getInt("minimum_players", 6)) {
                cancelTimer = null;
            }
        }

        return true;
    }

    public void unregisterPlayer(Player p) {
        Team t = getPlayerTeam(p);
        if (t != null) {
            t.removePlayer(p);
            // Remember the team they were on, so they get put there if they re-join
            loggedPlayers.put(p.getName(), t);
        }
        else if (unassignedPlayers.contains(p)) {
            unassignedPlayers.remove(p);
        }

        // Check if there's enough players to maintain the war.
        if (getWarPlayerCount() < config.getInt("minimum_players", 6)) {
            int warTimeoutSeconds = config.getInt("not_enough_player_timeout");
            Calendar c = Calendar.getInstance();
            c.add(Calendar.SECOND, warTimeoutSeconds);

            cancelTimer = c.getTime();
        }        
    }
    
    public void setPlayerClass(Player p, String className) {
        WarClass warclass = WarClass.getClass(className);
        if (warclass != null) {
            playerClasses.put(p, warclass);
            p.sendMessage("You are now in the " + warclass.getName() + " class.");
        }
        else {
            p.sendMessage("You are now in the default class.");
        }
        if (state == WarState.RUNNING) {
            applyPlayerClass(p);
        }
    }
    
    public String getPlayerClass(Player p) { 
        WarClass wc = playerClasses.get(p);
        return (wc == null) ? null : wc.getName();
    }

    public boolean warHasEnded() { return state == WarState.ENDED; }
    
    public void cancel(String reason) { 
        Bukkit.getServer().broadcastMessage("War is canceled: " + reason);
        state = WarState.ENDED;

        cancelStreak++;
        // Too many cancellations mean stop trying to restart.  Reset with /war start
        // if (cancelStreak > 10) {
        //     state = WarState.FUCKED;
        // }
    }
    
    void begin() {

        int minPlayers = config.getInt("minimum_players", 6);
        if (unassignedPlayers.size() < minPlayers) {
            cancel("Less than " + minPlayers + " people signed up.");
            return;
        }
    
        Map<String,ChatColor> teamNames = new HashMap<String,ChatColor>(){{
        	put("Marauders",ChatColor.GREEN);
        	put("Crusaders",ChatColor.WHITE);
        	put("Destroyers",ChatColor.DARK_AQUA);
        	put("Bonebreakers",ChatColor.GRAY);
        	put("Buccaneers",ChatColor.AQUA);
        	put("Plunderers",ChatColor.GOLD);
        	put("Assassins",ChatColor.RED);
        	put("Templars",ChatColor.DARK_PURPLE);
        	put("Imperials",ChatColor.LIGHT_PURPLE);
        	put("Stormcloaks",ChatColor.BLUE);

        }};
        int numTeams = config.getInt("number_of_teams", 3);
        for (int i = 0; i < numTeams; i++) {
        	String rndTeamName = (String)teamNames.keySet().toArray()[new Random().nextInt(teamNames.size())];
            teams.add(new Team(rndTeamName, teamNames.remove(rndTeamName)));
        }

        broadcastWorldMessage(world, "War is starting!  Teams are as follows:");

        assignPlayers(false);
        postWarTeams(null);

        LinkedList<WarNode> nodesClone = new LinkedList<WarNode>();
        nodesClone.addAll(nodes);
        java.util.Collections.shuffle(nodesClone);

        // tp all team members to the top of a random node block
        for (Team t : teams) {
            Location dest = nodesClone.pop().location;
            dest = new Location(dest.getWorld(), dest.getBlockX(), dest.getBlockY() + 2, dest.getBlockZ());
            for (Player p : t.getPlayers()) {
                p.teleport(dest);
                applyPlayerClass(p);
            }
        }

        state = WarState.RUNNING;        
    }

    void assignPlayers(boolean broadcastAdditions) {

        // Repeatedly finds smallest team and adds a player
        while(unassignedPlayers.size() > 0) {
            Player p = unassignedPlayers.pop();
            Team team = null;

            if (loggedPlayers.containsKey(p.getName())) {
                team = loggedPlayers.get(p.getName());
            }
            else {
                int smallestValue = Integer.MAX_VALUE;
                for (Team t : teams) {
                    int size = t.size();
                    // If sizes are equal, 60% chance we'll switch our guess.  This way it doesn't load up the first team always.
                    if (size < smallestValue || ((size == smallestValue) && (random.nextInt(100) < 60))) {
                        smallestValue = size;
                        team = t;
                    }
                }
            }
            if (team != null) {
                team.addPlayer(p);
                if (broadcastAdditions) {
                    String message = "" + ChatColor.AQUA + p.getName();
                    message = message + ChatColor.GRAY + " has joined the war on team ";
                    message = message + ChatColor.GOLD + team.getName();
                    broadcastWorldMessage(world, message);
                }
            }
            applyPlayerClass(p);
        }
    }

    public void executeMaintenanceTick() {
        if (state == WarState.RUNNING) {
            postWarScoreboard(null);
        }
        else if (state == WarState.PENDING) {
            postWarPendingNotice();
        }
    }

    public void postWarPendingNotice() {

        final String[] warNotices = new String[] {
                "Put down the chicken and back away from that melon patch, ",
                "Nail down your armor and load up on ender pearls, ",
                "Put away the pickaxe and grab a sword, ",
                "Don't place another block, ",
                "Put that giant Lihad statue on hold, ",
                "Grab a bow and guzzle a potion, ",
                "Prep your weapons and shut off the redstone cactus massager, ",
                "Sharpen your armor and buff your sword, ",
                "Get ready to rumble! ",
                "Look out below, "
        };


        Date now = new Date();
        if (now.after(beginTime)) {
            begin();
        }
        else {
            long mins = (beginTime.getTime() - now.getTime()) / (1000 * 60);
            int noticeIndex = random.nextInt(warNotices.length);
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE.toString() + warNotices[noticeIndex] + ChatColor.GOLD + "WAR is starting in " + mins + " minutes!");
            Bukkit.getServer().broadcastMessage(ChatColor.GRAY.toString() + "Type /war join to get in on the action.");
        }
    }

    public void postWarTeams(org.bukkit.command.CommandSender sender){
        for (Team team : this.teams) {
            String message = ChatColor.GOLD + team.getName() +" Members | ";

            boolean noComma = true;
            for (Player player: team.getPlayers()) {
                if (!noComma)
                    message+=ChatColor.GOLD + ",";
                else
                    noComma = false;
                message += ChatColor.GREEN + player.getName();
            }
            if (sender != null) {
                sender.sendMessage(message);
            }
            else {
                broadcastWorldMessage(world, message);
            }
        }
    }

    public void postWarScoreboard(org.bukkit.command.CommandSender sender){
    	Date now = new Date();
    	for (WarNode node : nodes) {
    		String message = ChatColor.GOLD + node.name +" Tally | ";

    		if (node.conquered) {
    			message += ChatColor.GREEN + "Conquered by " + node.owner.getName();
    		}
    		else if (node.teamCounters.isEmpty()) {
    			message += ChatColor.DARK_AQUA + "Not claimed by any team!";
    		}
    		else {
    			for (Map.Entry entry : node.teamCounters.entrySet()) {
    				Team team = (Team)entry.getKey();
    				int count = (Integer)entry.getValue();

    				String countText = (team == node.owner) ? ("" + ChatColor.YELLOW + "[" + count + "]") : ("" + ChatColor.WHITE + count);
    				message += "" + ChatColor.AQUA + team.getName() + ": " + countText + "; ";
    			}
    		}

    		if (sender != null) {
    			sender.sendMessage(message);
    		}
            else {
                broadcastWorldMessage(world, message);
            }
    		
        }
        if (sender != null) {
            sender.sendMessage("Minutes Remaining: "+(endTime.getTime() - now.getTime()) / (1000 * 60));
        }
        else {
            broadcastWorldMessage(world, "Minutes Remaining: "+(endTime.getTime() - now.getTime()) / (1000 * 60));
        }
        
    }

    public void executeWarTick() {

        if (state == WarState.ENDED) {
            // Nothing to do here.
            return;
        }
    
        if (state == WarState.PENDING) {
            Date now = new Date();
            if (now.after(beginTime)) {
                begin();
            }
            return;
        }

        // Otherwise, war is running

        // Check if it should end.
        Date now = new Date();
        if (allNodesConquered || now.after(endTime)) {
            endWar();
            return;
        }
        
        if (cancelTimer != null && now.after(cancelTimer)) {
            cancel("Too few players to carry on the fight.");
        }

        if (unassignedPlayers.size() > 0) {
            // Somebody joined late
            assignPlayers(true);
        }

        for (WarNode node : nodes) {
            node.captureTeamTemp = null;
            node.capturingPlayers.clear();
        }

        List<Player> players = world.getPlayers();

        for(Player player : players){
            if (getPlayerTeam(player) == null) {
                // Ignore anyone not in the war
                continue;
            }
            if(player.getLocation().getWorld() == world){

                for (WarNode node : nodes) {
                    if (node.conquered) {
                        continue;
                    }
                    if (player.getLocation().distanceSquared(node.location) < captureRadiusSquared){  
                        if(node.captureTeamTemp == Contested){
                            continue;
                        }
                        else if( getPlayerTeam(player) == node.owner ) {
                            node.captureTeamTemp = getPlayerTeam(player);
                            continue;
                        }
                        if(node.captureTeamTemp == null){
                            node.captureTeamTemp = getPlayerTeam(player);
                            node.captureCounter++;
                            player.sendMessage(ChatColor.GOLD+"Taking point. "+node.captureCounter+"/30");
                            node.capturingPlayers.add(player);
                        }
                        else if( getPlayerTeam(player) == node.captureTeamTemp ) {
                            node.captureCounter++;
                            player.sendMessage(ChatColor.GOLD+"Taking point. "+node.captureCounter+"/30");
                            node.capturingPlayers.add(player);
                        }else{
                            node.captureTeamTemp = Contested;
                            node.captureCounter = 0;
                        }
                    }
                }
            }
        }

        boolean unconqueredNodesRemain = false;

        for (WarNode node : nodes) {
            if (!node.conquered) {
                unconqueredNodesRemain = true;

                if((node.captureTeamTemp != null) && (node.captureTeamTemp != Contested) && node.captureCounter >= 30) {
                    node.owner = node.captureTeamTemp;
                    node.captureCounter = 0;
                    broadcastWorldMessage(world, "The " + ChatColor.RED + node.owner.getName() + " have taken control of " + node.name + "!");

                    for (Player p : node.capturingPlayers) {
                        Statistics.incrementNodesCaptured(p);
                    }

                    if (!node.teamCounters.containsKey(node.owner)) {
                        node.teamCounters.put(node.owner, 0);
                    }
                }

                if (node.owner != null) {
                    int counter = node.teamCounters.get(node.owner);
                    counter++;

                    if (counter > ticksToConquer) {
                        node.conquered = true;
                    }
                    node.teamCounters.put(node.owner, counter);
                }
            }
        }
        if (!unconqueredNodesRemain) {
            allNodesConquered = true;
        }
    }

    public void endWar() {
        state = WarState.ENDED;

        Bukkit.getServer().broadcastMessage(ChatColor.RED+"Lay down your weapons and shag a wench!  The war has ended!");

        for (WarNode node : nodes) {

            Team winner = null;

            if (node.conquered) {
                winner = node.owner;
            }
            else {
                // Not conquered.  We have to go through the cities and find the highest
                int highestScore = 0;
                for (Map.Entry entry : node.teamCounters.entrySet()) {
                    if ((Integer)entry.getValue() > highestScore) {
                        highestScore = (Integer)entry.getValue();
                        winner = (Team)entry.getKey();
                    }
                }
            }

            if (winner == null) {
            	Bukkit.getServer().broadcastMessage("" + ChatColor.GRAY + "Nobody captured the " + node.name);
                continue;
            }
            
            Bukkit.getServer().broadcastMessage("" + ChatColor.GOLD + winner.getName() + ChatColor.GRAY + " has won the " + node.name);

            //Reward
            if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            	Bukkit.getServer().broadcastMessage("Can't find economy plugin!  Nobody gets any rewards!");
            }
            //RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            // if (rsp == null) {
                // return false;
            // }
            
            // If economy isn't found, force an exception
            net.milkbowl.vault.economy.Economy econ = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();

            for (Player p : winner.getPlayers()) {
            	double rewardAmount = config.getDouble("rewards.conquer_zone", 100);
            	econ.bankDeposit(p.getName(), rewardAmount);
            	Statistics.incrementWins(p);
            	p.sendMessage("You were rewarded "+ChatColor.GOLD+rewardAmount+" "+ChatColor.WHITE+econ.currencyNameSingular());
            }
        }
        for(int j = 0; j<teams.size();j++){
        	for(int i = 0; i<teams.get(j).getPlayers().length;i++){

        		teams.get(j).getPlayers()[i].sendMessage(ChatColor.GRAY+"Kills: "+ChatColor.GREEN+Statistics.getKills(teams.get(j).getPlayers()[i])
        				+ChatColor.GRAY+"Deaths: "+ChatColor.GREEN+Statistics.getDeaths(teams.get(j).getPlayers()[i])+" "+ChatColor.AQUA+"("+(Statistics.getKills(teams.get(j).getPlayers()[i])/(double)Statistics.getDeaths(teams.get(j).getPlayers()[i]))+")");
        	}
        }
        Statistics.endRound();
        cancelStreak = 0;
    }

    public void applyPlayerClass(Player player) {
    	if (playerClasses.containsKey(player)) {
    		playerClasses.get(player).applyInv(player);
        }
        else {
            WarClass.getClass("default").applyInv(player);
        }
    }
    
    static void broadcastWorldMessage(org.bukkit.World world, String message) {
        String worldName = world.getName();
        for (Player p : world.getPlayers()) {
            p.sendMessage("[" + worldName + "] " + message);
        }
    }
};
