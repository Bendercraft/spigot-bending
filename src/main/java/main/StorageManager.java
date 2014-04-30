package main;

import java.io.File;

import org.bukkit.plugin.Plugin;

public class StorageManager {

	private File dataFolder;
	public BendingPlayers config;
	public static Boolean useMySQL;
	public static Boolean useFlatFile;
	public MySQL MySql;

	private Plugin tapi;

	// private InputStream defConfigStream;

	// public BendingPlayers(File file, InputStream inputStream) {
	// load();
	// dataFolder = file;
	// defConfigStream = inputStream;
	// }

	static final String on_air_choose = "As an airbender, you now take no falling damage, have faster sprinting and higher "
			+ "jumps. Additionally, daily activities are easier for you - your food meter decays at a "
			+ "much slower rate";
	static final String on_earth_choose = "As an earthbender, upon landing on bendable earth, you will briefly turn the "
			+ "area to soft sand, negating any fall damage you would have otherwise taken.";
	static final String on_water_choose = "As a waterbender, you no longer take any fall damage when landing on ice, snow "
			+ "or even 1-block-deep water. Additionally, sneaking in the water with a bending ability "
			+ "selected that does not utilize sneak (or no ability at all)"
			+ " will give you accelerated swimming. "
			+ "Lastly, you can pull water from plants with your abilities.";
	static final String on_fire_choose = "As a firebender, you now more quickly smother yourself when you catch on fire.";
	static final String on_chi_choose = "As a chiblocker, you have no active abilities to bind. Instead, you have improved "
			+ "sprinting and jumping, have a dodge chance and deal more damage with your fists. "
			+ "Additionally, punching a bender will block his/her chi for a few seconds, preventing "
			+ "him/her from bending (and even stopping their passive!).";

	// public StorageManager(File file) {
	// dataFolder = file;
	// initialize(dataFolder);
	// tapi = Bukkit.getPluginManager().getPlugin("TagAPI");
	// }
	//
	// public void removeBending(OfflinePlayer player) {
	// if (StorageManager.useFlatFile) {
	// if (config.checkKeys(player.getName())) {
	// for (int i = 0; i <= 8; i++) {
	// removeAbility(player, i);
	// }
	// for (Material mat : Material.values()) {
	// removeAbility(player, mat);
	// }
	// config.setKey(player.getName(), "");
	// if (player instanceof Player)
	// ((Player) player).setDisplayName(player.getName());
	// }
	// } else if (StorageManager.useMySQL) {
	// String removeEle = "DELETE FROM bending_element WHERE player ='"
	// + player.getName() + "'";
	// this.MySql.delete(removeEle);
	// String removeBind = "DELETE FROM bending_ability WHERE player ='"
	// + player.getName() + "'";
	// this.MySql.delete(removeBind);
	// // MySql Query
	// }
	// // List<BendingType> templist = new ArrayList<BendingType>();
	// // Bending.benders.put(player.getName(), templist);
	// if (tapi != null && ConfigManager.useTagAPI) {
	// try {
	// if (player instanceof Player)
	// TagAPI.refreshPlayer((Player) player);
	// } catch (Exception e) {
	//
	// }
	// }
	// // Bending.benders.remove(player.getName());
	// return;
	// }
	//
	// // public boolean isBender(Player player, BendingType type) {
	// // if (StorageManager.useFlatFile) {
	// // if (config.checkKeys(player.getName())) {
	// // if (config.getKey(player.getName()).contains("a")
	// // && type == BendingType.Air) {
	// // return true;
	// // }
	// // if (config.getKey(player.getName()).contains("e")
	// // && type == BendingType.Earth) {
	// // return true;
	// // }
	// // if (config.getKey(player.getName()).contains("w")
	// // && type == BendingType.Water) {
	// // return true;
	// // }
	// // if (config.getKey(player.getName()).contains("f")
	// // && type == BendingType.Fire) {
	// // return true;
	// // }
	// // if (config.getKey(player.getName()).contains("c")
	// // && type == BendingType.ChiBlocker) {
	// // return true;
	// // }
	// // }
	// // } else if (StorageManager.useMySQL) {
	// // try {
	// // String getEle = "SELECT bending FROM bending_element WHERE player ='"
	// // + player.getName() + "'";
	// // ResultSet bending = this.MySql.getConnection()
	// // .createStatement().executeQuery(getEle);
	// // if (bending.next()) {
	// // if (bending.getString("bending").contains("a")
	// // && type == BendingType.Air) {
	// // return true;
	// // }
	// // if (bending.getString("bending").contains("e")
	// // && type == BendingType.Earth) {
	// // return true;
	// // }
	// // if (bending.getString("bending").contains("w")
	// // && type == BendingType.Water) {
	// // return true;
	// // }
	// // if (bending.getString("bending").contains("f")
	// // && type == BendingType.Fire) {
	// // return true;
	// // }
	// // if (bending.getString("bending").contains("c")
	// // && type == BendingType.ChiBlocker) {
	// // return true;
	// // }
	// // }
	// // } catch (SQLException e) {
	// // return false;
	// // }
	// // }
	// // return false;
	// // }
	//
	// public boolean isBender(String player, BendingType type) {
	// if (StorageManager.useFlatFile) {
	// if (config.checkKeys(player)) {
	// if (config.getKey(player).contains("a")
	// && type == BendingType.Air) {
	// return true;
	// }
	// if (config.getKey(player).contains("e")
	// && type == BendingType.Earth) {
	// return true;
	// }
	// if (config.getKey(player).contains("w")
	// && type == BendingType.Water) {
	// return true;
	// }
	// if (config.getKey(player).contains("f")
	// && type == BendingType.Fire) {
	// return true;
	// }
	// if (config.getKey(player).contains("c")
	// && type == BendingType.ChiBlocker) {
	// return true;
	// }
	// }
	// } else if (StorageManager.useMySQL) {
	// try {
	// String getEle = "SELECT bending FROM bending_element WHERE player ='"
	// + player + "'";
	// ResultSet bending = this.MySql.getConnection()
	// .createStatement().executeQuery(getEle);
	// if (bending.next()) {
	// if (bending.getString("bending").contains("a")
	// && type == BendingType.Air) {
	// return true;
	// }
	// if (bending.getString("bending").contains("e")
	// && type == BendingType.Earth) {
	// return true;
	// }
	// if (bending.getString("bending").contains("w")
	// && type == BendingType.Water) {
	// return true;
	// }
	// if (bending.getString("bending").contains("f")
	// && type == BendingType.Fire) {
	// return true;
	// }
	// if (bending.getString("bending").contains("c")
	// && type == BendingType.ChiBlocker) {
	// return true;
	// }
	// }
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }
	// return false;
	// }
	//
	// public String getLanguage(String playername) {
	// String language = Tools.getDefaultLanguage();
	// if (StorageManager.useFlatFile) {
	// String key = playername + "<Language>";
	// if (config.checkKeys(key)) {
	// language = config.getKey(key);
	// }
	// } else {
	// String getLanguage =
	// "SELECT language  FROM bending_language WHERE player ='"
	// + playername + "'";
	// ResultSet rs = this.MySql.select(getLanguage);
	//
	// try {
	// if (rs.next()) {
	// language = rs.getString("language");
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// return language;
	// }
	//
	// public void setLanguage(OfflinePlayer player, String language) {
	// language = language.toLowerCase();
	// if (StorageManager.useFlatFile) {
	// config.setKey(player.getName() + "<Language>", language);
	// } else if (StorageManager.useMySQL) {
	// String checkEntry =
	// "SELECT language FROM bending_language WHERE player ='"
	// + player.getName() + "'";
	// ResultSet rs = this.MySql.select(checkEntry);
	// try {
	// if (rs.next()) {
	// String updateEntry = "UPDATE bending_language SET language = "
	// + language
	// + " WHERE player ='"
	// + player.getName()
	// + "'";
	// this.MySql.update(updateEntry);
	// } else {
	// String insertEntry = "INSERT INTO bending_language VALUES('"
	// + player.getName() + "','" + language + "')";
	// this.MySql.insert(insertEntry);
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// public void setBending(OfflinePlayer player, BendingType type) {
	// String bending = "";
	// String bendingstring = "";
	// if (type == BendingType.Air) {
	// bending = "a";
	// bendingstring = "Air";
	// } else if (type == BendingType.Earth) {
	// bending = "e";
	// bendingstring = "Earth";
	// } else if (type == BendingType.Water) {
	// bending = "w";
	// bendingstring = "Water";
	// } else if (type == BendingType.Fire) {
	// bending = "f";
	// bendingstring = "Fire";
	// } else if (type == BendingType.ChiBlocker) {
	// bending = "c";
	// bendingstring = "Chi";
	// } else {
	// bending = "s";
	// if (player instanceof Player)
	// ((Player) player).setDisplayName(player.getName());
	// return;
	// }
	// if (StorageManager.useFlatFile) {
	// config.setKey(player.getName(), bending);
	// } else if (StorageManager.useMySQL) {
	// String checkEntry = "SELECT bending FROM bending_element WHERE player ='"
	// + player.getName() + "'";
	// ResultSet rs = this.MySql.select(checkEntry);
	// try {
	// if (rs.next()) {
	// String updateEntry = "UPDATE bending_element SET bending = "
	// + bending
	// + " WHERE player ='"
	// + player.getName()
	// + "'";
	// this.MySql.update(updateEntry);
	// } else {
	// String insertEntry = "INSERT INTO bending_element VALUES('"
	// + player.getName() + "','" + bending + "')";
	// this.MySql.insert(insertEntry);
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// if (player instanceof Player)
	// if (bendingstring == "Chi") {
	//
	// Tools.sendMessage((Player) player, ChatColor.GOLD,
	// "Chiblocker.ChiChoose");
	// } else {
	// Tools.sendMessage((Player) player, ChatColor.GOLD,
	// bendingstring + "." + bendingstring + "Choose");
	// }
	// // player.sendMessage(ChatColor.GOLD + bendingstring);
	// // player.sendMessage(ChatColor.GOLD
	// // + "Use '/bending help' if you need assistance.");
	//
	// if (ConfigManager.enabled) {
	// String append = "";
	// if (!player.isOp()) {
	// if (Tools.isBender(player.getName(), BendingType.Air)) {
	// append = ConfigManager.getPrefix("Air");
	// } else if (Tools.isBender(player.getName(), BendingType.Earth)) {
	// append = ConfigManager.getPrefix("Earth");
	// } else if (Tools.isBender(player.getName(), BendingType.Fire)) {
	// append = ConfigManager.getPrefix("Fire");
	// } else if (Tools.isBender(player.getName(), BendingType.Water)) {
	// append = ConfigManager.getPrefix("Water");
	// } else if (Tools.isBender(player.getName(),
	// BendingType.ChiBlocker)) {
	// append = ConfigManager.getPrefix("ChiBlocker");
	// }
	// if (!(ConfigManager.compatibility))
	// if (player instanceof Player)
	// ((Player) player).setDisplayName(append
	// + player.getName());
	// }
	// if ((ConfigManager.compatibility) && (ConfigManager.enabled)) {
	// ChatColor color = ChatColor.WHITE;
	// if (ConfigManager.colors && (!player.isOp())) {
	// if (Tools.isBender(player.getName(), BendingType.Air)) {
	// color = Tools.getColor(ConfigManager.getColor("Air"));
	// } else if (Tools.isBender(player.getName(),
	// BendingType.Earth)) {
	// color = Tools.getColor(ConfigManager.getColor("Earth"));
	// } else if (Tools.isBender(player.getName(),
	// BendingType.Fire)) {
	// color = Tools.getColor(ConfigManager.getColor("Fire"));
	// } else if (Tools.isBender(player.getName(),
	// BendingType.Water)) {
	// color = Tools.getColor(ConfigManager.getColor("Water"));
	// } else if (Tools.isBender(player.getName(),
	// BendingType.ChiBlocker)) {
	// color = Tools.getColor(ConfigManager
	// .getColor("ChiBlocker"));
	// }
	// if (player instanceof Player)
	// ((Player) player).setDisplayName("<" + color + append
	// + player.getName() + ChatColor.WHITE + ">");
	// }
	// }
	// }
	// List<BendingType> templist = new ArrayList<BendingType>();
	// templist.add(type);
	// // Bending.benders.put(player.getName(), templist);
	// if (tapi != null && ConfigManager.useTagAPI) {
	// try {
	// if (player instanceof Player)
	// TagAPI.refreshPlayer((Player) player);
	// } catch (Exception e) {
	//
	// }
	// }
	// }
	//
	// public void setBending(Player player, String type) {
	// BendingType bendingtype = BendingType.getType(type);
	// if (bendingtype != null) {
	// setBending(player, bendingtype);
	// }
	//
	// }
	//
	// public void addBending(Player player, BendingType type) {
	// String bending = "";
	// if (StorageManager.useFlatFile)
	// bending = config.getKey(player.getName());
	// else if (StorageManager.useMySQL) {
	// String getBending = "SELECT bending FROM bending_element WHERE player ='"
	// + player.getName() + "'";
	// ResultSet rs = this.MySql.select(getBending);
	//
	// try {
	// if (rs.next()) {
	// bending = rs.getString("bending");
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// if (!Tools.isBender(player.getName(), type)) {
	// if (type == BendingType.Air) {
	// bending += "a";
	// } else if (type == BendingType.Earth) {
	// bending += "e";
	// } else if (type == BendingType.Water) {
	// bending += "w";
	// } else if (type == BendingType.Fire) {
	// bending += "f";
	// } else if (type == BendingType.ChiBlocker) {
	// bending += "c";
	// }
	// }
	// if (StorageManager.useFlatFile)
	// config.setKey(player.getName(), bending);
	// else if (StorageManager.useMySQL) {
	// String checkEntry = "SELECT * FROM bending_element WHERE player ='"
	// + player.getName() + "'";
	// ResultSet rs = this.MySql.select(checkEntry);
	// try {
	// if (rs.next()) {
	// String updateEntry = "UPDATE bending_element SET bending = '"
	// + bending
	// + "' WHERE player ='"
	// + player.getName()
	// + "'";
	// this.MySql.update(updateEntry);
	// } else {
	// String insertEntry = "INSERT INTO bending_element VALUES('"
	// + player.getName() + "','" + bending + "')";
	// this.MySql.insert(insertEntry);
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// // List<BendingType> templist;
	// // if (Bending.benders.containsKey(player.getName())) {
	// // templist = Bending.benders.get(player.getName());
	// // templist.add(type);
	// // } else {
	// // templist = new ArrayList<BendingType>();
	// // templist.add(type);
	// // }
	// // Bending.benders.put(player.getName(), templist);
	// if (tapi != null && ConfigManager.useTagAPI) {
	// try {
	// TagAPI.refreshPlayer(player);
	// } catch (Exception e) {
	//
	// }
	// }
	// }
	//
	// public void addBending(Player player, String type) {
	// BendingType bendingtype = BendingType.getType(type);
	// if (bendingtype != null) {
	// addBending(player, bendingtype);
	// }
	// }
	//
	// public void addBending(String player, BendingType type) {
	// String bending = "";
	// if (StorageManager.useFlatFile)
	// bending = config.getKey(player);
	// else if (StorageManager.useMySQL) {
	// String getBending = "SELECT bending FROM bending_element WHERE player ='"
	// + player + "'";
	// ResultSet rs = this.MySql.select(getBending);
	//
	// try {
	// if (rs.next()) {
	// bending = rs.getString("bending");
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// // if (!Tools.isBender(player, type)) {
	// if (type == BendingType.Air) {
	// if (!bending.contains("a"))
	// bending += "a";
	// }
	// if (type == BendingType.Earth) {
	// if (!bending.contains("e"))
	// bending += "e";
	// }
	// if (type == BendingType.Water) {
	// if (!bending.contains("w"))
	// bending += "w";
	// }
	// if (type == BendingType.Fire) {
	// if (!bending.contains("f"))
	// bending += "f";
	// }
	// if (type == BendingType.ChiBlocker) {
	// if (!bending.contains("c"))
	// bending += "c";
	// }
	// // }
	// if (StorageManager.useFlatFile)
	// config.setKey(player, bending);
	// else if (StorageManager.useMySQL) {
	// String checkEntry = "SELECT * FROM bending_element WHERE player ='"
	// + player + "'";
	// ResultSet rs = this.MySql.select(checkEntry);
	// try {
	// if (rs.next()) {
	// String updateEntry = "UPDATE bending_element SET bending = '"
	// + bending + "' WHERE player ='" + player + "'";
	// this.MySql.update(updateEntry);
	// } else {
	// String insertEntry = "INSERT INTO bending_element VALUES('"
	// + player + "','" + bending + "')";
	// this.MySql.insert(insertEntry);
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// // List<BendingType> templist;
	// // if (Bending.benders.containsKey(player)) {
	// // templist = Bending.benders.get(player);
	// // templist.add(type);
	// // } else {
	// // templist = new ArrayList<BendingType>();
	// // templist.add(type);
	// // }
	// // Bending.benders.put(player, templist);
	// }
	//
	// public void addBending(String player, String type) {
	// BendingType bendingtype = BendingType.getType(type);
	// if (bendingtype != null) {
	// addBending(player, bendingtype);
	// }
	// }
	//
	// // public boolean isBender(Player player) {
	// // if (StorageManager.useFlatFile) {
	// // if (config.checkKeys(player.getName())) {
	// // if (config.getKey(player.getName()).contains("a")
	// // || config.getKey(player.getName()).contains("e")
	// // || config.getKey(player.getName()).contains("w")
	// // || config.getKey(player.getName()).contains("f")
	// // || config.getKey(player.getName()).contains("s")
	// // || config.getKey(player.getName()).contains("c")) {
	// // return true;
	// // }
	// // }
	// // } else if (StorageManager.useMySQL) {
	// // String getEle = "SELECT bending FROM bending_element WHERE player ='"
	// // + player.getName() + "'";
	// // ResultSet result = this.MySql.select(getEle);
	// // try {
	// // if (result.next()) {
	// // String bending = result.getString("bending");
	// // if (bending.contains("a") || bending.contains("e")
	// // || bending.contains("w") || bending.contains("f")
	// // || bending.contains("s") || bending.contains("c")) {
	// // return true;
	// // }
	// // }
	// // } catch (SQLException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // }
	// // }
	// // return false;
	// // }
	//
	// public void setAbility(Player player, String ability, int slot) {
	// for (Abilities a : Abilities.values()) {
	// if (ability.equalsIgnoreCase(a.name())) {
	// setAbility(player, a, slot);
	// }
	// }
	// }
	//
	// public void setAbility(String player, String ability, int slot) {
	// for (Abilities a : Abilities.values()) {
	// if (ability.equalsIgnoreCase(a.name())) {
	// setAbility(player, a, slot);
	// }
	// }
	// }
	//
	// public void setAbility(Player player, Abilities ability, int slot) {
	// String setter = player.getName() + "<Bind" + slot + ">";
	// if (StorageManager.useFlatFile) {
	// config.setKey(setter, ability.name());
	// } else if (StorageManager.useMySQL) {
	// String checkAbilities =
	// "SELECT ability FROM bending_ability WHERE setter ='"
	// + setter + "' AND player = '" + player.getName() + "'";
	// ResultSet set = this.MySql.select(checkAbilities);
	// try {
	// if (set.next()) {
	// String updateAbility = "UPDATE bending_ability SET ability = '"
	// + ability.name()
	// + "' WHERE player ='"
	// + player.getName()
	// + "' AND setter = '"
	// + setter
	// + "'";
	// this.MySql.update(updateAbility);
	// } else {
	// String insertAbility = "INSERT INTO bending_ability VALUES('"
	// + player.getName()
	// + "','"
	// + setter
	// + "','"
	// + ability.name() + "')";
	// this.MySql.insert(insertAbility);
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// public void setAbility(String player, Abilities ability, int slot) {
	// String setter = player + "<Bind" + slot + ">";
	// if (StorageManager.useFlatFile) {
	// config.setKey(setter, ability.name());
	// } else if (StorageManager.useMySQL) {
	// String checkAbilities =
	// "SELECT ability FROM bending_ability WHERE setter ='"
	// + setter + "' AND player = '" + player + "'";
	// ResultSet set = this.MySql.select(checkAbilities);
	// try {
	// if (set.next()) {
	// String updateAbility = "UPDATE bending_ability SET ability = '"
	// + ability.name()
	// + "' WHERE player ='"
	// + player
	// + "' AND setter = '" + setter + "'";
	// this.MySql.update(updateAbility);
	// } else {
	// String insertAbility = "INSERT INTO bending_ability VALUES('"
	// + player
	// + "','"
	// + setter
	// + "','"
	// + ability.name()
	// + "')";
	// this.MySql.insert(insertAbility);
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// // Bind to item
	// public void setAbility(Player player, String ability, Material mat) {
	// for (Abilities a : Abilities.values()) {
	// if (ability.equalsIgnoreCase(a.name())) {
	// setAbility(player, a, mat);
	// }
	// }
	// }
	//
	// public void setAbility(String player, String ability, Material mat) {
	// for (Abilities a : Abilities.values()) {
	// if (ability.equalsIgnoreCase(a.name())) {
	// setAbility(player, a, mat);
	// }
	// }
	// }
	//
	// public void setAbility(Player player, Abilities ability, Material mat) {
	// String setter = player.getName() + "<Bind" + mat.name() + ">";
	// if (StorageManager.useFlatFile) {
	// config.setKey(setter, ability.name());
	// } else if (StorageManager.useMySQL) {
	// String checkAbilities =
	// "SELECT ability FROM bending_ability WHERE setter ='"
	// + setter + "' AND player ='" + player.getName() + "'";
	// ResultSet set = this.MySql.select(checkAbilities);
	// try {
	// if (set.next()) {
	// String updateAbility = "UPDATE bending_ability SET ability = '"
	// + ability.name()
	// + "' WHERE player ='"
	// + player.getName()
	// + "' AND setter = '"
	// + setter
	// + "'";
	// this.MySql.update(updateAbility);
	// } else {
	// String insertAbility = "INSERT INTO bending_ability VALUES('"
	// + player.getName()
	// + "','"
	// + setter
	// + "','"
	// + ability.name() + "')";
	// this.MySql.insert(insertAbility);
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// public void setAbility(String player, Abilities ability, Material mat) {
	// String setter = player + "<Bind" + mat.name() + ">";
	// if (StorageManager.useFlatFile) {
	// config.setKey(setter, ability.name());
	// } else if (StorageManager.useMySQL) {
	// String checkAbilities =
	// "SELECT ability FROM bending_ability WHERE setter ='"
	// + setter + "' AND player ='" + player + "'";
	// ResultSet set = this.MySql.select(checkAbilities);
	// try {
	// if (set.next()) {
	// String updateAbility = "UPDATE bending_ability SET ability = '"
	// + ability.name()
	// + "' WHERE player ='"
	// + player
	// + "' AND setter = '" + setter + "'";
	// this.MySql.update(updateAbility);
	// } else {
	// String insertAbility = "INSERT INTO bending_ability VALUES('"
	// + player
	// + "','"
	// + setter
	// + "','"
	// + ability.name()
	// + "')";
	// this.MySql.insert(insertAbility);
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// // public Abilities getAbility(String player) {
	// // if (player instanceof Player) {
	// // if (ConfigManager.bendToItem == false)
	// // return getAbility(player, ((Player) player).getInventory()
	// // .getHeldItemSlot());
	// // return getAbility(player, ((Player) player).getItemInHand()
	// // .getType());
	// // }
	// // return null;
	// // }
	//
	// public Abilities getAbility(String player, int slot) {
	// String ability = "";
	// String setter = player + "<Bind" + slot + ">";
	// if (StorageManager.useFlatFile) {
	// ability = config.getKey(setter);
	// } else if (StorageManager.useMySQL) {
	// String selectAbility =
	// "SELECT ability FROM bending_ability WHERE setter ='"
	// + setter + "' AND player = '" + player + "'";
	// ResultSet abilitySet = this.MySql.select(selectAbility);
	//
	// try {
	// if (abilitySet.next()) {
	// ability = abilitySet.getString("ability");
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// for (Abilities a : Abilities.values()) {
	// if (ability.equalsIgnoreCase(a.name()))
	// return a;
	// }
	// return null;
	// }
	//
	// // Bind to item
	//
	// public Abilities getAbility(String player, Material mat) {
	// String ability = "";
	// String setter = player + "<Bind" + mat.name() + ">";
	// if (StorageManager.useFlatFile) {
	// ability = config.getKey(setter);
	// } else if (StorageManager.useMySQL) {
	// String selectAbility =
	// "SELECT ability FROM bending_ability WHERE setter ='"
	// + setter + "' AND player = '" + player + "'";
	// ResultSet abilitySet = this.MySql.select(selectAbility);
	//
	// try {
	// if (abilitySet.next()) {
	// ability = abilitySet.getString("ability");
	// }
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// for (Abilities a : Abilities.values()) {
	// if (ability.equalsIgnoreCase(a.name()))
	// return a;
	// }
	// return null;
	// }
	//
	// // public boolean hasAbility(Player player, Abilities ability) {
	// // for (int i = 0; i <= 8; i++) {
	// // if (getAbility(player, i) != null)
	// // if (getAbility(player, i) == ability)
	// // return true;
	// // }
	// // return false;
	// // }
	//
	// public List<BendingType> getBendingTypes(Player player) {
	// List<BendingType> list = Arrays.asList();
	//
	// for (BendingType type : BendingType.values()) {
	// if (isBender(player.getName(), type)) {
	// list.add(type);
	// }
	// }
	// return list;
	// }
	//
	// public List<BendingType> getBendingTypes(String player) {
	// List<BendingType> list = new ArrayList<BendingType>();
	//
	// for (BendingType type : BendingType.values()) {
	// if (isBender(player, type)) {
	// list.add(type);
	// }
	// }
	// return list;
	// }
	//
	// public void removeAbility(OfflinePlayer player, int slot) {
	// if (StorageManager.useFlatFile) {
	// String setter = player.getName() + "<Bind" + slot + ">";
	// config.setKey(setter, null);
	// } else if (StorageManager.useMySQL) {
	// String setter = player.getName() + "<Bind" + slot + ">";
	// String removeBind = "DELETE FROM bending_ability WHERE player ='"
	// + player.getName() + "' AND setter = '" + setter + "'";
	// this.MySql.delete(removeBind);
	// }
	//
	// }
	//
	// public void removeAbility(OfflinePlayer player, Material mat) {
	// if (StorageManager.useFlatFile) {
	// String setter = player.getName() + "<Bind" + mat.name() + ">";
	// config.setKey(setter, null);
	// }
	// }
	//
	// public void permaRemoveBending(OfflinePlayer player) {
	// removeBending(player);
	// BendingType type = null;
	// setBending(player, type);
	//
	// }
	//
	// public void initialize(File file) {
	// StorageManager.useMySQL = ConfigManager.useMySQL;
	// StorageManager.useFlatFile = !ConfigManager.useMySQL;
	// if (StorageManager.useMySQL) {
	// this.MySql = new MySQL(ConfigManager.dbHost, ConfigManager.dbUser,
	// ConfigManager.dbPass, ConfigManager.dbDB,
	// ConfigManager.dbPort);
	// if (this.MySql.initialize()) {
	// String createTable1 =
	// "CREATE TABLE IF NOT EXISTS bending_element(player TEXT NOT NULL, bending TEXT NOT NULL)";
	// String createTable2 =
	// "CREATE TABLE IF NOT EXISTS bending_ability(player TEXT NOT NULL, setter TEXT NOT NULL, ability TEXT NOT NULL)";
	// String createTable3 =
	// "CREATE TABLE IF NOT EXISTS bending_language(player TEXT NOT NULL, language TEXT NOT NULL)";
	// MySql.execute(createTable1);
	// MySql.execute(createTable2);
	// MySql.execute(createTable3);
	// } else {
	// this.MySql = null;
	// this.config = new BendingPlayers(file);
	// StorageManager.useMySQL = false;
	// StorageManager.useFlatFile = true;
	// }
	// } else if (StorageManager.useFlatFile) {
	// this.config = new BendingPlayers(file);
	// }
	// // Tools.verbose(StorageManager.useFlatFile ? "Flat" : "MySQL");
	// }
	//
	// public void close() {
	// if (useMySQL) {
	// dataFolder = null;
	// MySql.close();
	// MySql = null;
	// } else {
	// config.close();
	// config = null;
	// }
	// }
	//
	// public BendingPlayer getBendingPlayer(String playername) {
	// if (config.checkKeys(playername)) {
	// return (BendingPlayer) config.get(playername);
	// }
	// return null;
	// }

}
