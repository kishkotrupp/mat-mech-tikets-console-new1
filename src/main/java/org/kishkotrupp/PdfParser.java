package org.kishkotrupp;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.net.URL;

public class PdfParser {
    public String extractTitle(String pdfUrl) {
        // try-with-resources автоматически закроет поток и документ
        try (InputStream in = new URL(pdfUrl).openStream();
             PDDocument document = PDDocument.load(in)) {

            // Настраиваем извлекатель текста
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(1);

            String text = stripper.getText(document);

            // Ищем символ параграфа
            int sectionIndex = text.indexOf('§');
            if (sectionIndex != -1) {
                // Отрезаем символ и берем всё до конца строки
                String title = text.substring(sectionIndex + 1).trim();
                int newLineIndex = title.indexOf('\n');
                if (newLineIndex != -1) {
                    title = title.substring(0, newLineIndex).trim();
                }
                return title;
            }

            // Если символа нет — берем первую строку
            int newLineIndex = text.indexOf('\n');
            if (newLineIndex != -1) {
                return text.substring(0, newLineIndex).trim();
            }
            return text.trim();

        } catch (Exception e) {
            System.err.println("Ошибка парсинга PDF: " + pdfUrl + " (" + e.getMessage() + ")");
            return null; // Если не получилось — вернем null
        }
    }
}
