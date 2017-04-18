package com.dgarcia.project_firebase;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
// ADAPTED FROM http://www.androidhive.info/2016/01/android-working-with-recycler-view/
public class StringAdapter extends RecyclerView.Adapter<StringAdapter.MyViewHolder>{

    private List<String> stringList;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView;

        public MyViewHolder(View view){
            super(view);
            mTextView = (TextView)view.findViewById(R.id.TV_outputInfo);
        }
    }

    public StringAdapter(List<String> stringList){
        this.stringList = stringList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.string_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        String s = stringList.get(position);

        //Change text color depending on input
        if(s.startsWith("->")) {
            holder.mTextView.setTextColor(Color.rgb(0,255,55)); //Green
        }
        else if(s.startsWith("<-")){
            if(s.contains("ERROR") || s.contains("Exception")) holder.mTextView.setTextColor(Color.rgb(255,0,0)); //Red
            else if(s.contains("Connected")) holder.mTextView.setTextColor(Color.rgb(0, 255, 42)); //Green
            else holder.mTextView.setTextColor(Color.rgb(0, 229, 255)); //blue
        }
        else {
            holder.mTextView.setTextColor(Color.WHITE);
        }
        holder.mTextView.setText(s);
    }

    @Override
    public int getItemCount(){
        return stringList.size();
    }
}
