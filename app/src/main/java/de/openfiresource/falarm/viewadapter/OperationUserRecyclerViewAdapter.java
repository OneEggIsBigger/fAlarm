package de.openfiresource.falarm.viewadapter;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orm.SugarRecord;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.openfiresource.falarm.R;
import de.openfiresource.falarm.models.OperationMessage;
import de.openfiresource.falarm.models.OperationRule;
import de.openfiresource.falarm.models.OperationUser;
import de.openfiresource.falarm.ui.RuleDetailActivity;
import de.openfiresource.falarm.ui.RuleDetailFragment;

/**
 * Created by stieglit on 03.08.2016.
 */
public class OperationUserRecyclerViewAdapter
        extends RecyclerView.Adapter<OperationUserRecyclerViewAdapter.ViewHolder> {

    private final List<OperationUser> mValues;
    private final OperationMessage mOperationMessage;
    private final Context mContext;

    public OperationUserRecyclerViewAdapter(Context context, OperationMessage operationMessage) {
        mOperationMessage = operationMessage;
        mValues = operationMessage.getOperationUser();
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_operationuser, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        OperationUser operationUser = mValues.get(position);

        holder.mCountView.setText(Integer.toString(position + 1));
        holder.mContentView.setText(operationUser.getName());

        if(operationUser.isCome())
            holder.mCountLayoutView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_green_400));
        else
            holder.mCountLayoutView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_red_400));

        /*
        holder.mView.setOnClickListener((v) -> {

        });*/
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        @BindView(R.id.id_layout)
        public LinearLayout mCountLayoutView;

        @BindView(R.id.id)
        public TextView mCountView;

        @BindView(R.id.content)
        public TextView mContentView;


        public OperationUser mItem;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
