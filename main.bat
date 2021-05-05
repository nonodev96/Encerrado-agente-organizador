@echo off
title This is your first batch script!
echo Welcome to batch scripting!
java -jar .\target\Pr2-jar-with-dependencies.jar -cp jade.Boot -gui -agents Mercado:es.uja.ssmmaa.pr2.agentes.AgentMarket;Comprador_1:es.uja.ssmmaa.pr2.agentes.AgentBuyer;Comprador_2:es.uja.ssmmaa.pr2.agentes.AgentBuyer;Vendedor_1:es.uja.ssmmaa.pr2.agentes.AgentSeller;Vendedor_2:es.uja.ssmmaa.pr2.agentes.AgentSeller


pause

