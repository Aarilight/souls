package yuudaari.soulus.common.config.item;

import yuudaari.soulus.Soulus;
import yuudaari.soulus.common.config.ConfigFile;
import yuudaari.soulus.common.util.serializer.Serializable;
import yuudaari.soulus.common.util.serializer.Serialized;

@ConfigFile(file = "item/misc", id = Soulus.MODID, path = "sledgehammer")
@Serializable
public class ConfigSledgehammer {

	@Serialized public int durability = 256;
	@Serialized public int durabilityEndersteel = 2048;
	@Serialized public int durabilityEndersteelDark = 4096;
	@Serialized public int durabilityNiobium = -1;
}
