create DATABASE coreapi;
create DATABASE creditservice;
create DATABASE userservice;
create DATABASE transactionservice;
create DATABASE preferencesservice;

GRANT ALL PRIVILEGES ON DATABASE coreapi TO c417110f943e48c5a62d45633bdf9ea2;
GRANT ALL PRIVILEGES ON DATABASE creditservice TO c417110f943e48c5a62d45633bdf9ea2;
GRANT ALL PRIVILEGES ON DATABASE userservice TO c417110f943e48c5a62d45633bdf9ea2;
GRANT ALL PRIVILEGES ON DATABASE transactionservice TO c417110f943e48c5a62d45633bdf9ea2;
GRANT ALL PRIVILEGES ON DATABASE preferencesservice TO c417110f943e48c5a62d45633bdf9ea2;

-- Не рабочее решение т.к таблички в которые они заносятся создаются на этапе включения приложений, т.е их там нет что приводит к ошибкам
INSERT INTO public.card_account (id,currency,deleted,is_main,money,name,user_id) VALUES ('18f35126-66c0-4997-9922-39c70e924634'::uuid,'RUB',false,false,100000.00,'MASTER_CARD_ACCOUNT','b4d01963-ac7a-4644-923f-1cb66a17c157'::uuid);

INSERT INTO public.users (id,email,is_active,name,"password") VALUES
    ('b4d01963-ac7a-4644-923f-1cb66a17c157'::uuid,'st@rin.g',true,'master','$2a$10$Vx9BwO.5/Z0XN/p0aNBmWetlW3bIj/zw5VUBIaxtEfVHsaV0x6x8a');
INSERT INTO public.user_roles (user_id,roles) VALUES
    ('b4d01963-ac7a-4644-923f-1cb66a17c157'::uuid,'WORKER');
