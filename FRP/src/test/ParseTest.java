package test;

import main.java.parse.Parser;
import org.junit.Test;

/**
 * Created by zzyo on 2017/3/17.
 */
public class ParseTest {

    private String test = "<p>Hi! Json export not good for Cyrillic in unicode.If I add JSON_UNESCAPED_UNICODE&nbsp;</p><p><br></p><p><br></p><pre style=\"max-width: 100%;\"><code class=\"php hljs\" codemark=\"1\"> <span class=\"hljs-keyword\">if</span> (<span class=\"hljs-keyword\">isset</span>($GLOBALS[<span class=\"hljs-string\">'json_pretty_print'</span>])\n" +
            "                &amp;&amp; $GLOBALS[<span class=\"hljs-string\">'json_pretty_print'</span>]\n" +
            "            ) {\n" +
            "                $encoded = json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);\n" +
            "            } <span class=\"hljs-keyword\">else</span> {\n" +
            "                $encoded = json_encode($data, JSON_UNESCAPED_UNICODE);&nbsp;<span style=\"font-size: inherit;\">}</span></code></pre><p>all exported beautifully.<a href=\"http://www.baidu.com\" target=\"_blank\">linkExample</a>&nbsp; &nbsp; &nbsp; &nbsp; /home/user/file.sql</p><p><br></p><p><br></p><p>C:\\project\\classes</p><p><br></p>";


    private String test2 = "<p>Hi!&nbsp;\"Json&nbsp;export\"&nbsp;not&nbsp;good&nbsp;for&nbsp;Cyrillic&nbsp;in&nbsp;unicode.</p>\n" +
            "<p>If&nbsp;I&nbsp;add&nbsp;JSON_UNESCAPED_UNICODE&nbsp;all&nbsp;exported&nbsp;beautifully.</p><p><br></p><pre style=\"max-width: 100%;\"><code class=\"php hljs\" codemark=\"1\"> <span class=\"hljs-keyword\">if</span> (<span class=\"hljs-keyword\">isset</span>($GLOBALS[<span class=\"hljs-string\">'json_pretty_print'</span>])\n" +
            "                &amp;&amp; $GLOBALS[<span class=\"hljs-string\">'json_pretty_print'</span>]\n" +
            "            ) {\n" +
            "                $encoded = json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);\n" +
            "            } <span class=\"hljs-keyword\">else</span> {\n" +
            "                $encoded = json_encode($data, JSON_UNESCAPED_UNICODE);\n" +
            "            }\n" +
            "\t\t\t</code></pre><p><br></p><p>../home/user/file1.sql &nbsp; \'file example:\'C:\\Windows\\System32-calc\\calc.exe &nbsp;/home/user/file.sql &nbsp; &nbsp;./home/user/file.java &nbsp; &nbsp;C:\\project\\classes</p><pre style=\"max-width: 100%;\"><code class=\"javascript hljs\" codemark=\"1\"> <span class=\"hljs-function\"><span class=\"hljs-keyword\">function</span> <span class=\"hljs-title\">submit</span>(<span class=\"hljs-params\"></span>) </span>{\n" +
            "        <span class=\"hljs-comment\">// 获取编辑器区域完整html代码</span>\n" +
            "        <span class=\"hljs-keyword\">var</span> html = editor.$txt.html();\n" +
            "        <span class=\"hljs-comment\">/*html = html.replace(/\\s+/g, \"\");*/</span>\n" +
            "        <span class=\"hljs-comment\">//alert(html);</span>\n" +
            "        $.base64.utf8encode = <span class=\"hljs-literal\">true</span>;\n" +
            "        html = $.base64().encode(html,<span class=\"hljs-string\">\"utf8\"</span>);\n" +
            "        <span class=\"hljs-comment\">//alert(html);</span>\n" +
            "        <span class=\"hljs-comment\">//document.write(html);</span>\n" +
            "        html = <span class=\"hljs-string\">\"title=\"</span>+$(<span class=\"hljs-string\">'#title'</span>).val()+<span class=\"hljs-string\">\"&amp;data=\"</span>+html+<span class=\"hljs-string\">\"&amp;type=\"</span>+$(<span class=\"hljs-string\">'#type'</span>).val();\n" +
            "        $.ajax(\n" +
            "                {\n" +
            "                    type: <span class=\"hljs-string\">\"POST\"</span>,\n" +
            "                    url: <span class=\"hljs-string\">\"/edit.html\"</span>,\n" +
            "                    data: html,\n" +
            "                    dataType:<span class=\"hljs-string\">'Text'</span>,\n" +
            "                    success: <span class=\"hljs-function\"><span class=\"hljs-keyword\">function</span>(<span class=\"hljs-params\">data</span>) </span>{\n" +
            "                        location.href = data;\n" +
            "                    }\n" +
            "                }\n" +
            "        );<br></code></pre><p><br></p><p><br></p><p><ol><li>move&nbsp;the&nbsp;code&nbsp;which&nbsp;finds&nbsp;$tmp_subdir&nbsp;from&nbsp;file.php&nbsp;to&nbsp;configfile.php</li><li>call&nbsp;this&nbsp;function&nbsp;in&nbsp;both&nbsp;file.php&nbsp;and&nbsp;Encoding.php</li><li>right&nbsp;zzyo1995@qq.com?</li><li>Can&nbsp;i&nbsp;also&nbsp;align&nbsp;the&nbsp;parenthesis&nbsp;a&nbsp;756257660@qq.combit&nbsp;as&nbsp;it&nbsp;doesn't&nbsp;look&nbsp;that&nbsp;good&nbsp;?</li><li>Also&nbsp;I&nbsp;have&nbsp;to&nbsp;move&nbsp;it&nbsp;to&nbsp;ConfigFile.php&nbsp;right&nbsp;?</li></ol><div><br></div><div></div><p>Yes,zzyo@hust.edu.cn&nbsp;C:\\project\\classes;&nbsp;  core.java.util;&nbsp; /ext-name/&nbsp; ./&nbsp; /var/cache/mopidy&nbsp;please&nbsp;use&nbsp;coding&nbsp;style&nbsp;as&nbsp;described&nbsp;in&nbsp;our&nbsp;docs:&nbsp;<p>e.g e.g. i.e .NET Imo </p><a href=\"https://github.com/phpmyadmin/phpmyadmin/wiki/Developer_guidelines#coding-style\" target=\"_blank\">https://github.com/phpmyadmin/phpmyadmin/wiki/Developer_guidelines#coding-style</a><p><br></p>";

    @Test
    public void testCode(){
        System.out.println("---------------------   parse code  -----------------------------\n");
        Parser parser = new Parser();
        String tmp = parser.parseCode(test2);
        System.out.println(tmp);
        System.out.println("---------------------   parse link  -----------------------------\n");
        tmp = parser.parseLink(tmp);
        System.out.println(tmp);
        System.out.println("---------------------   parse email  ------------------------------\n");
        tmp = parser.parseEmail(tmp);
        System.out.println(tmp);
        System.out.println("---------------------   parse quote  ------------------------------\n");
        tmp = parser.parseQuote(tmp);
        System.out.println(tmp);
        System.out.println("---------------------   parse shorts  ------------------------------\n");
        tmp = parser.parseShort(tmp);
        System.out.println(tmp);
        System.out.println("---------------------   parse file  ------------------------------\n");
        tmp = parser.parseFile(tmp);
        System.out.println(tmp);
        System.out.println("---------------------   parse path  ------------------------------\n");
        tmp = parser.parsePath(tmp);
        System.out.println(tmp);
        System.out.println("---------------------   parse html&list  ------------------------------\n");
        tmp = parser.parseHtmlToText(tmp);
        System.out.println(tmp);
        System.out.println("---------------------   parse block  ------------------------------\n");
        String[] blocks = parser.parseBlock(tmp);
        System.out.println(blocks.toString());
    }
}
