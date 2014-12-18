package br.gov.jfrj.siga.vraptor;

public enum Modulo {
	GI("GI:M�dulo de Gest�o de Identidade;"),
	SIGA("");

	private final String descricao;
	
	private Modulo(String descricao) {
		this.descricao = descricao;
	}
	
	public String getDescricao() {
		return descricao;
	}
	
}
