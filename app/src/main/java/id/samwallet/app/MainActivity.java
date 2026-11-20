package id.samwallet.app;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.Typeface;
import android.widget.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    final int NAVY=0xFF172D3B, ORANGE=0xFFF29A38, BG=0xFFF4F6F8, MUTED=0xFF687782;
    DB db; TextView balance,income,expense,budgetText,history;
    String[] cats={"Makanan","Transportasi","Kebutuhan pribadi","Tagihan","Hiburan","Kesehatan","Lainnya"};
    NumberFormat rupiah=NumberFormat.getCurrencyInstance(new Locale("id","ID"));

    @Override public void onCreate(Bundle b){
        super.onCreate(b); getWindow().setStatusBarColor(NAVY); getWindow().setNavigationBarColor(NAVY);
        db=new DB(this);
        if(!getPreferences(0).contains("pin")) setupPin(); else askPin();
    }
    String money(long n){return rupiah.format(n).replace(",00","");}
    int pinInput(){return 0x00000081;}
    void askPin(){
        final EditText p=new EditText(this); p.setInputType(pinInput()); p.setHint("Masukkan PIN 6 digit");
        new AlertDialog.Builder(this).setTitle("SAM WALLET terkunci")
          .setMessage("Masukkan PIN untuk membuka dompet keuanganmu.").setView(p).setCancelable(false)
          .setPositiveButton("Buka",(d,w)->{
            if(getPreferences(0).getString("pin","").equals(p.getText().toString())) showHome();
            else {toast("PIN salah"); askPin();}
          }).show();
    }
    void setupPin(){
        final EditText p=new EditText(this); p.setInputType(pinInput()); p.setHint("Buat PIN 6 digit");
        new AlertDialog.Builder(this).setTitle("Selamat datang di SAM WALLET")
          .setMessage("Buat PIN 6 digit untuk melindungi aplikasi. Jangan gunakan PIN ATM/rekening.")
          .setView(p).setCancelable(false).setPositiveButton("Simpan",(d,w)->{
            String s=p.getText().toString();
            if(!s.matches("\\d{6}")){toast("PIN harus tepat 6 angka"); setupPin();}
            else {getPreferences(0).edit().putString("pin",s).apply(); showHome();}
          }).show();
    }
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    TextView txt(String s,int size,int color,boolean bold){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if(bold)t.setTypeface(null,Typeface.BOLD); return t;
    }
    Button btn(String s){
        Button b=new Button(this); b.setText(s); b.setTextColor(0xFFFFFFFF);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ORANGE)); return b;
    }
    void showHome(){
        LinearLayout page=new LinearLayout(this); page.setOrientation(1); page.setPadding(0,0,0,18); page.setBackgroundColor(BG);
        ScrollView sc=new ScrollView(this); sc.addView(page); setContentView(sc);
        LinearLayout header=new LinearLayout(this); header.setOrientation(1); header.setPadding(22,24,22,20); header.setBackgroundColor(NAVY);
        header.addView(txt("SAM WALLET",25,0xFFFFFFFF,true));
        header.addView(txt("Kontrol keuangan pribadi",13,0xFFD6E0E6,false)); page.addView(header);

        LinearLayout card=new LinearLayout(this); card.setOrientation(1); card.setPadding(20,18,20,18); card.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.setMargins(16,16,16,8); page.addView(card,cp);
        card.addView(txt("SALDO TERSEDIA",12,MUTED,true)); balance=txt("Rp0",29,NAVY,true); card.addView(balance);
        LinearLayout totals=new LinearLayout(this); totals.setPadding(0,14,0,0);
        income=txt("↑ Masuk Rp0",13,0xFF23845B,true); expense=txt("↓ Keluar Rp0",13,0xFFBE5148,true);
        totals.addView(income,new LinearLayout.LayoutParams(0,-2,1)); totals.addView(expense); card.addView(totals);

        LinearLayout actions=new LinearLayout(this); actions.setPadding(16,8,16,8);
        Button add=btn("+ Catat transaksi"); add.setOnClickListener(v->addTransaction());
        Button budget=btn("Atur anggaran");
        budget.setOnClickListener(v->setBudget());
        actions.addView(add,new LinearLayout.LayoutParams(0,54,1));
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,54,1); bp.setMargins(8,0,0,0); actions.addView(budget,bp); page.addView(actions);

        LinearLayout panel=new LinearLayout(this); panel.setOrientation(1); panel.setPadding(18,16,18,16); panel.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2); pp.setMargins(16,8,16,8); page.addView(panel,pp);
        panel.addView(txt("Anggaran bulan ini",18,NAVY,true)); budgetText=txt("Belum ada anggaran",14,MUTED,false); budgetText.setPadding(0,8,0,18); panel.addView(budgetText);
        panel.addView(txt("Riwayat transaksi",18,NAVY,true)); history=txt("Belum ada transaksi.",14,MUTED,false); history.setPadding(0,10,0,14); panel.addView(history);
        Button report=btn("Ringkasan & tips keuangan"); report.setOnClickListener(v->showReport()); panel.addView(report);
        Button lock=btn("Kunci aplikasi"); lock.setOnClickListener(v->askPin()); panel.addView(lock);
        refresh();
    }
    void addTransaction(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(1); box.setPadding(12,4,12,0);
        RadioGroup type=new RadioGroup(this); type.setOrientation(0);
        RadioButton out=new RadioButton(this); out.setText("Pengeluaran"); out.setChecked(true);
        RadioButton in=new RadioButton(this); in.setText("Pemasukan"); type.addView(out); type.addView(in); box.addView(type);
        EditText amount=new EditText(this); amount.setHint("Nominal rupiah"); amount.setInputType(2); box.addView(amount);
        EditText note=new EditText(this); note.setHint("Keterangan transaksi"); box.addView(note);
        Spinner cat=new Spinner(this); cat.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,cats)); box.addView(cat);
        new AlertDialog.Builder(this).setTitle("Catat transaksi").setView(box).setNegativeButton("Batal",null)
          .setPositiveButton("Simpan",(d,w)->{
            try{
              long val=Long.parseLong(amount.getText().toString().trim()); if(val<=0)throw new Exception();
              int category=cat.getSelectedItemPosition(); String desc=note.getText().toString().trim();
              if(desc.isEmpty())desc=cats[category];
              db.insert(in.isChecked()?1:0,val,desc,cats[category],System.currentTimeMillis());
              refresh(); toast("Transaksi tersimpan");
            }catch(Exception e){toast("Masukkan nominal yang valid");}
          }).show();
    }
    void setBudget(){
        EditText e=new EditText(this); e.setInputType(2); e.setHint("Batas pengeluaran per bulan (Rp)");
        new AlertDialog.Builder(this).setTitle("Anggaran bulanan")
          .setMessage("Tentukan batas total pengeluaran untuk bulan berjalan.").setView(e)
          .setNegativeButton("Batal",null).setPositiveButton("Simpan",(d,w)->{
            try{long x=Long.parseLong(e.getText().toString()); if(x<1)throw new Exception();
              getPreferences(0).edit().putLong("budget",x).apply(); refresh();
            }catch(Exception ex){toast("Nominal anggaran tidak valid");}
          }).show();
    }
    void refresh(){
        long ins=db.sum(1),outs=db.sum(0),monthOut=db.sumThisMonth(0),bud=getPreferences(0).getLong("budget",0);
        balance.setText(money(ins-outs)); income.setText("↑ Masuk "+money(ins)); expense.setText("↓ Keluar "+money(outs));
        if(bud>0){
            long remain=bud-monthOut;
            budgetText.setText("Batas: "+money(bud)+"\nTerpakai bulan ini: "+money(monthOut)+"\nSisa: "+money(remain)+(remain<0?" — anggaran terlampaui!":""));
            budgetText.setTextColor(remain<0?0xFFBE5148:MUTED);
        }else budgetText.setText("Belum ada anggaran. Tekan Atur anggaran untuk memulai.");
        Cursor c=db.getReadableDatabase().rawQuery("SELECT note,amount,type,category,created FROM tx ORDER BY created DESC LIMIT 15",null);
        StringBuilder sb=new StringBuilder(); SimpleDateFormat df=new SimpleDateFormat("dd MMM yyyy, HH:mm",new Locale("id","ID"));
        while(c.moveToNext()){
            sb.append(c.getInt(2)==1?"+ ":"− ").append(money(c.getLong(1))).append(" • ").append(c.getString(0)).append("\n");
            sb.append(c.getString(3)).append(" · ").append(df.format(new Date(c.getLong(4)))).append("\n\n");
        }
        c.close(); if(sb.length()==0)sb.append("Belum ada transaksi. Catat transaksi pertamamu.");
        history.setText(sb.toString());
    }
    void showReport(){
        long ins=db.sum(1),outs=db.sum(0),monthOut=db.sumThisMonth(0),bud=getPreferences(0).getLong("budget",0);
        String advice=outs>ins?"Pengeluaran lebih besar daripada pemasukan. Tinjau transaksi yang tidak wajib."
          :(bud>0&&monthOut>bud?"Pengeluaran bulan ini melewati anggaran. Kurangi belanja non-prioritas."
          :"Pertahankan kebiasaan mencatat. Periksa kategori yang paling sering menghabiskan uang.");
        new AlertDialog.Builder(this).setTitle("Ringkasan keuangan")
          .setMessage("Total pemasukan: "+money(ins)+"\nTotal pengeluaran: "+money(outs)+"\nSelisih: "+money(ins-outs)
            +"\nPengeluaran bulan ini: "+money(monthOut)+"\n\nSaran SAM WALLET:\n"+advice)
          .setPositiveButton("Tutup",null).show();
    }
    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"samwallet.db",null,1);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE tx(id INTEGER PRIMARY KEY AUTOINCREMENT,type INTEGER,amount INTEGER,note TEXT,category TEXT,created INTEGER)");}
        public void onUpgrade(SQLiteDatabase d,int oldV,int newV){}
        void insert(int type,long amount,String note,String category,long created){
            ContentValues v=new ContentValues(); v.put("type",type); v.put("amount",amount); v.put("note",note); v.put("category",category); v.put("created",created);
            getWritableDatabase().insert("tx",null,v);
        }
        long sum(int type){
            Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM tx WHERE type=?",new String[]{String.valueOf(type)});
            c.moveToFirst(); long n=c.getLong(0); c.close(); return n;
        }
        long sumThisMonth(int type){
            Calendar cal=Calendar.getInstance(); cal.set(Calendar.DAY_OF_MONTH,1); cal.set(Calendar.HOUR_OF_DAY,0); cal.set(Calendar.MINUTE,0); cal.set(Calendar.SECOND,0); cal.set(Calendar.MILLISECOND,0);
            Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM tx WHERE type=? AND created>=?",new String[]{String.valueOf(type),String.valueOf(cal.getTimeInMillis())});
            c.moveToFirst(); long n=c.getLong(0); c.close(); return n;
        }
    }
}
