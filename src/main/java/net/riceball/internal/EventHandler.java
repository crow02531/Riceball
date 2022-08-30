package net.riceball.internal;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.riceball.Riceball;
import net.wolftail.api.UniversalPlayerType;

@EventBusSubscriber(modid = Riceball.MOD_ID)
public final class EventHandler {
	
	private EventHandler() {
	}
	
	@SubscribeEvent
	public static void event_registerUniplayerTypes(RegistryEvent.Register<UniversalPlayerType> e) {
		e.getRegistry().register(Riceball.TYPE_SHIP);
	}
}
