package com.example.ai.source.ai;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;

@Component
public class ArticleExtractor {

    private static final int TIMEOUT_MS = 10_000;
    private static final int MIN_CONTENT_LENGTH = 200;
    private static final int MAX_CONTENT_LENGTH = 15_000;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; LearningAiBot/1.0)";

    public ExtractedArticle extract(String url) {
        validatePublicUrl(url);
        Document doc = fetch(url);

        String content = extractContent(doc);
        if (content.length() < MIN_CONTENT_LENGTH) {
            throw new ArticleExtractionException(
                    "본문을 충분히 추출하지 못했습니다. (유료 기사 또는 JavaScript 렌더링 페이지일 수 있음)");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            content = content.substring(0, MAX_CONTENT_LENGTH);
        }

        return new ExtractedArticle(doc.title(), content, sha256(content), extractPublishedAt(doc));
    }

    private void validatePublicUrl(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new ArticleExtractionException("올바르지 않은 URL입니다.");
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new ArticleExtractionException("http/https 링크만 등록할 수 있습니다.");
        }
        if (uri.getHost() == null) {
            throw new ArticleExtractionException("호스트가 없는 URL입니다.");
        }

        try {
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (address.isLoopbackAddress() || address.isSiteLocalAddress()
                        || address.isLinkLocalAddress() || address.isAnyLocalAddress()) {
                    throw new ArticleExtractionException("내부 네트워크 주소는 등록할 수 없습니다.");
                }
            }
        } catch (UnknownHostException e) {
            throw new ArticleExtractionException("도메인을 찾을 수 없습니다.");
        }
    }

    private Document fetch(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .get();
        } catch (HttpStatusException e) {
            throw new ArticleExtractionException("페이지 응답 오류: HTTP " + e.getStatusCode());
        } catch (IOException e) {
            throw new ArticleExtractionException("페이지를 불러오지 못했습니다: " + e.getMessage());
        }
    }

    private String extractContent(Document doc) {
        doc.select("script, style, nav, header, footer, aside, form, iframe, noscript").remove();
        Element article = doc.selectFirst("article");
        Element root = (article != null) ? article : doc.body();
        return (root == null) ? "" : root.text().strip();
    }

    private LocalDateTime extractPublishedAt(Document doc) {
        Element meta = doc.selectFirst("meta[property=article:published_time]");
        if (meta == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(meta.attr("content")).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}