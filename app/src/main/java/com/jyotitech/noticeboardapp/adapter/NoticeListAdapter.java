package com.jyotitech.noticeboardapp.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jyotitech.noticeboardapp.R;
import com.jyotitech.noticeboardapp.model.Notice;
import com.jyotitech.noticeboardapp.sugar_models.SONotice;
import com.jyotitech.noticeboardapp.sugar_models.SOUser;
import com.jyotitech.noticeboardapp.ui.NoticeListActivity;
import com.jyotitech.noticeboardapp.utils.AppPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to show list of notices on Screen.
 * Created by Pinky Walve on 20-Apr-16.
 */
public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.NoticeViewHolder> {

    private List<SONotice> notices;
    private NoticeListActivity mActivity;
    private long appOwnerId;

    public NoticeListAdapter(NoticeListActivity activity) {
        this.notices = new ArrayList<>();
        this.appOwnerId = AppPreferences.getInstance().getAppOwnerId();
        this.mActivity = activity;
    }

    public void setDataSource(List<SONotice> soNotices) {
        this.notices.clear();
        this.notices.addAll(soNotices);
        notifyDataSetChanged();
    }

    public void addDataToDatasource(SONotice soNotice) {
        this.notices.add(soNotice);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    @Override
    public void onBindViewHolder(NoticeViewHolder noticeViewHolder, int i) {
        SONotice notice = notices.get(i);
        noticeViewHolder.cardView.setTag(notice.getNoticeId());
        noticeViewHolder.title.setText(notice.getTitle());
        noticeViewHolder.description.setText(notice.getDescription());
        SOUser owner = SOUser.findByUserId(notice.getOwner());
        noticeViewHolder.createdBy.setText(owner != null ?
                (owner.getUserId() == appOwnerId ? "Me" :owner.getFullname()) : "");
        noticeViewHolder.imgAttachment.setVisibility(View.GONE);

/*        if(notice.getAttachments() != null) {
            byte[] decodedString;
            try{
                decodedString = Base64.decode(notice.getAttachments(), Base64.DEFAULT);
            }
            catch (Exception e) {
                decodedString = Base64.decode(notice.getAttachments(), Base64.URL_SAFE);
            }

            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            noticeViewHolder.imgAttachment.setVisibility(View.VISIBLE);
            noticeViewHolder.imgAttachment.setImageBitmap(decodedByte);
        }*/
    }

    @Override
    public NoticeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notice, viewGroup, false);
        return new NoticeViewHolder(v);
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