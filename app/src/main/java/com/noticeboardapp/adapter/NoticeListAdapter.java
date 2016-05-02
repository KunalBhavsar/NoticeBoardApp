package com.noticeboardapp.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.noticeboardapp.R;
import com.noticeboardapp.sugar_models.SONotice;
import com.noticeboardapp.sugar_models.SOUser;
import com.noticeboardapp.ui.NoticeListActivity;
import com.noticeboardapp.utils.AppPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to show list of notices on Screen.
 * Created by Pinky Walve on 20-Apr-16.
 */
public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.NoticeViewHolder> implements Filterable {

    private List<SONotice> notices;
    private List<SONotice> filteredNotices;
    private NoticeListActivity mActivity;
    private long appOwnerId;
    private String filterString;
    private NoticeListFilter noticeListFilter;

    public NoticeListAdapter(NoticeListActivity activity) {
        this.notices = new ArrayList<>();
        this.filteredNotices = new ArrayList<>();
        this.appOwnerId = AppPreferences.getInstance().getAppOwnerId();
        this.mActivity = activity;
        noticeListFilter = new NoticeListFilter();
    }

    public void setDataSource(List<SONotice> soNotices) {
        this.notices.clear();
        this.notices.addAll(soNotices);
        if(filterString != null && !filterString.trim().isEmpty()) {
            getFilter().filter(filterString);
        }
        else {
            this.filteredNotices.clear();
            this.filteredNotices.addAll(soNotices);
        }
        notifyDataSetChanged();
    }

    public void addDataToDatasource(SONotice soNotice) {
        this.notices.add(soNotice);
        if(filterString != null && !filterString.trim().isEmpty()) {
            getFilter().filter(filterString);
        }
        else {
            this.filteredNotices.add(soNotice);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredNotices.size();
    }

    @Override
    public void onBindViewHolder(NoticeViewHolder noticeViewHolder, int i) {
        SONotice notice = filteredNotices.get(i);
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

    @Override
    public Filter getFilter() {
        return noticeListFilter;
    }

    class NoticeListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {


            String filterString = constraint.toString().toLowerCase();

            int count = notices.size();
            ArrayList<SONotice> newFilteredNotieList = new ArrayList<>(count);

            for (SONotice soNotice : notices) {
                if(soNotice.getTitle().toLowerCase().contains(filterString)) {
                    newFilteredNotieList.add(soNotice);
                }
            }

            FilterResults results = new FilterResults();
            results.values = newFilteredNotieList;
            results.count = newFilteredNotieList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filterString = constraint.toString().trim();
            filteredNotices.clear();
            filteredNotices.addAll((ArrayList<SONotice>)results.values);
            notifyDataSetChanged();
        }
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