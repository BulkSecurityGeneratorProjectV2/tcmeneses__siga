-------------------------------------------
--	SCRIPT: ADICIONAR SERVI�O
-------------------------------------------

-- Adicionar servi�o para acesso a pasta de videoconfer�ncia
insert into corporativo.cp_servico (id_servico,sigla_servico,desc_servico,id_servico_pai,id_tp_servico)
      values (corporativo.cp_servico_seq.nextval,'FS-VIDEO','Acesso ao Diret�rio de Videoconfer�ncia', 
                (select cp.id_servico from corporativo.cp_servico cp where cp.sigla_servico = 'FS'), -- id_servico_pai = 5
                (select tp.id_tp_servico from corporativo.cp_tipo_servico tp where tp.desc_tp_servico = 'Diret�rio') -- id_tp_servico = 1
             );
             
-- Adicionar servi�o para acesso a pasta de audi�ncia
insert into corporativo.cp_servico (id_servico,sigla_servico,desc_servico,id_servico_pai,id_tp_servico)
      values (corporativo.cp_servico_seq.nextval,'FS-AUD','Acesso ao Diret�rio de Audi�ncia', 
              (select cp.id_servico from corporativo.cp_servico cp where cp.sigla_servico = 'FS'), -- id_servico_pai = 5
              (select tp.id_tp_servico from corporativo.cp_tipo_servico tp where tp.desc_tp_servico = 'Diret�rio') -- id_tp_servico = 1
             );
             

-------------------------------------------
--	SCRIPT: ADICIONAR CONFIGURA��O
-------------------------------------------

insert into corporativo.cp_configuracao (id_configuracao,his_dt_ini,id_sit_configuracao,id_tp_configuracao,id_servico,id_tp_lotacao)
    values (corporativo.cp_configuracao_seq.nextval,
              (select to_char(systimestamp,'dd/mm/rrrr hh24:mi:ss') from dual),
              (select sit.id_sit_configuracao from cp_situacao_configuracao sit where sit.dsc_sit_configuracao = 'Pode'),202,
              (select cp.id_servico from corporativo.cp_servico cp where cp.sigla_servico = 'FS-AUD'),
              (select tp.id_tp_lotacao from corporativo.cp_tipo_lotacao tp where tp.sigla_tp_lotacao = 'JUD')
           );  
             
insert into corporativo.cp_configuracao (id_configuracao,his_dt_ini,id_sit_configuracao,id_tp_configuracao,id_servico,id_tp_lotacao)
    values (corporativo.cp_configuracao_seq.nextval,
              (select to_char(systimestamp,'dd/mm/rrrr hh24:mi:ss') from dual),
              (select sit.id_sit_configuracao from cp_situacao_configuracao sit where sit.dsc_sit_configuracao = 'Pode'),202,
              (select cp.id_servico from corporativo.cp_servico cp where cp.sigla_servico = 'FS-VIDEO'),
              (select tp.id_tp_lotacao from corporativo.cp_tipo_lotacao tp where tp.sigla_tp_lotacao = 'JUD')
           ); 