package net.riceball.internal;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.wolftail.api.IClientHandler;
import net.wolftail.api.INetworkHandler;
import net.wolftail.api.PlayContext;
import net.wolftail.util.MoreByteBufs;
import net.wolftail.util.client.renderer.LogUnit;

public final class ClientHandler implements IClientHandler, INetworkHandler {
	
	public static final ClientHandler INSTANCE = new ClientHandler();
	
	private static final int INPUT_HEIGHT = 13;
	
	private PlayContext context;
	
	private LogUnit log;
	private GuiTextField input;
	
	private ClientHandler() {
	}
	
	@Override
	public void handleEnter(PlayContext c) {
		int w = calcWidth();
		int h = calcHeight();
		
		context = c;
		log = new LogUnit(w * 2, (h - INPUT_HEIGHT) * 2);
		input = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 1, h - INPUT_HEIGHT, w - 2,
				INPUT_HEIGHT - 1);
		input.setCanLoseFocus(false);
		
		c.setHandler(this);
		
		log.pPrint(TextFormatting.GREEN);
		log.pPrintln("===================================");
		log.pPrint(TextFormatting.GREEN);
		log.pPrintln("Enter 'ctrl' to enable keyboard control");
		log.pPrint(TextFormatting.GREEN);
		log.pPrintln("mode. Press 'esc' to disable. Press again");
		log.pPrint(TextFormatting.GREEN);
		log.pPrintln("to quit game.");
		log.pPrint(TextFormatting.GREEN);
		log.pPrintln("===================================");
		
		log.pPrint(c.playName());
		log.pPrintln(" joined the world. Happy playing!");
		
		this.leaveKctrlMode();
	}
	
	@Override
	public void handleFrame() {
		int w = calcWidth();
		int h = calcHeight();
		
		if (Display.wasResized()) {
			this.log.resize(w * 2, (h - INPUT_HEIGHT) * 2);
			
			this.input.y = h - INPUT_HEIGHT;
			this.input.width = w - 2;
		}
		
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.loadIdentity();
		GlStateManager.ortho(0, w, h, 0, -1, 1);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.loadIdentity();
		
		this.log.flush();
		this.log.render(new Vector3f(0, h - INPUT_HEIGHT, 0), new Vector3f(w, h - INPUT_HEIGHT, 0),
				new Vector3f(w, 0, 0), new Vector3f(0, 0, 0));
		
		this.input.drawTextBox();
	}
	
	@Override
	public void handleChat(ChatType type, ITextComponent text) {
		this.log.pPrint(TextFormatting.YELLOW);
		this.log.pPrint("Chat: ");
		this.log.pPrint(TextFormatting.WHITE);
		this.log.pPrintln(text.getFormattedText());
	}
	
	@Override
	public void handleLeave() {
		this.log.release();
		this.log = null;
		this.input = null;
		this.context = null;
		
		Mouse.setGrabbed(false);
		Keyboard.enableRepeatEvents(false);
	}
	
	@Override
	public void handle(ByteBuf buf) {
		
	}
	
	@Override
	public void tick() {
		this.input.updateCursorCounter();
		
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				int i = Mouse.getEventX() >> 1;
				int j = (Minecraft.getMinecraft().displayHeight - Mouse.getEventY()) >> 1;
				
				this.input.mouseClicked(i, j, Mouse.getEventButton());
			}
			
			int i = Mouse.getEventDWheel();
			
			if (i != 0)
				this.log.pScrollMov(-MathHelper.clamp(i, -1, 1) * 2);
		}
		
		while (Keyboard.next()) {
			int key = Keyboard.getEventKey();
			boolean pressed = Keyboard.getEventKeyState();
			
			if (!this.input.isFocused()) {
				switch (key) {
				case Keyboard.KEY_ESCAPE:
					if (pressed) {
						this.leaveKctrlMode();
						
						this.log.pPrintln("Keyboard control mode disable. Press 'esc' again to quit game.");
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
			} else {
				if (pressed) {
					switch (key) {
					case Keyboard.KEY_ESCAPE:
						this.context.disconnect();
						
						break;
					case Keyboard.KEY_RETURN:
						String command = this.input.getText();
						
						if (!command.isEmpty()) {
							this.log.pPrint(this.context.playName());
							this.log.pPrint(">");
							this.log.pPrintln(command);
							
							this.executeCommand(command);
							
							this.input.setText("");
						}
						
						break;
					}
				}
				
				char c = Keyboard.getEventCharacter();
				
				if (key == 0 && c >= ' ' || pressed)
					this.input.textboxKeyTyped(c, key);
			}
		}
	}
	
	private void executeCommand(String cmd) {
		if (cmd.equals("ctrl")) {
			this.enterKctrlMode();
			
			this.log.pPrintln("Keyboard control mode enable. Press 'esc' to disable.");
		} else if (cmd.startsWith("say ")) {
			String said = cmd.substring(4).trim();
			
			this.context.send(MoreByteBufs.writeUTF(said, this.alloc(PacketOperation.SAY)));
		} else if (cmd.equals("cls"))
			this.log.pClear();
		else
			this.log.pPrintln("Unknow command.");
	}
	
	private void enterKctrlMode() {
		this.input.setFocused(false);
		
		Keyboard.enableRepeatEvents(false);
		Mouse.setGrabbed(true);
	}
	
	private void leaveKctrlMode() {
		this.input.setFocused(true);
		
		Keyboard.enableRepeatEvents(true);
		Mouse.setGrabbed(false);
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
