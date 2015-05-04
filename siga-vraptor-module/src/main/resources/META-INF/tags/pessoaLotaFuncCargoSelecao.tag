<%@ tag body-content="scriptless"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%-- <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%> --%>
<%@ taglib uri="http://localhost/jeetags" prefix="siga"%>

<%@ attribute name="nomeSelPessoa" required="false"%>
<%@ attribute name="nomeSelLotacao" required="false"%>
<%@ attribute name="nomeSelFuncao" required="false"%>
<%@ attribute name="nomeSelCargo" required="false"%>
<%@ attribute name="nomeSelGrupo" required="false"%>

<%@ attribute name="valuePessoa" required="false"%>
<%@ attribute name="valueLotacao" required="false"%>
<%@ attribute name="valueFuncao" required="false"%>
<%@ attribute name="valueCargo" required="false"%>
<%@ attribute name="valueGrupo" required="false"%>

<%@ attribute name="disabled" required="false"%>
<%@ attribute name="onchange" required="false"%>
<%@ attribute name="cssClass" required="false"%>
<%@ attribute name="id" required="false"%>

<c:set var="desativar" value="nao"></c:set>
<c:if test="${disabled == 'true'}">
	<c:set var="pessoaLotaFuncCargoSelecaoDisabled" value="disabled='disabled'"></c:set>
	<c:set var="desativar" value="sim"></c:set>
</c:if>

<select id="${nomeSelPessoa}${nomeSelLotacao}${nomeSelFuncao}${nomeSelCargo}${nomeSelGrupo}" onchange="" ${pessoaLotaFuncCargoSelecaoDisabled} >
  <option value="1">Pessoa</option>
  <option value="2">Lota��o</option>
  <option value="3">Fun��o</option>
  <option value="4">Cargo</option>
  <option value="5">Grupo</option>
</select>

<span id="spanPessoa${nomeSelPessoa}">
	<siga:selecao propriedade="pessoa" tipo="pessoa" tema="simple" modulo="siga" inputName="${nomeSelPessoa}" 
		 urlAcao="buscar" desativar="${desativar}" siglaInicial="${valuePessoa}"/>
</span>

<span id="spanLotacao${nomeSelLotacao}">
	<siga:selecao propriedade="lotacao" tipo="lotacao" tema="simple" modulo="siga" inputName="${nomeSelLotacao}" 
		 urlAcao="buscar" desativar="${desativar}" siglaInicial="${valueLotacao}"/>
</span>

<span id="spanFuncao${nomeSelFuncao}">
	<siga:selecao propriedade="funcao" tipo="funcao" tema="simple" modulo="siga" inputName="${nomeSelFuncao}" 
		 urlAcao="buscar" desativar="${desativar}" siglaInicial="${valueFuncao}"/>
</span>

<span id="spanCargo${nomeSelCargo}">
	<siga:selecao propriedade="cargo" tipo="cargo" tema="simple" modulo="siga" inputName="${nomeSelCargo}" 
		 urlAcao="buscar" desativar="${desativar}" siglaInicial="${valueCargo}"/>
</span>

<span id="spanGrupo${nomeSelGrupo}">
	<siga:selecao propriedade="perfil" tipo="perfil" tema="simple" modulo="siga" prefix="gi" inputName="${nomeSelGrupo}" 
		 urlAcao="buscar" desativar="${desativar}" siglaInicial="${valueGrupo}"/>
</span>


<script language="javascript">

var select = document.getElementById('${nomeSelPessoa}${nomeSelLotacao}${nomeSelFuncao}${nomeSelCargo}${nomeSelGrupo}');

// O onchange tem de ser definido da forma abaixo porque, quando esta tag está dentro de um código
// carregado por ajax, não funciona o tratamento do modo tradicional (onchange="", etc)
// http://stackoverflow.com/questions/8893786/uncaught-referenceerror-x-is-not-defined
function limparPessoa() {
	document.getElementById('spanPessoa${nomeSelPessoa}').style.display = 'none';
	document.getElementById('formulario_${nomeSelPessoa}_pessoaSel_sigla').value='';
	document.getElementById('formulario_${nomeSelPessoa}_pessoaSel_descricao').value='';
	document.getElementById('pessoa_pessoaSelSpan').innerHTML='';
}

function limparLotacao() { 
	document.getElementById('spanLotacao${nomeSelLotacao}').style.display = 'none';
	document.getElementById('formulario_${nomeSelLotacao}_lotacaoSel_sigla').value='';
	document.getElementById('formulario_${nomeSelLotacao}_lotacaoSel_descricao').value='';
	document.getElementById('lotacao_lotacaoSelSpan').innerHTML='';
}

function limparFuncao() {
	document.getElementById('spanFuncao${nomeSelFuncao}').style.display = 'none';
	document.getElementById('formulario_${nomeSelFuncao}_funcaoSel_sigla').value='';
	document.getElementById('formulario_${nomeSelFuncao}_funcaoSel_descricao').value='';
	document.getElementById('funcao_funcaoSelSpan').innerHTML='';
}

function limparCargo() {
	document.getElementById('spanCargo${nomeSelCargo}').style.display = 'none';
	document.getElementById('formulario_${nomeSelCargo}_cargoSel_sigla').value='';
	document.getElementById('formulario_${nomeSelCargo}_cargoSel_descricao').value='';
	document.getElementById('cargo_cargoSelSpan').innerHTML='';
}

function limparGrupo() {
	document.getElementById('spanGrupo${nomeSelGrupo}').style.display = 'none';
	document.getElementById('formulario_${nomeSelGrupo}_perfilSel_sigla').value='';
	document.getElementById('formulario_${nomeSelGrupo}_perfilSel_descricao').value='';
	document.getElementById('perfil_perfilSelSpan').innerHTML='';
}

select.onchange = function(){
	var select = document.getElementById('${nomeSelPessoa}${nomeSelLotacao}${nomeSelFuncao}${nomeSelCargo}${nomeSelGrupo}');

	if (select.value == '1'){
		document.getElementById('spanPessoa${nomeSelPessoa}').style.display = 'inline';
		limparLotacao();
		limparFuncao();
		limparCargo();
		limparGrupo();
	} else if (select.value == '2'){
		document.getElementById('spanLotacao${nomeSelLotacao}').style.display = 'inline';
		limparPessoa();
		limparFuncao();
		limparCargo();
		limparGrupo();
	} else if (select.value == '3'){
		document.getElementById('spanFuncao${nomeSelFuncao}').style.display = 'inline';
		limparPessoa();
		limparLotacao();
		limparCargo();
		limparGrupo();
	} else if (select.value == '4'){
		document.getElementById('spanCargo${nomeSelCargo}').style.display = 'inline';
		limparPessoa();
		limparLotacao();
		limparFuncao();
		limparGrupo();
	} else if (select.value == '5'){
		document.getElementById('spanGrupo${nomeSelGrupo}').style.display = 'inline';
		limparPessoa();
		limparLotacao();
		limparFuncao();
		limparCargo();
	}
}

select.changeValue = function(newValue) {
	select.value = newValue;
	select.onchange();
}
select.onchange();
</script>