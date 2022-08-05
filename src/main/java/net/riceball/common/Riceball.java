package net.riceball.common;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.InstanceFactory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.riceball.client.ClientHandler;
import net.wolftail.api.UniversalPlayerType;
import net.wolftail.api.lifecycle.PhysicalType;

@EventBusSubscriber
@Mod(modid = Riceball.MOD_ID, version = Riceball.MOD_VERSION, acceptedMinecraftVersions = Riceball.MC_VERSION)
public final class Riceball {
	
	public static final String MOD_ID = "riceball";
	public static final String MOD_VERSION = "0.1.0";
	
	public static final String MC_VERSION = "1.12.2";
	
	public static final Riceball INSTANCE = new Riceball();
	
	public static final ResourceLocation TYPE_SHIP_ID = new ResourceLocation(MOD_ID, "ship");
	public static final UniversalPlayerType TYPE_SHIP = UniversalPlayerType
			.create(ServerHandler.INSTANCE, PhysicalType.INTEGRATED_CLIENT.is() ? ClientHandler.INSTANCE : null)
			.setRegistryName(TYPE_SHIP_ID);
	
	private Riceball() {
	}
	
	@InstanceFactory
	private static Object fml_getModInstance() {
		// manually set display version
		Loader.instance().activeModContainer().getMetadata().version = MOD_VERSION;
		
		return INSTANCE;
	}
	
	@SubscribeEvent
	public static void event_registerUniplayerTypes(RegistryEvent.Register<UniversalPlayerType> e) {
		e.getRegistry().register(TYPE_SHIP);
	}
}
