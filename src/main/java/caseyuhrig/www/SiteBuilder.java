package caseyuhrig.www;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SiteBuilder implements Runnable {

    //final String © = "copy;";

    public static void main(final String[] args) {
        final var builder = new SiteBuilder();
        builder.run();
    }

    public final static String startPath = "C:\\Apache24\\htdocs";
    private int count = 0;
    private PrintWriter writer;

    public void run() {
        try {
            writer = new PrintWriter(new FileWriter(startPath + "\\sitemap.xml"));
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

            final long startTime = System.currentTimeMillis();
            walkDirectory(startPath);
            final long endTime = System.currentTimeMillis();
            System.out.println("Processed " + count + " HTML files");
            System.out.println("Time taken: " + (endTime - startTime) + " ms");

            writer.println("</urlset>");
            writer.flush();
            writer.close();
        } catch (final IOException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    private void walkDirectory(final String startPath) throws IOException {
        final Path start = Paths.get(startPath);
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                final String fileName = file.toString().toLowerCase();
                if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                    processHtmlFile(file.toFile());
                    count++;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                System.err.println("Failed to access file: " + file.toString());
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private final Pattern HEADER_NAV_PAT = Pattern.compile("<(?:header|footer)>.*?(<nav>.*?</nav>).*?</(?:header|footer)>", Pattern.DOTALL);
    private final Pattern ASIDE_NAV_PAT = Pattern.compile("<aside>.*?(<nav>.*?</nav>).*?</aside>", Pattern.DOTALL);
    private final Pattern HEADER_PAT = Pattern.compile("(<header>.*?</header>)", Pattern.DOTALL);

    private final Pattern HEAD_PAT = Pattern.compile("(<head>.*?</head>)", Pattern.DOTALL);
    private final Pattern META_TITLE_PAT = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private final Pattern META_DESC_PAT = Pattern.compile("<meta name=\"description\" content=\"(.*?)\">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private final Pattern META_KEYWORDS_PAT = Pattern.compile("<meta name=\"keywords\" content=\"(.*?)\">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    //private final Pattern META_OG_TITLE_PAT = Pattern.compile("<meta property=\"og:title\" content=\"(.*?)\">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    //private final Pattern META_OG_DESC_PAT = Pattern.compile("<meta property=\"og:description\" content=\"(.*?)\">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);


    private final Pattern FOOTER_PAT = Pattern.compile("(<footer>.*?</footer>)", Pattern.DOTALL);
    private final Pattern FIX_OVERVIEW_PAT = Pattern.compile("<overview>(.*?)</overview>(.*?)<main>", Pattern.DOTALL);
    private final Pattern FIX_ASIDE_PAT = Pattern.compile("</main>(.*?)<footer>", Pattern.DOTALL);
    private final Pattern COMMENT_PAT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private final Pattern INDEX_PAT = Pattern.compile("(<index>.*?</index>)", Pattern.DOTALL);
    private final Pattern LICENSE_TAG_PAT = Pattern.compile("<license>(.*?)</license>", Pattern.DOTALL);

    public static String today() {
        final LocalDate today = LocalDate.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return today.format(formatter);
    }

    private void processHtmlFile(final File file) {
        // TODO: Implement your HTML file processing logic here
        System.out.println("Processing HTML file: " + file.getAbsolutePath());

        final var relativePath = file.getAbsolutePath().substring(startPath.length()).replace("\\", "/").replaceAll("index.html$", "");

        System.out.println("Processing HTML file: " + relativePath);
        writer.println("<url>");
        writer.println("    <loc>https://www.caseyuhrig.com" + relativePath + "</loc>");
        writer.println("    <lastmod>" + today() + "</lastmod>");
        writer.println("    <changefreq>weekly</changefreq>");
        writer.println("    <priority>1.0</priority>");
        writer.println("</url>");

        final String newHeader = SiteUtils.loadResourceFile("header.html");
        final String newFooter = SiteUtils.loadResourceFile("footer.html");
        final String newAsideNav = SiteUtils.loadResourceFile("aside_nav.html");
        final String newHead = SiteUtils.loadResourceFile("head_meta.html");

        final String content = SiteUtils.readFile(file);
        final String result = parseHeader(parseFooter(content, newFooter), newHeader);
        final String res22 = parseHead(result, newHead);
        final String res2 = parseAsideNav(res22, newAsideNav);
        //final String res2 = fixOverview(result);
        //final String res3 = fixAside(res2);
        //final String res3 = replaceIndex(result, newAsideNav);
        //final String res4 = removeEmptyTags("social", res3);
        //final String res5 = removeEmptyTags("picture", res4);
        //final String res6 = fixLicenseTags(res5);
        //final String res6b = fixMultipleOpenAside(fixMultipleCloseAside(res6));
        final String res7 = removeComments(res2);
        final String res8 = SiteUtils.prettyHtmlDoc(res7);
        final String res9 = res8.replaceAll("©", "&copy;");

        //res9.re

        if (file.getName().endsWith("robot.html")) {
            //System.out.println("RESULT: " + res9);
        }
        //System.out.println("RESULT: " + res9);

        SiteUtils.writeFile(file, res9);
    }

    public String fixMultipleOpenAside(final String content) {
        final var sb = new StringBuilder(content);
        final Matcher matcher = Pattern.compile("<aside>.*?<aside>", Pattern.DOTALL).matcher(sb.toString());
        while (matcher.find()) {
            //final String part = matcher.group(1);
            sb.replace(matcher.start(), matcher.end(), "<aside>");
        }
        return sb.toString();
    }

    public String fixMultipleCloseAside(final String content) {
        final var sb = new StringBuilder(content);
        final Matcher matcher = Pattern.compile("</aside>.*?</aside>", Pattern.DOTALL).matcher(sb.toString());
        while (matcher.find()) {
            //final String part = matcher.group(1);
            sb.replace(matcher.start(), matcher.end(), "</aside>");
        }
        return sb.toString();
    }

    private String fixLicenseTags(final String content) {
        final var sb = new StringBuilder(content);
        final Matcher matcher = LICENSE_TAG_PAT.matcher(sb.toString());
        if (matcher.find()) {
            final String html = matcher.group(1);
            if (!html.trim().isEmpty()) {
                sb.replace(matcher.start(), matcher.end(), "<div id=\"license\">" + html + "</div>");
            } else {
                sb.delete(matcher.start(), matcher.end());
            }
        }
        return sb.toString();
    }

    private String removeEmptyTags(final String tagName, final String content) {
        final var sb = new StringBuilder(content);
        final Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(sb.toString());
        if (matcher.find()) {
            final String html = matcher.group(1);
            if (!html.trim().isEmpty()) {
                sb.replace(matcher.start(), matcher.end(), html);
            } else {
                sb.delete(matcher.start(), matcher.end());
            }
        }
        return sb.toString();
    }

    private String removeComments(final String content) {
        final Matcher matcher = COMMENT_PAT.matcher(content);
        final String result = matcher.replaceAll("");
        return result;
    }

    private String fixOverview(final String content) {
        final var sb = new StringBuilder(content);
        final Matcher matcher = FIX_OVERVIEW_PAT.matcher(content);
        if (matcher.find()) {
            final String part1 = matcher.group(1);
            final String part2 = matcher.group(2);
            sb.replace(matcher.start(), matcher.end(), "<main>" + part1 + part2);
        }
        return sb.toString();
    }

    private String extractPattern(final Pattern pattern, final String value) {
        final Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String parseHead(final String html, final String newHtml) {

        final Matcher matcher = HEAD_PAT.matcher(html);
        if (matcher.find()) {
            final var sb = new StringBuilder(html);

            // extract the title, description, and keywords
            final String title = extractPattern(META_TITLE_PAT, sb.toString());
            final String desc = extractPattern(META_DESC_PAT, sb.toString());
            final String keywords = extractPattern(META_KEYWORDS_PAT, sb.toString());


            //System.out.printf("[%s]\t[%s]\t[%s]\n", title, desc, keywords);

            sb.replace(matcher.start(1), matcher.end(1), newHtml);

            var result = sb.toString().replaceAll("<title>(.*?)</title>", "<title>" + title + "</title>");
            result = result.replaceAll("<meta name=\"description\" content=\"(.*?)\">", "<meta name=\"description\" content=\"" + desc + "\">");
            result = result.replaceAll("<meta name=\"keywords\" content=\"(.*?)\">", "<meta name=\"keywords\" content=\"" + keywords + "\">");
            result = result.replaceAll("<meta property=\"og:title\" content=\"(.*?)\">", "<meta property=\"og:title\" content=\"" + title + "\">");
            result = result.replaceAll("<meta property=\"og:description\" content=\"(.*?)\">", "<meta property=\"og:description\" content=\"" + desc + "\">");
            //matcher.reset(sb.toString());
            //}
            //System.out.println("RESULT: " + result);

            return result;
        }
        return html;
    }


    private String parseAsideNav(final String content, final String navHtml) {
        final var sb = new StringBuilder(content);
        final Matcher matcher = ASIDE_NAV_PAT.matcher(content);
        if (matcher.find()) {
            //if (matcher.hasMatch()) {
            //final String part1 = matcher.group(1);
            //final String part2 = matcher.group(2);
            sb.replace(matcher.start(1), matcher.end(1), navHtml);
            matcher.reset(sb.toString());
            //}
        }
        //final String result = SiteUtils.prettyHtmlDoc(sb.toString());
        //return result;
        return sb.toString();
    }

    private String fixAside(final String content) {
        final var sb = new StringBuilder(content);
        final Matcher matcher = FIX_ASIDE_PAT.matcher(content);
        if (matcher.find()) {
            final String part = matcher.group(1);
            sb.replace(matcher.start(1), matcher.end(1), "<aside>" + part + "</aside>");
        }
        //final String result = SiteUtils.prettyHtmlDoc(sb.toString());
        //return result;
        return sb.toString();
    }

    private String replaceIndex(final String content, final String newIndex) {
        final var sb = new StringBuilder(content);

        final Matcher matcher = INDEX_PAT.matcher(content);
        if (matcher.find()) {
            final String nav = matcher.group(1);
            //content.substring(matcher.start(1), matcher.end(1));
            sb.replace(matcher.start(1), matcher.end(1), newIndex);
            //System.out.println("Found index: " + SiteUtils.prettyHtml(nav));
            //matcher.replaceAll("test");
        }
        //System.out.println("RESULT: " + matcher.replaceAll("test"));
        //final String result = SiteUtils.prettyHtmlDoc(sb.toString());
        //return result;
        return sb.toString();
    }

    private String parseHeader(final String content, final String newNav) {
        final var sb = new StringBuilder(content);

        final Matcher matcher = HEADER_PAT.matcher(content);
        if (matcher.find()) {
            final String nav = matcher.group(1);
            //content.substring(matcher.start(1), matcher.end(1));
            sb.replace(matcher.start(1), matcher.end(1), newNav);
            //System.out.println("Found nav: " + SiteUtils.prettyHtml(nav));
            //matcher.replaceAll("test");
        }
        //System.out.println("RESULT: " + matcher.replaceAll("test"));
        //final String result = SiteUtils.prettyHtmlDoc(sb.toString());
        return sb.toString();
    }

    private String parseFooter(final String content, final String newNav) {
        final var sb = new StringBuilder(content);

        final Matcher matcher = FOOTER_PAT.matcher(content);
        if (matcher.find()) {
            final String nav = matcher.group(1);
            //content.substring(matcher.start(1), matcher.end(1));
            sb.replace(matcher.start(1), matcher.end(1), newNav);
            //System.out.println("Found nav: " + SiteUtils.prettyHtml(nav));
            //matcher.replaceAll("test");
        }
        //System.out.println("RESULT: " + matcher.replaceAll("test"));
        //final String result = SiteUtils.prettyHtmlDoc(sb.toString());
        //return result;
        return sb.toString();
    }
}
