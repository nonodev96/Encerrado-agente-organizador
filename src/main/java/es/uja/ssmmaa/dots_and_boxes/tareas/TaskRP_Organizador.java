/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeResponder;

/**
 *
 * @author nonodev96
 */
public class TaskRP_Organizador extends ProposeResponder {

    public TaskRP_Organizador(Agent a, MessageTemplate mt) {
        super(a, mt);
        System.out.println("        --> ProposeResponder(Agent a, MessageTemplate mt)");
    }

    public TaskRP_Organizador(Agent a, MessageTemplate mt, DataStore store) {
        super(a, mt, store);
        System.out.println("        --> ProposeResponder(Agent a, MessageTemplate mt, DataStore store)");
    }

    @Override
    protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
        System.out.println("        --> prepareResponse");
        ACLMessage reply = propose.createReply();

        String content = propose.getContent();
        System.out.println("content: " + content);

        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

        return reply;
    }

}
