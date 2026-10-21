package com.liquidglass.ime;
import org.junit.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;
public class PinyinEngineTest {
    private PinyinEngine engine() throws Exception {
        PinyinEngine e=new PinyinEngine();e.load(new ByteArrayInputStream(("ceshi\t测试\tce shi\t200\nni\t你\tni\t100\nhao\t好\thao\t100\nnihao\t你好\tni hao\t1000\nnihaoma\t你好吗\tni hao ma\t500\nceshi\t测视\tce shi\t10\n").getBytes(StandardCharsets.UTF_8)));return e;
    }
    @Test public void exactCandidateComesBeforeCompletions() throws Exception { assertEquals("你好",engine().candidates("nihao").get(0).text); }
    @Test public void partialSyllableCompletes() throws Exception { assertEquals("测试",engine().candidates("ces").get(0).text); }
    @Test public void longCompositionPreservesUnconsumedInput() throws Exception { PinyinEngine.Candidate c=engine().candidates("nihaoceshizzz").get(0);assertEquals("你好",c.text);assertEquals(5,c.consumed); }
    @Test public void apostropheConsumptionUsesOriginalOffsets() throws Exception { PinyinEngine.Candidate c=engine().candidates("ni'hao'zzz").get(0);assertEquals("你好",c.text);assertEquals(6,c.consumed); }
    @Test public void unknownAndEmptyHaveNoCandidate() throws Exception {assertTrue(engine().candidates("zzz").isEmpty());assertTrue(engine().candidates("").isEmpty());}
    @Test public void numericKeysDecodePinyinRatherThanInsertDigits() throws Exception {PinyinEngine.Candidate c=engine().candidates(PinyinEngine.t9("nihao"),true,true).get(0);assertEquals("你好",c.text);assertEquals("nihao",c.spelling);assertEquals(5,c.consumed);}
    @Test public void joinedDictionaryWordsFormNewSentence() throws Exception {assertEquals("你好测试",engine().candidates("nihaoceshi").get(0).text);}
    @Test public void personalChoiceRanksFirstAndSurvivesRestart() throws Exception {
        PinyinEngine a=engine();a.learn("ceshi","测视",42);assertEquals("测视",a.candidates("ceshi").get(0).text);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();a.writeLearning(bytes);PinyinEngine b=engine();b.readLearning(new ByteArrayInputStream(bytes.toByteArray()));assertEquals("测视",b.candidates("ceshi").get(0).text);
    }
    @Test public void privateQueryExcludesPersonalLexicon() throws Exception {PinyinEngine e=engine();e.learn("ceshi","私人词组",42);assertFalse(e.candidates("ceshi",false,false).stream().anyMatch(c->c.text.equals("私人词组")));}
    @Test public void customWordsWorkInBothLayoutsAndCanBeCleared() throws Exception {PinyinEngine e=engine();e.learn("yetaiboli","液态玻璃",42);assertEquals("液态玻璃",e.candidates("yetaiboli").get(0).text);assertEquals("液态玻璃",e.candidates(PinyinEngine.t9("yetaiboli"),true,true).get(0).text);e.clearLearning();assertTrue(e.candidates("yetaiboli").isEmpty());}
    @Test public void realDictionaryCoversEverydayPhrasesAndT9() throws Exception {
        PinyinEngine e=real();String[][] words={{"nihao","你好"},{"gongzuo","工作"},{"weixin","微信"},{"shijian","时间"},{"xiawu","下午"},{"shurufa","输入法"},{"meiguanxi","没关系"},{"pengyouquan","朋友圈"},{"xingqitian","星期天"}};
        for(String[] w:words){if(w[1].isEmpty())continue;assertTrue(w[0],e.candidates(w[0]).stream().anyMatch(c->c.text.equals(w[1])));assertTrue("T9 "+w[0],e.candidates(PinyinEngine.t9(w[0]),true,true).stream().anyMatch(c->c.text.equals(w[1])));}
    }
    @Test public void realDictionaryLookupLatencyIsBounded() throws Exception {
        PinyinEngine e=real();String[] queries={"ni","nihao","jintianxiawukaihui","zhonghuarenmingongheguo", "shurufa"};
        for(String q:queries)e.candidates(q);
        long begin=System.nanoTime();for(int i=0;i<50;i++)for(String q:queries)e.candidates(q);
        double averageMs=(System.nanoTime()-begin)/1e6/250;System.out.println("Average real-dictionary lookup ms: "+averageMs);assertTrue("average ms="+averageMs,averageMs<30);
    }
    @Test public void everyCharacterFollowsPhraseCandidates() throws Exception {
        PinyinEngine e=real();List<PinyinEngine.Candidate> found=e.candidates("shishi");
        Set<String> expected=new HashSet<>();try(BufferedReader r=new BufferedReader(new InputStreamReader(new FileInputStream("src/main/assets/characters.tsv"),StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null){String[] f=line.split("\t");if(f[0].equals("shi"))expected.add(f[1]);}}
        Set<String> actual=new HashSet<>();for(PinyinEngine.Candidate c:found)if(c.text.codePointCount(0,c.text.length())==1){actual.add(c.text);assertEquals("shi",c.spelling);assertEquals(3,c.consumed);}
        assertTrue("complete character tail",actual.containsAll(expected));assertTrue("more than old candidate cap",actual.size()>PinyinEngine.LIMIT);assertTrue(found.get(0).text.length()>1);
    }
    @Test public void selectingCharactersConsumesOnlyOneSyllableAndLearnsWholePhrase() throws Exception {
        PinyinEngine e=real();String raw="ni'hao",text="",spelling="";
        for(String wanted:new String[]{"旎","郝"}){PinyinEngine.Candidate c=e.candidates(raw).stream().filter(v->v.text.equals(wanted)).findFirst().orElseThrow(AssertionError::new);text+=c.text;spelling+=c.spelling;raw=raw.substring(c.consumed).replaceFirst("^'+","");}
        assertEquals("",raw);assertEquals("旎郝",text);assertEquals("nihao",spelling);e.learn(spelling,text,42);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();e.writeLearning(bytes);PinyinEngine restarted=real();restarted.readLearning(new ByteArrayInputStream(bytes.toByteArray()));assertEquals(text,restarted.candidates("nihao").get(0).text);assertEquals(text,restarted.candidates("64426",true,true).get(0).text);assertFalse(restarted.candidates("nihao",false,false).stream().anyMatch(c->c.text.equals("旎郝")));
    }
    @Test public void fullSyllableAndExplicitBoundaryStayDistinct() throws Exception {
        PinyinEngine e=real();assertTrue(e.candidates("xian").stream().anyMatch(c->c.text.equals("先")&&c.consumed==4));assertTrue(e.candidates("xi'an").stream().anyMatch(c->c.text.equals("喜")&&c.consumed==2));assertFalse(e.candidates("xi'an").stream().anyMatch(c->c.text.equals("先")&&c.consumed==2));
    }
    @Test public void t9CharacterAlternativesPreserveCanonicalReading() throws Exception {
        PinyinEngine e=real();PinyinEngine.Candidate c=e.candidates("64426",true,true).stream().filter(v->v.text.equals("旎")).findFirst().orElseThrow(AssertionError::new);assertEquals("ni",c.spelling);assertEquals(2,c.consumed);assertTrue(e.candidates("426",true,true).stream().anyMatch(v->v.text.equals("郝")&&v.consumed==3));
    }
    private PinyinEngine real()throws Exception{PinyinEngine e=new PinyinEngine();try(RandomAccessFile f=new RandomAccessFile("src/main/assets/dictionary.bin","r")){e.map(f.getChannel(),0,f.length());e.loadCharacters(new FileInputStream("src/main/assets/characters.tsv"));}return e;}
}
