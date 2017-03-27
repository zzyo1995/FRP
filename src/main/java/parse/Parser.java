package main.java.parse;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.*;
import main.java.bean.FeatureRequestOL;
import main.java.bean.Replacement;
import main.java.bean.Sentence;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zzyo on 2017/3/17.
 */
public class Parser {

    private Map<String, Replacement> replacementMap = new HashMap<>();
    private ArrayList<ArrayList<Integer>> blocks = new ArrayList<>();
    private ArrayList<Sentence> sentences = new ArrayList<>();

    public Parser() {
    }

    public Map<String, Replacement> getReplacementMap() {
        return replacementMap;
    }

    public ArrayList<String> parseBlock(String origin) {
        ArrayList<String> blockList = new ArrayList<>(Arrays.asList(origin.split("<\\$BLOCK-END\\$>")));
        for (int i = 0; i < blockList.size(); i++) {
            /*if (blockList.get(i).matches("^<\\$CODE-.+?>$")) {
                String tmp = blockList.get(i - 1);
                blockList.set(i - 1, tmp.concat("\n" + blockList.get(i) + "\n"));
                blockList.remove(i);
            }*/
            String tmp = blockList.get(i);
            tmp = tmp.replaceAll("(?<=<)\\$", "");
            tmp = tmp.replaceAll("\\$(?=\\d{0,2}>)", "");
            blockList.set(i, tmp);
        }
        for (String block : blockList) {
            System.out.println("block--->" + block);
        }
        System.out.println("------------------------------------------------");
        return blockList;
    }

    public void parseSentences(ArrayList<String> blocks) {
        int sIndex = 0, bIndex = 0;
        for (String str : blocks) {
            ArrayList<Integer> block = new ArrayList<>();
            this.blocks.add(block);
            Reader reader = new StringReader(str);
            DocumentPreprocessor dp = new DocumentPreprocessor(reader);
            ArrayList<String> sentences = new ArrayList<>();
            for (List<HasWord> words : dp) {
                String sentence = "";
                for (HasWord word : words) {
                    sentence = sentence.concat(" " + word.word());
                }
                sentences.add(sentence);
            }
            // 处理孤立的LIST,CODE
            for (int i = 0; i < sentences.size(); i++) {
                String sen = sentences.get(i);
                if (sen.matches("^\\s*<LIST\\d+.*")) {
                    sentences.set(i - 1, sentences.get(i - 1).concat(sen));
                    sentences.remove(i);
                } else if (sen.matches("^\\s*<CODE-.*")) {
                    sentences.set(i - 1, sentences.get(i - 1).concat(sen));
                    sentences.remove(i);
                }
            }
            for (String sen : sentences) {
                String tmp = sen;
                Sentence sentence = new Sentence();
                Pattern pattern = Pattern.compile("<LIST\\d+>");
                Matcher matcher = pattern.matcher(sen);
                while (matcher.find()){
                    String list = matcher.group(0);
                    Sentence sList = new Sentence();
                    String origin = this.replacementMap.get(list).getOrigin();
                    sList.setOrigin(origin);
                    sList.setResult(origin);
                    sentence.addItemList(sList);
                    tmp = tmp.replace(list, "\n* "+origin);
                }
                sentence.setOrigin(tmp);
                sentence.setResult(sen.replaceAll("<LIST\\d+>", ""));
                block.add(sIndex);
                this.blocks.set(bIndex, block);
                this.sentences.add(sentence);
                sIndex++;
            }
            bIndex++;
        }
    }

    /*public void parseSentencesTMP(ArrayList<String> blocks) {
        int sIndex = 0, bIndex = 0;
        for (String str : blocks) {
            ArrayList<Integer> block = new ArrayList<>();
            this.blocks.add(block);
            String[] sentences = {};
            // can not parse sentences like "the price is 2.3 is a number."
            sentences = str.split("(?<!\\s[A-Z])((\\.|\\?|\\!)((\\s*)|(?=[A-Z])))|(\\n)");
            for (String tmp : sentences) {
                System.out.println(tmp);
            }
            ArrayList<String> sentenceArrayList = new ArrayList<>(Arrays.asList(sentences));
            //System.out.println("length is :" + sentences.length);
            for (int i = 0; i < sentenceArrayList.size(); i++) {
                Sentence sentence = new Sentence();
                //System.out.println("sentence --->" + sentenceArrayList.get(i));
                if (sentenceArrayList.get(i).matches("^<LIST>.*")) {
                    if (sentenceArrayList.get(i - 1).matches(".*:$")) {
                        // 如果是  str：\n 1，～2.～3.～ 修改上个sentence:(addItmList,setOrigin)
                        //System.out.println("LISTS START:------->");
                        for (int j = i; j < sentenceArrayList.size(); j++) {
                            if (sentenceArrayList.get(j).matches("^<LIST>.*")) {
                                String list = sentenceArrayList.get(j).replaceFirst("<LIST>", "");
                                Sentence itemList = new Sentence();
                                itemList.setOrigin(list);
                                itemList.setResult(list);
                                //sentence = blockItem.getSentences().get(blockItem.getSentences().size() - 1);
                                sentence = this.sentences.get(this.sentences.size() - 1);
                                sentence.setOrigin(sentence.getOrigin().concat("\n" + list));
                                sentence.addItemList(itemList);
                                this.sentences.set(this.sentences.size() - 1, sentence);
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
                    //blockItem.addSentence(sentence);
                    // add sentence id in block array list
                    block.add(sIndex);
                    this.blocks.set(bIndex, block);
                    this.sentences.add(sentence);
                    sIndex++;
                }
            }
            //blockItems.add(blockItem);
            bIndex++;
        }
    }*/

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
        //System.out.println("Str is--->" + str);
        String[] results = getTokens(str);
        for (int i = 0; i < results.length; i++) {
            //System.out.println("Result is--->" + results[i] + "Token is--->" + token);
            token = getTokens(token)[0];
            if (token.equals(results[i])) {
                //System.out.println("TokenIndex is--->" + i);
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
        //Pattern pattern = Pattern.compile("<\\$.+?\\$\\d{0,2}>");
        Pattern pattern = Pattern.compile("<[a-zA-Z-\\s]+\\d+>");
        Matcher matcher = pattern.matcher(origin);
        while (matcher.find()) {
            flag = 1;
            String type = matcher.group(0);
            Replacement replacement = this.replacementMap.get(type);
            //System.out.println(replacement.toString());
            String content = type.replaceAll("\\$", "");
            tmp = tmp.replaceFirst(Matcher.quoteReplacement(type), content);
            int tokenIndex = getTokenIndex(tmp, content);
            //System.out.println("TYPE is--->" + type + "TMP is --->" + tmp);
            //System.out.println("Token index is--->" + tokenIndex);
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
        result = result.replaceAll("(?<=[a-zA-Z])\\d{0,2}>", ">");
        sentence.setResult(result);
        return sentence;
    }

    public void parseReplacement() {
        for (Sentence sen : this.sentences) {
            System.out.println(sen.getOrigin());
        }
        // change sentence.origin to origin text
        //set Replacement List in Sentence
        // set index of replacement
        for (int i = 0; i < this.sentences.size(); i++) {
            Sentence sentence = this.sentences.get(i);
            this.sentences.set(i, updateSentence(sentence));
        }
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
            String type = "<$CODE-" + codeType.toUpperCase() + "$" + codeIndex + ">";
            Replacement replacement = new Replacement();
            replacement.setOrigin(originCode);
            replacement.setReplacement("<CODE-" + codeType.toUpperCase() + ">");
            this.replacementMap.put("<CODE-" + codeType.toUpperCase() + codeIndex + ">", replacement);
            //code.text(codeType);
            //System.out.println(originCode+"\n"+codeType);
            //　认定code是BLOCK的结束
            code.parent().after(type + "<$BLOCK-END$>\n");
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
            this.replacementMap.put("<LINK-HTTP" + linkIndex + ">", replacement);
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

    public String parseList(String origin) {
        //origin = origin.replaceAll("<li>(.+)?<\\/li>", "<list>");
        int lIndex = 0;
        Pattern pattern = Pattern.compile("<li>(.+)?<\\/li>");
        Matcher matcher = pattern.matcher(origin);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String str = matcher.group(0);
            System.out.println("STR--->" + str);
            String list = matcher.group(1);
            System.out.println("LIST--->" + list);
            String type = "<LIST" + lIndex + ">";
            matcher.appendReplacement(sb, type);
            Replacement replacement = new Replacement();
            replacement.setOrigin(list);
            this.replacementMap.put(type, replacement);
            lIndex++;
        }
        matcher.appendTail(sb);
        origin = sb.toString();
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
                    "(([C-Z]:|(\\.){0,2}\\\\)?(((\\w*-|\\w*\\.)*\\w*)\\\\)*((\\w*-|\\w*\\.)*\\w*)))\\." + fileTypes[index] + "(?!\\.[a-z])";
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
                this.replacementMap.put("<FILE-" + fileTypes[index].toUpperCase() + fileIndex + ">", replacement);
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
            this.replacementMap.put("<PATH" + pathIndex + ">", replacement);
            matcher.appendReplacement(sb, type);
            pathIndex++;
        }
        matcher.appendTail(sb);
        origin = sb.toString();
        sb.setLength(0);
        return origin;
    }

    public String parseHtmlToText(String origin) {
        origin = origin.replaceAll("<p><\\/p>", "");
        origin = origin.replaceAll("<br>", "");
        origin = origin.replaceAll("<p><br><\\/p>", "");
        // replace space and \n
        origin = origin.replaceAll("(?<=[^$]>)\\s*(?=<)", "");
        //System.out.println("replace space：" + origin);
        //origin = origin.replaceAll("<li>", "<LIST>");
        // list 结束即BLOCK结束
        origin = origin.replaceAll("<\\/[o,u]l>", "<\\$BLOCK-END\\$>\n");     // 记得去掉换行
        //　</p> 后是list/code 不能替换BLOCK-END
        origin = origin.replaceAll("<\\/p>(?!(<ol>)|(<\\$CODE-))", "<\\$BLOCK-END\\$>\n");      // 记得去掉换行
        //origin = origin.replaceAll("<\\/li>", "\n");        // 记得去掉换行
        origin = origin.replaceAll("<[\\/]?[a-z]+>", "");
        origin = origin.replaceAll("&nbsp;", " ");
        // 去掉前面空格和空行
        origin = origin.replaceAll("(?<=\n|^)(\\s*)", "");
        // 去掉最后的换行
        origin = origin.replaceAll("\n$", "");
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
            this.replacementMap.put("<QUOTE" + quoteIndex + ">", replacement);
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
            this.replacementMap.put("<EMAIL" + emailIndex + ">", replacement);
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
                    this.replacementMap.put("<" + replace[i] + "" + shortIndex + ">", replacement);
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

    public void parseExe(String raw) {
        String tmp = parseCode(raw);
        tmp = parseLink(tmp);
        tmp = parseList(tmp);
        tmp = parseEmail(tmp);
        tmp = parseQuote(tmp);
        tmp = parseShort(tmp);
        tmp = parseFile(tmp);
        tmp = parsePath(tmp);
        tmp = parseHtmlToText(tmp);
        ArrayList<String> blocks = parseBlock(tmp);
        parseSentences(blocks);
        parseReplacement();
    }

    public String printResult(FeatureRequestOL fr) {
        String result = "";
        result = result.concat("\n----------------   parsing start   -----------------------------\n");
        int sIndex = 0;
        ArrayList<Sentence> sentences = fr.getFullSentences();
        ArrayList<ArrayList<Integer>> blocks = fr.getBlocks();

        for (int i = 0; i < blocks.size(); i++) {
            result = result.concat("\n------------------   block " + i + " start   --------------------------------\n\n");
            for (int j = 0; j < blocks.get(i).size(); j++) {
                result = result.concat("\n--------------------   sentence " + blocks.get(i).get(j) + " start   -------------------------\n");
                result = result.concat("\nOrigin is--->" + sentences.get(sIndex).getOrigin() + "\n");
                result = result.concat("\nResult is--->" + sentences.get(sIndex).getResult() + "\n");
                result = result.concat("\nReplace is--->" + sentences.get(sIndex).getReplacements().toString() + "\n");
                for (Sentence sentence1 : sentences.get(sIndex).getItemLists()) {
                    result = result.concat("\n        ---------------------------   list sentence start   --------------------\n");
                    result = result.concat("\n        Origin is--->" + sentence1.getOrigin() + "\n");
                    result = result.concat("\n        Result is--->" + sentence1.getResult() + "\n");
                    result = result.concat("\n        Replace is--->" + sentence1.getReplacements().toString() + "\n");
                    result = result.concat("\n        ----------------------------   list sentence end   ---------------------\n");
                }
                sIndex++;
                result = result.concat("\n-----------------------   sentence " + blocks.get(i).get(j) + " end   ----------------------------\n");
            }
            result = result.concat("\n------------------------   block " + i + " end   -----------------------------\n\n");
        }
        return result;
    }

    public FeatureRequestOL getFR(String name, String title, String des) {
        //fr.setBlockItems(parseExe(des));
        parseExe(des);
        FeatureRequestOL fr = new FeatureRequestOL(name, title, this.blocks, this.sentences);
        return fr;
    }
}
