package net.bendercraft.spigot.bending.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.BendingPlayerData;
import net.bendercraft.spigot.bending.controller.Settings;

public class MySQLDB {
	
	private static Map<UUID, BendingPlayerData> schedule = Collections.synchronizedMap(new HashMap<UUID, BendingPlayerData>());
	private static Object lock = new Object();
	
	private static Connection connection;
	
	private static List<UUID> fetching = new LinkedList<UUID>();
	private static List<UUID> updating = new LinkedList<UUID>();
	private static List<UUID> clearing = new LinkedList<UUID>();
	
	public static Connection openConnection() throws SQLException, ClassNotFoundException {
	    if (connection != null && !connection.isClosed()) {
	        return connection;
	    }

	    synchronized (lock) {
	        if (connection != null && !connection.isClosed()) {
	            return connection;
	        }
	        Class.forName("com.mysql.jdbc.Driver");
	        connection = DriverManager.getConnection("jdbc:mysql://" + Settings.DATABASE_HOST+ ":" + Settings.DATABASE_PORT + "/" + Settings.DATABASE_DB, Settings.DATABASE_USER, Settings.DATABASE_PASSWORD);
	    }
	    return connection;
	}
	
	public static void credentials(UUID player, String username, String token) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				Connection connection = null;
				try {
					connection = openConnection();
					connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
					connection.setAutoCommit(false);
					
					PreparedStatement credentials = connection.prepareStatement("INSERT INTO credentials(player_uuid, username, token) VALUES (?,?,?) ON DUPLICATE KEY UPDATE username = ?, token= ?");
					credentials.setString(1, player.toString());
					credentials.setString(2, username);
					credentials.setString(3, token);
					credentials.setString(4, username);
					credentials.setString(5, token);
					credentials.executeUpdate();
					
					connection.commit();
					Bending.getInstance().getLogger().info("Saved credentials for "+player);
				} catch (SQLException | ClassNotFoundException e) {
					Bending.getInstance().getLogger().log(Level.SEVERE, "Error while saving credentials "+player, e);
					try {
						if(connection != null) {
							connection.rollback();
						}
					} catch (SQLException e1) {
						Bending.getInstance().getLogger().log(Level.SEVERE, "Error while rollback connection", e1);
					}
				}
			}
		};
		
		Bending.getInstance().getServer().getScheduler().runTaskAsynchronously(Bending.getInstance(), run);
	}
	
	public static void fetch(UUID player) {
		if(fetching.contains(player)) {
			return;
		}
		fetching.add(player);
		Runnable run = new Runnable() {
			@Override
			public void run() {
				BendingPlayerData result = new BendingPlayerData();
				result.setPlayer(player);
				Connection connection = null;
				try {
					connection = openConnection();
					connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
					connection.setAutoCommit(false);
					
					result.setPerks(new LinkedList<BendingPerk>());
					PreparedStatement skills = connection.prepareStatement("SELECT perk FROM perks WHERE player_uuid = ?");
					skills.setString(1, player.toString());
					ResultSet rs = skills.executeQuery();
					while(rs.next()) {
						result.getPerks().add(BendingPerk.valueOf(rs.getString("perk")));
					}
					
					result.setBendings(new LinkedList<BendingElement>());
					PreparedStatement elements = connection.prepareStatement("SELECT element FROM elements WHERE player_uuid = ?");
					elements.setString(1, player.toString());
					rs = elements.executeQuery();
					while(rs.next()) {
						result.getBendings().add(BendingElement.valueOf(rs.getString("element")));
					}
					
					result.setAffinities(new LinkedList<BendingAffinity>());
					PreparedStatement affinities = connection.prepareStatement("SELECT affinity FROM affinities WHERE player_uuid = ?");
					affinities.setString(1, player.toString());
					rs = affinities.executeQuery();
					while(rs.next()) {
						result.getAffinities().add(BendingAffinity.valueOf(rs.getString("affinity")));
					}
					
					result.setAbilities(new LinkedList<String>());
					PreparedStatement abilities = connection.prepareStatement("SELECT ability FROM abilities WHERE player_uuid = ?");
					abilities.setString(1, player.toString());
					rs = abilities.executeQuery();
					while(rs.next()) {
						result.getAbilities().add(rs.getString("ability"));
					}
					
					result.setDecks(new HashMap<String, Map<Integer, String>>());
					PreparedStatement decks = connection.prepareStatement("SELECT uuid, owner_uuid, name, current FROM decks WHERE owner_uuid = ?");
					decks.setString(1, player.toString());
					rs = decks.executeQuery();
					while(rs.next()) {
						 Map<Integer, String> entries = new HashMap<Integer, String>();
						 PreparedStatement deckEntries = connection.prepareStatement("SELECT slot, ability FROM deck_entries WHERE deck_uuid = ?");
						 deckEntries.setString(1, rs.getString("uuid"));
						 ResultSet rsEntries = deckEntries.executeQuery();
						 while(rsEntries.next()) {
							 entries.put(rsEntries.getInt("slot"), rsEntries.getString("ability"));
						 }
						 result.getDecks().put(rs.getString("name"), entries);
						 if(rs.getBoolean("current")) {
							 result.setCurrentDeck(rs.getString("name"));
						 }
					}
					
					connection.commit();
					Bending.getInstance().getLogger().info("Fetched player "+player);
					Runnable insert = new Runnable() {
						@Override
						public void run() {
							BendingPlayer.insert(result);
							Bending.getInstance().getLogger().info("Inserted player "+player);
							fetching.remove(player);
						}
					};
					Bending.getInstance().getServer().getScheduler().runTask(Bending.getInstance(), insert);
				} catch (SQLException | ClassNotFoundException e) {
					Bending.getInstance().getLogger().log(Level.SEVERE, "Error while fetching "+player, e);
					try {
						if(connection != null) {
							connection.rollback();
						}
					} catch (SQLException e1) {
						Bending.getInstance().getLogger().log(Level.SEVERE, "Error while rollback connection", e1);
					}
					Runnable remove = new Runnable() {
						@Override
						public void run() {
							fetching.remove(player);
							Bending.getInstance().getLogger().log(Level.SEVERE, "Removing fetching status of "+player);
						}
					};
					Bending.getInstance().getServer().getScheduler().runTask(Bending.getInstance(), remove);
				}
			}
		};
		Bending.getInstance().getServer().getScheduler().runTaskAsynchronously(Bending.getInstance(), run);
	}
	
	public static void update(UUID player) {
		if(updating.contains(player)) {
			return;
		}
		updating.add(player);
		Runnable run = new Runnable() {
			@Override
			public void run() {
				BendingPlayerData result = new BendingPlayerData();
				result.setPlayer(player);
				Connection connection = null;
				try {
					connection = openConnection();
					connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
					connection.setAutoCommit(false);
					
					result.setPerks(new LinkedList<BendingPerk>());
					PreparedStatement skills = connection.prepareStatement("SELECT perk FROM perks WHERE player_uuid = ?");
					skills.setString(1, player.toString());
					ResultSet rs = skills.executeQuery();
					while(rs.next()) {
						result.getPerks().add(BendingPerk.valueOf(rs.getString("perk")));
					}
					
					connection.commit();
					Runnable insert = new Runnable() {
						@Override
						public void run() {
							BendingPlayer.update(result);
							updating.remove(player);
						}
					};
					Bending.getInstance().getServer().getScheduler().runTask(Bending.getInstance(), insert);
				} catch (SQLException | ClassNotFoundException e) {
					Bending.getInstance().getLogger().log(Level.SEVERE, "Error while fetching for updare "+player, e);
					try {
						if(connection != null) {
							connection.rollback();
						}
					} catch (SQLException e1) {
						Bending.getInstance().getLogger().log(Level.SEVERE, "Error while rollback connection", e1);
					}
					Runnable remove = new Runnable() {
						@Override
						public void run() {
							updating.remove(player);
							Bending.getInstance().getLogger().log(Level.SEVERE, "Removing updating status of "+player);
						}
					};
					Bending.getInstance().getServer().getScheduler().runTask(Bending.getInstance(), remove);
				}
			}
		};
		Bending.getInstance().getServer().getScheduler().runTaskAsynchronously(Bending.getInstance(), run);
	}

	public static void save(UUID player, BendingPlayerData data) {
		schedule.put(player, data);
	}
	
	public static void clearPerks(UUID player) {
		if(clearing.contains(player)) {
			return;
		}
		clearing.add(player);
		Runnable run = new Runnable() {
			@Override
			public void run() {
				Connection connection = null;
				try {
					connection = openConnection();
					connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
					connection.setAutoCommit(false);
					
					PreparedStatement clearPerks = connection.prepareStatement("DELETE FROM perks WHERE player_uuid = ?");
					clearPerks.setString(1, player.toString());
					clearPerks.executeUpdate();
					
					connection.commit();
					Runnable insert = new Runnable() {
						@Override
						public void run() {
							clearing.remove(player);
						}
					};
					Bending.getInstance().getServer().getScheduler().runTask(Bending.getInstance(), insert);
				} catch (SQLException | ClassNotFoundException e) {
					Bending.getInstance().getLogger().log(Level.SEVERE, "Error while fetching for clearing perks "+player, e);
					try {
						if(connection != null) {
							connection.rollback();
						}
					} catch (SQLException e1) {
						Bending.getInstance().getLogger().log(Level.SEVERE, "Error while rollback connection", e1);
					}
					Runnable remove = new Runnable() {
						@Override
						public void run() {
							clearing.remove(player);
							Bending.getInstance().getLogger().log(Level.SEVERE, "Removing clearing status of "+player);
						}
					};
					Bending.getInstance().getServer().getScheduler().runTask(Bending.getInstance(), remove);
				}
			}
		};
		Bending.getInstance().getServer().getScheduler().runTaskAsynchronously(Bending.getInstance(), run);
	}

	// Sync task
	public static class UpdateTask implements Runnable {
		@Override
		public void run() {
			for(Player player : Bukkit.getServer().getOnlinePlayers()) {
				MySQLDB.update(player.getUniqueId());
			}
		}
	}
	
	// Async task - do not use Bukkit resources /!\
	public static class SaveTask implements Runnable {
		@Override
		public void run() {
			Iterator<Entry<UUID, BendingPlayerData>> it = schedule.entrySet().iterator();
			while(it.hasNext()) {
				Entry<UUID, BendingPlayerData> itEntry = it.next();
				UUID player = itEntry.getKey();
				BendingPlayerData data = itEntry.getValue();
				Connection connection = null;
				try {
					connection = openConnection();
					connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
					connection.setAutoCommit(false);
					
					PreparedStatement players = connection.prepareStatement("INSERT INTO players(uuid, name) VALUES (?,?) ON DUPLICATE KEY UPDATE name = ?");
					players.setString(1, data.getPlayer().toString());
					players.setString(2, Bending.getInstance().getServer().getOfflinePlayer(data.getPlayer()).getName());
					players.setString(3, Bending.getInstance().getServer().getOfflinePlayer(data.getPlayer()).getName());
					players.executeUpdate();
					
					PreparedStatement cleanElements = connection.prepareStatement("DELETE FROM elements WHERE player_uuid = ?");
					cleanElements.setString(1, data.getPlayer().toString());
					cleanElements.executeUpdate();
					PreparedStatement elements = connection.prepareStatement("INSERT INTO elements(player_uuid, element) VALUES (?,?)");
					for(BendingElement element : data.getBendings()) {
						elements.setString(1, data.getPlayer().toString());
						elements.setString(2, element.name());
						elements.addBatch();
					}
					elements.executeBatch();
					
					PreparedStatement cleanAffinities = connection.prepareStatement("DELETE FROM affinities WHERE player_uuid = ?");
					cleanAffinities.setString(1, data.getPlayer().toString());
					cleanAffinities.executeUpdate();
					PreparedStatement affinities = connection.prepareStatement("INSERT INTO affinities(player_uuid, affinity) VALUES (?,?)");
					for(BendingAffinity affinity : data.getAffinities()) {
						affinities.setString(1, data.getPlayer().toString());
						affinities.setString(2, affinity.name());
						affinities.addBatch();
					}
					affinities.executeBatch();
					
					PreparedStatement cleanAbilities = connection.prepareStatement("DELETE FROM abilities WHERE player_uuid = ?");
					cleanAbilities.setString(1, data.getPlayer().toString());
					cleanAbilities.executeUpdate();
					PreparedStatement abilities = connection.prepareStatement("INSERT INTO abilities(player_uuid, ability) VALUES (?,?)");
					for(String ability : data.getAbilities()) {
						abilities.setString(1, data.getPlayer().toString());
						abilities.setString(2, ability);
						abilities.addBatch();
					}
					abilities.executeBatch();
					
					PreparedStatement cleanDeckEntries = connection.prepareStatement("DELETE FROM deck_entries WHERE deck_uuid IN (SELECT uuid FROM decks WHERE owner_uuid = ?)");
					cleanDeckEntries.setString(1, data.getPlayer().toString());
					cleanDeckEntries.executeUpdate();
					PreparedStatement cleanDecks = connection.prepareStatement("DELETE FROM decks WHERE owner_uuid = ?");
					cleanDecks.setString(1, data.getPlayer().toString());
					cleanDecks.executeUpdate();
					PreparedStatement decks = connection.prepareStatement("INSERT INTO decks(uuid, owner_uuid, name, current) VALUES (?,?,?,?)");
					PreparedStatement deckEntries = connection.prepareStatement("INSERT INTO deck_entries(deck_uuid, slot, ability) VALUES (?,?,?)");
					for(Entry<String, Map<Integer, String>> deck : data.getDecks().entrySet()) {
						UUID uuid = UUID.randomUUID();
						decks.setString(1, uuid.toString());
						decks.setString(2, data.getPlayer().toString());
						decks.setString(3, deck.getKey());
						decks.setBoolean(4, data.getCurrentDeck() != null && data.getCurrentDeck().equals(deck.getKey()));
						decks.addBatch();
						for(Entry<Integer, String> entry : deck.getValue().entrySet()) {
							deckEntries.setString(1, uuid.toString());
							deckEntries.setInt(2, entry.getKey());
							deckEntries.setString(3, entry.getValue());
							deckEntries.addBatch();
						}
					}
					decks.executeBatch();
					deckEntries.executeBatch();
					
					connection.commit();
					Bending.getInstance().getLogger().info("Saved player "+player);
				} catch (Exception e) {
					Bending.getInstance().getLogger().log(Level.SEVERE, "Error while saving "+player, e);
					try {
						if(connection != null) {
							connection.rollback();
						}
					} catch (SQLException e1) {
						Bending.getInstance().getLogger().log(Level.SEVERE, "Error while rollback connection", e1);
					}
				}
				it.remove();
			}
		}
	}
}
