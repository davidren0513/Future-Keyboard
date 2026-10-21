package com.liquidglass.ime;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Offline word completion and lexical continuations. Never reads editor history. */
public final class WordSuggestions {
    private final List<String> english=new ArrayList<>();
    private final Map<String,List<String>> englishNext=new HashMap<>();
    private ByteBuffer chinese;
    private int count,records;
    public void loadEnglish(InputStream words,InputStream pairs)throws IOException{
        try(BufferedReader r=new BufferedReader(new InputStreamReader(words,StandardCharsets.UTF_8))){String s;while((s=r.readLine())!=null)if(!s.isEmpty())english.add(s);}
        try(BufferedReader r=new BufferedReader(new InputStreamReader(pairs,StandardCharsets.UTF_8))){String s;while((s=r.readLine())!=null){String[] f=s.split("\t");if(f.length==2)englishNext.computeIfAbsent(f[0].toLowerCase(Locale.ROOT),k->new ArrayList<>()).add(f[1]);}}
    }
    public void mapChinese(FileChannel channel,long offset,long length)throws IOException{chinese=channel.map(FileChannel.MapMode.READ_ONLY,offset,length).order(ByteOrder.BIG_ENDIAN);if(chinese.getInt(0)!=0x4c474e31)throw new IOException("Unknown continuation format");count=chinese.getInt(4);records=8+count*4;}
    private String record(int i){int p=records+chinese.getInt(8+i*4),n=chinese.getInt(p);byte[] b=new byte[n];ByteBuffer v=chinese.duplicate();v.position(p+4);v.get(b);return new String(b,StandardCharsets.UTF_8);}
    public List<String> completeEnglish(String prefix){List<String> out=new ArrayList<>();String lower=prefix.toLowerCase(Locale.ROOT);if(lower.isEmpty())return out;for(String word:english)if(word.toLowerCase(Locale.ROOT).startsWith(lower)&&!word.equalsIgnoreCase(prefix)){if(prefix.equals(prefix.toUpperCase(Locale.ROOT))&&prefix.length()>1)word=word.toUpperCase(Locale.ROOT);else if(Character.isUpperCase(prefix.charAt(0)))word=Character.toUpperCase(word.charAt(0))+word.substring(1);out.add(word);if(out.size()==24)break;}return out;}
    public List<String> next(String previous,boolean zh){if(previous.isEmpty())return Collections.emptyList();if(!zh)return new ArrayList<>(englishNext.getOrDefault(previous.toLowerCase(Locale.ROOT),Collections.emptyList()));if(chinese==null)return Collections.emptyList();int lo=0,hi=count;while(lo<hi){int mid=(lo+hi)>>>1;String s=record(mid);String key=s.substring(0,s.indexOf('\t'));if(key.compareTo(previous)<0)lo=mid+1;else hi=mid;}if(lo==count)return Collections.emptyList();String s=record(lo);String[] f=s.split("\t");if(!f[0].equals(previous))return Collections.emptyList();return new ArrayList<>(Arrays.asList(f).subList(1,f.length));}
}
