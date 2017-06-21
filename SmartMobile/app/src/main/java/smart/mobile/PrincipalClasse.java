package smart.mobile;

import smart.mobile.model.Estoque;
import android.app.Application;

public class PrincipalClasse extends Application 
{

	private String vendedor;
	private String empresa;
	private String tipoMapa;
	private Estoque estoqueProduto;
	private String textFiltroProduto;

	public PrincipalClasse()
	{
		// TODO Auto-generated constructor stub
	}

	public String getVendedor()
	{
		return vendedor;
	}

	public void setVendedor(String vendedor)
	{
		this.vendedor = vendedor;
	}

	public String getEmpresa()
	{
		return empresa;
	}

	public void setEmpresa(String empresa)
	{
		this.empresa = empresa;
	}

	public String getTipoMapa()
	{
		return tipoMapa;
	}

	public void setTipoMapa(String tipoMapa)
	{
		this.tipoMapa = tipoMapa;
	}

	public Estoque getEstoqueProduto() {
		return estoqueProduto;
	}

	public void setEstoqueProduto(Estoque estoqueProduto) {
		this.estoqueProduto = estoqueProduto;
	}

	public void setTextFiltroProduto(String textFiltroProduto) {
		this.textFiltroProduto = textFiltroProduto;
	}

	public String getTextFiltroProduto() {
		return textFiltroProduto;
	}
}
