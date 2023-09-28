package com.echo.network.session;

import io.netty.channel.Channel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 内部连接
 *
 * @author li-yuanwen
 * @date 2021/12/8
 */
public class ServerSession extends AbstractSession {

    /**
     * 依附于该Session的PlayerSession#identity
     **/
    private final Map<Long, PlayerSession> playerSessions;

    public ServerSession(long sessionId, Channel channel) {
        super(sessionId, channel);
        this.playerSessions = new HashMap<>(256);
    }

    @Override
    public PlayerSession bindIdentity(long identity) {
        PlayerSession playerSession = new PlayerSession(getSessionId(), channel);
        playerSession.bindIdentity(identity);
        this.playerSessions.put(identity, playerSession);
        return playerSession;
    }

    public void logout(long identity) {
        this.playerSessions.remove(identity);
    }

    public Set<Long> getIdentities() {
        return Collections.unmodifiableSet(playerSessions.keySet());
    }

    public PlayerSession getPlayerSession(long identity) {
        return playerSessions.get(identity);
    }

}
