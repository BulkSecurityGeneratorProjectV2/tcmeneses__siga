--Cria campos denormalizados para armazenar as informa��es de controle de acesso

alter table siga.ex_documento add (DNM_DT_ACESSO date null);
alter table siga.ex_documento add (DNM_ACESSO varchar2(256) null);