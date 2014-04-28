package com.arphen.ownspritz.app;

import android.text.Html;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class Blinker {
    private Book m_book;
    private ArrayList<String[]> m_chapters;
    private double m_wpm = 500;
    private boolean m_init = false;
    private ArrayList<OnChapterChangedListener> chapterChangedListeners;
    public Blinker() {
        chapterChangedListeners= new ArrayList<OnChapterChangedListener>();
    }
    public void addChapterChangedListener(OnChapterChangedListener listener){
        chapterChangedListeners.add(listener);
    }
    public String getTitle(){
        return m_book.getMetadata().getTitles().get(0);

    }
    public String getAuthor(){
        return m_book.getMetadata().getAuthors().get(0).toString();
    }
public void init(InputStream epubInputStream) throws IOException {
    m_book = (new EpubReader()).readEpub(epubInputStream);
    m_chapters = new ArrayList<String[]>(m_book.getSpine().getSpineReferences().size());
    Log.i("Main", String.valueOf(m_book.getSpine().getSpineReferences().size()));
    for(int i=0;i<m_book.getSpine().getSpineReferences().size();i++)
        m_chapters.add(null);
    m_init=true;
    lazy_load_chapter(0,0);
    for(int i=1;i<m_book.getSpine().getSpineReferences().size();i++)
        lazy_load_chapter(i, 3000);

}
    public static List<String> splitEqually(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }

    /**
     * This Method will get the word at the specified chapter and word index.
     * Any necessary loading of chapters will be done here.
     * Objects using this method need to take care of empty string cases and request the next chapter.
     * @param p_chapter The requested chapter.
     * @param p_word The requested word.
     * @return If the word index exceeds the current chapter length an empty String will be returned.
     */
    public String getWord(int p_chapter,int p_word) {
        try {
            try {
                return m_chapters.get(p_chapter)[p_word];
            } catch (NullPointerException e) {
                Log.e("Blinker", "Need to fetch chapter" + String.valueOf(p_chapter));
                while(m_chapters.get(p_chapter)==null){
                    try {
                        Log.e("Blinker", "Sleeping while waiting");
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                String k = m_chapters.get(p_chapter)[p_word];
                return k;
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e("Blinker", "Out of bounds: Chapter "+ String.valueOf(p_chapter)+" Pos "+String.valueOf(p_word));
        }
        return "";
    }

    public int getLengthOfChapter(int chapter) {
        return m_chapters.get(chapter).length;
    }

    public int getBooklength() {
        return m_chapters.size();
    }

    public void m_logBook() {
        Log.i("Blinker", "Book author: " + m_book.getMetadata().getAuthors());
        Log.i("Blinker", "Book title: " + m_book.getMetadata().getTitles());
    }



    private String[] breakUpWords(String[] what) {
        ArrayList<String> cont = new ArrayList<String>(Arrays.asList(what));
        for (int i = 0; i < cont.size(); i++) {
            String word = cont.remove(i);
            if (word.length() > 12) {
                for (int start = 0; start < word.length(); start += 7) {
                    cont.addAll(i, splitEqually(word, 7));
                }
            }
        }
        return cont.toArray(new String[cont.size()]);
    }
    public void loadChapter(int c){
        if(!m_init){
            Log.e("Blinker","Trying to load chapter without init");
            return ;
        }
        if(m_chapters.get(c)!=null){
            Log.e("Blinker","Chapter"+String.valueOf(c)+" already loaded.");
            return ;
        }
        Log.i("Blinker","Chapter "+String.valueOf(c)+" loading.");
        Resource resource = m_book.getSpine().getSpineReferences().get(c).getResource();
        String decoded = null;
        try {
            decoded = new String(resource.getData(), resource.getInputEncoding());
        } catch (IOException e) {
            Log.e("Error","Failed to decode");
            e.printStackTrace();
        }
        if (decoded.contains("<body")) {
            decoded = decoded.substring(decoded.indexOf("<body"));
        } else {
            decoded = " ";
        }
        decoded = Html.fromHtml(decoded).toString().replaceAll("(?s)<!--.*?-->", "");
        m_chapters.set(c,decoded.split("\\s"));
        Log.i("Blinker","Chapter "+String.valueOf(c)+" loaded.");
    }
    public boolean isM_init(){return m_init;}

    /**
     * function to lazily load a chapter.
     */
    void lazy_load_chapter(final int c,final int delay) {

        Thread temp = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                    if (m_chapters.get(c) == null) {
                        loadChapter(c);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Blinker", e.toString());
                }
            }
        });
        temp.start();
    }

}
