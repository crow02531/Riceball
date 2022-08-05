package net.riceball.client;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.wolftail.api.IClientHandler;
import net.wolftail.api.INetworkHandler;
import net.wolftail.api.PlayContext;
import net.wolftail.util.MoreByteBufs;
import net.wolftail.util.client.renderer.CmdUnit;

public final class ClientHandler implements IClientHandler, INetworkHandler {
	
	public static final ClientHandler INSTANCE = new ClientHandler();
	
	private PlayContext context;
	private CmdUnit ui;
	
	private ClientHandler() {
	}
	
	@Override
	public void handleEnter(PlayContext context) {
		this.context = context;
		this.ui = new CmdUnit(400, 300);
		
		context.setHandler(this);
	}
	
	@Override
	public void handleFrame() {
		GlStateManager.clearColor(0, 0, 0, 1);
		GlStateManager.clearDepth(1);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.loadIdentity();
		GlStateManager.ortho(0, 1, 1, 0, -1, 1);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.loadIdentity();
		
		this.ui.flush();
		this.ui.render(new Vector3f(0, 1, 0), new Vector3f(1, 1, 0), new Vector3f(1, 0, 0), new Vector3f(0, 0, 0));
	}
	
	@Override
	public void handleLeave() {
		this.ui.release();
		this.ui = null;
		this.context = null;
	}
	
	@Override
	public void handle(ByteBuf buf) {
		
	}
	
	@Override
	public void tick() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				int key = Keyboard.getEventKey();
				
				switch (key) {
				case Keyboard.KEY_ESCAPE:
					this.context.disconnect();
					
					break;
				default:
					if (Keyboard.KEY_1 <= key && key <= Keyboard.KEY_0)
						this.context.send(MoreByteBufs.writeVarInt(key - 1, this.context.alloc().buffer()));
				}
			}
		}
	}
}
