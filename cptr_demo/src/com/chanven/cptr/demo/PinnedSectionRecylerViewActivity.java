package com.chanven.cptr.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chanven.lib.cptr.PtrClassicFrameLayout;
import com.chanven.lib.cptr.PtrDefaultHandler;
import com.chanven.lib.cptr.PtrFrameLayout;
import com.chanven.lib.cptr.loadmore.OnLoadMoreListener;
import com.chanven.lib.cptr.pinnedsection.IAdapter;
import com.chanven.lib.cptr.pinnedsection.OnPinnedSectionTouchListener;
import com.chanven.lib.cptr.pinnedsection.PinnedSectionRecyclerView;
import com.chanven.lib.cptr.recyclerview.RecyclerAdapterWithHF;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Copyright (C)
 * Author : gongcb
 * Date   : 18/1/19 上午9:03
 * Desc   :
 */
public class PinnedSectionRecylerViewActivity extends AppCompatActivity{

    private PtrClassicFrameLayout mPullLayout;
    private PinnedSectionRecyclerView mRv;

    private List<Item> mList = new ArrayList<>();

    private MyAdapter myAdapter;

    private static final int[] COLORS = new int[] {
            R.color.green_light, R.color.orange_light,
            R.color.blue_light, R.color.red_light };

    private static final int V_LISTVIEW = 0;
    private static final int V_GRIDVIEW = 2;

    Handler handler = new Handler();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_section);

        mPullLayout = (PtrClassicFrameLayout) findViewById(R.id.pull_lo);
        mRv = (PinnedSectionRecyclerView) findViewById(R.id.recyclerView);

        updateLayoutManager(V_LISTVIEW);
        genData('A','Z');

        myAdapter = new MyAdapter();
        RecyclerAdapterWithHF adapter = new RecyclerAdapterWithHF(myAdapter);
        mRv.setAdapter(adapter);

        mRv.setOnPinnedSectionTouchListener(mOnPinnedSectionTouchListener);

        mPullLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout ptrFrameLayout) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullLayout.refreshComplete();
                        showToast("22222");
                        mPullLayout.setLoadMoreEnable(true);
                    }
                },500);
            }
        });

        mPullLayout.setOnLoadMoreListener(new OnLoadMoreListener() {

            @Override
            public void loadMore() {
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        mPullLayout.loadMoreComplete(false);
                        Toast.makeText(PinnedSectionRecylerViewActivity.this,"111",Toast.LENGTH_SHORT).show();
                    }

                }, 1000);
            }
        });

    }


    private void updateLayoutManager(int mode){
        switch (mode){
            //-----listview
            case V_LISTVIEW:
                mRv.setLayoutManager(new LinearLayoutManager(this, LinearLayout.VERTICAL,false));
                break;

            //--------grid view
            case V_GRIDVIEW:
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3,LinearLayoutManager.VERTICAL,false);
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if( mList.get(position).type == Item.SECTION)
                            return 3;
                        else return 1;
                    }
                });
                mRv.setLayoutManager(gridLayoutManager);
                break;
        }
    }

    private void genData(char from, char to){
        mList.clear();
        final int sectionsNumber = to - from + 1;

        int sectionPosition = 0, listPosition = 0;
        int preSectionPosition = -1;
        for (char i=0; i<sectionsNumber; i++) {
            Item section = new Item(Item.SECTION, String.valueOf((char)(from + i)));
            section.sectionPosition = sectionPosition;
            section.listPosition = listPosition++;
            if(preSectionPosition > -1){
                Item preSection = mList.get(preSectionPosition);
                preSection.nextSectionPosition = sectionPosition;
            }
            mList.add(section);

            final int itemsNumber = (int) Math.abs((Math.cos(2f*Math.PI/3f * sectionsNumber / (i+1f)) * 25f));
            for (int j=0;j<itemsNumber;j++) {
                Item item = new Item(Item.ITEM, section.text.toUpperCase(Locale.ENGLISH) + " - " + j);
                item.sectionPosition = sectionPosition;
                item.listPosition = listPosition++;
                mList.add(item);
            }
            preSectionPosition = sectionPosition;
            sectionPosition = listPosition;
        }
    }

    Toast mToast;
    private void showToast(String text){
        if(mToast == null){
            mToast = Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT);
        }else{
            mToast.setText(text);
        }

        mToast.show();

    }

    OnPinnedSectionTouchListener mOnPinnedSectionTouchListener = new OnPinnedSectionTouchListener() {
        @Override
        public void onClick(View pinnedItemView, int position) {
            showToast("click: "+mList.get(position).text);
        }

        @Override
        public void onLongClick(View pinnedItemView, int position) {
            showToast("longClick: "+mList.get(position).text);
        }
    };

    View.OnClickListener mItemOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            Integer pos = (Integer) v.getTag();
            showToast("click: "+mList.get(pos).text);
        }
    };

    View.OnLongClickListener mItemOnLongClickListener = new View.OnLongClickListener(){

        @Override
        public boolean onLongClick(View v) {
            Integer pos = (Integer) v.getTag();
            showToast("longClick: "+mList.get(pos).text);
            return true;
        }
    };

    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IAdapter{

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
            MyAdapter.MyViewHolder myViewHolder =  new MyAdapter.MyViewHolder(itemView,mItemOnClickListener,mItemOnLongClickListener);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder1, int position) {
            Item item = mList.get(position);

            MyViewHolder holder = (MyViewHolder) holder1;

            if (item.type == Item.SECTION) {
                //view.setOnClickListener(PinnedSectionListActivity.this);
                int color = holder.itemView.getContext().getResources().getColor(COLORS[item.sectionPosition % COLORS.length]);
                holder.mTextView.setBackgroundColor(color);
            }
            holder.mTextView.setText(mList.get(position).text);
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return  mList.get(position).type;
        }

        @Override
        public boolean isPinnedSectionItem(int position) {
            return getItemViewType(position) == Item.SECTION;
        }

        @Override
        public int findSectionPosition(int position) {
            return mList.get(position).sectionPosition;
        }

        @Override
        public int findNextSectionPosition(int position) {
            return mList.get(position).nextSectionPosition;
        }

        class MyViewHolder extends RecyclerView.ViewHolder{

            TextView mTextView;
            public MyViewHolder(View itemView,View.OnClickListener itemOnClickListener,View.OnLongClickListener itemOnLongClickListener) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(R.id.tv);
                itemView.setOnClickListener(itemOnClickListener);
                itemView.setOnLongClickListener(itemOnLongClickListener);
            }

        }
    }

    static class Item {

        public static final int ITEM = 0;
        public static final int SECTION = 1;

        public final int type;
        public final String text;

        public int sectionPosition;
        public int nextSectionPosition = -1;
        public int listPosition;

        public Item(int type, String text) {
            this.type = type;
            this.text = text;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "type=" + type +
                    ", text='" + text + '\'' +
                    ", sectionPosition=" + sectionPosition +
                    ", nextSectionPosition=" + nextSectionPosition +
                    ", listPosition=" + listPosition +
                    '}';
        }
    }

}

