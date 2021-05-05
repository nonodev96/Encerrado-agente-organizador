/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.project.Constantes.NombreServicio;
import jade.core.AID;

/**
 * Comportamiento necesario para que un agente pueda añadir y eliminar agentes
 * que coincidan con la suscripción establecida en el servicio de páginas
 * amarillas.
 * @author pedroj
 */
public interface SubscripcionDF {
    public void addAgent(AID agente, NombreServicio servicio);
    public boolean removeAgent(AID agente, NombreServicio servicio);
}
