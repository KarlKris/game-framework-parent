package com.echo.common.util.sensitive;

import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.SensitiveProcessor;

/**
 * 自定义敏感词高亮处理器
 *
 * @author: li-yuanwen
 */
public class SensitiveHighlightProcessor implements SensitiveProcessor {

    private static final String SHIELD_START = "<shield>";
    private static final String SHIELD_END = "</shield>";
    private static final String DST_START = "<dst>";
    private static final String DST_END = "</dst>";
    private static final String WARN_START = "<warn>";
    private static final String WARN_END = "</warn>";

    @Override
    public String process(FoundWord foundWord) {
        String word = foundWord.getFoundWord();
        StringBuilder sb = new StringBuilder();
        sb.append(WARN_START).append(word).append(WARN_END);
        return sb.toString();
    }

    public String process(FoundWord foundWord, SensitiveWordModeEnum mode) {
        String word = foundWord.getFoundWord();
        StringBuilder sb = new StringBuilder();
        if (SensitiveWordModeEnum.SHIELD.equals(mode)) {
            sb.append(SHIELD_START).append(word).append(SHIELD_END);
        } else if (SensitiveWordModeEnum.DST.equals(mode)) {
            sb.append(DST_START).append(word).append(DST_END);
        } else if (SensitiveWordModeEnum.WARN.equals(mode)) {
            sb.append(WARN_START).append(word).append(WARN_END);
        }
        return sb.toString();
    }
}
