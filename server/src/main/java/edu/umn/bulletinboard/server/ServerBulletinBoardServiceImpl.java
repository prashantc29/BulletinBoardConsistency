package edu.umn.bulletinboard.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umn.bulletinboard.common.content.Article;
import edu.umn.bulletinboard.common.exception.IllegalIPException;
import edu.umn.bulletinboard.common.server.ServerInfo;
import edu.umn.bulletinboard.common.util.ConsistencyType;
import edu.umn.bulletinboard.common.util.IndentArticles;
import edu.umn.bulletinboard.common.util.LogUtil;
import edu.umn.bulletinboard.server.exceptions.InvalidArticleException;
import edu.umn.bulletinboard.server.storage.MemStore;

public class ServerBulletinBoardServiceImpl {
		private static final String CLASS_NAME = ServerBulletinBoardServiceImpl.class.getSimpleName();
	    
		public  int post(String article) throws RemoteException {
			final String method = CLASS_NAME + ".post()";
			LogUtil.log(method, "Server:"+  Server.getServerId() + " " +  " Posting " + article );
	        return Server.getCoodinatorServerRMIObjectHandle().writeToCoordinatingServer(new Article(-1, article) ,ServerConfig.getConsistencyType()) ;
	    }

	    public  String read() throws RemoteException {
	    	final String method = CLASS_NAME + ".read()";
	    	LogUtil.log(method, "Server:"+  Server.getServerId() + " " +  "Reading articles");
	    	if(ServerConfig.getConsistencyType().equals(ConsistencyType.SEQUENTIAL)) {
	    		LogUtil.log(method, "Server:"+  Server.getServerId() + " " +  "Returing articles str for memstore : " + MemStore.getInstance().getAllArticles().toString());
	    		return IndentArticles.getArticlesStr(MemStore.getInstance().getAllArticles());
	    	}
	    	List<Article> readFromCoordinatingServer = Server.getCoodinatorServerRMIObjectHandle().readFromCoordinatingServer(ServerConfig.getConsistencyType());
	        return IndentArticles.getArticlesStr(IndentArticles.getArticleMap(readFromCoordinatingServer));
	    }

	    public  Article choose(int id) throws RemoteException {
	    	final String method = CLASS_NAME + ".read()";
	    	LogUtil.log(method, "Server:"+  Server.getServerId() + " " +   "Choose for id : " + id);
	    	if(ServerConfig.getConsistencyType().equals(ConsistencyType.SEQUENTIAL)) {
	    		if(MemStore.getInstance().getAllArticles().containsKey(id)){
	    			String message = "Article with Id:" + id + " not present.";
	    			LogUtil.log(method, message);
					throw new RemoteException(message);
	    		}
	    		return MemStore.getInstance().getArticle(id);
	    	}
	    	return Server.getCoodinatorServerRMIObjectHandle().chooseFromCoordinatingServer(id, ServerConfig.getConsistencyType());
	    }

	    public  int reply(int id, Article reply) throws RemoteException {
	    	final String method = CLASS_NAME + ".reply()";
	    	LogUtil.log(method, "Server:"+  Server.getServerId() + " " +  "Reply : " + reply + " for id : "+ id);
	        return Server.getCoodinatorServerRMIObjectHandle().replyToCoordinatingServer(id, reply, ServerConfig.getConsistencyType());
	    }
	    
	    public  void writeToServer(int articleId, String articleText) throws RemoteException {
	    	final String method = CLASS_NAME + ".writeToServer()";
	    	LogUtil.log(method, "Server:"+  Server.getServerId() + " " +  "Writing " + articleId + ":" + articleText + " to current server");
	    	try {
				MemStore.getInstance().addArticle(new Article(articleId, articleText));
			} catch (InvalidArticleException e) {
				throw new RemoteException("Invalid article", e);
			}
	    	LogUtil.log(method,"Server:"+  Server.getServerId() + " " +   "Updated memstore after write : " + MemStore.getInstance().getAllArticles().toString());
	    }

	    public  Article readFromServer(int articleId) throws RemoteException {
	    	final String method = CLASS_NAME + ".readFromServer()";
	    	LogUtil.log(method,"Server:"+  Server.getServerId() + " " +   "Reading " + articleId + " from server");
	    	if(!MemStore.getInstance().getAllArticles().containsKey(articleId)) {
	    		String message = "Article with Id:" + articleId + " not present.";
	    		LogUtil.log(method, message);
				throw new RemoteException(message);
	    	}
	        return MemStore.getInstance().getArticle(articleId);
	    }

	    public  int getLatestArticleId() throws RemoteException {
	        return MemStore.getInstance().getAllArticles().size();
	    }

	    public  void addServer(int serverId) throws RemoteException, IllegalIPException {
	    	// TODO
	    	Server.getServers().add(new ServerInfo(serverId, null,  1));
	    }

		public  List<Article> readFromServer() {
			final String method = CLASS_NAME + ".readFromServer()";
	    	LogUtil.log(method, "Server:"+  Server.getServerId() + " " +  "Reading all articles from server");
			return new ArrayList<Article>(MemStore.getInstance().getAllArticles().values());
		}

		public  void sync(List<Article> articles) {
			final String method = CLASS_NAME + ".sync()";
			LogUtil.log(method, "Server:"+  Server.getServerId() + " " +  "Syncing articles : " + articles);
			Map<Integer, Article> allArticles = MemStore.getInstance().getAllArticles();
			for (Article article : articles) {
				int id = article.getId();
				if(allArticles.containsKey(id)) {
					Article currentArticle = allArticles.get(id);
					List<Integer> replies = currentArticle.getReplies();
					List<Integer> newReplies = article.getReplies();
					List<Integer> list = new ArrayList<Integer>();
					list.addAll(newReplies);
					list.removeAll(replies);
					for (Integer integer : list) {
						currentArticle.getReplies().add(integer);
					}
				}else{
					try {
						MemStore.getInstance().addArticle(article);
					} catch (InvalidArticleException e) {
						LogUtil.log(method,"Server:"+  Server.getServerId() + " " +   "Invalid article: " + article + " in sync. Will try again in next sync.");
					}
				}
			}
			
		}

		public void replyToServer(int id, Article article) throws RemoteException {
			final String method = CLASS_NAME + ".replyToServer()";
			LogUtil.log(method, "Server:" + Server.getServerId()+ " Replying to article:"+id+" reply:"+article);
			if(!MemStore.getInstance().getAllArticles().containsKey(id))  {
				String message = "ArticleId: " + id + " not present cannot reply.";
				LogUtil.log(method, message);
				throw new RemoteException(message);
			}
			try {
				MemStore.getInstance().addArticle(article);
			} catch (InvalidArticleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MemStore.getInstance().getArticle(id).getReplies().add(article.getId());
			LogUtil.log(method, "Server:" + Server.getServerId()+ " Updated memstore:" + MemStore.getInstance().getAllArticles().toString());
		}

}
