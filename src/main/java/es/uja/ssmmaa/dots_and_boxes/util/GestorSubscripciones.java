/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.util;

import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.Subscription;
import jade.proto.SubscriptionResponder.SubscriptionManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pedroj
 */
public class GestorSubscripciones implements SubscriptionManager {
    private final Map<String, Subscription> subscripciones;

    public GestorSubscripciones() {
        subscripciones = new HashMap();
    }

    @Override
    public boolean register(SubscriptionResponder.Subscription s) throws RefuseException, NotUnderstoodException {
        // Guardamos la suscripción asociada al agente que la solita
        String nombreAgente = s.getMessage().getSender().getName();
        subscripciones.put(nombreAgente, s);
        return true;
    }

    @Override
    public boolean deregister(SubscriptionResponder.Subscription s) throws FailureException {
        // Eliminamos la suscripción asociada a un agente
        String nombreAgente = s.getMessage().getSender().getName();
        subscripciones.remove(nombreAgente);
        s.close(); // queda cerrada la suscripción
        return true;
    }
    
    public Subscription getSubscripcion( String nombreAgente ) {
        return subscripciones.get(nombreAgente);
    }
    
    public Collection<Subscription> values() {
        return subscripciones.values();
    }
    
    public boolean haySubscripcion( String nombreAgente ) {
        return subscripciones.get(nombreAgente) != null;
    }
    
    public boolean isEmpty() {
        return subscripciones.isEmpty();
    }
    
    public int size() {
        return subscripciones.size();
    }
}
