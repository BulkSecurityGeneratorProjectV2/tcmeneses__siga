<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://localhost/jeetags" prefix="siga"%>

<siga:pagina titulo="Exibi&ccedil;&atilde;o de Solicita&ccedil;&atilde;o">
	<jsp:include page="../main.jsp"></jsp:include>
	
	<script src="//code.jquery.com/jquery-1.11.0.min.js"></script>
	<script src="/sigasr/javascripts/detalhe-tabela.js"></script>
	<script src="//cdn.datatables.net/1.10.2/js/jquery.dataTables.min.js"></script>
	<script src="/sigasr/javascripts/jquery.serializejson.min.js"></script>
	<script src="/sigasr/javascripts/jquery.populate.js"></script>
	<script src="/sigasr/javascripts/base-service.js"></script>
	<script src="/siga/javascript/jquery-ui-1.10.3.custom/js/jquery-ui-1.10.3.custom.min.js"></script>
	<script src="/sigasr/javascripts/jquery.blockUI.js"></script>
	<script src="/sigasr/javascripts/jquery.validate.min.js"></script>
	<script src="/sigasr/javascripts/base-service.js"></script>
	<script src="/sigasr/javascripts/language/messages_pt_BR.min.js"></script>		

	<style>
		#sortable table { height: 1.5em; line-height: 1.2em; }
		.ui-state-highlight { height: 1.5em; line-height: 1.2em; }
		
		/** CSS do numero **/
		.numero-solicitacao a {
			color: black;
		}
		.PRIORIDADE-IMEDIATO .numero-solicitacao a {
			color: red !important;
			font-weight: bold;
		}
		.PRIORIDADE-ALTO .numero-solicitacao a {
			color: #E0E000 !important;
			font-weight: bold;
		}
		.PRIORIDADE-MEDIO .numero-solicitacao a {
			color: blue !important;
			font-weight: bold;
		}
		.PRIORIDADE-BAIXO .numero-solicitacao a {
			color: green !important;
			font-weight: bold;
		}
		.PRIORIDADE-PLANEJADO .numero-solicitacao a {
			color: gray !important;
			font-weight: bold;
		}
		
		.legenda-prioridade {
			margin-top: 10px;
		}
		
		/** CSS da leganda **/
		.legenda-prioridade span{
			display: inline-block;
		}
		.legenda-prioridade .cor {
			height: 15px;
			width: 15px;
			display: inline-block;
			-webkit-border-radius: 3px;
			-moz-border-radius: 3px;
			border-radius: 3px;
		}
		
		.legenda-prioridade div {
			display: inline-block;
		}
		.legenda-prioridade .descricao {
			display: block;
		    margin-left: 20px;
		    margin-right: 15px;
		    margin-top: -18px;
		}
		.legenda-prioridade .PRIORIDADE-IMEDIATO .cor {
			background-color: red;
		}
		.legenda-prioridade .PRIORIDADE-ALTO .cor {
			background-color: #E0E000;
		}
		.legenda-prioridade .PRIORIDADE-MEDIO .cor {
			background-color: blue;
		}
		.legenda-prioridade .PRIORIDADE-BAIXO .cor {
			background-color: green;
		}
		.legenda-prioridade .PRIORIDADE-PLANEJADO .cor {
			background-color: gray;
		}
	</style>
	
	<div class="gt-bd clearfix">
		<div class="gt-content clearfix">
		<h2 id="tituloPagina"> ${lista.nomeLista}</h2>
		
			<p class="gt-table-action-list">
				<c:if test="${podeEditar}">
					<a href="#" onclick="javascript: editarLista(event, ${lista.toJson()})"> 
						<img src="/siga/css/famfamfam/icons/pencil.png" style="margin-right: 5px;">Editar
					</a>
				</c:if>
			</p>
			<siga:solicitacao solicitacaoListaVO="solicitacaoListaVO" filtro="filtro" modoExibicao="lista"></siga:solicitacao>
		</div>
		
		<div class="legenda-prioridade">
			<div class="PRIORIDADE-IMEDIATO">
				<span class="cor"></span>
				<span class="descricao">Imediata</span>
			</div>
			
			<div class="PRIORIDADE-ALTO">
				<span class="cor"></span>
				<span class="descricao">Alta</span>
			</div>
			
			<div class=PRIORIDADE-MEDIO>
				<span class="cor"></span>
				<span class="descricao">M�dia</span>
			</div>
			
			<div class="PRIORIDADE-BAIXO">
				<span class="cor"></span>
				<span class="descricao">Baixa</span>
			</div>
			
			<div class="PRIORIDADE-PLANEJADO">
				<span class="cor"></span>
				<span class="descricao">Planejada</span>
			</div>
		</div>
		
		<!-- /content box -->
		<div class="gt-table-buttons">
			<input type="hidden" name="idLista" value="${lista.idLista}">
			<c:if test="${podePriorizar}">
				<input type="button" id="btn" value="Gravar" class="gt-btn-medium gt-btn-left" />
			</c:if>
			<a href="${linkTo[SolicitacaoController].listarLista[false]}" class="gt-btn-medium gt-btn-left">Cancelar</a>
		</div>
	</div>
	
	<siga:modal nome="editarLista" titulo="Editar Lista">
		<div id="divEditarLista">
<%-- 			<jsp:include page="editarLista.jsp">
			</jsp:include> --%>
		</div>
	</siga:modal>
	
	<!-- modal de posicao -->	
	<siga:modal nome="posicao" titulo="Posi��o de Solicita��o na Lista">
		<div class="gt-form gt-content-box" style="width: 280px; height: 100px;">
			<form id="posicaoForm">
				<input id="idPrioridadePosicao" type="hidden" name="idSolicitacao" />
				
				<div id="numPosicao" class="gt-form-row gt-width-66">
					<label>Mover Para</label> 
					<input type="number" min="0" name="numPosicao"/>
				</div>
				
				<div class="gt-form-row">
					<input type="button" value="Ok" class="gt-btn-medium gt-btn-left" 
						onclick="reposicionar()" />
					<a class="gt-btn-medium gt-btn-left" onclick="modalPosicaoFechar()">Cancelar</a>
				</div>
			</form>
		</div>	
	</siga:modal>
	
	<!-- modal de prioridade -->
	<siga:modal nome="prioridade" titulo="Alterar Prioridade">
		<div class="gt-form gt-content-box">
			<form id="prioridadeForm">
				<input id="idPrioridadePrior" type="hidden" name="idSolicitacao" />
				
				<div id="prioridade" class="gt-form-row gt-width-66">
					<label>Prioridade</label> 
					<td>
<%-- 						<siga:select name="prioridade" id="selectPrioridade" list="${models.SrPrioridade.values()}" label="Prioridade">
							<!-- labelProperty:'descPrioridade', value:prioridade, style:'width:250px;' } --> 
							<option>Nenhuma</option>
						</siga:select> --%>
							
							 
					</td>					
				</div>
				<div id="naoReposicionarAutomatico" class="gt-width-250">
					<label>
						<siga:checkbox name="naoReposicionarAutomatico"
									   value="${naoReposicionarAutomatico}">
						</siga:checkbox> 
						<b>N�o reposicionar automaticamente</b>
					</label>
				</div>				
				
				<div class="gt-form-row">
					<input type="button" value="Ok" class="gt-btn-medium gt-btn-left" 
						onclick="gravarPrioridade()" />
					<a class="gt-btn-medium gt-btn-left" onclick="modalPrioridadeFechar()">Cancelar</a>					
				</div>
			</form>
		</div>
		
		<div id="jsonPrioridades" data-json="${jsonPrioridades}"></div>
	</siga:modal>	
	
</siga:pagina>

<script type="text/javascript">
	var solicitacaoTable,
		listaJson,
		QueryString = {};

	$(function(){
	    $('#btn').click(function() {
	        var prioridades=[];
	    	$("#sortable > tr").each(function() {
	    		var solicitacaoString = $(this).attr('data-json'),
	    			solicitacao = JSON.parse(solicitacaoString);

    			if (solicitacao)
    				prioridades.push(solicitacao.prioridadeSolicitacaoVO);
	 	    });
	 	    if (prioridades.length > 0) {
	 	    	$.post('${linkTo[SolicitacaoController].priorizarLista}', {
		 	    	listaPrioridadeSolicitacao : prioridades,
		 	    	id : $('[name=idLista]').val(),
		 	    	success: function() {
		 	    		alert('Lista gravada com sucesso');
		 	    	}
	 	    	});
		 	}
	    });
	});

	var opts = {
			urlGravar : '${linkTo[SolicitacaoController].gravarLista}',
			dialogCadastro : $('#editarLista_dialog'),
			objectName : 'lista',
			formCadastro : $('#formLista')
	};	

	// Define a "classe" listaService
	function ListaService(opts) {
		// super(opts)
		BaseService.call(this, opts);
	}
	// listaService extends BaseService
	ListaService.prototype = Object.create(BaseService.prototype);

	var listaService = new ListaService(opts);
	// Sobescreve o metodo cadastrar para limpara tela
	listaService.cadastrar = function(title) {
		BaseService.prototype.cadastrar.call(this, title);
	}

	listaService.getId = function(lista) {
		return lista.idLista;
	}
	/**
	 * Customiza o metodo editar
	 */
	listaService.editar = function(lista, title) {
		BaseService.prototype.editar.call(this, lista, title); // super.editar();

		limparDadosListaModal();
		// carrega as permiss�es da lista
		carregarPermissoes(lista.idLista);
		configuracaoInclusaoAutomaticaService.carregarParaLista(lista.idLista);
	}
	/**
	* Customiza o m�todo onGravar()
	*/
	listaService.onGravar = function(obj, objSalvo) {
		listaJson = objSalvo;

		if (listaJson)
			$("#tituloPagina").html(listaJson.nomeLista);
	}

	listaService.alterarPosicao = function(event) {
		var tr = $(event.target).parent().parent().parent(),
			obj = JSON.parse(tr.attr('data-json'));

		if (obj) {
			$('#posicao_dialog').dialog('open');
			new Formulario($('#posicaoForm')).populateFromJson(obj.prioridadeSolicitacaoVO);
		}
	}

	listaService.alterarPrioridade = function(event) {
		var tr = $(event.target).parent().parent().parent(),
			obj = JSON.parse(tr.attr('data-json'));

		if (obj) {
			$('#prioridade_dialog').dialog('open');
			obj.prioridadeSolicitacaoVO.checknaoReposicionarAutomatico = obj.prioridadeSolicitacaoVO.naoReposicionarAutomatico;
			new Formulario($('#prioridadeForm')).populateFromJson(obj.prioridadeSolicitacaoVO);
		}
	}

	function editarLista(event, jSon) {
		event.stopPropagation();

		if (!listaJson)
			listaJson = jSon;

		listaService.editar(listaJson, 'Alterar Lista');
	}
	
	function carregarPermissoes(id) {
        $.ajax({
        	type: "GET",
        	url: '${linkTo[SolicitacaoController].buscarPermissoesLista}'+"?idLista=" + id,
        	dataType: "text",
        	success: function(lista) {
        		var permissoesJSon = JSON.parse(lista);
        		populatePermissoesFromJSonList(permissoesJSon);
        	},
        	error: function(error) {
            	alert("N�o foi poss�vel carregar as Permiss�es desta Lista.");
        	}
       	});
    }

	function getAcaoPermissao(permissao) {
		if(permissao.ativo) {
	 			return '<a class="once desassociarPermissao" onclick="desativarPermissaoUsoListaEdicao(event, '+permissao.idConfiguracao+')" title="Remover permiss�o">' + 
						'<input class="idPermissao" type="hidden" value="'+permissao.idConfiguracao+'}"/>' + 
						'<img id="imgCancelar" src="/siga/css/famfamfam/icons/delete.png" style="margin-right: 5px;">' + 
					'</a>';
		}
		return new String();
	}
	
	function reposicionar() {
		var novaPosicao = $('[name=numPosicao]').val(),
			lista = $("#sortable > tr"),
			idSolicitacao = $('#idPrioridadePosicao').val(),
			tr = $('[data-json-id= '+ idSolicitacao + ']'),
			size = lista.size(),
			stringSolicitacao = $(tr).attr('data-json'),
			solicitacao = JSON.parse(stringSolicitacao);

		if (novaPosicao <= 0) {
			tr.insertBefore($(lista[0]));
		} 
		else if (novaPosicao >= size) {
			tr.insertAfter($(lista[size-1]));
		}
		else {
			if (solicitacao && solicitacao.prioridadeSolicitacaoVO && solicitacao.prioridadeSolicitacaoVO.numPosicao < novaPosicao) {
				tr.insertBefore($(lista[novaPosicao]));
			}
			else tr.insertBefore($(lista[novaPosicao-1]));
		}
		recalcularPosicao();
		modalPosicaoFechar();
	}

	function modalPosicaoFechar() {
		$("#posicao_dialog").dialog("close");
	}

	function modalPosicaoFechar() {
		$("#posicao_dialog").dialog("close");
	}
	
	function recalcularPosicao() {
		var posicao = 0;
		$("#sortable > tr").each(function() {
			var me = $(this),
				objString = me.attr('data-json'),
				obj = JSON.parse(objString),
				numPosicaoAntiga = -1;

			if (obj && obj.prioridadeSolicitacaoVO) {
				numPosicaoAntiga = obj.prioridadeSolicitacaoVO.numPosicao;
				posicao++;
				obj.prioridadeSolicitacaoVO.numPosicao = posicao;
			}
			me.attr('data-json', JSON.stringify(obj));
			me.find('td:first').find('a').html(posicao);
		});		
		
	}
	
	function gravarPrioridade() {
		var novaPrioridade = $('#selectPrioridade').val(),
			idSolicitacao = $('#idPrioridadePrior').val(),
			tr = $('[data-json-id= '+ idSolicitacao + ']'),
			objString = $(tr).attr('data-json'),
			obj = JSON.parse(objString);
		
		if (obj && obj.prioridadeSolicitacaoVO) {
			var prioridadeAntiga = obj.prioridadeSolicitacaoVO.prioridade;
			tr.removeClass('PRIORIDADE-' + obj.prioridadeSolicitacaoVO.prioridade);
			tr.addClass('PRIORIDADE-' + novaPrioridade);

			obj.prioridadeSolicitacaoVO.prioridade = novaPrioridade;
			obj.prioridadeSolicitacaoVO.naoReposicionarAutomatico = $('#checknaoReposicionarAutomatico').is(':checked');
			
			tr.attr('data-json', JSON.stringify(obj));
			
			if(prioridadeAntiga != novaPrioridade) {
				if (!obj.prioridadeSolicitacaoVO.naoReposicionarAutomatico) {
					reposicionarPorPrioridade(obj, tr);
				}
			}
		}
		recalcularPosicao();
		modalPrioridadeFechar();
	}

	function modalPrioridadeFechar() {
		$("#prioridade_dialog").dialog("close");
	}
	
	function reposicionarPorPrioridade(listaVO, tr) {
		var lista = $("#sortable > tr"),
			reposicionou = reposicionarAposIgual(lista, tr, listaVO);
		
		if (!reposicionou) {
			reposicionou = reposicionarPorPrecedenciaPrioridade(lista, tr, listaVO);
		}

		if (!reposicionou) {
			// Insere no final
			tr.insertAfter($(lista[lista.size() - 1]));
		}		
	}

	function reposicionarAposIgual(lista, tr, listaVO) {
		for (var i = lista.size() - 1; i >= 0; i--) {
			var trAdicionado = $(lista[i]), 
				listaVOAdicionado = JSON.parse(trAdicionado.attr('data-json'));

			if (registrosPrioridadesDiferentes(listaVO, listaVOAdicionado)) {
				if (listaVO && listaVO.prioridadeSolicitacaoVO && listaVO.prioridadeSolicitacaoVO.prioridade == listaVOAdicionado.prioridadeSolicitacaoVO.prioridade) {
					tr.insertAfter(trAdicionado);
					return true;
				}
			}
		}
		return false;
	}

	function reposicionarPorPrecedenciaPrioridade(lista, tr, listaVO) {
		var jsonPrioridades = $('#jsonPrioridades').data('json');
		
		for (var i = 0; i <= lista.size() - 1; i++) {
			if(listaVO && listaVO.prioridadeSolicitacaoVO) {
				var trAdicionado = $(lista[i]),
					listaVOAdicionado = JSON.parse(trAdicionado.attr('data-json')),
					idPrioridadeNovo = jsonPrioridades[listaVO.prioridadeSolicitacaoVO.prioridade],
					idPrioridadeAntigo = jsonPrioridades[listaVOAdicionado.prioridadeSolicitacaoVO.prioridade];
				
				if (idPrioridadeNovo > idPrioridadeAntigo) {
					tr.insertBefore(trAdicionado);
					return true;
				}
			}
		}
		return false;
	}

	function registrosPrioridadesDiferentes(listaVO, listaVOAdicionado) {
		return listaVO.prioridadeSolicitacaoVO.idPrioridadeSolicitacao != listaVOAdicionado.prioridadeSolicitacaoVO.idPrioridadeSolicitacao;
	}
</script>

