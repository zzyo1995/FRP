package main.java.parse;

import main.java.bean.FR;
import main.java.bean.Replacement;
import main.java.bean.Sentence;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zzyo on 2017/3/17.
 */
public class Parser {


    private String origin;
    private String result;
    private ArrayList<Replacement> replacements = new ArrayList<>();
    private ArrayList<Sentence> sentences = new ArrayList<>();

    public Parser() {
    }

    public String parseBlock(String origin) {
        Document doc = Jsoup.parse(origin);
        Elements blocks = doc.select("br");
        for (Element block : blocks) {
            if (block.parent().nextElementSibling() != null) {
                if (block.parent().nextElementSibling().select("br") != null) {
                    block.parent().nextElementSibling().select("br").after("<$BLOCK-END$>");
                    block.parent().nextElementSibling().select("br").remove();
                    block.parent().remove();
                    System.out.println("------------------------------");
                }
            }
        }
        return doc.outerHtml();
    }

    public String parseCode(String origin) {
        Document doc = Jsoup.parse(origin);
        Elements codes = doc.select("code");
        Pattern pattern = Pattern.compile(" ");
        int codeIndex = 0;
        for (Element code : codes) {
            String originCode = code.text();
            String codeType = pattern.split(code.className())[0];
            codeType = "<$CODE-" + codeType.toUpperCase() + "$" + codeIndex + ">";
            Replacement replacement = new Replacement();
            replacement.setOrigin(originCode);
            replacement.setReplacement(codeType);
            replacements.add(replacement);
            //System.out.println(originCode+"\n"+codeType);
            code.parent().after(codeType);
            code.parent().remove();
            codeIndex++;
        }
        return doc.outerHtml();
    }

    public String parseLink(String origin) {
        Document doc = Jsoup.parse(origin);
        Elements links = doc.select("a[href]");
        int linkIndex = 0;
        for (Element link : links) {
            String href = link.attr("href");
            String text = link.text();
            Replacement replacement = new Replacement();
            replacement.setOrigin(href);
            replacement.setReplacement("<$LINK-HTTP$" + linkIndex + ">");
            replacements.add(replacement);
            link.after(text + "<$LINK-HTTP$" + linkIndex + ">");
            link.remove();
            linkIndex++;
        }
        return doc.outerHtml();
    }

    public String parseFile(String origin) {
        String[] fileTypes = {"php", "sql", "java", "rm", "htaccess", "jar", "py", "exe"};
        String regEx = "";
        int fileIndex = 0;
        StringBuffer sb = new StringBuffer();
        for (int index = 0; index < fileTypes.length; index++) {
                    // parse unix path file(full path and relative path)
            regEx = "((((\\.){0,2}\\/)*((((\\w*-|\\w*\\.)*\\w*)\\/)*((\\w*-|\\w*\\.)*\\w*)))|" +
                    // windows path(full path and relative path)
                    "(([C-Z]:|(\\.){0,2}\\\\)?(((\\w*-|\\w*\\.)*\\w*)\\\\)*((\\w*-|\\w*\\.)*\\w*)))\\." + fileTypes[index] + "(?=[^\\.])";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(origin);
            while (matcher.find()) {
                //System.out.println("[" + matcher.group() + "]");
                String path = matcher.group(0);
                String type = fileTypes[index].toUpperCase();
                type = "<$FILE-" + type + "$" + fileIndex + ">";
                type = Matcher.quoteReplacement(type);
                System.out.println(path + "  " + type);
                Replacement replacement = new Replacement();
                replacement.setOrigin(path);
                replacement.setReplacement(type);
                replacements.add(replacement);
                matcher.appendReplacement(sb, type);
                fileIndex++;
            }
            matcher.appendTail(sb);
            origin = sb.toString();
            sb.setLength(0);
        }
        return origin;
    }

    public String parsePath(String origin) {
        // parse unix path(full path and relative path)     windows path(full path and relative path)    package path
        String regEx = "(?<!\\<)" +
                "(((\\.){0,2}\\/(((\\w*-|\\w*\\.)*\\w+)\\/)*((\\w*-|\\w*\\.)*\\w*))|" +
                "(((([C-Z]:)|(\\.){0,2})\\\\)(((\\w*-|\\w*\\.)*\\w+)\\\\)*((\\w*-|\\w*\\.)*\\w*))|" +
                "(([a-zA-Z]+\\.[a-zA-Z]+)[.]*\\w*))" +
                "(?!>)";
        int pathIndex = 0;
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(origin);
        while (matcher.find()) {
            String path = matcher.group(0);
            String type = "<$PATH-$" + pathIndex + ">";
            type = Matcher.quoteReplacement(type);
            System.out.println(path + "   " + type);
            Replacement replacement = new Replacement();
            replacement.setOrigin(path);
            replacement.setReplacement(type);
            replacements.add(replacement);
            matcher.appendReplacement(sb, type);
            pathIndex++;
        }
        matcher.appendTail(sb);
        origin = sb.toString();
        sb.setLength(0);
        return origin;
    }

    public String parseList(String origin) {
        Document doc = Jsoup.parse(origin);
        Elements ulLists = doc.select("ul");
        Elements olLists = doc.select("ol");
        int index = 0;
        for (Element ulList : ulLists) {
            Elements lists = ulList.select("li");
            for (Element list : lists) {

            }
        }
        return doc.outerHtml();
        //HtmlToPlainText htmlToPlainText = new HtmlToPlainText();
        //return htmlToPlainText.getPlainText(doc);
    }


}
