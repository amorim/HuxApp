package org.lamorim.huxflooderapp.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.lamorim.huxflooderapp.fragment.TaskFragment;
import org.lamorim.huxflooderapp.fragment.TaskFragment.OnListFragmentInteractionListener;
import org.lamorim.huxflooderapp.models.Job;
import org.lamorim.huxflooderapp.R;

import java.util.List;

public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskRecyclerViewAdapter.ViewHolder> {

    public List<Job> getmValues() {
        return mValues;
    }

    public void setmValues(List<Job> mValues) {
        this.mValues = mValues;
    }

    private List<Job> mValues;
    private final OnListFragmentInteractionListener mListener;
    ContextMenu.ContextMenuInfo info;
    public TaskRecyclerViewAdapter(List<Job> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mProgressBarJobView.getIndeterminateDrawable().setColorFilter(Color.parseColor("#F44336"), PorterDuff.Mode.SRC_ATOP);
        holder.mProgressBarJobView.getProgressDrawable().setColorFilter(Color.parseColor("#F44336"), PorterDuff.Mode.SRC_ATOP);
        if (holder.mItem.isPendingCancellation())
        holder.mJobStatusView.setText(holder.mJobStatusView.getContext().getString(R.string.status) + " " + holder.mJobStatusView.getContext().getString(R.string.pending_cancellation));
        else
        holder.mJobStatusView.setText(holder.mJobStatusView.getContext().getString(R.string.status) + " " + holder.mJobStatusView.getContext().getString(Job.getReadableStatus(holder.mItem.getjobStatus())));
        holder.mJobNumberView.setText(String.format(holder.mJobNumberView.getContext().getString(R.string.job_number), holder.mItem.getJobID()));
        holder.mJobProgressView.setText(holder.mItem.getSubmissionsUntilNow() + "/" + holder.mItem.getDesiredSubmissions());
        holder.mProblemNameView.setText(String.valueOf(holder.mItem.getProblemID()));
        if (holder.mItem.isInderteminate())
            holder.mProgressBarJobView.setIndeterminate(true);
        else {
            holder.mProgressBarJobView.setIndeterminate(false);
            double now = holder.mItem.getSubmissionsUntilNow();
            double des = holder.mItem.getDesiredSubmissions();
            Double res = (now/des) * 100.0;
            holder.mProgressBarJobView.setMax(100);
            holder.mProgressBarJobView.setProgress(res.intValue());
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        final View mView;
        final TextView mJobNumberView;
        final TextView mJobStatusView;
        final TextView mJobProgressView;
        final ProgressBar mProgressBarJobView;
        final TextView mProblemNameView;
        Job mItem;
        ViewHolder(View view) {
            super(view);
            mView = view;
            mJobNumberView = (TextView) view.findViewById(R.id.txtJobNumber);
            mJobStatusView = (TextView) view.findViewById(R.id.txtJobStatus);
            mJobProgressView = (TextView)view.findViewById(R.id.txtJobProgress);
            mProgressBarJobView = (ProgressBar)view.findViewById(R.id.progressJob);
            mProblemNameView = (TextView)view.findViewById(R.id.problem_name_id);
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            TaskFragment.FRAGMENT_IS_IN_FOREGROUND = false;
            TaskFragment.handler.removeCallbacksAndMessages(null);
            info = menuInfo;
            TaskFragment.didSelectItemInMenu = false;
            menu.setHeaderTitle(R.string.manage_task);
            if (this.mItem.getjobStatus().equals("completed") || this.mItem.getjobStatus().startsWith("cancelled")) {
                menu.add(0, 1, 0, R.string.restart_task);
                menu.add(0, 2, 0, R.string.delete_task);
            }
            if (this.mItem.getjobStatus().equals("running") || this.mItem.getjobStatus().equals("pending") || this.mItem.getjobStatus().equals("server_connected") || this.mItem.getjobStatus().equals("server_waiting_slot")) {
                menu.add(0, 3, 0, R.string.cancel_task);
            }
            menu.add(0, 4, 0, R.string.goback);
            TaskFragment.recyclerPosition = getAdapterPosition();
            }
    }
}
