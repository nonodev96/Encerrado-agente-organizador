/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.project;

import es.uja.ssmmaa.ontologia.juegoTablero.ClasificacionJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.IncidenciaJuego;
import jade.core.AID;
import jade.util.leap.List;

/**
 *
 * @author nono_
 */
public class Partida_Organizador {

    public AID agentMonitor;

    public ClasificacionJuego clasificacionJuego;

    public IncidenciaJuego incidenciaJuego;

    public List listaJugadores;
    
    public int maxRondas;

}
