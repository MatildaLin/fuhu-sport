package com.fuhu.gdx.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

public class MaskedButton extends Button {
    private boolean mIsDisable = false;
    private boolean mIsCheckable = false;
    private boolean mLinkedPress = false;

    private Drawable mMaskDrawable;

    private Label buttonTextLabel = null;
    private Color batchColor = new Color();

    public MaskedButton(Texture up) {
        super(new TextureRegionDrawable(new TextureRegion(up)));

        up.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mMaskDrawable = super.getStyle().up;
        // this.setSize(getWidth(), getHeight());
    }

    public MaskedButton(Drawable up) {
        super(up);
        mMaskDrawable = up;
        // this.setSize(getWidth(), getHeight());
    }

    public MaskedButton(Texture up, Label buttonText) {
        this(up);

        buttonTextLabel = buttonText;
    }

    public void changeTexture(Texture newMaskTexture) {
        newMaskTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mMaskDrawable = new TextureRegionDrawable(new TextureRegion(newMaskTexture));
    }

    public void changeTexture(Drawable newMaskDrawable) {
        mMaskDrawable = newMaskDrawable;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batchColor.set(batch.getColor());

        if (isDisabled()) {
            batch.setBlendFunction(GL20.GL_ONE, GL20.GL_BLEND_SRC_ALPHA);
            batch.setColor(0.4f, 0.4f, 0.4f, 1f);
        }

        if (isPressed() || (mIsCheckable && isChecked()) || mLinkedPress) {
            batch.setBlendFunction(GL20.GL_ONE, GL20.GL_BLEND_SRC_ALPHA);
            batch.setColor(0.7f, 0.7f, 0.7f, 1f);
        }

        mMaskDrawable.draw(batch, getX(), getY(), getWidth(), getHeight());

        if (buttonTextLabel != null) {
            buttonTextLabel.setBounds(getX(), getY(), getWidth(), getHeight());
            buttonTextLabel.setAlignment(Align.center);
            buttonTextLabel.draw(batch, 1.0f);
        }

        // restore batch
        batch.setColor(batchColor);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setCheckable(boolean isCheckable) {
        mIsCheckable = isCheckable;
    }

    public boolean isCheckable() {
        return mIsCheckable;
    }

    @Override
    public void setDisabled(boolean isDisabled) {
        mIsDisable = isDisabled;
        this.setTouchable(mIsDisable ? Touchable.disabled : Touchable.enabled);
    }

    @Override
    public boolean isDisabled() {
        return mIsDisable;
    }

    public void setLinkedPress(boolean isLinkedPress) {
        mLinkedPress = isLinkedPress;
    }

}