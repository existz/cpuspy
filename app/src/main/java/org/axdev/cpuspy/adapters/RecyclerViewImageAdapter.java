package org.axdev.cpuspy.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.axdev.cpuspy.R;
import org.axdev.cpuspy.data.RecyclerViewImageData;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewImageAdapter extends RecyclerView.Adapter<RecyclerViewImageAdapter.ViewHolder> {

    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        RecyclerViewImageAdapter.listener = listener;
    }

    private RecyclerViewImageData[] itemsData;

    public RecyclerViewImageAdapter(RecyclerViewImageData[] itemsData) {
        this.itemsData = itemsData;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_image_item_layout, null);

        return new ViewHolder(itemLayoutView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData

        viewHolder.circleImageView.setImageResource(itemsData[position].getDrawable());
        viewHolder.txtViewTitle.setText(itemsData[position].getTitle());
        viewHolder.txtViewSummary.setText(itemsData[position].getSummary());
    }

    // inner class to hold a reference to each item of RecyclerView
    protected class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView circleImageView;
        private TextView txtViewTitle;
        private TextView txtViewSummary;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            circleImageView = (CircleImageView) itemLayoutView.findViewById(R.id.imageView);
            txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.text1);
            txtViewSummary = (TextView) itemLayoutView.findViewById(R.id.text2);

            itemLayoutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(itemView, getLayoutPosition());
                }
            });
        }
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return itemsData.length;
    }

}