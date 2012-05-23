<%@ include file="/WEB-INF/page/include.jsp"%><!--  -->

<siga:pagina titulo="Tarefa">
	<div class="gt-bd gt-cols clearfix">
		<div class="gt-content clearfix">

			<div id="desc_editar" style="display: none;">
				<h3>Descri��o da Tarefa</h3>
				<div class="gt-form gt-content-box">
					<ww:url action="saveKnowledge" id="url"></ww:url>
					<form method="GET" action="${url}">
						<input name="tiId" type="hidden" value="${tiId}" />
						<div class="gt-form-row gt-width-100">
							<label>Descri��o</label>
							<textarea cols="80" rows="15" name="conhecimento"
								class="gt-form-textarea">${task.conhecimento}</textarea>
						</div>
						<div class="gt-form-row gt-width-100">
							<input name="salvar_conhecimento" type="submit" value="Salvar"
								class="gt-btn-medium gt-btn-left" />
						</div>
					</form>
				</div>
			</div>

			<!-- Adicionando a lista de Tarefas -->
			<ww:url action="executeTask" id="url"></ww:url>
			<form method="GET" action="${url}">
				<h3>Execu��o da Tarefa</h3>
				<div class="gt-form gt-content-box">
					<div class="gt-form-row gt-width-100">
						<span id="desc_ver">${task.descricao}</span>
						<c:if test="${task.conhecimentoEditavel}">
							<tr>
								<td><input name="editar_conhecimento" type="button"
									value="Editar a descri��o" id="desc_but_editar"
									onclick="javascript: document.getElementById('desc_ver').style.display='none'; document.getElementById('desc_editar').style.display=''; document.getElementById('desc_but_editar').style.display='none'; document.getElementById('desc_but_gravar').style.display=''; " />
									<input name="salvar_conhecimento" type="submit"
									id="desc_but_gravar" value="Salvar" style="display: none"
									onclick="javascript: this.form.action='${url}'; " /></td>
							</tr>
						</c:if>
					</div>
					<div class="gt-form-row gt-width-100">
						<table class="gt-form-table">
							<input type="hidden" value="${task.taskInstance.id}" name="tiId" />

							<c:forEach var="variable" items="${task.variableList}">
								<c:if test="${not variable.aviso}">
									<tr>
										<ww:if test="%{#attr.variable.mappedName.startsWith('sel_')}">
											<td width="">${fn:substring(variable.variableName,0,fn:indexOf(variable.variableName,'('))}</td>
										</ww:if>
										<ww:else>
											<td width="">${variable.variableName}</td>
										</ww:else>

										<td width=""><c:set var="editable"
												value="${variable.writable and (variable.readable or empty taskInstance.token.processInstance.contextInstance.variables[variable.mappedName])}" />
											<c:if test="${editable}">
												<input name="fieldNames" type="hidden"
													value="${variable.mappedName}" />
											</c:if> <ww:if
												test="%{#attr.variable.mappedName.startsWith('doc_')}">
												<c:choose>
													<c:when test="${editable}">
														<siga:selecao propriedade="${variable.mappedName}"
															modulo="../sigaex" tipo="expediente" tema="simple"
															ocultardescricao="sim"
															siglaInicial="${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}" />
													</c:when>
													<c:otherwise>
														<a
															href="/sigaex/expediente/doc/exibir.action?sigla=${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}">${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}</a>
													</c:otherwise>
												</c:choose>
											</ww:if> <ww:elseif
												test="%{#attr.variable.mappedName.startsWith('pes_')}">
												<c:choose>
													<c:when test="${editable}">
														<siga:selecao propriedade="${variable.mappedName}"
															modulo="../sigaex" tipo="pessoa" tema="simple"
															ocultardescricao="sim"
															siglaInicial="${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}" />
													</c:when>
													<c:otherwise>
									${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}
								</c:otherwise>
												</c:choose>
											</ww:elseif> <ww:elseif
												test="%{#attr.variable.mappedName.startsWith('lot_')}">
												<c:choose>
													<c:when test="${editable}">
														<siga:selecao propriedade="${variable.mappedName}"
															modulo="../sigaex" tipo="lotacao" tema="simple"
															ocultardescricao="sim"
															siglaInicial="${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}" />
													</c:when>
													<c:otherwise>
									${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}
								</c:otherwise>
												</c:choose>
											</ww:elseif> <ww:elseif
												test="%{#attr.variable.mappedName.startsWith('dt_')}">
												<c:choose>
													<c:when test="${editable}">
														<input name="fieldValues" type="text"
															value="<fmt:formatDate pattern="dd/MM/yyyy"	value="${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}" />"
															onblur="javascript:verifica_data(this, true);" />
													</c:when>
													<c:otherwise>
														<fmt:formatDate pattern="dd/MM/yyyy"
															value="${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}" />
													</c:otherwise>
												</c:choose>
											</ww:elseif> <ww:elseif
												test="%{#attr.variable.mappedName.startsWith('sel_')}">
												<c:choose>
													<c:when test="${editable}">
														<select name="fieldValues">
															<c:forEach var="opcao"
																items="${wf:listarOpcoes(variable.variableName)}">
																<option value="${opcao}">${opcao}</option>
															</c:forEach>
														</select>
													</c:when>
													<c:otherwise>
										${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}
									</c:otherwise>
												</c:choose>
											</ww:elseif> <ww:else>
												<c:choose>
													<c:when test="${editable}">
														<input name="fieldValues" type="text"
															value="${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}" />
													</c:when>
													<c:otherwise>
									${taskInstance.token.processInstance.contextInstance.variables[variable.mappedName]}
									</c:otherwise>
												</c:choose>
											</ww:else></td>
									</tr>
								</c:if>
							</c:forEach>
						</table>
					</div>
					<c:if
						test="${(titular.sigla eq taskInstance.actorId) or (wf:podePegarTarefa(cadastrante, titular,lotaCadastrante,lotaTitular,taskInstance) == true)}">
						<div class="gt-form-row gt-width-100">
							<c:forEach var="transition" items="${task.transitions}">
								<input name="transitionName" type="submit"
									value="${empty transition.name ? 'Prosseguir' : transition.name}${transition.resp}"
									class="gt-btn-large gt-btn-left" />
							</c:forEach>
						</div>
					</c:if>
					<c:forEach var="variable" items="${task.variableList}">
						<ww:if test="%{#attr.variable.mappedName.startsWith('doc_')}">
							<!-- 							<c:if test="${variable.aviso}">  -->
							<!-- 							</c:if>	-->
							<span style="color: red; font-weight: bold;">
								${task.msgAviso}</span>
						</ww:if>
					</c:forEach>
				</div>



























			</form>
			<c:if
				test="${(titular.sigla eq taskInstance.actorId) or (wf:podePegarTarefa(cadastrante, titular,lotaCadastrante,lotaTitular,taskInstance))}">
				<h3 class="gt-form-head">Designa�ao da Tarefa</h3>
				<div class="gt-form gt-content-box">
					<ww:url id="url" action="assignTask" />
					<form method="GET" action="${url}">
						<input name="tiId" type="hidden" value="${tiId}" />
						<div class="gt-form-row gt-width-100">
							<label>Pessoa</label>
							<siga:selecao propriedade="ator" modulo="../sigaex" tema="simple" />
						</div>
						<div class="gt-form-row gt-width-100">

							<label>Lota��o</label>
							<siga:selecao propriedade="lotaAtor" modulo="../sigaex"
								tema="simple" />
						</div>
						<div class="gt-form-row gt-width-33" style="float: left">
							<label>Prioridade</label>
							<ww:select name="prioridade"
								list="#{1:'Muito Alta', 2:'Alta', 3:'M�dia', 4:'Baixa', 5:'Muito Baixa'}"
								theme="simple" />
						</div>
						<div class="gt-form-row gt-width-66" style="float: right">
							<label>Justificativa (opcional)</label> <input type="text"
								name="justificativa" style="width: 100%" />
						</div>
						<div class="gt-form-row gt-width-100" style="clear: both">
							<input name="designar" type="submit" value="Designar"
								class="gt-btn-medium gt-btn-left" />
							<c:if test="${empty taskInstance.actorId}">
								<ww:url id="url" action="pegar">
									<ww:param name="tiId">${taskInstance.id}</ww:param>
								</ww:url>
								<input type="button" value="Pegar tarefa para mim"
									onclick="javascript:window.location.href='${url}'"
									class="gt-btn-medium gt-btn-left">
							</c:if>
						</div>
					</form>
				</div>

				<h3>Coment�rios</h3>
				<div class="gt-content-box">
					<table class="gt-table">
						<thead>
							<th>Data/Hora</th>
							<th>Atendente</th>
							<th>Descri��o</th>
						</thead>
						<c:forEach var="ti" items="${wf:ordenarTarefas(taskInstance)}">
							<c:forEach var="c" items="${wf:ordenarComentarios(ti)}">
								<tr>
									<td>${f:espera(c.time)}</td>
									<td><ww:property value="%{#attr.c.actorId}" /></td>
									<td><ww:property value="%{#attr.c.message}" /></td>
								</tr>
							</c:forEach>
							<tr>
								<td>${f:espera(ti.create)}</td>
								<td><ww:property value="%{#attr.ti.actorId}" /></td>
								<td><b><ww:property value="%{#attr.ti.name}" /> </b></td>
							</tr>
						</c:forEach>
					</table>
				</div>
				<div class="gt-form gt-content-box">
					<ww:url id="url" action="commentTask" />
					<form method="GET" action="${url}">
						<input name="tiId" type="hidden" value="${tiId}" /> <label>Coment�rio</label>
						<div class="gt-form-row gt-width-100">
							<input type="text" size="80" name="comentario"
								class="gt-form-text" />
						</div>
						<div class="gt-form-row gt-width-100">
							<input name="butc" type="submit" value="Adicionar"
								class="gt-btn-medium gt-btn-left" />
						</div>
					</form>
				</div>
			</c:if>
		</div>

		<div class="gt-sidebar">
			<!-- Sidebar Content -->
			<div class="gt-sidebar-content">
				<h3>Dados da Tarefa</h3>
				<p>
					<b>Procedimento:</b> ${taskInstance.task.processDefinition.name}
				</p>
				<p>
					<b>Tarefa:</b> ${taskInstance.task.name}
				</p>
				<p>
					<b>Prioridade:</b>
					<c:choose>
						<c:when test="${taskInstance.priority == 1}">Muito Alta</c:when>
						<c:when test="${taskInstance.priority == 2}">Alta</c:when>
						<c:when test="${taskInstance.priority == 3}">M�dia</c:when>
						<c:when test="${taskInstance.priority == 4}">Baixa</c:when>
						<c:when test="${taskInstance.priority == 51}">Muito Baixa</c:when>
					</c:choose>
				</p>
				<p>
					<b>Cadastrante:</b>
					<ww:property
						value="%{#attr.taskInstance.getVariable('wf_cadastrante')}" />
					(
					<ww:property
						value="%{#attr.taskInstance.getVariable('wf_lota_cadastrante')}" />
					)
				</p>
				<p>
					<b>Titular:</b>
					<ww:property
						value="%{#attr.taskInstance.getVariable('wf_titular')}" />
					(
					<ww:property
						value="%{#attr.taskInstance.getVariable('wf_lota_titular')}" />
					)
				</p>
				<p>
					<b>In�cio:</b> ${f:espera(taskInstance.create)}
				</p>
			</div>
			<!-- /Sidebar Content -->
			<%--
			<!-- Sidebar Navigation -->
			<div class="gt-sidebar-nav gt-sidebar-nav-brown">
				<h3>Quick Links</h3>
				<ul>
					<li><a href="http://www.gooeytemplates.com" target="_blank">GooeyTemplates.com</a>
					</li>
				</ul>
			</div>
			<!-- /Sidebar Navigation -->

			<!-- Sidebar Navigation -->
			<div class="gt-sidebar-nav gt-sidebar-nav-green">
				<h3>Contact Us</h3>
				<ul>
					<li><a href="mailto:help@gooeytemplates.com">Email Gooey
							Templates</a></li>
				</ul>
			</div>
			<!-- /Sidebar Navigation -->

			<!-- Sidebar Box -->
			<div class="gt-sidebar-box gt-sidebar-box-green">
				<!-- search -->
				<div class="gt-search">
					<div class="gt-search-inner">
						<input type="text" class="gt-search-text" value="Find an Article"
							onfocus="javascript:if(this.value=='Find an Article')this.value='';"
							onblur="javascript:if(this.value=='')this.value='Find an Article';" />
					</div>
				</div>
				<!-- /search -->
			</div>
			<!-- /Sidebar Box -->
--%>
			<!-- Sidebar List -->
			<div class="gt-sidebar-list">
				<h3>Mapa do Procedimento</h3>
				<ul class="gt-sidebar-list-content">
					<li class="gt-sidebar-list-row">
						<%--<div style="width: 251px">
							<tags:wfImage task="${taskInstance.id}"
								token="${taskInstance.token.id}" />
						</div> --%>
					</li>
				</ul>
			</div>
			<!-- /Sidebar List -->

		</div>
		<!-- / sidebar -->
</siga:pagina>