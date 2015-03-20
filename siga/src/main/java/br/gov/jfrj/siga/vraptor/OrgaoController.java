package br.gov.jfrj.siga.vraptor;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.view.Results;
import br.gov.jfrj.siga.base.AplicacaoException;
import br.gov.jfrj.siga.base.Texto;
import br.gov.jfrj.siga.dp.CpOrgao;
import br.gov.jfrj.siga.dp.CpOrgaoUsuario;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.dp.dao.CpOrgaoDaoFiltro;
import br.gov.jfrj.siga.model.dao.DaoFiltroSelecionavel;

@Resource
public class OrgaoController extends SigaSelecionavelControllerSupport<CpOrgao, DaoFiltroSelecionavel>{

	public OrgaoController(HttpServletRequest request, Result result, SigaObjects so, EntityManager em) {
		super(request, result, CpDao.getInstance(), so, em);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected DaoFiltroSelecionavel createDaoFiltro() {
		final CpOrgaoDaoFiltro flt = new CpOrgaoDaoFiltro();
		flt.setNome(Texto.removeAcentoMaiusculas(getNome()));
		return flt;
	}
	
	@Get("app/gi/orgao/listar")
	public void lista() throws Exception {
		setItens(CpDao.getInstance().consultarCpOrgaoOrdenadoPorNome());
		result.include("itens", getItens());
	}
	
	public void selecionarPorNome(){
		
	}
	
	public void excluir(final Long id){
		assertAcesso("FE:Ferramentas;CAD_ORGAO: Cadastrar Org�os");
		if (id != null) {
			try {
				dao().iniciarTransacao();
				CpOrgao orgao = daoOrgao(id);				
				dao().excluir(orgao);				
				dao().commitTransacao();				
			} catch (final Exception e) {
				dao().rollbackTransacao();
				throw new AplicacaoException("Erro na exclus�o de Org�o", 0, e);
			}
		} else
			throw new AplicacaoException("ID n�o informada");
//		this.result.redirectTo(this).aListar();
	}
	
	@Get("/app/orgao/editar")
	public void edita(final Long id){
		if (id != null) {
			CpOrgao orgao = daoOrgao(id);
			result.include("nmOrgao",orgao.getNmOrgao());
			result.include("siglaOrgao",orgao.getSigla());
			result.include("ativo",orgao.getAtivo() != null && !orgao.getAtivo().isEmpty() ? orgao.getAtivo().charAt(0) : 'N');
			result.include("idOrgaoUsu",orgao.getOrgaoUsuario() != null ? orgao.getOrgaoUsuario().getId() : null);
		}
		result.include("request",getRequest());
		result.include("id",id);
		result.include("orgaosUsu",this.getOrgaosUsu());
	}
	
	@Post("/app/orgao/gravar")
	public void editarGravar(final Long id, 
							 final String nmOrgao,
							 final String siglaOrgao, 
							 final Long idOrgaoUsu, 
							 final String ativo){
		assertAcesso("FE:Ferramentas;CAD_ORGAO: Cadastrar Org�os");
		
		if(nmOrgao == null)
			throw new AplicacaoException("Nome do �rg�o Externo n�o informado");
		
		if(siglaOrgao == null)
			throw new AplicacaoException("Sigla do �rg�o Externo n�o informada");
		
		CpOrgao orgao;		
		if (id == null)
			orgao = new CpOrgao();
		else
			orgao = daoOrgao(id);	
		
		orgao.setNmOrgao(nmOrgao);
		orgao.setSigla(siglaOrgao);
		if (idOrgaoUsu != null && idOrgaoUsu != 0) {
			CpOrgaoUsuario orgaoUsuario = new CpOrgaoUsuario();
			orgaoUsuario = dao().consultar(idOrgaoUsu, CpOrgaoUsuario.class, false);	
			orgao.setOrgaoUsuario(orgaoUsuario);
		}else
			orgao.setOrgaoUsuario(null);
		
		orgao.setAtivo(String.valueOf(ativo));
		
		try {
			dao().iniciarTransacao();
			dao().gravar(orgao);
			dao().commitTransacao();			
		} catch (final Exception e) {
			dao().rollbackTransacao();
			throw new AplicacaoException("Erro na grava��o", 0, e);
		}
//		this.result.redirectTo(this).aListar();
	}
	
	public CpOrgao daoOrgao(long id) {
		return dao().consultar(id, CpOrgao.class, false);
	}
	
	@Get
	@Post
	@Path("/app/orgao/buscar")
	public void busca(final String sigla,
			     	  final Integer offset,
			     	  final String postback,
			     	  final String propriedade) throws Exception {
		this.getP().setOffset(offset);
		this.aBuscar(sigla, postback);
		
		result.include("itens",this.getItens());
		result.include("tamanho",this.getTamanho());
		result.include("request",getRequest());
		result.include("sigla",sigla);
		result.include("offset",offset);
		result.include("postback",postback);
		result.include("propriedade",propriedade);
	}
	
	@Get("/app/orgao/selecionar")
	public void selecionar(final String sigla){
		String resultado =  super.aSelecionar(sigla);
		if (resultado == "ajax_retorno"){
			result.include("sel", getSel());
			result.use(Results.page()).forwardTo("/WEB-INF/jsp/ajax_retorno.jsp");
		}else{
			result.use(Results.page()).forwardTo("/WEB-INF/jsp/ajax_vazio.jsp");
		}
	}
	
}
