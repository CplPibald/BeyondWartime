package us.bemrose.mc.BeyondWartime;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Statistics implements Listener {
	
	public static YamlConfiguration statistics = new YamlConfiguration();
	private static File statFile = new File("plugins/BeyondWartime/statistics.yml");
	
	private static Map<String, Integer> kills = new HashMap<String, Integer>();
	private static Map<String, Integer> deaths = new HashMap<String, Integer>();
	private static Map<String, Integer> totalkills = new HashMap<String, Integer>();
	private static Map<String, Integer> totaldeaths = new HashMap<String, Integer>();
	private static Map<String, Integer> wins = new HashMap<String, Integer>();
	private static Map<String, Integer> losses = new HashMap<String, Integer>();
	private static Map<String, Integer> damagedealt = new HashMap<String, Integer>();
	private static Map<String, Integer> damagereceived = new HashMap<String, Integer>();
	private static Map<String, Integer> totaldamagedealt = new HashMap<String, Integer>();
	private static Map<String, Integer> totaldamagereceived = new HashMap<String, Integer>();
	private static Map<String, Integer> timeplayed = new HashMap<String, Integer>();
	private static Map<String, Integer> nodescaptured = new HashMap<String, Integer>();


	public static void init(){
		System.out.println("Begin reading wartime statistics...");
        loadStatFile(statistics, statFile);
        totalkills.clear();
        org.bukkit.configuration.ConfigurationSection section = statistics.getConfigurationSection("Players");
        if (section != null) {
            for(int i = 0;i< section.getKeys(false).size();i++){
                totalkills.put((String)section.getKeys(false).toArray()[i], statistics.getInt("Players."+section.getKeys(false).toArray()[i]+".totalkills"));
            }
            totaldeaths.clear();
            for(int i = 0;i<section.getKeys(false).size();i++){
                totaldeaths.put((String)section.getKeys(false).toArray()[i], statistics.getInt("Players."+section.getKeys(false).toArray()[i]+".totaldeaths"));
            }
            wins.clear();
            for(int i = 0;i<section.getKeys(false).size();i++){
                wins.put((String)section.getKeys(false).toArray()[i], statistics.getInt("Players."+section.getKeys(false).toArray()[i]+".wins"));
            }
            losses.clear();
            for(int i = 0;i<section.getKeys(false).size();i++){
                losses.put((String)section.getKeys(false).toArray()[i], statistics.getInt("Players."+section.getKeys(false).toArray()[i]+".losses"));
            }
            totaldamagedealt.clear();
            for(int i = 0;i<section.getKeys(false).size();i++){
                totaldamagedealt.put((String)section.getKeys(false).toArray()[i], statistics.getInt("Players."+section.getKeys(false).toArray()[i]+".totaldamagedealt"));
            }
            totaldamagereceived.clear();
            for(int i = 0;i<section.getKeys(false).size();i++){
                totaldamagereceived.put((String)section.getKeys(false).toArray()[i], statistics.getInt("Players."+section.getKeys(false).toArray()[i]+".totaldamagereceived"));
            }
            timeplayed.clear();
            for(int i = 0;i<section.getKeys(false).size();i++){
                timeplayed.put((String)section.getKeys(false).toArray()[i], statistics.getInt("Players."+section.getKeys(false).toArray()[i]+".timeplayed"));
            }
            nodescaptured.clear();
            for(int i = 0;i<section.getKeys(false).size();i++){
            	nodescaptured.put((String)section.getKeys(false).toArray()[i], statistics.getInt("Players."+section.getKeys(false).toArray()[i]+".nodescaptured"));
            }
        }
		System.out.println("End reading wartime statistics...");
	}
	public static void debug(){
		System.out.println("kills: "+kills.toString());
		System.out.println("deaths: "+deaths.toString());
		System.out.println("totalkills: "+totalkills.toString());
		System.out.println("totaldeaths: "+totaldeaths.toString());
		System.out.println("wins: "+wins.toString());
		System.out.println("losses: "+losses.toString());
		System.out.println("damagedealt: "+damagedealt.toString());
		System.out.println("damagereceived: "+damagereceived.toString());
		System.out.println("totaldamagedealt: "+totaldamagedealt.toString());
		System.out.println("totaldamagereceived: "+totaldamagereceived.toString());
		System.out.println("timeplayed: "+timeplayed.toString());
		System.out.println("nodescaptured: "+nodescaptured.toString());

	}
	private static void loadStatFile(YamlConfiguration config, File file) {
        try {
            config.load(file);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
	public static void saveStatistics() {
        for(int i = 0;i<totalkills.size();i++){
        	statistics.set(("Players."+totalkills.keySet().toArray()[i]+".totalkills"), totalkills.get(totalkills.keySet().toArray()[i]));
        }
        for(int i = 0;i<totaldeaths.size();i++){
        	statistics.set(("Players."+totaldeaths.keySet().toArray()[i]+".totaldeaths"), totaldeaths.get(totaldeaths.keySet().toArray()[i]));
        }
        for(int i = 0;i<wins.size();i++){
        	statistics.set(("Players."+wins.keySet().toArray()[i]+".wins"), wins.get(wins.keySet().toArray()[i]));
        }
        for(int i = 0;i<losses.size();i++){
        	statistics.set(("Players."+losses.keySet().toArray()[i]+".losses"), losses.get(losses.keySet().toArray()[i]));
        }
        for(int i = 0;i<totaldamagedealt.size();i++){
        	statistics.set(("Players."+totaldamagedealt.keySet().toArray()[i]+".totaldamagedealt"), totaldamagedealt.get(totaldamagedealt.keySet().toArray()[i]));
        }
        for(int i = 0;i<totaldamagereceived.size();i++){
        	statistics.set(("Players."+totaldamagereceived.keySet().toArray()[i]+".totaldamagereceived"), totaldamagereceived.get(totaldamagereceived.keySet().toArray()[i]));
        }
        for(int i = 0;i<timeplayed.size();i++){
        	statistics.set(("Players."+timeplayed.keySet().toArray()[i]+".timeplayed"), timeplayed.get(timeplayed.keySet().toArray()[i]));
        }
        for(int i = 0;i<nodescaptured.size();i++){
        	statistics.set(("Players."+nodescaptured.keySet().toArray()[i]+".nodescaptured"), nodescaptured.get(nodescaptured.keySet().toArray()[i]));
        }
		try {
			statistics.save(statFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void endRound(){
		for(int i = 0;i<kills.size();i++){
			incrementTotalKillsByX(Bukkit.getServer().getPlayer((String)kills.keySet().toArray()[i]) ,kills.get(kills.keySet().toArray()[i]));
			resetKills();
		}
		for(int i = 0;i<deaths.size();i++){
			incrementTotalDeathsByX(Bukkit.getServer().getPlayer((String)deaths.keySet().toArray()[i]) ,deaths.get(deaths.keySet().toArray()[i]));
			resetDeaths();
		}
		for(int i = 0;i<damagedealt.size();i++){
			incrementTotalDamageDealtByX(Bukkit.getServer().getPlayer((String)damagedealt.keySet().toArray()[i]) ,damagedealt.get(damagedealt.keySet().toArray()[i]));
			resetDamageDealt();
		}
		for(int i = 0;i<damagereceived.size();i++){
			incrementTotalDamageReceivedByX(Bukkit.getServer().getPlayer((String)damagereceived.keySet().toArray()[i]) ,damagereceived.get(damagereceived.keySet().toArray()[i]));
			resetDamageReceived();
		}
	}

	public static int getNodesCaptured(Player player){
		if(nodescaptured.get(player.getName()) != null)return nodescaptured.get(player.getName());
		else return 0;}
	public static void incrementNodesCaptured(Player player){
		if(nodescaptured.get(player.getName()) == null)setNodesCaptured(player,1);
		else nodescaptured.put(player.getName(), nodescaptured.get(player.getName())+1);}
	public static void incrementNodesCapturedByX(Player player, int  x){
		if(nodescaptured.get(player.getName()) == null)setNodesCaptured(player,x);
		else nodescaptured.put(player.getName(), nodescaptured.get(player.getName())+x);}
	public static void setNodesCaptured(Player player, int x){nodescaptured.put(player.getName(), x);}

	public static int getKills(Player player){
		if(kills.get(player.getName()) != null)return kills.get(player.getName());
		else return 0;}
	public static void incrementKills(Player player){
		if(kills.get(player.getName()) == null)setKills(player,1);
		else kills.put(player.getName(), kills.get(player.getName())+1);}
	public static void incrementKillsByX(Player player, int x){
		if(kills.get(player.getName()) == null)setKills(player,x);
		else kills.put(player.getName(), kills.get(player.getName())+x);}
	public static void setKills(Player player, int x){kills.put(player.getName(), x);}
	public static void resetKills(){kills.clear();}

	public static int getDeaths(Player player){
		if(deaths.get(player.getName()) != null)return deaths.get(player.getName());
		else return 0;}
	public static void incrementDeaths(Player player){
		if(deaths.get(player.getName()) == null)setDeaths(player,1);
		else deaths.put(player.getName(), (deaths.get(player.getName()))+1);}
	public static void incrementDeathsByX(Player player, int x){
		if(kills.get(player.getName()) == null)setDeaths(player,x);
		else deaths.put(player.getName(), deaths.get(player.getName())+x);}
	public static void setDeaths(Player player, int x){deaths.put(player.getName(), x);}
	public static void resetDeaths(){deaths.clear();}

	public static int getTotalKills(Player player){
		if(totalkills.get(player.getName()) != null)return totalkills.get(player.getName());
		else return 0;}
	public static void incrementTotalKills(Player player){
		if(totalkills.get(player.getName()) == null)setTotalKills(player,1);
		else totalkills.put(player.getName(), totalkills.get(player.getName())+1);}
	public static void incrementTotalKillsByX(Player player, int x){
		if(kills.get(player.getName()) == null)setTotalKills(player,x);
		else totalkills.put(player.getName(), totalkills.get(player.getName())+x);}
	public static void setTotalKills(Player player, int x){totalkills.put(player.getName(), x);}

	public static int getTotalDeaths(Player player){
		if(totaldeaths.get(player.getName()) != null)return totaldeaths.get(player.getName());
		else return 0;}
	public static void incrementTotalDeaths(Player player){
		if(totaldeaths.get(player.getName()) == null)setTotalDeaths(player,1);
		else totaldeaths.put(player.getName(), totaldeaths.get(player.getName())+1);}
	public static void incrementTotalDeathsByX(Player player, int x){
		if(totaldeaths.get(player.getName()) == null)setTotalDeaths(player,x);
		else totaldeaths.put(player.getName(), totaldeaths.get(player.getName())+x);}
	public static void setTotalDeaths(Player player, int x){totaldeaths.put(player.getName(), x);}

	public static int getWins(Player player){
		if(wins.get(player.getName()) != null)return wins.get(player.getName());
		else return 0;}
	public static void incrementWins(Player player){
		if(wins.get(player.getName()) == null)setWins(player,1);
		else wins.put(player.getName(), wins.get(player.getName())+1);}
	public static void incrementWinsByX(Player player, int x){
		if(wins.get(player.getName()) == null)setWins(player,x);
		else wins.put(player.getName(), wins.get(player.getName())+x);}
	public static void setWins(Player player, int x){wins.put(player.getName(), x);}

	public static int getLosses(Player player){
		if(losses.get(player.getName()) != null)return losses.get(player.getName());
		else return 0;}
	public static void incrementLosses(Player player){
		if(losses.get(player.getName()) == null)setLosses(player,1);
		else losses.put(player.getName(), losses.get(player.getName())+1);}
	public static void incrementLossesByX(Player player, int x){
		if(losses.get(player.getName()) == null)setLosses(player,x);
		else losses.put(player.getName(), losses.get(player.getName())+x);}
	public static void setLosses(Player player, int x){losses.put(player.getName(), x);}

	public static int getDamageDealt(Player player){
		if(damagedealt.get(player.getName()) != null)return damagedealt.get(player.getName());
		else return 0;}
	public static void incrementDamageDealt(Player player){
		if(damagedealt.get(player.getName()) == null)setDamageDealt(player,1);
		else damagedealt.put(player.getName(), damagedealt.get(player.getName())+1);}
	public static void incrementDamageDealtByX(Player player, int x){
		if(damagedealt.get(player.getName()) == null)setDamageDealt(player,x);
		else damagedealt.put(player.getName(), damagedealt.get(player.getName())+x);}
	public static void setDamageDealt(Player player, int x){damagedealt.put(player.getName(), x);}
	public static void resetDamageDealt(){damagedealt.clear();}

	public static int getDamageReceived(Player player){
		if(damagereceived.get(player.getName()) != null)return damagereceived.get(player.getName());
		else return 0;}
	public static void incrementDamageReceived(Player player){
		if(damagereceived.get(player.getName()) == null)setDamageReceived(player,1);
		else damagereceived.put(player.getName(), damagereceived.get(player.getName())+1);}
	public static void incrementDamageReceivedByX(Player player, int x){
		if(damagereceived.get(player.getName()) == null)setDamageReceived(player,x);
		else damagereceived.put(player.getName(), damagereceived.get(player.getName())+x);}
	public static void setDamageReceived(Player player, int x){damagereceived.put(player.getName(), x);}
	public static void resetDamageReceived(){damagereceived.clear();}

	public static int getTotalDamageDealt(Player player){
		if(totaldamagedealt.get(player.getName()) != null)return totaldamagedealt.get(player.getName());
		else return 0;}
	public static void incrementTotalDamageDealt(Player player){
		if(totaldamagedealt.get(player.getName()) == null)setTotalDamageDealt(player,1);
		else totaldamagedealt.put(player.getName(), totaldamagedealt.get(player.getName())+1);}
	public static void incrementTotalDamageDealtByX(Player player, int x){
		if(totaldamagedealt.get(player.getName()) == null)setTotalDamageDealt(player,x);
		else totaldamagedealt.put(player.getName(), totaldamagedealt.get(player.getName())+x);}
	public static void setTotalDamageDealt(Player player, int x){totaldamagedealt.put(player.getName(), x);}

	public static int getTotalDamageReceived(Player player){
		if(totaldamagereceived.get(player.getName()) != null)return totaldamagereceived.get(player.getName());
		else return 0;}
	public static void incrementTotalDamageReceived(Player player){
		if(totaldamagereceived.get(player.getName()) == null)setTotalDamageReceived(player,1);
		else totaldamagereceived.put(player.getName(), totaldamagereceived.get(player.getName())+1);}
	public static void incrementTotalDamageReceivedByX(Player player, int x){
		if(totaldamagereceived.get(player.getName()) == null)setTotalDamageReceived(player,x);
		else totaldamagereceived.put(player.getName(), totaldamagereceived.get(player.getName())+x);}
	public static void setTotalDamageReceived(Player player, int x){totaldamagereceived.put(player.getName(), x);}

	public static int getTimePlayed(Player player){
		if(timeplayed.get(player.getName()) != null)return timeplayed.get(player.getName());
		else return 0;}
	public static void incrementTimePlayed(Player player){
		if(timeplayed.get(player.getName()) == null)setTimePlayed(player,1);
		else timeplayed.put(player.getName(), timeplayed.get(player.getName())+1);}
	public static void incrementTimePlayedByX(Player player, int x){
		if(timeplayed.get(player.getName()) == null)setTimePlayed(player,x);
		else timeplayed.put(player.getName(), timeplayed.get(player.getName())+x);}
	public static void setTimePlayed(Player player, int x){timeplayed.put(player.getName(), x);}


}
