package com.ku.voltset;

import android.view.animation.Animation;
import android.util.FloatMath;
import android.view.animation.Animation;
import android.view.animation.Transformation;
public class RotateDownMeter extends Animation {
	private int totalBlinks;
    private boolean finishOff;
    public RotateDownMeter(int totalBlinks, boolean finishOff) {
        this.totalBlinks = totalBlinks;
        this.finishOff = finishOff;
    }
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float period = interpolatedTime * totalBlinks * 3.14f + (finishOff ? 3.14f / 2 : 0);
        t.setAlpha(Math.abs(FloatMath.cos(period)));
    }
    @Override
    public boolean willChangeBounds() {
        return false;
    }

    @Override
    public boolean willChangeTransformationMatrix() {
        return false;
    }
}
