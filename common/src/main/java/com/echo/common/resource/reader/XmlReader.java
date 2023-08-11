package com.echo.common.resource.reader;

import com.echo.common.convert.core.ConversionService;
import com.echo.common.util.ObjectUtils;
import com.echo.common.util.ReflectionUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * xml文件读取器
 * @author li-yuanwen
 * @date 2022/3/23
 */
public class XmlReader implements ResourceReader {

    private final SAXReader reader = new SAXReader();

    /** 数据标签 **/
    private static final String ELEMENT_NAME = "item";

    private final ConversionService conversionService;

    public XmlReader(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public String getFileSuffix() {
        return "xml";
    }

    @Override
    public <E> List<E> read(InputStream in, Class<E> clz) {
        FieldParser fieldParser = new FieldParser(clz);

        List<E> results = new LinkedList<>();
        for (Element element : getElements(in)) {
            E instance = ObjectUtils.newInstance(clz);
            for (XmlFieldResolver fieldHolder : fieldParser.fieldHolders) {
                String fieldName = fieldHolder.getFieldName();
                Attribute attribute = element.attribute(fieldName);
                if (attribute == null) {
                    continue;
                }
                fieldHolder.inject(instance, attribute.getValue());
            }
            results.add(instance);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private List<Element> getElements(InputStream in) {
        try {
            Document document = reader.read(in);
            return (List<Element>) document.getRootElement().elements(ELEMENT_NAME);
        } catch (DocumentException e) {
            throw new RuntimeException("读取xml资源异常", e);
        }
    }

    private final class FieldParser {

        private final List<XmlFieldResolver> fieldHolders;

        FieldParser(Class<?> clz) {
            final List<XmlFieldResolver> fieldHolders = new LinkedList<>();
            for (Field field : ReflectionUtils.getFields(clz, field -> !Modifier.isStatic(field.getModifiers()))) {
                fieldHolders.add(new XmlFieldResolver(field));
            }
            this.fieldHolders = fieldHolders;
        }

    }

    private final class XmlFieldResolver extends AbstractFieldResolver {
        XmlFieldResolver(Field field) {
            super(field, conversionService);
        }
    }
}
