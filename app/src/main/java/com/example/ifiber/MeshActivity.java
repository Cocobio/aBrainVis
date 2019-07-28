package com.example.ifiber;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by ncarc on 19-10-2017.
 */

public class MeshActivity extends AppCompatActivity {
    private String pathName;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent datos = getIntent();
        pathName = datos.getStringExtra("Path");
        fileName = datos.getStringExtra("Name");


//        setContentView(R.layout.mesh_layout);
//        TextView tvomision = (TextView) findViewById(R.id.mode);
//        String textoModo = "Modo: " + mallaPrueba.datosMesh.substring(0,9);
//        TextView tvomision2 = (TextView) findViewById(R.id.texture_type);
//        String textotype = "Texture Type: " + mallaPrueba.datosMesh.substring(12,17);
//        TextView tvomision3 = (TextView) findViewById(R.id.poly_dim);
//        String textopolydim = "Poly dim: " + mallaPrueba.datosMesh.substring(29,35);
//        tvomision.setText(textoModo);
//        tvomision2.setText(textotype);
//        tvomision3.setText(textopolydim);
    }
    /*public void setResult(){
        Intent datos = new Intent();
        datos.putExtra("datos", )
    }*/
}
