package com.inovaufrpe.makeparty.fornecedor.gui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.inovaufrpe.makeparty.R;
import com.inovaufrpe.makeparty.cliente.gui.fragment.dialog.SimOuNaoDialog;
import com.inovaufrpe.makeparty.fornecedor.dominio.Anuncio;
import com.inovaufrpe.makeparty.infra.ConectarServidor;
import com.inovaufrpe.makeparty.usuario.dominio.Endereco;
import com.inovaufrpe.makeparty.usuario.servico.ValidacaoGuiRapida;

import java.util.Date;

public class CriarAnuncioActivity extends AppCompatActivity {
    private EditText edtTitulo, edtValor, edtDescricao, edtTags, edtTelefone, edtRua, edtNumero, edtBairro, edtCidade, edtCep;
    private Spinner edtTipoAnuncio;
    private Button cadastroAnuncio;
    private ImageButton imgButtonImgsAnex,imgButtonAnexMaisFt;
    private String validar = "";
    private boolean isValido = false;
    private ProgressDialog mprogressDialog;
    private ValidacaoGuiRapida validacaoGuiRapida = new ValidacaoGuiRapida();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_anuncio);
        setTela();
        cadastroAnuncio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCriarAnuncio();
           }
       });
    }
    public void opcoesSpinner(){
        // Ainda tem que setar isso daqui
        // setei no xml strings
    }

    public void setTela(){
        edtTitulo = findViewById(R.id.editTextTituloCriarAn);
        edtValor = findViewById(R.id.editTextValorCriarAn);
        edtDescricao = findViewById(R.id.editTextDescricaoCriarAn);
        edtTags = findViewById(R.id.editTextTagsCriarAn);
        edtTelefone = findViewById(R.id.editTextTelefoneCriarAnun);
        edtRua = findViewById(R.id.RuaIdCriarAnuncio);
        edtNumero = findViewById(R.id.editTextNumeroCriarAnuncio);
        edtBairro = findViewById(R.id.editTextBairroCriarAnuncio);
        edtCidade = findViewById(R.id.editTextCidadeCriarAnuncio);
        edtCep = findViewById(R.id.editTextCepCriarAnuncio);
        cadastroAnuncio = findViewById(R.id.button_criar_anuncio);
        edtTipoAnuncio = findViewById(R.id.spinnertipoAnuncio);
        imgButtonImgsAnex = findViewById(R.id.imgButtonGalFotosAnexAn);
        imgButtonAnexMaisFt = findViewById(R.id.imgButtonAnexarMaisFtAn);
    }
    public void onClickCriarAnuncio(){
        SimOuNaoDialog.show(getSupportFragmentManager(),"Você confirma os dados desse anúncio?", new SimOuNaoDialog.Callback() {
            @Override
            public void metodoSimAoDialog() {
                mprogressDialog = new ProgressDialog(CriarAnuncioActivity.this);
                mprogressDialog.setMessage("Cadastrando anúncio...");
                mprogressDialog.show();
                if(verficarCampos()){
                    String anuncio = setarAnuncio();
                    try{
                        cadastrar(anuncio);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }}
                mprogressDialog.dismiss();
                exibirMensagemSeValidouCadastro();
                if (isValido){
                    mudarTela(AnunciosFornecedorActivity.class);
                }

            }
        });
    }


    private boolean verficarCampos(){
        String cidade = edtCidade.getText().toString().trim();
        String bairro = edtBairro.getText().toString().trim();
        String cep = edtCep.getText().toString().trim();
        String rua = edtRua.getText().toString().trim();
        String titulo = edtTitulo.getText().toString().trim();
//        double valor = Double.parseDouble(edtValor.getText().toString().trim());
        String descricao = edtDescricao.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();

        Boolean camposOk = true;
        if (!validacaoGuiRapida.isCampoAceitavel(titulo)){
            this.edtTitulo.setError(("Digite o título do seu anúncio"));
            this.edtTitulo.requestFocus();
            return false;
        }else if(!validacaoGuiRapida.isCampoAceitavel(descricao)){
            this.edtDescricao.setError("Descreva melhor o seu serviço");
            this.edtDescricao.requestFocus();
            return false;
        }else if(!validacaoGuiRapida.isTelefoneValido(telefone)){
            this.edtTelefone.setError("Telefone Invalido ou sem ddd");
            this.edtTelefone.requestFocus();
            return false;
        }else if(validacaoGuiRapida.isCampoVazio(cidade)){
            this.edtCidade.setError("Favor insira a cidade");
            this.edtCidade.requestFocus();
            return false;
        }else if(validacaoGuiRapida.isCampoVazio(bairro)){
            this.edtBairro.setError("Favor insira o Bairro");
            this.edtBairro.requestFocus();
            return false;
        }else if(validacaoGuiRapida.isCampoVazio(rua)){
            this.edtRua.setError("Favor insira a Rua");
            return false;
        }else if(!validacaoGuiRapida.isCepValido(cep)){
            this.edtCep.setError("Favor insira um CEP Válido");
            this.edtCep.requestFocus();
            return false;
        }else{
            return true;
        }
    }

    public String setarAnuncio(){
        String cidade = edtCidade.getText().toString().trim();
        String bairro = edtBairro.getText().toString().trim();
        String cep = edtCep.getText().toString().trim();
        String rua = edtRua.getText().toString().trim();
        String numero = edtNumero.getText().toString().trim();

        Endereco endereco = new Endereco();
        endereco.setCity(cidade);
        endereco.setNeighborhood(bairro);
        endereco.setZipcode(cep);
        endereco.setStreet(rua);
        endereco.setNumber(numero);

        String titulo = edtTitulo.getText().toString().trim();
        double valor = Double.parseDouble(edtValor.getText().toString().trim());
        String descricao = edtDescricao.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();
        String tipo = edtTipoAnuncio.getSelectedItem().toString();
//        String stringTags = edtTags.getText().toString().trim();
//        String[] stags = stringTags.split(",");
//        ArrayList<String> tags = new ArrayList<String>(stags);
//
////        ArrayList<String> tags = new ArrayList<>(stringTags.split(","));


        Anuncio anuncio = new Anuncio();
        anuncio.setTitle(titulo);
        anuncio.setPrice(valor);
        anuncio.setDataAd(new Date());
        anuncio.setDescription(descricao);
        anuncio.setPhone(telefone);
        anuncio.setType(tipo);
        anuncio.setAddress(endereco);
//        anuncio.setTags(tags);

        Gson gson = new Gson();
        String ad = gson.toJson(anuncio);
        return ad;
    }

    private void cadastrar(String json) throws InterruptedException{
        callServer("POST",json);
    }

    private void callServer(final String method, final String data) throws InterruptedException{
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                validar = ConectarServidor.postComToken("https://makepartyserver.herokuapp.com/ads", data);
                Log.i("Script", "OLHAAA: "+ validar);
                if (validar.substring(2, 5).equals("err")){
                    // Não sei qual o erro
                    validar = "Não foi possível criar o anúncio";
                    // Rever a mensagem
                }else{
                    validar = "Anúncio criado com sucesso";
                    isValido = true;
                }
            }
        });
        thread.start();
        thread.join();
    }

    private void exibirMensagemSeValidouCadastro() {
        Toast.makeText(getApplicationContext(), validar, Toast.LENGTH_SHORT).show();
    }

    private void mudarTela(Class proximaTela){
        Intent intent = new Intent( CriarAnuncioActivity.this, proximaTela);
        startActivity(intent);
        finish();
    }

    public void cadastroAnuncio(View view) {
        this.mudarTela(AnunciosFornecedorActivity.class);
    }

    @Override
    public void onBackPressed() {
        this.mudarTela(AnunciosFornecedorActivity.class);
    }
}