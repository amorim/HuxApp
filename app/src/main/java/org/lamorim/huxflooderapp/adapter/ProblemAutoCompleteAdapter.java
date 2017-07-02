package org.lamorim.huxflooderapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.json.JSONArray;
import org.lamorim.huxflooderapp.models.Problem;
import org.lamorim.huxflooderapp.R;
import org.lamorim.huxflooderapp.utility.HttpRequest;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucas on 13/12/2016.
 */

public class ProblemAutoCompleteAdapter extends BaseAdapter implements Filterable {
    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<Problem> resultList = new ArrayList<>();
    public ProblemAutoCompleteAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Problem getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.txtProblemSugestion)).setText(this.getItem(position).getName());
        ((TextView) convertView.findViewById(R.id.txtIdSuggestion)).setText(String.valueOf(this.getItem(position).getId()));
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Problem> problems = findProblems(constraint.toString());
                    filterResults.values = problems;
                    filterResults.count = problems.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<Problem>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }



    private List<Problem> findProblems(String problemTitle) {
        try {
            HttpRequest req = new HttpRequest("https://www.thehuxley.com/api/v1/problems?max=" + MAX_RESULTS + "&q=" + URLEncoder.encode(problemTitle, "utf-8"));
            String json = req.prepare().sendAndReadString();
            JSONArray vetor = new JSONArray(json);
            List<Problem> list = new ArrayList<>();
            for (int i = 0; i < vetor.length(); i++) {
                Problem problem = new Problem();
                problem.setId(vetor.getJSONObject(i).getInt("id"));
                problem.setName(vetor.getJSONObject(i).getString("name"));
                list.add(problem);
            }
            return list;
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
}