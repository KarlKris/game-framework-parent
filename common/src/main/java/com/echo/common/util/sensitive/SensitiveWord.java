package com.echo.common.util.sensitive;

/**
 * 敏感词
 *
 * @author: li-yuanwen
 */
public class SensitiveWord {

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 发布者姓名
     */
    private String userName;

    /**
     * 敏感字
     */
    private String word;

    /**
     * 分类：谩骂脏话、政治
     */
    private String type;

    /**
     * 影响方式
     */
    private SensitiveWordModeEnum mode;

    /**
     * 影响范围, 0全部 1动态 2用户 3好友聊天 4群聊天 5游戏 多个以,分隔
     */
    private String scope;

    /**
     * 替换符
     */
    private String repl;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SensitiveWordModeEnum getMode() {
        return mode;
    }

    public void setMode(SensitiveWordModeEnum mode) {
        this.mode = mode;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRepl() {
        return repl;
    }

    public void setRepl(String repl) {
        this.repl = repl;
    }
}
