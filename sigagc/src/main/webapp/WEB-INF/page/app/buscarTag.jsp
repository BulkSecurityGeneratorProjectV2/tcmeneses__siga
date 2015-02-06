<%@ include file="/WEB-INF/page/include.jsp"%>

<siga:pagina titulo="Buscar">
	<div class="gt-bd clearfix">
		<div class="gt-content clearfix">
			<div class="gt-content-box gt-for-table">
				<form action="@{Application.buscarTag}" id="frm">
					<input type="hidden" name="popup" value="true" />
					<table class="gt-form-table">
						<tr class="header">
							<td align="center" valign="top" colspan="4">Dados do item</td>
						</tr>
						<tr>
							<td width="25%">Título:</td>
							<td><input type="text" name="filtro.titulo"
								value="${filtro.titulo}" />
							</td>
						</tr>
						<tr>
							<siga:select  label="Categoria" name="filtro.categoria" list="listaTagCategorias" listKey="false" listValue="false">
								#{option null}[Todas]#{/option}
								</siga:select>
						</tr>
						<tr>
							<td><input type="hidden" name="nome" value="${nome}" /> <input
								type="hidden" name="pessoa" value="${pessoa}" /> <input
								type="submit" class="gt-btn-medium gt-btn-left" value="Pesquisar" />
							</td>
						</tr>
					</table>
				</form>
			</div>
		</div>
	
		<br />
	
		<div class="gt-content-box gt-for-table">
			<table class="gt-table">
				<tr>
					<th>Título
					</td>
					<th>Categoria
					</td>
				</tr>
				<c:forEach var="item" items="${itens}">
				<tr>
					<td><a
						href="javascript:opener.retorna_item${nome}('${item.id}','${item.sigla}','${item.descricao}');window.close()">
							<siga:selecionado sigla="${item.sigla}" descricao="${item.descricao}" /></a>
					</td>
					<td>${item.categoria}</td>
				</tr>
				</c:forEach>
			</table>
		</div>
	</div>
	
	<script language="javascript">
	function sbmt(nivel){
		document.getElementById('alterou').value=nivel;
		frm.submit();
	}
	</script>

</siga:pagina>
