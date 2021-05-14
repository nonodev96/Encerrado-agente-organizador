/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.interfaces;

import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;

import jade.content.ContentManager;
import jade.content.onto.Ontology;
import jade.core.Agent;

/**
 *
 * @author nono_
 */
public interface TasksOrganizador {

    public Ontology getOntology();

    public ContentManager getManager();

    public GestorSubscripciones getGestor();

    public void addMsgConsole(String msg);

}
