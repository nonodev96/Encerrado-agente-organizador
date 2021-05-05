/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.util;

/**
 *
 * @author nono_
 */
public class MessageSubscription {

    String message;

    public MessageSubscription() {
        this.message = "";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageSubscription{"
                + "message=" + message
                + '}';
    }

}
