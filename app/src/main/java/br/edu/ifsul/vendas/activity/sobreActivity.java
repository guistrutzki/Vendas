package br.edu.ifsul.vendas.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import br.edu.ifsul.vendas.R;

public class sobreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);
        TextView texto = (TextView)findViewById(R.id.sobreText);
        texto.setText("Lorem ipsum dolar sit, lorem ipsum dolar sit.");

    }
}
