package com.cedarsoftware.util.io;

import com.cedarsoftware.util.io.models.NestedZonedDateTime;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

class ZonedDateTimeTests extends SerializationDeserializationMinimumTests<ZonedDateTime> {
    private static final ZoneId Z1 = ZoneId.of("America/Chicago");

    private static final ZoneId Z2 = ZoneId.of("America/Anchorage");

    private static final ZoneId Z3 = ZoneId.of("America/Los_Angeles");

    @Test
    void testSimpleCase() {
        ZonedDateTime date = ZonedDateTime.of(LocalDate.now(), LocalTime.now(), ZoneId.of(ZoneId.getAvailableZoneIds().iterator().next()));
        ZonedDateTime date2 = ZonedDateTime.of(LocalDate.of(2022, 12, 23), LocalTime.now(), ZoneId.of(ZoneId.getAvailableZoneIds().iterator().next()));
        NestedZonedDateTime expected = new NestedZonedDateTime(date, date2);
        String json = TestUtil.toJson(expected);
        NestedZonedDateTime result = (NestedZonedDateTime) TestUtil.toJava(json);
        assertThat(result.date1).isEqualTo(date);
    }

    @Test
    void testOldFormat_nested_withRef() {
        String json = loadJsonForTest("old-format-nested-with-ref.json");
        NestedZonedDateTime zonedDateTime = TestUtil.toJava(json);

        assertZonedDateTime(zonedDateTime.date1, 2023, 10, 22, 12, 03, 01, 4539375 * 100, "Asia/Aden", 10800);
        assertZonedDateTime(zonedDateTime.date2, 2022, 12, 23, 12, 03, 00, 4549357 * 100, "Asia/Aden", 10800);
        assertSame(zonedDateTime.date1.getOffset(), zonedDateTime.date2.getOffset());
    }

    @Test
    void testOldFormat_nested() {
        String json = loadJsonForTest("old-format-nested.json");
        NestedZonedDateTime zonedDateTime = TestUtil.toJava(json);

        assertZonedDateTime(zonedDateTime.date1, 2023, 10, 22, 12, 03, 01, 4539375 * 100, "Asia/Aden", 10800);
        assertZonedDateTime(zonedDateTime.date2, 2022, 12, 23, 12, 03, 00, 4549357 * 100, "Asia/Aden", 10800);
        assertSame(zonedDateTime.date1.getOffset(), zonedDateTime.date2.getOffset());
    }

    @Test
    void testOldFormat_topLevel() {
        String json = loadJsonForTest("old-format-simple-case.json");
        ZonedDateTime zonedDateTime = TestUtil.toJava(json);

        assertZonedDateTime(zonedDateTime, 2023, 10, 22, 11, 39, 27, 2496504 * 100, "Asia/Aden", 10800);
    }

    private void assertZonedDateTime(ZonedDateTime zonedDateTime, int year, int month, int day, int hour, int min, int sec, int nano, String zone, Number totalOffset) {
        assertThat(zonedDateTime.getYear()).isEqualTo(year);
        assertThat(zonedDateTime.getMonthValue()).isEqualTo(month);
        assertThat(zonedDateTime.getDayOfMonth()).isEqualTo(day);
        assertThat(zonedDateTime.getHour()).isEqualTo(hour);
        assertThat(zonedDateTime.getMinute()).isEqualTo(min);
        assertThat(zonedDateTime.getSecond()).isEqualTo(sec);
        assertThat(zonedDateTime.getNano()).isEqualTo(nano);
        assertThat(zonedDateTime.getZone()).isEqualTo(ZoneId.of(zone));
        assertThat(zonedDateTime.getOffset()).isEqualTo(ZoneOffset.ofTotalSeconds(totalOffset.intValue()));
    }

    private String loadJsonForTest(String fileName) {
        return TestUtil.fetchResource("zoneddatetime/" + fileName);
    }

    @Override
    protected ZonedDateTime provideT1() {
        LocalDateTime localDateTime = LocalDateTime.of(2019, 12, 15, 9, 7, 16, 2000);
        return ZonedDateTime.of(localDateTime, Z1);
    }

    @Override
    protected ZonedDateTime provideT2() {
        LocalDateTime localDateTime = LocalDateTime.of(2027, 12, 23, 9, 7, 16, 2000);
        return ZonedDateTime.of(localDateTime, Z2);
    }

    @Override
    protected ZonedDateTime provideT3() {
        LocalDateTime localDateTime = LocalDateTime.of(2027, 12, 23, 6, 7, 16, 2000);
        return ZonedDateTime.of(localDateTime, Z3);
    }

    @Override
    protected ZonedDateTime provideT4() {
        LocalDateTime localDateTime = LocalDateTime.of(2027, 12, 23, 6, 7, 16, 2000);
        return ZonedDateTime.of(localDateTime, Z1);
    }

    @Override
    protected NestedZonedDateTime provideNestedInObject() {
        LocalDateTime localDateTime1 = LocalDateTime.of(2027, 12, 23, 6, 7, 16, 2000);
        LocalDateTime localDateTime2 = LocalDateTime.of(2027, 12, 23, 6, 7, 16, 2000);
        return new NestedZonedDateTime(
                ZonedDateTime.of(localDateTime1, Z1),
                ZonedDateTime.of(localDateTime2, Z2));
    }

    @Override
    protected void assertNestedInObject(Object expected, Object actual) {
        NestedZonedDateTime nestedExpected = (NestedZonedDateTime) expected;
        NestedZonedDateTime nestedActual = (NestedZonedDateTime) actual;

        assertThat(nestedActual.date1).isEqualTo(nestedExpected.date1);
        assertThat(nestedActual.date2).isEqualTo(nestedExpected.date2);
    }

    @Override
    protected Object provideDuplicatesNestedInObject() {
        LocalDateTime localDateTime1 = LocalDateTime.of(2027, 12, 23, 6, 7, 16, 2000);
        return new NestedZonedDateTime(
                ZonedDateTime.of(localDateTime1, Z1));
    }

    @Override
    protected void assertDuplicatesNestedInObject(Object expected, Object actual) {
        NestedZonedDateTime nestedExpected = (NestedZonedDateTime) expected;
        NestedZonedDateTime nestedActual = (NestedZonedDateTime) actual;

        assertThat(nestedActual.date1).isEqualTo(nestedExpected.date1);
        assertThat(nestedActual.date2).isEqualTo(nestedExpected.date2);
        assertThat(nestedActual.date2).isSameAs(nestedActual.date1);
    }

    @Override
    protected void assertT1_serializedWithoutType_parsedAsMaps(ZonedDateTime expected, Object actual) {
        String value = (String) actual;
        assertThat(value).isEqualTo("2019-12-15T09:07:16.000002-06:00[America/Chicago]");
    }
}
