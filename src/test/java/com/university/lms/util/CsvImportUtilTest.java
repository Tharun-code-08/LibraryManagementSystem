package com.university.lms.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.university.lms.dto.request.BookImportRowDTO;

class CsvImportUtilTest {

    @Test
    void parsesDataRowsSkippingHeader() {
        String csv = """
                isbn,title,authors,publisher,category,edition,language,cost
                978-0-13-468599-1,Effective Java,Joshua Bloch,Addison-Wesley,Programming,3rd,English,45.00
                978-0-596-00712-6,Head First Design Patterns,Freeman;Robson,O'Reilly,Programming,1st,English,39.99
                """;

        List<BookImportRowDTO> rows = CsvImportUtil.parseBookRows(new StringReader(csv));

        assertEquals(2, rows.size());
        BookImportRowDTO first = rows.get(0);
        assertEquals(2, first.rowNumber());
        assertEquals("978-0-13-468599-1", first.isbn());
        assertEquals("Effective Java", first.title());
        assertEquals("Joshua Bloch", first.authorNames());
        assertEquals(new BigDecimal("45.00"), first.cost());

        BookImportRowDTO second = rows.get(1);
        assertEquals(3, second.rowNumber());
        assertEquals("Freeman;Robson", second.authorNames());
    }

    @Test
    void defaultsCostToZeroWhenMissing() {
        String csv = "isbn,title,authors,publisher,category,edition,language,cost\n"
                + "978-1-2-3,No Cost Book,Author,Pub,Cat,1st,English,\n";

        List<BookImportRowDTO> rows = CsvImportUtil.parseBookRows(new StringReader(csv));

        assertEquals(BigDecimal.ZERO, rows.get(0).cost());
    }
}
