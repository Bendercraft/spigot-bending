package net.avatarrealms.minecraft.bending.model;

import org.bukkit.Bukkit;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.data.BendingLevelData;

// http://rechneronline.de/function-graphs/
public class BendingLevel {
	// 120 seconds, but SPAM_THRESHOLD is in milliseconds
	public static int SPAM_THRESHOLD = 120 * 1000;
	
	private BendingPlayer bPlayer;
	private BendingType bendingType;
	private Integer level = 1;
	private Integer experience = 0;
	private long lastTime;
	private int spamHistory = 0;

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
	
	private double degress(int x) {
		// (e^(x*0.35) -1)/100
		double result = 0;
		
		result = (double) (Math.exp(x*0.35) - 1) / 100;
		
		if(result > 1) {
			result = 1;
		}
		return result;
	}
	
	private double augment(int x) {
		//(2*log(x +1)+1)
		return 2 * Math.log(x+1) +1;
	}
	
	public void earnXP(IAbility ability) {
		if(ability.getParent() == null) {
			long now = System.currentTimeMillis();
			if (now - lastTime > SPAM_THRESHOLD) {
				spamHistory = 0;
			}
			double degressFactor = this.degress(spamHistory);
			double augmentFactor = this.augment(level);
			//Progression could be resolved by 
			//  [(getExperienceNeeded / augment) / baseXP] 
			//    it will give number of ability to spam to get level (no degression assumed)
			double finalXP = ability.getBaseExperience() * augmentFactor * (1-degressFactor);
			String message = "Ability : "+ability.getClass().getSimpleName()+
					" with no parent, got degress factor : "+degressFactor+
					" and thus gave "+finalXP+
					" over "+ability.getBaseExperience()+"*"+augmentFactor;
			Bukkit.getLogger().info(message);
			bPlayer.getPlayer().sendMessage(message);
			
			if(finalXP > 0) {
				giveXP((int) finalXP);
			}
			
			lastTime = now;
			//Be kind enough to not use no-rewarding ability as spam count
			if(ability.getBaseExperience() > 0) {
				spamHistory++;
			}
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append("Got parent : ");
			builder.append(ability.getClass().getSimpleName());
			IAbility parent = ability.getParent();
			while(parent.getParent() != null) {
				builder.append(" -> ");
				builder.append(parent.getClass().getSimpleName());
			}
			Bukkit.getLogger().info(builder.toString());
		}
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
