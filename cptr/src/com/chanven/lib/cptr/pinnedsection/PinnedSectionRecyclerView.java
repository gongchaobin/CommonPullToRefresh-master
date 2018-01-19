package com.chanven.lib.cptr.pinnedsection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import com.chanven.lib.cptr.recyclerview.RecyclerAdapterWithHF;

/**
 * Copyright (C)
 * Author : gongcb
 * Date   : 18/1/18 下午11:35
 * Desc   : PinnedSectionRecylerview
 */
public class PinnedSectionRecyclerView extends RecyclerView {

    private PinnedSection mPinnedSection;
    private PinnedSection mRecycleSection;

    private GradientDrawable mShadowDrawable;
    private int mSectionsDistanceY;
    private int mShadowHeight;

    int mTranslateY;

    private final Rect mTouchRect = new Rect();
    private View mTouchTarget;
    private GestureDetector mGestureDetector;

    private OnPinnedSectionTouchListener mOnPinnedSectionTouchListener;

    private static final String TAG = PinnedSectionRecyclerView.class.getSimpleName();

    public PinnedSectionRecyclerView(Context context) {
        super(context);
        initView();
    }

    public PinnedSectionRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PinnedSectionRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        addOnScrollListener(mOnScrollListener);
        initShadow(true);
        mGestureDetector = new GestureDetector(getContext().getApplicationContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                playSoundEffect(SoundEffectConstants.CLICK);
                if (mPinnedSection.holder.itemView != null) {
                    mPinnedSection.holder.itemView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
                }
                if (mOnPinnedSectionTouchListener != null)
                    mOnPinnedSectionTouchListener.onClick(mPinnedSection.holder.itemView, mPinnedSection.position);
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (mPinnedSection.holder.itemView != null) {
                    mPinnedSection.holder.itemView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
                if (mOnPinnedSectionTouchListener != null)
                    mOnPinnedSectionTouchListener.onLongClick(mPinnedSection.holder.itemView, mPinnedSection.position);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return true;
            }
        });
    }


    RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            checkOnScrolled();
        }
    };


    public void initShadow(boolean visible) {
        if (visible) {
            if (mShadowDrawable == null) {
                mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{Color.parseColor("#ffa0a0a0"), Color.parseColor("#50a0a0a0"), Color.parseColor("#00a0a0a0")});
                mShadowHeight = (int) (8 * getResources().getDisplayMetrics().density);
            }
        } else {
            if (mShadowDrawable != null) {
                mShadowDrawable = null;
                mShadowHeight = 0;
            }
        }
    }

    public void setShadowVisible(boolean visible) {
        initShadow(visible);
        if (mPinnedSection != null) {
            View v = mPinnedSection.holder.itemView;
            invalidate(v.getLeft(), v.getTop(), v.getRight(), v.getBottom() + mShadowHeight);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mPinnedSection != null) {
            canvas.save();

            View view = mPinnedSection.holder.itemView;
            int pLeft = getPaddingLeft();
            int pTop = getPaddingTop();

            int clipHeight = view.getHeight() + (mShadowDrawable == null ? 0 : Math.min(mShadowHeight, mSectionsDistanceY));
            canvas.clipRect(0, 0, view.getWidth(), clipHeight);
            canvas.translate(pLeft, pTop + mTranslateY);
            drawChild(canvas, view, getDrawingTime());

            //绘制阴影
            if (mShadowDrawable != null && mSectionsDistanceY > 0) {
                mShadowDrawable.setBounds(view.getLeft(), view.getBottom(), view.getRight(), view.getBottom() + mShadowHeight);
                mShadowDrawable.draw(canvas);
            }
            canvas.restore();
        }
    }


    /**
     * 发生滚动时检查
     */
    private void checkOnScrolled() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        int firstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        RecyclerView.Adapter adapter = getAdapter();

        if(adapter instanceof RecyclerAdapterWithHF) {
            // 如果是下拉刷新 重新处理
            adapter = ((RecyclerAdapterWithHF)adapter).getAdapter();
        }

        if(adapter instanceof IAdapter) {
            updatePinnedSection(firstVisibleItemPosition, firstCompletelyVisibleItemPosition);
        } else
            throw new IllegalArgumentException("Does your adapter implement PinnedSectRecyclerView.Adapter?");
    }

    private IAdapter getIAdapter() {
        RecyclerView.Adapter adapter = getAdapter();

        if(adapter instanceof RecyclerAdapterWithHF) {
            adapter = ((RecyclerAdapterWithHF)adapter).getAdapter();
        }

        if(adapter instanceof IAdapter)
            return (IAdapter)adapter;
        return null;
    }

    /**
     * 找出悬挂的Section
     *
     * @param position
     * @return
     */
    private int findPinnedSection(int position) {
        IAdapter iAdapter = getIAdapter();
        if(iAdapter != null) {
            int sectionPosition = iAdapter.findSectionPosition(position);
            return sectionPosition;
        }
        return 0;
    }

    /**
     * 找出一个悬挂的sectin
     * @return
     */
    private int findNextSectionByPinnedSection() {
        IAdapter iAdapter = getIAdapter();
        if(iAdapter != null) {
            int sectionPosition = iAdapter.findNextSectionPosition(mPinnedSection.position);
            return sectionPosition;
        }
        return 0;
    }


    @Override
    public void setAdapter(Adapter adapter) {
        RecyclerView.Adapter mAdapter = getAdapter();

        if(adapter != null) {
            if(adapter instanceof RecyclerAdapterWithHF) {
                mAdapter = ((RecyclerAdapterWithHF)adapter).getAdapter();
            }

            if(!(mAdapter instanceof IAdapter)) {
                throw new IllegalArgumentException("Does your adapter implement PinnedSectionListAdapter?");
            }

            if(mAdapter.getItemCount() < 2)
                throw new IllegalArgumentException("Does your adapter handle at least two types" +
                        " of views in getViewTypeCount() method: items and sections?");

        }


        // unregister observer at old adapter and register on new one
        RecyclerView.Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) oldAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
        if (mAdapter != null) mAdapter.registerAdapterDataObserver(mAdapterDataObserver);

        // destroy pinned shadow, if new adapter is not same as old one
        if (oldAdapter != mAdapter) removePinnedSection();

        super.setAdapter(adapter);
    }


    private final AdapterDataObserver mAdapterDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            removePinnedSection();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            removePinnedSection();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            removePinnedSection();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            removePinnedSection();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            removePinnedSection();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            removePinnedSection();
        }
    };


    /**
     * 移除pinnedSection并回收复用
     */
    private void removePinnedSection() {
        Log.d(TAG, "removePinnedSection: ");
        mSectionsDistanceY = 0;
        if (mPinnedSection != null) {
            mRecycleSection = mPinnedSection;
            mPinnedSection = null;
        }
    }

    private void createPinnedSection(int position) {
        removePinnedSection();
        Log.d(TAG, "createPinnedSection: " + position);
        // try to recycle shadow
        PinnedSection pinnedShadow = mRecycleSection;
        mRecycleSection = null;
        mTranslateY = 0;
        mSectionsDistanceY = 0;
        if (mRecycleSection == null) {
            pinnedShadow = new PinnedSection();
            pinnedShadow.position = position;
        }

        RecyclerView.Adapter adapter = getAdapter();
        ViewHolder viewHolder = adapter.createViewHolder(this, adapter.getItemViewType(position));
        adapter.bindViewHolder(viewHolder, position);

        // read layout parameters
        ViewGroup.LayoutParams layoutParams = viewHolder.itemView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = generateDefaultLayoutParams();
            viewHolder.itemView.setLayoutParams(layoutParams);
        }

        int heightMode = MeasureSpec.getMode(layoutParams.height);
        int heightSize = MeasureSpec.getSize(layoutParams.height);

        if (heightMode == MeasureSpec.UNSPECIFIED) heightMode = MeasureSpec.EXACTLY;

        int maxHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (heightSize > maxHeight) heightSize = maxHeight;

        // measure & layout
        int ws = MeasureSpec.makeMeasureSpec(getWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
        int hs = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        viewHolder.itemView.measure(ws, hs);
        viewHolder.itemView.layout(0, 0, viewHolder.itemView.getMeasuredWidth(), viewHolder.itemView.getMeasuredHeight());

        pinnedShadow.holder = viewHolder;
        mPinnedSection = pinnedShadow;
        updatePinnedSectionLocation();

    }

    /**
     * 更新悬挂视图Section
     *
     * @param firstVisibleItemPosition
     * @param firstCompletelyVisibleItemPosition
     */
    private void updatePinnedSection(int firstVisibleItemPosition, int firstCompletelyVisibleItemPosition) {
        //找出目标悬挂section位置
        int pinnedSectionPosition = findPinnedSection(firstVisibleItemPosition);

        if (mPinnedSection != null && mPinnedSection.position == firstCompletelyVisibleItemPosition) {
            //若目标悬挂section位置相等于第一个完全显示的item位置,移除悬挂
            Log.d(TAG, "updatePinnedSection: removePinnedSection");
            removePinnedSection();
        }

        if (mPinnedSection != null && mPinnedSection.position == pinnedSectionPosition) {
            //若当前绘制的悬挂位置已经等于目标悬挂位置，更新具体位置
            Log.d(TAG, "updatePinnedSection: update position " + pinnedSectionPosition);

            updatePinnedSectionLocation();
        } else {
            createPinnedSection(pinnedSectionPosition);
        }
    }

    /**
     * 更新悬挂视图的垂直位置
     */
    private void updatePinnedSectionLocation() {

        int nextSectionPosition = findNextSectionByPinnedSection();
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        View nexSectionView = layoutManager.findViewByPosition(nextSectionPosition);
        if (nexSectionView == null) {
            Log.d(TAG, "updatePinnedSectionLocation: nextSectionPosition:" + nextSectionPosition);
            int firstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            //在看不到下一个section时，保证绘制阴影高度为正常高度
            mSectionsDistanceY = firstVisibleItemPosition == firstCompletelyVisibleItemPosition ? 0 : mShadowHeight;
            return;
        }

        int cBottom = mPinnedSection.holder.itemView.getBottom();
        int nTop = nexSectionView.getTop();

        mSectionsDistanceY = nTop - cBottom;
        Log.d(TAG, "updatePinnedSectionLocation: distance=" + mSectionsDistanceY + " ,nTop=" + nTop + " ,cBottom=" + cBottom);
        if (mSectionsDistanceY < 0)
            mTranslateY = mSectionsDistanceY;
        else
            mTranslateY = 0;

    }

    public void setOnPinnedSectionTouchListener(OnPinnedSectionTouchListener onPinnedSectionTouchListener) {
        mOnPinnedSectionTouchListener = onPinnedSectionTouchListener;
    }


    private void clearTouchTarget() {
        mTouchTarget = null;
    }

    private boolean isPinnedViewTouched(View view, float x, float y) {
        view.getHitRect(mTouchRect);

        // by taping top or bottom padding, the list performs on click on a border item.
        // we don't add top padding here to keep behavior consistent.
        mTouchRect.top += mTranslateY;

        mTouchRect.bottom += mTranslateY + getPaddingTop();
        mTouchRect.left += getPaddingLeft();
        mTouchRect.right -= getPaddingRight();
        return mTouchRect.contains((int) x, (int) y);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        final int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN
                && mTouchTarget == null
                && mPinnedSection != null
                && isPinnedViewTouched(mPinnedSection.holder.itemView, x, y)) {

            // user touched pinned view
            mTouchTarget = mPinnedSection.holder.itemView;
        }

        if (mTouchTarget != null) {
            boolean ret = mGestureDetector.onTouchEvent(ev);
            Log.d(TAG, "dispatchTouchEvent: ret=" + ret);
            switch (action) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    clearTouchTarget();
//                    MotionEvent event = MotionEvent.obtain(ev);
//                    event.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(ev);
//                    event.recycle();
                    break;
            }
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

}
