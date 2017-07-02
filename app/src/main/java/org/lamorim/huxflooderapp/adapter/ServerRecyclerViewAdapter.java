package org.lamorim.huxflooderapp.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.lamorim.huxflooderapp.fragment.ServerFragment;
import org.lamorim.huxflooderapp.models.Server;
import org.lamorim.huxflooderapp.R;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by lucas on 22/12/2016.
 */

public class ServerRecyclerViewAdapter extends RecyclerView.Adapter<ServerRecyclerViewAdapter.ViewHolder> {

    public void setmValues(List<Server> mValues) {
        this.mValues = mValues;
    }

    private List<Server> mValues;
    private final ServerFragment.OnListFragmentInteractionListener mListener;
    public ServerRecyclerViewAdapter(List<Server> items, ServerFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ServerRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.server_item, parent, false);
        return new ServerRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ServerRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.serverLoad.getProgressDrawable().setColorFilter(Color.parseColor("#F44336"), PorterDuff.Mode.SRC_ATOP);
        if (holder.mItem.isServerOnline())
            holder.imgOnline.setImageResource(R.drawable.online);
        else
            holder.imgOnline.setImageResource(R.drawable.offline);
        double max = (double)holder.mItem.getServerMaxLoad();
        double now = (double)holder.mItem.getLoad();
        Double progress = (now/max)*100;
        holder.serverLoad.setMax(100);
        holder.serverLoad.setProgress(progress.intValue());
        holder.txtServerName.setText(String.format(holder.txtServerName.getContext().getString(R.string.server_name), holder.mItem.getServerName()));
        holder.txtServerLocation.setText(holder.mItem.getServerLocation());
        String load = holder.txtServerLoad.getContext().getResources().getQuantityString(R.plurals.jobs, holder.mItem.getServerMaxLoad(), holder.mItem.getLoad(), holder.mItem.getServerMaxLoad());
        holder.txtServerLoad.setText(load);
        DateFormat df = android.text.format.DateFormat.getDateFormat(holder.txtServerLoad.getContext());
        String data = df.format(holder.mItem.getServerLastSeen());
        DateFormat df2 = android.text.format.DateFormat.getTimeFormat(holder.txtServerLoad.getContext());
        String hora = df2.format(holder.mItem.getServerLastSeen());
        String timeFinal = data + ", " + hora;
        holder.txtServerLastSeen.setText(String.format(holder.txtServerLastSeen.getContext().getString(R.string.online_in), timeFinal));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView txtServerName;
        final TextView txtServerLocation;
        final TextView txtServerLoad;
        final TextView txtServerLastSeen;
        final ProgressBar serverLoad;
        final ImageView imgOnline;
        Server mItem;
        ViewHolder(View view) {
            super(view);
            mView = view;
            txtServerName = (TextView)view.findViewById(R.id.txtServerName);
            txtServerLocation = (TextView)view.findViewById(R.id.txtServerLocation);
            txtServerLoad = (TextView)view.findViewById(R.id.txtServerLoad);
            txtServerLastSeen = (TextView)view.findViewById(R.id.txtServerLastSeen);
            serverLoad = (ProgressBar)view.findViewById(R.id.serverLoad);
            imgOnline = (ImageView)view.findViewById(R.id.imgServerOnline);
        }

    }
}
