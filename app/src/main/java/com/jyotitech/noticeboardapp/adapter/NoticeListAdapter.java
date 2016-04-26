package com.jyotitech.noticeboardapp.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jyotitech.noticeboardapp.NoticeListActivity;
import com.jyotitech.noticeboardapp.R;
import com.jyotitech.noticeboardapp.model.Notice;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.utils.KeyConstants;

import java.util.List;

/**
 * Created by kiran on 20-Apr-16.
 */
public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.NoticeViewHolder> {

    List<Notice> notices;
    NoticeListActivity mActivity;
    long appOwnerId;

    public NoticeListAdapter(NoticeListActivity activity, List<Notice> notices, long appOwnerId) {
        this.notices = notices;
        this.appOwnerId = appOwnerId;
        this.mActivity = activity;
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    @Override
    public void onBindViewHolder(NoticeViewHolder noticeViewHolder, int i) {
        Notice notice = notices.get(i);
        noticeViewHolder.cardView.setTag(notice.getId());
        noticeViewHolder.title.setText(notice.getTitle());
        noticeViewHolder.description.setText(notice.getDescription());
        noticeViewHolder.createdBy.setText(" " + notice.getOwner().getFullname());

        if(notice.getAttachments() != null) {
            byte[] decodedString = Base64.decode(notice.getAttachments(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            noticeViewHolder.imgAttachment.setVisibility(View.VISIBLE);
            Log.i("bitmap", "bitmap " + decodedString);
            noticeViewHolder.imgAttachment.setImageBitmap(decodedByte);
        }

    }

    @Override
    public NoticeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notice, viewGroup, false);
        NoticeViewHolder pvh = new NoticeViewHolder(v);
        return pvh;
    }

    public static class NoticeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView title;
        TextView description;
        TextView createdBy;
        ImageView imgAttachment;

        NoticeViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            title = (TextView)itemView.findViewById(R.id.txt_title);
            description = (TextView)itemView.findViewById(R.id.txt_description);
            createdBy = (TextView)itemView.findViewById(R.id.txt_created_by);
            imgAttachment = (ImageView)itemView.findViewById(R.id.img_attachment);
        }
    }
}