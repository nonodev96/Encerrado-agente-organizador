/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.interfaces;

import jade.content.ContentElement;
import jade.core.AID;
import jade.proto.SubscriptionInitiator;

/**
 *
 * @author nono_
 */
public interface TasksOrganizadorSub extends TasksOrganizador {

    public void addSubscription(AID agente, SubscriptionInitiator sub);

    public void cancelSubscription(AID agente);

    // ?
//    public void setResultado(AID agenteOrganizador, ContentElement resultado);

}
