package me.leejay.jenkins.dateparameter;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by JuHyunLee on 2017. 6. 2..
 */
public class StringLocalDateValue {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final static String JAVA_PATTERN = "^LocalDate\\.now\\(\\)(\\.(plus|minus)(Days|Months|Years)\\([0-9]+\\))*;?$";

    private final String stringLocalDate;

    private final String stringDateFormat;

    private final DateTimeFormatter dateFormat;

    public StringLocalDateValue(String stringLocalDate, String stringDateFormat) {
        this.stringLocalDate = stringLocalDate;
        this.stringDateFormat = stringDateFormat;
        this.dateFormat = DateTimeFormat.forPattern(stringDateFormat);
    }

    public String getStringLocalDate() {
        return stringLocalDate;
    }

    public String getStringValue() {
        return stringLocalDate;
    }

    public boolean isCompletionFormat() {
        try {
            return LocalDate.parse(stringLocalDate, dateFormat) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isJavaFormat() {
        return stringLocalDate.matches(JAVA_PATTERN);
    }

    public String getStringDateFormat() {
        return stringDateFormat;
    }

    LocalDate parseJava() {
        List<String> codes = Arrays.asList(stringLocalDate.split("\\."));
        if (codes.size() == 2) { // LocalDate.now();
            if (stringLocalDate.equals("LocalDate.now()") || stringLocalDate.equals("LocalDate.now();")) {
                return LocalDate.now();
            }
            return null;
        }

        LocalDate localDate = LocalDate.now();
        for (String code : codes.subList(2, codes.size())) {
            IntegerParamMethod paramMethod = new IntegerParamMethod(code);
            if (paramMethod.getName() == null || paramMethod.getParameter() == null) {
                log.error("Failed to parse method={}, parameter={}", paramMethod.getName(), paramMethod.getParameter());
                return null;
            }

            try {
                Method method = localDate.getClass().getMethod(paramMethod.getName(), int.class);
                if (method == null) {
                    log.error("Failed to getMethod={}", paramMethod.getName());
                    return null;
                }
                localDate = (LocalDate) method.invoke(localDate, paramMethod.getParameter());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error("Failed to invoke method, {}, {}", paramMethod.getName(), paramMethod.getParameter());
                return null;
            }
        }

        return localDate;
    }

    String getValue() {
        if (isCompletionFormat()) {
            return stringLocalDate;
        }

        if (isJavaFormat()) {
            return parseJava().toString(dateFormat);
        }

        return "";
    }

}