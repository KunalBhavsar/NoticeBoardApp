package com.noticeboardapp.adapter;

import android.content.Intent;
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
import com.noticeboardapp.ui.NoticeDetailActivity;
import com.noticeboardapp.ui.NoticeListActivity;
import com.noticeboardapp.utils.AppPreferences;
import com.noticeboardapp.utils.KeyConstants;

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
    }

    @Override
    public NoticeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notice, viewGroup, false);
        return new NoticeViewHolder(v, new NoticeViewHolder.IMyViewHolderClicks() {

            @Override
            public void onCardViewClicked(CardView cardView) {
                long id = (long)cardView.getTag();
                Intent intent = new Intent(mActivity, NoticeDetailActivity.class);
                intent.putExtra(KeyConstants.EXTRA_FROM_NOTICE_LIST_TO_NOTICE_DETAIL_ACTIVITY, id);
                mActivity.startActivity(intent);
            }
        });
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

    public static class NoticeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardView;
        TextView title;
        TextView description;
        TextView createdBy;
        ImageView imgAttachment;
        IMyViewHolderClicks mListener;
        NoticeViewHolder(View itemView, IMyViewHolderClicks mListener) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            title = (TextView)itemView.findViewById(R.id.txt_title);
            description = (TextView)itemView.findViewById(R.id.txt_description);
            createdBy = (TextView)itemView.findViewById(R.id.txt_created_by);
            this.mListener = mListener;
            imgAttachment = (ImageView)itemView.findViewById(R.id.img_attachment);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof CardView){
                mListener.onCardViewClicked((CardView)v);
            }
        }

        public interface IMyViewHolderClicks {
            void onCardViewClicked(CardView cardView);
        }
    }
}