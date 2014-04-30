package main;

public class PlayerStorageWriter {

}

// public class PlayerStorageWriter implements Runnable {

// private static ConcurrentHashMap<Integer, Queue> queue = new
// ConcurrentHashMap<Integer, Queue>();
// private static int ID = Integer.MIN_VALUE;
//
// private static boolean run = true;
//
// private static class Queue {
//
// public boolean addBending, removeBending, setBending, bindSlot,
// bindItem, removeSlot, removeItem, setLanguage,
// permaRemoveBending;
// public int slot;
// public Material item;
// public Abilities ability;
// public BendingType type;
// public OfflinePlayer player;
// private String language;
//
// public Queue(OfflinePlayer player, int slot, Abilities ability) {
// this.player = player;
// bindSlot = true;
// this.slot = slot;
// this.ability = ability;
//
// }
//
// public Queue(OfflinePlayer player, Material item, Abilities ability) {
// this.player = player;
// bindItem = true;
// this.item = item;
// this.ability = ability;
//
// }
//
// public Queue(OfflinePlayer player, BendingType type, boolean add) {
// this.player = player;
// this.type = type;
// if (add) {
// addBending = true;
// } else {
// setBending = true;
// }
//
// }
//
// public Queue(OfflinePlayer player, int slot) {
// this.player = player;
// removeSlot = true;
// this.slot = slot;
//
// }
//
// public Queue(OfflinePlayer player, Material item) {
// this.player = player;
// removeItem = true;
// this.item = item;
//
// }
//
// public Queue(OfflinePlayer player, boolean permanent) {
// this.player = player;
// if (permanent) {
// permaRemoveBending = true;
// } else {
// removeBending = true;
// }
//
// }
//
// public Queue(OfflinePlayer player, String language) {
// this.player = player;
// setLanguage = true;
// this.language = language;
// }
//
// public String toString() {
// String string = "Queue{ Player: " + player + ", slot: " + slot
// + ", item: " + item + ", ability: " + ability + ", type: "
// + type + ", language: " + language + "}";
// return string;
// }
//
// }
//
// public static void addBending(OfflinePlayer player, BendingType type) {
// queue.put(ID++, new Queue(player, type, true));
// BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
// bPlayer.addBender(type);
// }
//
// public static void removeBending(OfflinePlayer player) {
// queue.put(ID++, new Queue(player, false));
// BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
// bPlayer.removeBender();
// }
//
// public static void permaRemoveBending(OfflinePlayer player) {
// queue.put(ID++, new Queue(player, true));
// BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
// bPlayer.removeBender();
// }
//
// public static void setBending(OfflinePlayer player, BendingType type) {
// queue.put(ID++, new Queue(player, type, false));
// BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
// bPlayer.setBender(type);
// }
//
// public static void bindSlot(OfflinePlayer player, int slot,
// Abilities ability) {
// queue.put(ID++, new Queue(player, slot, ability));
// BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
// bPlayer.setAbility(slot, ability);
// }
//
// public static void bindItem(OfflinePlayer player, Material item,
// Abilities ability) {
// queue.put(ID++, new Queue(player, item, ability));
// BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
// bPlayer.setAbility(item, ability);
// }
//
// public static void removeSlot(OfflinePlayer player, int slot) {
// queue.put(ID++, new Queue(player, slot));
// BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
// bPlayer.removeAbility(slot);
// }
//
// public static void removeItem(OfflinePlayer player, Material item) {
// queue.put(ID++, new Queue(player, item));
// BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
// bPlayer.removeAbility(item);
// }
//
// public static void setLanguage(OfflinePlayer player, String language) {
// BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
// bPlayer.setLanguage(language);
// queue.put(ID++, new Queue(player, language));
// }
//
// private class Task implements Callable<Object> {
//
// Queue item;
//
// public Task(Queue queue) {
// item = queue;
// }
//
// @Override
// public Object call() throws Exception {
// if (item.addBending) {
// Tools.config.addBending(item.player.getName(), item.type);
// } else if (item.removeBending) {
// Tools.config.removeBending(item.player);
// } else if (item.setBending) {
// Tools.config.setBending(item.player, item.type);
// } else if (item.bindSlot) {
// Tools.config.setAbility(item.player.getName(), item.ability,
// item.slot);
// } else if (item.bindItem) {
// Tools.config.setAbility(item.player.getName(), item.ability,
// item.item);
// } else if (item.removeSlot) {
// Tools.config.removeAbility(item.player, item.slot);
// } else if (item.removeItem) {
// Tools.config.removeAbility(item.player, item.item);
// } else if (item.permaRemoveBending) {
// Tools.config.permaRemoveBending(item.player);
// } else if (item.setLanguage) {
// Tools.config.setLanguage(item.player, item.language);
// }
// return null;
// }
//
// }
//
// @Override
// public void run() {
//
// try {
//
// // Bending plugin = Bending.plugin;
// // Future<Object> returnFuture;
//
// if (queue.isEmpty()) {
// ID = Integer.MIN_VALUE;
// // Tools.verbose("Queue empty.");
// return;
// }
//
// ArrayList<Integer> index = new ArrayList<Integer>(
// new TreeSet<Integer>(queue.keySet()));
//
// // for (int i : queue.keySet()) {
// for (int i = 0; i < index.size(); i++) {
// if (run) {
// Queue item = queue.get(index.get(i));
// // Tools.verbose(item);
// if (item != null) {
// Task task = new Task(item);
// task.call();
// }
//
// // returnFuture = plugin.getServer().getScheduler()
// // .callSyncMethod(plugin, task);
// // returnFuture.get();
//
// queue.remove(index.get(i));
//
// } else {
// break;
// }
// }
//
// } catch (Exception e) {
// Tools.writeToLog(ExceptionUtils.getStackTrace(e));
// e.printStackTrace();
// }
//
// }
//
// public static void finish() {
// run = false;
// ArrayList<Integer> index = new ArrayList<Integer>(new TreeSet<Integer>(
// queue.keySet()));
//
// // for (int i : queue.keySet()) {
// for (int i = 0; i < index.size(); i++) {
// Queue item = queue.get(index.get(i));
// // Tools.verbose(item);
//
// if (item.addBending) {
// Tools.config.addBending(item.player.getName(), item.type);
// } else if (item.removeBending) {
// Tools.config.removeBending(item.player);
// } else if (item.setBending) {
// Tools.config.setBending(item.player, item.type);
// } else if (item.bindSlot) {
// Tools.config.setAbility(item.player.getName(), item.ability,
// item.slot);
// } else if (item.bindItem) {
// Tools.config.setAbility(item.player.getName(), item.ability,
// item.item);
// } else if (item.removeSlot) {
// Tools.config.removeAbility(item.player, item.slot);
// } else if (item.removeItem) {
// Tools.config.removeAbility(item.player, item.item);
// } else if (item.permaRemoveBending) {
// Tools.config.permaRemoveBending(item.player);
// } else if (item.setLanguage) {
// Tools.config.setLanguage(item.player, item.language);
// }
//
// queue.remove(index.get(i));
//
// }
// Tools.config.close();
// }

// }
