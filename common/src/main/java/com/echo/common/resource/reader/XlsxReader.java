package com.echo.common.resource.reader;

import com.echo.common.util.ObjectUtils;
import com.echo.common.util.StringUtils;
import com.echo.common.conversion.ConversionService;
import com.echo.common.resource.resolver.Resolver;
import com.echo.common.resource.resolver.ResolverFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.helpers.MessageFormatter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * excel xlsx文件读取器
 * @author li-yuanwen
 * @date 2022/3/23
 */
@Slf4j
public class XlsxReader extends DefaultHandler implements ResourceReader {

    /** 服务端属性名称字段标识 **/
    private static final String ROW_SERVER = "SERVER";
    /** 忽略行标识 **/
    private static final String ROW_IGNORE = "NO";
    /** 结束行标识 **/
    private static final String ROW_END = "END";
    /** 上行结束标识 **/
    private static final String ROW_END_BEFORE = "END_BEFORE";

    // ------------------- 标签  -----------------------

    /** row标签 **/
    private static final String ROW = "row";
    /** cell标签 **/
    private static final String CELL = "c";
    /** cell子标签 r **/
    private static final String R = "r";
    /** cell子标签 t 单元格数据类型 **/
    private static final String CELL_DATA_TYPE = "t";
    /** cell子标签 s 单元格数据样式 **/
    private static final String CELL_DATA_STYLE = "s";
    /** 值标签 **/
    private static final String VALUE = "v";

    // ------------------- 日期格式 ---------------------

    private static final String M_D_Y = "m/d/yyyy";
    private static final String Y_MM_DD = "yyyy/mm/dd";
    private static final String Y_M_D = "yyyy/m/d";
    private static final String Y_M_D_H_M_S = "yyyy-MM-dd hh:mm:ss";


    private final ConversionService conversionService;

    public XlsxReader(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public String getFileSuffix() {
        return "xlsx";
    }

    @Override
    public <E> List<E> read(InputStream in, Class<E> clz) {
        try {
            OPCPackage xlsxPackage = OPCPackage.open(in);
            XSSFReader xssfReader = new XSSFReader(xlsxPackage);
            StylesTable styles = xssfReader.getStylesTable();
            SharedStringsTable sharedStringsTable = xssfReader.getSharedStringsTable();
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            // 创建返回数据集
            List<E> result = new LinkedList<E>();
            while (iter.hasNext()) {
                InputStream inputStream = iter.next();
                String sheetName = iter.getSheetName();
                List<E> list = readSheet(clz, styles, sharedStringsTable, inputStream, sheetName);
                result.addAll(list);
                inputStream.close();
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("读取excel资源异常", e);
        }
    }

    // ------------------ 私有方法 ----------------------------------

    private <E> List<E> readSheet(Class<E> clz,  StylesTable styles, SharedStringsTable sst, InputStream sheetInputStream,
                                  String sheetName) throws IOException, SAXException, ParserConfigurationException {
        InputSource inputSource = new InputSource(sheetInputStream);
        SheetHandler<E> sheetHandler = new SheetHandler<>(clz, sheetName, styles, sst);
        XMLReader sheetParser = fetchSheetParser(sheetHandler);
        sheetParser.parse(inputSource);
        return sheetHandler.results;
    }


    /**
     * SAXParser
     * @param handler xlsx文件解析成xml后的解析器
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private XMLReader fetchSheetParser(DefaultHandler handler) throws ParserConfigurationException, SAXException {
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        sheetParser.setContentHandler(handler);
        return sheetParser;
    }


    // ------------------ DefaultHandler ---------------------------

    /** SAXParser 会将excel文件解析成xml格式,所以需要自定义一个解析xml的,也就是继承DefaultHandler **/
    private final class SheetHandler<E> extends DefaultHandler {

        /** 解析类型 **/
        private final Class<E> clz;
        /** 分页名称 **/
        private final String sheetName;
        /** 样式 **/
        private final StylesTable styles;
        /** sst **/
        private final SharedStringsTable sst;
        /** 解析结果 **/
        private final List<E> results = new LinkedList<>();
        /** 属性信息 **/
        private final List<XlsxFieldResolver> fieldHolders = new LinkedList<>();
        /** 同一行列信息 **/
        private final List<String> colValues = new ArrayList<>();
        /** 单个单元格内容 **/
        private final StringBuilder cellValue = new StringBuilder();
        /** 当前行 **/
        private int curRow = 0;
        /** 上一次的列数 **/
        private int lastColumn = -1;
        /** 当前列数 **/
        private int curColumn = -1;
        /** 下一个数据类型 **/
        private XSSFDataType nextDataType;
        /** 日期类型formatIndex **/
        private int formatIndex;
        /** 具体日期类型 **/
        private String formatString;
        /** formatter **/
        private final DataFormatter formatter = new DataFormatter();
        /** 当读取到v标签时，开始读取单元格内容 **/
        private boolean vIsOpen;
        /** 读取完成标识 **/
        private boolean done;
        /** 主键解析器 **/
        private final Resolver identifier;

        public SheetHandler(Class<E> clz, String sheetName, StylesTable styles, SharedStringsTable sst) {
            this.clz = clz;
            this.sheetName = sheetName;
            this.styles = styles;
            this.sst = sst;
            this.identifier = ResolverFactory.createIdResolver(clz);
        }

        /** 读到一个xml开始标签时的回调处理 **/
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // 值标签
            if (VALUE.equals(qName)) {
                vIsOpen = true;
                // 清空
                cellValue.setLength(0);
            } else if (CELL.equals(qName)) {
                // 计算当前列数
                curColumn = calculateColumnIndex(attributes.getValue(R));
                // 设置单元格数据类型
                setNextDataType(attributes);
            }
        }

        /** 标签结束的回调处理 **/
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // 值标签
            if (VALUE.equals(qName)) {
                // 获取值
                String value = getDataValue(cellValue.toString());
                addCellValue(value);
            } else if (ROW.equals(qName)) {
                // 一行的结尾
                try {
                    parseRow();
                } finally {
                    lastColumn = -1;
                    colValues.clear();
                    curRow++;
                }
            }
        }

        /** 处理xml中的v标签中间的内容的回调 **/
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (!vIsOpen) {
                return;
            }
            cellValue.append(ch, start, length);
        }

        /**
         * 求单元格处于第几列
         * @param r 类似 A15
         * @return 单元格处于第几列
         */
        private int calculateColumnIndex(String r) {
            int firstDigit = -1;
            for (int c = 0; c < r.length(); ++c) {
                if (Character.isDigit(r.charAt(c))) {
                    firstDigit = c;
                    break;
                }
            }
            String letter = r.substring(0, firstDigit);
            int columnIndex = -1;
            for (int i = 0; i < letter.length(); i++) {
                int c = letter.charAt(i);
                columnIndex = (columnIndex + 1) * 26 + c - 'A';
            }
            return columnIndex;
        }

        private void addCellValue(String value) {
            // 处理单元格间的空单元格
            for (int i = lastColumn + 1; i < curColumn; i++) {
                colValues.add(null);
            }

            if (lastColumn == -1) {
                lastColumn = 0;
            }

            if (curColumn > -1) {
                lastColumn = curColumn;
                colValues.add(value);
            }
        }

        /**
         * 处理数据类型
         * @param attributes /
         */
        private void setNextDataType(Attributes attributes) {
            String cellDataType = attributes.getValue(CELL_DATA_TYPE);
            String cellDataStyle = attributes.getValue(CELL_DATA_STYLE);

            // 默认数字
            nextDataType = XSSFDataType.Number;

            if (XSSFDataType.Bool.value.equals(cellDataType)) {
                nextDataType = XSSFDataType.Bool;
            } else if (XSSFDataType.Error.value.equals(cellDataType)) {
                nextDataType = XSSFDataType.Error;
            } else if (XSSFDataType.InlineStr.value.equals(cellDataType)) {
                nextDataType = XSSFDataType.InlineStr;
            } else if (XSSFDataType.SharedStringsTable.value.equals(cellDataType)) {
                nextDataType = XSSFDataType.SharedStringsTable;
            } else if (XSSFDataType.Formula.value.equals(cellDataType)) {
                nextDataType = XSSFDataType.Formula;
            }

            // 处理日期
            if (cellDataStyle != null) {
                int styleIndex = Integer.parseInt(cellDataStyle);
                XSSFCellStyle style = styles.getStyleAt(styleIndex);
                formatIndex = style.getDataFormat();
                formatString = style.getDataFormatString();
                if (formatString == null) {
                    nextDataType = XSSFDataType.Null;
                    formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
                }

                if (formatString.contains(M_D_Y)
                        || formatString.contains(Y_M_D)
                        || formatString.contains(Y_MM_DD)) {
                    nextDataType = XSSFDataType.Date;
                    formatString = Y_M_D_H_M_S;
                }

            }
        }

        /**
         * 对解析出来的数据进行类型处理
         * @param content 单元格值
         *                Bool的为0或1， Error的为内容值，Str的为内容值，InlineStr的为索引值需转换为内容值，
         *                SharedStringsTable的为索引值需转换为内容值， Number为内容值，Date为内容值
         * @return /
         */
        private String getDataValue(String content) {
            String value;
            switch (nextDataType) {
                case Bool: {
                    // 布尔值
                    char first = content.charAt(0);
                    value = first == '0' ? "FALSE" : "TRUE";
                    break;
                }
                case Error: {
                    // 错误
                    value = "\"ERROR:" + content + '"';
                    break;
                }
                case Formula: {
                    // 公式
                    value = '"' + content +'"';
                    break;
                }
                case InlineStr: {
                    value = new XSSFRichTextString(content).toString();
                    break;
                }
                case  SharedStringsTable: {
                    // 字符串
                    int index = Integer.parseInt(content);
                    value = sst.getItemAt(index).toString();
                    break;
                }
                case Number: {
                    // 数字
                    value = content;
                    break;
                }
                case Date: {
                    value = formatter.formatRawCellContents(Double.parseDouble(content), formatIndex, formatString)
                            .replace("T", " ");
                    break;
                }
                default: {
                    value = " ";
                    break;
                }
            }

            return value;
        }

        private void parseRow() {
            if (colValues.isEmpty()) {
                return;
            }
            if (done) {
                return;
            }
            // 第一个单元格是标识
            String tag = colValues.get(0);
            // 第一个空格有内容才继续读,否则跳过当前页
            if (curRow == 0 && !StringUtils.hasLength(tag)) {
                done = true;
                return;
            }
            // 服务端属性行
            if (ROW_SERVER.equalsIgnoreCase(tag)) {
                buildFieldHolders();
                return;
            } else if (ROW_IGNORE.equalsIgnoreCase(tag)) {
                // 忽略行
                return;
            } else if (ROW_END_BEFORE.equalsIgnoreCase(tag)) {
                // 上行结束标识
                done = true;
                return;
            }

            // 判断属性列表是否已初始化
            if (fieldHolders.isEmpty()) {
                return;
            }
            // 开始解析
            parseRow0();

            // 结束行标识
            if (ROW_END.equalsIgnoreCase(tag)) {
                done = true;
            }

        }

        private void buildFieldHolders() {
            List<XlsxFieldResolver> list = new ArrayList<>();
            for (int i = 1; i < colValues.size(); i++) {
                String fieldName = colValues.get(i);
                if (!StringUtils.hasLength(fieldName)) {
                    continue;
                }
                try {
                    Field field = clz.getDeclaredField(fieldName);
                    list.add(new XlsxFieldResolver(field, i));
                } catch (NoSuchFieldException e) {
                    log.warn("资源类[{}]分页[{}]的声明属性[{}]不存在", clz, sheetName, fieldName);
                } catch (Exception e) {
                    String message = MessageFormatter.arrayFormat("资源类[{}]分页[{}]的声明属性[{}]无法获取",
                            new Object[] { clz, sheetName, fieldName }).getMessage();
                    throw new IllegalStateException(message, e);
                }
            }
            if (!list.isEmpty()) {
                fieldHolders.clear();
                fieldHolders.addAll(list);
            }
        }

        private void parseRow0() {
            E instance = ObjectUtils.newInstance(clz);
            int indexSize = colValues.size() - 1;
            for (XlsxFieldResolver fieldHolder : fieldHolders) {
                int index = fieldHolder.index;
                if (index > indexSize) {
                    continue;
                }
                String content = colValues.get(index);
                if (!StringUtils.hasLength(content)) {
                    continue;
                }
                fieldHolder.inject(instance, content);
            }

            if (identifier.resolve(instance) == null) {
                String message = MessageFormatter.arrayFormat("数值表[{}]的[{}]分页第[{}]行的配置内容错误 - 主键列NULL"
                        , new Object[]{clz.getSimpleName(), sheetName, curRow}).getMessage();
                throw new IllegalArgumentException(message);
            }

            results.add(instance);
        }
    }

    /** 数据类型 **/
    private enum XSSFDataType {

        /** 多个单元格共享索引 **/
        SharedStringsTable("s"),

        /** 布尔 **/
        Bool("b"),

        InlineStr("inlineStr"),

        /** 错误信息 **/
        Error("e"),

        /** 公式 **/
        Formula("str"),

        /** 数字,默认类型 **/
        Number,

        /** date **/
        Date,

        /** 空 **/
        Null,

        ;

        /** excel单元格数据类型 **/
        private String value;

        XSSFDataType() {}

        XSSFDataType(String value) {
            this.value = value;
        }
    }

    /** 属性持有对象 **/
    private final class XlsxFieldResolver extends AbstractFieldResolver {

        /** 列数 **/
        private final int index;

        public XlsxFieldResolver(Field field, int index)  {
            super(field, conversionService);
            this.index = index;
        }


    }

}
