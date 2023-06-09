package com.li.logging.appender;

import com.li.logging.message.RecordMessage;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.message.Message;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;

/**
 * 埋点日志记录器,实现输出到RecordMessage.getRecordName()为文件名的日志里,且带有滚动功能
 * @author li-yuanwen
 * @date 2022/3/29
 */
@Plugin(name = RecordRollingFileAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class RecordRollingFileAppender extends AbstractAppender {

    /** APPENDER_NAME **/
    public static final String PLUGIN_NAME = "RecordRollingFile";
    /** 文件名分隔符 **/
    private static final String SEPARATOR = "-";

    private final ConcurrentHashMap<String, RollingFileManager> rollingFileManagerHolder;

    /** 文件根路径 **/
    private final String rootPath;
    /** 缓冲区大小 **/
    private final int bufferSize;
    /** Lays out a LogEvent in different formats.  **/
    private final Layout<? extends Serializable> layout;



    // -------- FileManager所需参数 -----------

    /** 归档日志文件的文件名的模式, 模式的格式取决于所使用的RolloverPolicy **/
    private final String filePattern;
    /** 用于确定是否应该发生过渡的策略 **/
    private final TriggeringPolicy policy;
    /** 用于确定存档文件的名称和位置的策略 **/
    private final RolloverStrategy strategy;
    private final Configuration configuration;

    public RecordRollingFileAppender(String name, Filter filter, Layout<? extends Serializable> layout
            , boolean ignoreExceptions, Property[] properties
            , String rootPath, int bufferSize, String filePattern, TriggeringPolicy policy
            , RolloverStrategy strategy, Configuration configuration) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.rootPath = rootPath;
        this.bufferSize = bufferSize;
        this.layout = layout;
        this.filePattern = filePattern;
        this.policy = policy;
        this.strategy = strategy;
        this.configuration = configuration;
        this.rollingFileManagerHolder = new ConcurrentHashMap<>(64);
        PluginManager.addPackages(configuration.getPluginPackages());
    }

    @Override
    public void append(LogEvent event) {
        Message message = event.getMessage();
        if (!(message instanceof RecordMessage)) {
            return;
        }

        RecordMessage recordMessage = (RecordMessage) message;
        RollingFileManager manager = loadRollingFileManager(recordMessage.getRecordName());

        manager.checkRollover(event);
        try {
            tryAppend(event, manager);
        } catch (final AppenderLoggingException ex) {
            error("RecordLogAppender Unable to write to stream " + manager.getName() + " for appender " + getName(), event, ex);
            throw ex;
        }
    }

    private void tryAppend(final LogEvent event, final RollingFileManager manager) {
        if (Constants.ENABLE_DIRECT_ENCODERS) {
            directEncodeEvent(event, manager);
        } else {
            writeByteArrayToManager(event, manager);
        }
    }

    private void directEncodeEvent(final LogEvent event, final RollingFileManager manager) {
        getLayout().encode(event, manager);
        if (event.isEndOfBatch()) {
            manager.flush();
        }
    }

    private void writeByteArrayToManager(final LogEvent event, final RollingFileManager manager) {
        final byte[] bytes = getLayout().toByteArray(event);
        if (bytes != null && bytes.length > 0) {
            manager.writeBytes(bytes, 0, bytes.length);
        }
    }

    @Override
    public void start() {
        if (getLayout() == null) {
            LOGGER.error("RecordLogAppender : No layout set for the appender named [" + getName() + "].");
        }
        super.start();
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        setStopping();
        final boolean stopped = stop(timeout, timeUnit, true);
        setStopped();
        return stopped;
    }

    @Override
    protected boolean stop(long timeout, TimeUnit timeUnit, boolean changeLifeCycleState) {
        boolean stopped = super.stop(timeout, timeUnit, changeLifeCycleState);
        for (RollingFileManager manager : rollingFileManagerHolder.values()) {
            stopped &= manager.stop(timeout, timeUnit);
        }
        if (changeLifeCycleState) {
            setStopped();
        }
        LOGGER.debug("RecordLogAppender : Appender {} stopped with status {}", getName(), stopped);
        return stopped;
    }

    private RollingFileManager loadRollingFileManager(String recordName) {
        return rollingFileManagerHolder.computeIfAbsent(recordName, this::initializeManager);
    }

    private RollingFileManager initializeManager(String recordName) {
        RollingFileManager fileManager = RollingFileManager.getFileManager(buildFileName(recordName), buildPattern(recordName)
                , true, true, policy, strategy
                , null, layout, bufferSize, false
                , false, null, null, null, configuration);
        if (fileManager == null) {
            return null;
        }

        fileManager.initialize();

        return fileManager;
    }

    private String buildFileName(String recordName) {
        return rootPath + File.separator + recordName + File.separator + recordName + ".log";
    }

    private String buildPattern(String recordName) {
        // 重命名模式本身包含路径,则嵌入日志名称,否则依据根路径嵌入日志名
        int index = filePattern.lastIndexOf(File.separator);
        if (index < 0) {
            return rootPath + File.separator + recordName + File.separator + recordName + SEPARATOR + filePattern;
        }
        return filePattern.substring(0, index + 1) + recordName + File.separator + recordName + SEPARATOR + filePattern.substring(index + 1);
    }

    /**
     * Builder模式
     * @param <B>
     */
    public static class RecordRollingFileAppenderBuilder<B extends Builder<B>> extends Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<RecordRollingFileAppender> {

        @PluginBuilderAttribute
        private String fileRootPath;

        @PluginBuilderAttribute
        private boolean bufferedIo = true;

        @PluginBuilderAttribute
        private int bufferSize = Constants.ENCODER_BYTE_BUFFER_SIZE;

        @PluginBuilderAttribute
        @Required
        private String filePattern;

        @PluginBuilderAttribute
        private boolean append = true;

        @PluginElement("Policy")
        @Required
        private TriggeringPolicy policy;

        @PluginElement("Strategy")
        private RolloverStrategy strategy;

        @Override
        public RecordRollingFileAppender build() {
            final boolean isBufferedIo = isBufferedIo();
            final int bufferSize = getBufferSize();

            if (!isBufferedIo && bufferSize > 0) {
                LOGGER.warn("RecordLogAppender '{}': The bufferSize is set to {} but bufferedIO is not true", getName(), bufferSize);
            }

            if (filePattern == null) {
                LOGGER.error("RecordLogAppender '{}': No file name pattern provided.", getName());
                return null;
            }

            if (policy == null) {
                LOGGER.error("RecordLogAppender '{}': No TriggeringPolicy provided.", getName());
                return null;
            }

            if (strategy == null) {
                strategy = DefaultRolloverStrategy.newBuilder()
                        .withCompressionLevelStr(String.valueOf(Deflater.DEFAULT_COMPRESSION))
                        .withConfig(getConfiguration())
                        .build();
            }

            final Layout<? extends Serializable> layout = getOrCreateLayout();

            return new RecordRollingFileAppender(getName(), getFilter(), layout, !isBufferedIo, getPropertyArray()
                    , getFileRootPath(), bufferSize, filePattern, policy, strategy,  getConfiguration());
        }

        public String getFileRootPath() {
            return fileRootPath;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public boolean isBufferedIo() {
            return bufferedIo;
        }

        public B withFileRootPath(final String fileRootPath) {
            this.fileRootPath = fileRootPath;
            return asBuilder();
        }

        public B withBufferedIo(final boolean bufferedIo) {
            this.bufferedIo = bufferedIo;
            return asBuilder();
        }

        public B withBufferSize(final int bufferSize) {
            this.bufferSize = bufferSize;
            return asBuilder();
        }

        public B withAppend(final boolean append) {
            this.append = append;
            return asBuilder();
        }

        public String getFilePattern() {
            return filePattern;
        }

        public TriggeringPolicy getPolicy() {
            return policy;
        }

        public RolloverStrategy getStrategy() {
            return strategy;
        }

        public B withFilePattern(final String filePattern) {
            this.filePattern = filePattern;
            return asBuilder();
        }

        public B withPolicy(final TriggeringPolicy policy) {
            this.policy = policy;
            return asBuilder();
        }

        public B withStrategy(final RolloverStrategy strategy) {
            this.strategy = strategy;
            return asBuilder();
        }
    }

    @PluginBuilderFactory
    public static <B extends RecordRollingFileAppenderBuilder<B>> B newBuilder() {
        return new RecordRollingFileAppenderBuilder<B>().asBuilder();
    }


}
