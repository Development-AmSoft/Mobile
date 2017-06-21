package smart.mobile.model;

public class ProdutoComparacao
{
	private int produtoId;
	private int colunaId;
	private int linhaId;
	private int unidadeId;
	private String data;
	private double qtde;

	public ProdutoComparacao()
	{
	}

	public int getProdutoId()
	{
		return produtoId;
	}

	public void setProdutoId(int produtoId)
	{
		this.produtoId = produtoId;
	}

	public int getColunaId()
	{
		return colunaId;
	}

	public void setColunaId(int colunaId)
	{
		this.colunaId = colunaId;
	}

	public int getLinhaId()
	{
		return linhaId;
	}

	public void setLinhaId(int linhaId)
	{
		this.linhaId = linhaId;
	}

	public int getUnidadeId()
	{
		return unidadeId;
	}

	public void setUnidadeId(int unidadeId)
	{
		this.unidadeId = unidadeId;
	}

	public String getData()
	{
		return data;
	}

	public void setData(String data)
	{
		this.data = data;
	}

	public double getQtde()
	{
		return qtde;
	}

	public void setQtde(double qtde)
	{
		this.qtde = qtde;
	}

}
