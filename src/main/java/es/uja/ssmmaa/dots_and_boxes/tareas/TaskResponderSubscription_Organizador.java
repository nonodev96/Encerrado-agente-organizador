/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.agentes.AgenteOrganizador;
import es.uja.ssmmaa.dots_and_boxes.interfaces.TasksOrganizadorSub;
import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;
import es.uja.ssmmaa.dots_and_boxes.util.MessageSubscription;
import es.uja.ssmmaa.dots_and_boxes.util.GsonUtil;

import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.proto.SubscriptionResponder;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.core.Agent;

/**
 *
 * @author nono_
 */
public class TaskResponderSubscription_Organizador extends SubscriptionResponder {

    private Subscription subscription;
    // Interfaz
    private final AgenteOrganizador myAgent_Organizador;

    public TaskResponderSubscription_Organizador(Agent a, MessageTemplate mt, SubscriptionManager sm) {
        super(a, mt, sm);
        this.myAgent_Organizador = (AgenteOrganizador) a;
    }

    @Override
    protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
        // Eliminamos la suscripción del agente jugador
        String nombreAgente = cancel.getSender().getName();

        try {
            this.subscription = this.myAgent_Organizador.getGestor().getSubscripcion(nombreAgente);
            this.mySubscriptionManager.deregister(this.subscription);
        } catch (FailureException e) {
            this.myAgent_Organizador.addMsgConsole("Error al cancelar la subscripción para " + nombreAgente);
        }

        // Mensaje de cancelación
        ACLMessage cancela = cancel.createReply();
        this.myAgent_Organizador.addMsgConsole("Subscripción cancelada en " + this.myAgent_Organizador.getLocalName() + " para " + nombreAgente);
        //agente.setMensaje(msgConsola);
        cancela.setPerformative(ACLMessage.INFORM);
        cancela.setSender(this.myAgent_Organizador.getAID());

        return cancela;
    }

    @Override
    protected ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
        String nombreAgente = subscription.getSender().getName();

        try {
            // Registra la suscripción si no hay una previa
            if (!this.myAgent_Organizador.getGestor().haySubscripcion(nombreAgente)) {
                this.myAgent_Organizador.addMsgConsole("Subscripción creada en " + this.myAgent.getLocalName() + " para " + nombreAgente);
                this.subscription = createSubscription(subscription);
                this.mySubscriptionManager.register(this.subscription);
            } else {
                this.myAgent_Organizador.addMsgConsole("Subscripción NO creada en " + this.myAgent.getLocalName() + " para " + nombreAgente);
            }
        } catch (Exception e) {
            this.myAgent_Organizador.addMsgConsole("Error al registrar la subscripción en " + this.myAgent_Organizador.getLocalName());
        }

        // Responde afirmativamente a la suscripción
        ACLMessage agree = subscription.createReply();
        agree.setPerformative(ACLMessage.AGREE);

        return agree;
    }
}
