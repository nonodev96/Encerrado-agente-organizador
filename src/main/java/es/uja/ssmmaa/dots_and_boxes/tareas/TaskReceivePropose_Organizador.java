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
 * @author nono_
 */
public class TaskReceivePropose_Organizador extends ProposeResponder {

    public TaskReceivePropose_Organizador(Agent a, MessageTemplate mt) {
        super(a, mt);
    }

    public TaskReceivePropose_Organizador(Agent a, MessageTemplate mt, DataStore store) {
        super(a, mt, store);
    }

    @Override
    protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
        return super.prepareResponse(propose); //To change body of generated methods, choose Tools | Templates.
    }

}
