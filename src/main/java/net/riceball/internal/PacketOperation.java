package net.riceball.internal;

import io.netty.buffer.ByteBuf;
import net.wolftail.util.MoreByteBufs;
import net.wolftail.util.MoreServers;

public enum PacketOperation {
	
	EMOTION {
		
		@Override
		void process(ByteBuf buf, ServerNetworkHandler handler) {
			int emotion = MoreByteBufs.readVarInt(buf);
			
			MoreServers.forceLogicServer(() -> {
				handler.applyEmotion(emotion);
			});
		}
	},
	
	TRAVEL {
		
		@Override
		void process(ByteBuf buf, ServerNetworkHandler handler) {
			float arg = buf.readFloat();
			
			MoreServers.forceLogicServer(() -> {
				handler.applyTravel(arg);
			});
		}
	},
	
	TURN {
		
		@Override
		void process(ByteBuf buf, ServerNetworkHandler handler) {
			float arg = buf.readFloat();
			
			MoreServers.forceLogicServer(() -> {
				handler.applyTurn(arg);
			});
		}
	},
	
	JUMP {
		
		@Override
		void process(ByteBuf buf, ServerNetworkHandler handler) {
			MoreServers.forceLogicServer(() -> {
				handler.applyJump();
			});
		}
	},
	
	SAY {
		
		@Override
		void process(ByteBuf buf, ServerNetworkHandler handler) {
			String s = MoreByteBufs.readUTF(buf);
			
			MoreServers.forceLogicServer(() -> {
				handler.say(s);
			});
		}
	};
	
	abstract void process(ByteBuf buf, ServerNetworkHandler handler);
}
