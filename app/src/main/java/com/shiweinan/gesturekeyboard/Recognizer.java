package com.shiweinan.gesturekeyboard;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class Recognizer {

    public List<String> recognize(List<PointF> gesture) {
        List<String> ret = new ArrayList<>();
        ret.add("a");
        ret.add("b");
        ret.add(gesture.get(0).x+","+gesture.get(0).y);
        return ret;
    }
}
