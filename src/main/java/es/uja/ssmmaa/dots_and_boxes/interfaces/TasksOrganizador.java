/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.interfaces;

import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;
import es.uja.ssmmaa.dots_and_boxes.util.Partida_Organizador;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.core.Agent;

/**
 *
 * @author nono_
 */
public interface TasksOrganizador {

    public ContentManager getManager();

    public Ontology getOntology();

    public Codec getCodec();

    public GestorSubscripciones getGestor();

    public void addMsgConsole(String msg);
    
    public Partida_Organizador getPartida(String idPartida);
}
