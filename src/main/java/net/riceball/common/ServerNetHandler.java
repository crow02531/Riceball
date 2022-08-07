package net.riceball.common;

import com.lulan.shincolle.crafting.ShipCalc;
import com.lulan.shincolle.entity.BasicEntityShip;
import com.lulan.shincolle.reference.ID;
import com.lulan.shincolle.reference.Reference;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.wolftail.api.INetworkHandler;
import net.wolftail.api.PlayContext;
import net.wolftail.util.MoreByteBufs;
import net.wolftail.util.MoreServers;

class ServerNetHandler implements INetworkHandler {
	
	final PlayContext context;
	
	private BasicEntityShip ship;
	
	private float travel;
	private float turn;
	
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
	
	void applyEmotion(int type) {
		switch (type) {
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
	
	void applyTravel(float arg) {
		this.travel = arg;
	}
	
	void applyTurn(float arg) {
		this.turn = arg;
	}
	
	void applyJump() {
		this.ship.getJumpHelper().setJumping();
	}
	
	void say(String s) {
		MoreServers.serverInstance().getPlayerList().sendMessage(
				new TextComponentTranslation("chat.type.text", this.context.playName(), ForgeHooks.newChatWithLinks(s)),
				false);
	}
	
	@Override
	public void handle(ByteBuf buf) {
		PacketOperation.values()[MoreByteBufs.readVarInt(buf)].process(buf, this);
	}
	
	@Override
	public void tick() {
		BasicEntityShip e = this.ship;
		
		if (e.isDead) {
			this.joinWorld();
			
			e = this.ship;
		}
		
		e.tasks.taskEntries.clear();
		
		if (e.getGrudge() <= 0)
			e.addGrudge(1000);
		
		if (this.travel != 0.0F)
			e.travel(0, 0, this.travel);
		
		e.rotationYaw += this.turn;
		e.rotationYawHead = e.rotationYaw;
	}
}
