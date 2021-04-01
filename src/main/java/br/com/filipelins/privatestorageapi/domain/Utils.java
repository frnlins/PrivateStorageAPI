package br.com.filipelins.privatestorageapi.domain;

import static java.math.RoundingMode.HALF_UP;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;

import org.springframework.http.MediaType;

public abstract class Utils {

	private static final BigDecimal bytes = BigDecimal.valueOf(1024);
	private static final DateTimeFormatter dateTimeformatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM,
			FormatStyle.SHORT);
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

	public static MediaType extractMediaType(String objectName) {
		Path file = Path.of(objectName);
		try {
			String contentType = Files.probeContentType(file);
			return MediaType.parseMediaType(contentType);
		} catch (IOException e) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	public static BigDecimal getBytesToKB(BigDecimal byteValue, int scale) {
		return byteValue.divide(bytes).setScale(scale, HALF_UP);
	}

	public static String getFormattedDateTime(TemporalAccessor temporalAccessor) {
		return dateTimeformatter.format(temporalAccessor);
	}

	public static String getFormattedDate(TemporalAccessor temporalAccessor) {
		return dateFormatter.format(temporalAccessor);
	}

	public static String getFormattedTime(TemporalAccessor temporalAccessor) {
		return timeFormatter.format(temporalAccessor);
	}
}
