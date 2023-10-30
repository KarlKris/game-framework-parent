package com.echo.network.session;

import com.echo.network.protocol.ChannelAttributeKeys;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * session 容器
 *
 * @author: li-yuanwen
 */
@Slf4j
public class SessionContext {

    /**
     * session id generator
     **/
    private final AtomicLong sessionIdGenerator = new AtomicLong(0);

    /**
     * Identity2Session
     **/
    private final ConcurrentHashMap<Long, PlayerSession> identities = new ConcurrentHashMap<>();

    /**
     * 为Channel注册PlayerSession
     **/
    public PlayerSession registerPlayerSession(Channel channel) {
        long nextId = this.sessionIdGenerator.incrementAndGet();
        PlayerSession playerSession = new PlayerSession(nextId, channel);
        // channel绑定属性
        channel.attr(ChannelAttributeKeys.SESSION).set(playerSession);
        return playerSession;
    }

    /**
     * 为Channel注册PlayerSession
     **/
    public ServerSession registerServerSession(Channel channel) {
        long nextId = this.sessionIdGenerator.incrementAndGet();
        ServerSession serverSession = new ServerSession(nextId, channel);
        // channel绑定属性
        channel.attr(ChannelAttributeKeys.SESSION).set(serverSession);
        return serverSession;
    }


    /**
     * 删除为Channel注册的Session
     **/
    public PlayerSession removePlayerSession(Channel channel) {
        PlayerSession session = (PlayerSession) channel.attr(ChannelAttributeKeys.SESSION).get();
        if (session == null) {
            return null;
        }

        if (session.isIdentity()) {
            this.identities.remove(session.getIdentity());
        }

        return session;
    }

    /**
     * 删除为Channel注册的Session
     **/
    public ServerSession removeServerSession(Channel channel) {
        ServerSession session = (ServerSession) channel.attr(ChannelAttributeKeys.SESSION).get();
        if (session == null) {
            return null;
        }

        for (long identity : session.getIdentities()) {
            identities.remove(identity);
        }

        return session;
    }

    /**
     * 是否在线
     **/
    public boolean isOnline(Long identity) {
        PlayerSession playerSession = this.identities.get(identity);
        return playerSession != null
                && playerSession.getChannel() != null
                && playerSession.getChannel().isActive();
    }

    /**
     * 绑定身份
     *
     * @param session  连接Session
     * @param identity 身份标识
     * @return 旧Session or null
     */
    public ISession bindIdentity(ISession session, long identity) {

        if (log.isDebugEnabled()) {
            log.debug("session[{}]绑定某个身份[{}]", session.getSessionId(), identity);
        }

        PlayerSession playerSession = session.bindIdentity(identity);

        return this.identities.put(identity, playerSession);

    }

    /**
     * 断开连接
     **/
    public void kickOut(ISession session) {
        session.close();
    }

    /**
     * 断开连接
     **/
    public void kickOut(long identity) {
        ISession session = this.getIdentitySession(identity);
        if (session != null) {
            session.close();
        }
    }

    /**
     * 登出
     **/
    public void logout(long identity) {
        identities.remove(identity);
    }

    /**
     * 获取指定Session
     **/
    public ISession getIdentitySession(long identity) {
        return this.identities.get(identity);
    }

    /**
     * 获取已绑定身份的标识集
     **/
    public Collection<Long> getOnlineIdentities() {
        return Collections.unmodifiableCollection(this.identities.keySet());
    }
}
