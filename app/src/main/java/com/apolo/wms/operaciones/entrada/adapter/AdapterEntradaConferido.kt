package com.apolo.wms.operaciones.entrada.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apolo.wms.clases.entrada.ArticuloConferidoPlanillaEntrada
import com.apolo.wms.operaciones.entrada.EntradaMercaderia
import com.apolo.wms.utilidades.FuncionesUtiles
import com.apolo.wms2.R


class AdapterEntradaConferido (private val context: Context,
                               private val dataSource: List<ArticuloConferidoPlanillaEntrada>,
                               private val molde: Int ) : RecyclerView.Adapter<AdapterEntradaConferido.ViewHolder>() {


    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(context)
            .inflate(molde, parent, false)

        return ViewHolder(view)


    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = dataSource[position]

        // sets the image to the imageview from our itemHolder class
        //holder.imageView.setImageResource(ItemsViewModel.image)

        // sets the text to the textview from our itemHolder class
        //holder.textView.text = ItemsViewModel.text

        holder.tvCodArticulo.text = ItemsViewModel.codArticulo
        holder.tvDescArticulo.text = ItemsViewModel.descArticulo
        holder.tvCodUnidadMedida.text = ItemsViewModel.codUnidadMedida
        holder.tvDescUnidadMedida.text = ItemsViewModel.descUnidadMedida
        holder.tvCantidadDisp.text = ItemsViewModel.cantidad
        holder.tvAnomalia.text = ItemsViewModel.anomalia
        holder.tvVencimiento.text = ItemsViewModel.fecVencimiento
        holder.tvDeposito.text = ItemsViewModel.deposito

        holder.llback2.setOnClickListener {
            FuncionesUtiles.posicionCabecera = position
            this.notifyDataSetChanged()
        }

        holder.ibtnEliminar.setOnClickListener {

            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Atencion")
                .setContentText("¿Desea Borrar el articulo ${ItemsViewModel.codArticulo}?")
                .setConfirmText("Si")
                .setConfirmClickListener { sDialog ->

                    EntradaMercaderia.eliminarRecepcionMercaderia(ItemsViewModel)
                    sDialog.dismissWithAnimation()

                }
                .setCancelButton(
                    "No"
                ) { sDialog -> sDialog.dismissWithAnimation() }
                .show()

        }


        if (position%2==0){
            holder.llback.setBackgroundColor(Color.parseColor("#EEEEEE"))
        } else {
            holder.llback.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        if (FuncionesUtiles.posicionCabecera == position) {
            holder.llback.setBackgroundColor(Color.BLUE)
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return dataSource.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        //val imageView: ImageView = itemView.findViewById(R.id.imageview)
        //val textView: TextView = itemView.findViewById(R.id.textView)
        val llback: LinearLayout = itemView.findViewById(R.id.llBack)
        val llback2: LinearLayout = itemView.findViewById(R.id.llBack2)
        val ibtnEliminar: ImageButton = itemView.findViewById(R.id.ibtnEliminar)
        val tvCodArticulo: TextView = itemView.findViewById(R.id.tvCodArticulo)
        val tvDescArticulo: TextView = itemView.findViewById(R.id.tvDescArticulo)
        val tvCodUnidadMedida: TextView = itemView.findViewById(R.id.tvCodUnidadMedida)
        val tvDescUnidadMedida: TextView = itemView.findViewById(R.id.tvDescUnidadMedida)
        val tvCantidadDisp: TextView = itemView.findViewById(R.id.tvCantidadDisp)
        val tvAnomalia: TextView = itemView.findViewById(R.id.tvAnomalia)
        val tvVencimiento: TextView = itemView.findViewById(R.id.tvVencimiento)
        val tvDeposito: TextView = itemView.findViewById(R.id.tvDeposito)



    }


/*
    Context context;
    private Methods methods;
    private ArrayList<ItemCat> arrayList;
    private int columnWidth;
    private ArrayList<ItemCat> filteredArrayList;
    private NameFilter filter;

    class MyViewHolder : RecyclerView.ViewHolder {
        TextView textView_title;
        ImageView imageView;
        View views;
        RelativeLayout rl;

        MyViewHolder(view: View) {
            /*super(view);
            textView_title = view.findViewById(R.id.tv_category_title);
            imageView = view.findViewById(R.id.iv_category);
            views = view.findViewById(R.id.view_category);
            rl = view.findViewById(R.id.rl);*/

            //textView_title = view.findViewById(R.id.tv_cat);
            //imageView = view.findViewById(R.id.iv_cat);
            //views = view.findViewById(R.id.view_cat);

        }
    }

    public AdapterCat(Context context, ArrayList<ItemCat> arrayList) {
        this.arrayList = arrayList;
        methods = new Methods(context);
        this.filteredArrayList = arrayList;
        this.context = context;

        Resources r = context.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constant.GRID_PADDING, r.getDisplayMetrics());
        columnWidth = (int) ((methods.getScreenWidth() - ((Constant.NUM_OF_COLUMNS_CATEGORY + 1) * padding)) / Constant.NUM_OF_COLUMNS_CATEGORY);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_cat2, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        LinearLayout.LayoutParams params_image = new LinearLayout.LayoutParams(columnWidth, columnWidth);

        holder.rl.setLayoutParams(params_image);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(columnWidth, columnWidth/2);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        holder.views.setLayoutParams(params);

        holder.textView_title.setText(arrayList.get(position).getName());

        ImageUtils images = new ImageUtils();
        String urlImage = arrayList.get(position).getImage();
        String fileName = urlImage.substring( urlImage.lastIndexOf('/')+1, urlImage.length() );

        Bitmap b = images.loadImageFromStorage(fileName, AdapterCat.this.context);
        if (b != null) {
            holder.imageView.setImageBitmap(b);
        } else {
            holder.imageView.setImageResource(R.drawable.sin_imagen);
        }



    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public String getID(int pos) {
        return arrayList.get(pos).getId();
    }

    public Filter getFilter() {
        if (filter == null){
            filter  = new NameFilter();
        }
        return filter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint != null && constraint.toString().length() > 0) {
                ArrayList<ItemCat> filteredItems = new ArrayList<>();

                for (int i = 0, l = filteredArrayList.size(); i < l; i++) {
                    String nameList = filteredArrayList.get(i).getName();
                    if (nameList.toLowerCase().contains(constraint))
                        filteredItems.add(filteredArrayList.get(i));
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = filteredArrayList;
                    result.count = filteredArrayList.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
            FilterResults results) {

            arrayList = (ArrayList<ItemCat>) results.values;
            notifyDataSetChanged();
        }
    }*/
}