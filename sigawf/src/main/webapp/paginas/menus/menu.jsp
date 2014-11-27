<%@ page language="java" contentType="text/html; charset=UTF-8"
	buffer="128kb"%>
<%@ taglib prefix="c" uri="/core"%>
<%@ taglib uri="http://localhost/libstag" prefix="ff"%>


<li><a href="#">Procedimentos</a>
	<ul>
		<li><a href="${linkTo[AppController].resumo}">Ativos</a>
		</li>

		<c:if
			test="${ff:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;WF;OPERAR:Executar comandos da tela inicial')}">
			<li><a href="#">Iniciar</a>
				<ul class="navmenu-large">
					<c:forEach var="pd" items="${processDefinitions}">
						<li><a href="${linkTo[AppController].initializeProcess[pd.id]}">${pd.name}</a>
						</li>
					</c:forEach>
				</ul></li>
		</c:if>
	</ul> <c:if
		test="${ff:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;WF;FE:Ferramentas')}">
		<li><a href="#">Ferramentas</a>
			<ul>
				<c:if
					test="${ff:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;WF;CONFIGURAR:Configurar iniciadores')}">
					<li><a href="#">Configurar</a>
						<ul class="navmenu-large">
							<c:forEach var="pd" items="${processDefinitions}">
								<li><a href="${linkTo[ConfiguracaoController].pesquisar[lotaTitular.orgaoUsuario.acronimoOrgaoUsu][pd.name]}">${pd.name}</a>
								</li>
							</c:forEach>
						</ul></li>
				</c:if>

				<c:if
					test="${ff:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;WF;DESIGNAR:Designar tarefas')}">
					<li><a href="#">Designar Tarefas</a>
						<ul class="navmenu-large">
							<c:forEach var="pd" items="${processDefinitions}">
								<li><a href="${linkTo[DesignacaoController].pesquisar[lotaTitular.orgaoUsuario.acronimoOrgaoUsu][pd.name]}">${pd.name}</a>
								</li>
							</c:forEach>
						</ul></li>
				</c:if>
				
				<c:if
					test="${ff:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;WF;EDITAR:Editar procedimento')}">
					<li><a href="#">Editar Procedimento</a>
						<ul class="navmenu-large">
							<c:forEach var="pd" items="${processDefinitions}">
								<li><a href="${linkTo[EdicaoController].form[pd.name]}">${pd.name}</a>
								</li>
							</c:forEach>
						</ul></li>
				</c:if>
			</ul>
		</li>
	</c:if>
</li>
<c:if
	test="${ff:podeUtilizarServicoPorConfiguracao(titular,lotaTitular,'SIGA;WF;MEDIR: Analisar métricas')}">

	<li><a href="#">Relatórios</a>
		<ul>
			<li><a href="#">Apresentar Métricas</a>
				<ul class="navmenu-large">
					<c:forEach var="pd" items="${processDefinitions}">
						<li><a href="${linkTo[MedicaoController].medir}?orgao=${lotaTitular.orgaoUsuario.acronimoOrgaoUsu}&procedimento=${pd.name}">${pd.name}</a>
						</li>
					</c:forEach>
				</ul></li>
		</ul>
	</li>
	</li>
</c:if>