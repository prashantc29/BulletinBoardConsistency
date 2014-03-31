package edu.umn.bulletinboard.server;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import edu.umn.bulletinboard.common.constants.RMIConstants;
import edu.umn.bulletinboard.common.content.Article;
import edu.umn.bulletinboard.common.rmi.BulletinBoardService;
import edu.umn.bulletinboard.common.server.ServerInfo;
import edu.umn.bulletinboard.common.util.ConsistencyType;
import edu.umn.bulletinboard.common.util.LogUtil;
import edu.umn.bulletinboard.server.coordinator.Coordinator;

public class CoordinatorSyncThread implements Callable<Boolean> {
	
	private final long WAIT_INTERVAL = 5 * 1000;

	@Override
	public Boolean call() throws Exception {
		
		if(ServerConfig.getConsistencyType().equals(ConsistencyType.SEQUENTIAL)) {
			return new Boolean(true);
		}
		
		int i = 0;
		while(true) {
			//TODO get list from coordinator
			LogUtil.log("CoordinatorSyncThread.call()", "Syncing ");
			Set<ServerInfo> servers = Coordinator.getInstance().getServers();
			List<Article> readFromCoordinatingServer = Coordinator.getInstance().readFromCoordinatingServer(ConsistencyType.QUORUM);
			for (ServerInfo serverInfo : servers) {
				BulletinBoardService server = (BulletinBoardService) Naming.lookup("rmi://" + serverInfo.getIp()
						+ ":" + serverInfo.getPort() + "/"
						+ RMIConstants.BB_SERVICE);
				server.sync(new ArrayList(readFromCoordinatingServer));
			}
			Thread.sleep(WAIT_INTERVAL);
			++i;
			if(i == Integer.MAX_VALUE) {
				break;
			}
		}
		return null;
	}

}
