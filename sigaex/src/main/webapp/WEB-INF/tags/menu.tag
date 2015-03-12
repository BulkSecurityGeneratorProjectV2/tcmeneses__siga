<%@ tag body-content="empty"%>
<%@ taglib uri="/WEB-INF/tld/func.tld" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://localhost/jeetags" prefix="siga"%>

<li><a href="#">Documentos</a>
	<ul>
		<li>
			<a href="/sigaex/app/expediente/doc/editar">Novo</a>
		</li>
		<li>
			<a href="/sigaex/app/expediente/doc/listar?primeiraVez=sim">Pesquisar</a>
		</li>
		
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
			<c:if test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;ASS:Assinatura digital;EXT:Extens�o')}">
				<li>
					<siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/assinar_lote" texto="Assinar em lote" />
				</li>
			</c:if>
		</c:catch>
		<c:catch>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;ASS:Assinatura digital;EXT:Extens�o')}">
				<li> 
					<siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/assinar_despacho_lote" texto="Assinar Despacho em lote" />
				</li>
			</c:if>
		</c:catch>
		<li> 
			<siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/arquivar_corrente_lote" texto="Arquivar em lote" />
		</li>
		<c:catch>
			<c:if
				test="${f:podeArquivarPermanentePorConfiguracao(titular,lotaTitular)}">
				<li> 
					<siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/arquivar_intermediario_lote" texto="Arquivar Intermedi�rio em lote" />
				</li>
			</c:if>
		</c:catch>
		<c:catch>
			<c:if
				test="${f:podeArquivarPermanentePorConfiguracao(titular,lotaTitular)}">
				<li> 
					<siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/arquivar_permanente_lote" texto="Arquivar Permanente em lote" />
				</li>
			</c:if>
		</c:catch>
		<c:catch>
			<c:if
				test="${f:testaCompetencia('atenderPedidoPublicacao',titular,lotaTitular,null)}">
				<li>
					<siga:monolink href="${pageContext.request.contextPath}/app/expediente/mov/atender_pedido_publicacao" texto="Gerenciar Publica��o DJE" />
<%-- 					<ww:url id="url" action="atender_pedido_publicacao" --%>
<%-- 						namespace="/expediente/mov" /> <a href="%{url}">Gerenciar Publica��o DJE</a> --%>
				</li>
			</c:if>
		</c:catch>
		<%--<c:catch>
			<c:if
				test="${f:testaCompetencia('definirPublicadoresPorConfiguracao',titular,lotaTitular,null)}">
				<li><ww:url id="url" action="definir_publicadores"
					namespace="/expediente/configuracao" /><a href="%{url}">Definir Publicadores DJE</a></li>
			</c:if>
		</c:catch>--%>
		<c:catch>
			<c:if
				test="${f:testaCompetencia('gerenciarPublicacaoBoletimPorConfiguracao',titular,lotaTitular,null)}">
				<li>
					<a href="${pageContext.request.contextPath}/app/expediente/configuracao/gerenciar_publicacao_boletim">Definir Publicadores Boletim</a>
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
			<li><a href="/sigaex/app/modelo/listar">Cadastro de modelos</a>
			</li>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas;DESP:Tipos de despacho')}">
				<li><a href="/sigaex/app/despacho/tipodespacho/listar">Cadastro de tipos de despacho</a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas;CFG:Configura��es')}">
				<li> <a href="/sigaex/app/expediente/configuracao/listar">Cadastro de configura��es</a>
				</li>
			</c:if>
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas;EMAIL:Email de Notifica��o')}">
				<li> <a href="/sigaex/app/expediente/emailNotificacao/listar">Cadastro de email de notifica��o</a>
				</li>
			</c:if>
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas;PC:Plano de Classifica��o')}">
				<a href="/sigaex/app/expediente/classificacao/listar">Classifica��o Documental</a>
				</li>
			</c:if>
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;FE:Ferramentas;TT:Tabela de Temporalidade')}">
				<li> <a href="${pageContext.request.contextPath}/app/expediente/temporalidade/listar">Temporalidade Documental</a>
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
					<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relFormularios.jsp">
						Rela��o de formul�rios
					</a>
				</li>
			</c:if>

			<%-- Substitu�do pelo pelo "relConsultaDocEntreDatas"
		<li><ww:url id="url" action="relRelatorios"
				namespace="/expediente/rel">
				<ww:param name="nomeArquivoRel">relExpedientes.jsp</ww:param>
			</ww:url> <a href="%{url}">Relat�rio de Expedientes</a></li>  --%>


			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;DATAS:Rela��o de documentos entre datas')}">
				<li>
					<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relConsultaDocEntreDatas.jsp">
						Rela��o de documentos entre datas
					</a>
				</li>
			</c:if>
			<!-- 
			<li><ww:url id="url" action="relRelatorios"
				namespace="/expediente/rel">
				<ww:param name="nomeArquivoRel">relModelos.jsp</ww:param>
			</ww:url> <a href="%{url}">Relat�rio de Modelos</a></li>
	-->
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;SUBORD:Relat�rio de documentos em setores subordinados')}">
				<li>
					<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relDocumentosSubordinados.jsp">
						Relat�rio de Documentos em Setores Subordinados
					</a>
				</li>
			</c:if>

			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;MVSUB:Relat�rio de movimenta��o de documentos em setores subordinados')}">
				<li>
					<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relMovimentacaoDocSubordinados.jsp">
						Relat�rio de Movimenta��o de Documentos em Setores Subordinados
					</a>
				</li>
			</c:if>
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;RELMVP:Relat�rio de movimenta��es de processos')}">
				<li>
					<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relMovProcesso.jsp">
						Relat�rio de Movimenta��es de Processos
					</a>
				</li>
			</c:if>
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;CRSUB:Relat�rio de documentos criados em setores subordinados')}">
				<li>
					<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relCrDocSubordinados.jsp">
						Relat�rio de Cria��o de Documentos em Setores Subordinados
					</a>
				</li>
			</c:if>
			

			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;MOVLOT:Rela��o de movimenta��es')}">
				<li>
					<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relMovimentacao.jsp">
						Relat�rio de Movimenta��es
					</a>
				</li>
			</c:if>
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;MOVCAD:Rela��o de movimenta��es por cadastrante')}">
				<li>
					<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relMovCad.jsp">
						Relat�rio de Movimenta��es por Cadastrante
					</a>
				</li>
			</c:if>

			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;DSPEXP:Rela��o de despachos e transfer�ncias')}">
				<li>
					<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relOrgao.jsp">
						Relat�rio de Despachos e Transfer�ncias
					</a>
				</li>
			</c:if>

			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;DOCCRD:Rela��o de documentos criados')}">
				<li>
					<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relTipoDoc.jsp">
						Rela��o de Documentos Criados
					</a>
				</li>
			</c:if>
			
			
			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;CLSD:Classifica��o Documental')}">
				<li><a href="#">Classifica��o Documental</a>
					<ul>
						<c:if
							test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;CLSD:Classifica��o Documental;CLASS:Rela��o de classifica��es')}">
							<li>
								<a href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relClassificacao.jsp">
									Rela��o de Classifica��es
								</a>
							</li>
						</c:if>
						
						<c:if
							test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;DOC:M�dulo de Documentos;REL:Gerar relat�rios;CLSD:Classifica��o Documental;DOCS:Rela��o de documentos classificados')}">
							<li> 
								<a id="relclassificados" href="${pageContext.request.contextPath}/app/expediente/rel/relRelatorios?nomeArquivoRel=relDocsClassificados.jsp">
									Rela��o de Documentos Classificados
								</a>
							</li>
						</c:if>
						
					</ul>		
				</li>
			</c:if>
			

		</ul></li>
</c:if>
