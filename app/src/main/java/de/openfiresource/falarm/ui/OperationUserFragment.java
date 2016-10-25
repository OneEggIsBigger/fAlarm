package de.openfiresource.falarm.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.openfiresource.falarm.R;
import de.openfiresource.falarm.models.OperationMessage;
import de.openfiresource.falarm.viewadapter.OperationUserRecyclerViewAdapter;

/**
 * A fragment representing a list of Items.
 * interface.
 */
public class OperationUserFragment extends Fragment {
    public static final String INTENT_USER_CHANGED = "de.openfiresource.falarm.ui.operationUserChanged";
    private static final String ARG_ID = "id";

    private OperationMessage mOperationMessage;
    private RecyclerView mRecyclerView;
    private int mColumnCount = 1;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadAdapter();
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OperationUserFragment() {
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param operationMessageId Id of the operation Message.
     * @return A new instance of fragment OperationFragment.
     */
    public static OperationUserFragment newInstance(long operationMessageId) {
        OperationUserFragment fragment = new OperationUserFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, operationMessageId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            long notificationId = getArguments().getLong(ARG_ID);
            mOperationMessage = OperationMessage.findById(OperationMessage.class, notificationId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operationuser_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;

            assert mRecyclerView != null;

            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            loadAdapter();
        }

        //Broadcast receiver
        IntentFilter filterSend = new IntentFilter();
        filterSend.addAction(INTENT_USER_CHANGED);
        getActivity().registerReceiver(receiver, filterSend);

        return view;
    }

    private void loadAdapter() {
        assert mRecyclerView != null;
        mRecyclerView.setAdapter(new OperationUserRecyclerViewAdapter(this.getContext(), mOperationMessage));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(receiver);
    }
}
