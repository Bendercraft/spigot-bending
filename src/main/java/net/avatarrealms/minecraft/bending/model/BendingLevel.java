package net.avatarrealms.minecraft.bending.model;

import java.util.Random;

import org.bukkit.Bukkit;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.data.BendingLevelData;

public class BendingLevel {

	private static int defaultExperience = 5;
	private static float loseExperienceOnSpam = 0.05f;

	private BendingPlayer bPlayer;
	private BendingType bendingType;
	private Integer level = 1;
	private Integer experience = 0;
	private long lasttime;
	//private long firstTimeDegression;
	private int currentXPToReceive;

	public BendingLevel(BendingType type, BendingPlayer player) {
		this.bPlayer = player;
		this.bendingType = type;
		this.level = 1;
		this.experience = 0;
		
		this.lasttime = 0;
		//this.firstTimeDegression = 0;
		this.currentXPToReceive = defaultExperience;
	}

	public Integer getLevel() {
		return level;
	}

	public BendingType getBendingType() {
		return bendingType;
	}

	public String toString() {
		String str = "";
		switch (bendingType) {
		case Air: str+= "Air : "; break;
		case Earth : str+= "Earth : "; break;
		case Fire : str += "Fire : "; break;
		case Water : str += "Water : "; break;
		case ChiBlocker : str += "ChiBlocker : "; break;
		}
		str += " Level " + level;

		if (level < ConfigManager.maxlevel) {
			str += " with " + experience + "/" + getExperienceNeeded()
					+ " experience";
		}
		return str;
	}

	public Integer getExperienceNeeded() {
		double xp;
		Integer xpArr = (int) 0;
		xp = (8*level);
		if (level >= 32) {
			xp += 5*(level-30);
		}
		else if (level == 31){
			xp += 6;
		}
		else if (level == 30) {
			xp += 3;
		}
		else {
			xp += 1;
		}
		xp*=(5*level + 45);
		xpArr = ((int)(xp/100))*100;;

		return xpArr;
	}

	public BendingLevel(BendingLevelData data) {
		bendingType = data.getBendingType();
		level = data.getLevel();
		experience = data.getExperience();
	}

	public BendingLevelData serialize() {
		BendingLevelData result = new BendingLevelData();
		result.setBendingType(bendingType);
		result.setLevel(level);
		result.setExperience(experience);
		return result;
	}

	public static BendingLevel deserialize(BendingLevelData data) {
		return new BendingLevel(data);
	}

	public static BendingLevel valueOf(BendingLevelData data) {
		return deserialize(data);
	}

	public void setLevel(Integer level) {
		if (level >= ConfigManager.maxlevel) {
			level = ConfigManager.maxlevel;
		}
		if (level < 1) {
			level = 1;
		}
		experience = 0;
		this.level = level;
	}

	public void giveXP(Integer xpAmount) {
		if (level < ConfigManager.maxlevel) {
			experience += xpAmount;
			while (experience >= getExperienceNeeded()) {
				experience -= getExperienceNeeded();
				if (level < ConfigManager.maxlevel) {
					String str = "Level up : ";
					level++;
					str += level;
					
					switch (bendingType) {
					case Air : str += " (Air)"; break;
					case Earth : str += " (Terre)"; break;
					case ChiBlocker : str += " (ChiBlocker)"; break;
					case Fire : str += " (Feu)"; break;
					case Water : str += " (Eau)";break;
					default : break;
					}
	
					bPlayer.getPlayer().sendMessage(str);
				}

			}
		}
	}

	public void earnXP() {
		Random rand = new Random();
		Integer xpReceived = 0;
		long now = System.currentTimeMillis();
		if (lasttime - now >= 120000) { // 2 minutes
			currentXPToReceive = defaultExperience;
		}
		else {
			if (lasttime - now < 300) {
				if (lasttime - now < 100) {
					currentXPToReceive *= (1 - 2 * loseExperienceOnSpam);
				}
				else {
					currentXPToReceive *= (1 - loseExperienceOnSpam);
				}		
			}
		}
		if (level >= 10) {
			xpReceived = currentXPToReceive * (rand.nextInt(level/10)+1);
		}
		else {
			xpReceived = currentXPToReceive;
		}
		xpReceived = currentXPToReceive * (rand.nextInt(level/10)+1);
		giveXP(xpReceived);
		lasttime = now;
	}

	public void setXP(double d) {
		experience = (int) d;
	}

	public int getXP() {
		return experience;
	}

	public void setBendingPlayer(BendingPlayer player) {
		this.bPlayer = player;
	}
}
