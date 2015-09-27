package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.controller.Settings;

/**
 * Class that manages combo points for a chiblocker (There is no check about
 * being a chiblocker or not)
 *
 * @author Noko
 */
public class ComboPoints {

	private static final int MAX_COMBO_POINTS = 5;
	
	public static final Sound SOUND = Sound.LEVEL_UP;
	public static final Effect VISUAL = Effect.HAPPY_VILLAGER;

	private static Map<UUID, ComboPoints> combos = new HashMap<UUID, ComboPoints>();

	@SuppressWarnings("unused")
	private Player player;
	private long lastTime;
	private int comboAmount;
	private LivingEntity target;

	private ComboPoints(Player player) {
		this.player = player;
		this.comboAmount = 0;
		this.target = null;
		this.lastTime = 0;
	}

	/**
	 * Add a combo point to a player
	 *
	 * @param player
	 *            The {@link org.bukkit.entity.Player player} that receive a
	 *            combo point
	 * @param target
	 *            The target that the player has hit to get the combo point.
	 *            Know that if the target is different from the previous target,
	 *            combo points will be reset
	 * @return <code>true</code> if the combo point has been added (even if
	 *         already at maximum) <code>false</code> if no target or if too
	 *         soon after the previous combo point addition
	 */
	@SuppressWarnings ("deprecation")
	public static boolean addComboPoint(Player player, LivingEntity target) {
		ComboPoints combo = null;
		if (combos.containsKey(player.getUniqueId())) {
			combo = combos.get(player.getUniqueId());
		}
		if (combo == null) {
			combo = new ComboPoints(player);
			combos.put(player.getUniqueId(), combo);
		}

		if (combo.addComboPoint(target)) {
			player.playSound(player.getLocation(), SOUND, 1, 1.1f);
			player.playEffect(target.getEyeLocation(), VISUAL, 0x1);
			return true;
		}
		return false;
	}
	
	/**
	 * Add a combo point to a player
	 *
	 * @param player
	 *        The {@link org.bukkit.entity.Player player} that receive a
	 *        combo point
	 * @param target
	 *        The target that the player has hit to get the combo point.
	 *        Know that if the target is different from the previous target,
	 *        combo points will be reset
	 * @param amount
	 *        The amount of combo points to add
	 * @return <code>true</code> if the combo point has been added (even if
	 *         already at maximum) <code>false</code> if no target or if too
	 *         soon after the previous combo point addition
	 */
	@SuppressWarnings ("deprecation")
	public static boolean addComboPoint(Player player, LivingEntity target, int amount) {
		ComboPoints combo = null;
		if (combos.containsKey(player.getUniqueId())) {
			combo = combos.get(player.getUniqueId());
		}
		if (combo == null) {
			combo = new ComboPoints(player);
			combos.put(player.getUniqueId(), combo);
		}

		if (combo.addComboPoint(target, amount)) {
			player.playSound(player.getLocation(), SOUND, 1, 1.1f);
			player.playEffect(target.getEyeLocation(), VISUAL, 0x1);
			return true;
		}
		return false;
	}

	/**
	 * Get how many combo points has a player got
	 *
	 * @param player
	 *            The player whose you want the combo points
	 * @return <code>0</code> if the player is null or never got point before
	 *         <code> a number between 0 and 5 included</code> according to the
	 *         player combo amount
	 */
	public static int getComboPointAmount(Player player) {
		if (player == null) {
			return 0;
		}
		if (combos.containsKey(player.getUniqueId())) {
			ComboPoints combo = combos.get(player.getUniqueId());
			if (combo != null) {
				return combo.getComboAmount();
			} else {
				combos.remove(player.getUniqueId());
			}
		}
		return 0;
	}

	private int getComboAmount() {
		long now = System.currentTimeMillis();
		if ((now - this.lastTime) > Settings.CHI_COMBO_RESET) {
			this.comboAmount = 0;
		}
		if (this.comboAmount < 0) {
			this.comboAmount = 0;
		}
		this.lastTime = now;
		return this.comboAmount;
	}

	private boolean addComboPoint(LivingEntity target) {
		if (target == null) {
			return false;
		}
		long now = System.currentTimeMillis();
		
		if (!target.equals(this.target)) {
			this.comboAmount = 0;
			this.target = target;
		}

		if ((this.lastTime != 0) && ((now - this.lastTime) > Settings.CHI_COMBO_RESET)) {
			this.comboAmount = 0;
		}

		if (this.comboAmount < MAX_COMBO_POINTS) {
			this.comboAmount++;
		}

		this.lastTime = now;

		return true;
	}

	private boolean addComboPoint(LivingEntity target, int amount) {
		if (target == null) {
			return false;
		}
		long now = System.currentTimeMillis();

		if (!target.equals(this.target)) {
			this.comboAmount = 0;
			this.target = target;
		}

		if ((this.lastTime != 0) && ((now - this.lastTime) > Settings.CHI_COMBO_RESET)) {
			this.comboAmount = 0;
		}

		if (this.comboAmount < MAX_COMBO_POINTS) {
			this.comboAmount += amount;
		}
		
		this.lastTime = now;
		return true;
	}

	/**
	 * Use some combo points to improve an ability
	 *
	 * @param player
	 *            The player that use combo points
	 * @param amount
	 *            How many combo points must be used
	 * @return <code>true</code> If player can consume his combo points,
	 *         <code>false</code> if no player, if the amount is less than 0 or
	 *         if player has less combo point that he wants to use
	 */
	public static boolean consume(Player player, int amount) {
		if (player == null) {
			return false;
		}

		if (amount < 0) {
			return false;
		}

		if (amount > MAX_COMBO_POINTS) {
			amount = MAX_COMBO_POINTS;
		}

		if (!combos.containsKey(player.getUniqueId())) {
			return false;
		}

		ComboPoints combo = combos.get(player.getUniqueId());
		if (combo == null) {
			combos.remove(player.getUniqueId());
			return false;
		}

		if (combo.comboAmount < amount) {
			return false;
		}

		combo.consume(amount);

		return true;
	}

	/**
	 * Use all combo points to improve an ability
	 *
	 * @param player
	 *            The player that consume his combo points.
	 * @return <code>true</code> if the player has consumed his combo points
	 *         <code>false</code> if no player, or if player has no combo points
	 */
	public static boolean consume(Player player) {
		if (player == null) {
			return false;
		}

		if (!combos.containsKey(player.getUniqueId())) {
			return false;
		}

		ComboPoints combo = combos.get(player.getUniqueId());
		if (combo == null) {
			combos.remove(player.getUniqueId());
			return false;
		}

		if (combo.comboAmount == 0) {
			return false;
		}

		combo.consume();

		return true;
	}

	private void consume() {
		this.comboAmount = 0;
		this.lastTime = System.currentTimeMillis();
	}

	private void consume(int amount) {
		this.comboAmount -= amount;
		this.lastTime = System.currentTimeMillis();
	}

}
