package com.chanven.lib.cptr.pinnedsection;

import android.view.View;

/**
 * Copyright (C)
 * Author : gongcb
 * Date   : 18/1/19 上午8:55
 * Desc   :
 */
public interface OnPinnedSectionTouchListener {
    /**
     * 点击事件
     * @param pinnedItemView
     * @param position
     */
    void onClick(View pinnedItemView, int position);

    /**
     * 长按事件
     * @param pinnedItemView
     * @param position
     */
    void onLongClick(View pinnedItemView, int position);

}