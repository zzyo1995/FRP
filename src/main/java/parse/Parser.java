package main.java.parse;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import main.java.bean.BlockItem;
import main.java.bean.FR;
import main.java.bean.Replacement;
import main.java.bean.Sentence;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zzyo on 2017/3/17.
 */
public class Parser {


    private Map<String, Replacement> replacementMap = new HashMap<>();

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
        //origin = origin.replaceAll("<\\$", "<");
        //origin = origin.replaceAll("\\$>", ">");
        System.out.println(origin);
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
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i].matches("^<\\$CODE-.+>$")) {
                blocks[i - 1] = blocks[i - 1].concat("\n" + blocks[i] + "\n");
                int index = i;
                for (; index < blocks.length - 1; index++) {
                    blocks[index] = blocks[index + 1];
                }
                blocks[index] = "";
            }
            System.out.println("block--->" + blocks[i]);
        }
        System.out.println("------------------------------------------------");
        return blocks;
    }

    public ArrayList<BlockItem> parseSentences(String[] blocks) {
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        for (String str : blocks) {
            BlockItem blockItem = new BlockItem();
            String[] sentences = {};
            // can not parse sentences like "the price is 2.3 is a number."
            sentences = str.split("((\\.|\\?|\\!)((\\s*)|(?=[A-Z])))|(\\n)");
            ArrayList<String> sentenceArrayList = new ArrayList<String>(Arrays.asList(sentences));
            //System.out.println("length is :" + sentences.length);
            for (int i = 0; i < sentenceArrayList.size(); i++) {
                Sentence sentence = new Sentence();
                //System.out.println("sentence --->" + sentenceArrayList.get(i));
                if (sentenceArrayList.get(i).matches("^<\\$LIST\\$>.*")) {
                    if (sentenceArrayList.get(i - 1).matches(".*:$")) {
                        // 如果是  str：\n 1，～2.～3.～ 修改上个sentence:(addItmList,setOrigin)
                        //System.out.println("LISTS START:------->");
                        for (int j = i; j < sentenceArrayList.size(); j++) {
                            if (sentenceArrayList.get(j).matches("^<\\$LIST\\$>.*")) {
                                String list = sentenceArrayList.get(j).replaceFirst("<\\$LIST\\$>", "");
                                Sentence itemList = new Sentence();
                                itemList.setOrigin(list);
                                itemList.setResult(list);
                                sentence = blockItem.getSentences().get(blockItem.getSentences().size() - 1);
                                sentence.setOrigin(sentence.getOrigin().concat("\n" + list));
                                sentence.addItemList(itemList);
                                //System.out.println("LIST--->" + list);
                                i = j;
                            } else break;
                        }
                    } else {
                        // 如果是   str.\n   1，～2.～3.～ 修改?
                    }
                } else {
                    sentence.setOrigin(sentenceArrayList.get(i));
                    sentence.setResult(sentenceArrayList.get(i));
                    blockItem.addSentence(sentence);
                }
            }
            blockItems.add(blockItem);
        }
        return blockItems;
    }

    public static List<CoreLabel> getRawWords(String text) {
        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(text));
        List<CoreLabel> rawWords = tok.tokenize();
        return rawWords;
    }

    public static String[] getTokens(String content) {
        List<CoreLabel> words = getRawWords(content);
        String[] results = new String[words.size()];
        for (int i = 0; i < words.size(); i++)
            results[i] = words.get(i).word();
        return results;
    }

    public static int getTokenIndex(String str, String token) {
        System.out.println("Str is--->" + str);
        String[] results = getTokens(str);
        for (int i = 0; i < results.length; i++) {
            System.out.println("Result is--->" + results[i] + "Token is--->" + token);
            token = getTokens(token)[0];
            if (token.equals(results[i])) {
                System.out.println("TokenIndex is--->" + i);
                return i;
            }
        }
        return -1;
    }

    public Sentence updateSentence(Sentence sentence) {
        StringBuffer sb = new StringBuffer();
        String origin = sentence.getOrigin();
        String tmp = origin;
        int flag = 0;
        Pattern pattern = Pattern.compile("<\\$.+?\\$\\d{0,2}>");
        Matcher matcher = pattern.matcher(origin);
        while (matcher.find()) {
            flag = 1;
            String type = matcher.group(0);
            Replacement replacement = this.replacementMap.get(type);
            //System.out.println(replacement.toString());
            String content = type.replaceAll("\\$", "");
            tmp = tmp.replaceFirst(Matcher.quoteReplacement(type), content);
            int tokenIndex = getTokenIndex(tmp, content);
            System.out.println("TYPE is--->" + type + "TMP is --->" + tmp);
            System.out.println("Token index is--->" + tokenIndex);
            replacement.setIndexOfReplace(tokenIndex);
            sentence.addReplacements(replacement);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.getOrigin()));
        }
        if (flag == 1) {
            matcher.appendTail(sb);
            sentence.setOrigin(sb.toString());
            sb.setLength(0);
        }
        String result = sentence.getResult();
        result = result.replaceAll("<\\$", "<");
        result = result.replaceAll("\\$\\d{0,2}>", ">");
        sentence.setResult(result);
        return sentence;
    }

    public ArrayList<BlockItem> parseReplacement(ArrayList<BlockItem> blockItems) {
        // change sentence.origin to origin text
        //set Replacement List in Sentence
        // set index of replacement
        for (int i = 0; i < blockItems.size(); i++) {
            BlockItem blockItem = blockItems.get(i);
            for (int j = 0; j < blockItem.getSentences().size(); j++) {
                Sentence sentence = blockItem.getSentences().get(j);
                blockItems.get(i).getSentences().set(j, updateSentence(sentence));
                for (int k = 0; k < sentence.getItemLists().size(); k++) {
                    Sentence sentence1 = sentence.getItemLists().get(k);
                    blockItems.get(i).getSentences().get(j).getItemLists().set(k, updateSentence(sentence1));
                }
            }
        }
        return blockItems;
    }

/*    public String parseSysName(){

    }*/

    public String parseCode(String origin) {
        Document doc = Jsoup.parse(origin);
        Elements codes = doc.select("code");
        Pattern pattern = Pattern.compile(" ");
        int codeIndex = 0;
        for (Element code : codes) {
            //System.out.println(code.html());
            String originCode = code.text();
            String codeType = pattern.split(code.className())[0];
            String type = "<$CODE-" + codeType.toUpperCase() + "$" + codeIndex + ">";
            Replacement replacement = new Replacement();
            replacement.setOrigin(originCode);
            replacement.setReplacement("<CODE-" + codeType.toUpperCase() + ">");
            this.replacementMap.put(type, replacement);
            //code.text(codeType);
            //System.out.println(originCode+"\n"+codeType);
            code.parent().after(type);
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
            String type = "<$LINK-HTTP$" + linkIndex + ">";
            Replacement replacement = new Replacement();
            replacement.setOrigin(href);
            replacement.setReplacement("<LINK-HTTP>");
            this.replacementMap.put(type, replacement);
            if (text.matches("(http|https|ftp):\\/\\/.+")) {
                link.after(type);
            } else {
                link.after(text + type);
            }
            link.remove();
            linkIndex++;
        }
        origin = doc.outerHtml();
        origin = origin.replaceAll("&lt;", "<");
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
                replacement.setReplacement("<FILE-" + fileTypes[index].toUpperCase() + ">");
                this.replacementMap.put("<$FILE-" + fileTypes[index].toUpperCase() + "$" + fileIndex + ">", replacement);
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
            String type = "<\\$PATH\\$" + pathIndex + ">";
            //type = Matcher.quoteReplacement(type);
            System.out.println(path + "   " + type);
            Replacement replacement = new Replacement();
            replacement.setOrigin(path);
            replacement.setReplacement("<PATH>");
            this.replacementMap.put("<$PATH$" + pathIndex + ">", replacement);
            matcher.appendReplacement(sb, type);
            pathIndex++;
        }
        matcher.appendTail(sb);
        origin = sb.toString();
        sb.setLength(0);
        return origin;
    }

    public String parseHtmlToText(String origin) {
        // code area 自带换行
        origin = origin.replaceAll("(?<!\\$CODE-(.*)?)(?<=>)\\s*(?=<)", "");
        System.out.println("去空格："+origin);
        origin = origin.replaceAll("<p><\\/p>", "");
        System.out.println("去<p></p>："+origin);
        origin = origin.replaceAll("<li>", "<\\$LIST\\$>");
        origin = origin.replaceAll("<p><br><\\/p>", "\n");
        origin = origin.replaceAll("<br>", "\n");
        origin = origin.replaceAll("<\\/p>", "\n");
        origin = origin.replaceAll("<\\/li>", "\n");
        origin = origin.replaceAll("<[\\/]?[a-z]+>", "");
        origin = origin.replaceAll("&nbsp;", " ");
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
            replacement.setReplacement("<QUOTE>");
            this.replacementMap.put("<$QUOTE$" + quoteIndex + ">", replacement);
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
            replacement.setReplacement("<EMAIL>");
            this.replacementMap.put("<$EMAIL$" + emailIndex + ">", replacement);
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
        String[] replace = {"for example", "dotNET", "in my opinion"};
        StringBuffer sb = new StringBuffer();
        int shortIndex = 0;
        for (int i = 0; i < shorts.length; i++) {
            for (int j = 0; j < shorts[i].length; j++) {
                Pattern pattern = Pattern.compile(shorts[i][j]);
                Matcher matcher = pattern.matcher(origin);
                while (matcher.find()) {
                    String str = matcher.group(0);
                    String blank = matcher.group(1);
                    String type = "<\\$" + replace[i] + "\\$" + shortIndex + ">";
                    System.out.println(str + "  " + type);
                    Replacement replacement = new Replacement();
                    replacement.setOrigin(str);
                    replacement.setReplacement(replace[i]);
                    this.replacementMap.put("<$" + replace[i] + "$" + shortIndex + ">", replacement);
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

    public Map<String, Replacement> getReplacementMap() {
        return this.replacementMap;
    }

    public ArrayList<BlockItem> parseExe(String raw) {
        Parser parser = new Parser();
        String tmp = parser.parseCode(raw);
        tmp = parser.parseLink(tmp);
        tmp = parser.parseEmail(tmp);
        tmp = parser.parseQuote(tmp);
        tmp = parser.parseShort(tmp);
        tmp = parser.parseFile(tmp);
        tmp = parser.parsePath(tmp);
        tmp = parser.parseHtmlToText(tmp);
        String[] blocks = parser.parseBlock(tmp);
        ArrayList<BlockItem> blockItems = parser.parseSentences(blocks);
        blockItems = parser.parseReplacement(blockItems);
        return blockItems;
    }

    public String printResult(ArrayList<BlockItem> blockItems){
        String result = "";
        for (BlockItem blockItem : blockItems) {
            result = result.concat("\n------------------   block start   --------------------------------\n\n");

            for (Sentence sentence : blockItem.getSentences()) {
                result = result.concat("\n--------------------   sentence start   -------------------------\n");
                result = result.concat("\nOrigin is--->" + sentence.getOrigin()+"\n");
                result = result.concat("\nResult is--->" + sentence.getResult()+"\n");
                result = result.concat("\nReplace is--->" + sentence.getReplacements().toString()+"\n");
                for (Sentence sentence1 : sentence.getItemLists()) {
                    result = result.concat("\n        ---------------------------   list sentence start   --------------------\n");
                    result = result.concat("\n        Origin is--->" + sentence1.getOrigin()+"\n");
                    result = result.concat("\n        Result is--->" + sentence1.getResult()+"\n");
                    result = result.concat("\n        Replace is--->" + sentence1.getReplacements().toString()+"\n");
                    result = result.concat("\n        ----------------------------   list sentence end   ---------------------\n");
                }
                result = result.concat("\n-----------------------   sentence end   ----------------------------\n");
            }
            result  = result.concat("\n------------------------   block end   -----------------------------\n\n");
        }
        return result;
    }

    public FR getFR(String name, String title, String des){
        FR fr = new FR();
        fr.setSystemName(name);
        fr.setTitle(title);
        fr.setBlockItems(parseExe(des));
        return fr;
    }
}
