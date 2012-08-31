package us.bemrose.mc.BeyondWartime;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

	public static void init(){
		System.out.println("Begin reading wartime statistics...");
        loadStatFile(statistics, statFile);
        totalkills.clear();
        for(int i = 0;i<statistics.getConfigurationSection("Players").getKeys(false).size();i++){
        	totalkills.put((String)statistics.getConfigurationSection("Players").getKeys(false).toArray()[i], statistics.getInt("Players."+statistics.getConfigurationSection("Players").getKeys(false).toArray()[i]+".totalkills"));
        }
        totaldeaths.clear();
        for(int i = 0;i<statistics.getConfigurationSection("Players").getKeys(false).size();i++){
        	totaldeaths.put((String)statistics.getConfigurationSection("Players").getKeys(false).toArray()[i], statistics.getInt("Players."+statistics.getConfigurationSection("Players").getKeys(false).toArray()[i]+".totaldeaths"));
        }
        wins.clear();
        for(int i = 0;i<statistics.getConfigurationSection("Players").getKeys(false).size();i++){
        	wins.put((String)statistics.getConfigurationSection("Players").getKeys(false).toArray()[i], statistics.getInt("Players."+statistics.getConfigurationSection("Players").getKeys(false).toArray()[i]+".wins"));
        }
        losses.clear();
        for(int i = 0;i<statistics.getConfigurationSection("Players").getKeys(false).size();i++){
        	losses.put((String)statistics.getConfigurationSection("Players").getKeys(false).toArray()[i], statistics.getInt("Players."+statistics.getConfigurationSection("Players").getKeys(false).toArray()[i]+".losses"));
        }
        totaldamagedealt.clear();
        for(int i = 0;i<statistics.getConfigurationSection("Players").getKeys(false).size();i++){
        	totaldamagedealt.put((String)statistics.getConfigurationSection("Players").getKeys(false).toArray()[i], statistics.getInt("Players."+statistics.getConfigurationSection("Players").getKeys(false).toArray()[i]+".totaldamagedealt"));
        }
        totaldamagereceived.clear();
        for(int i = 0;i<statistics.getConfigurationSection("Players").getKeys(false).size();i++){
        	totaldamagereceived.put((String)statistics.getConfigurationSection("Players").getKeys(false).toArray()[i], statistics.getInt("Players."+statistics.getConfigurationSection("Players").getKeys(false).toArray()[i]+".totaldamagereceived"));
        }
        timeplayed.clear();
        for(int i = 0;i<statistics.getConfigurationSection("Players").getKeys(false).size();i++){
        	timeplayed.put((String)statistics.getConfigurationSection("Players").getKeys(false).toArray()[i], statistics.getInt("Players."+statistics.getConfigurationSection("Players").getKeys(false).toArray()[i]+".timeplayed"));
        }
		System.out.println("End reading wartime statistics...");

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
		try {
			statistics.save(statFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static int getKills(Player player){return kills.get(player.getName());}
	public static void incrementKills(Player player){kills.put(player.getName(), kills.get(player.getName())+1);}
	public static void incrementKillsByX(Player player, int x){kills.put(player.getName(), kills.get(player.getName())+x);}
	public static void setKills(Player player, int x){kills.put(player.getName(), x);}
	public static void resetKills(){kills.clear();}

	public static int getDeaths(Player player){return deaths.get(player.getName());}
	public static void incrementDeaths(Player player){deaths.put(player.getName(), deaths.get(player.getName())+1);}
	public static void incrementDeathsByX(Player player, int x){deaths.put(player.getName(), deaths.get(player.getName())+x);}
	public static void setDeaths(Player player, int x){deaths.put(player.getName(), x);}
	public static void resetDeaths(){deaths.clear();}
	
	public static int getTotalKills(Player player){return totalkills.get(player.getName());}
	public static void incrementTotalKills(Player player){totalkills.put(player.getName(), totalkills.get(player.getName())+1);}
	public static void incrementTotalKillsByX(Player player, int x){totalkills.put(player.getName(), totalkills.get(player.getName())+x);}
	public static void setTotalKills(Player player, int x){totalkills.put(player.getName(), x);}

	public static int getTotalDeaths(Player player){return totaldeaths.get(player.getName());}
	public static void incrementTotalDeaths(Player player){totaldeaths.put(player.getName(), totaldeaths.get(player.getName())+1);}
	public static void incrementTotalDeathsByX(Player player, int x){totaldeaths.put(player.getName(), totaldeaths.get(player.getName())+x);}
	public static void setTotalDeaths(Player player, int x){totaldeaths.put(player.getName(), x);}
	
	public static int getWins(Player player){return wins.get(player.getName());}
	public static void incrementWins(Player player){wins.put(player.getName(), wins.get(player.getName())+1);}
	public static void incrementWinsByX(Player player, int x){wins.put(player.getName(), wins.get(player.getName())+x);}
	public static void setWins(Player player, int x){wins.put(player.getName(), x);}

	public static int getLosses(Player player){return losses.get(player.getName());}
	public static void incrementLosses(Player player){losses.put(player.getName(), losses.get(player.getName())+1);}
	public static void incrementLossesByX(Player player, int x){losses.put(player.getName(), losses.get(player.getName())+x);}
	public static void setLosses(Player player, int x){losses.put(player.getName(), x);}
	
	public static int getDamageDealt(Player player){return damagedealt.get(player.getName());}
	public static void incrementDamageDealt(Player player){damagedealt.put(player.getName(), damagedealt.get(player.getName())+1);}
	public static void incrementDamageDealtByX(Player player, int x){damagedealt.put(player.getName(), damagedealt.get(player.getName())+x);}
	public static void setDamageDealt(Player player, int x){damagedealt.put(player.getName(), x);}
	public static void resetDamageDealt(){damagedealt.clear();}

	public static int getDamageReceived(Player player){return damagereceived.get(player.getName());}
	public static void incrementDamageReceived(Player player){damagereceived.put(player.getName(), damagereceived.get(player.getName())+1);}
	public static void incrementDamageReceivedByX(Player player, int x){damagereceived.put(player.getName(), damagereceived.get(player.getName())+x);}
	public static void setDamageReceived(Player player, int x){damagereceived.put(player.getName(), x);}
	public static void resetDamageReceived(){damagereceived.clear();}
	
	public static int getTotalDamageDealt(Player player){return totaldamagedealt.get(player.getName());}
	public static void incrementTotalDamageDealt(Player player){totaldamagedealt.put(player.getName(), totaldamagedealt.get(player.getName())+1);}
	public static void incrementTotalDamageDealtByX(Player player, int x){totaldamagedealt.put(player.getName(), totaldamagedealt.get(player.getName())+x);}
	public static void setTotalDamageDealt(Player player, int x){totaldamagedealt.put(player.getName(), x);}

	public static int getTotalDamageReceived(Player player){return totaldamagereceived.get(player.getName());}
	public static void incrementTotalDamageReceived(Player player){totaldamagereceived.put(player.getName(), totaldamagereceived.get(player.getName())+1);}
	public static void incrementTotalDamageReceivedByX(Player player, int x){totaldamagereceived.put(player.getName(), totaldamagereceived.get(player.getName())+x);}
	public static void setTotalDamageReceived(Player player, int x){totaldamagereceived.put(player.getName(), x);}

	public static int getTimePlayed(Player player){return timeplayed.get(player.getName());}
	public static void incrementTimePlayed(Player player){timeplayed.put(player.getName(), timeplayed.get(player.getName())+1);}
	public static void incrementTimePlayedByX(Player player, int x){timeplayed.put(player.getName(), timeplayed.get(player.getName())+x);}
	public static void setTimePlayed(Player player, int x){timeplayed.put(player.getName(), x);}

	
}
