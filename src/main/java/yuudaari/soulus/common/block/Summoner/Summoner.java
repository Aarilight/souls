package yuudaari.soulus.common.block.summoner;

import yuudaari.soulus.Soulus;
import yuudaari.soulus.common.CreativeTab;
import yuudaari.soulus.common.ModBlocks;
import yuudaari.soulus.common.ModItems;
import yuudaari.soulus.common.block.summoner.SummonerTileEntity.Upgrade;
import yuudaari.soulus.common.item.BloodCrystal;
import yuudaari.soulus.common.item.OrbMurky;
import yuudaari.soulus.common.item.Soulbook;
import yuudaari.soulus.common.item.SummonerUpgrade;
import yuudaari.soulus.common.util.Material;
import yuudaari.soulus.common.util.MobTarget;
import yuudaari.soulus.common.util.ModBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Soulus.MODID)
public class Summoner extends ModBlock {

	@ObjectHolder("soulus:blood_crystal")
	public static SummonerUpgrade CountUpgrade;
	@ObjectHolder("soulus:gear_oscillating")
	public static SummonerUpgrade DelayUpgrade;
	@ObjectHolder("soulus:orb_murky")
	public static SummonerUpgrade RangeUpgrade;

	private static class Upgrades {
		public int delayUpgrades;
		public int rangeUpgrades;
		public int countUpgrades;

		public Upgrades(int delay, int range, int count) {
			delayUpgrades = delay;
			rangeUpgrades = range;
			countUpgrades = count;
		}
	}

	private static Upgrades getSummonerUpgrades(NBTTagCompound summonerData) {
		NBTTagCompound upgradeData = summonerData.getCompoundTag("upgrades");
		return new Upgrades(upgradeData.getByte("delay"), upgradeData.getByte("range"), upgradeData.getByte("count"));
	}

	private static Upgrades getSummonerUpgrades(SummonerTileEntity te) {
		return new Upgrades(te.getUpgradeCount(Upgrade.DELAY), te.getUpgradeCount(Upgrade.RANGE),
				te.getUpgradeCount(Upgrade.COUNT));
	}

	private static String getSummonerEntity(NBTTagCompound summonerData) {
		return summonerData.getString("entity_type");
	}

	public Summoner() {
		super("summoner", new Material(MapColor.STONE).setTransparent());
		setHasItem();
		setHardness(5F);
		setResistance(30F);
		setHarvestLevel("pickaxe", 1);
		setSoundType(SoundType.METAL);
		disableStats();
	}

	public static class SummonerItemBlock extends ItemBlock {
		public SummonerItemBlock(Block b) {
			super(b);
			setRegistryName(Soulus.MODID + ":summoner");
		}

		@Override
		public String getUnlocalizedNameInefficiently(@Nonnull ItemStack stack) {
			return "tile." + Soulus.MODID + ":summoner." + MobTarget.getMobTarget(stack);
		}
	}

	private final SummonerItemBlock ITEM = new SummonerItemBlock(this);

	public ItemStack getItemStack(SummonerTileEntity te) {
		ItemStack itemStack = new ItemStack(ITEM);

		SummonerTileEntity tileEntity = (SummonerTileEntity) te.getWorld().getTileEntity(te.getPos());
		itemStack.setTagCompound(tileEntity.writeToNBT(new NBTTagCompound()));

		return itemStack;
	}

	@Override
	public ItemBlock getItemBlock() {
		return ITEM;
	}

	@SubscribeEvent
	public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		IBlockState blockState = event.getWorld().getBlockState(event.getPos());
		if (blockState.getBlock() instanceof Summoner && event.getItemStack().getItem() instanceof SummonerUpgrade) {
			event.setUseBlock(Result.ALLOW);
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public Class<? extends TileEntity> getTileEntityClass() {
		return SummonerTileEntity.class;
	}

	@Override
	@Nonnull
	@ParametersAreNonnullByDefault
	public TileEntity createTileEntity(World worldIn, IBlockState blockState) {
		return new SummonerTileEntity();
	}

	public static NBTTagCompound lastBrokenSummonerData;

	@SubscribeEvent
	public static void onSummonerBreak(BlockEvent.BreakEvent event) {
		if (event.getState().getBlock() == ModBlocks.SUMMONER) {
			SummonerTileEntity tileEntity = (SummonerTileEntity) event.getWorld().getTileEntity(event.getPos());
			if (tileEntity == null)
				throw new RuntimeException("Summoner has no tile entity");
			lastBrokenSummonerData = tileEntity.writeToNBT(new NBTTagCompound());
		}
	}

	@Nonnull
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
		List<ItemStack> drops = getDrops(lastBrokenSummonerData);

		drops.add(ModBlocks.SUMMONER_EMPTY.getItemStack());

		return drops;
	}

	private ItemStack getSoulbook(SummonerTileEntity te) {
		return getSoulbook(te.getMob());
	}

	private ItemStack getSoulbook(NBTTagCompound summonerData) {
		return getSoulbook(getSummonerEntity(summonerData));
	}

	private ItemStack getSoulbook(String entityName) {
		ItemStack soulbook = ModItems.SOULBOOK.getItemStack();
		MobTarget.setMobTarget(soulbook, entityName);
		Soulbook.setContainedEssence(soulbook, Soulus.getSoulInfo(entityName).quantity);

		return soulbook;
	}

	private List<ItemStack> getDrops(NBTTagCompound summonerData) {
		List<ItemStack> drops = new ArrayList<>();

		drops.add(getSoulbook(summonerData));

		Upgrades upgrades = getSummonerUpgrades(summonerData);
		drops.addAll(getUpgradeStacks(CountUpgrade, upgrades.countUpgrades));
		drops.addAll(getUpgradeStacks(DelayUpgrade, upgrades.delayUpgrades));
		drops.addAll(getUpgradeStacks(RangeUpgrade, upgrades.rangeUpgrades));

		return drops;
	}

	private List<ItemStack> getDrops(SummonerTileEntity te) {
		List<ItemStack> drops = new ArrayList<>();

		drops.add(getSoulbook(te));

		Upgrades upgrades = getSummonerUpgrades(te);
		drops.addAll(getUpgradeStacks(CountUpgrade, upgrades.countUpgrades));
		drops.addAll(getUpgradeStacks(DelayUpgrade, upgrades.delayUpgrades));
		drops.addAll(getUpgradeStacks(RangeUpgrade, upgrades.rangeUpgrades));

		return drops;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			SummonerTileEntity summoner = (SummonerTileEntity) world.getTileEntity(pos);

			ItemStack heldStack = player.inventory.mainInventory.get(player.inventory.currentItem);
			Item heldItem = heldStack.getItem();
			boolean sneaking = player.isSneaking();

			if (heldItem.equals(ModItems.SOULBOOK)) {
				returnItemToPlayer(world, getSoulbook(summoner), player);
				if (!world.isRemote) {
					String mob = MobTarget.getMobTarget(heldStack);
					summoner.setMob(mob);
					summoner.reset();
				}
				heldStack.shrink(1);

			} else if (heldItem.equals(CountUpgrade)) {
				if (BloodCrystal.getContainedBlood(heldStack) == ModItems.BLOOD_CRYSTAL.requiredBlood) {
					heldStack.shrink(summoner.addUpgradeStack(Upgrade.COUNT, sneaking ? heldStack.getCount() : 1));
				}

			} else if (heldItem.equals(DelayUpgrade)) {
				heldStack.shrink(summoner.addUpgradeStack(Upgrade.DELAY, sneaking ? heldStack.getCount() : 1));

			} else if (heldItem.equals(RangeUpgrade)) {
				if (OrbMurky.getContainedEssence(heldStack) == ModItems.ORB_MURKY.requiredEssence) {
					heldStack.shrink(summoner.addUpgradeStack(Upgrade.RANGE, sneaking ? heldStack.getCount() : 1));
				}

			} else if (heldItem.equals(Items.AIR)) {
				if (sneaking) {
					// empty hand and sneaking = return all items from summoner
					for (ItemStack drop : getDrops(summoner)) {
						returnItemToPlayer(world, drop, player);
					}

					world.setBlockState(pos, ModBlocks.SUMMONER_EMPTY.getDefaultState());

				} else {
					Upgrade lastInserted = summoner.getLastInserted();
					if (lastInserted == null) {
						returnItemToPlayer(world, getSoulbook(summoner.writeToNBT(new NBTTagCompound())), player);
						world.setBlockState(pos, ModBlocks.SUMMONER_EMPTY.getDefaultState());

					} else {
						int amtRemoved = summoner.removeUpgrade(lastInserted);
						if (amtRemoved > 0) {

							List<ItemStack> stacks = null;

							if (lastInserted == Upgrade.COUNT) {
								stacks = getUpgradeStacks(CountUpgrade, amtRemoved);
							} else if (lastInserted == Upgrade.DELAY) {
								stacks = getUpgradeStacks(DelayUpgrade, amtRemoved);
							} else if (lastInserted == Upgrade.RANGE) {
								stacks = getUpgradeStacks(RangeUpgrade, amtRemoved);
							}

							if (stacks != null) {
								for (int i = 0; i < stacks.size(); i++) {
									returnItemToPlayer(world, stacks.get(i), player);
								}
							}
						}
					}
				}
			}
		}

		return true;
	}

	private List<ItemStack> getUpgradeStacks(SummonerUpgrade upgrade, int count) {
		List<ItemStack> result = new ArrayList<>();
		do {
			ItemStack stack = upgrade.getFilledStack();
			stack.setCount(Math.min(stack.getMaxStackSize(), count));
			count -= stack.getMaxStackSize();
			result.add(stack);
		} while (count > 0);

		return result;
	}

	private void returnItemToPlayer(World world, ItemStack item, EntityPlayer player) {
		EntityItem dropItem = new EntityItem(world, player.posX, player.posY, player.posZ, item);
		dropItem.setNoPickupDelay();
		world.spawnEntity(dropItem);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
			EntityPlayer player) {
		// if they're requesting the block with nbt data, it needs to be this block, not an empty summoner
		return // player.isCreative() && GuiScreen.isCtrlKeyDown() ? getItemStack() : 
		ModBlocks.SUMMONER_EMPTY.getItemStack();
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		SummonerTileEntity te = (SummonerTileEntity) world.getTileEntity(pos);
		return te.getSignalStrength();
	}

	@Override
	public CreativeTab getCreativeTabToDisplayOn() {
		return null;
	}
}