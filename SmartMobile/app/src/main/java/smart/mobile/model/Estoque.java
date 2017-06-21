package smart.mobile.model;

public class Estoque {
	// PRODUTOID INTEGER, LINHAID INTEGER, COLUNAID INTEGER, UNIDADEID
	// INTEGER, ESTOQUE DECIMAL, OBS TEXT

	private long id;
	private int produtoId;
	private int linhaId;
	private int colunaId;
	private int unidadeId;
	private double estoque;
	private double acrescimo;
	private double decrescimo;
	private double coletado;
	private String obs;

	private String codigoBarra;
	private String descricao;
	private String und;

	public Estoque() {
		setObs("");
	}

	public int getProdutoId() {
		return produtoId;
	}

	public void setProdutoId(int produtoId) {
		this.produtoId = produtoId;
	}

	public int getLinhaId() {
		return linhaId;
	}

	public void setLinhaId(int linhaId) {
		this.linhaId = linhaId;
	}

	public int getColunaId() {
		return colunaId;
	}

	public void setColunaId(int colunaId) {
		this.colunaId = colunaId;
	}

	public int getUnidadeId() {
		return unidadeId;
	}

	public void setUnidadeId(int unidadeId) {
		this.unidadeId = unidadeId;
	}

	public double getEstoque() {
		return estoque;
	}

	public void setEstoque(double estoque) {
		this.estoque = estoque;
	}

	public String getObs() {
		return obs;
	}

	public void setObs(String obs) {
		this.obs = obs;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getUnd() {
		return und;
	}

	public void setUnd(String und) {
		this.und = und;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCodigoBarra() {
		return codigoBarra;
	}

	public void setCodigoBarra(String codigoBarra) {
		this.codigoBarra = codigoBarra;
	}

	public double getAcrescimo() {
		return acrescimo;
	}

	public void setAcrescimo(double acrescimo) {
		this.acrescimo = acrescimo;
	}

	public double getDecrescimo() {
		return decrescimo;
	}

	public void setDecrescimo(double decrescimo) {
		this.decrescimo = decrescimo;
	}

	public double getColetado() {
		return coletado;
	}

	public void setColetado(double coletado) {
		this.coletado = coletado;
	}
	
}