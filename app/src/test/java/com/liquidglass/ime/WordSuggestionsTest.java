package com.liquidglass.ime;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
public class WordSuggestionsTest {
 private WordSuggestions load()throws Exception{WordSuggestions w=new WordSuggestions();w.loadEnglish(new FileInputStream("src/main/assets/en-words.txt"),new FileInputStream("src/main/assets/en-next.tsv"));try(FileInputStream f=new FileInputStream("src/main/assets/zh-next.bin")){w.mapChinese(f.getChannel(),0,f.getChannel().size());}return w;}
 @Test public void englishCompletionAndCase()throws Exception{WordSuggestions w=load();assertTrue(w.completeEnglish("hel").contains("hello"));assertTrue(w.completeEnglish("Hel").contains("Hello"));assertTrue(w.completeEnglish("HEL").contains("HELLO"));assertFalse(w.completeEnglish("hello").contains("hello"));assertTrue(w.completeEnglish("").isEmpty());assertTrue(w.completeEnglish("zzzzzzzzzz").isEmpty());}
 @Test public void nextWordsAreSuffixesOnly()throws Exception{WordSuggestions w=load();assertTrue(w.next("good",false).contains("morning"));assertTrue(w.next("GOOD",false).contains("morning"));assertTrue(w.next("北京",true).contains("大学"));assertFalse(w.next("北京",true).contains("北京大学"));assertTrue(w.next("zzzzzz",true).isEmpty());assertTrue(w.next("",true).isEmpty());}
}
