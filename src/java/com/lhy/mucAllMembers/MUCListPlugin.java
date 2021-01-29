package com.lhy.mucAllMembers;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.event.SessionEventDispatcher;
import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoom;
import org.jivesoftware.openfire.muc.spi.MultiUserChatServiceImpl;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import com.lhy.mucAllMembers.dao.MUCDao;;

/**
* https://blog.csdn.net/lhy24680/article/details/113353293
* 在用户登录时获取用户所在会议室的信息
*/
public class MUCListPlugin implements Plugin, SessionEventListener {

	private XMPPServer server;
	private MultiUserChatServiceImpl mucService;
	
	private IQRouter router;

	/**
	 * SessionEventListener回话监听
	 * 用户登录后会走这里
	 * @param session the authenticated session of a non anonymous user.
	 */
	@Override
	public void sessionCreated(Session session) {
//		JID userJid = session.getAddress();
		joinRooms(session);
	}

	@Override
	public void sessionDestroyed(Session session) {
	}

	@Override
	public void resourceBound(Session session) {
	}

	@Override
	public void anonymousSessionCreated(Session session) {
	}

	@Override
	public void anonymousSessionDestroyed(Session session) {
	}

	/**
	 * 插件初始化
	 * @param manager the plugin manager.
	 * @param pluginDirectory the directory where the plugin is located.
	 */
	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		server = XMPPServer.getInstance();
		SessionEventDispatcher.addListener(this);
		System.out.println("Join room plugin is running!");
		
		IQHandler myHandler = new MyIQHander();
		IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
		iqRouter.addHandler(myHandler);
		
		router = XMPPServer.getInstance().getIQRouter();
	}

	public void joinRooms(Session session) {
		JID userJid = session.getAddress();
		List<Map<String, String>> data = MUCDao.getMUCInfo(userJid.toBareJID());

		if (data == null || data.isEmpty()) {
			return;
		}
		Map<String, String> map = null;
		
		/**
		 * 构建iq的扩展包，用于发送用户所在房间的名称。
		 */
		Document document = DocumentHelper.createDocument();
		Element iqe = document.addElement("iq");
		iqe.addAttribute("type", "result");
		// 需要这个from，不然会崩溃
		iqe.addAttribute("from", XMPPServer.getInstance().getServerInfo().getXMPPDomain());
		iqe.addAttribute("to", userJid.toFullJID());
		iqe.addAttribute("id", "YANG");
		
		Namespace namespace = new Namespace("", "YANG");
		Element muc = iqe.addElement("muc");
		muc.add(namespace);
		
		
		for (int i = 0, len = data.size(); i < len; i++) {
			map = data.get(i);

			String serviceID = map.get("serviceID");
			mucService = (MultiUserChatServiceImpl) server
					.getMultiUserChatManager().getMultiUserChatService(
							Long.parseLong(serviceID));
			String roomName = map.get("name");
			LocalMUCRoom room = (LocalMUCRoom) mucService.getChatRoom(roomName);

			//增加room和account信息
			Element roome = muc.addElement("room");
			roome.setText(room.getJID().toBareJID());
			roome.addAttribute("account", userJid.toFullJID());
		}
		//最后发送出去
		IQ iq = new IQ(iqe);
		System.out.println("iq " + iq.toXML());
		router.route(iq);
	}

	@Override
	public void destroyPlugin() {
		SessionEventDispatcher.removeListener(this);
		server = null;
		mucService = null;
	}

	
}