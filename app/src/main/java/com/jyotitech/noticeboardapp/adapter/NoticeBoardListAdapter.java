package com.jyotitech.noticeboardapp.adapter;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jyotitech.noticeboardapp.model.Notice;
import com.jyotitech.noticeboardapp.sugar_models.SONotice;
import com.jyotitech.noticeboardapp.sugar_models.SONoticeBoard;
import com.jyotitech.noticeboardapp.sugar_models.SOUser;
import com.jyotitech.noticeboardapp.sugar_models.SOUserMember;
import com.jyotitech.noticeboardapp.ui.NoticeBoardListActivity;
import com.jyotitech.noticeboardapp.ui.NoticeListActivity;
import com.jyotitech.noticeboardapp.R;
import com.jyotitech.noticeboardapp.utils.AppPreferences;
import com.jyotitech.noticeboardapp.utils.KeyConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Adapter to show list of notice boards on Screen.
 * Created by Pinky Walve on 19-Apr-16.
 */
public class NoticeBoardListAdapter extends RecyclerView.Adapter<NoticeBoardListAdapter.NoticeBoardViewHolder> {

    List<SONoticeBoard> noticeBoards;
    NoticeBoardListActivity mActivity;
    HashMap<SONoticeBoard, List<SOUser>> ownersHashmap;
    long appOwnerId;

    public NoticeBoardListAdapter(NoticeBoardListActivity activity) {
        this.noticeBoards = new ArrayList<>();
        this.appOwnerId = AppPreferences.getInstance().getAppOwnerId();
        this.mActivity = activity;
        ownersHashmap = new HashMap<>();
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
        notifyDataSetChanged();
    }

    public void addDataToDataSource(SONoticeBoard noticeBoard) {
        this.noticeBoards.add(noticeBoard);
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
        return noticeBoards.size();
    }

    @Override
    public void onBindViewHolder(NoticeBoardViewHolder personViewHolder, int position) {

        SONoticeBoard noticeBoard = noticeBoards.get(position);

        personViewHolder.cardView.setTag(noticeBoard.getNoticeBoardId());
        personViewHolder.title.setText(noticeBoard.getTitle());

        StringBuilder ownersStringBuilder = new StringBuilder();

        for (SOUser owner : ownersHashmap.get(noticeBoard)) {
            if (appOwnerId == owner.getId()) {
                ownersStringBuilder.append(" Me,");
            } else {
                ownersStringBuilder.append(" " + owner.getFullname() + ",");
            }
        }

        if(ownersStringBuilder.length() > 1) {
            ownersStringBuilder.replace(ownersStringBuilder.length() - 1, ownersStringBuilder.length(), ".");
        }
        personViewHolder.owners.setText(ownersStringBuilder.toString());
        personViewHolder.noticeCount.setText(String.valueOf(SONotice.count(SONotice.class, "notice_board_id=?", new String[]{String.valueOf(noticeBoard.getNoticeBoardId())})));
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