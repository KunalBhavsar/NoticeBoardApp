package com.jyotitech.noticeboardapp.adapter;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jyotitech.noticeboardapp.ui.NoticeBoardListActivity;
import com.jyotitech.noticeboardapp.ui.NoticeListActivity;
import com.jyotitech.noticeboardapp.R;
import com.jyotitech.noticeboardapp.model.NoticeBoard;
import com.jyotitech.noticeboardapp.model.UserMember;
import com.jyotitech.noticeboardapp.utils.KeyConstants;

import java.util.List;

/**
 * Created by kiran on 19-Apr-16.
 */
public class NoticeBoardListAdapter extends RecyclerView.Adapter<NoticeBoardListAdapter.NoticeBoardViewHolder> {

    List<NoticeBoard> noticeBoards;
    long appOwnerId;
    NoticeBoardListActivity mActivity;

    public NoticeBoardListAdapter(NoticeBoardListActivity activity, List<NoticeBoard> noticeBoards, long appOwnerId) {
        this.noticeBoards = noticeBoards;
        this.appOwnerId = appOwnerId;
        this.mActivity = activity;
    }

    @Override
    public int getItemCount() {
        return noticeBoards.size();
    }

    @Override
    public void onBindViewHolder(NoticeBoardViewHolder personViewHolder, int i) {
        NoticeBoard noticeBoard = noticeBoards.get(i);
        personViewHolder.cardView.setTag(noticeBoard.getId());
        personViewHolder.title.setText(noticeBoard.getTitle());
        StringBuilder owners = new StringBuilder();
        for (UserMember owner :
                noticeBoard.getMembers()) {
            if (owner.getPermissions().equals(KeyConstants.PERMISSION_WRITE)) {
                if (appOwnerId == owner.getId()) {
                    owners.append(" Me,");
                } else {
                    owners.append(" " + owner.getFullname() + ",");
                }
            }
        }
        if(owners.length() > 1) {
            owners.replace(owners.length() - 1, owners.length(), ".");
        }
        personViewHolder.owners.setText(owners.toString());
     /*   personViewHolder.noticeCount.setText(noticeBoard.getNotices() != null ?
                " " + noticeBoard.getNotices().size() : " " + 0);*/
    }

    @Override
    public NoticeBoardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notice_board, viewGroup, false);
        NoticeBoardListAdapter.NoticeBoardViewHolder noticeBoardViewHolder = new NoticeBoardViewHolder(v, new NoticeBoardListAdapter.NoticeBoardViewHolder.IMyViewHolderClicks() {

            @Override
            public void onCardViewClicked(CardView cardView) {
                long id = (long)cardView.getTag();
                Intent intent = new Intent(mActivity, NoticeListActivity.class);
                intent.putExtra(KeyConstants.EXTRA_FROM_NOTICE_BOARD_LIST_TO_NOTICE_LIST_ACTIVITY, id);
                mActivity.startActivity(intent);
            }
        });
        return noticeBoardViewHolder;
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