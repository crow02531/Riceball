package net.riceball.internal;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.wolftail.api.IClientHandler;
import net.wolftail.api.INetworkHandler;
import net.wolftail.api.PlayContext;
import net.wolftail.util.MoreByteBufs;
import net.wolftail.util.client.renderer.CmdUnit;

public final class ClientHandler implements IClientHandler, INetworkHandler {
	
	public static final ClientHandler INSTANCE = new ClientHandler();
	
	private PlayContext context;
	private CmdUnit ui;
	
	private boolean kctrlEnable;
	
	private StringBuilder commandBuf;
	
	private ClientHandler() {
	}
	
	@Override
	public void handleEnter(PlayContext c) {
		context = c;
		ui = new CmdUnit(calcWidth(), calcHeight());
		commandBuf = new StringBuilder();
		kctrlEnable = false;
		
		c.setHandler(this);
		
		ui.pPrint(TextFormatting.GREEN);
		ui.pPrintln("===================================");
		ui.pPrint(TextFormatting.GREEN);
		ui.pPrintln("Enter 'ctrl' to enable keyboard control");
		ui.pPrint(TextFormatting.GREEN);
		ui.pPrintln("mode. Press 'esc' to disable. Press again");
		ui.pPrint(TextFormatting.GREEN);
		ui.pPrintln("to quit game.");
		ui.pPrint(TextFormatting.GREEN);
		ui.pPrintln("===================================");
		
		ui.pPrint(c.playName());
		ui.pPrintln(" joined the world. Happy playing!");
		
		printPrefix();
	}
	
	@Override
	public void handleFrame() {
		Mouse.setGrabbed(this.kctrlEnable);
		
		if (Display.wasResized())
			this.ui.resize(calcWidth(), calcHeight());
		
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
	public void handleChat(ChatType type, ITextComponent text) {
		
	}
	
	@Override
	public void handleLeave() {
		this.ui.release();
		this.ui = null;
		this.context = null;
		this.commandBuf = null;
	}
	
	@Override
	public void handle(ByteBuf buf) {
		
	}
	
	@Override
	public void tick() {
		while (Keyboard.next()) {
			int key = Keyboard.getEventKey();
			boolean pressed = Keyboard.getEventKeyState();
			
			if (this.kctrlEnable) {
				switch (key) {
				case Keyboard.KEY_ESCAPE:
					if (pressed) {
						this.kctrlEnable = false;
						
						this.ui.pPrintln("Keyboard control mode disable. Press 'esc' again to quit game.");
						this.printPrefix();
					}
					
					break;
				case Keyboard.KEY_W:
				case Keyboard.KEY_S:
					this.context.send(this.alloc(PacketOperation.TRAVEL)
							.writeFloat(pressed ? (key == Keyboard.KEY_W ? 0.25F : -0.125F) : 0));
					
					break;
				case Keyboard.KEY_A:
				case Keyboard.KEY_D:
					this.context.send(this.alloc(PacketOperation.TURN)
							.writeFloat(pressed ? (key == Keyboard.KEY_A ? -4.25F : 4.25F) : 0));
					
					break;
				case Keyboard.KEY_SPACE:
					if (pressed)
						this.context.send(this.alloc(PacketOperation.JUMP));
					
					break;
				default:
					if (Keyboard.KEY_1 <= key && key <= Keyboard.KEY_0 && pressed)
						this.context.send(MoreByteBufs.writeVarInt(key - 1, this.alloc(PacketOperation.EMOTION)));
				}
			} else if (pressed) {
				switch (key) {
				case Keyboard.KEY_ESCAPE:
					this.context.disconnect();
					
					break;
				case Keyboard.KEY_RETURN:
					if (this.commandBuf.length() != 0) {
						this.ui.pPrintln();
						
						this.executeCommand(this.commandBuf.toString());
						this.commandBuf.setLength(0);
					}
					
					break;
				default:
					char c = Keyboard.getEventCharacter();
					
					if (ChatAllowedCharacters.isAllowedCharacter(c)) {
						if (this.commandBuf.length() == 0)
							this.ui.pPrint(TextFormatting.YELLOW);
						
						this.commandBuf.append(c);
						this.ui.pPrint(c);
					}
				}
			}
		}
	}
	
	private void executeCommand(String cmd) {
		if (cmd.equals("ctrl")) {
			this.kctrlEnable = true;
			
			this.ui.pPrintln("Keyboard control mode enable. Press 'esc' to disable.");
			
			return;
		} else if (cmd.startsWith("say ")) {
			String said = cmd.substring(4).trim();
			
			this.context.send(MoreByteBufs.writeUTF(said, this.alloc(PacketOperation.SAY)));
			
			this.ui.pPrint("You said: ");
			this.ui.pPrintln(said);
		} else
			this.ui.pPrintln("Unknow command.");
		
		this.printPrefix();
	}
	
	private void printPrefix() {
		this.ui.pPrint(this.context.playName());
		this.ui.pPrint(">");
	}
	
	private ByteBuf alloc(PacketOperation op) {
		return MoreByteBufs.writeVarInt(op.ordinal(), this.context.alloc().buffer());
	}
	
	private static int calcHeight() {
		return Minecraft.getMinecraft().displayHeight >> 1;
	}
	
	private static int calcWidth() {
		return Minecraft.getMinecraft().displayWidth >> 1;
	}
}
