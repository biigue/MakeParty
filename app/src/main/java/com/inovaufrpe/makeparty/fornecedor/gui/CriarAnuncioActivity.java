package com.inovaufrpe.makeparty.fornecedor.gui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.inovaufrpe.makeparty.R;
import com.inovaufrpe.makeparty.fornecedor.dominio.Owner;
import com.inovaufrpe.makeparty.fornecedor.dominio.Plano;
import com.inovaufrpe.makeparty.fornecedor.gui.adapter.FotosAnuncioAdapter;
import com.inovaufrpe.makeparty.user.dominio.Address;
import com.inovaufrpe.makeparty.user.gui.dialog.SimOuNaoDialog;
import com.inovaufrpe.makeparty.fornecedor.dominio.Ad;
import com.inovaufrpe.makeparty.infra.ConectarServidor;
import com.inovaufrpe.makeparty.infra.SessaoApplication;
import com.inovaufrpe.makeparty.infra.utils.Mask;
import com.inovaufrpe.makeparty.user.servico.ValidacaoGuiRapida;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CriarAnuncioActivity extends AppCompatActivity {
    private EditText edtTitulo, edtValor, edtDescricao, edtTags, edtTelefone, edtRua, edtNumero, edtBairro, edtCidade, edtCep;
    private Spinner edtTipoAnuncio;
    private Button cadastroAnuncio;
    private ImageButton imgButtonAnexMaisFt;
    private String validar = "";
    private String validarPlano = "";
    private boolean isValido = false;
    private ProgressDialog mprogressDialog;
    private ValidacaoGuiRapida validacaoGuiRapida = new ValidacaoGuiRapida();
    public static final int IMAGE_GALLERY_REQUEST = 20;
    public static final int CAMERA_REQUEST_CODE = 228;
    private Button botaoSelecionarFoto;
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Bitmap bitmap;
    private TextView textoPlano;
    private Owner owner;
    private int limiteFotos;
    private int limiteAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_anuncio);
        SessaoApplication.getInstance().setTelaAtual(CriarAnuncioActivity.class);
        setTela();
        cadastroAnuncio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCriarAnuncio();
           }
       });
        imgButtonAnexMaisFt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGaleriaClicked(v);
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
        edtTelefone.addTextChangedListener(Mask.insert("(##)#####-####",edtTelefone));
        edtRua = findViewById(R.id.RuaIdCriarAnuncio);
        edtNumero = findViewById(R.id.editTextNumeroCriarAnuncio);
        edtBairro = findViewById(R.id.editTextBairroCriarAnuncio);
        edtCidade = findViewById(R.id.editTextCidadeCriarAnuncio);
        edtCep = findViewById(R.id.editTextCepCriarAnuncio);
        edtCep.addTextChangedListener(Mask.insert("#####-###",edtCep));
        cadastroAnuncio = findViewById(R.id.button_criar_anuncio);
        edtTipoAnuncio = findViewById(R.id.spinnertipoAnuncio);
        imgButtonAnexMaisFt = findViewById(R.id.imgButtonAnexarMaisFtAn);
        botaoSelecionarFoto = findViewById(R.id.botao);
        textoPlano = findViewById(R.id.textViewObsLimitesAnuncioForn);
        try{
            owner = SessaoApplication.getInstance().getObjOwnerSeEleForTipoLogado();
            if (owner.getPlan() != null) {
                calcularLimites();
                textoPlano.setText("Você ainda pode postar " + (limiteFotos - owner.getPlan().getTotalphoto()) + " fotos e " + (limiteAds - owner.getPlan().getTotalad()) + " anúncios");
            }else {
                Toast.makeText(CriarAnuncioActivity.this, "Você precisa adiquirir um plano para postar anúncios", Toast.LENGTH_SHORT).show();
                finish();
            }
        }catch (Error e){
            e.printStackTrace();
        }
        recyclerView = findViewById(R.id.view);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FotosAnuncioAdapter(bitmaps);
        recyclerView.setAdapter(adapter);
    }

    public void onClickCriarAnuncio(){
        SimOuNaoDialog.show(getSupportFragmentManager(),"Você confirma os dados desse anúncio?", new SimOuNaoDialog.Callback() {
            @Override
            public void metodoSimAoDialog() {
                boolean estourouLimite = false;
                mprogressDialog = new ProgressDialog(CriarAnuncioActivity.this);
                mprogressDialog.setMessage("Cadastrando anúncio...");
                mprogressDialog.show();
                try {
                    if (limiteFotos - (owner.getPlan().getTotalphoto() + bitmaps.size()) < 0 || limiteAds - (owner.getPlan().getTotalad() + 1) < 0) {
                        Toast.makeText(CriarAnuncioActivity.this, "Você não tem limite suficiente para postar este anúncio", Toast.LENGTH_SHORT).show();
                        estourouLimite = true;
                    }
                }catch (Error e){
                    e.printStackTrace();
                }
                if (!estourouLimite) {
                    if (verficarCampos()) {
                        String anuncio = setarAnuncio();
                        try {
                            cadastrar(anuncio);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    exibirMensagemSeValidouCadastro();
                    if (isValido) {
                        atualizarOwner();
                        mudarTela(AnunciosFornecedorActivity.class);
                    }
                }
                mprogressDialog.dismiss();

            }
        });
    }

    public void onGaleriaClicked(View v) {
        Intent selecionarFoto = new Intent(Intent.ACTION_PICK);
        File diretorioImagem = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String diretorioImagemPath = diretorioImagem.getPath();
        Uri data = Uri.parse(diretorioImagemPath);
        selecionarFoto.setDataAndType(data, "image/*");
        startActivityForResult(selecionarFoto, IMAGE_GALLERY_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Toast.makeText(this, "Imagem salva", Toast.LENGTH_LONG).show();
            }
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Uri imageUri = data.getData();
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.add(bitmap);
                    adapter.notifyDataSetChanged();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Não foi possível abrir a imagem", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    private boolean verficarCampos(){
        String cidade = edtCidade.getText().toString().trim();
        String bairro = edtBairro.getText().toString().trim();
        String cep = edtCep.getText().toString().trim();
        String rua = edtRua.getText().toString().trim();
        String numero = edtNumero.getText().toString().trim();
        String titulo = edtTitulo.getText().toString().trim();
        String valor = edtValor.getText().toString().trim();
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
        }else if(!validacaoGuiRapida.isDouble(valor)){
            this.edtValor.setError("Digite o valor do seu serviço");
            this.edtValor.requestFocus();
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
        String cep = edtCep.getText().toString().trim().replace("-","");
        String rua = edtRua.getText().toString().trim();
        String numero = edtNumero.getText().toString().trim();

        Address address = new Address();
        address.setCity(cidade);
        address.setNeighborhood(bairro);
        address.setZipcode(cep);
        address.setStreet(rua);
        address.setNumber(numero);

        String titulo = edtTitulo.getText().toString().trim();
        double valor = Double.parseDouble(edtValor.getText().toString().trim());
        String descricao = edtDescricao.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim().replace("(","").replace(")","").replace("-","");
//        String stringTags = edtTags.getText().toString().trim();
//        String[] stags = stringTags.split(",");
//        ArrayList<String> tags = new ArrayList<String>(stags);
//
////        ArrayList<String> tags = new ArrayList<>(stringTags.split(","));

        Ad ads = new Ad();
        ads.setTitle(titulo);
        ads.setPrice(valor);
        ads.setCreatedAt(new Date());
        ads.setDescription(descricao);
        ads.setPhone(telefone);
//        ads.setPhotos(bitmaps);
        String tipo = edtTipoAnuncio.getSelectedItem().toString().trim();
        tipo=ValidacaoGuiRapida.deAccent(tipo);
        ads.setType(tipo);
        ads.setAddress(address);
        String textoTags = edtTags.getText().toString();
        String[] itensDasTags = textoTags.split(",");
        List<String> arrayDasTags = new ArrayList<String>();
        Collections.addAll(arrayDasTags, itensDasTags);
        ads.setTags((ArrayList) arrayDasTags);
//        ads.setTags(tags);
        Log.i("Script", "OLHAAA: "+ SessaoApplication.getInstance().getTokenUser());
        Gson gson = new Gson();
        String ad = gson.toJson(ads);
        Log.i("Script", "OLHAAA: "+ ad);
        ad=ad.substring(0,ad.length()-1)+","+"\"token\""+":"+ "\""+SessaoApplication.getInstance().getTokenUser()+"\""+ "}";
        Log.i("Script", "OLHAAA: "+ ad);

        return ad;
    }

    private void cadastrar(String json) throws InterruptedException{
        callServer("POST",json);
    }

    private void callServer(final String method, final String data) throws InterruptedException{
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                validar = ConectarServidor.post("https://makepartyserver.herokuapp.com/ads", data);
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

    private void atualizarOwner(){
        Plano plano = owner.getPlan();
        plano.setTotalad(plano.getTotalad() + 1);
//        plano.setTotalphoto(plano.getTotalphoto() + bitmaps.size());
        Gson gson = new Gson();
        String newOwner = gson.toJson(owner);
        newOwner = newOwner.substring(0, newOwner.length() - 1) + "," + "\"token\"" + ":" + "\"" + SessaoApplication.getInstance().getTokenUser() + "\"" + "}";
        try {
            atualizarPlano(newOwner);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    private void calcularLimites(){
        if (owner.getPlan().getType().equals("Plano Gratuito")){
            limiteFotos = 1;
            limiteAds = 1;
        }else if (owner.getPlan().getType().equals("Plano Bronze Mensal") || owner.getPlan().getType().equals("Plano Bronze Anual")){
            limiteFotos = 50;
            limiteAds = 10;
        }else if (owner.getPlan().getType().equals("Plano Prata Mensal") || owner.getPlan().getType().equals("Plano Prata Anual")){
            limiteFotos = 100;
            limiteAds = 20;
        }else if (owner.getPlan().getType().equals("Plano Ouro Mensal") || owner.getPlan().getType().equals("Plano Ouro Anual")){
            limiteAds = 40;
            limiteFotos = 200;
        }
    }

    public void atualizarPlano(String json) throws InterruptedException {
        callServerPlan("PUT", json);
    }

    private void callServerPlan(final String method, final String data) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (method.equals("PUT")) {
                    validarPlano = ConectarServidor.putJadiel("https://makepartyserver.herokuapp.com/advertisers", data);
                    Log.i("Script", "OLHAAA: " + validarPlano);
                    if (validarPlano.substring(2, 5).equals("err")) {
                        validarPlano = "Não foi possível editar o perfil";
                    } else {
                        validarPlano = "Perfil editado com sucesso";
                    }
                }
            }
        });
        thread.start();
        thread.join();
    }
}
