package net.avatarrealms.minecraft.bending.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.data.BendingLevelData;

// http://rechneronline.de/function-graphs/
public class BendingLevel {
	// 240 seconds, but SPAM_THRESHOLD is in milliseconds
	public static int SPAM_THRESHOLD = 240 * 1000;
	
	private BendingPlayer bPlayer;
	private BendingType bendingType;
	private Integer level = 1;
	private Integer experience = 0;
	
	private long lastTime;
	private Location lastLocation;
	private double spamHistory = 0;

	public BendingLevel(BendingType type, BendingPlayer player) {
		this.bPlayer = player;
		this.bendingType = type;
		this.level = 1;
		this.experience = 0;
		this.lastTime = 0;
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
		//((8*x)+6)*(5*x+45)
		double xp = 0;
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
						case Air : 
							str += " (Air)";
							break;
						case Earth : 
							str += " (Terre)";
							break;
						case ChiBlocker : 
							str += " (ChiBlocker)";
							break;
						case Fire : 
							str += " (Feu)";
							break;
						case Water : 
							str += " (Eau)";
							break;
						default :
							break;
					}
					bPlayer.getPlayer().sendMessage(str);
				}

			}
		}
	}
	
	private double degress(double x) {
		// (e^(x*0.1) -1)/100
		double result = 0;
		
		result = (double) (Math.exp(x*0.1) - 1) / 100;
		
		//Since this function is not limited on Y range between 0-1, just make it so
		if(result > 1) {
			result = 1;
		}
		if(result < 0) {
			result = 0;
		}
		return result;
	}
	
	private double augment(int x) {
		//(2*log(x +1)+1)
		return 2 * Math.log(x+1) +1;
	}
	
	public void earnXP(IAbility ability) {
		if(ability.getParent() == null) {
			//If player has not bended long enough, allow him to reset his spam history
			long now = System.currentTimeMillis();
			if (now - lastTime > SPAM_THRESHOLD) {
				spamHistory = 0;
			}
			
			//A player that travel far enough will suffer less from degression factor
			double distance = 1;
			if(this.lastLocation != null) {
				//If player is still in same world as previous location
				Player player = bPlayer.getPlayer();
				if(player!=null && player.getLocation().getWorld().getUID().equals(this.lastLocation.getWorld().getUID())) {
					distance = player.getLocation().distance(this.lastLocation);
					if(distance < 5) {
						//Between 0-5 blocks, player will take 100% of degression factor
						distance = 1;
					} else {
						//Between 5-infinite blocks, player will take (distance / 4)% degression factor 
						distance = distance / 4;
					}
				} else {
					distance = 1;
				}
			}
			
			//Minor spamHistory by distance, and calculate degression factor
			double degressFactor = this.degress(spamHistory /distance);
			
			//Calculate augment factor, only depends on level
			double augmentFactor = this.augment(level);
			
			//Progression could be resolved by 
			//  [(getExperienceNeeded / augment) / baseXP] 
			//    it will give number of ability to spam to get level (no degression assumed)
			double finalXP = ability.getBaseExperience() * augmentFactor;
			
			//Apply degression factor
			finalXP = finalXP * (1-degressFactor);
			
			//For safety mesure, if finalXP is negative, does not allow to give it (because player will be losing exp)
			if(finalXP > 0) {
				giveXP((int) finalXP);
			}
			
			//In any case, to be able to calculate NEXT degression factor, store player location and increment spamHistory
			this.lastLocation = this.bPlayer.getPlayer().getLocation();
			lastTime = now;
			//Be kind enough to not use no-rewarding ability as spam count
			if(ability.getBaseExperience() > 0) {
				spamHistory++;
			}
		}
	}
	
	public double getDegressFactor() {
		return this.degress(spamHistory);
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
	
	public long getLastTime() {
		return lastTime;
	}

	public double getSpamHistory() {
		return spamHistory;
	}
}
