package com.guarda.ethereum.managers;


import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.guarda.ethereum.R;

public class AnimatorHelper {

    public static void fadeInAnimation(View view){
        Animation startAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in_animation);
        view.startAnimation(startAnimation);
    }

    public static void fadeOutAnimation(View view){
        Animation startAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out_animation);
        view.startAnimation(startAnimation);
    }
}
