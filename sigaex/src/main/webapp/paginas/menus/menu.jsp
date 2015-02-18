<%@ taglib prefix="ww" uri="/webwork"%>
<%@ taglib uri="/WEB-INF/tld/func.tld" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://localhost/sigatags" prefix="siga"%>
<li><a href="#">Documentos</a>
	<ul>
		<li>
			<a href="/sigaex/app/expediente/doc/editar">Novo</a>
		</li>
		<li>
			<a href="/sigaex/app/expediente/doc/listar?primeiraVez=sim">Pesquisar</a>
		</li>

		<c:if test="${f:resource('siga.lucene.ativo')}">
			<li><ww:url id="url" action="full_search"
					namespace="/expediente/doc">
				</ww:url> <ww:a href="%{url}">Pesquisar por texto</ww:a>
			</li>
		</c:if>

		<li>
			<siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/transferir_lote"
				texto="Transferir em lote" />
		</li>
		<li> <siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/receber_lote"
				texto="Receber em lote" />
		</li>
		<li><siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/anotar_lote"
				texto="Anotar em lote" />
		</li>
		<c:catch>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;ASS:Assinatura digital;EXT:Extens�o')}">
				<li><ww:url id="url" action="assinar_lote"
						namespace="/expediente/mov" /> <siga:monolink href="%{url}"
						texto="Assinar em lote" />
				</li>
			</c:if>
		</c:catch>
		<c:catch>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;ASS:Assinatura digital;EXT:Extens�o')}">
				<li> <siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/assinar_despacho_lote"
						texto="Assinar Despacho em lote" />
				</li>
			</c:if>
		</c:catch>
		<li> <siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/arquivar_corrente_lote"
				texto="Arquivar em lote" />
		</li>
		<c:catch>
			<c:if
				test="${f:podeArquivarPermanentePorConfiguracao(titular,lotaTitular)}">
				<li> <siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/arquivar_intermediario_lote"
					texto="Arquivar Intermedi�rio em lote" />
				</li>
			</c:if>
		</c:catch>
		<c:catch>
			<c:if
				test="${f:podeArquivarPermanentePorConfiguracao(titular,lotaTitular)}">
				<li> <siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/arquivar_permanente_lote"
					texto="Arquivar Permanente em lote" />
				</li>
			</c:if>
		</c:catch>
		<c:catch>
			<c:if
				test="${f:testaCompetencia('atenderPedidoPublicacao',titular,lotaTitular,null)}">
				<li><ww:url id="url" action="atender_pedido_publicacao"
						namespace="/expediente/mov" /> <ww:a href="%{url}">Gerenciar Publica��o DJE</ww:a>
				</li>
			</c:if>
		</c:catch>
		<%--<c:catch>
			<c:if
				test="${f:testaCompetencia('definirPublicadoresPorConfiguracao',titular,lotaTitular,null)}">
				<li><ww:url id="url" action="definir_publicadores"
					namespace="/expediente/configuracao" /><ww:a href="%{url}">Definir Publicadores DJE</ww:a></li>
			</c:if>
		</c:catch>--%>
		<c:catch>
			<c:if
				test="${f:testaCompetencia('gerenciarPublicacaoBoletimPorConfiguracao',titular,lotaTitular,null)}">
				<li><ww:url id="url" action="gerenciar_publicacao_boletim"
						namespace="/expediente/configuracao" /> <ww:a href="%{url}">Definir Publicadores Boletim</ww:a>
				</li>
			</c:if>
		</c:catch>
	</ul>
</li>

<c:if
	test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas')}">
	<li><a href="#">Ferramentas</a>
		<ul>
		    <li><siga:monolink href="${pageContext.request.contextPath}/app/forma/listar"
					texto="Cadastro de Formas" />
			</li>
			<li><ww:url id="url" action="listar" namespace="/modelo" /> <ww:a
					href="%{url}">Cadastro de modelos</ww:a>
			</li>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas;DESP:Tipos de despacho')}">
				<li><ww:url id="url" action="listar"
						namespace="/despacho/tipodespacho" /> <ww:a href="%{url}">Cadastro de tipos de despacho</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas;CFG:Configura��es')}">
				<li><ww:url id="url" action="listar"
						namespace="/expediente/configuracao" /> <ww:a href="%{url}">Cadastro de configura��es</ww:a>
				</li>
			</c:if>
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas;EMAIL:Email de Notifica��o')}">
				<li><ww:url id="url" action="listar"
						namespace="/expediente/emailNotificacao" /> <ww:a href="%{url}">Cadastro de email de notifica��o</ww:a>
				</li>
			</c:if>
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas;PC:Plano de Classifica��o')}">
				<li><ww:url id="url" action="listar"
						namespace="/expediente/classificacao" /> <ww:a href="%{url}">Classifica��o Documental</ww:a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas;TT:Tabela de Temporalidade')}">
				<li><ww:url id="url" action="listar"
						namespace="/expediente/temporalidade" /> <ww:a href="%{url}">Temporalidade Documental</ww:a>
				</li>
			</c:if>
			
		</ul>
	</li>
</c:if>

<c:if
	test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios')}">

	<li><a href="#">Relat�rios</a>
		<ul id="relatorios" class="navmenu-large">
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;FORMS:Rela��o de formul�rios')}">
				<li>
					<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relFormularios.jsp">
						Rela��o de formul�rios
					</ww:a>
				</li>
			</c:if>

			<%-- Substitu�do pelo pelo "relConsultaDocEntreDatas"
		<li><ww:url id="url" action="relRelatorios"
				namespace="/expediente/rel">
				<ww:param name="nomeArquivoRel">relExpedientes.jsp</ww:param>
			</ww:url> <ww:a href="%{url}">Relat�rio de Expedientes</ww:a></li>  --%>


			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;DATAS:Rela��o de documentos entre datas')}">
				<li>
					<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relConsultaDocEntreDatas.jsp">
						Rela��o de documentos entre datas
					</ww:a>
				</li>
			</c:if>
			<!-- 
			<li><ww:url id="url" action="relRelatorios"
				namespace="/expediente/rel">
				<ww:param name="nomeArquivoRel">relModelos.jsp</ww:param>
			</ww:url> <ww:a href="%{url}">Relat�rio de Modelos</ww:a></li>
	-->
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;SUBORD:Relat�rio de documentos em setores subordinados')}">
				<li>
					<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relDocumentosSubordinados.jsp">
						Relat�rio de Documentos em Setores Subordinados
					</ww:a>
				</li>
			</c:if>

			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;MVSUB:Relat�rio de movimenta��o de documentos em setores subordinados')}">
				<li>
					<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relMovimentacaoDocSubordinados.jsp">
						Relat�rio de Movimenta��o de Documentos em Setores Subordinados
					</ww:a>
				</li>
			</c:if>
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;RELMVP:Relat�rio de movimenta��es de processos')}">
				<li>
					<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relMovProcesso.jsp">
						Relat�rio de Movimenta��es de Processos
					</ww:a>
				</li>
			</c:if>
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;CRSUB:Relat�rio de documentos criados em setores subordinados')}">
				<li>
					<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relCrDocSubordinados.jsp">
						Relat�rio de Cria��o de Documentos em Setores Subordinados
					</ww:a>
				</li>
			</c:if>
			

			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;MOVLOT:Rela��o de movimenta��es')}">
				<li>
					<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relMovimentacao.jsp">
						Relat�rio de Movimenta��es
					</ww:a>
				</li>
			</c:if>
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;MOVCAD:Rela��o de movimenta��es por cadastrante')}">
				<li>
					<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relMovCad.jsp">
						Relat�rio de Movimenta��es por Cadastrante
					</ww:a>
				</li>
			</c:if>

			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;DSPEXP:Rela��o de despachos e transfer�ncias')}">
				<li>
					<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relOrgao.jsp">
						Relat�rio de Despachos e Transfer�ncias
					</ww:a>
				</li>
			</c:if>

			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;DOCCRD:Rela��o de documentos criados')}">
				<li>
					<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relTipoDoc.jsp">
						Rela��o de Documentos Criados
					</ww:a>
				</li>
			</c:if>
			
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;CLSD:Classifica��o Documental')}">
				<li><a href="#">Classifica��o Documental</a>
					<ul>
						<c:if
							test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;CLSD:Classifica��o Documental;CLASS:Rela��o de classifica��es')}">
							<li>
								<ww:a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relClassificacao.jsp">
									Rela��o de Classifica��es
								</ww:a>
							</li>
						</c:if>
						
						<c:if
							test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;CLSD:Classifica��o Documental;DOCS:Rela��o de documentos classificados')}">
							<li> 
								<ww:a id="relclassificados" href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relDocsClassificados.jsp">
									Rela��o de Documentos Classificados
								</ww:a>
							</li>
						</c:if>
						
					</ul>		
				</li>
			</c:if>
			

		</ul></li>
</c:if>
