package us.bemrose.mc.BeyondWartime;

import java.util.List;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.DyeColor;

class WarClass {

    String name;
    List<ItemStack> items;
    List<ItemStack> armor;
    
    static List<WarClass> classes = new LinkedList<WarClass>();
    static ConfigurationSection config;

    public static void initClasses(ConfigurationSection c) {
        config = c;
        classes = new LinkedList<WarClass>(); 
    }
    
    public static WarClass getClass(String n) {

        for (WarClass w : classes) {
            if (w.name.equalsIgnoreCase(n)) {
                return w;
            }
        }
    
        WarClass w = new WarClass();
        w.name = n;
        
        if (w.loadClass()) {
            classes.add(w);
            return w;
        }

        if (n.equalsIgnoreCase("default")) {
            // Protection against infinite recursion
            return null;
        }
        
        return getClass("default");
    }

    private WarClass() {
    }
    
    public String getName() { return name; }

    boolean loadClass() {
        String sectionName = name.toLowerCase();
        ConfigurationSection section = config.getConfigurationSection(sectionName);
        if (section == null) {
            return false;
        }
        
        items = makeItemStackList(section.getString("items"));
        armor = makeItemStackList(section.getString("armor"));
        
        return true;
    }

    public void applyInv(Player player) {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        inv.clear();

        if (armor != null && armor.size() >= 1) {
            inv.setHelmet(armor.get(0));
            if (armor.size() >= 2) {
                inv.setChestplate(armor.get(1));
                if (armor.size() >= 3) {
                    inv.setLeggings(armor.get(2));
                    if (armor.size() >= 4) {
                        inv.setBoots(armor.get(3));
                    }
                }
            }
        }
        if (items != null) {
            for (ItemStack i : items) {
                inv.addItem(i);
            }
        }
    }

    
     /**
     * makeItemStackList
     * 
     * This code "borrowed" from MobArena: http://dev.bukkit.org/server-mods/mobarena/
     * Much thanks to those guys for standardizing item lists
     *
     * Takes a comma-separated list of items in the <type>:<amount> format and
     * returns a list of ItemStacks created from that data.
     */
    static List<ItemStack> makeItemStackList(String string)
    {
        List<ItemStack> result = new LinkedList<ItemStack>();
        if (string == null || string.isEmpty()) return result;
        
        // Trim commas and whitespace, and split items by commas
        string = string.trim();
        if (string.endsWith(","))
            string = string.substring(0, string.length()-1);
        String[] items = string.split(",");
        
        for (String item : items)
        {
            // Trim whitespace and split by colons.
            item = item.trim();
            String[] parts = item.split(":");
            
            // Grab the amount.
            int amount = 1;
            if (parts.length == 1 && parts[0].matches("\\$[0-9]+"))
                amount = Integer.parseInt(parts[0].substring(1, parts[0].length()));
            else if (parts.length == 2 && parts[1].matches("(-)?[0-9]+"))
                amount = Integer.parseInt(parts[1]);
            else if (parts.length == 3 && parts[2].matches("(-)?[0-9]+"))
                amount = Integer.parseInt(parts[2]);
            
            
            // Make the ItemStack.
            ItemStack stack = (parts.length == 3) ?
                    makeItemStack(parts[0], amount, parts[1]) :
                    makeItemStack(parts[0], amount);
            
            if (stack != null)
                result.add(stack);
        }
        return result;
    }

    /* Helper methods for making ItemStacks out of strings and ints */
    static ItemStack makeItemStack(String name, int amount, String data)
    {
        // If this is economy money, create a dummy ItemStack.
        // if (name.matches("\\$[0-9]+"))
        //      return new ItemStack(MobArena.ECONOMY_MONEY_ID, amount);
        
        try
        {
            byte offset = 0;
            
            Material material = (name.matches("[0-9]+")) ?
                Material.getMaterial(Integer.parseInt(name)) :
                Material.valueOf(name.toUpperCase());
            
            if (material == Material.INK_SACK)
                offset = 15;
                
            DyeColor dye = (data.matches("[0-9]+")) ?
                DyeColor.getByData((byte) Math.abs(offset - Integer.parseInt(data))) :
                DyeColor.valueOf(data.toUpperCase());
                
            //return new ItemStack(material, amount, (byte) Math.abs((offset - dye.getData())));
            return new ItemStack(material, amount, (byte) Math.abs(offset - dye.getData()));
        }
        catch (Exception e)
        {
            System.out.println("Could not create item \"" + name + "\". Check config.yml");
            return null;
        }
    }

    static ItemStack makeItemStack(String name, int amount)
    {
        return makeItemStack(name, amount, "0");
    }

}