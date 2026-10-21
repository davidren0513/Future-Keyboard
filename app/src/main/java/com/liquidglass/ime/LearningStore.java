package com.liquidglass.ime;

import android.content.Context;
import android.util.AtomicFile;
import java.io.*;

/** App-private atomic storage. All callers perform disk work on a background thread. */
final class LearningStore {
    private static final Object LOCK=new Object();
    private static AtomicFile file(Context c){return new AtomicFile(new File(c.getFilesDir(),"personal-phrases.tsv"));}
    static void load(Context c,PinyinEngine engine) throws IOException {
        synchronized(LOCK){AtomicFile f=file(c);if(!f.getBaseFile().exists()){engine.clearLearning();return;}engine.readLearning(f.openRead());}
    }
    static void record(Context c,PinyinEngine engine,String spelling,String text) throws IOException {
        record(c,engine,spelling,text,c.getSharedPreferences("glass",Context.MODE_PRIVATE).getLong("learning_epoch",0));
    }
    static void record(Context c,PinyinEngine engine,String spelling,String text,long epoch) throws IOException {
        synchronized(LOCK){if(epoch!=c.getSharedPreferences("glass",Context.MODE_PRIVATE).getLong("learning_epoch",0))return;load(c,engine);engine.learn(spelling,text,System.currentTimeMillis());save(c,engine);}
    }
    private static void save(Context c,PinyinEngine engine)throws IOException {
        AtomicFile f=file(c);FileOutputStream stream=null;
        try{stream=f.startWrite();engine.writeLearning(stream);f.finishWrite(stream);}catch(IOException e){if(stream!=null)f.failWrite(stream);throw e;}
    }
    static void clear(Context c){synchronized(LOCK){android.content.SharedPreferences p=c.getSharedPreferences("glass",Context.MODE_PRIVATE);p.edit().putLong("learning_epoch",p.getLong("learning_epoch",0)+1).apply();file(c).delete();}}
}
