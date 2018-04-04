package anghami.com.diffingadapterexample;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created on 04/04/2018.
 */

public class ViewHolder extends RecyclerView.ViewHolder {
    private TextView idTextView;
    private TextView descriptionTextView;

    public ViewHolder(View itemView) {
        super(itemView);
        idTextView = itemView.findViewById(R.id.id_textview);
        descriptionTextView = itemView.findViewById(R.id.description_textview);
    }

    void bind(DataItem dataItem) {
        idTextView.setText(dataItem.getId());
        descriptionTextView.setText(dataItem.getDescription());
    }

    void unbind() {

    }
}
