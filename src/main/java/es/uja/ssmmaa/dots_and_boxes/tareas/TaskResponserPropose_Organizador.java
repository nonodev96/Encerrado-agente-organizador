/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.agentes.AgenteOrganizador;

import jade.content.Concept;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeResponder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nono_
 */
public class TaskResponserPropose_Organizador extends ProposeResponder {

    private final AgenteOrganizador myAgent_organizador;

    public TaskResponserPropose_Organizador(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.myAgent_organizador = (AgenteOrganizador) a;
        this.myAgent_organizador.addMsgConsole("        --> ProposeResponder(Agent a, MessageTemplate mt)");
    }

    @Override
    protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
        System.out.println("        --> prepareResponse");
        Action a;

        try {
            a = (Action) this.myAgent_organizador.getManager().extractContent(propose);
            Concept c = a.getAction();
            System.out.println("C : " + c.toString());
        } catch (Codec.CodecException | OntologyException ex) {
            Logger.getLogger(TaskResponserPropose_Organizador.class.getName()).log(Level.SEVERE, null, ex);
        }

        ACLMessage reply = response_completar_juego(propose);

        return reply;
    }

    /**
     * TODO
     *
     * @param propose
     * @return
     */
    private ACLMessage response_completar_juego(ACLMessage propose) {
        return propose.createReply();
    }

}
