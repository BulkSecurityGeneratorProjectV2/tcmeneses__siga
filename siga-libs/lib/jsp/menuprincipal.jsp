<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	buffer="64kb"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://localhost/libstag" prefix="f"%>
<%@ taglib prefix="ww" uri="/webwork"%>

<!-- Mensagens remotas 
	<div id="mensagens-remotas"></div>
	<c:url value="/" var="url"></c:url>
	<script type="text/javascript" src="${url}sigalibs/mensagensremotas.js"></script>
	<script type="text/javascript" >
		exibirMensagensRemotas("arquivos/mensagens/mensagens_remotas.xml"
								, "mensagens-remotas"
								, "color: yellow; display: inline-block; font-weight: bolder;  font-size:medium; position: relative; width: 100%; text-align: center; background-color: black; vertical-align: middle; border-width: 1px; border-color: #254189 ; border-style: solid;"
								);
	</script> -->
<!-- Fim das mensagens remotas  -->

<!--|**START IMENUS**|imenus0,inline-->
<!--[if IE]><style type="text/css">.imcm .imea span{position:absolute;}.imcm .imclear,.imclear{display:none;}.imcm{zoom:1;} .imcm li{curosr:hand;} .imcm ul{zoom:1}.imcm a{zoom:1;}</style><![endif]-->
<!--[if gte IE 7]><style type="text/css">.imcm .imsubc{background-image:url(ie_css_fix);}</style><![endif]-->

<!--|**START IMENUS**|imenus1,inline-->
<!--[if IE]><style type="text/css">.imcm .imea span{position:absolute;}.imcm .imclear,.imclear{display:none;}.imcm{zoom:1;} .imcm li{curosr:hand;} .imcm ul{zoom:1}.imcm a{zoom:1;}</style><![endif]-->
<!--[if gte IE 7]><style type="text/css">.imcm .imsubc{background-image:url(ie_css_fix);}</style><![endif]-->

<!-- <body style="padding: 0px 0px 0px 0px; margin: 0px 0px 0px 0px;"> -->

<div style="width: 100%; background-color: #254189">
<div
	style="z-index: 999999; padding: 0px 0px 0px 0px; margin: 0px 0px 0px 0px; float: left">
<div class="imrcmain0 imgl"
	style="width: 186px; z-index: 999999; position: relative; float: left">
<div class="imcm imde" id="imouter0" style="float: left">
<ul id="imenus0">
	<li class="imatm" style="width: 186px;"><a class="" href="#">
	<span class="imea imeam"><span></span></span>SIGA</a>
	<div class="imsc">
	<div class="imsubc" style="width: 186px; top: 0px; left: 0px;">
	<ul style="">
		<li><ww:url id="url" action="principal" namespace="/" /> <ww:a
			href="%{url}">P�gina Inicial</ww:a></li>

		<c:if test="${empty pagina_de_erro}">

			<li><a href="#"> <span class="imea imeas"><span></span></span>M�dulos</a>
			<div class="imsc">
			<div class="imsubc" style="width: 155px; top: -23px; left: 139px;">
			<ul style="">
				<li><a
					href="/sigaex/expediente/doc/listar.action?primeiraVez=sim">Documentos</a></li>
				<li><a href="/sigatr/">Treinamento</a></li>
				<li><a href="/SigaServicos/">Servi�os</a></li>
				<li><a href="/siga-beneficios/">Benef�cios</a></li>
			</ul>
			</div>
			</div>
			</li>




			<li><a href="#"> <span class="imea imeas"><span></span></span>Administra��o</a>
			<div class="imsc">
			<div class="imsubc" style="width: 155px; top: -23px; left: 139px;">
			<ul style="">
				<li><ww:a href="/siga/trocar_senha.action">Trocar	senha</ww:a></li>
				<li><a href="/siga/substituicao/substituir.action">Entrar
				como substituto</a></li>
				<%--
			<li><ww:url id="url" action="substituir"
				namespace="/substituicao" /><ww:a href="%{url}">Entrar como substituto</ww:a></li>
			--%>
				<c:if
					test="${(not empty lotaTitular && lotaTitular.idLotacao!=cadastrante.lotacao.idLotacao) ||(not empty titular && titular.idPessoa!=cadastrante.idPessoa)}">
					<%--
				<li><ww:url id="url" action="finalizar"
					namespace="/substituicao" /> <ww:a href="%{url}">
					Finalizar substitui��o de 
					<c:choose>
						<c:when
							test="${not empty titular && titular.idPessoa!=cadastrante.idPessoa}">${titular.nomePessoa}</c:when>
						<c:otherwise>${lotaTitular.sigla}</c:otherwise>
					</c:choose>
				</ww:a></li>
			--%>

					<li><ww:a href="/siga/substituicao/finalizar.action">
					Finalizar substitui��o de 
					<c:choose>
							<c:when
								test="${not empty titular && titular.idPessoa!=cadastrante.idPessoa}">${titular.nomePessoa}</c:when>
							<c:otherwise>${lotaTitular.sigla}</c:otherwise>
						</c:choose>
					</ww:a></li>

				</c:if>

				<%--
			<li><ww:url id="url" action="listar" namespace="/substituicao" /><ww:a
				href="%{url}">Gerenciar poss�veis substitutos</ww:a></li>
			--%>
				<li><ww:a href="/siga/substituicao/listar.action">Gerenciar poss�veis substitutos</ww:a></li>
			</ul>
			</div>
			</div>
			</li>


			<c:if
				test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;GI:M�dulo de Gest�o de Identidade')}">
				<li><a href="#"> <span class="imea imeas"><span></span></span>Gest�o
				de Identidade</a>
				<div class="imsc">
				<div class="imsubc" style="width: 155px; top: -23px; left: 139px;">
				<ul style="">
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;ID:Gerenciar identidades')}">
						<li><ww:a href="/siga/gi/identidade/listar.action">Identidade</ww:a></li>
					</c:if>
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;PERMISSAO:Gerenciar permiss�es')}">
						<li><ww:a href="/siga/gi/acesso/listar.action">Configurar Permiss�es</ww:a></li>
					</c:if>
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;PERFIL:Gerenciar perfis de acesso')}">
						<li><ww:a href="/siga/gi/perfil/listar.action">Perfil de Acesso</ww:a></li>
					</c:if>
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;PERFILJEE:Gerenciar perfis do JEE')}">
						<li><ww:a href="/siga/gi/perfiljee/listar.action">Perfil de Acesso do JEE</ww:a></li>
					</c:if>
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;GEMAIL:Gerenciar grupos de email')}">
						<li><ww:a href="/siga/gi/email/listar.action">Grupo de Email</ww:a></li>
					</c:if>
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;SELFSERVICE:Gerenciar servi�os da pr�pria lota��o')}">
						<li><ww:a href="/siga/gi/servico/acesso.action">Acesso a Servi�os</ww:a></li>
					</c:if>
					<c:if
						test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;GI;REL:Gerar relat�rios')}">
						<li><a href="#"> <span class="imea imeas"><span></span></span>Relat�rios</a>
						<div class="imsc">
						<div class="imsubc" style="width: 155px; top: -23px; left: 139px;">
						<ul style="">
							<li><ww:a
								href="/siga/gi/relatorio/selecionar_acesso_servico.action">Acesso aos Servi�os</ww:a></li>
							<li><ww:a
								href="/siga/gi/relatorio/selecionar_permissao_usuario.action">Permiss�es de Usu�rio</ww:a></li>
							<li><ww:a
								href="/siga/gi/relatorio/selecionar_alteracao_direitos.action">Altera��o de Direitos</ww:a></li>
							<li><ww:a
								href="/siga/gi/relatorio/selecionar_historico_usuario.action">Hist�rico de Usu�rio</ww:a></li>
						</ul>
						</div>
						</div>
						</li>
					</c:if>
				</ul>
				</div>
				</div>
				</li>
			</c:if>

		</c:if>
		
		
		<c:if test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA:Sistema Integrado de Gest�o Administrativa;FE:Ferramentas')}">
			<li><a href="#"> <span class="imea imeas"><span></span></span>Ferramentas</a>
			<div class="imsc">
			<div class="imsubc" style="width: 155px; top: -23px; left: 139px;">
			<ul style="">
				<c:if
					test="${f:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;FE;MODVER:Visualizar modelos')}">
					<li><ww:a href="/siga/modelo/listar.action">Cadastro de modelos</ww:a></li>
				</c:if>
			</ul>
			</div>
			</div>
			</li>
		</c:if>

		

		<li><a target="_blank"
			href="/wiki/Wiki.jsp?page=${f:removeAcento(titulo)}">Ajuda</a></li>
		<li><ww:a href="/siga/logoff.action">Logoff</ww:a></li>
		<%--
								<li><a href="#">
									<span class="imea imeas"><span></span></span>Gest&atilde;o Documental</a>
									<div class="imsc">
										<div class="imsubc" style="width:155px;top:-23px;left:139px;">
											<ul style="">
												<li><a href="#">Expedientes</a></li>
												<li><a href="#">Malotes</a></li>
												<li><a href="#">Processos Administrativos</a></li>
												<li><a href="#">Correspondencia</a></li>
												<li><a href="#">Boletim Interno</a></li>
												<li><a href="#">Arquivamento</a></li>
											</ul>
										</div>
									</div>
								</li>
								<li><a href="#">
									<span class="imea imeas"><span></span></span>Gest&atilde;o do Trabalho</a>
									<div class="imsc">
										<div class="imsubc" style="width:155px;top:-23px;left:139px;">
											<ul style="">
												<li><a href="#">Servi&ccedil;os</a></li>
												<li><a href="#">Projetos</a></li>
												<li><a href="#">Manuten&ccedil;&otilde;es Preventivas</a></li>
												<li><a href="#">Reprografia</a></li>
												<li><a href="#">Liga&ccedil;&otilde;es Telef&ocirc;nicas</a></li>
												<li><a href="#">Ve&iacute;culos</a></li>
											</ul>
										</div>
									</div>
								</li>
								<li><a href="#">
									<span class="imea imeas"><span></span></span>Recursos Humanos</a>
									<div class="imsc">
										<div class="imsubc" style="width:155px;top:-23px;left:139px;">
											<ul style="">
												<li><a href="/SigaRH/Cadastro.action">Cadastro</a></li>
												<li><a href="/SigaRH/beneficios/index.jsp">Benef&iacute;cios</a></li>
												<li><a href="#">Treinamento</a></li>
												<li><a href="#">&Oacute;rg&atilde;os Externos</a></li>
												<li><a href="#">Avalia&ccedil;&atilde;o de Desempenho</a></li>
											</ul>
										</div>
									</div>
								</li>
								<li><a href="#">
									<span class="imea imeas"><span></span></span>Gest&atilde;o Patrimonial</a>
									<div class="imsc">
										<div class="imsubc" style="width:155px;top:-23px;left:139px;">
											<ul style="">
												<li><a href="#">Aquisi&ccedil;&otilde;es</a></li>
												<li><a href="#">Contratos</a></li>
												<li><a href="#">Patrim&ocirc;nio</a></li>
												<li><a href="#">Biblioteca</a></li>
												<li><a href="#">Estoque Local</a></li>
											</ul>
										</div>
									</div>
								</li>
								<li><a href="#">
									<span class="imea imeas"><span></span></span>Gest&atilde;o Institucional</a>
									<div class="imsc">
										<div class="imsubc" style="width:155px;top:-23px;left:139px;">
											<ul style="">
												<li><a href="#">Conhecimento</a></li>
												<li><a href="#">Apoio Gerencial</a></li>
											</ul>
										</div>
									</div>
								</li>
								<li><a href="#">
									<span class="imea imeas"><span></span></span>Administra&ccedil;&atilde;o</a>
									<div class="imsc">
										<div class="imsubc" style="width:155px;top:-23px;left:139px;">
											<ul style="">
												<li><a href="#">Acesso</a></li>
												<li><a href="#">Prefer&ecirc;ncias Pessoais</a></li>
												<li><a href="#">Colabora&ccedil;&atilde;o</a></li>
												<li><a href="#">Qualidade</a></li>
												<li><a href="#">Tabelas Institucionais</a></li>
												<li><a href="#">Manuten&ccedil;&atilde;o do Sistema</a></li>
												<li><a href="#">Ger&ecirc;ncia de Mensagens</a></li>
												<li><a href="#">Cadastro da Institui&ccedil;&atilde;o</a></li>
												<li><a href="#">Apoio ao Usu&aacute;rio</a></li>
											</ul>
										</div>
									</div>
								</li>
--%>
	</ul>
	</div>
	</div>
	</li>
</ul>
</div>
</div>
<div class="imclear"></div>
</div>

<c:import url="/paginas/menus/menu.jsp"></c:import>

<div class="imclear"></div>
</div>

<%--
<c:if test="${not empty barranav}">
	<c:if test="${barranav!='nao'}">
		<c:import url="/sigalibs/barranav.jsp"></c:import>
	</c:if>
</c:if>
 --%>
<c:import url="/sigalibs/staticjs.jsp"></c:import>
