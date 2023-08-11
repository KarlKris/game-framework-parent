package com.echo.common.convert.support;

import com.echo.common.convert.core.ConversionService;
import com.echo.common.convert.core.ConverterRegistry;
import com.echo.common.convert.core.GenericConversionService;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * 默认使用的转换服务实现，继承自GenericConversionService
 * 在其基础行只做了一件事：构造时添加内建的默认转换器们。从而天然具备有了基本的类型转换能力
 * @author: li-yuanwen
 */
public class DefaultConversionService extends GenericConversionService {


    public DefaultConversionService() {
        addDefaultConverters(this);
    }

    public static void addDefaultConverters(ConverterRegistry converterRegistry) {
        addScalarConverters(converterRegistry);
        addCollectionConverters(converterRegistry);

        converterRegistry.addConverter(new StringToTimeZoneConverter());
        converterRegistry.addConverter(new ZoneIdToTimeZoneConverter());
        converterRegistry.addConverter(new ZonedDateTimeToCalendarConverter());
    }

    public static void addCollectionConverters(ConverterRegistry converterRegistry) {
        ConversionService conversionService = (ConversionService) converterRegistry;

        converterRegistry.addConverter(new ArrayToCollectionConverter(conversionService));
        converterRegistry.addConverter(new CollectionToArrayConverter(conversionService));

        converterRegistry.addConverter(new ArrayToArrayConverter(conversionService));
        converterRegistry.addConverter(new CollectionToCollectionConverter(conversionService));
        converterRegistry.addConverter(new MapToMapConverter(conversionService));

        converterRegistry.addConverter(new ArrayToStringConverter(conversionService));
        converterRegistry.addConverter(new StringToArrayConverter(conversionService));

        converterRegistry.addConverter(new CollectionToStringConverter(conversionService));
        converterRegistry.addConverter(new StringToCollectionConverter(conversionService));

        converterRegistry.addConverter(new StringToMapConverter(conversionService));
    }

    private static void addScalarConverters(ConverterRegistry converterRegistry) {
        converterRegistry.addConverterFactory(new NumberToNumberConverterFactory());

        converterRegistry.addConverterFactory(new StringToNumberConverterFactory());
        converterRegistry.addConverter(Number.class, String.class, new ObjectToStringConverter());

        converterRegistry.addConverter(new StringToCharacterConverter());
        converterRegistry.addConverter(Character.class, String.class, new ObjectToStringConverter());

        converterRegistry.addConverter(new NumberToCharacterConverter());
        converterRegistry.addConverterFactory(new CharacterToNumberFactory());

        converterRegistry.addConverter(new StringToBooleanConverter());
        converterRegistry.addConverter(Boolean.class, String.class, new ObjectToStringConverter());

        converterRegistry.addConverterFactory(new StringToEnumConverterFactory());
        converterRegistry.addConverter(new EnumToStringConverter((ConversionService) converterRegistry));

        converterRegistry.addConverterFactory(new IntegerToEnumConverterFactory());
        converterRegistry.addConverter(new EnumToIntegerConverter((ConversionService) converterRegistry));

        converterRegistry.addConverter(new StringToLocaleConverter());
        converterRegistry.addConverter(Locale.class, String.class, new ObjectToStringConverter());

        converterRegistry.addConverter(new StringToCharsetConverter());
        converterRegistry.addConverter(Charset.class, String.class, new ObjectToStringConverter());
    }
}
