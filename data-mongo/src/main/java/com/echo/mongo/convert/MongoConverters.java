package com.echo.mongo.convert;

import cn.hutool.core.lang.Assert;
import com.echo.common.convert.ReadingConverter;
import com.echo.common.convert.WritingConverter;
import com.echo.common.convert.converter.ConditionalConverter;
import com.echo.common.convert.converter.Converter;
import com.echo.common.convert.core.ConverterFactory;
import com.echo.common.convert.core.TypeDescriptor;
import com.echo.common.convert.exception.ConversionFailedException;
import com.echo.common.util.NumberUtils;
import com.echo.common.util.StringUtils;
import com.mongodb.MongoClientSettings;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * mongodb 相关的转换器容器
 * @author: li-yuanwen
 */
public class MongoConverters {


    /**
     * Private constructor to prevent instantiation.
     */
    private MongoConverters() {}

    /**
     * Returns the converters to be registered.
     *
     * @return
     * @since 1.9
     */
    static Collection<Object> getConvertersToRegister() {

        List<Object> converters = new ArrayList<>();

        converters.add(BigDecimalToStringConverter.INSTANCE);
        converters.add(BigDecimalToDecimal128Converter.INSTANCE);
        converters.add(StringToBigDecimalConverter.INSTANCE);
        converters.add(Decimal128ToBigDecimalConverter.INSTANCE);
        converters.add(BigIntegerToStringConverter.INSTANCE);
        converters.add(StringToBigIntegerConverter.INSTANCE);
        converters.add(URLToStringConverter.INSTANCE);
        converters.add(StringToURLConverter.INSTANCE);
        converters.add(DocumentToStringConverter.INSTANCE);
        converters.add(CurrencyToStringConverter.INSTANCE);
        converters.add(StringToCurrencyConverter.INSTANCE);
        converters.add(AtomicIntegerToIntegerConverter.INSTANCE);
        converters.add(AtomicLongToLongConverter.INSTANCE);
        converters.add(LongToAtomicLongConverter.INSTANCE);
        converters.add(IntegerToAtomicIntegerConverter.INSTANCE);
        converters.add(BinaryToByteArrayConverter.INSTANCE);
        converters.add(BsonTimestampToInstantConverter.INSTANCE);
        converters.add(BsonUndefinedToObjectConverter.INSTANCE);

        return converters;
    }

    @ReadingConverter
    enum BsonUndefinedToObjectConverter implements Converter<BsonUndefined, Object> {
        INSTANCE;

        @Override
        public Object convert(BsonUndefined source) {
            return null;
        }

    }

    /**
     * Simple singleton to convert {@link ObjectId}s to their {@link String} representation.
     *
     * @author Oliver Gierke
     */
    enum ObjectIdToStringConverter implements Converter<ObjectId, String> {
        INSTANCE;

        public String convert(ObjectId id) {
            return id.toString();
        }
    }

    /**
     * Simple singleton to convert {@link String}s to their {@link ObjectId} representation.
     *
     * @author Oliver Gierke
     */
    enum StringToObjectIdConverter implements Converter<String, ObjectId> {
        INSTANCE;

        public ObjectId convert(String source) {
            return StringUtils.hasLength(source) ? new ObjectId(source) : null;
        }
    }

    /**
     * Simple singleton to convert {@link ObjectId}s to their {@link java.math.BigInteger} representation.
     *
     * @author Oliver Gierke
     */
    enum ObjectIdToBigIntegerConverter implements Converter<ObjectId, BigInteger> {
        INSTANCE;

        public BigInteger convert(ObjectId source) {
            return new BigInteger(source.toString(), 16);
        }
    }

    /**
     * Simple singleton to convert {@link BigInteger}s to their {@link ObjectId} representation.
     *
     * @author Oliver Gierke
     */
    enum BigIntegerToObjectIdConverter implements Converter<BigInteger, ObjectId> {
        INSTANCE;

        public ObjectId convert(BigInteger source) {
            return new ObjectId(source.toString(16));
        }
    }

    enum BigDecimalToStringConverter implements Converter<BigDecimal, String> {
        INSTANCE;

        public String convert(BigDecimal source) {
            return source.toString();
        }
    }

    /**
     * @since 2.2
     */
    enum BigDecimalToDecimal128Converter implements Converter<BigDecimal, Decimal128> {
        INSTANCE;

        public Decimal128 convert(BigDecimal source) {
            return new Decimal128(source);
        }
    }

    enum StringToBigDecimalConverter implements Converter<String, BigDecimal> {
        INSTANCE;

        public BigDecimal convert(String source) {
            return StringUtils.hasLength(source) ? new BigDecimal(source) : null;
        }
    }

    /**
     * @since 2.2
     */
    enum Decimal128ToBigDecimalConverter implements Converter<Decimal128, BigDecimal> {
        INSTANCE;

        public BigDecimal convert(Decimal128 source) {
            return source.bigDecimalValue();
        }
    }

    enum BigIntegerToStringConverter implements Converter<BigInteger, String> {
        INSTANCE;

        public String convert(BigInteger source) {
            return source.toString();
        }
    }

    enum StringToBigIntegerConverter implements Converter<String, BigInteger> {
        INSTANCE;

        public BigInteger convert(String source) {
            return StringUtils.hasLength(source) ? new BigInteger(source) : null;
        }
    }

    enum URLToStringConverter implements Converter<URL, String> {
        INSTANCE;

        public String convert(URL source) {
            return source.toString();
        }
    }

    enum StringToURLConverter implements Converter<String, URL> {
        INSTANCE;

        private static final TypeDescriptor SOURCE = TypeDescriptor.valueOf(String.class);
        private static final TypeDescriptor TARGET = TypeDescriptor.valueOf(URL.class);

        public URL convert(String source) {

            try {
                return new URL(source);
            } catch (MalformedURLException e) {
                throw new ConversionFailedException(SOURCE, TARGET, source, e);
            }
        }
    }

    @ReadingConverter
    enum DocumentToStringConverter implements Converter<Document, String> {

        INSTANCE;

        private final Codec<Document> codec = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(new Codec<UUID>() {

            @Override
            public void encode(BsonWriter writer, UUID value, EncoderContext encoderContext) {
                writer.writeString(value.toString());
            }

            @Override
            public Class<UUID> getEncoderClass() {
                return UUID.class;
            }

            @Override
            public UUID decode(BsonReader reader, DecoderContext decoderContext) {
                throw new IllegalStateException("decode not supported");
            }
        }), MongoClientSettings.getDefaultCodecRegistry()).get(Document.class);

        @Override
        public String convert(Document source) {
            return source.toJson(codec);
        }
    }

    /**
     * {@link Converter} implementation converting {@link Currency} into its ISO 4217-2018 {@link String} representation.
     *
     * @author Christoph Strobl
     * @since 1.9
     */
    @WritingConverter
    enum CurrencyToStringConverter implements Converter<Currency, String> {

        INSTANCE;

        @Override
        public String convert(Currency source) {
            return source.getCurrencyCode();
        }
    }

    /**
     * {@link Converter} implementation converting ISO 4217-2018 {@link String} into {@link Currency}.
     *
     * @author Christoph Strobl
     * @since 1.9
     */
    @ReadingConverter
    enum StringToCurrencyConverter implements Converter<String, Currency> {

        INSTANCE;

        @Override
        public Currency convert(String source) {
            return StringUtils.hasLength(source) ? Currency.getInstance(source) : null;
        }
    }

    /**
     * {@link ConverterFactory} implementation using {@link NumberUtils} for number conversion and parsing. Additionally
     * deals with {@link AtomicInteger} and {@link AtomicLong} by calling {@code get()} before performing the actual
     * conversion.
     *
     * @author Christoph Strobl
     * @since 1.9
     */
    @WritingConverter
    enum NumberToNumberConverterFactory implements ConverterFactory<Number, Number>, ConditionalConverter {

        INSTANCE;

        @Override
        public <T extends Number> Converter<Number, T> getConverter(Class<T> targetType) {
            return new NumberToNumberConverter<T>(targetType);
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return !sourceType.equals(targetType);
        }

        private final static class NumberToNumberConverter<T extends Number> implements Converter<Number, T> {

            private final Class<T> targetType;

            /**
             * Creates a new {@link NumberToNumberConverter} for the given target type.
             *
             * @param targetType must not be {@literal null}.
             */
            public NumberToNumberConverter(Class<T> targetType) {

                Assert.notNull(targetType, "Target type must not be null");

                this.targetType = targetType;
            }

            @Override
            public T convert(Number source) {

                if (source instanceof AtomicInteger) {
                    AtomicInteger atomicInteger = (AtomicInteger) source;
                    return NumberUtils.convertNumberToTargetClass(atomicInteger.get(), this.targetType);
                }

                if (source instanceof AtomicLong) {
                    AtomicLong atomicLong = (AtomicLong) source;
                    return NumberUtils.convertNumberToTargetClass(atomicLong.get(), this.targetType);
                }

                return NumberUtils.convertNumberToTargetClass(source, this.targetType);
            }
        }
    }

    /**
     * {@link ConverterFactory} implementation converting {@link AtomicLong} into {@link Long}.
     *
     * @author Christoph Strobl
     * @since 1.10
     */
    @WritingConverter
    enum AtomicLongToLongConverter implements Converter<AtomicLong, Long> {
        INSTANCE;

        @Override
        public Long convert(AtomicLong source) {
            return NumberUtils.convertNumberToTargetClass(source, Long.class);
        }
    }

    /**
     * {@link ConverterFactory} implementation converting {@link AtomicInteger} into {@link Integer}.
     *
     * @author Christoph Strobl
     * @since 1.10
     */
    @WritingConverter
    enum AtomicIntegerToIntegerConverter implements Converter<AtomicInteger, Integer> {
        INSTANCE;

        @Override
        public Integer convert(AtomicInteger source) {
            return NumberUtils.convertNumberToTargetClass(source, Integer.class);
        }
    }

    /**
     * {@link ConverterFactory} implementation converting {@link Long} into {@link AtomicLong}.
     *
     * @author Christoph Strobl
     * @since 1.10
     */
    @ReadingConverter
    enum LongToAtomicLongConverter implements Converter<Long, AtomicLong> {
        INSTANCE;

        @Override
        public AtomicLong convert(Long source) {
            return new AtomicLong(source);
        }
    }

    /**
     * {@link ConverterFactory} implementation converting {@link Integer} into {@link AtomicInteger}.
     *
     * @author Christoph Strobl
     * @since 1.10
     */
    @ReadingConverter
    enum IntegerToAtomicIntegerConverter implements Converter<Integer, AtomicInteger> {
        INSTANCE;

        @Override
        public AtomicInteger convert(Integer source) {
            return new AtomicInteger(source);
        }
    }

    /**
     * {@link Converter} implementation converting {@link Binary} into {@code byte[]}.
     *
     * @author Christoph Strobl
     * @since 2.0.1
     */
    @ReadingConverter
    enum BinaryToByteArrayConverter implements Converter<Binary, byte[]> {

        INSTANCE;

        @Override
        public byte[] convert(Binary source) {
            return source.getData();
        }
    }

    /**
     * {@link Converter} implementation converting {@link BsonTimestamp} into {@link Instant}.
     *
     * @author Christoph Strobl
     * @since 2.1.2
     */
    @ReadingConverter
    enum BsonTimestampToInstantConverter implements Converter<BsonTimestamp, Instant> {

        INSTANCE;

        @Override
        public Instant convert(BsonTimestamp source) {
            return Instant.ofEpochSecond(source.getTime(), 0);
        }
    }

    @WritingConverter
    enum DateToLongConverter implements Converter<Date, Long> {
        INSTANCE;

        @Override
        public Long convert(Date source) {
            return source.getTime();
        }
    }

    @ReadingConverter
    enum LongToDateConverter implements Converter<Long, Date> {
        INSTANCE;

        @Override
        public Date convert(Long source) {
            return new Date(source);
        }
    }

    @ReadingConverter
    enum ObjectIdToDateConverter implements Converter<ObjectId, Date> {
        INSTANCE;

        @Override
        public Date convert(ObjectId source) {
            return new Date(source.getTimestamp());
        }
    }

    @ReadingConverter
    enum CodeToStringConverter implements Converter<Code, String> {
        INSTANCE;

        @Override
        public String convert(Code source) {
            return source.getCode();
        }
    }

}
