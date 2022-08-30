package net.riceball.internal;

import net.wolftail.api.IServerHandler;
import net.wolftail.api.PlayContext;

public final class ServerHandler implements IServerHandler {
	
	public static final ServerHandler INSTANCE = new ServerHandler();
	
	private ServerHandler() {
	}
	
	@Override
	public void handleEnter(PlayContext context) {
		ServerNetworkHandler sh = new ServerNetworkHandler(context);
		
		context.setHandler(sh);
		sh.joinWorld();
	}
	
	@Override
	public void handleLeave(PlayContext context) {
		((ServerNetworkHandler) context.getHandler()).leaveWorld();
	}
}
