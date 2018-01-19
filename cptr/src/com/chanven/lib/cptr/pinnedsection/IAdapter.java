package com.chanven.lib.cptr.pinnedsection;

/**
 * Copyright (C)
 * Author : gongcb
 * Date   : 18/1/18 下午11:28
 * Desc   : 实现pinnersection的adapter接口
 */
public interface IAdapter {

    /**
     * 是否是PinnedSection的Item
     * @param position
     * @return
     */
    boolean isPinnedSectionItem(int position);

    /**
     * 找出position所属的section位置
     * @param position
     * @return
     */
    int findSectionPosition(int position);

    /**
     * 找出下一个所属的section位置
     * @param position
     * @return
     */
    int findNextSectionPosition(int position);


}
