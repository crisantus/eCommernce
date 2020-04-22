package com.example.ecommernce.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommernce.Interface.ItemClickListner;
import com.example.ecommernce.R;

import org.w3c.dom.Text;

public class productViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtProductName, txtProductDescription, txtProductPrice;
    public ImageView imageView;
    public ItemClickListner listner;

    public productViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView=(ImageView)itemView.findViewById(R.id.product_image);
        txtProductDescription=(TextView)itemView.findViewById(R.id.product_description);
        txtProductName=(TextView)itemView.findViewById(R.id.product_name);
        txtProductPrice=(TextView)itemView.findViewById(R.id.product_price);
    }

    public void setItemClickListner(ItemClickListner listner){
        this.listner=listner;
    }

    @Override
    public void onClick(View v) {

        listner.onClick(v,getAdapterPosition(),false);

    }
}
