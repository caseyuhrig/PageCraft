package caseyuhrig.www;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class SiteUtils {

    public static void writeFile(final File file, final String content) {
        try (final var writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (final IOException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }


    public static String readFile(final File file) {
        if (file == null) {
            throw new RuntimeException("File is null");
        }
        if (!file.exists()) {
            throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
        }
        try (final var lines = Files.lines(file.toPath())) {
            return lines.collect(Collectors.joining("\n"));
        } catch (final IOException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public static String loadResourceFile(final String fileName) {
        try {
            final ClassLoader classLoader = SiteUtils.class.getClassLoader();
            final InputStream inputStream = classLoader.getResourceAsStream(fileName);

            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + fileName);
            }

            try (final InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 final BufferedReader reader = new BufferedReader(streamReader)) {

                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }


    public static String prettyHtml(final String html) {
        final Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        return doc.toString();
    }


    /**
     * Formats HTML content with pretty printing and preserves HTML entities.
     * <p>
     * This method takes a string of HTML content and formats it with the following settings:
     * - Indentation of 4 spaces
     * - Pretty printing enabled
     * - HTML entities preserved (e.g., &amp;copy; remains as is)
     * - UTF-8 charset
     * - HTML syntax
     *
     * @param html The input HTML string to be formatted.
     * @return A formatted string of HTML with preserved entities and 4-space indentation.
     */
    public static String prettyHtmlDoc(final String html) {
        final Document doc = Jsoup.parse(html);
        doc.outputSettings()
                .indentAmount(4)
                .prettyPrint(true)
                .escapeMode(Entities.EscapeMode.xhtml)
                .charset("UTF-8")
                .syntax(Document.OutputSettings.Syntax.html);
        // Preserve existing entities -- doesn't work!
        //doc.select("*").forEach(element -> {
        //    element.html(Parser.unescapeEntities(element.html(), false));
        //});
        return doc.html();
    }

    public static String prettyCssDoc(final String css) {
        // Create a dummy HTML document with the CSS in a style tag
        final Document doc = Jsoup.parse("<html><head><style>" + css + "</style></head></html>");
        // Get the style element
        final Element style = doc.select("style").first();
        if (style != null) {
            // Get the formatted CSS
            String formattedCss = style.html();
            // Remove extra newlines and trim
            //formattedCss = formattedCss.replaceAll("(?m)^\\s*$[\n\r]{1,}", "").trim();
            formattedCss = formattedCss.replaceAll("(?m)^\\s*$[\n\r]+", "").trim();
            return formattedCss;
        }
        return css; // Return original if formatting fails
    }
}
