package com.twofromkt.ecomap.map_activity.choose_type_panel;

import android.animation.Animator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.twofromkt.ecomap.R;
import com.twofromkt.ecomap.map_activity.MapActivity;

public class ChooseTypePanel extends LinearLayout {

    MapActivity parentActivity;
    RelativeLayout panel;
    ImageButton[] typeButtons;
    ImageButton openButton;

    boolean animating, showing;
    boolean[] chosenTypes;
    float panelOffset;

    public ChooseTypePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.choose_type_panel, this);
    }

    public void attach(MapActivity parentActivity) {
        this.parentActivity = parentActivity;
        panel = (RelativeLayout) findViewById(R.id.choose_type_panel);
        openButton = (ImageButton) findViewById(R.id.show_choose_type_panel);
        typeButtons = new ImageButton[3];
        chosenTypes = new boolean[3];
        typeButtons[0] = (ImageButton) findViewById(R.id.type_button1);
        typeButtons[1] = (ImageButton) findViewById(R.id.type_button2);
        typeButtons[2] = (ImageButton) findViewById(R.id.type_button3);
        animating = showing = false;
        panel.setVisibility(INVISIBLE);

        setListeners();
    }

    private void setListeners() {
        openButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animating) return;
                if (panel.getVisibility() == INVISIBLE) {
                    panelOffset = panel.getX();
                    panel.setX(-panel.getWidth());
                    panel.setVisibility(VISIBLE);
                }
                ViewPropertyAnimator animator = panel.animate();
                animator.setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        animating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animating = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                int duration = 400;
                animator.setDuration(duration);
                ViewPropertyAnimator buttonAnumator = openButton.animate();
                buttonAnumator.setDuration(duration);
                animator.xBy((showing ? -1 : 1) * (panel.getWidth() + panelOffset));
                buttonAnumator.rotationBy((showing ? 1 : -1) * 90);
                showing = !showing;
            }
        });

        final int[] imageIds = {R.mipmap.trashbox_icon, R.mipmap.cafes_icon, R.mipmap.other_icon};
        final int[] imageIdsChosen = {R.mipmap.trashbox_icon_chosen,
                R.mipmap.cafes_icon_chosen, R.mipmap.other_icon_chosen};

        for (int i = 0; i < 3; i++) {
            final int index = i;
            typeButtons[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton thisButton = (ImageButton) v;
                    thisButton.setImageBitmap(BitmapFactory.decodeResource(
                            getResources(),
                            chosenTypes[index] ? imageIds[index] : imageIdsChosen[index]));
                    chosenTypes[index] = !chosenTypes[index];
                    parentActivity.searchBar.setChosen(index, chosenTypes[index], true);
                }
            });
        }
    }
}