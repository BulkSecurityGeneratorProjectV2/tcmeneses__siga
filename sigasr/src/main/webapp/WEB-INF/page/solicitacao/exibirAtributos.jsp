<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://localhost/jeetags" prefix="siga"%>

<script src="/sigasr/javascripts/jquery.maskedinput.min.js"></script>

<script type="text/javascript">
    carregarConhecimentosRelacionados();
    
    // Codigo para atualizacao dos filtros na tela
    removerFiltrosSemCampo();
    carregarFiltrosAtributos();
    carregarSolRelacionadas();
</script>

<c:if test="${not empty solicitacao.itemConfiguracao && not empty solicitacao.acao && podeUtilizarServicoSigaGC}">
    <div style="display: inline-block" >
        <div id="gc-ancora-item-acao"></div>
    </div>
    <script type="text/javascript">
        var url = "/../sigagc/app/knowledge?tags=${solicitacao.gcTagAbertura}&testarAcesso=true&estilo=inplace&popup=true&podeCriar=${exibirMenuConhecimentos}&msgvazio=&titulo=${solicitacao.gcTituloAbertura}";
        Siga.ajax(url, null, "GET", function(response){
            document.getElementById('gc-ancora-item-acao').innerHTML = response;
        });
    </script>
</c:if>

<script type="text/javascript"> 
    carregarConhecimentosRelacionados();
</script>

<div id="atributos">
    <c:forEach items="${solicitacao.atributoAssociados}" var="atributo">
        <div class="gt-form-row gt-width-66">
            <label>
                ${atributo.nomeAtributo} 
                <c:if test="${atributo.descrAtributo != null && atributo.descrAtributo != ''}">
                    (${atributo.descrAtributo})
                </c:if>
            </label>
            <c:if test="${atributo.tipoAtributo != null}">
                <c:if test="${atributo.tipoAtributo.name() == 'TEXTO'}">
                    <input type="text" name="solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]" value="${solicitacao.atributoSolicitacaoMap[atributo.idAtributo]}" class="${atributo.idAtributo}"
                        onchange="notificarCampoAtributoMudou('.${atributo.idAtributo}', '${atributo.nomeAtributo}', 'solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]');" size="70" maxlength="255" />
<%--                     <span style="color: red">#{error 'solicitacao.atributoSolicitacaoMap['+atributo.idAtributo+']' /}</span> --%>
                </c:if>
                <c:if test="${atributo.tipoAtributo.name() == 'TEXT_AREA'}">
                    <textarea cols="85" rows="10" name="solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]" class="${atributo.idAtributo}"
                        onchange="notificarCampoAtributoMudou('.${atributo.idAtributo}', '${atributo.nomeAtributo}', 'solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]');" maxlength="255">${solicitacao.atributoSolicitacaoMap[atributo.idAtributo]}</textarea>
<%--                     <span style="color: red">#{error 'solicitacao.atributoSolicitacaoMap['+atributo.idAtributo+']' /}</span> --%>
                </c:if>
                <c:if test="${atributo.tipoAtributo.name() == 'DATA'}">
                    <siga:dataCalendar nome="solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]" id="calendarioAtributo${atributo.idAtributo}"
                        value="${solicitacao.atributoSolicitacaoMap[atributo.idAtributo]}" onchange="notificarCampoAtributoMudou('.${atributo.idAtributo}', '${atributo.nomeAtributo}', 'solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}];')"
                        cssClass="${atributo.idAtributo}"/>
<%--                     <span style="color: red">#{error 'solicitacao.atributoSolicitacaoMap['+atributo.idAtributo+']' /}</span> --%>
                </c:if>
                <c:if test="${atributo.tipoAtributo.name() == 'NUM_INTEIRO'}">
                    <input type="text" class="${atributo.idAtributo}"
                        onkeypress="javascript: var tecla=(window.event)?event.keyCode:e.which;if((tecla>47 && tecla<58)) return true;  else{  if (tecla==8 || tecla==0) return true;  else  return false;  }"
                        onchange="notificarCampoAtributoMudou('.${atributo.idAtributo}', '${atributo.nomeAtributo}', 'solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]');"
                        name="solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]" value="${solicitacao.atributoSolicitacaoMap[atributo.idAtributo]}" maxlength="9"/>
<%--                     <span style="color: red">#{error 'solicitacao.atributoSolicitacaoMap['+atributo.idAtributo+']' /}</span> --%>
                </c:if>
                <c:if test="${atributo.tipoAtributo.name() == 'NUM_DECIMAL'}">
                    <input type="text" name="solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]" value="${solicitacao.atributoSolicitacaoMap[atributo.idAtributo]}" 
                        id="numDecimal" pattern="^\d*(\,\d{2}$)?" title="Somente n�mero e com duas casas decimais EX: 222,22" class="${atributo.idAtributo}"
                        onchange="notificarCampoAtributoMudou('.${atributo.idAtributo}', '${atributo.nomeAtributo}', 'solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]');" maxlength="9"/>
<%--                     <span style="color: red">#{error 'solicitacao.atributoSolicitacaoMap['+atributo.idAtributo+']' /}</span> --%>
                </c:if>
                <c:if test="${atributo.tipoAtributo.name() == 'HORA'}">
                    <input type="text" name="solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]" value="${solicitacao.atributoSolicitacaoMap[atributo.idAtributo]}" id="horarioAtributo${atributo.idAtributo}" class="${atributo.idAtributo}"
                        onchange="notificarCampoAtributoMudou('.${atributo.idAtributo}', '${atributo.nomeAtributo}', 'solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]');" />
<%--                     <span style="color: red">#{error 'solicitacao.atributoSolicitacaoMap['+atributo.idAtributo+']' /}</span> --%>
                    <span style="color: red; display: none;" id="erroHoraAtributo${atributo.idAtributo}">Hor�rio inv�lido</span>
                    <script>
                        $(function() {
                            $("#horarioAtributo${atributo.idAtributo}").mask("99:99");
                            $("#horarioAtributo${atributo.idAtributo}").blur(function() {
                                var hora = this.value;
                                var array = hora.split(':');
                                if (array[0] > 23 || array[1] > 59) {
                                    $('#erroHoraAtributo${atributo.idAtributo}').show(); 
                                    return;
                                }
                                $('#erroHoraAtributo${atributo.idAtributo}').hide();    
                            });
                        });
                    </script>
                </c:if>
                <c:if test="${atributo.tipoAtributo.name() == 'VL_PRE_DEFINIDO'}" >
                    <select name="solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]" value="${solicitacao.atributoSolicitacaoMap[atributo.idAtributo]}" class="${atributo.idAtributo}"
                         onchange="notificarCampoAtributoMudou('.${atributo.idAtributo}','${atributo.nomeAtributo}', 'solicitacao.atributoSolicitacaoMap[${atributo.idAtributo}]');"} >
                        <c:forEach items="${atributo.preDefinidoSet}" var="valorAtributoSolicitacao">
                            <option value="${valorAtributoSolicitacao}">
                                ${valorAtributoSolicitacao}
                            </option>
                        </c:forEach>
                    </select>
                </c:if>
            </c:if>
        </div>
    </c:forEach>
</div>