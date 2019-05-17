package com.nqm.event_manager.custom_views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nqm.event_manager.R;

public class AddScheduleItemViewHolder extends RecyclerView.ViewHolder {

    TextView timeEditText, contentEditText;
    ImageView reorderImageView;

    public AddScheduleItemViewHolder(View itemView) {
        super(itemView);
        timeEditText = itemView.findViewById(R.id.add_schedule_time_edit_text);
        contentEditText = itemView.findViewById(R.id.add_schedule_content_edit_text);
        reorderImageView = itemView.findViewById(R.id.add_schedule_reorder);
    }

    public TextView getTimeEditText() {
        return timeEditText;
    }

    public void setTimeEditText(TextView timeEditText) {
        this.timeEditText = timeEditText;
    }

    public TextView getContentEditText() {
        return contentEditText;
    }

    public void setContentEditText(TextView contentEditText) {
        this.contentEditText = contentEditText;
    }

    public ImageView getReorderImageView() {
        return reorderImageView;
    }

    public void setReorderImageView(ImageView reorderImageView) {
        this.reorderImageView = reorderImageView;
    }
}
