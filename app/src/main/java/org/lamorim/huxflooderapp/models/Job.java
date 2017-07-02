package org.lamorim.huxflooderapp.models;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lamorim.huxflooderapp.api.FlooderAPIClient;
import org.lamorim.huxflooderapp.fragment.TaskFragment;
import org.lamorim.huxflooderapp.R;
import org.lamorim.huxflooderapp.utility.ProgressHelper;

import cz.msebera.android.httpclient.Header;

/**
 * Created by lucas on 01/12/2016.
 */

public class Job {
    private int jobID;
    private String jobStatus;
    private int submissionsUntilNow;
    private int desiredSubmissions;

    public boolean isPendingCancellation() {
        return pendingCancellation;
    }

    public void setPendingCancellation(boolean pendingCancellation) {
        this.pendingCancellation = pendingCancellation;
    }

    private boolean pendingCancellation;
    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    private String problemName;
    public String getProblemID() {
        return problemID;
    }

    public void setProblemID(String problemID) {
        this.problemID = problemID;
    }

    private String problemID;

    public int getJobID() {
        return jobID;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }

    public String getjobStatus() {
        return jobStatus;
    }

    public void setjobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public int getSubmissionsUntilNow() {
        return submissionsUntilNow;
    }

    public void setSubmissionsUntilNow(int submissionsUntilNow) {
        this.submissionsUntilNow = submissionsUntilNow;
    }

    public int getDesiredSubmissions() {
        return desiredSubmissions;
    }

    public void setDesiredSubmissions(int desiredSubmissions) {
        this.desiredSubmissions = desiredSubmissions;
    }
    public static int getReadableStatus(String status) {
        switch (status) {
            case "pending":
                return R.string.pending;
            case "pending_cancellation":
                return R.string.cancelling;
            case "server_connected":
                return R.string.server_connected;
            case "running":
                return R.string.running;
            case "cancelled_by_user":
                return R.string.cancelled_by_user;
            case "cancelled_by_too_many_failed_attempts":
                return R.string.cancelled_by_too_many_failed_attempts;
            case "completed":
                return R.string.completed;
            case "server_waiting_slot":
                return R.string.server_waiting_slot;
            default:
                return R.string.unknown_status;
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Job) {
            Job job = (Job)obj;
            if (isPendingCancellation() && !job.jobStatus.equals("pending_cancellation"))
                return true;
            else if (isPendingCancellation()) {
                setPendingCancellation(false);
                return false;
            }
            return this.jobStatus.equals(job.getjobStatus()) && this.submissionsUntilNow == job.getSubmissionsUntilNow() && this.jobID == job.getJobID();
        }
        return false;
    }
    public boolean isInderteminate() {
        return !jobStatus.equals("completed") && !jobStatus.equals("running") && !jobStatus.startsWith("cancelled") && !jobStatus.equals("unknown_status");
    }
    public void updateSelf(final TaskFragment frag) {
        final Job jobref = this;
        FlooderAPIClient.get("jobs/updateJob/" + this.jobID + "/" + this.jobStatus + "/" + this.submissionsUntilNow + "/1", null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                TaskFragment.dialog.dismiss();
                frag.getRecyclerAdapter().getmValues().set(frag.recyclerPosition, jobref);
                frag.getRecyclerAdapter().notifyItemChanged(frag.recyclerPosition);
                frag.restartFetch();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                TaskFragment.dialog.dismiss();
                if (errorResponse != null) {
                    Log.e("error", errorResponse.toString());
                }
                ProgressHelper.showCustomDialog(frag.getActivity(), frag.getActivity().getString(R.string.sorry_error), frag.getActivity().getString(R.string.interjection_2), true);
                frag.restartFetch();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                TaskFragment.dialog.dismiss();
                if (errorResponse != null) {
                    Log.e("error", errorResponse.toString());
                }
                ProgressHelper.showCustomDialog(frag.getActivity(), frag.getActivity().getString(R.string.sorry_error), frag.getActivity().getString(R.string.interjection_2), true);
                frag.restartFetch();
            }
        });
    }
    public void cancelSelf(final TaskFragment frag) {
        FlooderAPIClient.get("jobs/cancelJob/" + this.jobID, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                frag.restartFetch();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    Log.e("error", errorResponse.toString());
                }
                ProgressHelper.showCustomDialog(frag.getActivity(), frag.getActivity().getString(R.string.sorry_error), frag.getActivity().getString(R.string.interjection_2), true);
                frag.restartFetch();
            }
        });
    }
    public void deleteSelf(final TaskFragment frag) {
        FlooderAPIClient.get("jobs/deleteJob/" + this.jobID, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                TaskFragment.dialog.dismiss();
                frag.getRecyclerAdapter().getmValues().remove(frag.recyclerPosition);
                frag.getRecyclerAdapter().notifyItemRemoved(frag.recyclerPosition);
                if (frag.getRecyclerAdapter().getmValues().size() == 0)
                    frag.changeToEmptyView();
                frag.restartFetch();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                TaskFragment.dialog.dismiss();
                if (errorResponse != null) {
                    Log.e("error", errorResponse.toString());
                }
                ProgressHelper.showCustomDialog(frag.getActivity(), frag.getActivity().getString(R.string.sorry_error), frag.getActivity().getString(R.string.interjection_2), true);
                frag.restartFetch();
            }
        });
    }



}
