package com.noticeboardapp.adapter;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.noticeboardapp.sugar_models.SONotice;
import com.noticeboardapp.sugar_models.SONoticeBoard;
import com.noticeboardapp.sugar_models.SOUser;
import com.noticeboardapp.sugar_models.SOUserMember;
import com.noticeboardapp.ui.NoticeBoardListActivity;
import com.noticeboardapp.ui.NoticeListActivity;
import com.noticeboardapp.R;
import com.noticeboardapp.utils.AppPreferences;
import com.noticeboardapp.utils.KeyConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Adapter to show list of notice boards on Screen.
 * Created by Pinky Walve on 19-Apr-16.
 */
public class NoticeBoardListAdapter extends RecyclerView.Adapter<NoticeBoardListAdapter.NoticeBoardViewHolder> implements Filterable {

    List<SONoticeBoard> noticeBoards;
    List<SONoticeBoard> filteredNoticeBoards;
    NoticeBoardListActivity mActivity;
    HashMap<SONoticeBoard, List<SOUser>> ownersHashmap;
    NoticeBoardListFilter noticeBoardListFilter;
    long appOwnerId;
    String filterString;

    public NoticeBoardListAdapter(NoticeBoardListActivity activity) {
        this.noticeBoards = new ArrayList<>();
        this.filteredNoticeBoards = new ArrayList<>();
        this.appOwnerId = AppPreferences.getInstance().getAppOwnerId();
        this.mActivity = activity;
        ownersHashmap = new HashMap<>();
        noticeBoardListFilter = new NoticeBoardListFilter();
    }

    public void setDataSource(List<SONoticeBoard> noticeBoards) {
        this.noticeBoards.clear();
        this.noticeBoards.addAll(noticeBoards);
        this.ownersHashmap.clear();
        for (SONoticeBoard noticeBoard : noticeBoards) {
            List<SOUser> userList = new ArrayList<>();
            List<SOUserMember> userMemberList = noticeBoard.getUserMembers();
            for (SOUserMember userMember : userMemberList) {
                if(userMember.getPermissions().equals(KeyConstants.PERMISSION_WRITE)) {
                    userList.add(SOUser.findByUserId(userMember.getUserId()));
                }
            }
            ownersHashmap.put(noticeBoard, userList);
        }
        if(filterString != null && !filterString.trim().isEmpty()) {
            getFilter().filter(filterString);
        }
        else {
            this.filteredNoticeBoards.clear();
            this.filteredNoticeBoards.addAll(noticeBoards);
        }

        notifyDataSetChanged();
    }

    public void addDataToDataSource(SONoticeBoard noticeBoard) {
        this.noticeBoards.add(noticeBoard);
        if(filterString != null && !filterString.trim().isEmpty()) {
            getFilter().filter(filterString);
        }
        else {
            this.filteredNoticeBoards.add(noticeBoard);
        }

        List<SOUser> userList = new ArrayList<>();
        List<SOUserMember> userMemberList = noticeBoard.getUserMembers();
        for (SOUserMember userMember : userMemberList) {
            if(userMember.getPermissions().equals(KeyConstants.PERMISSION_WRITE)) {
                userList.add(SOUser.findByUserId(userMember.getUserId()));
            }
        }
        ownersHashmap.put(noticeBoard, userList);
        notifyDataSetChanged();

    }
    @Override
    public int getItemCount() {
        return filteredNoticeBoards.size();
    }

    @Override
    public void onBindViewHolder(NoticeBoardViewHolder personViewHolder, int position) {

        SONoticeBoard noticeBoard = filteredNoticeBoards.get(position);

        personViewHolder.cardView.setTag(noticeBoard.getNoticeBoardId());
        personViewHolder.title.setText(noticeBoard.getTitle());

        StringBuilder ownersStringBuilder = new StringBuilder();

        for (SOUser owner : ownersHashmap.get(noticeBoard)) {
            if (appOwnerId == owner.getUserId()) {
                ownersStringBuilder.append(" Me,");
            } else {
                ownersStringBuilder.append(" " + owner.getFullname() + ",");
            }
        }

        if(ownersStringBuilder.length() > 1) {
            ownersStringBuilder.replace(ownersStringBuilder.length() - 1, ownersStringBuilder.length(), ".");
        }
        personViewHolder.owners.setText(ownersStringBuilder.toString());
        personViewHolder.noticeCount.setText(String.valueOf(SONotice.count(SONotice.class, SONotice.COLUMN_NOTICE_BOARD_ID + "=?", new String[]{String.valueOf(noticeBoard.getNoticeBoardId())})));
    }

    @Override
    public NoticeBoardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notice_board, viewGroup, false);
        return new NoticeBoardViewHolder(v, new NoticeBoardListAdapter.NoticeBoardViewHolder.IMyViewHolderClicks() {

            @Override
            public void onCardViewClicked(CardView cardView) {
                long id = (long)cardView.getTag();
                Intent intent = new Intent(mActivity, NoticeListActivity.class);
                intent.putExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY, id);
                mActivity.startActivity(intent);
            }
        });
    }

    @Override
    public Filter getFilter() {
        return noticeBoardListFilter;
    }

    class NoticeBoardListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            int count = noticeBoards.size();
            ArrayList<SONoticeBoard> newFilteredNotieBoardList = new ArrayList<>(count);

            for (SONoticeBoard soNoticeBoard : noticeBoards) {
                if(soNoticeBoard.getTitle().toLowerCase().contains(filterString)) {
                    newFilteredNotieBoardList.add(soNoticeBoard);
                }
            }

            FilterResults results = new FilterResults();
            results.values = newFilteredNotieBoardList;
            results.count = newFilteredNotieBoardList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filterString = constraint.toString().trim();
            filteredNoticeBoards.clear();
            filteredNoticeBoards.addAll((ArrayList<SONoticeBoard>)results.values);
            notifyDataSetChanged();
        }
    }

    public static class NoticeBoardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView cardView;
        TextView title;
        TextView owners;
        TextView noticeCount;
        public IMyViewHolderClicks mListener;

        NoticeBoardViewHolder(View itemView, IMyViewHolderClicks mListener) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            title = (TextView)itemView.findViewById(R.id.txt_title);
            owners = (TextView)itemView.findViewById(R.id.txt_owners);
            noticeCount = (TextView)itemView.findViewById(R.id.txt_notice_count);
            this.mListener = mListener;
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