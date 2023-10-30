package com.echo.common.util;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.SensitiveProcessor;
import cn.hutool.dfa.WordTree;
import com.echo.common.util.sensitive.SensitiveDefaultProcessor;
import com.echo.common.util.sensitive.SensitiveHighlightProcessor;
import com.echo.common.util.sensitive.SensitiveWord;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感词工具
 *
 * @author: li-yuanwen
 */
@Slf4j
public class SensitiveWordUtils {


    private static WordTree sensitiveTree = new WordTree();

    private static ConcurrentHashMap<String, SensitiveWord> SENSITIVE_WORDS_MAP = new ConcurrentHashMap<>();

    /**
     * 初始化敏感词树
     *
     * @param isAsync        是否异步初始化
     * @param sensitiveWords 敏感词列表
     */
    static void init(final Collection<String> sensitiveWords, boolean isAsync) {
        if (isAsync) {
            ThreadUtil.execAsync(() -> {
                init(sensitiveWords);
                return true;
            });
        } else {
            init(sensitiveWords);
        }
    }

    /**
     * 初始化敏感词树
     *
     * @param sensitiveWords 敏感词列表
     */
    static void init(Collection<String> sensitiveWords) {
        sensitiveTree.clear();
        sensitiveTree.addWords(sensitiveWords);
    }

    public static void addSensitiveWord(SensitiveWord sw) {
        SENSITIVE_WORDS_MAP.put(sw.getWord(), sw);
        sensitiveTree.addWord(sw.getWord());
    }

    public static void removeSensitiveWord(String word) {
        SENSITIVE_WORDS_MAP.remove(word);
        sensitiveTree.clear();
        sensitiveTree.addWords(SENSITIVE_WORDS_MAP.keySet());
    }

    /**
     * 查找敏感词，返回找到的第一个敏感词
     *
     * @param text 文本
     * @return 敏感词
     */
    public static FoundWord getFoundFirstSensitive(String text) {
        return sensitiveTree.matchWord(text);
    }

    /**
     * 查找敏感词，返回找到的所有敏感词
     *
     * @param text 文本
     * @return 敏感词
     */
    public static List<FoundWord> getFoundAllSensitive(String text) {
        return sensitiveTree.matchAllWords(text);
    }

    /**
     * 查找敏感词，返回找到的所有敏感词<br>
     * 密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab]<br>
     * 贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     *
     * @param text           文本
     * @param isDensityMatch 是否使用密集匹配原则
     * @param isGreedMatch   是否使用贪婪匹配（最长匹配）原则
     * @return 敏感词
     */
    public static List<FoundWord> getFoundAllSensitive(String text, boolean isDensityMatch, boolean isGreedMatch) {
        return sensitiveTree.matchAllWords(text, -1, isDensityMatch, isGreedMatch);
    }

    /**
     * 处理过滤文本中的敏感词，默认替换成*
     *
     * @param text               文本
     * @param isGreedMatch       贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     * @param sensitiveProcessor 敏感词处理器，默认按匹配内容的字符数替换成*
     *                           SensitiveDefaultProcessor、SensitiveHighlightProcessor
     * @return 敏感词过滤处理后的文本
     */
    public static String sensitiveFilter(String text, boolean isGreedMatch, SensitiveProcessor sensitiveProcessor) {
        if (!StringUtils.hasLength(text)) {
            return text;
        }
//        TimeInterval timer = DateUtil.timer();
        //敏感词过滤场景下，不需要密集匹配
        List<FoundWord> foundWordList = getFoundAllSensitive(text, false, isGreedMatch);
        if (CollectionUtils.isEmpty(foundWordList)) {
            return text;
        }

        sensitiveProcessor = sensitiveProcessor == null ? new SensitiveProcessor() {
        } : sensitiveProcessor;

        Map<Integer, FoundWord> foundWordMap = new HashMap<>(foundWordList.size());
        foundWordList.forEach(foundWord -> foundWordMap.put(foundWord.getStartIndex(), foundWord));
        int length = text.length();
        StringBuilder textStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            FoundWord fw = foundWordMap.get(i);
            if (fw != null) {
                //只过滤[脱敏]类型的词汇, 非脱敏类型的敏感词直接跳过
                SensitiveWord dto = SENSITIVE_WORDS_MAP.get(fw.getWord());
                if (dto != null) {
                    if (sensitiveProcessor instanceof SensitiveHighlightProcessor) {
                        textStringBuilder.append(((SensitiveHighlightProcessor) sensitiveProcessor).process(fw, dto.getMode()));
                    }
                    if (sensitiveProcessor instanceof SensitiveDefaultProcessor) {
                        textStringBuilder.append(((SensitiveDefaultProcessor) sensitiveProcessor).process(fw));
                    }
                    i = fw.getEndIndex();
                }
            } else {
                textStringBuilder.append(text.charAt(i));
            }
        }
//        log.info("过滤敏感词, 耗时: " + timer.intervalMs() + "ms");
        return textStringBuilder.toString();
    }


    public static void main(String[] args) throws JsonProcessingException {
        List<String> list = new ArrayList<>(2);
        list.add("习近平");
        list.add("王八");
        list.add("操");

        init(list, false);
        List<FoundWord> words = getFoundAllSensitive("习￥近@平");
        if (!words.isEmpty()) {
            System.out.println(JsonUtils.toJson(words));
        }
        List<FoundWord> foundWordList = getFoundAllSensitive("王*八");
        if (!foundWordList.isEmpty()) {
            System.out.println(JsonUtils.toJson(foundWordList));
        }
        List<FoundWord> list2 = getFoundAllSensitive("曹操");
        if (!list2.isEmpty()) {
            System.out.println(JsonUtils.toJson(list2));
        }
    }

}
