package br.edu.ifsul.vendas.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Date;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import br.edu.ifsul.vendas.R;
import br.edu.ifsul.vendas.adapter.CarrinhoAdapter;
import br.edu.ifsul.vendas.model.ItemPedido;
import br.edu.ifsul.vendas.model.Pedido;
import br.edu.ifsul.vendas.model.Produto;
import br.edu.ifsul.vendas.setup.AppSetup;

public class CarrinhoActivity extends AppCompatActivity {

    private ListView lv_carrinho;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        TextView tvTotalPedidoCarrinho = findViewById(R.id.tvTotalPedidoCarrinho);
        TextView tvClienteCarrinho = findViewById(R.id.tvClienteCarrinho);


        lv_carrinho = findViewById(R.id.lv_carrinho);

        lv_carrinho.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editItem(position);
            }
        });
        lv_carrinho.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removeItem(position);
                return false;
            }
        });

        atualizaView();

        tvTotalPedidoCarrinho.setText(String.valueOf(total));
        tvClienteCarrinho.setText(String.valueOf(AppSetup.cliente.getNome().concat(" "+AppSetup.cliente.getSobrenome())));
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_activity_carrinho, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menuitem_salvar:
                confirmaSalvar();
                break;
            case R.id.menuitem_cancelar:
                confirmaCancelar();
                break;
        }
        return true;
    }

    private void confirmaCancelar(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.title_confirmar);
        builder.setMessage(R.string.message_confirma_cancelar);

        builder.setPositiveButton(R.string.alertdialog_sim, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                for(ItemPedido item : AppSetup.carrinho){
                    DatabaseReference dbRef = database.getReference("vendas/produtos")
                            .child(item.getProduto().getKey()).child("quantidade");
                    dbRef.setValue(item.getQuantidade() + item.getProduto().getQuantidade());
                    Log.d("removido" , item.toString());
                }


                AppSetup.carrinho.clear();
                AppSetup.cliente = null;
                finish();
            }
        });

        builder.setNegativeButton(R.string.alertdialog_nao, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    // Working ---
    private void confirmaSalvar(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.title_confirmar);
        builder.setMessage(R.string.message_confirma_salvar);

        builder.setPositiveButton(R.string.alertdialog_sim, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(AppSetup.carrinho.isEmpty()){
                    Toast.makeText(CarrinhoActivity.this, "Carrinho vazio", Toast.LENGTH_SHORT).show();
                } else {
                    Date timestamp = new Date();

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference dbRef = database.getReference("vendas/pedidos");

                    String key = dbRef.push().getKey();

                    Pedido pedido = new Pedido();

                    pedido.setCliente(AppSetup.cliente);
                    pedido.setDataCriacao(timestamp);
                    pedido.setDataModificacao(timestamp);
                    pedido.setEstado("aberto");
                    pedido.setFormaDePagamento("Cartão");
                    pedido.setItens(AppSetup.carrinho);
                    pedido.setSituacao(true);
                    pedido.setTotalPedido(total);

                    dbRef.child(key).setValue(pedido);

                    AppSetup.cliente = null;
                    AppSetup.carrinho.clear();
                    AppSetup.pedido = null;

                    finish();
                }
            }
        });

        builder.setNegativeButton(R.string.alertdialog_nao, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }


    // Working ---
    private void editItem(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ops tem certeza?");
        builder.setMessage("Este produto será retirado do carrinho ao editar a venda");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                atualizaEstoque(position);
                Intent intent = new Intent(CarrinhoActivity.this, ProdutoDetalheActivity.class);
                intent.putExtra("position", AppSetup.produtos.get(position).getIndex());
                startActivity(intent);

            }
        });
        builder.setNegativeButton(R.string.alertdialog_nao, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    // Working ---
    private void removeItem(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.title_confirmar);
        builder.setMessage("Deseja retirar este produto do carrinho?");

        builder.setPositiveButton(R.string.alertdialog_sim, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                atualizaEstoque(position);
                atualizaView();
                Toast.makeText(CarrinhoActivity.this, "Produto removido com sucesso", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.alertdialog_nao, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();


//

    }

    // Working ---
    public void atualizaView(){
        lv_carrinho.setAdapter(new CarrinhoAdapter(CarrinhoActivity.this, AppSetup.carrinho));
        for(ItemPedido itemPedido: AppSetup.carrinho){
            total = total + itemPedido.getTotalItem();
        }
    }


    // Working ---
    public void atualizaEstoque(int position) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("vendas/produtos").child(AppSetup.carrinho.get(position).getProduto().getKey()).child("quantidade");

        myRef.setValue(AppSetup.carrinho.get(position).getQuantidade() + AppSetup.carrinho.get(position).getProduto().getQuantidade());

        Log.d("removido", AppSetup.carrinho.get(position).toString());
        AppSetup.carrinho.remove(position);
        Log.d("item", "item removido");

        atualizaView();

        if(AppSetup.carrinho.isEmpty()){
            finish();
        }

        Toast.makeText(CarrinhoActivity.this, "Produto removido com sucesso!", Toast.LENGTH_SHORT).show();
    }
}

