/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;
import es.uja.ssmmaa.dots_and_boxes.util.GsonUtil;
import es.uja.ssmmaa.dots_and_boxes.util.MessageSubscription;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;

/**
 *
 * @author nono_
 */
public class TaskSubscriptionResponder extends SubscriptionResponder {

    private Subscription subscription;
    // Interfaz
    private final TasksOrganizadorSub agente;

    private final MessageSubscription infMessageSubscription;
    private final GsonUtil<MessageSubscription> gsonUtil;
    private final GestorSubscripciones gestor;

    public TaskSubscriptionResponder(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agente = (TasksOrganizadorSub) a;
        this.gsonUtil = new GsonUtil<>();
        this.gestor = this.agente.getGestor();
        this.infMessageSubscription = new MessageSubscription();
    }

    protected ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
        String nombreAgente = subscription.getSender().getName();

        try {
            // Registra la suscripción si no hay una previa
            if (!this.gestor.haySubscripcion(nombreAgente)) {
                this.subscription = createSubscription(subscription);
                this.mySubscriptionManager.register(this.subscription);
            }
        } catch (Exception e) {
            this.agente.addMsgConsola("Error al registrar la subscripción en " + this.myAgent.getLocalName());
            throw new RefuseException(this.gsonUtil.encode(this.infMessageSubscription, MessageSubscription.class));
        }

        // Responde afirmativamente a la suscripción
        ACLMessage agree = subscription.createReply();
        this.agente.addMsgConsola("Subscripción creada en " + this.myAgent.getLocalName() + " para " + nombreAgente);

        agree.setPerformative(ACLMessage.AGREE);
        agree.setContent(this.gsonUtil.encode(this.infMessageSubscription, MessageSubscription.class));
        return agree;
    }

    protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
        //return super.handleCancel(cancel); //To change body of generated methods, choose Tools | Templates.
        System.out.println("handleCancel");

        // Eliminamos la suscripción del agente jugador
        String nombreAgente = cancel.getSender().getName();

        try {
            this.subscription = gestor.getSubscripcion(nombreAgente);
            this.mySubscriptionManager.deregister(this.subscription);
        } catch (Exception e) {
            this.infMessageSubscription.setMessage("Error al cancelar la subscripción para " + nombreAgente);
            throw new FailureException(this.gsonUtil.encode(this.infMessageSubscription, MessageSubscription.class));
        }

        // Mensaje de cancelación
        ACLMessage cancela = cancel.createReply();
        this.agente.addMsgConsola("Subscripción cancelada en " + this.myAgent.getLocalName() + " para " + nombreAgente);
        //agente.setMensaje(msgConsola);
        cancela.setPerformative(ACLMessage.INFORM);
        cancela.setSender(this.myAgent.getAID());
        cancela.setContent(this.gsonUtil.encode(this.infMessageSubscription, MessageSubscription.class));
        return cancela;
    }
}
