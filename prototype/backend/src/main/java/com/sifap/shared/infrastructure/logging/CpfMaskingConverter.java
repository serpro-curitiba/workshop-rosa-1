package com.sifap.shared.infrastructure.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CpfMaskingConverter extends ClassicConverter {

    private static final Pattern CPF_PATTERN = Pattern.compile("\\b\\d{3}\\.?\\d{3}\\.?([0-9]{3})-?([0-9]{2})\\b");

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null || message.isBlank()) {
            return message;
        }

        Matcher matcher = CPF_PATTERN.matcher(message);
        StringBuffer masked = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(masked, "XXX.XXX." + matcher.group(1) + "-" + matcher.group(2));
        }
        matcher.appendTail(masked);
        return masked.toString();
    }
}