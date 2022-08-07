package net.riceball.common;

import io.netty.buffer.ByteBuf;
import net.wolftail.util.MoreByteBufs;
import net.wolftail.util.MoreServers;

public enum PacketOperation {
	
	EMOTION {
		
		@Override
		void process(ByteBuf buf, ServerNetHandler handler) {
			int emotion = MoreByteBufs.readVarInt(buf);
			
			MoreServers.serverInstance().callFromMainThread(() -> {
				handler.applyEmotion(emotion);
				
				return null;
			});
		}
	},
	
	TRAVEL {
		
		@Override
		void process(ByteBuf buf, ServerNetHandler handler) {
			float arg = buf.readFloat();
			
			MoreServers.serverInstance().callFromMainThread(() -> {
				handler.applyTravel(arg);
				
				return null;
			});
		}
	},
	
	TURN {
		
		@Override
		void process(ByteBuf buf, ServerNetHandler handler) {
			float arg = buf.readFloat();
			
			MoreServers.serverInstance().callFromMainThread(() -> {
				handler.applyTurn(arg);
				
				return null;
			});
		}
	},
	
	JUMP {
		
		@Override
		void process(ByteBuf buf, ServerNetHandler handler) {
			MoreServers.serverInstance().callFromMainThread(() -> {
				handler.applyJump();
				
				return null;
			});
		}
	},
	
	SAY {
		
		@Override
		void process(ByteBuf buf, ServerNetHandler handler) {
			String s = MoreByteBufs.readUTF(buf);
			
			MoreServers.serverInstance().callFromMainThread(() -> {
				handler.say(s);
				
				return null;
			});
		}
	};
	
	abstract void process(ByteBuf buf, ServerNetHandler handler);
}
