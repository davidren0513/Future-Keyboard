package com.liquidglass.ime;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/** Compact author details, project attribution and bundled license text. */
public final class AboutActivity extends Activity {
    private LinearLayout content;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xFF101C24);
        scroll.setOnApplyWindowInsetsListener((v,insets)->{
            if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}
            else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;
        });
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(24),dp(12),dp(24),dp(24));scroll.addView(content);setContentView(scroll);
        TextView back=text("返回设置",14,GlassStyle.MINT);back.setMinHeight(dp(44));back.setGravity(Gravity.CENTER_VERTICAL);back.setOnClickListener(v->finish());content.addView(back);
        LinearLayout brand=new LinearLayout(this);brand.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon=new ImageView(this);icon.setImageResource(R.mipmap.ic_launcher);icon.setContentDescription("Future Keyboard 图标");brand.addView(icon,new LinearLayout.LayoutParams(dp(64),dp(64)));
        LinearLayout name=new LinearLayout(this);name.setOrientation(LinearLayout.VERTICAL);name.setPadding(dp(14),0,0,0);name.addView(text("Future Keyboard",20,GlassStyle.INK));name.addView(text("版本 "+version(),12,0xFF9CB2BA));brand.addView(name);content.addView(brand);
        section("作者与公司");
        detail("作者","David Ren");detail("公司","南通未界科技有限公司");
        detail("公司官网","nt-future.tech");
        content.addView(text("永不联网 · 隐私输入法",12,GlassStyle.MINT));
        section("鸣谢开源项目");
        content.addView(text("感谢以下项目及其作者、贡献者。地址仅作文字展示，应用不会联网或打开浏览器。",12,0xFF9CB2BA));
        credit("雾凇拼音 · Rime Ice","iDvel 与贡献者 · 中文词库与英文单词表 · GPL-3.0","https://github.com/iDvel/rime-ice");
        credit("Material Design Icons","Google 与贡献者 · 界面图标 · Apache-2.0","https://github.com/google/material-design-icons");
        credit("Android Open Source Project","AOSP 社区 · Android 输入法与窗口基础框架","https://github.com/aosp-mirror/platform_frameworks_base");
        credit("Gradle","Gradle 与贡献者 · 构建工具 · Apache-2.0","https://github.com/gradle/gradle");
        TextView licenses=text("开源许可与词库来源",13,GlassStyle.MINT);licenses.setMinHeight(dp(48));licenses.setGravity(Gravity.CENTER_VERTICAL);licenses.setOnClickListener(v->licenseMenu());content.addView(licenses);
        content.addView(text("词库的更早数据来源与作者信息保留于随附的词库来源说明。",11,0xFF8BA5AD));
    }
    private int dp(float n){return GlassStyle.dp(this,n);}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(5),0,dp(5));t.setLineSpacing(dp(2),1);return t;}
    private void section(String title){TextView t=text(title,15,GlassStyle.INK);t.setTypeface(null,Typeface.BOLD);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(20);content.addView(t,lp);}
    private void detail(String title,String value){content.addView(text(title,11,0xFF8BA5AD));TextView t=text(value,14,GlassStyle.INK);t.setAutoLinkMask(0);t.setLinksClickable(false);content.addView(t);}
    private void credit(String title,String author,String url){
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(0,dp(8),0,dp(8));
        row.addView(text(title,14,GlassStyle.INK));row.addView(text(author,11,0xFF9CB2BA));TextView address=text(url,11,0xFF8BA5AD);address.setAutoLinkMask(0);address.setLinksClickable(false);row.addView(address);content.addView(row);
        View divider=new View(this);divider.setBackgroundColor(0xFF263A43);content.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));
    }
    private String version(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(android.content.pm.PackageManager.NameNotFoundException e){return "";}}
    private void licenseMenu(){
        String[] titles={"雾凇拼音 · GPL-3.0","词库来源与原作者说明","Material Design Icons · Apache-2.0","词库版本与固定提交","英文词表来源与作者"};
        String[] files={"rime-ice-GPL-3.0.txt","rime-ice-sources.txt","material-icons.txt","dictionary-provenance.json","en-source.txt"};
        new AlertDialog.Builder(this).setTitle("开源许可与来源").setItems(titles,(dialog,which)->showLicense(titles[which],files[which])).setNegativeButton("关闭",null).show();
    }
    private void showLicense(String title,String file){
        io.execute(()->{String body;try(InputStream stream=getAssets().open("licenses/"+file);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            byte[] buffer=new byte[4096];int n;while((n=stream.read(buffer))!=-1)bytes.write(buffer,0,n);body=bytes.toString(StandardCharsets.UTF_8.name());
        }catch(IOException e){body="暂时无法读取许可文件。";}String result=body;
            runOnUiThread(()->{if(isFinishing()||isDestroyed())return;TextView copy=text(result,12,GlassStyle.INK);copy.setTypeface(Typeface.MONOSPACE);copy.setTextIsSelectable(true);copy.setPadding(dp(20),dp(8),dp(20),dp(8));ScrollView scroll=new ScrollView(this);scroll.addView(copy);new AlertDialog.Builder(this).setTitle(title).setView(scroll).setPositiveButton("关闭",null).show();});
        });
    }
    @Override protected void onDestroy(){io.shutdown();super.onDestroy();}
}
