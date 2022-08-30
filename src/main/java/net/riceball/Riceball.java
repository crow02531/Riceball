package net.riceball;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.InstanceFactory;
import net.riceball.internal.ClientHandler;
import net.riceball.internal.ServerHandler;
import net.wolftail.api.Introduction;
import net.wolftail.api.UniversalPlayerType;
import net.wolftail.api.lifecycle.PhysicalType;

@Mod(modid = Riceball.MOD_ID, version = Riceball.MOD_VERSION, acceptedMinecraftVersions = Riceball.MC_VERSION)
public final class Riceball {
	
	public static final String MOD_ID = "riceball";
	public static final String MOD_VERSION = "0.1.0";
	
	public static final String MC_VERSION = "1.12.2";
	
	public static final Riceball INSTANCE = new Riceball();
	
	public static final ResourceLocation TYPE_SHIP_ID = new ResourceLocation(MOD_ID, "ship");
	public static final UniversalPlayerType TYPE_SHIP = UniversalPlayerType.create(ServerHandler.INSTANCE,
			PhysicalType.INTEGRATED_CLIENT.is() ? ClientHandler.INSTANCE : null, new Introduction(
					"utype.riceball.ship.name", "utype.riceball.ship.desc", "riceball:textures/misc/utype/ship.json"))
			.setRegistryName(TYPE_SHIP_ID);
	
	private Riceball() {
	}
	
	@InstanceFactory
	private static Object fml_getModInstance() {
		// manually set display version
		Loader.instance().activeModContainer().getMetadata().version = MOD_VERSION;
		
		return INSTANCE;
	}
}
