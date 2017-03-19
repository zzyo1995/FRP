package main.java.parse;

import main.java.bean.BlockItem;
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


    //private String origin;
    //private String result;
    private FR fr;
    private ArrayList<Replacement> replacements = new ArrayList<>();
    //private ArrayList<Sentence> sentences = new ArrayList<>();

    public Parser() {
    }

/*    public String parseBlock(String origin) {
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
    }*/

    public String[] parseBlock(String origin) {
        Pattern pattern = Pattern.compile("(\\s*?\\n){2,}");
        Matcher matcher = pattern.matcher(origin);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<\\$BLOCK-END\\$>");
        }
        matcher.appendTail(sb);
        origin = sb.toString();
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        String[] blocks = origin.split("<\\$BLOCK-END\\$>");
        for (String str : blocks) {
            System.out.println("block--->" + str);
        }
        System.out.println("------------------------------------------------");
        return blocks;
    }

    public String parseSentences(String[] blocks){

        return "";
    }

    public String parseCode(String origin) {
        Document doc = Jsoup.parse(origin);
        Elements codes = doc.select("code");
        Pattern pattern = Pattern.compile(" ");
        int codeIndex = 0;
        for (Element code : codes) {
            //System.out.println(code.html());
            String originCode = code.text();
            String codeType = pattern.split(code.className())[0];
            codeType = "<$CODE-" + codeType.toUpperCase() + "$" + codeIndex + ">";
            Replacement replacement = new Replacement();
            replacement.setOrigin(originCode);
            replacement.setReplacement(codeType);
            replacements.add(replacement);
            code.text(codeType);
            //System.out.println(originCode+"\n"+codeType);
            code.parent().after(codeType);
            code.parent().remove();
            codeIndex++;
        }
        return doc.html();
    }

    /***
     *
     * 1.字符串附加超链接       >=	    原字符串<HTTP-LINK>
     * 2.URL无超链接           >=	    <HTTP-LINK>
     * 3.URL附加自身超链接      >=	    <HTTP-LINK>
     */

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
            if (text.matches("(http|https|ftp):\\/\\/.+")) {
                link.after("<$LINK-HTTP$" + linkIndex + ">");
            } else {
                link.after(text + "<$LINK-HTTP$" + linkIndex + ">");
            }
            link.remove();
            linkIndex++;
        }
        origin = doc.outerHtml();
        origin = origin.replaceAll("&lt;\\$", "<\\$");
        origin = origin.replaceAll("&gt;", ">");
        return origin;
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
                type = "<\\$FILE-" + type + "\\$" + fileIndex + ">";
                //type = Matcher.quoteReplacement(type);
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

    /**
     * Unix 绝对路径 | 相对路径必须以（./）（../）开头
     * Windows 绝对路径 | 相对路径必须以（.\）（..\）开头
     */
    public String parsePath(String origin) {
        // parse unix path(full path and relative path)     windows path(full path and relative path)    package path
        String regEx = "(?<![\\<(http)])" +
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
            String type = "<\\$PATH-\\$" + pathIndex + ">";
            //type = Matcher.quoteReplacement(type);
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

    public String parseHtmlToText(String origin) {
        /*Pattern pattern = Pattern.compile(">(\\s*)<");
        Matcher matcher = pattern.matcher(origin);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()){
            matcher.appendReplacement()
        }*/
        origin = origin.replaceAll("(?<!\\$CODE-(.*)?)(?<=>)\\s*(?=<)", "");
        origin = origin.replaceAll("<li>", "<\\$LIST\\$>");
        origin = origin.replaceAll("<p><br><\\/p>", "\n");
        origin = origin.replaceAll("<br>", "\n");
        origin = origin.replaceAll("<\\/p>", "\n");
        origin = origin.replaceAll("<\\/li>", "\n");
        origin = origin.replaceAll("<[\\/]?[a-z]+>", "");
        return origin;
    }

    public String parseQuote(String origin) {
        String regEx = "([\"']).*?\\1";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(origin);
        StringBuffer sb = new StringBuffer();
        int quoteIndex = 0;
        while (matcher.find()) {
            String quote = matcher.group(0);
            String type = "<\\$QUOTE\\$" + quoteIndex + ">";
            //type = Matcher.quoteReplacement(type);
            System.out.println(quote + "    " + type);
            Replacement replacement = new Replacement();
            replacement.setOrigin(quote);
            replacement.setReplacement(type);
            replacements.add(replacement);
            matcher.appendReplacement(sb, type);
            quoteIndex++;
        }
        matcher.appendTail(sb);
        origin = sb.toString();
        sb.setLength(0);
        return origin;
    }

    public String parseEmail(String origin) {
        String regEx = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(origin);
        int emailIndex = 0;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String email = matcher.group(0);
            String type = "<\\$EMAIL\\$" + emailIndex + ">";
            //type = Matcher.quoteReplacement(type);
            System.out.println(email + "    " + type);
            Replacement replacement = new Replacement();
            replacement.setOrigin(email);
            replacement.setReplacement(type);
            replacements.add(replacement);
            matcher.appendReplacement(sb, type);
            emailIndex++;
        }
        matcher.appendTail(sb);
        origin = sb.toString();
        sb.setLength(0);
        return origin;
    }

    public String parseShort(String origin) {
        String[][] shorts = {{"\\be\\.g(\\s)", "\\be\\.g.(\\s)", "(\\b)eg(\\s)", "\\beg\\.(\\s)", "\\bi\\.e.(\\s)", "\\bi\\.e(\\s)"}, {"\\.NET(.?)"}, {"\\bImo(\\s)"}};
        String[] replace = {"For example", "dotNET", "In my opinion"};
        StringBuffer sb = new StringBuffer();
        int shortIndex = 0;
        for (int i = 0; i < shorts.length; i++) {
            for (int j = 0; j < shorts[i].length; j++) {
                Pattern pattern = Pattern.compile(shorts[i][j]);
                Matcher matcher = pattern.matcher(origin);
                while (matcher.find()) {
                    String str = matcher.group(0);
                    String blank = matcher.group(1);
                    String type = replace[i] + "\\$" + shortIndex + "\\$";
                    System.out.println(str + "  " + type);
                    Replacement replacement = new Replacement();
                    replacement.setOrigin(str);
                    replacement.setReplacement(type);
                    replacements.add(replacement);
                    matcher.appendReplacement(sb, type + blank);
                    shortIndex++;
                }
                matcher.appendTail(sb);
                origin = sb.toString();
                sb.setLength(0);
            }
        }
        return origin;
    }

    public String parseList(String origin) {
        Document doc = Jsoup.parse(origin);
        Elements ulLists = doc.select("ul");
        Elements olLists = doc.select("ol");
        for (Element ulList : ulLists) {
            Elements lists = ulList.select("li");
            for (Element list : lists) {
                //list.tagName("ULIST");
            }
        }
        for (Element olList : olLists) {
            Elements lists = olList.select("li");
            for (Element list : lists) {
                //list.tagName("OLIST");
            }
        }
        //return doc.outerHtml();
        HtmlToPlainText htmlToPlainText = new HtmlToPlainText();
        return htmlToPlainText.getPlainText(doc);
    }


}
