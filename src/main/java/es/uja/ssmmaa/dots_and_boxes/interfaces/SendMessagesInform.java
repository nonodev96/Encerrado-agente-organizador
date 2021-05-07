/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.interfaces;

import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;
import java.util.List;

/**
 *
 * @author nono_
 */
public interface SendMessagesInform<T> extends MessageInform<T> {

    public GestorSubscripciones getGestor();

    public List<T> getMessagesInform();

    public void addMsgConsole(String msg);

}
