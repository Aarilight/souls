package yuudaari.soulus.common.item;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import yuudaari.soulus.common.registration.Registration;

public class BoneEnder extends Registration.Item implements IBone {

	public BoneEnder () {
		super("bone_ender");
		setHasGlint();
		setHasDescription();
	}

	@Override
	public double feedToWolf (final EntityWolf wolf, final ItemStack stack, final EntityPlayer player) {
		wolf.addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 500));
		return 0;
	}
}
