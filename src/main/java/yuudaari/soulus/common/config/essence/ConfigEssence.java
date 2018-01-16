package yuudaari.soulus.common.config.essence;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import yuudaari.soulus.common.util.serializer.DefaultMapSerializer;
import yuudaari.soulus.common.util.serializer.Serializable;
import yuudaari.soulus.common.util.serializer.Serialized;

@Serializable
public class ConfigEssence {

	@Serialized public String essence;
	@Serialized @Nullable public ConfigColor colors;
	@Serialized public ConfigCreatureBone bones = new ConfigCreatureBone();
	@Serialized public int soulbookQuantity = 16;
	@Serialized(DoubleMapSerializer.class) @Nullable public Map<String, Double> spawns;
	@Serialized(LootMapSerializer.class) @Nullable public Map<String, ConfigCreatureLoot> loot;


	public ConfigEssence () {}

	public ConfigEssence (String essence, ConfigCreatureBone bones) {
		this.essence = essence;
		this.bones = bones;
	}

	public ConfigEssence addSpawnChance (String entity, double chance) {
		if (spawns == null) spawns = new HashMap<>();
		spawns.put(entity, chance);
		return this;
	}

	public ConfigEssence addLoot (int min, int max, double chance) {
		if (loot == null) loot = new HashMap<>();
		loot.put(this.essence, new ConfigCreatureLoot(min, max, chance));
		return this;
	}

	public ConfigEssence addLoot (String entity, int min, int max, double chance) {
		if (loot == null) loot = new HashMap<>();
		loot.put(entity, new ConfigCreatureLoot(min, max, chance));
		return this;
	}

	public static class DoubleMapSerializer extends DefaultMapSerializer<Double> {

		@Override
		public Class<Double> getValueClass () {
			return Double.class;
		}
	}

	public static class LootMapSerializer extends DefaultMapSerializer<ConfigCreatureLoot> {

		@Override
		public Class<ConfigCreatureLoot> getValueClass () {
			return ConfigCreatureLoot.class;
		}
	}
}
