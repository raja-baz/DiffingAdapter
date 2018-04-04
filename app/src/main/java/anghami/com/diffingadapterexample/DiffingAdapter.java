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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 04/04/2018.
 */

public class DiffingAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final long MAX_DIFF_TIME_MS = 1_000;

    private static final HandlerThread mBackgroundDiffThread = new HandlerThread("diffing-thread");
    private static final HandlerThread mCancelingThread = new HandlerThread("canceling-thread");
    static {
        if (!mBackgroundDiffThread.isAlive()) {
            mBackgroundDiffThread.start();
        }
        if (!mCancelingThread.isAlive()) {
            mCancelingThread.start();
        }
    }
    private static final Handler mBackgroundHandler = new Handler(mBackgroundDiffThread.getLooper());
    private static final Handler mCancelingHandler = new Handler(mCancelingThread.getLooper());
    private static final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private AtomicInteger mDataVersion = new AtomicInteger(0);

    private class DiffRequest {
        final int dataVersion;
        List<DataItem> oldItems;
        List<DataItem> newItems;

        DiffUtil.DiffResult result;

        public DiffRequest(List<DataItem> oldItems, List<DataItem> newItems) {
            this.dataVersion = mDataVersion.incrementAndGet();
            this.oldItems = oldItems;
            this.newItems = newItems;
        }
    }

    private static class DiffTimeoutException extends RuntimeException {}

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

        // Make sure the oldItems list doesn't mutate during diff
        List<DataItem> oldItems = new ArrayList<>(mData);

        DiffRequest request = new DiffRequest(oldItems, newItems);
        computeDiff(request);
    }

    private void computeDiff(final DiffRequest request) {
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    _computeDiff(request);
                } catch (DiffTimeoutException ignored) {}

                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        applyRequest(request);
                    }
                });
            }
        });
    }

    private void applyRequest(DiffRequest request) {
        if (mDataVersion.intValue() != request.dataVersion) {
            return;
        }
        mData = request.newItems;
        if (request.result == null) {
            notifyDataSetChanged();
        } else {
            request.result.dispatchUpdatesTo(this);
        }
    }

    private void _computeDiff(final DiffRequest request) throws DiffTimeoutException {
        final AtomicBoolean isCanceled = new AtomicBoolean(false);
        final AtomicBoolean isDone = new AtomicBoolean(false);
        final Object canceledLock = new Object();
        Runnable cancelRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (canceledLock) {
                    if (isDone.get()) {
                        return;
                    }
                    isCanceled.set(true);
                }
            }
        };
        mCancelingHandler.postDelayed(cancelRunnable, MAX_DIFF_TIME_MS);

        request.result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            private void checkCanceled() {
                if (isCanceled.get()) {
                    throw new DiffTimeoutException();
                }
            }
            @Override
            public int getOldListSize() {
                checkCanceled();
                return request.oldItems.size();
            }

            @Override
            public int getNewListSize() {
                checkCanceled();
                return request.newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                checkCanceled();
                return request.oldItems.get(oldItemPosition).isSameItem(request.newItems.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                checkCanceled();
                return request.oldItems.get(oldItemPosition).hasSameContent(request.newItems.get(newItemPosition));
            }
        });
        mCancelingHandler.removeCallbacks(cancelRunnable);
        synchronized (canceledLock) {
            isDone.set(true);
            isCanceled.set(false);
        }
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
