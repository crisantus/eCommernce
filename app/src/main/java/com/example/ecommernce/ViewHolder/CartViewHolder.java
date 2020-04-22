package com.example.ecommernce.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommernce.Interface.ItemClickListner;
import com.example.ecommernce.R;


public class CartViewHolder extends RecyclerView.ViewHolder implements View .OnClickListener{

    public TextView txtProductName,txtProductPrice,txtProductQuantiy;
    private ItemClickListner itemClickListner;

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);

        txtProductName=(TextView)itemView.findViewById(R.id.cart_product_name);
        txtProductPrice=(TextView)itemView.findViewById(R.id.cart_product_price);
        txtProductQuantiy=(TextView)itemView.findViewById(R.id.cart_product_quantity);
    }

    @Override
    public void onClick(View v) {
        itemClickListner.onClick(v,getAdapterPosition(),false);
    }

    public void setItemClickListner(ItemClickListner itemClickListner){
        this.itemClickListner=itemClickListner;
    }
}
