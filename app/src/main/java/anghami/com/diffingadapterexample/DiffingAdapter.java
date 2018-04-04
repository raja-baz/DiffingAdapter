package anghami.com.diffingadapterexample;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 04/04/2018.
 */

public class DiffingAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<DataItem> mData = new ArrayList<>();
    private RecyclerView mRecyclerView;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.unbind();
    }

    public void refresh(final List<DataItem> dataItems) {
        if (mRecyclerView == null) {
            return;
        }
        List<DataItem> newItems = new ArrayList<>(dataItems.size());
        // Deep-copy the new data in case someone else mutates those objects
        for (DataItem item : dataItems) {
            newItems.add(item.copy());
        }

        DiffUtil.DiffResult result = computeDiff(mData, newItems);
        mData = newItems;
        result.dispatchUpdatesTo(this);
    }

    private DiffUtil.DiffResult computeDiff(final List<DataItem> oldItems, final List<DataItem> newItems) {
        return DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldItems.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldItems.get(oldItemPosition).isSameItem(newItems.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return oldItems.get(oldItemPosition).hasSameContent(newItems.get(newItemPosition));
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }
}
