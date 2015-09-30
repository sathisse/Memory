package org.tbadg.memory;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;


public class Card extends ImageView {
    public static final int CARD_FLIP_MSECS = 750;

    private static final String TAG = "Card";
    private static final int CARD_FLIP_DEGREES = 180;
    private static final int HALF_CARD_FLIP_DEGREES = CARD_FLIP_DEGREES / 2;
    private static final int HALF_CARD_FLIP_MSECS = CARD_FLIP_MSECS / 2;
    private static final int CARD_REMOVE_MSECS = 750;

    private Integer mValue;
    private final int[] mImages = new int[MemoryActivity.MAX_MATCHES];
    private static boolean resourceLoadingFinished = false;

    // Animator set used for flipping a card:
    private AnimatorSet mFlipCardAnimSet = null;
    private int mCurrentImage;

    // Animator set used for removing a card:
    private AnimatorSet mRemoveCardAnimSet = null;


    public Card(Context context) {
        super(context);
        setup(context);

        setScaleType(ScaleType.FIT_CENTER);
        setBackgroundResource(R.drawable.card_bg);
    }

    private void setup(Context context) {
        for (int x = 0; x < mImages.length; x++) {
            mImages[x] = getResources().getIdentifier("@drawable/card_" + String.valueOf(x), null,
                                                      context.getPackageName());
        }

        // Create animator used to start flipping a card. At the end of this, the card has
        //   been rotated halfway, showing it's edge, making the current card image disappear:
        ValueAnimator mStartFlip = ObjectAnimator.ofFloat(this, "rotationY", 0,
                                                          HALF_CARD_FLIP_DEGREES);
        mStartFlip.setDuration(HALF_CARD_FLIP_MSECS);
        mStartFlip.setInterpolator(new AccelerateInterpolator());
        mStartFlip.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Change the card image when the card is half-flipped:
                Card.this.setImageResource(mCurrentImage);
            }
        });

        // Create animator used to finish flipping a card. At the start, the card edge is facing
        //   the user. The new image is then rotated into view until it is fully displayed:
        ValueAnimator mFinishFlip = ObjectAnimator.ofFloat(this, "rotationY",
                                                           -HALF_CARD_FLIP_DEGREES, 0);
        mFinishFlip.setDuration(HALF_CARD_FLIP_MSECS);
        mFinishFlip.setInterpolator(new DecelerateInterpolator());

        // Create an Animator set with the sequence: mStartFlip, mFinishFlip:
        mFlipCardAnimSet = new AnimatorSet();
        mFlipCardAnimSet.play(mStartFlip).before(mFinishFlip);

        // Create animators to remove a card by shrinking it to nothing:
        ValueAnimator removeCardX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0f);
        ValueAnimator removeCardY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0f);

        // Create animator set used to remove a card:
        mRemoveCardAnimSet = new AnimatorSet();
        mRemoveCardAnimSet.setDuration(CARD_REMOVE_MSECS);
        mRemoveCardAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());
        mRemoveCardAnimSet.play(removeCardX).with(removeCardY);
        mRemoveCardAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Card.this.setVisibility(INVISIBLE);
            }
        });

        resourceLoadingFinished = true;
    }


    public void setValue(Integer value) {
        mValue = value;
    }

    public Integer getValue() {
        return mValue;
    }

    public void hide() {
        setVisibility(View.INVISIBLE);

        // Without this, the card will sometimes stay visible:
        requestLayout();
    }

    public void remove() {
        mRemoveCardAnimSet.start();
    }

    public void showBack() {
        setScaleX(1f);
        setScaleY(1f);
        setVisibility(View.VISIBLE);
        setImageResource(R.drawable.card_back);
    }

    @SuppressWarnings("unused")
    public void showFront() {
        setScaleX(1f);
        setScaleY(1f);
        setVisibility(View.VISIBLE);
        Log.d(TAG, "Resource ID = " + mValue);
        setImageResource(mImages[mValue]);
    }

    public boolean equals(Card other) {
        return mValue.equals(other.mValue);
    }

    static public boolean isResourceLoadingFinished() {
        return resourceLoadingFinished;
    }

    public void flipToBack() {
        Log.d(TAG, String.format("Flipping card %d to back", mValue));
        flipCard(R.drawable.card_back);
    }

    public void flipToFront() {
        Log.d(TAG, String.format("Flipping card %d to front", mValue));
        flipCard(mImages[mValue]);
    }

    private void flipCard(int image) {
        mCurrentImage = image;
        mFlipCardAnimSet.start();
    }
}
