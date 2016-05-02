package com.noticeboardapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.noticeboardapp.R;
import com.noticeboardapp.sugar_models.SOAttachment;
import com.noticeboardapp.ui.ImageActivity;
import com.noticeboardapp.utils.KeyConstants;
import com.noticeboardapp.utils.ToastMaker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kunal Bhavsar on 1/5/16.
 */
public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.AttahmentViewHolder> {

    private static final String TAG = AttachmentAdapter.class.getSimpleName();
    private List<SOAttachment> soAttachmentList;
    private Context context;
    public AttachmentAdapter(Context context) {
        soAttachmentList = new ArrayList<>();
        this.context = context;
    }

    public void addAttachment(SOAttachment soAttachment) {
        this.soAttachmentList.add(soAttachment);
        notifyDataSetChanged();
    }

    public List<SOAttachment> getAttachments() {
        return soAttachmentList;
    }

    @Override
    public AttachmentAdapter.AttahmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachments, parent, false);
        return new AttahmentViewHolder(v, new AttachmentAdapter.AttahmentViewHolder.IMyViewHolderClicks() {

            @Override
            public void onCardViewClicked(CardView cardView) {
                SOAttachment attachment = (SOAttachment)cardView.getTag();
                if(attachment.getAttachmentType().equals(KeyConstants.MEDIA_TYPE_IMAGE)) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putExtra(KeyConstants.EXTRA_FROM_NOTICE_LIST_TO_IMAGE_VIEW_ACTIVITY, attachment.getLocalFilePath());
                    context.startActivity(intent);
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(attachment.getLocalFilePath()));
                    intent.setType("application/pdf");
                    PackageManager pm = context.getPackageManager();
                    List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
                    if (activities.size() > 0) {
                        context.startActivity(intent);
                    }
                    else {
                        ToastMaker.createShortToast(R.string.toast_no_pdf_launcher, context);
                    }
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(AttachmentAdapter.AttahmentViewHolder holder, int position) {
        SOAttachment soAttachment = soAttachmentList.get(position);
        holder.title.setText(soAttachment.getName());
        holder.type.setText(soAttachment.getAttachmentType());
        holder.cardView.setTag(soAttachment);
    }

    @Override
    public int getItemCount() {
        return soAttachmentList.size();
    }

    public static class AttahmentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView cardView;
        TextView title;
        TextView type;
        public IMyViewHolderClicks mListener;

        AttahmentViewHolder(View itemView, IMyViewHolderClicks mListener) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            title = (TextView)itemView.findViewById(R.id.txt_name);
            type = (TextView)itemView.findViewById(R.id.txt_type);
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
