/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.interfaces.SendMessagesInform;
import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;
import es.uja.ssmmaa.dots_and_boxes.util.GsonUtil;
import es.uja.ssmmaa.ontologia.juegoTablero.SubInform;

import jade.proto.SubscriptionResponder.Subscription;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.Agent;
import java.util.List;

/**
 *
 * @author nono_
 */
public class TaskSendNotifications_Organizador extends TickerBehaviour {

    private final SendMessagesInform<SubInform> agente;
    private final GsonUtil<SubInform> gsonUtil;

    public TaskSendNotifications_Organizador(Agent a, long period) {
        super(a, period);
        this.agente = (SendMessagesInform) a;
        this.gsonUtil = new GsonUtil();
    }

    @Override
    protected void onTick() {
        GestorSubscripciones gestor = this.agente.getGestor();
        List<SubInform> mensajes = this.agente.getMessagesInform();
        // Hay mensajes pendientes y subscripciones activas
        if (!gestor.isEmpty() && !mensajes.isEmpty()) {
            // Para todas las subscripciones activas
            for (Subscription subscripcion : gestor.values()) {
                enviar(subscripcion, mensajes);
            }
        }

        mensajes.clear();
    }

    private void enviar(Subscription subscription, List<SubInform> messages) {
        GestorSubscripciones gestor = this.agente.getGestor();
        this.agente.addMsgConsole("Enviando notificaciones a " + subscription.getMessage().getSender().getLocalName());
        for (SubInform content : messages) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent(this.gsonUtil.encode(content, SubInform.class));
            // Enviamos el mensaje al suscriptor
            subscription.notify(msg);
        }
    }

}
