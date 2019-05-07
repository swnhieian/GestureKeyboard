package com.shiweinan.gesturekeyboard;

import android.graphics.Point;
import android.graphics.PointF;
import android.content.Context;
import android.content.SharedPreferences;
import  android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.util.Random;
import androidx.preference.PreferenceManager;

public class Lexicon {
    /*from former individual class PathCalc
    * */
    private static final int SampleSize = 32;
    private static final float eps = 1e-6f;
    private static final float inf = 1e10f;
    private static final float DTWWindowConst = 0.1f;
    private static int[] DTWL = new int[SampleSize + 1], DTWR = new int[SampleSize + 1];
    private static float[][] dtw = new float[SampleSize + 1][];
    private static float keyboardWidth, keyboardHeight;
    private static float endOffset = 3.0f, keyWidth = 0f, keyHeight = 0f, radius = 0, radiusMul = 0.20f;
    public enum Formula{
        Basic, MinusR, DTW, Null, End,
    }
    public enum Mode{
        Basic, FixStart, AnyStart, End,
    }
    public enum UserStudy{
        Basic, Study1_Train, Study1, Study2, End,
    }

    public  Context context;
    public SharedPreferences sharedPreferences;
    private static final int CandidatesNum = 500;
    private Candidate[] cands = new Candidate[CandidatesNum], candsTmp_0 = new Candidate[CandidatesNum];
    private Candidate[] candsTmp_1 = new Candidate[CandidatesNum];
    private Candidate[] candsTmp_2 = new Candidate[CandidatesNum];
    private Candidate[] candsTmp_3 = new Candidate[CandidatesNum];
    private Candidate[] candsTmp_4 = new Candidate[CandidatesNum];
    //private List<Candidate> history =  new ArrayList<>();
    public String text = "", under = "";

    //Constants
    private static final int LexiconSize = 10000;
    private static final int RadialNum = 5;
    private static final float AnyStartThr = 2.5f;

    public static PointF StartPoint;
    public static PointF StartPointRelative = new PointF(0f, 0f);

    //Parameters
    public static boolean isCut = false;
    public static boolean useRadialMenu = true;
    private Map<String, Float> katzAlpha = new HashMap<String, Float>();
    private Map<String, Float> bigramMap = new HashMap<String, Float>();
    //Internal Variables
    private String[] words;
    private int panel;
    private List<PointF> keyPos = new ArrayList<>();

    private List<String> phrase = new ArrayList<>();
    private List<Integer> phraseList = new ArrayList<>();

    private List<Entry> dict_0 = new ArrayList<>(); //size of 10000
    private int[] dictSize = {10000,5000,2000,1000,500};

    //from former class Parameter
    //private Mode mode = Mode.FixStart; //for AnyStart, it should be dealt with extra care in the method recognize
    private Mode mode = Mode.Basic;
    public static Formula locationFormula = Formula.DTW;
    //public static Formula locationFormula = Formula.MinusR;

    Lexicon(Context NewContext){
        context = NewContext;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        initDTW();
        setGeometry();
        Start();
    }

    void initDTW(){
        int w = (int)(SampleSize * DTWWindowConst);
        for (int i = 0; i <= SampleSize; ++i)
        {
            dtw[i] = new float[SampleSize + 1];
            DTWL[i] = Math.max(i - w, 0);
            DTWR[i] = Math.min(i + w, SampleSize);
            for (int j = 0; j <= SampleSize; ++j)
                dtw[i][j] = Float.MAX_VALUE;
        }
        dtw[0][0] = 0;
    }
    void setGeometry()
    {
        keyboardWidth = Integer.parseInt(sharedPreferences.getString("kbd_width", "267"));
        keyboardHeight = Integer.parseInt(sharedPreferences.getString("kbd_height", "130"));
        keyWidth = keyboardWidth * 0.1f;
        keyHeight = keyboardHeight * 0.22f;
        Log.i("keyboardWidth for recognize result:", Float.toString(keyboardWidth));
        Log.i("keyboardHeight for recognize result:", Float.toString(keyboardHeight));
        //keyWidth = 25;
        //keyHeight = 40;
        ChangeRadius(0);
        ChangeEndOffset(0);
    }
    public void ChangeRadius(float delta)
    {
        if (radiusMul + delta <= eps)
            return;
        radiusMul += delta;
        radius = keyWidth * radiusMul;
    }

    public void ChangeEndOffset(float delta)
    {
        if (endOffset + delta <= 0)
            return;
        endOffset += delta;
    }

    void Start(){
        //history.clear();
        //ChangeCandidatesChoose(false);
        //SetRadialMenuDisplay(false);
        CalcKeyLayout();
        CalcLexicon();
        InitPhrases();
    }

    void CalcKeyLayout(){
        keyPos.add(new PointF(31 * keyboardWidth/267,66 * keyboardHeight/130));
        keyPos.add(new PointF(159 * keyboardWidth/267,106 * keyboardHeight/130));
        keyPos.add(new PointF(108 * keyboardWidth/267,106 * keyboardHeight/130));
        keyPos.add(new PointF(83 * keyboardWidth/267,66 * keyboardHeight/130));
        keyPos.add(new PointF(70 * keyboardWidth/267,26 * keyboardHeight/130));
        keyPos.add(new PointF(109 * keyboardWidth/267,66 * keyboardHeight/130));
        keyPos.add(new PointF(134 * keyboardWidth/267,66 * keyboardHeight/130));
        keyPos.add(new PointF(159 * keyboardWidth/267,66 * keyboardHeight/130));
        keyPos.add(new PointF(198 * keyboardWidth/267,26 * keyboardHeight/130));
        keyPos.add(new PointF(184 * keyboardWidth/267,66 * keyboardHeight/130));
        keyPos.add(new PointF(210 * keyboardWidth/267,66 * keyboardHeight/130));
        keyPos.add(new PointF(235 * keyboardWidth/267,66 * keyboardHeight/130));
        keyPos.add(new PointF(210 * keyboardWidth/267,106 * keyboardHeight/130));
        keyPos.add(new PointF(184 * keyboardWidth/267,106 * keyboardHeight/130));
        keyPos.add(new PointF(223 * keyboardWidth/267,26 * keyboardHeight/130));
        keyPos.add(new PointF(248 * keyboardWidth/267,26 * keyboardHeight/130));
        keyPos.add(new PointF(20 * keyboardWidth/267,26 * keyboardHeight/130));
        keyPos.add(new PointF(95 * keyboardWidth/267,26 * keyboardHeight/130));
        keyPos.add(new PointF(57 * keyboardWidth/267,66 * keyboardHeight/130));
        keyPos.add(new PointF(122 * keyboardWidth/267,26 * keyboardHeight/130));
        keyPos.add(new PointF(172 * keyboardWidth/267,26 * keyboardHeight/130));
        keyPos.add(new PointF(133 * keyboardWidth/267,106 * keyboardHeight/130));
        keyPos.add(new PointF(45 * keyboardWidth/267,26 * keyboardHeight/130));
        keyPos.add(new PointF(83 * keyboardWidth/267,106 * keyboardHeight/130));
        keyPos.add(new PointF(147 * keyboardWidth/267,26 * keyboardHeight/130));
        keyPos.add(new PointF(58 * keyboardWidth/267,106 * keyboardHeight/130));
        StartPoint = new PointF(147 * keyboardWidth/267,26 * keyboardHeight/130);//Y
    }

    void CalcLexicon() {
        InputStream inputStream = context.getResources().openRawResource(R.raw.anc_written_noduplicatepluspangram);
        //InputStream inputStream = context.getResources().openRawResource(R.raw.bigrams_ldc_10k_katz_1m_phrase);
        List<String> lines = new ArrayList<>();
        try {
            if (inputStream != null) {
                InputStreamReader inputreader = new InputStreamReader(inputStream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                while ((line = buffreader.readLine()) != null) {
                    lines.add(line);
                }
                inputStream.close();
            }
        } catch (java.io.FileNotFoundException e) {
            Log.d("ANC_written_clean", "The File doesn't not exist.");
        } catch (IOException e) {
            Log.d("ANC_written_clean", e.getMessage());
        }

        //int bigramsNum = Integer.parseInt(lines.get(0));
        /* below are for bigram
        /*
        if (bigramMap.size() == 0) {
            for (int i = 1; i <= bigramsNum; ++i) {
                String[] data = lines.get(i).split(" ");
                bigramMap.put(data[0] + ' ' + data[1], Float.parseFloat(data[2]));
            }
        }
        */

        //int unigramsNum = Integer.parseInt(lines.get(bigramsNum + 1));
        /*unigramsNum shrinks half fours time*/
        int unigramsNum = dictSize[0];
        Log.d("Unigram", "Unigram: " + unigramsNum);
        dict_0.clear();

        for (int i = 0; i < unigramsNum; ++i) {
            //String[] data = lines.get(i + bigramsNum + 2).split(" ");
            String[] data = lines.get(i).split(" ");

            /* below are for unigram's katzAlpha. Since the "pre" word in this project is fixed, we do not need pre's frequency from katzAlpha.
            /*
            if (!katzAlpha.containsKey(data[0]))
                katzAlpha.put(data[0], Float.parseFloat(data[2]));
            */
            if (data[0].equals("<s>"))
                continue;
            Entry entry = new Entry(data[0], Long.parseLong(data[1]), this);
            /*save entry in dicts of different size*/
            dict_0.add(entry);

        }


    }

    void InitPhrases()
    {
        InputStream inputStream = context.getResources().openRawResource(R.raw.pangrams_phrases);
        List<String> lines=new ArrayList<>();
        try {
            if (inputStream != null) {
                InputStreamReader inputreader = new InputStreamReader(inputStream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                while ((line = buffreader.readLine()) != null) {
                    lines.add(line);
                }
                inputStream.close();
            }
        }
        catch (java.io.FileNotFoundException e) {
            Log.d("pangrams_phrases", "The File doesn't not exist.");
        }
        catch (IOException e) {
            Log.d("pangrams_phrases", e.getMessage());
        }
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            if (line.length() <= 1)
                continue;
            if (line.charAt(line.length() - 1) < 'a' || line.charAt(line.length() - 1) > 'z')
                line = line.substring(0, line.length() - 1);
            phrase.add(line);
            phraseList.add(i);
        }
        Log.d("Phrases", "Phrases: "+phrase.size());
        SetPhraseList(UserStudy.Study1);
        ChangePhrase(-3);
    }


    public void SetPhraseList(UserStudy study)
    {
        Random random = new Random();
        for (int i = 0; i < phrase.size(); ++i)
            phraseList.set(i,i);
        for (int i = 2; i < phrase.size(); ++i)
        {
            int j = random.nextInt(phrase.size())%(phrase.size()-2) + 2;
            int k = phraseList.get(i);
            phraseList.set(i,phraseList.get(j));
            phraseList.set(j,k);
        }
        if (study == UserStudy.Study2)
        {
            for (int b = 1; b < 8; ++b)
                phraseList.set(b * 10, phraseList.get(1));
        }
    }


    public void ChangePhrase() {
        ChangePhrase(-1);
    }

    public void ChangePhrase(int id) {
        if (id == -3)
            words = "a problem with the engine".split(" ");
            //textManager.SetPhrase("a problem with the engine");
        else if (id == -1) {
            Random random = new Random();
            words = phrase.get(random.nextInt(phrase.size())).split(" ");
            //textManager.SetPhrase(phrase.get(random.nextInt(phrase.size())));
        } else
            words = phrase.get(phraseList.get(id)).split(" ");
        //textManager.SetPhrase(phrase[phraseList[id]]);
        //words = textManager.GetWords();
        Clear();
    }

    public String ChangeWord(int id) {
        Entry entry = dict_0.get(id);
        Log.i("change word for recognize result to:", entry.word);
        return entry.word;
    }

    public void Clear() {
        for (int i = 0; i < CandidatesNum; ++i) {
            //btn[i].GetComponentInChildren<Text>().text = "";
            if (cands[i] != null)
                cands[i].word = "";
        }
        /*for (int i = 0; i < RadialNum; ++i)
            radialText[i].text = "";*/
        /*for (int i = 0; i < CandidatesNum; ++i)
            listText[i].text = "";*/
        //SetRadialMenuDisplay(false);
        //gesture.chooseCandidate = false;
        //history.clear();
        //textManager.Clear();
    }

    public static float Distance(PointF a, PointF b) {
        return (float)Math.hypot(a.x-b.x, a.y-b.y);
    }

    public static List<PointF> temporalSampling(List<PointF> stroke){
        float length = 0;
        int count = stroke.size();

        List<PointF> gesture = new ArrayList<>(SampleSize);

        if (count == 1)
        {
            for (int i = 0; i < SampleSize; ++i)
                gesture.add(i,new PointF(stroke.get(0).x, stroke.get(0).y));
            return gesture;
        }

        for (int i = 0; i < count - 1; ++i)
            length += Distance(stroke.get(i),stroke.get(i+1));
        float increment = length / (SampleSize - 1);

        PointF last = new PointF(stroke.get(0).x, stroke.get(0).y);
        float distSoFar = 0;
        int id = 1, vecID = 1;

        gesture.add(0, new PointF(stroke.get(0).x, stroke.get(0).y));

        while (id < count) {
            float dist = Distance(last, stroke.get(id));
            if (distSoFar + dist >= increment) {
                float ratio = (increment - distSoFar) / dist;
                last.x = last.x + ratio * (stroke.get(id).x - last.x);
                last.y = last.y + ratio * (stroke.get(id).y - last.y);
                gesture.add(vecID++, new PointF(last.x, last.y));
                distSoFar = 0;
            } else {
                distSoFar += dist;
                last = stroke.get(id++);
            }
        }

        for (int i = vecID; i < SampleSize; ++i)
            gesture.add(i, new PointF(stroke.get(count-1).x, stroke.get(count-1).y));

        return gesture;
    }

    public static List<PointF> normalize(List<PointF> pts){
        if (pts == null)
            return null;
        float minX = 1f, minY = 1f;
        float maxX = -1f, maxY = -1f;

        PointF center = new PointF(0,0);
        int size = pts.size();
        for (int i = 0; i < size; ++i)
        {
            center.x += pts.get(i).x;
            center.y += pts.get(i).y;
            minX = Math.min(minX, pts.get(i).x);
            maxX = Math.max(maxX, pts.get(i).x);
            minY = Math.min(minY, pts.get(i).y);
            maxY = Math.max(maxY, pts.get(i).y);
        }
        center.x = center.x / size;
        center.y = center.y / size;
        float ratio = 1.0f / Math.max(maxX - minX, maxY - minY);
        List<PointF> nPts = new ArrayList<>(size);
        PointF temp = new PointF(0,0);
        for (int i = 0; i < size; ++i){
            temp.x = (pts.get(i).x - center.x) * ratio;
            temp.y = (pts.get(i).y - center.y) * ratio;
            nPts.add(i, new PointF(temp.x, temp.y));
        }
        return nPts;
    }

    public static float match(List<PointF> A, List<PointF> B, Formula formula){
        return match(A, B, formula, inf, false);
    }
    public static float match(List<PointF> A, List<PointF> B, Formula formula, float threshold){
        return match(A, B, formula, threshold, false);
    }
    public static float match(List<PointF> A, List<PointF> B, Formula formula,
                              float threshold, boolean isShape){
        if (A.size() != B.size() || formula == Formula.Null)
            return inf;
        /*if (Vector2.Distance(A[0], B[0]) > KeyWidth)
			return 0;*/
        float dis = 0;

        switch (formula)
        {
            case Basic:
                for (int i = 0; i < SampleSize; ++i)
                {
                    dis += Distance(A.get(i),B.get(i));
                }
                break;
            case MinusR:
                for (int i = 0; i < SampleSize; ++i)
                {
                    dis += Math.max(0, Distance(A.get(i),B.get(i)) - radius);
                }
                break;
            case DTW:
                for (int i = 0; i < SampleSize; ++i)
                {
                    float gap = Float.MAX_VALUE;
                    for (int j = DTWL[i]; j < DTWR[i]; ++j)
                    {
                        dtw[i + 1][j + 1] =  Distance(A.get(i),B.get(j))+Math.min(dtw[i][j], Math.min(dtw[i][j + 1], dtw[i + 1][j]));
                        gap = Math.min(gap, dtw[i+1][j+1]);
                    }
                    if (gap > threshold)
                        return inf;
                }
                dis = dtw[SampleSize][SampleSize];
                break;
        }
        return dis / SampleSize / keyboardWidth;
        /*if (!isShape)
            return Mathf.Exp(-0.5f * dis * dis / Parameter.radius / Parameter.radius);
        else
            return Mathf.Exp(-0.5f * dis * dis / (Parameter.radiusMul * 0.1f) / (Parameter.radiusMul * 0.1f));*/
    }

    public class Entry
    {
        public String word;
        public long frequency;
        public List<PointF> pts = new ArrayList<>();
        public List<List<PointF>> locationSample = new ArrayList<>();
        public List<List<PointF>> shapeSample = new ArrayList<>();
        public Entry(String word, long frequency, Lexicon lexicon)
        {
            this.word = word;
            this.frequency = frequency;
            int key = -1;

            for (int i = 0; i < word.length(); ++i)
            {
                key = word.charAt(i) - 'a';
                pts.add(new PointF(lexicon.keyPos.get(key).x, lexicon.keyPos.get(key).y));
            }

            locationSample.add(0,temporalSampling(pts));

            boolean ins = false;

            if (Distance(pts.get(0), StartPoint) > eps)
                ins = true;
            if (ins)
                pts.add(0, StartPoint);
            locationSample.add(1,temporalSampling(pts));
            if (ins)
                pts.remove(0);

            for(int i = 0; i < Mode.AnyStart.ordinal(); ++i)
                shapeSample.add(i,normalize(locationSample.get(i)));
            /*
            for(int i = 0; i < Mode.End.ordinal(); ++i)
                shapeSample.add(i,normalize(locationSample.get(i)));
            */
        }
    }

    public class Candidate
    {
        public String word;
        public float location, shape, language, confidence;
        Candidate(String word_)
        {
            word = word_;
            confidence = -inf;
        }
        public Candidate(Candidate x)
        {
            word = x.word;
            location = x.location;
            shape = x.shape;
            confidence = x.confidence;
        }
    }

    public List<String> recognize(List<PointF> rawStroke, String word) {

        List<String> ret = new ArrayList<>();
        List<PointF> stroke = temporalSampling(rawStroke);

        for (int i = 0; i < CandidatesNum; ++i) {
            candsTmp_0[i] = new Candidate("");
            candsTmp_1[i] = new Candidate("");
            candsTmp_2[i] = new Candidate("");
            candsTmp_3[i] = new Candidate("");
            candsTmp_4[i] = new Candidate("");
        }

        //int h = history.size();
        for (int ii = 0; ii < dictSize[0]; ++ii)
        {
            Entry entry = dict_0.get(ii);
            if (rawStroke.size() == 1 && entry.word.length() != 1)
                continue;
            Candidate newCandidate = new Candidate(entry.word);

            if (mode == Mode.AnyStart) {
                if (Distance(entry.locationSample.get(0).get(0), stroke.get(0)) > AnyStartThr * keyWidth)
                    continue;
                entry.pts.add(0, stroke.get(0));
                entry.locationSample.set(mode.ordinal(), temporalSampling(entry.pts));
                entry.shapeSample.set(mode.ordinal(), normalize(entry.locationSample.get(mode.ordinal())));
                entry.pts.remove(0);
            }
            /*
            if (Distance(stroke.get(SampleSize - 1), entry.locationSample.get(mode.ordincal()).get(SampleSize - 1))
                    > endOffset * keyWidth && (h >= words.length || entry.word != words[h]))*/
            if (Distance(stroke.get(SampleSize - 1), entry.locationSample.get(mode.ordinal()).get(SampleSize - 1))
                    > endOffset * keyWidth)
                continue;

            float biF = 0;

            biF = entry.frequency;
            if (biF == 0)
                continue;

            newCandidate.language = (float)Math.log(biF);
            newCandidate.location = match(stroke, entry.locationSample.get(mode.ordinal()), locationFormula,
                    (newCandidate.language - candsTmp_0[CandidatesNum - 1].confidence) / 100 * keyboardWidth * SampleSize);
            if (newCandidate.location == inf)
                continue;

            newCandidate.confidence = newCandidate.language - 100 * newCandidate.location;
            //if (newCandidate.confidence < candsTmp[CandidatesNum - 1].confidence)
            //continue;

            for (int i = 0; i < CandidatesNum; ++i)
                if (newCandidate.confidence > candsTmp_0[i].confidence)
                {
                    for (int j = CandidatesNum - 1; j > i; j--)
                        candsTmp_0[j] = candsTmp_0[j-1];
                    candsTmp_0[i] = newCandidate;
                    break;
                }

            if (ii<dictSize[1])
            {
                newCandidate.location = match(stroke, entry.locationSample.get(mode.ordinal()), locationFormula,
                        (newCandidate.language - candsTmp_1[CandidatesNum - 1].confidence) / 100 * keyboardWidth * SampleSize);
                if (newCandidate.location == inf)
                    continue;

                newCandidate.confidence = newCandidate.language - 100 * newCandidate.location;

                for (int i = 0; i < CandidatesNum; ++i)
                    if (newCandidate.confidence > candsTmp_1[i].confidence)
                    {
                        for (int j = CandidatesNum - 1; j > i; j--)
                            candsTmp_1[j] = candsTmp_1[j-1];
                        candsTmp_1[i] = newCandidate;
                        break;
                    }
            }
            if (ii<dictSize[2])
            {
                for (int i = 0; i < CandidatesNum; ++i)
                    if (newCandidate.confidence > candsTmp_2[i].confidence)
                    {
                        newCandidate.location = match(stroke, entry.locationSample.get(mode.ordinal()), locationFormula,
                                (newCandidate.language - candsTmp_2[CandidatesNum - 1].confidence) / 100 * keyboardWidth * SampleSize);
                        if (newCandidate.location == inf)
                            continue;

                        newCandidate.confidence = newCandidate.language - 100 * newCandidate.location;
                        for (int j = CandidatesNum - 1; j > i; j--)
                            candsTmp_2[j] = candsTmp_2[j-1];
                        candsTmp_2[i] = newCandidate;
                        break;
                    }
            }
            if (ii<dictSize[3])
            {
                for (int i = 0; i < CandidatesNum; ++i)
                    if (newCandidate.confidence > candsTmp_3[i].confidence)
                    {
                        newCandidate.location = match(stroke, entry.locationSample.get(mode.ordinal()), locationFormula,
                                (newCandidate.language - candsTmp_3[CandidatesNum - 1].confidence) / 100 * keyboardWidth * SampleSize);
                        if (newCandidate.location == inf)
                            continue;

                        newCandidate.confidence = newCandidate.language - 100 * newCandidate.location;

                        for (int j = CandidatesNum - 1; j > i; j--)
                            candsTmp_3[j] = candsTmp_3[j-1];
                        candsTmp_3[i] = newCandidate;
                        break;
                    }
            }
            if (ii<dictSize[4])
            {
                for (int i = 0; i < CandidatesNum; ++i)
                    if (newCandidate.confidence > candsTmp_4[i].confidence)
                    {
                        newCandidate.location = match(stroke, entry.locationSample.get(mode.ordinal()), locationFormula,
                                (newCandidate.language - candsTmp_4[CandidatesNum - 1].confidence) / 100 * keyboardWidth * SampleSize);
                        if (newCandidate.location == inf)
                            continue;

                        newCandidate.confidence = newCandidate.language - 100 * newCandidate.location;

                        for (int j = CandidatesNum - 1; j > i; j--)
                            candsTmp_4[j] = candsTmp_4[j-1];
                        candsTmp_4[i] = newCandidate;
                        break;
                    }
            }
        }
        /*
        if (h < words.length)
            for (int i = 0; i < CandidatesNum; ++i)
                if (candsTmp_0[i].word == words[h].toLowerCase())
                    candsTmp_0[i].word = words[h];
        */
        ret.add(word);
        Log.i("recognize result for word:", word);
        String dictInfo = Integer.toString(dictSize[0])+","+Integer.toString(dictSize[1])+","+Integer.toString(dictSize[2])+","+Integer.toString(dictSize[3])+","+Integer.toString(dictSize[4]);
        Log.i("recognize dict size: ", dictInfo);
        for (int i = 0; i < CandidatesNum; ++i){
            if (candsTmp_0[i].word.equals(word)) {
                ret.add("0:");
                //ret.add(candsTmp_0[i].word);
                ret.add(Integer.toString(i));
                Log.i("recognize result 0:", Integer.toString(i));
                break;
            }
        }
        for (int i = 0; i < CandidatesNum; ++i){
            if (candsTmp_1[i].word.equals(word)) {
                ret.add("1:");
                //ret.add(candsTmp_0[i].word);
                ret.add(Integer.toString(i));
                Log.i("recognize result 1:", Integer.toString(i));
                break;
            }
        }
        for (int i = 0; i < CandidatesNum; ++i){
            if (candsTmp_2[i].word.equals(word)) {
                ret.add("2:");
                //ret.add(candsTmp_0[i].word);
                ret.add(Integer.toString(i));
                Log.i("recognize result 2:", Integer.toString(i));
                break;
            }
        }
        for (int i = 0; i < CandidatesNum; ++i){
            if (candsTmp_3[i].word.equals(word)) {
                ret.add("3:");
                //ret.add(candsTmp_0[i].word);
                ret.add(Integer.toString(i));
                Log.i("recognize result 3:", Integer.toString(i));
                break;
            }
        }
        for (int i = 0; i < CandidatesNum; ++i){
            if (candsTmp_4[i].word.equals(word)) {
                ret.add("4:");
                //ret.add(candsTmp_0[i].word);
                ret.add(Integer.toString(i));
                Log.i("recognize result 4:", Integer.toString(i));
                break;
            }
        }

        /*
        for (Entry entry : dict_0)
        {
            if (rawStroke.size() == 1 && entry.word.length() != 1)
                continue;
            Candidate newCandidate = new Candidate(entry.word);

            if (mode == Mode.AnyStart) {
                if (Distance(entry.locationSample.get(0).get(0), stroke.get(0)) > AnyStartThr * keyWidth)
                    continue;
                entry.pts.add(0, stroke.get(0));
                entry.locationSample.set(mode.ordinal(), temporalSampling(entry.pts));
                entry.shapeSample.set(mode.ordinal(), normalize(entry.locationSample.get(mode.ordinal())));
                entry.pts.remove(0);
            }
            if (Distance(stroke.get(SampleSize - 1), entry.locationSample.get(mode.ordinal()).get(SampleSize - 1))
                    > endOffset * keyWidth && (h >= words.length || entry.word != words[h]))
                continue;

            float biF = 0;
            /*
            if (bigramMap.containsKey(pre + ' ' + entry.word))
                biF = bigramMap.get(pre + ' ' + entry.word);
            else
            {
                biF = katzAlpha.get(pre) * entry.frequency;
                if (biF == 0)
                    continue;
            }
            */
            //biF = katzAlpha.get(pre) * entry.frequency;
        /*
            biF = entry.frequency;
            if (biF == 0)
                continue;

            newCandidate.language = (float)Math.log(biF);
            newCandidate.location = match(stroke, entry.locationSample.get(mode.ordinal()), locationFormula,
                    (newCandidate.language - candsTmp[CandidatesNum - 1].confidence) / 100 * keyboardWidth * SampleSize);
            if (newCandidate.location == inf)
                continue;

            newCandidate.confidence = newCandidate.language - 100 * newCandidate.location;
            //if (newCandidate.confidence < candsTmp[CandidatesNum - 1].confidence)
            //continue;
            for (int i = 0; i < CandidatesNum; ++i)
                if (newCandidate.confidence > candsTmp[i].confidence)
                {
                    for (int j = CandidatesNum - 1; j > i; j--)
                        candsTmp[j] = candsTmp[j-1];
                    candsTmp[i] = newCandidate;
                    break;
                }
        }
        if (h < words.length)
            for (int i = 0; i < CandidatesNum; ++i)
                if (candsTmp[i].word == words[h].toLowerCase())
                    candsTmp[i].word = words[h];

        for (int i = 0; i < CandidatesNum; ++i){
            if (candsTmp[i].word.equals("good")) {
                ret.add(candsTmp[i].word);
                ret.add(Integer.toString(i));
                break;
            }
        }
        */
        /*
        for (int i = 0; i < 5; ++i){
            ret.add(i,candsTmp[i].word);
        }
        */
        /*
        ret.add(dict.get(0).word);
        ret.add(dict.get(1).word);
        ret.add(dict.get(2).word);
        */
        //ret.add(Float.toString(stroke.get(0).x));
        //ret.add(Float.toString(stroke.get(0).y));
        return ret;
    }
}

