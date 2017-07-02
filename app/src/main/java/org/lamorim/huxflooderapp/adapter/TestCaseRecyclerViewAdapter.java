package org.lamorim.huxflooderapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lamorim.huxflooderapp.R;
import org.lamorim.huxflooderapp.fragment.TestCaseFragment;
import org.lamorim.huxflooderapp.models.TestCase;
import java.util.List;

/**
 * Created by lucas on 03/02/2017.
 */

public class TestCaseRecyclerViewAdapter extends RecyclerView.Adapter<TestCaseRecyclerViewAdapter.ViewHolder> {

    public void setmValues(List<TestCase> mValues) {
        this.mValues = mValues;
    }

    private List<TestCase> mValues;
    public TestCaseRecyclerViewAdapter(List<TestCase> items) {
        mValues = items;
    }

    @Override
    public TestCaseRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.test_case_item, parent, false);
        return new TestCaseRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TestCaseRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mItem = getmValues().get(position);
        Context c = holder.txtId.getContext();
        holder.txtId.setText(String.format(c.getString(R.string.test_case_id_desc),holder.mItem.getId()));
        holder.txtTestCaseInput.setText(holder.mItem.getInput());
        holder.txtTestCaseOutPut.setText(holder.mItem.getOutput());
    }

    @Override
    public int getItemCount() {
        return getmValues().size();
    }

    public List<TestCase> getmValues() {
        return mValues;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        final View mView;
        final TextView txtTestCaseInput;
        final TextView txtTestCaseOutPut;
        final TextView txtId;
        TestCase mItem;
        ViewHolder(View view) {
            super(view);
            mView = view;
            txtId = (TextView)view.findViewById(R.id.test_case_id);
            txtTestCaseInput = (TextView)view.findViewById(R.id.txtInput);
            txtTestCaseOutPut = (TextView)view.findViewById(R.id.txtOutput);
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            Context c = this.txtId.getContext();
            menu.setHeaderTitle(c.getString(R.string.choose_an_option));
            menu.add(0, 1, 0, c.getString(R.string.copy_input));
            menu.add(0, 2, 0, c.getString(R.string.copy_output));
            TestCaseFragment.recyclerPosition = getAdapterPosition();
        }
    }
}
