package net.riceball.common;

import com.lulan.shincolle.crafting.ShipCalc;
import com.lulan.shincolle.entity.BasicEntityShip;
import com.lulan.shincolle.reference.ID;
import com.lulan.shincolle.reference.Reference;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.wolftail.api.INetworkHandler;
import net.wolftail.api.PlayContext;
import net.wolftail.util.MoreByteBufs;
import net.wolftail.util.MoreServers;

class ServerNetHandler implements INetworkHandler {
	
	final PlayContext context;
	
	private BasicEntityShip ship;
	
	ServerNetHandler(PlayContext c) {
		this.context = c;
	}
	
	void joinWorld() {
		WorldServer w = MoreServers.serverInstance().getWorld(0);
		BasicEntityShip e = this.ship = (BasicEntityShip) EntityList.createEntityByIDFromName(
				new ResourceLocation(Reference.MOD_ID, ShipCalc.getEntityToSpawnName(ID.ShipClass.CVAkagi)), w);
		
		e.setPosition(0, 4, 0);
		e.forceSpawn = true;
		e.setShipLevel(1, true);
		
		w.spawnEntity(e);
	}
	
	void leaveWorld() {
		this.ship.setDead();
		this.ship = null;
	}
	
	@Override
	public void handle(ByteBuf buf) {
		switch (MoreByteBufs.readVarInt(buf)) {
		case 1:
			this.ship.reactionStranger();
			break;
		case 2:
			this.ship.reactionDamaged();
			break;
		case 3:
			this.ship.reactionAttack();
			break;
		case 4:
			this.ship.reactionIdle();
			break;
		case 5:
			this.ship.reactionCommand();
			break;
		case 6:
			this.ship.reactionShock();
			break;
		default:
			this.ship.reactionNormal();
		}
	}
	
	@Override
	public void tick() {
		if (this.ship.isDead)
			this.joinWorld();
		
		if (this.ship.getGrudge() <= 0)
			this.ship.addGrudge(1000);
	}
}
